/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.dataProfiling

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.enumerations.ProfilingType
import com.stratio.sparta.plugin.enumerations.ProfilingType.ProfilingType
import com.stratio.sparta.plugin.helper.SchemaHelper._
import com.stratio.sparta.plugin.helper.SparkStepHelper
import com.stratio.sparta.plugin.models.PropertyColumn
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.TransformStep
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}

abstract class DataProfilingTransformStep[Underlying[Row]](
                                                     name: String,
                                                     outputOptions: OutputOptions,
                                                     transformationStepsManagement: TransformationStepManagement,
                                                     ssc: Option[StreamingContext],
                                                     xDSession: XDSession,
                                                     properties: Map[String, JSerializable]
                                                   )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  lazy val columnName: Option[String] = properties.getString("columnName", None).notBlank
  lazy val limitResults: String = properties.getString("limitResults", None).notBlank.getOrElse("10")
  lazy val columns: Seq[String] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val cols = s"${properties.getString("columns", None).notBlank.fold("[]") { values => values.toString }}"

    read[Seq[PropertyColumn]](cols).map(_.name)
  }
  lazy val profilingType: ProfilingType = Try {
    ProfilingType.withName(properties.getString("analysisType", "DATASET").toUpperCase())
  }.getOrElse(ProfilingType.DATASET)

  //scalastyle:off
  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The step name $name is not valid", name))

    //If contains schemas, validate if it can be parsed
    if (inputsModel.inputSchemas.nonEmpty) {
      inputsModel.inputSchemas.foreach { input =>
        if (parserInputSchema(input.schema).isFailure)
          validation = ErrorValidations(
            valid = false,
            messages = validation.messages :+ WorkflowValidationMessage(s"The input schema from step ${input.stepName} is not valid", name))
      }

      inputsModel.inputSchemas.filterNot(is => SdkSchemaHelper.isCorrectTableName(is.stepName)).foreach { is =>
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ WorkflowValidationMessage(s"The input table name ${is.stepName} is not valid", name))
      }
    }

    if (profilingType == ProfilingType.COLUMNS && columns.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"It's mandatory to specify at least one column", name)
      )

    if (profilingType == ProfilingType.COLUMN && columnName.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"It's mandatory to set the column name", name)
      )

    validation
  }

  def applyProfiling(rdd: RDD[Row], inputStep: String): (RDD[Row], Option[StructType], Option[StructType]) = {
    Try {
      val inputSchema = getSchemaFromSessionOrModelOrRdd(xDSession, inputStep, inputsModel, rdd)
      createOrReplaceTemporalViewDf(xDSession, rdd, inputStep, inputSchema) match {
        case Some(df) =>
          import org.apache.spark.sql.functions._

          val newDataFrame = profilingType match {
            case ProfilingType.DATASET =>
              val query = s"select count(*) as value from $inputStep"

              xDSession.sql(query).withColumn("aggregate", lit("count"))
            case ProfilingType.COLUMNS =>
              val queryResults = columns.map { column =>
                val query = s"select count(distinct($column)) as distinct, min($column) as min, max($column) as max from $inputStep"
                val result = xDSession.sql(query).withColumn("column", lit(column))

                if(df.schema.fieldNames.contains(column))
                  result.withColumn("columnType", lit(df.schema.fields.find(field => field.name == column).get.dataType.simpleString))
                else result
              }
              val schema = queryResults.head.schema
              val unionResults = xDSession.sparkContext.union(queryResults.map(_.rdd))
              val dfResults = xDSession.createDataFrame(unionResults, schema)
              val notNullResults = columns.map { column =>
                val query = s"select count(*) as notnull from $inputStep where $column is not null"
                xDSession.sql(query).withColumn("column", lit(column))
              }
              val nullSchema = notNullResults.head.schema
              val unionNullResults = xDSession.sparkContext.union(notNullResults.map(_.rdd))
              val dfNullsResults = xDSession.createDataFrame(unionNullResults, nullSchema)

              dfResults.join(dfNullsResults, "column")


            case ProfilingType.COLUMN =>
              val query = s"select ${columnName.get} as value, count(*) as total from $inputStep group by value order by value DESC limit $limitResults"

              xDSession.sql(query)
          }

          (newDataFrame.rdd, Option(newDataFrame.schema), inputSchema)
        case None =>
          (rdd.filter(_ => false), None, inputSchema)
      }
    } match {
      case Success(sqlResult) => sqlResult
      case Failure(e) => (SparkStepHelper.failRDDWithException(rdd, e), None, None)
    }
  }
}

