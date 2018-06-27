/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.services

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import org.apache.spark.sql.catalog.{Column, Database, Table}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.json.RowJsonHelper._

import scala.util.{Failure, Success, Try}

class CrossdataService() extends SLF4JLogging {

  def listTables(dbName: Option[String], temporary: Boolean, userId: Option[String]): Try[Array[Table]] =
    Try {
      (dbName.notBlank, temporary) match {
        case (Some(database), true) =>
          getOrCreateStandAloneXDSession(userId).catalog.listTables(database).collect().filter(_.isTemporary)
        case (Some(database), false) =>
          getOrCreateStandAloneXDSession(userId).catalog.listTables(database).collect().filterNot(_.isTemporary)
        case (None, true) =>
          getOrCreateStandAloneXDSession(userId).catalog.listDatabases().collect().flatMap(db =>
            getOrCreateStandAloneXDSession(userId).catalog.listTables(db.name).collect()
          ).filter(_.isTemporary)
        case (None, false) =>
          getOrCreateStandAloneXDSession(userId).catalog.listDatabases().collect().flatMap(db =>
            getOrCreateStandAloneXDSession(userId).catalog.listTables(db.name).collect()
          ).filterNot(_.isTemporary)
      }
    }

  def listAllTables(userId: Option[String]): Try[Array[Table]] =
    Try {
      getOrCreateStandAloneXDSession(userId).catalog.listDatabases().collect().flatMap(db =>
        Try(getOrCreateStandAloneXDSession(userId).catalog.listTables(db.name).collect()) match {
          case Success(table) => Option(table)
          case Failure(e) =>
            log.debug(s"Error obtaining tables from database ${db.name}", e)
            None
        }
      ).flatten
    }

  def listDatabases(userId: Option[String]): Try[Array[Database]] =
    Try(getOrCreateStandAloneXDSession(userId).catalog.listDatabases().collect())

  def listColumns(tableName: String, dbName: Option[String], userId: Option[String]): Try[Array[Column]] =
    Try {
      dbName match {
        case Some(database) =>
          getOrCreateStandAloneXDSession(userId).catalog.listColumns(database, tableName).collect()
        case None =>
          val table = getOrCreateStandAloneXDSession(userId).catalog.listDatabases().collect().flatMap(db =>
            getOrCreateStandAloneXDSession(userId).catalog.listTables(db.name).collect()
          ).find(_.name == tableName).getOrElse(throw new Exception(s"Unable to find table $tableName in XDCatalog"))
          getOrCreateStandAloneXDSession(userId).catalog.listColumns(table.database, table.name).collect()
      }
    }

  def executeQuery(query: String, userId: Option[String]): Try[Array[Map[String, Any]]] =
    Try {
      getOrCreateStandAloneXDSession(userId).sql(query.trim)
        .collect()
        .map { row =>
          row.schema.fields.zipWithIndex.map { case (field, index) =>
            val oldValue = row.get(index)
            val newValue = oldValue match {
              case v: java.math.BigDecimal => BigDecimal(v)
              case v: GenericRowWithSchema => toJSON(v, Map.empty[String, String])
              case _ => oldValue
            }
            field.name -> newValue
          }.toMap
        }
    }
}