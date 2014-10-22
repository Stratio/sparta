/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.driver.service

import com.stratio.sparkta.aggregator.{DataCube, Rollup}
import com.stratio.sparkta.driver.configuration._
import com.stratio.sparkta.driver.dto.AggregationPoliciesDto
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.driver.service.ValidatingPropertyMap._
import com.stratio.sparkta.plugin.bucketer.datetime.DateTimeBucketer
import com.stratio.sparkta.plugin.bucketer.geohash.GeoHashBucketer
import com.stratio.sparkta.plugin.bucketer.passthrough.PassthroughBucketer
import com.stratio.sparkta.plugin.operator.count.CountOperator
import com.stratio.sparkta.plugin.output.mongodb.MongoDbOutput
import com.stratio.sparkta.plugin.output.print.PrintOutput
import com.stratio.sparkta.sdk._
import com.typesafe.config.ConfigFactory
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.flume.FlumeUtils
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import twitter4j.auth.AuthorizationFactory
import twitter4j.conf.ConfigurationBuilder
import twitter4j.{HashtagEntity, Status, URLEntity}

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Created by ajnavarro on 8/10/14.
 */
class StreamingContextService(generalConfiguration: GeneralConfiguration) {

  def createStreamingContext(aggregationPoliciesConfiguration: AggregationPoliciesDto): StreamingContext = {
    val ssc = new StreamingContext(
      new SparkContext(configToSparkConf(generalConfiguration, aggregationPoliciesConfiguration.name)),
      //TODO one spark context to all streaming contexts is not working
      //SparkContextFactory.sparkContextInstance(generalConfiguration),
      new Duration(aggregationPoliciesConfiguration.duration))

    aggregationPoliciesConfiguration.jarPaths.foreach(j => ssc.sparkContext.addJar(j))

    var receivers: Map[String, DStream[Event]] = Map()
    aggregationPoliciesConfiguration.receivers.foreach(element => {
      val config = element.configuration
      val receiver: DStream[InputEvent] = element.elementType match {
        case "kafka" =>
          KafkaUtils.createStream(ssc = ssc,
            zkQuorum = config.getMandatory("zkQuorum"),
            groupId = config.getMandatory("groupId"),
            topics = config.getMandatory("topics")
              .split(",")
              .map(s => (s.trim, config.getMandatory("partitions").toInt))
              .toMap,
            storageLevel = StorageLevel.fromString(config.getMandatory("storageLevel"))
            //TODO add headers
          ).map(data => new InputEvent(null, data._2.getBytes))
        case "flume" =>
          FlumeUtils.createPollingStream(
            ssc, config.getMandatory("hostname"),
            config.getMandatory("port").toInt
            //TODO add headers
          ).map(data => new InputEvent(null, data.event.getBody.array()))
        case "socket" =>
          ssc.socketTextStream(
            config.getMandatory("hostname"),
            config.getMandatory("port").toInt,
            StorageLevel.fromString(config.getMandatory("storageLevel")))
            .map(data => new InputEvent(null, data.getBytes))
        case "twitter" =>
          val config = new ConfigurationBuilder()
            .setDebugEnabled(false)
            .setOAuthConsumerKey("jqjbh5egthtW7B0k9Sb3A")
            .setOAuthConsumerSecret("ipxSCbLxKfzfVXRfUFnVqZ2JJkS4ddaEG5oKUexk")
            .setOAuthAccessToken("308647659-iYqwCEJgt0pajby3BlVinj93ljlN1tYXZFUSQzir")
            .setOAuthAccessTokenSecret("lxSuzxPLu7PJO2Bii74IRiVFE1fwUPREpaySLvz9k")
            .build()
          val auth = AuthorizationFactory.getInstance(config)

          TwitterUtils.createStream(ssc, Some(auth), Seq[String](), StorageLevel.MEMORY_ONLY)
            .map((t: Status) => {
            val firstHashTag = t.getHashtagEntities.headOption match {
              case Some(h: HashtagEntity) => h.getText
              case _ => "NONE"
            }

            val firstUrl = t.getURLEntities.headOption match {
              case Some(h: URLEntity) => h.getExpandedURL
              case _ => "NONE.COM"
            }

            val coordinates = t.getGeoLocation match {
              case g if g != null => g.getLatitude + "__" + g.getLongitude
              //TODO null treatment
              case _ => "38.897833__-77.036498"
            }

            val map: Map[String, Any] = Map(
              "userId" -> t.getUser.getId,
              "createdAt" -> t.getCreatedAt,
              "lang" -> t.getUser.getLang,
              "hashTagsCount" -> t.getHashtagEntities.size,
              "urlsCount" -> t.getURLEntities.size,
              "userMentionCount" -> t.getUserMentionEntities.size,
              "firstHashtag" -> firstHashTag,
              "latLong" -> coordinates,
              "firstUrl" -> firstUrl
            )
            new InputEvent(map, null)
          })
        case _ =>
          throw new DriverException("Receiver " + element.elementType + " not supported in receiver " + element.name)
      }

      //TODO
      val parser = config.getMandatory("parser") match {
        //        case "keyValueParser" => new KeyValueParser
        //        case "twitterParser" => new TwitterParser
        case _ => throw new DriverException("Parser not supported")
      }
      //TODO
      //receivers += (element.name -> parser.map(receiver))
    })

    var outputs: Map[String, Output] = Map()
    aggregationPoliciesConfiguration.outputs.foreach(element => {
      //TODO val config = element.configuration
      val output = element.elementType match {
        case "print" => new PrintOutput()

        case "mongo" =>
          val mapConfig = Map("client_uri" -> "mongodb://localhost", "dbName" -> "SPARKTA")
          new MongoDbOutput(ConfigFactory.parseMap(mapConfig.asJava))
        case _ =>
          throw new DriverException("Output " + element.elementType + " not supported")
      }
      outputs += (element.name -> output)
    })

    val dimensions: Map[String, Dimension] = aggregationPoliciesConfiguration.dimensions.map(element => {
      val dimension: Dimension = element.dimensionType match {
        case "string" => new Dimension(element.name, new PassthroughBucketer())
        case "date" => new Dimension(element.name, new DateTimeBucketer())
        case "geo" => new Dimension(element.name, new GeoHashBucketer())
        case x => throw new DriverException("Dimension type " + x + " not supported.")
      }
      (element.name -> dimension)
    }).toMap

    //TODO workaround to obtain seq
    val dimensionsSeq: Seq[Dimension] = aggregationPoliciesConfiguration.dimensions.map(element => {
      val dimension: Dimension = element.dimensionType match {
        case "string" => new Dimension(element.name, new PassthroughBucketer())
        case "date" => new Dimension(element.name, new DateTimeBucketer())
        case "geo" => new Dimension(element.name, new GeoHashBucketer())
        case x => throw new DriverException("Dimension type " + x + " not supported.")
      }
      dimension
    })

    val rollups = aggregationPoliciesConfiguration.rollups.map(element => {
      val dimAndTypes: Seq[(Dimension, BucketType)] = element.dimensionAndBucketTypes.map(dabt => {
        dimensions.get(dabt.dimensionName) match {
          case Some(x: Dimension) => x.bucketTypes.contains(new BucketType(dabt.bucketType)) match {
            case true => (x, new BucketType(dabt.bucketType))
            case _ =>
              throw new DriverException(
                "Bucket type " + dabt.bucketType + " not supported in dimension " + dabt.dimensionName)
          }
          case None => throw new DriverException("Dimension name " + dabt.dimensionName + " not found.")
        }
      }).seq

      new Rollup(dimAndTypes, Seq[Operator](new CountOperator))
    })
    val datacube = new DataCube(dimensionsSeq, rollups)

    //TODO implement multiple outputs and inputs
    outputs.head._2.persist(datacube.setUp(receivers.head._2))

    ssc
  }

  private def configToSparkConf(generalConfiguration: GeneralConfiguration, name: String): SparkConf = {
    val conf = new SparkConf()
    conf.setMaster(generalConfiguration.master)
      .setAppName(name)

    conf.set("spark.cores.max", generalConfiguration.cpus.toString)

    // Should be a -Xmx style string eg "512m", "1G"
    conf.set("spark.executor.memory", generalConfiguration.memory)

    Try(generalConfiguration.sparkHome).foreach { home => conf.setSparkHome(generalConfiguration.sparkHome)}

    // Set the Jetty port to 0 to find a random port
    conf.set("spark.ui.port", "0")

    conf.set("spark.streaming.concurrentJobs", "20")

    conf
  }
}
