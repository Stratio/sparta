/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.elasticsearch

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.core.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.common.elasticsearch.ElasticSearchBase
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession

import scala.util.Try

class ElasticSearchOutputStep(
                               name: String,
                               xDSession: XDSession,
                               properties: Map[String, JSerializable]
                             ) extends OutputStep(name, xDSession, properties) with ElasticSearchBase {

  val sparkConf = xDSession.conf.getAll
  val tlsEnabled = Try(properties.getString("tlsEnabled", "false").toBoolean).getOrElse(false)
  val autoCreateIndex = Try(properties.getString("enableAutoCreateIndex", "true").toBoolean).getOrElse(true)
  val mappingType = properties.getString("indexMapping", DefaultIndexType)
  val httpNodes = getHostPortConf(NodesName, DefaultNode, DefaultHttpPort, NodeName, HttpPortName)
  val securityOpts = if (tlsEnabled) SecurityHelper.elasticSecurityOptions(sparkConf) else Map.empty

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
  } ++ securityOpts

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (httpNodes.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"nodes should have at least one host", name)
      )

    if(httpNodes.nonEmpty) {
      if (httpNodes.forall(_._1.trim.isEmpty))
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ WorkflowValidationMessage(s"nodes host should be a valid url", name)
        )

      if (httpNodes.forall(hp => hp._2.isInstanceOf[Int] && hp._2 <= 0))
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ WorkflowValidationMessage(s"nodes port should be a positive number", name)
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
