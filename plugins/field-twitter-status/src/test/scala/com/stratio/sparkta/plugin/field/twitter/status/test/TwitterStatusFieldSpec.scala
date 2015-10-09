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

package com.stratio.sparkta.plugin.field.twitter.status.test

import com.stratio.sparkta.plugin.field.twitter.status.TwitterStatusField
import org.junit.runner.RunWith
import org.scalamock.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}
import twitter4j.{User, Status}

@RunWith(classOf[JUnitRunner])
class TwitterStatusFieldSpec extends FlatSpec with MockFactory with Matchers {


  val toTest = new TwitterStatusField()
  "A status " should "return text" in {
    val result = getPrecision(TwitterStatusField.TextName)
    result._2 should be("Some text")
  }
  it should "return contributors" in {
    val result =getPrecision(TwitterStatusField.ContributorsName)
    result._2 should be ("")
  }
  it should "return hashtags" in {
    val result =getPrecision(TwitterStatusField.HashtagsName)
    result._2 should be ("0")
  }
  it should "return first hashtags" in {
    val result =getPrecision(TwitterStatusField.FirstHastagName)
    result._2 should be ("")
  }
  it should "return place" in {
    val result =getPrecision(TwitterStatusField.PlacesName)
    result._2 should be ("")
  }
  it should "return RetweetsName" in {
    val result =getPrecision(TwitterStatusField.RetweetsName)
    result._2 should be ("1")
  }
  it should "return UrlsName" in {
    val result =getPrecision(TwitterStatusField.UrlsName)
    result._2 should be ("0")
  }
  it should "return MentionsName" in {
    val result =getPrecision(TwitterStatusField.MentionsName)
    result._2 should be ("")
  }
  it should "return IdentityName" in {
    val result =getPrecision(TwitterStatusField.IdentityName)
    result._2.isInstanceOf[String] should be (true)
  }
  it should "return WordsName" in {
    val result =getPrecision(TwitterStatusField.WordsName)
    result._2 should be ("2")
  }
  it should "return LocationName" in {
    val result =getPrecision(TwitterStatusField.LocationName)
    result._2 should be ("madrid")
  }
  it should "return NameName" in {
    val result =getPrecision(TwitterStatusField.NameName)
    result._2 should be ("madrid")
  }
  it should "return LanguageName" in {
    val result =getPrecision(TwitterStatusField.LanguageName)
    result._2 should be ("madrid")
  }
  it should "return to String" in {
    val result=toTest.toString
    result should be ("TwitterStatusField(Map())")
  }
  private def getPrecision(key:String) ={
    toTest.precisionValue(key,getMockStatus)
  }
  //scalastyle:off
  private def getMockStatus: Status = {
    val status = mock[Status]
    (status.getText _).expects().returning("Some text").anyNumberOfTimes()
    (status.getContributors _).expects().returning(null).anyNumberOfTimes()
    (status.getHashtagEntities _).expects().returning(null).anyNumberOfTimes()
    (status.getUser _).expects().returning(mock[User]).anyNumberOfTimes()
    (status.getRetweetCount _).expects().returning(1L).anyNumberOfTimes()
    (status.getPlace _).expects().returning(null).anyNumberOfTimes()
    (status.getURLEntities _).expects().returning(null).anyNumberOfTimes()
    (status.getUserMentionEntities _).expects().returning(null).anyNumberOfTimes()
    (status.getUser.getLocation _).expects().returning("madrid").anyNumberOfTimes()
    (status.getUser.getLang _).expects().returning("madrid").anyNumberOfTimes()
    (status.getUser.getName _).expects().returning("madrid").anyNumberOfTimes()
    status
  }
  //scalastyle:on
}
