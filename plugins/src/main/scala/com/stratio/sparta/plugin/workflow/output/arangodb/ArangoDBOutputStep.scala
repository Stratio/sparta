/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */


package com.stratio.sparta.plugin.workflow.output.arangodb

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.arangodb.ArangoDB
import com.arangodb.spark.{SpartaArangoSpark, WriteOptions}
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession
import org.json4s.jackson.Serialization.read

import scala.util._


case class ArangoHostPort(host: String, port: String)

case class ArangoConnectionContext(uri: String, user: String, password: String, useSSL: Boolean)

class ArangoDBOutputStepBatch(
                          name: String,
                          xDSession: XDSession,
                          properties: Map[String, JSerializable]
                        ) extends OutputStep(name, xDSession, properties) with SLF4JLogging {

  import com.stratio.sparta.plugin.models.SerializationImplicits._

  lazy val hosts: Seq[ArangoHostPort] = read[Seq[ArangoHostPort]](properties.getString("hosts", None).notBlank.getOrElse("[]"))

  lazy val arangoUriList = getArangoURI(hosts)
  lazy val databaseName = properties.getString("databaseName", None).notBlank.getOrElse("_system")
  lazy val username = properties.getString("username", None).notBlank.getOrElse("root")
  lazy val password = properties.getString("password", None).notBlank.getOrElse("openSesame")
  lazy val tlsEnable = Try(properties.getBoolean("tlsEnabled")).getOrElse(false)
  lazy val failOnConflict = Try(properties.getBoolean("failOnConflict")).getOrElse(true)
  lazy val maxConnections = properties.getString("maxConnections", "1").toInt

  lazy val jsonParserOptions: Map[String, String] = Try(properties.getOptionsList("jsonParserOptions", "parserKey", "parserValue"))
    .getOrElse(Map.empty)

  override def supportedSaveModes: Seq[SaveModeEnum.Value] =
    Seq(SaveModeEnum.Append, SaveModeEnum.Upsert)

  lazy val arangoOptions: WriteOptions = {
    if (tlsEnable) new WriteOptions()
      .database(databaseName)
      .hosts(arangoUriList)
      .useSsl(true)
      .user(username)
      .password(password)
      .maxConnections(maxConnections)

    else new WriteOptions()
      .database(databaseName)
      .hosts(arangoUriList)
      .maxConnections(maxConnections)
      .user(username)
      .password(password)
  }

  implicit val arangoConnectionContext = ArangoConnectionContext(arangoUriList, username, password, tlsEnable)


  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {

    val emptyValues: Seq[(HasError, String)] = hosts.zipWithIndex.flatMap({ case (element: ArangoHostPort, index: Int) =>
      Seq(
        element.host.isEmpty -> s"Element ${index + 1}: Host is empty",
        element.port.isEmpty -> s"Element ${index + 1}: Port is empty"
      )
    })

    val validationSeq = Seq[(HasError, String)](
      !SdkSchemaHelper.isCorrectTableName(name) -> s"The step name $name is not valid",
      hosts.isEmpty -> "It's mandatory to specify at least one host",
      databaseName.isEmpty -> "It's mandatory to specify the database name",
      username.isEmpty -> "It's mandatory to specify an user"

    ) ++ emptyValues

    ErrorValidationsHelper.validate(validationSeq, name)
  }

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {

    val arangoCollection = getTableNameFromOptions(options)
    ArangoDBOutputStepBatch.createArangoDbCollection(databaseName, arangoCollection)

    if(saveMode == SaveModeEnum.Upsert)
      SpartaArangoSpark.upsertDF(dataFrame, arangoCollection, arangoOptions)
    else
      SpartaArangoSpark.saveDF(dataFrame, arangoCollection, arangoOptions, failOnConflict)
  }


  object ArangoDBOutputStepBatch {

    var arangoConnection: Option[ArangoDB] = None

    def getOrCreateArangoDBConnection(implicit arangoConnectionContext: ArangoConnectionContext): ArangoDB = {
      arangoConnection.getOrElse {
        val builder = new ArangoDB.Builder()
          .useSsl(arangoConnectionContext.useSSL)
          .user(arangoConnectionContext.user)
          .password(arangoConnectionContext.password)
        val connectionBuilder = arangoConnectionContext.uri.split(",").foldLeft(builder) { (connection, hostPort) =>
          val splittedHost = hostPort.split(":")
          connection.host(splittedHost.head, splittedHost.last.toInt)
        }
        val newConnection = connectionBuilder.build
        arangoConnection = Option(newConnection)
        newConnection
      }
    }

    def createArangoDbCollection(database: String, collection: String)(implicit arangoConnectionContext: ArangoConnectionContext): Unit = {
      val arangoConnection = getOrCreateArangoDBConnection

      if (!arangoConnection.getDatabases.contains(database))
        arangoConnection.createDatabase(database)

      if (!arangoConnection.db(database).collection(collection).exists())
        arangoConnection.db(database).createCollection(collection)
    }
  }


  def getArangoURI(arangoHostsPorts: Seq[ArangoHostPort]): String = {
    arangoHostsPorts.map(hostport => hostport.host + ":" + hostport.port).mkString(",")
  }

}
