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
package com.stratio.sparkta.plugin.dimension.tag.test

import java.io.Serializable
import com.stratio.sparkta.plugin.dimension.tag.TagDimension
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.TableDrivenPropertyChecks

/**
 * Created by ajnavarro on 27/10/14.
 */
@RunWith(classOf[JUnitRunner])
class TagDimensionSpec extends WordSpecLike
with Matchers
with BeforeAndAfter
with BeforeAndAfterAll
with TableDrivenPropertyChecks {

  val tags1 = Seq("a", "b", "c")
  val tags2 = Seq("a", "b", "c", "a")
  val tags3 = Seq("a", System.currentTimeMillis(), System.currentTimeMillis(), "a")
  val tags4 = Seq("a", "a", "a", "a")

  "A TagDimension" should {
    "In default implementation, every proposed combination should be ok" in {
      val tagBucketer = new TagDimension()
      val data = Table(
        ("s", "rz"),
        (tags1, tags1.size),
        (tags2, tags2.size),
        (tags3, tags3.size),
        (tags4, tags4.size)
      )

      forAll(data) { (s: Seq[Any], rz: Int) =>
        val result = tagBucketer.bucket(s.map(_.asInstanceOf[Serializable]).toList.asInstanceOf[Serializable])
        val allTags = result(TagDimension.allTags).asInstanceOf[Seq[String]]
        allTags.size should be(rz)
      }
    }
  }
}
