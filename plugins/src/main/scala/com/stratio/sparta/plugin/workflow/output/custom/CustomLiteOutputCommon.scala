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
import com.stratio.sparta.core.workflow.step.OutputStep._
import com.stratio.sparta.sdk.lite.common.SDKCustomOutput
import com.stratio.sparta.sdk.lite.common.models._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.{Failure, Success, Try}

object CustomLiteOutputCommon{
  val CustomLiteClassTypeProp = "customLiteClassType"
}

class CustomLiteOutputCommon[T <: SDKCustomOutput](name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  lazy val customClassType: Option[String] = properties.getString(CustomLiteOutputCommon.CustomLiteClassTypeProp, None)

  lazy val spark2spartaSaveMode: Map[SaveModeEnum.Value, SaveMode] = Map(
    SaveModeEnum.Append -> Append,
    SaveModeEnum.ErrorIfExists -> ErrorIfExists,
    SaveModeEnum.Overwrite -> Overwrite,
    SaveModeEnum.Ignore -> Ignore,
    SaveModeEnum.Upsert -> Append,
    SaveModeEnum.Delete -> Append
  )

  def customStep: Try[T] = Try {
    val customClassProperty = customClassType.getOrElse(throw new Exception("The class property is mandatory"))
    val classpathUtils = new ClasspathUtils
    val (customClass, customClassAndPackage) = classpathUtils.getCustomClassAndPackage(customClassProperty)
    val sparkSession = xDSession.asInstanceOf[SparkSession]
    val properties = propertiesWithCustom.mapValues(_.toString)

    classpathUtils.tryToInstantiate[T](
      classAndPackage = customClass,
      block = (c) => {
        val constructor = c.getDeclaredConstructor(
          classOf[SparkSession],
          classOf[Map[String, String]]
        )
        val instance = constructor.newInstance(sparkSession, properties)

        instance.asInstanceOf[T]
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

        val tableName = options.get(TableNameKey).notBlank
        val customOptions = getCustomProperties
        val primaryKey = getPrimaryKeyOptions(options)

        val partitionByKey = getPartitionByKeyOptions(options).map{ partitions =>
          val fieldsInDataFrame = dataFrame.schema.fields.map(field => field.name)
          val partitionFields = partitions.split(",").map(_.trim)
          if (partitionFields.forall(field => fieldsInDataFrame.contains(field)))
            partitionFields.toSeq
          else {
            log.warn(s"Impossible to execute partition by fields: $partitionFields because the dataFrame does not " +
              s"contain all fields. The dataFrame only contains: ${fieldsInDataFrame.mkString(",")}")
            Seq.empty
          }
        }.getOrElse(Seq.empty)


        val spartaSaveMode = spark2spartaSaveMode.getOrElse(
          saveMode,
          {log.warn(s"SaveMode $saveMode not found. Using Append as default SaveMode"); Append }
        )

        customLiteOutput.save(
          dataFrame,
          OutputOptions(spartaSaveMode, tableName, primaryKey, partitionByKey, customOptions)
        )

      case Failure(e) =>
        throw e
    }
  }

}