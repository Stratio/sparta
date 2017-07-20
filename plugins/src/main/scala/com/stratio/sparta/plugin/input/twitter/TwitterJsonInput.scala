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
package com.stratio.sparta.plugin.input.twitter

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.google.gson.Gson
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.twitter.TwitterUtils
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

import scala.util.{Failure, Success, Try}

/**
 * Connects to Twitter's stream and generates stream events.
 */
class TwitterJsonInput(
                        name: String,
                        ssc: StreamingContext,
                        sparkSession: XDSession,
                        properties: Map[String, JSerializable]
                      ) extends Input(name, ssc, sparkSession, properties) with SLF4JLogging {

  System.setProperty("twitter4j.oauth.consumerKey", properties.getString("consumerKey"))
  System.setProperty("twitter4j.oauth.consumerSecret", properties.getString("consumerSecret"))
  System.setProperty("twitter4j.oauth.accessToken", properties.getString("accessToken"))
  System.setProperty("twitter4j.oauth.accessTokenSecret", properties.getString("accessTokenSecret"))

  val cb = new ConfigurationBuilder()
  val tf = new TwitterFactory(cb.build())
  val twitterApi = tf.getInstance()
  val trends = twitterApi.getPlaceTrends(1).getTrends.map(trend => trend.getName)
  val terms: Option[Seq[String]] = Try(properties.getString("termsOfSearch")) match {
    case Success("") => None
    case Success(t: String) => Some(t.split(",").toSeq)
    case Failure(_) => None
  }
  val search = terms.getOrElse(trends.toSeq)

  def initStream: DStream[Row] = {
    TwitterUtils.createStream(ssc, None, search, storageLevel)
      .map(stream => {
        val gson = new Gson()
        Row(gson.toJson(stream))
      }
      )
  }
}
