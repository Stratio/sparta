/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.streaming.datasource.receiver

import akka.event.slf4j.SLF4JLogging
import org.apache.commons.lang.StringUtils
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.catalyst.analysis.UnresolvedRelation
import org.apache.spark.sql.catalyst.catalog.CatalogTable
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.execution.datasources.CreateTempViewUsing
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.streaming.datasource.models.OffsetConditions

import scala.util.Try
import scala.util.matching.Regex

trait DatasourceRDDHelper {

  private val complexOperands = Seq("UNION","JOIN","CLUSTER BY","INTERSECT", "HAVING")
  private val delimiters = Seq("LIMIT","OFFSET","ORDER","GROUP BY")

  def selectAll(table: String) =  s"select * from $table"

  def checkIfComplexQuery(inputQuery: String) : Boolean = {
    val upperCaseQuery = inputQuery.toUpperCase
    StringUtils.countMatches(upperCaseQuery, "SELECT") > 1 ||
      complexOperands.exists(operand => upperCaseQuery.contains(operand))
  }

  def retrieveWhereCondition(inputQuery: String): Option[String] = {
    if(inputQuery.toUpperCase.contains("WHERE")) {
      Try(inputQuery.split("(?i)WHERE").last
        .split(s"""(?<=.+)(?i)(${delimiters.mkString("|")})""").head).toOption
    } else None
  }

  def retrieveBasicQuery(inputQuery : String): Option[String] =
    Try(inputQuery.split(s"(?i)(WHERE|${delimiters.mkString("|")})").head).toOption

  def possibleConflictsWRTColumns(initialWhereCondition: Option[String],
                                  offsetConditions: OffsetConditions): Boolean =
    initialWhereCondition match {
      case None => false
      case Some(cond) =>
        offsetConditions.fromOffset.map(_.name).intersect(cond.split("\\W+")).nonEmpty
    }

}

object DatasourceRDDHelper extends SLF4JLogging {

  val InitTableName = "initTable"

  def retrieveValidPollingTable(sparkSession: SparkSession, parsedPlan: LogicalPlan): Option[CatalogTable] = {
    val tablesInQuery = parsedPlan.collectLeaves().collect {
      case UnresolvedRelation(tableIdentifier) => tableIdentifier
    }
    if (tablesInQuery.length == 1) {
      for {
        tableMetadata <- Try(sparkSession.sessionState.catalog.getTableMetadata(tablesInQuery.head)).toOption // TODO validation
        if tableMetadata.viewText.isEmpty
        datasourceProvider <- tableMetadata.provider
        if pushdownQueryJdbcConnectors.contains(datasourceProvider)
        dbTable <- tableMetadata.storage.properties.get(JDBCOptions.JDBC_TABLE_NAME).orElse(tableMetadata.properties.get(JDBCOptions.JDBC_TABLE_NAME))
        if !dbTable.toUpperCase.contains("SELECT")
      } yield tableMetadata
    } else {
      None
    }
  }

  def getCreateViewIncludingQuery(xdTable: CatalogTable, originalQuery: String, additionalSqlQuery: String): CreateTempViewUsing = {
    val newProperties =
      xdTable.storage.properties.map { case (JDBCOptions.JDBC_TABLE_NAME, tName) =>

        val queryWithJDBCRelation: String = {
          val queryReplacingQualifiedTable = regexWordWithBoundaries(xdTable.identifier.toString).replaceAllIn(s"$originalQuery $additionalSqlQuery", tName)
          regexWordWithBoundaries(xdTable.identifier.table).replaceAllIn(queryReplacingQualifiedTable, tName)
        }
        val tmpQuery = s"($queryWithJDBCRelation) tmpalias"
        log.debug(s"Query used as dbtable: $tmpQuery")
        (JDBCOptions.JDBC_TABLE_NAME, tmpQuery)
      case other => other
      }

    CreateTempViewUsing(
      tableIdent = TableIdentifier(InitTableName),
      userSpecifiedSchema = None,
      replace = true,
      global = false,
      provider = xdTable.provider.getOrElse(throw new RuntimeException("Datasource provider cannot be empty")),
      options = newProperties
    )
  }

  lazy val pushdownQueryJdbcConnectors: Seq[String] = {
    val basicNames =
      Seq(
        "org.apache.spark.sql.jdbc",
        "org.apache.spark.sql.execution.datasources.jdbc",
        "jdbc",
        "com.stratio.crossdata.connector.postgresql"
      )
    basicNames ++ basicNames.map(_ + ".DefaultSource") ++ sys.env.get("DATASOURCE_PUSHDOWN_QUERY_EXTRA_PROVIDER").toSeq
  }

  private def regexWordWithBoundaries(str: String): Regex = s"\\b$str\\b".r
}