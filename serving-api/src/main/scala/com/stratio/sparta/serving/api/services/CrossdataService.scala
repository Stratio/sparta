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

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import org.apache.spark.sql.catalog.{Column, Database, Table}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.json.RowJsonHelper._

import scala.util.{Failure, Success, Try}

class CrossdataService() extends SLF4JLogging {

  def listTables(dbName: Option[String], temporary: Boolean): Try[Array[Table]] =
    Try {
      (dbName.notBlank, temporary) match {
        case (Some(database), true) =>
          getOrCreateXDSession().catalog.listTables(database).collect().filter(_.isTemporary)
        case (Some(database), false) =>
          getOrCreateXDSession().catalog.listTables(database).collect().filterNot(_.isTemporary)
        case (None, true) =>
          getOrCreateXDSession().catalog.listDatabases().collect().flatMap(db =>
            getOrCreateXDSession().catalog.listTables(db.name).collect()
          ).filter(_.isTemporary)
        case (None, false) =>
          getOrCreateXDSession().catalog.listDatabases().collect().flatMap(db =>
            getOrCreateXDSession().catalog.listTables(db.name).collect()
          ).filterNot(_.isTemporary)
      }
    }

  def listAllTables: Try[Array[Table]] =
    Try {
      getOrCreateXDSession().catalog.listDatabases().collect().flatMap(db =>
        Try(getOrCreateXDSession().catalog.listTables(db.name).collect()) match {
          case Success(table) => Option(table)
          case Failure(e) =>
            log.debug(s"Error obtaining tables from database ${db.name}", e)
            None
        }
      ).flatten
    }

  def listDatabases: Try[Array[Database]] =
    Try(getOrCreateXDSession().catalog.listDatabases().collect())

  def listColumns(tableName: String, dbName: Option[String]): Try[Array[Column]] =
    Try {
      dbName match {
        case Some(database) =>
          getOrCreateXDSession().catalog.listColumns(database, tableName).collect()
        case None =>
          val table = getOrCreateXDSession().catalog.listDatabases().collect().flatMap(db =>
            getOrCreateXDSession().catalog.listTables(db.name).collect()
          ).find(_.name == tableName).getOrElse(throw new Exception(s"Unable to find table $tableName in XDCatalog"))
          getOrCreateXDSession().catalog.listColumns(table.database, table.name).collect()
      }
    }

  def executeQuery(query: String): Try[Array[Map[String, Any]]] =
    Try {
      if (validateQuery(query.trim))
        getOrCreateXDSession().sql(query.trim)
          .collect()
          .map { row =>
            row.schema.fields.zipWithIndex.map { case (field, index) =>
              val oldValue = row.get(index)
              val newValue = oldValue match {
                case v: java.math.BigDecimal => BigDecimal(v)
                case v: GenericRowWithSchema => {
                  toJSON(v, Map.empty[String, String])
                }
                case _ => oldValue
              }
              field.name -> newValue
            }.toMap
          }
      else throw new IllegalArgumentException("Invalid query, the supported queries are: CREATE TABLE ... , " +
        "CREATE TEMPORARY TABLE ..., DROP TABLE ..., TRUNCATE TABLE...,IMPORT TABLES ..., SELECT ..., " +
        "IMPORT TABLES ..., CREATE EXTERNAL TABLE ..., SHOW ..., ANALYZE ..., DESCRIBE ... and IMPORT ...")
    }

  private def validateQuery(query: String): Boolean = {
    val upperQuery = query.toUpperCase

    upperQuery.startsWith("CREATE") || upperQuery.startsWith("DROP") || upperQuery.startsWith("TRUNCATE") ||
      upperQuery.startsWith("SELECT") || upperQuery.startsWith("SHOW") || upperQuery.startsWith("DESCRIBE") ||
      upperQuery.startsWith("IMPORT") || upperQuery.startsWith("ALTER") || upperQuery.startsWith("ANALYZE")
  }
}
