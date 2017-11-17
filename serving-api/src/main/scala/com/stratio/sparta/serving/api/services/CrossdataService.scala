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

package com.stratio.sparta.serving.api.services

import java.io.File
import java.nio.file.{Files, Paths}
import javax.xml.bind.DatatypeConverter

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.api.services.CrossdataService._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.services.HdfsService
import org.apache.spark.SparkConf
import org.apache.spark.security.ConfigSecurity
import org.apache.spark.sql.catalog.{Column, Database, Table}
import org.apache.spark.sql.crossdata.XDSession

import scala.util.{Properties, Try}

class CrossdataService() extends SpartaSerializer with SLF4JLogging {

  def listTables(dbName: Option[String], temporary: Boolean): Try[Array[Table]] =
    Try {
      (dbName.notBlank, temporary) match {
        case (Some(database), true) =>
          crossdataSession.catalog.listTables(database).collect().filter(_.isTemporary)
        case (Some(database), false) =>
          crossdataSession.catalog.listTables(database).collect().filterNot(_.isTemporary)
        case (None, true) =>
          crossdataSession.catalog.listDatabases().collect().flatMap(db =>
            crossdataSession.catalog.listTables(db.name).collect()
          ).filter(_.isTemporary)
        case (None, false) =>
          crossdataSession.catalog.listDatabases().collect().flatMap(db =>
            crossdataSession.catalog.listTables(db.name).collect()
          ).filterNot(_.isTemporary)
      }
    }

  def listAllTables: Try[Array[Table]] =
    Try {
      crossdataSession.catalog.listDatabases().collect().flatMap(db =>
        crossdataSession.catalog.listTables(db.name).collect()
      )
    }

  def listDatabases: Try[Array[Database]] =
    Try(crossdataSession.catalog.listDatabases().collect())

  def listColumns(tableName: String, dbName: Option[String]): Try[Array[Column]] =
    Try {
      dbName match {
        case Some(database) =>
          crossdataSession.catalog.listColumns(database, tableName).collect()
        case None =>
          val table = crossdataSession.catalog.listDatabases().collect().flatMap(db =>
            crossdataSession.catalog.listTables(db.name).collect()
          ).find(_.name == tableName).getOrElse(throw new Exception(s"Unable to find table $tableName in XDCatalog"))
          crossdataSession.catalog.listColumns(table.database, table.name).collect()
      }
    }

  def executeQuery(query: String): Try[Array[Map[String, Any]]] =
    Try {
      if (validateQuery(query))
        crossdataSession.sql(query)
          .collect()
          .map(row => row.schema.fields.zipWithIndex.map { case (field, index) => field.name -> row.get(index) }.toMap)
      else throw new IllegalArgumentException("Invalid query, the supported queries are: CREATE TABLE ... , " +
        "CREATE TEMPORARY TABLE ..., DROP TABLE ..., TRUNCATE TABLE...,IMPORT TABLES ..., SELECT ..., " +
        "IMPORT TABLES ...," + "CREATE EXTERNAL TABLE ..., SHOW ..., DESCRIBE ... and IMPORT ...")
    }

  private def validateQuery(query: String): Boolean = {
    val upperQuery = query.toUpperCase

    upperQuery.startsWith("CREATE") || upperQuery.startsWith("DROP") || upperQuery.startsWith("TRUNCATE") ||
      upperQuery.startsWith("SELECT") || upperQuery.startsWith("SHOW") || upperQuery.startsWith("DESCRIBE") ||
      upperQuery.startsWith("IMPORT") || upperQuery.startsWith("ALTER") || upperQuery.startsWith("ANALYZE")
  }
}

object CrossdataService {

  val crossdataSession: XDSession = {
    val reference = getClass.getResource("/reference.conf").getPath
    val sparkConf = new SparkConf()
      .setAll(kerberosYarnDefaultVariables)
      .setAppName(Properties.envOrElse("MARATHON_APP_LABEL_DCOS_SERVICE_NAME", "sparta") + "-crossdata")

    if (Try(Properties.envOrElse("CROSSDATA_SERVER_SPARK_DATASTORE_SSL_ENABLE", "false").toBoolean).getOrElse(false)) {
      val vaultToken = sys.env.get("VAULT_TOKEN")
      val vaultUrl = for {
        protocol <- sys.env.get("VAULT_PROTOCOL")
        host <- sys.env.get("VAULT_HOSTS")
        port <- sys.env.get("VAULT_PORT")
      } yield s"$protocol://$host:$port"

      ConfigSecurity.prepareEnvironment(vaultToken, vaultUrl)
    }

    val xDSession = XDSession.builder()
      .config(new File(reference))
      .config(sparkConf)
      .create(Properties.envOrElse("MARATHON_APP_LABEL_DCOS_SERVICE_NAME", "sparta"))

    val jdbcDrivers = new File("/jdbc-drivers")
    if (jdbcDrivers.exists && jdbcDrivers.isDirectory)
      jdbcDrivers.listFiles()
        .filter(file => file.isFile && file.getName.endsWith("jar"))
        .foreach(file => xDSession.sparkContext.addJar(file.getAbsolutePath))

    xDSession
  }

  //scalastyle:off
  private def kerberosYarnDefaultVariables: Seq[(String, String)] = {
    val hdfsConfig = SpartaConfig.getHdfsConfig
    (HdfsService.getPrincipalName(hdfsConfig).notBlank, HdfsService.getKeyTabPath(hdfsConfig).notBlank) match {
      case (Some(principal), Some(keyTabPath)) =>
        Seq(
          ("spark.mesos.kerberos.keytabBase64", DatatypeConverter.printBase64Binary(Files.readAllBytes(Paths.get(keyTabPath)))),
          ("spark.yarn.principal", principal),
          ("spark.hadoop.yarn.resourcemanager.principal", principal)
        )
      case _ =>
        Seq.empty[(String, String)]
    }
  }

  //scalastyle:on
}
