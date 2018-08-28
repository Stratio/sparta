/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.cassandra

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.enumerators.SaveModeEnum.{Append, ErrorIfExists, Ignore, Overwrite}
import com.stratio.sparta.core.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.OutputStep
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession

import scala.util.Try
import CassandraOutputStep._
import org.apache.spark.sql.cassandra._

class CassandraOutputStep(
                               name: String,
                               xDSession: XDSession,
                               properties: Map[String, JSerializable]
                             ) extends OutputStep(name, xDSession, properties) {

  val CassandraClass = "org.apache.spark.sql.cassandra"
  val keyspace = properties.getString("keyspace", None).notBlank
  val cluster = properties.getString("cluster", None).notBlank
  val connectionHosts = getConnectionHostsConf(properties)

  override def supportedSaveModes: Seq[SaveModeEnum.Value] = Seq(Append, ErrorIfExists, Ignore, Overwrite)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {

    require(keyspace.isDefined, "It is mandatory to define the keyspace")
    require(cluster.isDefined, "It is mandatory to define the cluster")

    val tableName = getTableNameFromOptions(options)
    val sparkConfig = getSparkConfig(saveMode, tableName)

    validateSaveMode(saveMode)

    dataFrame.write
      .format(CassandraClass)
      .mode(getSparkSaveMode(saveMode))
      .options(sparkConfig ++ getCustomProperties)
      .save()
  }

  def getSparkConfig(saveMode: SaveModeEnum.Value, tableName: String): Map[String, String] =
    Map(
      "table" -> tableName,
      "keyspace" -> keyspace.get,
      "cluster" -> cluster.get
    )

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (connectionHosts.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"nodes should have at least one host", name)
      )

    if (keyspace.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"keyspace cannot be empty", name)
      )

    if (cluster.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"cluster cannot be empty", name)
      )

    validation
  }
}

object CassandraOutputStep {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    Seq(
      ("spark.cassandra.connection.host", getConnectionHostsConf(configuration))
    )
  }

  def getConnectionHostsConf(properties: Map[String, JSerializable]): String = {
    val values = Try(properties.getMapFromArrayOfValues("nodes")).getOrElse(Seq.empty[Map[String, String]])

    values.map(c => c.getOrElse("node", "localhost")).mkString(",")
  }
}


