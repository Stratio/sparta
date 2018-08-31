/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mongodb

import com.stratio.datasource.mongodb.config.MongodbConfig
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import com.typesafe.config.ConfigException.Missing
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.crossdata.XDSession
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
class MongoOutputStepIT extends TemporalSparkContext with Matchers with BeforeAndAfterAll {

  val dbtestname = "testspartadb"

  val mongoHost = Try(ConfigFactory.load().getString("mongo.host")) match {
    case Success(configHost) =>
      log.info(s"MongoDB host from config: $configHost")
      configHost
    case Failure(_: Missing) =>
      log.info(s"MongoDB host from default")
      "localhost"
    case Failure(exception) =>
      log.info(s"Error while reading config, fallback to localhost", exception)
      "localhost"
  }

  trait CommonValues {
    val xdSession = XDSession.builder().config(sc.getConf).create("dummyUser")

    import xdSession.implicits._
    val data = sc.parallelize(Seq(Person(1L, "Marcos", 18), Person(2L, "Juan", 21), Person(3L, "Jose", 26))).toDS().toDF
  }

  trait WithMongoOutput extends CommonValues {
    val properties = Map(
      "hosts" -> JsoneyString(s"""
                                 |[{
                                 |  "host":"$mongoHost",
                                 |  "port":27017
                                 |}]
                               """.stripMargin),
      "dbName" -> dbtestname
    )
    val mongoOutput = new MongoDbOutputStep("mongo.out", sparkSession, properties)
  }

  "MongoDB" should "save not validate a MongoDB configuration without port" in {

    val properties = Map(
      "hosts" -> JsoneyString(s"""
                                 |[{
                                 |  "host":"$mongoHost"
                                 |}]
                               """.stripMargin),
      "dbName" -> dbtestname
    )
    val mongoOutput = new MongoDbOutputStep("mongo.out", sparkSession, properties)

    val validations = mongoOutput.validate()
    validations.valid shouldBe false
    validations.messages should have length 1

  }


  it should "save a dataFrame with mode 'Append'" in new WithMongoOutput {
    val validations = mongoOutput.validate()
    validations.valid shouldBe true

    mongoOutput.save(data, SaveModeEnum.Append, Map(mongoOutput.TableNameKey -> "sparta"))

    val loadData = xdSession.read.format(MongoDbOutputStep.MongoDbSparkDatasource)
      .options(Map(
        MongodbConfig.Host -> mongoOutput.hosts,
        MongodbConfig.Database -> dbtestname,
        MongodbConfig.Collection -> "sparta"))
      .load()

    loadData.count() shouldBe 3
  }

  it should "save a dataFrame with mode 'Upsert' by specifying a custom primary key" in new WithMongoOutput {
    val validations = mongoOutput.validate()
    validations.valid shouldBe true

    val collection = "spartaupsert"
    mongoOutput.save(data, SaveModeEnum.Upsert, Map(mongoOutput.TableNameKey -> collection, mongoOutput.PrimaryKey -> "pkid"))

    val loadData = xdSession.read.format(MongoDbOutputStep.MongoDbSparkDatasource)
      .options(Map(
        MongodbConfig.Host -> mongoOutput.hosts,
        MongodbConfig.Database -> dbtestname,
        MongodbConfig.Collection -> collection))
      .load()

    loadData.count() shouldBe 3

    import xdSession.implicits._

    val dataUpdate = sc.parallelize(Seq(Person(2L, "Juana", 21), Person(4L, "Juan", 90))).toDS().toDF
    mongoOutput.save(dataUpdate, SaveModeEnum.Upsert, Map(mongoOutput.TableNameKey -> collection, mongoOutput.PrimaryKey -> "pkid"))

    loadData.count() shouldBe 4
    loadData.as[Person].collect().to should contain allOf (Person(2L, "Juana", 21), Person(4L, "Juan", 90))

  }
}

case class Person(pkid: Long, name: String, age: Int) extends Serializable