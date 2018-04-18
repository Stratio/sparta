/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.jdbc

import java.io.{Serializable => JSerializable}
import java.util.Properties

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.helper.SecurityHelper.getDataStoreUri
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{ErrorValidations, InputStep, OutputOptions}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

import scala.util.Try

class JdbcInputStepBatch(
                          name: String,
                          outputOptions: OutputOptions,
                          ssc: Option[StreamingContext],
                          xDSession: XDSession,
                          properties: Map[String, JSerializable]
                        )
  extends InputStep[RDD](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val url = properties.getString("url", None)
  lazy val table = properties.getString("dbtable", None)
  lazy val tlsEnable = Try(properties.getBoolean("tlsEnabled")).getOrElse(false)

  val sparkConf = xDSession.conf.getAll
  val securityUri = getDataStoreUri(sparkConf)
  val urlWithSSL = url.map(inputUrl => if (tlsEnable) inputUrl + securityUri else inputUrl)

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (urlWithSSL.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the url must be provided"
      )

    if (table.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the table must be provided"
      )

    if(tlsEnable && securityUri.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: when TLS is enabled the sparkConf must contain the security options"
      )
    validation
  }

  //Dummy function on batch inputs that generates DataSets with schema
  def init(): DistributedMonad[RDD] = {
    throw new Exception("Not used on inputs that generates DataSets with schema")
  }

  override def initWithSchema(): (DistributedMonad[RDD], Option[StructType]) = {
    require(urlWithSSL.nonEmpty, "JDBC url must be provided")
    require(table.nonEmpty, "Table must be provided")

    val userOptions = propertiesWithCustom.flatMap { case (key, value) =>
      if (key != url.get)
        Option(key -> value.toString)
      else None
    }
    val userProperties = new Properties()
    userOptions.foreach { case (key, value) =>
      if (value.toString.nonEmpty) userProperties.put(key, value.toString)
    }
    val df = xDSession.read.jdbc(urlWithSSL.get, table.get, userProperties)

    (df.rdd, Option(df.schema))
  }
}


