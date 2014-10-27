package com.stratio.sparkta.plugin.bucketer.tag.test

import java.io.Serializable

import com.stratio.sparkta.plugin.bucketer.tag.TagBucketer
import com.stratio.sparkta.plugin.bucketer.tag.TagBucketer._
import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks

/**
 * Created by ajnavarro on 27/10/14.
 */
class TagBucketerSpec extends WordSpecLike
with Matchers
with BeforeAndAfter
with BeforeAndAfterAll
with TableDrivenPropertyChecks {

  var tagBucketer: TagBucketer = null
  before {
    tagBucketer = new TagBucketer()
  }

  after {
    tagBucketer = null
  }

  "A TagBucketer" should {
    "In default implementation, every proposed combination should be ok" in {
      val data = Table(
        ("i", "rz"),
        (Seq("a", "b", "c"), 3),
        (Seq("a", "b", "c", "a"), 4),
        (Seq("a", 45, 3, "a"), 4),
        (Seq("a", "a", "a", "a"), 4)
      )

      forAll(data) { (s: Seq[Any], rz: Int) =>
        val result = tagBucketer.bucketForWrite(s.toIterable.asInstanceOf[Serializable])
        result.get(allTags).get.size should be(rz)
      }
    }
  }
}
