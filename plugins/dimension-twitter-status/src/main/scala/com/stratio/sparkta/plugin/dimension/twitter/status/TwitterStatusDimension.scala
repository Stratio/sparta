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

package com.stratio.sparkta.plugin.dimension.twitter.status

import java.io

import TwitterStatusDimension._
import com.stratio.sparkta.sdk.{Bucketer, BucketType}
import twitter4j.{Status}

case class TwitterStatusDimension() extends Bucketer {

  override val bucketTypes: Seq[BucketType] =
    Seq(text, contributors, hastags, places, retweets, urls, mentions, words, identity, location, firstHastag)

  override def bucket(value: io.Serializable): Map[BucketType, io.Serializable] = {
    bucketTypes.map(bucketType =>
      bucketType -> TwitterStatusDimension.bucket(value.asInstanceOf[Status], bucketType)
    ).toMap
  }

}

object TwitterStatusDimension {

  //scalastyle:off
  private def bucket(value: Status, bucketType: BucketType): io.Serializable = {
    val getText: io.Serializable = value.getText
    val getContributors: io.Serializable = if (value.getContributors != null) value.getContributors.toString else ""
    val getHastags: io.Serializable = if (value.getHashtagEntities != null)
      value.getHashtagEntities.map(_.getText).length
    else 0
    val getFirstHastag: io.Serializable = if ((value.getHashtagEntities != null) && value.getHashtagEntities.length > 0)
      value.getHashtagEntities.head.getText
    else ""
    val getPlaces: io.Serializable = if (value.getPlace != null) value.getPlace.getFullName else ""
    val getRetweets: io.Serializable = value.getRetweetCount
    val getUrls: io.Serializable = if (value.getURLEntities != null) value.getURLEntities.map(_.getURL).length else 0
    val getMentions: io.Serializable = if (value.getUserMentionEntities != null)
      value.getUserMentionEntities.map(_.getName)
    else ""
    val getWordsCount = value.getText.split(" ").length
    val getLocation : io.Serializable = value.getUser.getLocation.toLowerCase
    val getLanguage = value.getUser.getLang
    val getName = value.getUser.getName

    (bucketType match {
      case a if a == text => getText
      case c if c == contributors => getContributors
      case h if h == hastags => getHastags
      case h if h == firstHastag => getFirstHastag
      case p if p == places => getPlaces
      case r if r == retweets => getRetweets
      case u if u == urls => getUrls
      case m if m == mentions => getMentions
      case i if i == identity => value
      case w if w == words => getWordsCount
      case l if l == location => getLocation
      case n if n == name => getName
      case l if l == language => getLanguage

    }).toString.asInstanceOf[io.Serializable]
  }
  //scalastyle:on

  val text = new BucketType("text")
  val contributors = new BucketType("contributors")
  val hastags = new BucketType("hastags")
  val firstHastag = new BucketType("firsthastag")
  val places = new BucketType("places")
  val retweets = new BucketType("retweets")
  val urls = new BucketType("urls")
  val mentions = new BucketType("mentions")
  val identity = new BucketType("identity")
  val words = new BucketType("words")
  val location = new BucketType("location")
  val name = new BucketType("name")
  val language = new BucketType("language")


  override def toString : String = s"TwitterStatusBucketer(" +
    s"text=$text, contributors=$contributors, hastags=$hastags, firsthastag=$firstHastag" +
    s" places=$places, retweets=$retweets, urls=$urls, mentions=$mentions," +
    s" words=$words, location=$location, name=$name, language=$language)"
}




