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

package com.stratio.sparkta.plugin.dimension.tag.test

import java.io.{Serializable => JSerializable}

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.TableDrivenPropertyChecks

import com.stratio.sparkta.plugin.dimension.tag.TagDimension

@RunWith(classOf[JUnitRunner])
class TagDimensionSpec extends WordSpecLike with Matchers with TableDrivenPropertyChecks {

  val tagDimension: TagDimension = new TagDimension()
  val tags1 = Seq("a", "b", "c")
  val tags2 = Seq("a", "b", "c", "a")
  val tags3 = Seq("a", System.currentTimeMillis(), System.currentTimeMillis(), "a")
  val tags4 = Seq("a", "a", "a", "a")

  "A TagDimension" should {
    "In default implementation, get 3 precisions for all precision sizes" in {
      val precisions = tagDimension.dimensionValues(Seq("").asInstanceOf[JSerializable]).map(_._1.id)

      precisions.size should be(3)
      precisions should contain(TagDimension.AllTagsName)
      precisions should contain(TagDimension.FirstTagName)
      precisions should contain(TagDimension.LastTagName)
    }

    "In default implementation, every proposed combination should be ok" in {
      val data = Table(
        ("s", "rz"),
        (tags1, tags1.size),
        (tags2, tags2.size),
        (tags3, tags3.size),
        (tags4, tags4.size)
      )

      forAll(data) { (s: Seq[Any], rz: Int) =>
        val result =
          tagDimension.dimensionValues(s.map(_.asInstanceOf[JSerializable]).toList.asInstanceOf[JSerializable])
        val allTags = result(tagDimension.precisions(TagDimension.AllTagsName)).asInstanceOf[Seq[String]]
        allTags.size should be(rz)
      }
    }
  }
}
