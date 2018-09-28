/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.custom

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.utils.ClasspathUtils
import com.stratio.sparta.core.workflow.step.TransformStep
import com.stratio.sparta.plugin.helper.SchemaHelper.{createOrReplaceTemporalViewDf, getSchemaFromSessionOrModelOrRdd}
import com.stratio.sparta.sdk.lite.batch.models.ResultBatchData
import com.stratio.sparta.sdk.lite.xd.batch.LiteCustomXDBatchTransform
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

import scala.util.{Failure, Success, Try}

class CustomLiteXDTransformStepBatch(
                                      name: String,
                                      outputOptions: OutputOptions,
                                      transformationStepsManagement: TransformationStepManagement,
                                      ssc: Option[StreamingContext],
                                      xDSession: XDSession,
                                      properties: Map[String, JSerializable]
                                    )
  extends TransformStep[RDD](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  lazy val customClassType: Option[String] = properties.getString("customLiteClassType", None)

  def customStep: Try[LiteCustomXDBatchTransform] = Try {
    val customClassProperty = customClassType.getOrElse(throw new Exception("The class property is mandatory"))
    val classpathUtils = new ClasspathUtils
    val (customClass, customClassAndPackage) = classpathUtils.getCustomClassAndPackage(customClassProperty)
    val properties = propertiesWithCustom.mapValues(_.toString)

    classpathUtils.tryToInstantiate[LiteCustomXDBatchTransform](
      classAndPackage = customClass,
      block = (c) => {
        val constructor = c.getDeclaredConstructor(
          classOf[XDSession],
          classOf[Map[String, String]]
        )
        val instance = constructor.newInstance(xDSession, properties)

        instance.asInstanceOf[LiteCustomXDBatchTransform]
      },
      inputClazzMap = Map(customClass -> customClassAndPackage)
    )
  }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)
    val customLiteStep = customStep

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The step name $name is not valid.", name))

    if (customClassType.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The custom class field cannot be empty", name)
      )

    if (customLiteStep.isFailure)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"There are errors creating the custom class", name)
      )

    customLiteStep match {
      case Success(step) =>
        val validationResult = step.validate()
        if (!validationResult.valid)
          ErrorValidations(
            valid = false,
            messages = validation.messages ++
              validationResult.messages.map(message => WorkflowValidationMessage(message, name)))
        else validation
      case Failure(_) =>
        validation
    }
  }

  override def transformWithDiscards(inputData: Map[String, DistributedMonad[RDD]])
  : (DistributedMonad[RDD], Option[StructType], Option[DistributedMonad[RDD]], Option[StructType]) = {
    customStep match {
      case Success(customLiteOutput) =>
        val inputBatchData = inputData.map { case (stepName, data) =>
          val schema = getSchemaFromSessionOrModelOrRdd(xDSession, stepName, inputsModel, data.ds)
          createOrReplaceTemporalViewDf(xDSession, data.ds, stepName, schema)
          stepName -> ResultBatchData(data.ds, schema)
        }
        val transformResult = customLiteOutput.transform(inputBatchData)

        (transformResult.data, transformResult.schema, transformResult.discardedData, transformResult.discardedSchema)
      case Failure(e) =>
        throw e
    }
  }

}