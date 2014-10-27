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
package com.stratio.sparkta.plugin.bucketer.hierarchy.test

import java.io.Serializable

import com.stratio.sparkta.plugin.bucketer.hierarchy.HierarchyBucketer
import com.stratio.sparkta.plugin.bucketer.hierarchy.HierarchyBucketer._
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * Created by ajnavarro on 27/10/14.
 */
class HierarchyBucketerSpec extends WordSpecLike
with Matchers
with BeforeAndAfter
with BeforeAndAfterAll
with TableDrivenPropertyChecks {

  var hbs: HierarchyBucketer = null

  before {
    hbs = new HierarchyBucketer()
  }

  after {
    hbs = null
  }

  "A HierarchyBucketer" should {
    "In default implementation, every proposed combination should be ok" in {
      val data = Table(
        ("i", "o"),
        ("google.com", Seq("*.com", "google.com", "*"))
      )

      forAll(data) { (i: String, o: Seq[String]) =>
        val result = hbs.bucketForWrite(i.asInstanceOf[Serializable])
        o.map(r => result.get(leftToRightWithWildCard).get should contain(r))
        result.get(leftToRightWithWildCard).get.size should be(o.size)
      }
    }
    "In reverse implementation, every proposed combination should be ok" in {
      hbs = new HierarchyBucketer(Seq(rightToLeftWithWildCard))
      val data = Table(
        ("i", "o"),
        ("com.stratio.sparkta", Seq("*", "com.*", "com.stratio.*", "com.stratio.sparkta"))
      )

      forAll(data) { (i: String, o: Seq[String]) =>
        val result = hbs.bucketForWrite(i.asInstanceOf[Serializable])
        o.map(r => result.get(rightToLeftWithWildCard).get should contain(r))
        result.get(rightToLeftWithWildCard).get.size should be(o.size)
      }
    }
    "In reverse implementation without wildcards, every proposed combination should be ok" in {
      hbs = new HierarchyBucketer(Seq(rightToLeft))
      val data = Table(
        ("i", "o"),
        ("com.stratio.sparkta", Seq("*", "com", "com.stratio", "com.stratio.sparkta"))
      )

      forAll(data) { (i: String, o: Seq[String]) =>
        val result = hbs.bucketForWrite(i.asInstanceOf[Serializable])
        o.map(r => result.get(rightToLeft).get should contain(r))
        result.get(rightToLeft).get.size should be(o.size)
      }
    }
    "In non-reverse implementation without wildcards, every proposed combination should be ok" in {
      hbs = new HierarchyBucketer(Seq(leftToRight))
      val data = Table(
        ("i", "o"),
        ("google.com", Seq("*", "com", "google.com"))
      )

      forAll(data) { (i: String, o: Seq[String]) =>
        val result = hbs.bucketForWrite(i.asInstanceOf[Serializable])
        o.map(r => result.get(leftToRight).get should contain(r))
        result.get(leftToRight).get.size should be(o.size)
      }
    }
  }
}
