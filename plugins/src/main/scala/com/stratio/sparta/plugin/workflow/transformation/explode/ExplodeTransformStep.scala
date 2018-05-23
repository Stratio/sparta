/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.explode

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.enumerations.FieldsPreservationPolicy
import com.stratio.sparta.plugin.helper.SchemaHelper._
import com.stratio.sparta.plugin.helper.SparkStepHelper
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper
import com.stratio.sparta.sdk.models.{DiscardCondition, ErrorValidations, OutputOptions, TransformationStepManagement}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.functions.{col, explode}
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

import scala.util.{Failure, Success, Try}

abstract class ExplodeTransformStep[Underlying[Row]](
                                                      name: String,
                                                      outputOptions: OutputOptions,
                                                      transformationStepsManagement: TransformationStepManagement,
                                                      ssc: Option[StreamingContext],
                                                      xDSession: XDSession,
                                                      properties: Map[String, JSerializable]
                                                    )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  lazy val inputField: String = Try(properties.getString("inputField"))
    .getOrElse(throw new IllegalArgumentException("The inputField is mandatory"))
  lazy val preservationPolicy: FieldsPreservationPolicy.Value = FieldsPreservationPolicy.withName(
    properties.getString("fieldsPreservationPolicy", "REPLACE").toUpperCase)
  lazy val explodedField: String = Try(properties.getString("explodedField"))
    .getOrElse(throw new IllegalArgumentException("The exploded new field name is mandatory"))

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

    if(explodedField.isEmpty) {
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the exploded field cannot be empty")
    }

    if(explodedField.nonEmpty && preservationPolicy.equals(FieldsPreservationPolicy.APPEND) &&
      explodedField.equals(inputField)) {
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the exploded field $explodedField cannot be" +
          s"the same as the input field")
    }

    validation
  }



  def applyExplode(
                    rdd: RDD[Row],
                    column: String,
                    inputStep: String
                  ): (RDD[Row], Option[StructType], Option[StructType]) = {
    Try {
      val inputSchema = getSchemaFromSessionOrModelOrRdd(xDSession, inputStep, inputsModel,rdd)
      createOrReplaceTemporalViewDf(xDSession, rdd, inputStep, inputSchema) match {
        case Some(df) =>
          Try{
            val newDataFrame = preservationPolicy match {
              case FieldsPreservationPolicy.APPEND =>
                df.withColumn(explodedField, explode(col(inputField)))
              case FieldsPreservationPolicy.JUST_EXTRACTED =>
                df.select(explode(col(inputField)).as(explodedField))
              case FieldsPreservationPolicy.REPLACE =>
                val renamedDf = df.withColumnRenamed(inputField, explodedField)
                renamedDf.withColumn(explodedField, explode(col(explodedField)))
            }
            newDataFrame
          } match {
              case Success(dfResult) =>
                (dfResult.rdd, Option(dfResult.schema), inputSchema)
              case Failure(e) => throw new Exception(s"The input value must be an array or a map", e)
            }
        case None => (rdd.filter(_ => false), None, inputSchema)
      }
    } match {
      case Success(result) => result
      case Failure(e) => (SparkStepHelper.failRDDWithException(rdd, e), None, None)
    }
  }
}