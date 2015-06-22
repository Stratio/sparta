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

package com.stratio.sparkta.plugin.field.hierarchy.test

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.plugin.field.hierarchy.HierarchyField
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class HierarchyFieldSpec extends WordSpecLike
with Matchers
with BeforeAndAfter
with BeforeAndAfterAll
with TableDrivenPropertyChecks {

  var hbs: Option[HierarchyField] = _

  before {
    hbs = Some(new HierarchyField())
  }

  after {
    hbs = None
  }

  "A HierarchyDimension" should {
    "In default implementation, get 4 precisions for all precision sizes" in {
      val precisions = hbs.get.dimensionValues("").map(_._1.id)

      precisions.size should be(4)

      precisions should contain(HierarchyField.LeftToRightName)
      precisions should contain(HierarchyField.RightToLeftName)
      precisions should contain(HierarchyField.LeftToRightWithWildCardName)
      precisions should contain(HierarchyField.RightToLeftWithWildCardName)
    }

    "In default implementation, every proposed combination should be ok" in {
      val data = Table(
        ("i", "o"),
        ("google.com", Seq("google.com", "*.com", "*"))
      )

      forAll(data) { (i: String, o: Seq[String]) =>
        val result = hbs.get.dimensionValues(i)
        val value = result(hbs.get.LeftToRightWithWildCard)
        assertResult(o)(value)
      }
    }
    "In reverse implementation, every proposed combination should be ok" in {
      hbs = Some(new HierarchyField())
      val data = Table(
        ("i", "o"),
        ("com.stratio.sparkta", Seq("com.stratio.sparkta", "com.stratio.*", "com.*", "*"))
      )

      forAll(data) { (i: String, o: Seq[String]) =>
        val result = hbs.get.dimensionValues(i.asInstanceOf[JSerializable])
        val value = result(hbs.get.RightToLeftWithWildCard)
        assertResult(o)(value)
      }
    }
    "In reverse implementation without wildcards, every proposed combination should be ok" in {
      hbs = Some(new HierarchyField())
      val data = Table(
        ("i", "o"),
        ("com.stratio.sparkta", Seq("com.stratio.sparkta", "com.stratio", "com", "*"))
      )

      forAll(data) { (i: String, o: Seq[String]) =>
        val result = hbs.get.dimensionValues(i.asInstanceOf[JSerializable])
        val value = result(hbs.get.RightToLeft)
        assertResult(o)(value)
      }
    }
    "In non-reverse implementation without wildcards, every proposed combination should be ok" in {
      hbs = Some(new HierarchyField())
      val data = Table(
        ("i", "o"),
        ("google.com", Seq("google.com", "com", "*"))
      )

      forAll(data) { (i: String, o: Seq[String]) =>
        val result = hbs.get.dimensionValues(i.asInstanceOf[JSerializable])
        val value = result(hbs.get.LeftToRight)
        assertResult(o)(value)
      }
    }
  }
}
