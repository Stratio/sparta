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
package com.stratio.sparkta.plugin.bucketer.twitter.status.test

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.plugin.bucketer.twitter.status.TwitterStatusBucketer
import org.scalatest.{FlatSpec,  Matchers}
import twitter4j.{HashtagEntity, Status}
import org.scalamock.scalatest._

/**
 * Created by arincon on 16/03/15.
 */
@RunWith(classOf[JUnitRunner])
class TwitterStatusBucketerSpec extends FlatSpec with MockFactory with Matchers {



//  "A status " should "have 9 dimensions" in {
//
//    val status: Status = getMockStatus
//
//
//    val toTest = new TwitterStatusBucketer
//    val expected =toTest.bucket(status)
//
//    expected.size should be(9)
//   // expected.get(TwitterStatusBucketer.text) should be equals ("Some text")
//  }


  def getMockStatus: Status = {
    val status = mock[Status]
    (status.getText _).expects().returning("Some text").anyNumberOfTimes()
    (status.getContributors _).expects().returning(null).anyNumberOfTimes()
    (status.getHashtagEntities _).expects().returning(null).anyNumberOfTimes()
    (status.getRetweetCount _).expects().returning(1L).anyNumberOfTimes()
    (status.getPlace _).expects().returning(null).anyNumberOfTimes()
    (status.getURLEntities _).expects().returning(null).anyNumberOfTimes()
    (status.getUserMentionEntities _).expects().returning(null).anyNumberOfTimes()
    status
  }
}
