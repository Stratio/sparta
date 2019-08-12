/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.input.arangodb

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.arangodb.entity.BaseDocument
import com.arangodb.spark.rdd.ArangoRDD
import com.arangodb.spark.{ArangoSpark, ReadOptions}
import com.fasterxml.jackson.databind.ObjectMapper
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.InputStep
import com.stratio.sparta.plugin.helper.{SchemaHelper, SecurityHelper}
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.json4s.jackson.Serialization.read

import scala.util._

case class ArangoHostPort(host: String, port: String)

class ArangoDBInputStepBatch(
                              name: String,
                              outputOptions: OutputOptions,
                              ssc: Option[StreamingContext],
                              xDSession: XDSession,
                              properties: Map[String, JSerializable]
                            )
  extends InputStep[RDD](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {


  import com.stratio.sparta.plugin.models.SerializationImplicits._

  lazy val hosts: Seq[ArangoHostPort] = read[Seq[ArangoHostPort]](properties.getString("hosts", None).notBlank.getOrElse("[]"))

  lazy val arangoUriList = getArangoURI(hosts)
  lazy val databaseName = properties.getString("databaseName", None).notBlank.getOrElse("_system")
  lazy val username = properties.getString("username", None).notBlank.getOrElse("root")
  lazy val password = properties.getString("password", None).notBlank.getOrElse("openSesame")
  lazy val arangoCollection = properties.getString("collection", None).notBlank.getOrElse("")
  lazy val tlsEnabled = Try(properties.getBoolean("tlsEnabled")).getOrElse(false)
  lazy val hasSchema = Try(properties.getBoolean("hasSchema")).getOrElse(false)
  lazy val hasFilterQuery = Try(properties.getBoolean("addFilterQuery")).getOrElse(false)
  lazy val filterQuery = properties.getString("filterQuery", None).notBlank.getOrElse("")
  lazy val maxConnections = properties.getString("maxConnections", "1").toInt

  lazy val schemaProvided = properties.getString("arangoSchema", None).notBlank.getOrElse("")
  lazy val arangoSchema = SchemaHelper.getSparkSchemaFromString(schemaProvided)

  lazy val addArangoId = Try(properties.getBoolean("addArangoId")).getOrElse(false)
  lazy val addArangoKey = Try(properties.getBoolean("addArangoKey")).getOrElse(false)
  lazy val addArangoRevision = Try(properties.getBoolean("addArangoRevision")).getOrElse(false)

  lazy val jsonParserOptions: Map[String, String] = Try(properties.getOptionsList("jsonParserOptions", "parserKey", "parserValue"))
    .getOrElse(Map.empty)

  lazy val sparkConf = xDSession.conf.getAll
  lazy val securityOpts = if (tlsEnabled) SecurityHelper.arangoSecurityOptions(sparkConf) else Map.empty

  lazy val arangoOptions: ReadOptions = {
    if (tlsEnabled) new ReadOptions()
      .database(databaseName)
      .hosts(arangoUriList)
      .collection(arangoCollection)
      .maxConnections(maxConnections)
      .useSsl(true)
      .user(username)
      .password(password)

    else new ReadOptions()
      .database(databaseName)
      .hosts(arangoUriList)
      .collection(arangoCollection)
      .user(username)
      .password(password)
      .maxConnections(maxConnections)
  }


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
      arangoCollection.isEmpty -> "It's mandatory to specify a collection",
      username.isEmpty -> "It's mandatory to specify an user"

    ) ++ emptyValues

    ErrorValidationsHelper.validate(validationSeq, name)
  }

  //Dummy function on batch inputs that generates DataSets with schema
  def init(): DistributedMonad[RDD] = {
    throw new Exception("Not used on inputs that generates DataSets with schema")
  }

  //scalastyle:off
  override def initWithSchema(): (DistributedMonad[RDD], Option[StructType]) = {
    require(arangoUriList.trim.nonEmpty, "It's mandatory to define at least one host and port")
    require(arangoCollection.trim.nonEmpty, "It's mandatory to define an AnrangoDB collection")

    val arangoRDD: ArangoRDD[BaseDocument] = {
      if (hasFilterQuery)
        ArangoSpark.load[BaseDocument](xDSession.sparkContext, arangoCollection, arangoOptions).filter(filterQuery)
      else
        ArangoSpark.load[BaseDocument](xDSession.sparkContext, arangoCollection, arangoOptions)
    }

    import xDSession.implicits._

    val arangoDF = xDSession.createDataset(
      arangoRDD.map { baseDoc =>
        val propMap = baseDoc.getProperties
        if (addArangoId) baseDoc.getProperties.putIfAbsent("_id", baseDoc.getId)
        if (addArangoKey) baseDoc.getProperties.putIfAbsent("_key", baseDoc.getKey)
        if (addArangoRevision) baseDoc.getProperties.putIfAbsent("_rev", baseDoc.getRevision)
        new ObjectMapper().writeValueAsString(propMap)
      }
    )

    val dataFrameReader = xDSession.read.options(jsonParserOptions)

    val dataFrameReaderWithSchema = if (hasSchema) {

      val newColumns =
        Seq(
          if (addArangoId) Some(StructField("_id", StringType)) else None,
          if (addArangoKey) Some(StructField("_key", StringType)) else None,
          if (addArangoRevision) Some(StructField("_rev", StringType)) else None
        ).flatten

      val finalSchema = (arangoSchema /: newColumns) { (schemaAcc, column) => schemaAcc.add(column) }
      dataFrameReader.schema(finalSchema)

    } else dataFrameReader

    val df = dataFrameReaderWithSchema.json(arangoDF)

    (df.rdd, Option(df.schema))
  }

  def getArangoURI(arangoHostsPorts: Seq[ArangoHostPort]): String = {
    arangoHostsPorts.map(hostport => hostport.host + ":" + hostport.port).mkString(",")
  }

}

object ArangoDBInputStepBatch {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }
}