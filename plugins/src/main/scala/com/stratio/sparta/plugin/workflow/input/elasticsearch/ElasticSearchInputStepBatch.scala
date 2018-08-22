/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.elasticsearch

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.helpers.{SdkSchemaHelper, ValidationsHelper}
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.InputStep
import com.stratio.sparta.plugin.common.elasticsearch.ElasticSearchBase
import com.stratio.sparta.plugin.helper.SecurityHelper
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

import scala.util.Try


class ElasticSearchInputStepBatch(
                                   name: String,
                                   outputOptions: OutputOptions,
                                   ssc: Option[StreamingContext],
                                   xDSession: XDSession,
                                   properties: Map[String, JSerializable]
                                 )
  extends InputStep[RDD](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging with ElasticSearchBase {

  lazy val tlsKey = "tlsEnabled"
  lazy val resourceKey = "resource"
  lazy val readMetadataKey = "readMetadata"
  lazy val sparkConf = xDSession.conf.getAll
  lazy val tlsEnabled = Try(properties.getString(tlsKey, "false").toBoolean).getOrElse(false)
  lazy val resource = properties.getString(resourceKey, None).notBlank
  lazy val readMetadata = Try(properties.getBoolean("readMetadata", default = true)).getOrElse(true)
  lazy val httpNodes = getHostPortConf(NodesName, DefaultNode, DefaultHttpPort, NodeName, HttpPortName)
  lazy val securityOpts = if (tlsEnabled) SecurityHelper.elasticSecurityOptions(sparkConf) else Map.empty

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The step name $name is not valid.", name)
      )

    if (resource.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The input resource cannot be empty.", name)
      )

    if (httpNodes.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"At least one host should be defined", name)
      )

    if (httpNodes.nonEmpty) {
      if (httpNodes.forall(_._1.trim.isEmpty))
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ WorkflowValidationMessage(s"At least a valid URL for a host should be defined", name)
        )

      if (httpNodes.forall(hp => hp._2.isInstanceOf[Int] && !ValidationsHelper.validateTcpUdpPort(hp._2)))
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ WorkflowValidationMessage(s"Nodes port should be a positive number", name)
        )
    }

    validation
  }

  //Dummy function on batch inputs that generates DataSets with schema
  def init(): DistributedMonad[RDD] = {
    throw new Exception("Not used on inputs that generates DataSets with schema")
  }

  override def initWithSchema(): (DistributedMonad[RDD], Option[StructType]) = {
    require(httpNodes.nonEmpty, "The input nodes cannot be empty")
    require(resource.nonEmpty, "The input resource cannot be empty")

    val filterOptions = Seq(NodesName, tlsKey, resourceKey, readMetadataKey)
    val userOptions = getCustomProperties.flatMap { case (key, value) =>
      if (!filterOptions.contains(key))
        Option(key -> value.toString)
      else None
    }
    val df = xDSession.sqlContext.read
      .format(ElasticSearchClass).options(getSparkConfig ++ userOptions)
      .load(resource.get)

    (df.rdd, Option(df.schema))
  }

  def getSparkConfig: Map[String, String] =
    Map(
      "es.nodes" -> httpNodes.head._1,
      "es.port" -> httpNodes.head._2.toString,
      "es.read.metadata" -> readMetadata.toString
    ) ++ securityOpts
}

object ElasticSearchInputStepBatch {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }
}