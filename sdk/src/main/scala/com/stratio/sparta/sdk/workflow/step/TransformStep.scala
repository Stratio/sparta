/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.workflow.step

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper
import com.stratio.sparta.sdk.properties.Parameterizable
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import com.stratio.sparta.sdk.utils.CastingUtils
import com.stratio.sparta.sdk.workflow.enumerators.{WhenError, WhenFieldError, WhenRowError}
import com.stratio.sparta.sdk.workflow.enumerators.WhenError.WhenError
import org.apache.spark.sql.types.{StructField, StructType}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.properties.models.PropertiesSchemasInputsModel
import com.stratio.sparta.sdk.workflow.enumerators.WhenFieldError.WhenFieldError
import com.stratio.sparta.sdk.workflow.enumerators.WhenRowError.WhenRowError
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema

import scala.util.{Failure, Success, Try}

abstract class TransformStep[Underlying[Row]](
                                               val name: String,
                                               val outputOptions: OutputOptions,
                                               val transformationStepsManagement: TransformationStepManagement,
                                               @transient private[sparta] val ssc: Option[StreamingContext],
                                               @transient private[sparta] val xDSession: XDSession,
                                               properties: Map[String, JSerializable]
                                             ) extends Parameterizable(properties) with GraphStep with DistributedMonadImplicits {

  override lazy val customKey = "transformationOptions"
  override lazy val customPropertyKey = "transformationOptionsKey"
  override lazy val customPropertyValue = "transformationOptionsValue"

  lazy val whenErrorDo: WhenError = WhenError.withName(propertiesWithCustom.getString("whenError", None)
    .notBlank.getOrElse(transformationStepsManagement.whenError.toString))

  lazy val whenRowErrorDo: WhenRowError = WhenRowError.withName(propertiesWithCustom.getString("whenRowError", None)
    .notBlank.getOrElse(transformationStepsManagement.whenRowError.toString))

  lazy val whenFieldErrorDo: WhenFieldError = WhenFieldError.withName(
    propertiesWithCustom.getString("whenFieldError", None)
      .notBlank.getOrElse(transformationStepsManagement.whenFieldError.toString))

  lazy val inputsModel: PropertiesSchemasInputsModel =
    SdkSchemaHelper.getInputSchemasModel(properties.getString("inputSchemas", None).notBlank)

  def transformWithSchema(
                           inputData: Map[String, DistributedMonad[Underlying]]
                         ): (DistributedMonad[Underlying], Option[StructType]) = {
    val monad = transform(inputData)

    (monad, None)
  }

  /* METHODS TO IMPLEMENT */

  /**
    * Transformation function that all the transformation plugins must implements.
    *
    * @param inputData Input steps data that the function receive. The key is the name of the step and the value is
    *                  the collection ([[DistributedMonad]])
    * @return The output [[DistributedMonad]] generated after apply the function
    */
  def transform(inputData: Map[String, DistributedMonad[Underlying]]): DistributedMonad[Underlying] = {
    inputData.head._2
  }

  /* METHODS IMPLEMENTED */

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the step name $name is not valid")

    validation
  }
  /**
    * Execute the transform function passed as parameter over the first data of the map.
    *
    * @param inputData                Input data that must contains only one distributed collection.
    * @param generateDistributedMonad Function to apply
    * @return The transformed distributed collection [[DistributedMonad]]
    */
  def applyHeadTransform[Underlying[Row]](inputData: Map[String, DistributedMonad[Underlying]])
                                         (
                                           generateDistributedMonad: (String, DistributedMonad[Underlying]) =>
                                             DistributedMonad[Underlying]
                                         ): DistributedMonad[Underlying] = {
    assert(inputData.size == 1, s"The step $name must have one input, now have: ${inputData.keys}")

    val (firstStep, firstStream) = inputData.head

    generateDistributedMonad(firstStep, firstStream)
  }

  /**
    * Execute the transform function passed as parameter over the first data of the map.
    *
    * @param inputData                Input data that must contains only one distributed collection.
    * @param generateDistributedMonad Function to apply
    * @return The transformed distributed collection [[DistributedMonad]]
    */
  def applyHeadTransformSchema[Underlying[Row]](inputData: Map[String, DistributedMonad[Underlying]])
                                               (
                                                 generateDistributedMonad: (String, DistributedMonad[Underlying]) =>
                                                   (DistributedMonad[Underlying], Option[StructType])
                                               ): (DistributedMonad[Underlying], Option[StructType]) = {
    assert(inputData.size == 1, s"The step $name must have one input, now have: ${inputData.keys}")

    val (firstStep, firstStream) = inputData.head

    generateDistributedMonad(firstStep, firstStream)
  }

  //scalastyle:off
  def returnWhenFieldError(exception: Exception): Null =
    whenFieldErrorDo match {
      case WhenFieldError.Null => null
      case _ => throw exception
    }

  //scalastyle:on

  def castingToOutputSchema(outSchema: StructField, inputValue: Any): Any =
    Try {
      CastingUtils.castingToSchemaType(outSchema.dataType, inputValue.asInstanceOf[Any])
    } match {
      case Success(result) => result
      case Failure(e) => returnWhenFieldError(new Exception(
        s"Error casting to output type the value: ${inputValue.toString}", e))
    }

  def returnSeqDataFromRow(newData: => Row): Seq[Row] = manageErrorWithTry(newData)

  def returnSeqDataFromRows(newData: => Seq[Row]): Seq[Row] = manageErrorWithTry(newData)


  private def manageErrorWithTry[T](newData: => T): Seq[Row] =
    Try(newData) match {
      case Success(data) => manageSuccess(data)
      case Failure(e) => whenRowErrorDo match {
        case WhenRowError.RowDiscard => Seq.empty[GenericRowWithSchema]
        case _ => throw e
      }
    }

  private def manageSuccess[T](newData: T): Seq[Row] =
    newData match {
      case data: Seq[GenericRowWithSchema] => data
      case data: GenericRowWithSchema => Seq(data)
      case _ => whenRowErrorDo match {
        case WhenRowError.RowDiscard => Seq.empty[GenericRowWithSchema]
        case _ => throw new Exception("Invalid new data struct in step")
      }
    }


}

object TransformStep {
  val StepType = "transformation"
}