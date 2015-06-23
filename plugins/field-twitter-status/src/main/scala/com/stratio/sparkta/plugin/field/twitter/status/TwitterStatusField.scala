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

package com.stratio.sparkta.plugin.field.twitter.status

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import twitter4j.Status

import TwitterStatusField._
import com.stratio.sparkta.sdk._

case class TwitterStatusField(props: Map[String, JSerializable]) extends DimensionType
with JSerializable with SLF4JLogging {

  def this() {
    this(Map())
  }

  override val defaultTypeOperation = TypeOp.String

  override val operationProps : Map[String, JSerializable] = props

  override val properties: Map[String, JSerializable] = props

  //scalastyle:off
  override def precision(keyName: String): Precision = keyName match {
    case TextName => getPrecision(TextName, getTypeOperation(TextName))
    case ContributorsName => getPrecision(ContributorsName, getTypeOperation(ContributorsName))
    case HastagsName => getPrecision(HastagsName, getTypeOperation(HastagsName))
    case FirstHastagName => getPrecision(FirstHastagName, getTypeOperation(FirstHastagName))
    case PlacesName => getPrecision(PlacesName, getTypeOperation(PlacesName))
    case RetweetsName => getPrecision(RetweetsName, getTypeOperation(RetweetsName))
    case UrlsName => getPrecision(UrlsName, getTypeOperation(UrlsName))
    case MentionsName => getPrecision(MentionsName, getTypeOperation(MentionsName))
    case IdentityName => getPrecision(IdentityName, getTypeOperation(IdentityName))
    case WordsName => getPrecision(WordsName, getTypeOperation(WordsName))
    case IdentityName => getPrecision(IdentityName, getTypeOperation(IdentityName))
    case LocationName => getPrecision(LocationName, getTypeOperation(LocationName))
    case NameName => getPrecision(NameName, getTypeOperation(NameName))
    case LanguageName => getPrecision(LanguageName, getTypeOperation(LanguageName))
  }
  //scalastyle:on

  override def precisionValue(keyName: String, value: JSerializable): (Precision, JSerializable) = {
    val precisionKey = precision(keyName)
    (precisionKey, TypeOp.transformValueByTypeOp(precisionKey.typeOp,
        TwitterStatusField.getPrecision(value.asInstanceOf[Status], precisionKey)))
  }
}

object TwitterStatusField {

  final val TextName = "text"
  final val ContributorsName = "contributors"
  final val HastagsName = "hastags"
  final val FirstHastagName = "firsthastag"
  final val PlacesName = "places"
  final val RetweetsName = "retweets"
  final val UrlsName = "urls"
  final val MentionsName = "mentions"
  final val IdentityName = "identity"
  final val WordsName = "words"
  final val LocationName = "location"
  final val NameName = "name"
  final val LanguageName = "language"

  //scalastyle:off
  def getPrecision(value: Status, precision: Precision): JSerializable = {
    val getText: JSerializable = value.getText
    val getContributors: JSerializable = if (value.getContributors != null) value.getContributors.toString else ""
    val getHastags: JSerializable = if (value.getHashtagEntities != null)
      value.getHashtagEntities.map(_.getText).length
    else 0
    val getFirstHastag: JSerializable = if ((value.getHashtagEntities != null) && value.getHashtagEntities.length > 0)
      value.getHashtagEntities.head.getText
    else ""
    val getPlaces: JSerializable = if (value.getPlace != null) value.getPlace.getFullName else ""
    val getRetweets: JSerializable = value.getRetweetCount
    val getUrls: JSerializable = if (value.getURLEntities != null) value.getURLEntities.map(_.getURL).length else 0
    val getMentions: JSerializable = if (value.getUserMentionEntities != null)
      value.getUserMentionEntities.map(_.getName)
    else ""
    val getWordsCount = value.getText.split(" ").length
    val getLocation: JSerializable = value.getUser.getLocation.toLowerCase
    val getLanguage = value.getUser.getLang
    val getName = value.getUser.getName

    (precision.id match {
      case a if a == TextName => getText
      case c if c == ContributorsName => getContributors
      case h if h == HastagsName => getHastags
      case h if h == FirstHastagName => getFirstHastag
      case p if p == PlacesName => getPlaces
      case r if r == RetweetsName => getRetweets
      case u if u == UrlsName => getUrls
      case m if m == MentionsName => getMentions
      case i if i == IdentityName => value
      case w if w == WordsName => getWordsCount
      case l if l == LocationName => getLocation
      case n if n == NameName => getName
      case l if l == LanguageName => getLanguage
    }).toString.asInstanceOf[JSerializable]
  }

  //scalastyle:on

  override def toString: String = s"TwitterStatusBucketer(" +
    s"text=$TextName, contributors=$ContributorsName, hastags=$HastagsName, firsthastag=$FirstHastagName" +
    s" places=$PlacesName, retweets=$RetweetsName, urls=$UrlsName, mentions=$MentionsName," +
    s" words=$WordsName, location=$LocationName, name=$NameName, language=$LanguageName)"
}




