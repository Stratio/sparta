/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.sparta.plugin.workflow.output.elasticsearch

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.sdk.workflow.step.{ErrorValidations, OutputStep}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession
import scala.util.Try

class ElasticSearchOutputStep(
                               name: String,
                               xDSession: XDSession,
                               properties: Map[String, JSerializable]
                             ) extends OutputStep(name, xDSession, properties) {


  val DefaultIndexType = "sparta"
  val DefaultNode = "localhost"
  val DefaultHttpPort = "9200"
  val NodeName = "node"
  val NodesName = "nodes"
  val HttpPortName = "httpPort"
  val DefaultCluster = "elasticsearch"
  val ElasticSearchClass = "org.elasticsearch.spark.sql"
  val sparkConf = xDSession.conf.getAll


  val tlsEnabled = Try(properties.getString("tlsEnabled", "false").toBoolean).getOrElse(false)
  val timeStampMapper = properties.getString("timeStampMapperFormat", None).notBlank
  val autoCreateIndex = Try(properties.getString("enableAutoCreateIndex", "true").toBoolean).getOrElse(true)
  val mappingType = properties.getString("indexMapping", DefaultIndexType)
  val clusterName = properties.getString("clusterName", DefaultCluster)
  val httpNodes = getHostPortConf(NodesName, DefaultNode, DefaultHttpPort, NodeName, HttpPortName)

  val securityOpts =
    if (tlsEnabled)
      elasticSecurityOptions(sparkConf)
    else Map.empty

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val tableName = getTableNameFromOptions(options)
    val primaryKeyOption = getPrimaryKeyOptions(options)
    val sparkConfig = getSparkConfig(saveMode, primaryKeyOption)

    validateSaveMode(saveMode)

    dataFrame.write
      .format(ElasticSearchClass)
      .mode(getSparkSaveMode(saveMode))
      .options(sparkConfig ++ getCustomProperties)
      .save(indexNameType(tableName))
  }

  def indexNameType(tableName: String): String =
    s"${tableName.toLowerCase}/$mappingType"


  def getHostPortConf(key: String,
                      defaultHost: String,
                      defaultPort: String,
                      nodeName: String,
                      portName: String): Seq[(String, Int)] = {
    val values = Try(properties.getMapFromArrayOfValues(key)).getOrElse(Seq.empty[Map[String, String]])

    values.map(c =>
      (c.getOrElse(nodeName, defaultHost), c.getOrElse(portName, defaultPort).toInt))
  }

  def getSparkConfig(saveMode: SaveModeEnum.Value, primaryKey: Option[String])
  : Map[String, String] = {
    saveMode match {
      case SaveModeEnum.Upsert => primaryKey.fold(Map.empty[String, String]) { field =>
        Map("es.mapping.id" -> field)
      }
      case _ => Map.empty[String, String]
    }
  } ++ {
    Map("es.nodes" -> httpNodes.head._1, "es.port" -> httpNodes.head._2.toString,
      "es.index.auto.create" -> autoCreateIndex.toString)
  } ++ {
    timeStampMapper match {
      case Some(timeStampMapperValue) => Map("es.mapping.timestamp" -> timeStampMapperValue)
      case None => Map.empty[String, String]
    }
  } ++ securityOpts

  def elasticSecurityOptions(sparkConf: Map[String, String]): Map[String, String] = {

    val prefixSparkElastic = "spark.ssl.datastore."
    val prefixElasticSecurity = "es.net.ssl"

    if (sparkConf.get(prefixSparkElastic + "enabled").isDefined &&
      sparkConf(prefixSparkElastic + "enabled") == "true") {

      val configElastic = sparkConf.flatMap { case (key, value) =>
        if (key.startsWith(prefixSparkElastic))
          Option(key.replace(prefixSparkElastic, "") -> value)
        else None
      }

      val mappedProps = Map(
        s"$prefixElasticSecurity" -> configElastic("enabled"),
        s"$prefixElasticSecurity.keystore.pass" -> configElastic("keyStorePassword"),
        s"$prefixElasticSecurity.keystore.location" -> s"file:${configElastic("keyStore")}",
        s"$prefixElasticSecurity.truststore.location" -> s"file:${configElastic("trustStore")}",
        s"$prefixElasticSecurity.truststore.pass" -> configElastic("trustStorePassword"))

      mappedProps
    } else {
      Map()
    }
  }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (httpNodes.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: nodes should have at least one host"
      )

    if(httpNodes.nonEmpty) {
      if (httpNodes.forall(_._1.trim.isEmpty))
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ s"$name: nodes host should be a valid url"
        )

      if (httpNodes.forall(hp => hp._2.isInstanceOf[Int] && hp._2 <= 0))
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ s"$name: nodes port should be a positive number"
        )
    }

    validation
  }
}

object ElasticSearchOutputStep {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }
}
