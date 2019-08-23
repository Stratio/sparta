/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.output.arangodb

import com.arangodb.ArangoDB
import com.arangodb.entity.BaseDocument
import com.arangodb.entity.MultiDocumentEntity
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.workflow.step.OutputStep.TableNameKey
import com.stratio.sparta.plugin.TemporalSparkContext
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.crossdata.XDSession
import org.scalatest.{BeforeAndAfterAll, ShouldMatchers}

import scala.util.{Failure, Success, Try}

class ArangoDBOutputStepIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  private lazy val config = ConfigFactory.load()

  val arangoHost = Try(config.getString("arangodb.host")) match {
    case Success(configHost) =>
      log.info(s"Arango from config: $configHost")
      s"$configHost"
    case Failure(_) =>
      log.info(s"Arango default config")
      "localhost"
  }

  val arangoPort = Try(config.getString("arangodb.port")) match {
    case Success(configPort) =>
      log.info(s"Arango from config: $configPort")
      s"$configPort"
    case Failure(_) =>
      log.info(s"Arango default config")
      "8529"
  }

  lazy val arangoHosts =
    s"""[
       |{
       |  "host": "$arangoHost",
       |  "port": $arangoPort
       |}
       |]
    """.stripMargin

  def getArangoConnection(): ArangoDB = {
    val arangoDB = new ArangoDB.Builder().host(arangoHost, arangoPort.toInt).user("root").password("openSesame").build
    arangoDB
  }

     //scalastyle:off
  "ArangodbOutput" should "save a dataframe" in {

    val xdSession = XDSession.builder().config(sc.getConf).create("dummyUser")

    import xdSession.implicits._
    val df = sc.parallelize(Seq(Person("Marcos", 18, Seq("coding", "sports"),"777"), Person("Juan", 21, Seq("sports"), "778"), Person("Jose", 26, Seq("maths", "music"), "779"))).toDS().toDF

    val outputProperties = Map(
      "hosts" -> arangoHosts,
      "databaseName" -> "mydb",
      "username" -> "root",
      "password" -> "openSesame",
      "tlsEnabled" -> "false"
    )

    val arangoOutput = new ArangoDBOutputStepBatch(
      "dummy",
      sparkSession,
      outputProperties
    )

    arangoOutput.save(df, SaveModeEnum.Append, Map(TableNameKey -> "sparta"))
    lazy val arangoDB = getArangoConnection().db(outputProperties("databaseName")).collection("sparta")
    import java.util
    val keys = util.Arrays.asList("777", "778", "779")

    val documents: MultiDocumentEntity[BaseDocument] = arangoDB.getDocuments(keys, classOf[BaseDocument])

    val ndocuments = documents.getDocuments.size()
    ndocuments should be (3)
  }

}

case class Person(name: String, age: Int, skills: Seq[String], _key: String) extends Serializable