/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.custom

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.utils.ClasspathUtils
import com.stratio.sparta.core.workflow.step.InputStep
import com.stratio.sparta.plugin.helper.SchemaHelper
import com.stratio.sparta.sdk.lite.streaming.LiteCustomStreamingInput
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.util.{Failure, Success, Try}

class CustomLiteInputStepStreaming(
                                    name: String,
                                    outputOptions: OutputOptions,
                                    ssc: Option[StreamingContext],
                                    xDSession: XDSession,
                                    properties: Map[String, JSerializable]
                                  ) extends InputStep[DStream](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val customClassType: Option[String] = properties.getString("customLiteClassType", None)

  def customStep: Try[LiteCustomStreamingInput] = Try {
    val customClassProperty = customClassType.getOrElse(throw new Exception("The class property is mandatory"))
    val classpathUtils = new ClasspathUtils
    val (customClass, customClassAndPackage) = classpathUtils.getCustomClassAndPackage(customClassProperty)
    val sparkSession = xDSession.asInstanceOf[SparkSession]
    val properties = propertiesWithCustom.mapValues(_.toString)

    classpathUtils.tryToInstantiate[LiteCustomStreamingInput](
      classAndPackage = customClass,
      block = (c) => {
        val constructor = c.getDeclaredConstructor(
          classOf[SparkSession],
          classOf[StreamingContext],
          classOf[Map[String, String]]
        )
        val instance = constructor.newInstance(sparkSession, ssc.get, properties)

        instance.asInstanceOf[LiteCustomStreamingInput]
      },
      inputClazzMap = Map(customClass -> customClassAndPackage)
    )
  }

  override def validate(options: Map[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)
    val customLiteStep = customStep

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The step name $name is not valid.", name)
      )

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

    if (debugOptions.isDefined && !validDebuggingOptions)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"$errorDebugValidation", name)
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

  def init(): DistributedMonad[DStream] = {
    throw new Exception("Not used on inputs that generates DataSets with schema")
  }

  override def initWithSchema(): (DistributedMonad[DStream], Option[StructType]) = {
    customStep match {
      case Success(customLiteInput) =>
        val result = customLiteInput.init()
        val dataResult = result.data.transform { rdd =>
          result.schema.orElse {
            SchemaHelper.getSchemaFromSessionOrRdd(xDSession, name, rdd)
          }.foreach(schema => xDSession.createDataFrame(rdd, schema).createOrReplaceTempView(name))
          rdd
        }
        (dataResult, result.schema)
      case Failure(e) =>
        throw e
    }
  }
}