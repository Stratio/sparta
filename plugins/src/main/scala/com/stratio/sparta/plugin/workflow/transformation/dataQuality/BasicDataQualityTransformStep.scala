/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.dataQuality

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.enumerations.DataZone.DataZone
import com.stratio.sparta.plugin.enumerations.{DataZone, ProfilingType}
import com.stratio.sparta.plugin.enumerations.ProfilingType.ProfilingType
import com.stratio.sparta.plugin.helper.SchemaHelper._
import com.stratio.sparta.plugin.helper.SparkStepHelper
import com.stratio.sparta.plugin.models.{BasicDataQualityAnalysis, PropertyColumn}
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper
import com.stratio.sparta.sdk.models._
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.TransformStep
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}

abstract class BasicDataQualityTransformStep[Underlying[Row]](
                                                     name: String,
                                                     outputOptions: OutputOptions,
                                                     transformationStepsManagement: TransformationStepManagement,
                                                     ssc: Option[StreamingContext],
                                                     xDSession: XDSession,
                                                     properties: Map[String, JSerializable]
                                                   )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  lazy val path: Option[String] = properties.getString("path", None).notBlank
  lazy val addExecutionDate: Boolean = Try(properties.getBoolean("addExecutionDate")).getOrElse(true)
  lazy val columns: Seq[BasicDataQualityAnalysis] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val cols = s"${properties.getString("columns", None).notBlank.fold("[]") { values => values.toString }}"

    read[Seq[BasicDataQualityAnalysis]](cols)
  }
  lazy val dataZone: DataZone = Try {
    DataZone.withName(properties.getString("dataZone", "MASTER").toUpperCase())
  }.getOrElse(DataZone.MASTER)

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

    if (columns.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"It's mandatory to specify at least one column", name)
      )

    validation
  }

  def applyQuality(rdd: RDD[Row], inputStep: String): (RDD[Row], Option[StructType], Option[StructType]) = {
    Try {
      val inputSchema = getSchemaFromSessionOrModelOrRdd(xDSession, inputStep, inputsModel, rdd)
      createOrReplaceTemporalViewDf(xDSession, rdd, inputStep, inputSchema) match {
        case Some(_) =>
          import org.apache.spark.sql.functions._

          val totalValues = rdd.count()
          val totalValuesExpression = if(totalValues != 0){
            s"round(count(*)/$totalValues*100, 2)"
          } else s"lit(0.0)"
          val queryResults = columns.flatMap { column =>
            val nullDf = if(column.nullAnalysis) {
              val query = s"select count(*) as outputData, $totalValuesExpression as percentage from $inputStep where ${column.name} is not null"
              Seq(xDSession.sql(query)
                .withColumn("column", lit(column.name))
                .withColumn("operation", lit("notNull"))
              )
            } else Seq.empty
            val characterDf = if(column.characters.notBlank.isDefined){
              val charactersLike = column.characters.get.split(",").map(character => s"${column.name} not like '%${character.trim}%'").mkString(" AND ")
              val query = s"select count(*) as outputData, $totalValuesExpression as percentage from $inputStep where $charactersLike"
              Seq(xDSession.sql(query)
                .withColumn("column", lit(column.name))
                .withColumn("operation", lit("invalidCharacters"))
              )
            } else Seq.empty

            nullDf ++ characterDf
          }
          val nullSchema = queryResults.head.schema
          val unionResults = xDSession.sparkContext.union(queryResults.map(_.rdd))
          val dfResults = xDSession.createDataFrame(unionResults, nullSchema)
            .withColumn("zone", lit(dataZone.toString))
            .withColumn("tableName", lit(inputStep))
            .withColumn("inputData", lit(totalValues))
          val dfWithPath = path.fold(dfResults) {pathToAdd => dfResults.withColumn("path", lit(pathToAdd))}
          val dfWithDate = {
            if(addExecutionDate)
              dfWithPath.withColumn("executionDate", current_date())
            else dfWithPath
          }

          (dfWithDate.rdd, Option(dfWithDate.schema), inputSchema)
        case None =>
          (rdd.filter(_ => false), None, inputSchema)
      }
    } match {
      case Success(sqlResult) => sqlResult
      case Failure(e) => (SparkStepHelper.failRDDWithException(rdd, e), None, None)
    }
  }
}

