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

package com.stratio.sparkta.plugin.dimension.hierarchy.test

import java.io.{Serializable => JSerializable}

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import com.stratio.sparkta.plugin.dimension.hierarchy.HierarchyDimension

@RunWith(classOf[JUnitRunner])
class HierarchyDimensionSpec extends WordSpecLike
with Matchers
with BeforeAndAfter
with BeforeAndAfterAll
with TableDrivenPropertyChecks {

  var hbs: HierarchyDimension = null

  before {
    hbs = new HierarchyDimension()
  }

  after {
    hbs = null
  }

  "A HierarchyDimension" should {
    "In default implementation, get 4 buckets for all precision sizes" in {
      val buckets = hbs.bucket("").map(_._1.id)

      buckets.size should be(4)

      buckets should contain(HierarchyDimension.LeftToRightName)
      buckets should contain(HierarchyDimension.RightToLeftName)
      buckets should contain(HierarchyDimension.LeftToRightWithWildCardName)
      buckets should contain(HierarchyDimension.RightToLeftWithWildCardName)
    }

    "In default implementation, every proposed combination should be ok" in {
      val data = Table(
        ("i", "o"),
        ("google.com", Seq("google.com", "*.com", "*"))
      )

      forAll(data) { (i: String, o: Seq[String]) =>
        val result = hbs.bucket(i)
        val value = result(hbs.LeftToRightWithWildCard)
        assertResult(o)(value)
      }
    }
    "In reverse implementation, every proposed combination should be ok" in {
      hbs = new HierarchyDimension()
      val data = Table(
        ("i", "o"),
        ("com.stratio.sparkta", Seq("com.stratio.sparkta", "com.stratio.*", "com.*", "*"))
      )

      forAll(data) { (i: String, o: Seq[String]) =>
        val result = hbs.bucket(i.asInstanceOf[JSerializable])
        val value = result(hbs.RightToLeftWithWildCard)
        assertResult(o)(value)
      }
    }
    "In reverse implementation without wildcards, every proposed combination should be ok" in {
      hbs = new HierarchyDimension()
      val data = Table(
        ("i", "o"),
        ("com.stratio.sparkta", Seq("com.stratio.sparkta", "com.stratio", "com", "*"))
      )

      forAll(data) { (i: String, o: Seq[String]) =>
        val result = hbs.bucket(i.asInstanceOf[JSerializable])
        val value = result(hbs.RightToLeft)
        assertResult(o)(value)
      }
    }
    "In non-reverse implementation without wildcards, every proposed combination should be ok" in {
      hbs = new HierarchyDimension()
      val data = Table(
        ("i", "o"),
        ("google.com", Seq("google.com", "com", "*"))
      )

      forAll(data) { (i: String, o: Seq[String]) =>
        val result = hbs.bucket(i.asInstanceOf[JSerializable])
        val value = result(hbs.LeftToRight)
        assertResult(o)(value)
      }
    }
  }
}
