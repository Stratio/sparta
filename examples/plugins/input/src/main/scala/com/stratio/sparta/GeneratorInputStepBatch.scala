/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step._
import org.apache.spark.sql.{Row, _}
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import DistributedMonad.Implicits._

import scala.util.{Failure, Success, Try}
import org.apache.spark.sql._
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}

import scala.util.Try

class GeneratorInputStepBatch(
                               name: String,
                               outputOptions: OutputOptions,
                               ssc: Option[StreamingContext],
                               xDSession: XDSession,
                               properties: Map[String, JSerializable]
                             )
  extends InputStep[RDD](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val stringSchema = StructType(Seq(StructField("raw", StringType)))

  def init(): DistributedMonad[RDD] = {
    val register = Seq(new GenericRowWithSchema(Array("test-data"), stringSchema).asInstanceOf[Row])
    val defaultRDD = xDSession.sparkContext.parallelize(register)

    defaultRDD
  }
}
