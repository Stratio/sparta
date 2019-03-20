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
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

class FileSystemInputStepStreaming(
                                    name: String,
                                    outputOptions: OutputOptions,
                                    ssc: Option[StreamingContext],
                                    xDSession: XDSession,
                                    properties: Map[String, JSerializable]
                                  ) extends InputStep[DStream](name, outputOptions, ssc, xDSession, properties)
  with SLF4JLogging with HdfsLineage {

  lazy val path: String = properties.getString("path", "").trim

  override lazy val lineagePath: String = path

  override lazy val lineageResourceSuffix: Option[String] = {
    val regex = ".*\\.(csv|avro|json|xml|txt)$"

    if (lineagePath.matches(regex))
      lineagePath.split("/").lastOption
    else None
  }

  protected def defaultFilter(path: Path): Boolean =
    !path.getName.startsWith(".") && !path.getName.endsWith("_COPYING_") &&
      !path.getName.startsWith("_")

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

  override def lineageProperties(): Map[String, String] = getHdfsLineageProperties(InputStep.StepType)

  def init(): DistributedMonad[DStream] = {
    require(path.nonEmpty, "Input path can not be empty")

    val filters = properties.getString("filterString", None).notBlank
    val flagNewFiles = properties.getBoolean("newFilesOnly")
    val outputField = properties.getString("outputField", DefaultRawDataField)
    val outputSchema = StructType(Seq(StructField(outputField, StringType)))
    val applyFilters = (path: Path) =>
      defaultFilter(path) && filters.forall(_.split(",").forall(!path.getName.contains(_)))

    ssc.get.fileStream[LongWritable, Text, TextInputFormat](path, applyFilters, flagNewFiles).transform { rdd =>
      val newRdd = rdd.map { case (_, text) =>
        new GenericRowWithSchema(Array(text.toString), outputSchema).asInstanceOf[Row]
      }

      xDSession.createDataFrame(newRdd, outputSchema).createOrReplaceTempView(name)
      newRdd
    }

  }

}
