/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.xml

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.InputStep
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext


class XMLInputStepBatch(name: String,
                         outputOptions: OutputOptions,
                         ssc: Option[StreamingContext],
                         xDSession: XDSession,
                         properties: Map[String, JSerializable]
                        )
  extends InputStep[RDD](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val pathKey = "path"
  lazy val path: Option[String] = properties.getString(pathKey, None).notBlank
  lazy val rowTag: Option[String]  = properties.getString("rowTag", None).notBlank
  lazy val mode: String = properties.getString("mode", "PERMISSIVE")


  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The step name $name is not valid.", name)
      )

    if (path.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The input path cannot be empty.", name)
      )

    if(debugOptions.isDefined && !validDebuggingOptions)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"$errorDebugValidation", name)
      )

    if(rowTag.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The user should specify a row tag of the" +
          s" xml files to treat as a row", name)
      )

    validation
  }

  //Dummy function on batch inputs that generates DataSets with schema
  def init(): DistributedMonad[RDD] = {
    throw new Exception("Not used on inputs that generates DataSets with schema")
  }

  override def initWithSchema(): (DistributedMonad[RDD], Option[StructType]) = {
    require(path.nonEmpty, "The input path cannot be empty")

    val userOptions = propertiesWithCustom.flatMap { case (key, value) =>
      if (key != pathKey)
        Option(key -> value.toString)
      else None
    } ++ Map("rowTag" -> rowTag.get,
      "mode" -> mode
    )

    val df = xDSession.sqlContext.read.format("com.databricks.spark.xml").options(userOptions).load(path.get)

    (df.rdd, Option(df.schema))
  }
}
