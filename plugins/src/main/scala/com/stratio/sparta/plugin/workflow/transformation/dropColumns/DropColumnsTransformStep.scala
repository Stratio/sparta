/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.dropColumns

import java.io.{Serializable => JSerializable}

import scala.util.{Failure, Success, Try}
import akka.event.slf4j.SLF4JLogging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Column, Row}
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.plugin.helper.SchemaHelper.{createOrReplaceTemporalViewDf, getSchemaFromSessionOrModelOrRdd, parserInputSchema}
import com.stratio.sparta.plugin.helper.SqlHelper
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper
import com.stratio.sparta.sdk.models.{DiscardCondition, PropertyFields}
import com.stratio.sparta.sdk.workflow.step.{ErrorValidations, OutputOptions, TransformStep, TransformationStepManagement}

//scalastyle:off
abstract class DropColumnsTransformStep[Underlying[Row]](
                                                         name: String,
                                                         outputOptions: OutputOptions,
                                                         transformationStepsManagement: TransformationStepManagement,
                                                         ssc: Option[StreamingContext],
                                                         xDSession: XDSession,
                                                         properties: Map[String, JSerializable]
                                                       )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
      with SLF4JLogging {

  lazy val fields: Option[PropertyFields] = Try(properties.getPropertiesFields("schema.fields")).toOption

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the step name $name is not valid")

    if (Try(fields.nonEmpty).isFailure) {
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the input fields are not valid")
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

    if (fields.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: it's mandatory at least one column to drop"
      )

    validation
  }


  def applyDrop(rdd: RDD[Row], inputStep: String): (RDD[Row], Option[StructType], Option[StructType]) = {
    Try {
      val schemaFields = fields.get.fields.map(_.name)
      val inputSchema = getSchemaFromSessionOrModelOrRdd(xDSession, inputStep, inputsModel, rdd)
      createOrReplaceTemporalViewDf(xDSession, rdd, inputStep, inputSchema) match {
        case Some(df) =>
          val newDataFrame  = df.drop(schemaFields:_*)
          (newDataFrame.rdd, Option(newDataFrame.schema), inputSchema)
        case None =>
          (rdd.filter(_ => false), None, inputSchema)
      }
    } match {
      case Success(sqlResult) => sqlResult
      case Failure(e) => (SqlHelper.failWithException(rdd, e), None, None)
    }
  }

}