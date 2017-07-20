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

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.models.SpartaSerializer
import org.apache.spark.SparkConf
import org.apache.spark.sql.catalog.{Column, Database, Table}
import org.apache.spark.sql.crossdata.XDSession
import CrossdataService._

import scala.util.Try

class CrossdataService() extends SpartaSerializer with SLF4JLogging {

  private val MaxQueryResults = 300

  def listTables(dbName: Option[String], temporary: Boolean): Try[Array[Table]] =
    Try {
      (dbName, temporary) match {
        case (Some(database), true) => crossdataSession.catalog.listTables(database).collect().filter(_.isTemporary)
        case (Some(database), false) => crossdataSession.catalog.listTables(database).collect().filterNot(_.isTemporary)
        case (None, true) => crossdataSession.catalog.listTables.collect().filter(_.isTemporary)
        case (None, false) => crossdataSession.catalog.listTables.collect().filterNot(_.isTemporary)
      }
    }

  def listAllTables: Try[Array[Table]] =
    Try {
      crossdataSession.catalog.listTables.collect()
    }

  def listDatabases(): Try[Array[Database]] =
    Try(crossdataSession.catalog.listDatabases().collect())

  def listColumns(tableName: String, dbName: Option[String]): Try[Array[Column]] =
    Try {
      dbName match {
        case Some(database) => crossdataSession.catalog.listColumns(database, tableName).collect()
        case None => crossdataSession.catalog.listColumns(tableName).collect()
      }
    }

  def executeQuery(query: String): Try[Array[Map[String, Any]]] =
    Try {
      if (validateQuery(query))
        crossdataSession.sql(query)
          .limit(MaxQueryResults)
          .collect()
          .map(row => row.schema.fields.zipWithIndex.map { case (field, index) => field.name -> row.get(index) }.toMap)
      else throw new IllegalArgumentException("Invalid query, the supported queries are: CREATE TABLE ... , " +
        "CREATE TEMPORARY TABLE ..., DROP TEMPORARY TABLE ..., IMPORT TABLES ..., SELECT ..., IMPORT TABLES ...," +
        "CREATE EXTERNAL TABLE ..., SHOW ..., DESCRIBE ... and IMPORT ...")
    }

  private def validateQuery(query: String): Boolean = {
    val upperQuery = query.toUpperCase

    upperQuery.startsWith("CREATE") || upperQuery.startsWith("DROP TEMPORARY TABLE") ||
      upperQuery.startsWith("SELECT") || upperQuery.startsWith("SHOW") || upperQuery.startsWith("DESCRIBE") ||
      upperQuery.startsWith("IMPORT")
  }
}

object CrossdataService {

  private lazy val reference = getClass.getResource("/reference.conf").getPath
  private lazy val sparkConf = new SparkConf()
    .setMaster("local[*]")
    .setAppName("Crossdata-API")
    .set("spark.ui.port", "4045")

  lazy val crossdataSession = XDSession.builder()
    .config(new File(reference))
    .config(sparkConf)
    .create("dummyUser")
}
