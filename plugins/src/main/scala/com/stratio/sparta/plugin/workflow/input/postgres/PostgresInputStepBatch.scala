/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.postgres

import java.io.{Serializable => JSerializable}
import java.util.Properties

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.InputStep
import com.stratio.sparta.plugin.enumerations.SelectInput
import com.stratio.sparta.plugin.enumerations.SelectInput.SelectInput
import com.stratio.sparta.plugin.helper.SecurityHelper.{addUserToConnectionURI, getDataStoreUri}
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.workflow.lineage.JdbcLineage
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.jdbc.SpartaJdbcUtils.{getConnection, quoteTable}
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError

import scala.util.{Failure, Success, Try}

class PostgresInputStepBatch(
                          name: String,
                          outputOptions: OutputOptions,
                          ssc: Option[StreamingContext],
                          xDSession: XDSession,
                          properties: Map[String, JSerializable]
                        )
  extends InputStep[RDD](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging with JdbcLineage {


  lazy val selectExp: Option[String] = properties.getString("selectExp", None).notBlank

  lazy val url: Option[String] = properties.getString("url", None).notBlank

  lazy val selectInput: SelectInput = Try {
    properties.getString("selectInput", None).map(  sInput =>
      SelectInput.withName(sInput.toUpperCase())
    )
  }.toOption.flatten.getOrElse(SelectInput.TABLE)

  lazy val tableFromOptions: Option[String] = properties.getString("dbtable", None).notBlank
  lazy val table: Option[String]  = if (isCaseSensitive) quotedTable else tableFromOptions


  lazy val isCaseSensitive: Boolean = Try(properties.getBoolean("caseSensitiveEnabled")).getOrElse(false)

  val postgresDriver: String = DefaultEnvironmentParametersMap("POSTGRES_DRIVER").value.getOrElse("org.postgresql.Driver")

  val jdbcPropertiesMap: Map[String, String] =
    propertiesWithCustom.mapValues(_.toString).filter(_._2.nonEmpty) + ("driver" -> postgresDriver)

  val sparkConf: Map[String, String] = xDSession.conf.getAll
  val securityUri: String = getDataStoreUri(sparkConf)

  val urlWithSSL: Option[String] = url.map(inputUrl =>
    if (tlsEnable)
      addUserToConnectionURI(spartaTenant, inputUrl) + securityUri
    else
      inputUrl)

  lazy val quotedTable = for {
     nameTable <- tableFromOptions
     url <- urlWithSSL
    }
   yield quoteTable(nameTable, getConnection(new JDBCOptions(url, nameTable, jdbcPropertiesMap), name))


  override lazy val lineageResource: String = table.getOrElse("")
  override lazy val lineageUri: String = url.getOrElse("")
  override lazy val tlsEnable: Boolean = Try(properties.getBoolean("tlsEnabled")).getOrElse(false)

  override def validate(options: Map[String, String]): ErrorValidations = {

    val validationSeq = Seq[(HasError,String)](
      !SdkSchemaHelper.isCorrectTableName(name) -> s"The step name $name is not valid",
      urlWithSSL.isEmpty -> "The Url cannot be empty",
      (selectInput == SelectInput.TABLE && tableFromOptions.isEmpty) -> s"the table must be provided",
      (selectInput == SelectInput.QUERY && selectExp.isEmpty) -> s"the query must be provided",
      (tlsEnable && securityUri.isEmpty) -> s"when TLS is enabled the sparkConf must contain the security options",
      (debugOptions.isDefined && !validDebuggingOptions) -> s"$errorDebugValidation"
    )

    ErrorValidationsHelper.validate(validationSeq, name)
  }

  //Dummy function on batch inputs that generates DataSets with schema
  def init(): DistributedMonad[RDD] = {
    throw new Exception("Not used on inputs that generates DataSets with schema")
  }

  override def lineageProperties(): Map[String, String] = getJdbcLineageProperties(InputStep.StepType)

  override def initWithSchema(): (DistributedMonad[RDD], Option[StructType]) = {
    require(urlWithSSL.nonEmpty, "Postgres url must be provided")

    if (selectInput == SelectInput.TABLE) {
      require(table.nonEmpty, "Table must be provided")
    }else {
      require(selectExp.nonEmpty, "Query expression must be provided")
    }

    val userOptions = propertiesWithCustom.flatMap { case (key, value) =>
      if (key != url.get)
        Option(key -> value.toString)
      else None
    }
    val userProperties = new Properties()
    userOptions.foreach { case (key, value) =>
      if (value.toString.nonEmpty)
        userProperties.put(key, value.toString)
    }

    userProperties.put("driver", postgresDriver)
    val query : String = s"(${selectExp.getOrElse("")} ) as T"
    val tableOrQuery: String = if (selectInput == SelectInput.QUERY) query else table.get


    val df = xDSession.read.jdbc(urlWithSSL.get, tableOrQuery, userProperties)
    (df.rdd, Option(df.schema))
  }

}