/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.ignite

import java.io.{Serializable => JSerializable}
import java.util.Properties

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.InputStep
import com.stratio.sparta.plugin.helper.SecurityHelper._
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

import scala.util.Try

class IgniteInputStepBatch(name: String,
                          outputOptions: OutputOptions,
                           ssc: Option[StreamingContext],
                           xDSession: XDSession,
                           properties: Map[String, JSerializable]
                          )
extends InputStep[RDD](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val url = properties.getString("host", None)
  lazy val table = properties.getString("dbtable", None)
  lazy val tlsEnable = Try(properties.getBoolean("tlsEnabled")).getOrElse(false)
  lazy val fetchSize = properties.getString("fetchSize", None)
  val sparkConf = xDSession.conf.getAll
  val securityUri = igniteSecurityUri(sparkConf)

  val urlWithSSL = url.map(inputUrl =>
    if (tlsEnable)
      addUserToConnectionURI(spartaTenant, inputUrl) + securityUri
    else
      inputUrl)


  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {

    val validationSeq = Seq[(HasError,String)](
      !SdkSchemaHelper.isCorrectTableName(name) -> s"The step name $name is not valid",
      urlWithSSL.isEmpty -> s"the url must be provided",
      table.isEmpty -> s"the table must be provided",
      (tlsEnable && securityUri.isEmpty) -> s"when TLS is enabled the sparkConf must contain the security options",
      (fetchSize.isEmpty || fetchSize.get.toInt <= 0) -> s"fetch size must be greater than 0",
      (debugOptions.isDefined && !validDebuggingOptions) -> s"$errorDebugValidation"
    )
    ErrorValidationsHelper.validate(validationSeq, name)
  }

  //Dummy function on batch inputs that generates DataSets with schema
  def init(): DistributedMonad[RDD] = {
    throw new Exception("Not used on inputs that generates DataSets with schema")
  }

  override def initWithSchema(): (DistributedMonad[RDD], Option[StructType]) = {
    require(urlWithSSL.nonEmpty, "Ignite url must be provided")
    require(table.nonEmpty, "Table must be provided")

    val userOptions = propertiesWithCustom.filterKeys(key => key != url.get)

    val userProperties = new Properties()
    userOptions.foreach { case (key, value) =>
      if (value.toString.nonEmpty) userProperties.put(key, value.toString)
    }
    val df = xDSession.read.jdbc(urlWithSSL.get, table.get, userProperties)

    (df.rdd, Option(df.schema))
  }
}
