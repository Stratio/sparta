/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.dropNulls

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.enumerations.CleanMode
import com.stratio.sparta.plugin.helper.SchemaHelper.{createOrReplaceTemporalViewDf, getSchemaFromSessionOrModelOrRdd, parserInputSchema}
import com.stratio.sparta.plugin.models.PropertyColumn
import com.stratio.sparta.plugin.enumerations.CleanMode.CleanMode
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{ErrorValidations, OutputOptions, TransformStep, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.Serialization.read

import scala.util.{Failure, Success, Try}

abstract class DropNullsTransformStep[Underlying[Row]](
                                                         name: String,
                                                         outputOptions: OutputOptions,
                                                         transformationStepsManagement: TransformationStepManagement,
                                                         ssc: Option[StreamingContext],
                                                         xDSession: XDSession,
                                                         properties: Map[String, JSerializable]
                                                       )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  lazy val columns: Seq[String] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val cols = s"${properties.getString("columns", None).notBlank.fold("[]") { values => values.toString }}"

    read[Seq[PropertyColumn]](cols).map(_.name)
  }
  lazy val cleanMode: CleanMode = Try {
    CleanMode.withName(properties.getString("cleanMode", "any").toLowerCase())
  }.getOrElse(CleanMode.any)

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the step name $name is not valid")

    if (Try(columns.nonEmpty).isFailure) {
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the input columns are not valid")
    }

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

    validation
  }

  def transformFunction(
                         inputSchema: String,
                         inputStream: DistributedMonad[Underlying]
                       ): DistributedMonad[Underlying] =
    inputStream.flatMap { row =>
      returnSeqDataFromOptionalRow {
        if (columns.isEmpty) {
          if (cleanMode == CleanMode.any && row.anyNull) None
          else if (cleanMode == CleanMode.all && row.toSeq.forall(Option(_).isEmpty)) None else Option(row)
        } else {
          val indexes = columns.map(col => row.fieldIndex(col))
          if (cleanMode == CleanMode.any && indexes.exists(index => row.isNullAt(index))) None
          else if (cleanMode == CleanMode.all && indexes.forall(index => row.isNullAt(index))) None else Option(row)
        }
      }
    }

  /* Not used function at DataFrame level

  def applyCleanNulls(
                       rdd: RDD[Row],
                       columns: Seq[String],
                       cleanMode: CleanMode,
                       inputStep: String
                     ): (RDD[Row], Option[StructType]) = {
    Try {
      val schema = getSchemaFromSessionOrModelOrRdd(xDSession, inputStep, inputsModel, rdd)
      createOrReplaceTemporalViewDf(xDSession, rdd, inputStep, schema) match {
        case Some(df) =>
          val newDataFrame = if (columns.isEmpty) df.na.drop(cleanMode.toString)
          else df.na.drop(cleanMode.toString, columns)

          (newDataFrame.rdd, Option(newDataFrame.schema))
        case None =>
          (rdd.filter(_ => false), None)
      }
    } match {
      case Success(sqlResult) => sqlResult
      case Failure(e) => (rdd.map(_ => Row.fromSeq(throw e)), None)
    }
  }
  */
}
