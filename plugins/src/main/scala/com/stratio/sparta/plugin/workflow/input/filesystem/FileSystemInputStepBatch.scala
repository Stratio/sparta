/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.filesystem

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.InputStep
import com.stratio.sparta.serving.core.workflow.lineage.HdfsLineage
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext

class FileSystemInputStepBatch(
                                name: String,
                                outputOptions: OutputOptions,
                                ssc: Option[StreamingContext],
                                xDSession: XDSession,
                                properties: Map[String, JSerializable]
                              ) extends InputStep[RDD](name, outputOptions, ssc, xDSession, properties)
  with SLF4JLogging with HdfsLineage {

  lazy val pathKey = "path"
  lazy val path: Option[String] = properties.getString(pathKey, None)
  lazy val outputField = properties.getString("outputField", DefaultRawDataField)
  lazy val outputSchema = StructType(Seq(StructField(outputField, StringType)))

  override lazy val lineagePath: String = path.getOrElse("")

  override lazy val lineageResourceSuffix: Option[String] = {
    val regex = ".*\\.(csv|avro|json|xml|txt|parquet|xls|xlsx)$"

    if (lineagePath.matches(regex))
      lineagePath.split("/").lastOption
    else None
  }

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
        messages = validation.messages :+ WorkflowValidationMessage(s"the path cannot be empty", name)
      )

    if(debugOptions.isDefined && !validDebuggingOptions)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"$errorDebugValidation", name)
      )

    validation
  }

  //Dummy function on batch inputs that generates DataSets with schema
  def init(): DistributedMonad[RDD] = {
    throw new Exception("Not used on inputs that generates DataSets with schema")
  }

  override def lineageProperties(): Map[String, String] = getHdfsLineageProperties(InputStep.StepType)

  override def initWithSchema(): (DistributedMonad[RDD], Option[StructType]) = {
    require(path.nonEmpty, "Input path cannot be empty")

    val rdd = xDSession.sparkContext.textFile(path.get).map { text =>
      new GenericRowWithSchema(Array(text), outputSchema).asInstanceOf[Row]
    }

    (rdd, Option(outputSchema))
  }
}