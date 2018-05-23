/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta

import java.io.{Serializable => JSerializable}
import java.nio.charset.StandardCharsets

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, InputStep}
import org.apache.spark.sql.{Row, _}
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputStep
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import DistributedMonad.Implicits._

import scala.collection.mutable
import scala.util.{Failure, Success, Try}
import org.apache.spark.sql._
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}

import scala.util.Try

class GeneratorInputStepStream(name: String,
                                           outputOptions: OutputOptions,
                                           ssc: Option[StreamingContext],
                                           xDSession: XDSession,
                                           properties: Map[String, JSerializable])
  extends InputStep[DStream](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val stringSchema = StructType(Seq(StructField("raw", StringType)))

  override def init(): DistributedMonad[DStream] = {
    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataIn = Seq(new GenericRowWithSchema(Array("test-data"), stringSchema))
    dataQueue += xDSession.sparkContext.parallelize(dataIn)

    ssc.get.queueStream(dataQueue)

  }
}
