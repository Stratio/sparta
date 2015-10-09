/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.plugin.input.twitter

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.Input._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.{Event, Input}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.twitter.TwitterUtils
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

import scala.util.Try

/**
 * Connects to Twitter's stream and generates stream events.
 */
@deprecated
class TwitterInput(properties: Map[String, JSerializable]) extends Input(properties) {

  System.setProperty("twitter4j.oauth.consumerKey", properties.getString("consumerKey"))
  System.setProperty("twitter4j.oauth.consumerSecret", properties.getString("consumerSecret"))
  System.setProperty("twitter4j.oauth.accessToken", properties.getString("accessToken"))
  System.setProperty("twitter4j.oauth.accessTokenSecret", properties.getString("accessTokenSecret"))

  val cb = new ConfigurationBuilder().setUseSSL(true)
  val tf = new TwitterFactory(cb.build())
  val twitterApi = tf.getInstance()
  val trends = twitterApi.getPlaceTrends(1).getTrends.map(trend => trend.getName)
  val terms: Try[Seq[String]] = Try(properties.getString("termsOfSearch").split(","))
  val search = terms.getOrElse(trends.toSeq)

  override def setUp(ssc: StreamingContext, sparkStorageLevel: String): DStream[Event] = {
    TwitterUtils.createStream(ssc, None, search, storageLevel(sparkStorageLevel)).map(data => new Event(Map(
      RawDataKey -> data,
      "status" -> data.asInstanceOf[java.io.Serializable],
      "wordsN" -> data.getText.split(" ").size,
      "retweets" -> data.getRetweetCount,
      "userLocation" -> data.getUser.getLocation.toLowerCase,
      "timestamp" -> data.getCreatedAt,
      "text" -> data.getText,
      "geolocation" -> (Option(data.getGeoLocation) match {
        case Some(geo) => Some(geo.getLatitude + "__" + geo.getLongitude)
        case _ => None
      }).asInstanceOf[JSerializable]
    ), Some(data)))
  }
}
