/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.filesystem

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.DistributedMonad
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.sql.Row
import org.apache.spark.streaming.dstream.DStream
import org.apache.hadoop.fs.Path
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{ErrorValidations, InputStep, OutputOptions}
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import DistributedMonad.Implicits._

class FileSystemInputStepStreaming(
                                    name: String,
                                    outputOptions: OutputOptions,
                                    ssc: Option[StreamingContext],
                                    xDSession: XDSession,
                                    properties: Map[String, JSerializable]
                                  ) extends InputStep[DStream](name, outputOptions, ssc, xDSession, properties)
  with SLF4JLogging {

  lazy val path: String = properties.getString("path", "").trim

  protected def defaultFilter(path: Path): Boolean =
    !path.getName.startsWith(".") && !path.getName.endsWith("_COPYING_") &&
      !path.getName.startsWith("_")

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (path.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the path cannot be empty")

    validation
  }

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
