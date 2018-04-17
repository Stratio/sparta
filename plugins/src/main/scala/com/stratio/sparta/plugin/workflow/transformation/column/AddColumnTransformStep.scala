/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.column

import java.io.{Serializable => JSerializable}
import scala.util.{Failure, Success, Try}

import akka.event.slf4j.SLF4JLogging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import com.stratio.sparta.plugin.helper.SchemaHelper.{createOrReplaceTemporalViewDf, getSchemaFromSessionOrModelOrRdd, parserInputSchema}
import com.stratio.sparta.plugin.models.PropertyQuery
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{ErrorValidations, OutputOptions, TransformStep, TransformationStepManagement}

//scalastyle:off
abstract class AddColumnTransformStep[Underlying[Row]](
                                                                  name: String,
                                                                  outputOptions: OutputOptions,
                                                                  transformationStepsManagement: TransformationStepManagement,
                                                                  ssc: Option[StreamingContext],
                                                                  xDSession: XDSession,
                                                                  properties: Map[String, JSerializable]
                                                                )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  lazy val columns: Seq[PropertyQuery] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val cols = s"${properties.getString("columns", None).notBlank.fold("[]") { values => values.toString }}"

    read[Seq[PropertyQuery]](cols)
  }

  lazy val newColumnsSchemaTypes: Map[String, Any] = columns.map { outputField =>
    val outputType = outputField.`type`.notBlank.getOrElse("string")
    val schema = StructField(
      name = outputField.field,
      dataType = SparkTypes.get(outputType) match {
        case Some(sparkType) => sparkType
        case None => schemaFromString(outputType)
      },
      nullable = true
    )
    outputField.field -> castingToOutputSchema(schema, outputField.query)
  }.toMap

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the step name $name is not valid")

    //If contains schemas, validate if it can be parsed
    if (inputsModel.inputSchemas.nonEmpty) {
      inputsModel.inputSchemas.foreach { input =>
        if (parserInputSchema(input.schema).isFailure)
          validation = ErrorValidations(
            valid = false,
            messages = validation.messages :+ s"$name: the input schema from step ${input.stepName} is not valid")
      }

      inputsModel.inputSchemas.filterNot(is => SdkSchemaHelper.isCorrectTableName(is.stepName)).foreach { is =>
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ s"$name: the input table name ${is.stepName} is not valid")
      }
    }

    if (Try(newColumnsSchemaTypes.nonEmpty).isFailure) {
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the default values for types are invalid")
    }

    if (columns.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: it's mandatory at least one column"
      )

    columns.filter(c => c.field.isEmpty || c.field.contains(" ")).foreach(col => validation = ErrorValidations(
      valid = false,
      messages = validation.messages :+ s"$name: new column name is not valid"
    ))

    columns.filter(c => c.query.isEmpty).foreach(col => validation = ErrorValidations(
      valid = false,
      messages = validation.messages :+ s"$name: new column value is not valid"
    ))

    validation
  }

  def applyValues(rdd: RDD[Row], inputStep: String): (RDD[Row], Option[StructType]) = {
    Try {
      val schema = getSchemaFromSessionOrModelOrRdd(xDSession, inputStep, inputsModel, rdd)
      createOrReplaceTemporalViewDf(xDSession, rdd, inputStep, schema) match {
        case Some(df) => {
          val newDataFrame = columns.foldLeft(df) {
            (df, defValue) => {
              import org.apache.spark.sql.functions._
              df.withColumn(defValue.field, lit(newColumnsSchemaTypes.get(defValue.field).getOrElse("")))
            }
          }
          (newDataFrame.rdd, Option(newDataFrame.schema))
        }
        case None =>
          (rdd.filter(_ => false), None)
      }
    } match {
      case Success(sqlResult) => sqlResult
      case Failure(e) => (rdd.map(_ => Row.fromSeq(throw e)), None)
    }
  }
}