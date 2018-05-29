/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.websocket

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.InputStep
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import DistributedMonad.Implicits._
import com.stratio.sparta.sdk.models.{ErrorValidations, OutputOptions, WorkflowValidationMessage}

class WebSocketInputStepStreaming(
                                   name: String,
                                   outputOptions: OutputOptions,
                                   ssc: Option[StreamingContext],
                                   xDSession: XDSession,
                                   properties: Map[String, JSerializable]
                                 ) extends InputStep[DStream](name, outputOptions, ssc, xDSession, properties) {

  lazy val url: String = properties.getString("url", "").trim
  lazy val outputField = properties.getString("outputField", DefaultRawDataField)
  lazy val outputType = properties.getString("outputType", DefaultRawDataType)
  lazy val outputSparkType = SparkTypes.get(outputType) match {
    case Some(sparkType) => sparkType
    case None => schemaFromString(outputType)
  }
  lazy val outputSchema = StructType(Seq(StructField(outputField, outputSparkType)))

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (url.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"url cannot be empty", name)
      )

    validation
  }

  def init(): DistributedMonad[DStream] = {
    require(url.nonEmpty, "Input url cannot be empty")
    ssc.get.receiverStream(new WebSocketReceiver(url, storageLevel, outputSchema)).transform { rdd =>
      xDSession.createDataFrame(rdd, outputSchema).createOrReplaceTempView(name)
      rdd
    }
  }
}
