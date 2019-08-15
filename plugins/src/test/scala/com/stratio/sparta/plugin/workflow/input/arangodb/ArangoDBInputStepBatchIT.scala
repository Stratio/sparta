/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.input.arangodb

import com.arangodb.entity.BaseDocument
import com.arangodb.{ArangoDB, ArangoDBException}
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.OutputOptions
import com.stratio.sparta.plugin.TemporalSparkContext
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types._
import org.json4s.jackson.Serialization.read
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, ShouldMatchers}

import scala.util.{Failure, Random, Success, Try}


case class Got(name: String, surname: String, alive: Boolean, age: Option[Int], traits: Seq[String])

@RunWith(classOf[JUnitRunner])
class ArangoDBInputStepBatchIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

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

  //scalastyle:off
  private lazy val randomCollectionName = (Random.alphanumeric take 5).dropWhile(!_.isLetter).mkString

  override def beforeAll(): Unit = {
    prepareArangoForTests(getArangoConnection())
  }


  def getArangoConnection(): ArangoDB = {
    val arangoDB = new ArangoDB.Builder().host(arangoHost, arangoPort.toInt).user("root").password("openSesame").build
    arangoDB
  }

  def prepareArangoForTests(arangoDB: ArangoDB): Unit = {
    val dbName = "mydb"
    try {
      arangoDB.createDatabase(dbName)
      log.info("Database created: " + dbName)
    } catch {
      case e: ArangoDBException =>
        System.err.println("Failed to create database: " + dbName + "; " + e.getMessage)
    }

    val collectionName = randomCollectionName
    try {
      val myArangoCollection = arangoDB.db(dbName).createCollection(collectionName)
      log.info("Collection created: " + myArangoCollection.getName)
    } catch {
      case e: ArangoDBException =>
        System.err.println("Failed to create collection: " + collectionName + "; " + e.getMessage)
    }


    val data =
      """[
    { "name": "Robert", "surname": "Baratheon", "alive": false, "traits": ["A","H","C"] },
    { "name": "Jaime", "surname": "Lannister", "alive": true, "age": 36, "traits": ["A","F","B"] },
    { "name": "Catelyn", "surname": "Stark", "alive": false, "age": 40, "traits": ["D","H","C"] },
    { "name": "Cersei", "surname": "Lannister", "alive": true, "age": 36, "traits": ["H","E","F"] },
    { "name": "Daenerys", "surname": "Targaryen", "alive": true, "age": 16, "traits": ["D","H","C"] },
    { "name": "Jorah", "surname": "Mormont", "alive": false, "traits": ["A","B","C","F"] },
    { "name": "Petyr", "surname": "Baelish", "alive": false, "traits": ["E","G","F"] },
    { "name": "Viserys", "surname": "Targaryen", "alive": false, "traits": ["O","L","N"] },
    { "name": "Jon", "surname": "Snow", "alive": true, "age": 16, "traits": ["A","B","C","F"] },
    { "name": "Sansa", "surname": "Stark", "alive": true, "age": 13, "traits": ["D","I","J"] },
    { "name": "Arya", "surname": "Stark", "alive": true, "age": 11, "traits": ["C","K","L"] },
    { "name": "Robb", "surname": "Stark", "alive": false, "traits": ["A","B","C","K"] },
    { "name": "Theon", "surname": "Greyjoy", "alive": true, "age": 16, "traits": ["E","R","K"] },
    { "name": "Bran", "surname": "Stark", "alive": true, "age": 10, "traits": ["L","J"] },
    { "name": "Joffrey", "surname": "Baratheon", "alive": false, "age": 19, "traits": ["I","L","O"] },
    { "name": "Sandor", "surname": "Clegane", "alive": true, "traits": ["A","P","K","F"] },
    { "name": "Tyrion", "surname": "Lannister", "alive": true, "age": 32, "traits": ["F","K","M","N"] },
    { "name": "Khal", "surname": "Drogo", "alive": false, "traits": ["A","C","O","P"] },
    { "name": "Tywin", "surname": "Lannister", "alive": false, "traits": ["O","M","H","F"] },
    { "name": "Davos", "surname": "Seaworth", "alive": true, "age": 49, "traits": ["C","K","P","F"] },
    { "name": "Samwell", "surname": "Tarly", "alive": true, "age": 17, "traits": ["C","L","I"] },
    { "name": "Stannis", "surname": "Baratheon", "alive": false, "traits": ["H","O","P","M"] },
    { "name": "Margaery", "surname": "Tyrell", "alive": false, "traits": ["M","D","B"] },
    { "name": "Jeor", "surname": "Mormont", "alive": false, "traits": ["C","H","M","P"] },
    { "name": "Talisa", "surname": "Maegyr", "alive": false, "traits": ["D","C","B"] },
    { "name": "Tormund", "surname": "Giantsbane", "alive": true, "traits": ["C","P","A","I"] },
    { "name": "Brienne", "surname": "Tarth", "alive": true, "age": 32, "traits": ["P","C","A","K"] },
    { "name": "Ramsay", "surname": "Bolton", "alive": true, "traits": ["E","O","G","A"] },
    { "name": "Ellaria", "surname": "Sand", "alive": true, "traits": ["P","O","A","E"] },
    { "name": "Daario", "surname": "Naharis", "alive": true, "traits": ["K","P","A"] },
    { "name": "Tommen", "surname": "Baratheon", "alive": true, "traits": ["I","L","B"] },
    { "name": "Jaqen", "surname": "H'ghar", "alive": true, "traits": ["H","F","K"] },
    { "name": "Roose", "surname": "Bolton", "alive": true, "traits": ["H","E","F","A"] }
    ]"""

    import com.stratio.sparta.plugin.models.SerializationImplicits._
    lazy val characters: Seq[Got] = read[Seq[Got]](data)

    val objects: Seq[BaseDocument] =
      characters.map {
        character =>
          val bd = new BaseDocument()
          bd.addAttribute("name", character.name)
          bd.addAttribute("surname", character.surname)
          bd.addAttribute("alive", character.alive)
          bd.addAttribute("age", character.age)
          bd.addAttribute("traits", character.traits)
          bd
      }

    objects.foreach(character =>
      try {
        arangoDB.db(dbName).collection(collectionName).insertDocument(character)
        log.info("Document created");
      } catch {
        case e: ArangoDBException =>
          System.err.println("Failed to create document. " + e.getMessage())
      }
    )
  }

  "ArangodbInput" should "read a collection" in {

    val inputProperties = Map(
      "hosts" -> arangoHosts,
      "databaseName" -> "mydb",
      "collection" -> randomCollectionName,
      "username" -> "root",
      "password" -> "openSesame",
      "tlsEnabled" -> "false"
    )

    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val arangoInput = new ArangoDBInputStepBatch(
      "dummy",
      outputOptions,
      None,
      sparkSession,
      inputProperties
    )

    val (distMonad, schemaOpt) = arangoInput.initWithSchema()
    arangoInput.initWithSchema()._1.ds.count() should be(33)
  }


  "ArangodbInput" should "read a filtered collection" in {

    val xdSession = XDSession.builder().config(sc.getConf).create("dummyUser")

    val inputProperties = Map(
      "hosts" -> arangoHosts,
      "databaseName" -> "mydb",
      "collection" -> randomCollectionName,
      "username" -> "root",
      "password" -> "openSesame",
      "tlsEnabled" -> "false",
      "addFilterQuery" -> "true",
      "filterQuery" -> "doc.`surname` == \"Stark\""
    )

    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val arangoInput = new ArangoDBInputStepBatch(
      "dummy",
      outputOptions,
      None,
      sparkSession,
      inputProperties
    )

    val (distMonad, schemaOpt) = arangoInput.initWithSchema()
    arangoInput.initWithSchema()._1.ds.count() should be(5)
  }


  "ArangodbInput" should "add id, key and revision to the schema when reads from a collection" in {

    val xdSession = XDSession.builder().config(sc.getConf).create("dummyUser")

    val inputProperties = Map(
      "hosts" -> arangoHosts,
      "databaseName" -> "mydb",
      "collection" -> randomCollectionName,
      "username" -> "root",
      "password" -> "openSesame",
      "tlsEnabled" -> "false",
      "addArangoId" -> "true",
      "addArangoKey" -> "true",
      "addArangoRevision" -> "true"
    )

    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val arangoInput = new ArangoDBInputStepBatch(
      "dummy",
      outputOptions,
      None,
      sparkSession,
      inputProperties
    )

    val (distMonad, schemaOpt) = arangoInput.initWithSchema()
    val df: DataFrame = xdSession.createDataFrame(distMonad.ds, schemaOpt.get)

    val rowIds = df.select("_id").collect()
    for (ids <- rowIds) {
      val arangoId = ids.getString(ids.fieldIndex("_id"))
      arangoId.trim should not be empty
    }

    val idkeys = df.select("_key").collect()
    for (keys <- idkeys) {
      val arangoKey = keys.getString(keys.fieldIndex("_key"))
      arangoKey.trim should not be empty
    }

    val idRevs = df.select("_rev").collect()
    for (revs <- idRevs) {
      val arangoRev = revs.getString(revs.fieldIndex("_rev"))
      arangoRev.trim should not be empty
    }
  }

  "ArangodbInput" should "read a collection given a schema" in {

    val schema: StructType = StructType(Seq(StructField("age", LongType, true), StructField("alive", BooleanType, true), StructField("name", StringType, true), StructField("surname", StringType, true), StructField("traits", ArrayType(StringType, true))))

    val inputProperties = Map(
      "hosts" -> arangoHosts,
      "databaseName" -> "mydb",
      "collection" -> randomCollectionName,
      "username" -> "root",
      "password" -> "openSesame",
      "tlsEnabled" -> "false",
      "hasSchema" -> "true",
      "addArangoId" -> "true",
      "addArangoKey" -> "true",
      "addArangoRevision" -> "true",
      "arangoSchema" -> s"${schema.json}"
    )

    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val arangoInput = new ArangoDBInputStepBatch(
      "dummy",
      outputOptions,
      None,
      sparkSession,
      inputProperties
    )

    val finalSchema = Some(StructType(Seq(StructField("age", LongType, true), StructField("alive", BooleanType, true), StructField("name", StringType, true), StructField("surname", StringType, true), StructField("traits", ArrayType(StringType, true), true), StructField("_id", StringType, true), StructField("_key", StringType, true), StructField("_rev", StringType, true))))
    val (distMonad, schemaOpt) = arangoInput.initWithSchema()

    finalSchema shouldBe schemaOpt
  }

}


