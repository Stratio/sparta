/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.duplicateColumns

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import org.apache.spark.rdd.RDD
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, TransformationStepManagement, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.core.workflow.step.TransformStep
import org.apache.spark.sql.types.StructType
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.plugin.helper.SchemaHelper.{createOrReplaceTemporalViewDf, getSchemaFromSessionOrModelOrRdd, parserInputSchema}
import com.stratio.sparta.plugin.helper.SparkStepHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError
import com.stratio.sparta.plugin.models.PropertyColumnsToDuplicate
import org.apache.spark.sql.{Column, Row}
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}

abstract class DuplicateColumnsTransformStep[Underlying[Row]](
                                                               name: String,
                                                               outputOptions: OutputOptions,
                                                               transformationStepsManagement: TransformationStepManagement,
                                                               ssc: Option[StreamingContext],
                                                               xDSession: XDSession,
                                                               properties: Map[String, JSerializable]
                                                             )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  lazy val groupPropertyName = "columnsToDuplicate"
  lazy val keyPropertyName = "columnToDuplicate"
  lazy val valuePropertyName = "newColumnName"


  lazy val columnProperties: Seq[PropertyColumnsToDuplicate] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val columnsToDuplicate = properties.getString("columnsToDuplicate", None).notBlank.getOrElse("[]")

    read[Seq[PropertyColumnsToDuplicate]](columnsToDuplicate)
  }


  def printEmpty(propertyColumnsToDuplicate:Seq[(Option[String],Option[String])]): String =
    propertyColumnsToDuplicate.map { _ match {
      case (Some(old), None) => s"[Column to duplicate: $old, newName: EMPTY]"
      case (None, Some(newDupl)) => s"[Column to duplicate: EMPTY, newName: $newDupl]"
      case (None, None) => s"Empty fields in Columns to duplicate"
      }
    }.mkString(",\n")


  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {

    val emptyValues: Seq[(Option[String],Option[String])] = columnProperties.map { p: PropertyColumnsToDuplicate =>
      (Option(p.columnToDuplicate).notBlank, Option(p.newColumnName).notBlank)}.filter( t => t._1.isEmpty || t._2.isEmpty)


    //If contains schemas, validate if it can be parsed
    val inputSchemaValidationErrors: Seq[(HasError, String)] = inputsModel.inputSchemas.flatMap { input =>
      Seq(
        parserInputSchema(input.schema).isFailure -> "The input schema from step ${input.stepName} is not valid.",
        !SdkSchemaHelper.isCorrectTableName(input.stepName) -> s"The input table name ${input.stepName} is not valid."
      )
    }

    val validationSeq = Seq[(HasError, String)](
      !SdkSchemaHelper.isCorrectTableName(name) -> s"The step name $name is not valid",
      columnProperties.isEmpty -> "It's mandatory to specify at least one column to be duplicated",
      emptyValues.nonEmpty -> s"${printEmpty(emptyValues)}."

    ) ++ inputSchemaValidationErrors

    ErrorValidationsHelper.validate(validationSeq, name)

  }


  def applyDuplicate(rdd: RDD[Row], inputStep: String): (RDD[Row], Option[StructType], Option[StructType]) = {
    Try {
      val inputSchema = getSchemaFromSessionOrModelOrRdd(xDSession, inputStep, inputsModel, rdd)
      createOrReplaceTemporalViewDf(xDSession, rdd, inputStep, inputSchema) match {
        case Some(df) =>
          import org.apache.spark.sql.functions._

          val newDf = columnProperties.foldLeft(df)({
            case (df2, columnProperty:PropertyColumnsToDuplicate) =>
              df2.withColumn(columnProperty.newColumnName, col(columnProperty.columnToDuplicate))
          })

          (newDf.rdd, Option(newDf.schema), inputSchema)
        case None =>
          (rdd.filter(_ => false), None, inputSchema)
      }
    } match {
      case Success(sqlResult) => sqlResult
      case Failure(e) => (SparkStepHelper.failRDDWithException(rdd, e), None, None)
    }
  }
}