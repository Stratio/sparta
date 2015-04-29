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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.plugin.bucketer.hierarchy.HierarchyBucketer
import com.stratio.sparkta.plugin.bucketer.hierarchy.HierarchyBucketer._
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * Created by ajnavarro on 27/10/14.
 */
@RunWith(classOf[JUnitRunner])
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
        ("google.com", Seq("google.com", "*.com", "*"))
      )

      forAll(data) { (i: String, o: Seq[String]) =>
        val result = hbs.bucket(i)
        val value = result(leftToRightWithWildCard)
        assertResult(o)(value)
      }
    }
    "In reverse implementation, every proposed combination should be ok" in {
      hbs = new HierarchyBucketer()
      val data = Table(
        ("i", "o"),
        ("com.stratio.sparkta", Seq("com.stratio.sparkta", "com.stratio.*", "com.*", "*"))
      )

      forAll(data) { (i: String, o: Seq[String]) =>
        val result = hbs.bucket(i.asInstanceOf[Serializable])
        val value = result(rightToLeftWithWildCard)
        assertResult(o)(value)
      }
    }
    "In reverse implementation without wildcards, every proposed combination should be ok" in {
      hbs = new HierarchyBucketer()
      val data = Table(
        ("i", "o"),
        ("com.stratio.sparkta", Seq("com.stratio.sparkta", "com.stratio", "com", "*"))
      )

      forAll(data) { (i: String, o: Seq[String]) =>
        val result = hbs.bucket(i.asInstanceOf[Serializable])
        val value = result(rightToLeft)
        assertResult(o)(value)
      }
    }
    "In non-reverse implementation without wildcards, every proposed combination should be ok" in {
      hbs = new HierarchyBucketer()
      val data = Table(
        ("i", "o"),
        ("google.com", Seq("google.com", "com", "*"))
      )

      forAll(data) { (i: String, o: Seq[String]) =>
        val result = hbs.bucket(i.asInstanceOf[Serializable])
        val value = result(leftToRight)
        assertResult(o)(value)
      }
    }
  }
}
