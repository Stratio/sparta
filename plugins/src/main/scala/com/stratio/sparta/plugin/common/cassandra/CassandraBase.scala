/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.common.cassandra

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._

import scala.util.Try

trait CassandraBase {

  val CassandraClass = "org.apache.spark.sql.cassandra"
  val KeyspaceKey = "keyspace"
  val ClusterKey = "cluster"
  val NodesKey = "nodes"
  val TableKey = "table"
  val ConnectionKey = "spark.cassandra.connection.host"

  val properties: Map[String, JSerializable]
  val name: String

  lazy val keyspace = properties.getString(KeyspaceKey, None).notBlank
  lazy val cluster = properties.getString(ClusterKey, None).notBlank
  lazy val connectionHosts = CassandraBase.getConnectionHostsConf(properties)

  def getSparkConfig(tableName: String): Map[String, String] =
    Map(
      TableKey -> tableName,
      KeyspaceKey -> keyspace.get,
      ClusterKey -> cluster.get,
      ConnectionKey -> connectionHosts
    )

  def validateProperties(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
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

object CassandraBase {

  def getConnectionHostsConf(properties: Map[String, JSerializable]): String = {
    val values = Try(properties.getMapFromArrayOfValues("nodes")).getOrElse(Seq.empty[Map[String, String]])

    values.map(c => c.getOrElse("node", "localhost")).mkString(",")
  }

}
