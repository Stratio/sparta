/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.custom

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.utils.ClasspathUtils
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.sdk.lite.xd.common.LiteCustomXDOutput
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession

import scala.util.{Failure, Success, Try}

class CustomLiteXDOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  lazy val customClassType: Option[String] = properties.getString("customLiteClassType", None)

  def customStep: Try[LiteCustomXDOutput] = Try {
    val customClassProperty = customClassType.getOrElse(throw new Exception("The class property is mandatory"))
    val classpathUtils = new ClasspathUtils
    val (customClass, customClassAndPackage) = {
      if (customClassProperty.contains(".")) {
        (customClassProperty.substring(customClassProperty.lastIndexOf(".")), customClassProperty)
      } else (customClassProperty, s"com.stratio.sparta.$customClassProperty")
    }
    val properties = propertiesWithCustom.mapValues(_.toString)

    classpathUtils.tryToInstantiate[LiteCustomXDOutput](
      classAndPackage = customClass,
      block = (c) => {
        val constructor = c.getDeclaredConstructor(
          classOf[XDSession],
          classOf[Map[String, String]]
        )
        val instance = constructor.newInstance(xDSession, properties)

        instance.asInstanceOf[LiteCustomXDOutput]
      },
      inputClazzMap = Map(customClass -> customClassAndPackage)
    )
  }

  override def validate(options: Map[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)
    val customLiteStep = customStep

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

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    validateSaveMode(saveMode)

    customStep match {
      case Success(customLiteOutput) =>
        customLiteOutput.save(dataFrame, saveMode.toString, options)
      case Failure(e) =>
        throw e
    }
  }
}


