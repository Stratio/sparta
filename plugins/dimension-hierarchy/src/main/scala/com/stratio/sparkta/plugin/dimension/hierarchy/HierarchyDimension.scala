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

package com.stratio.sparkta.plugin.dimension.hierarchy

import java.io
import java.io.Serializable

import HierarchyDimension._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk._


class HierarchyDimension(override val properties:
                        Map[String, Serializable] =
                        Map((SPLITTER_PROPERTY_NAME, DEFAULT_SPLITTER), (WILDCARD_PROPERTY_NAME, DEFAULT_WILDCARD)))
  extends Bucketer {

  override val bucketTypes: Seq[BucketType] =
    Seq(leftToRight, rightToLeft, leftToRightWithWildCard, rightToLeftWithWildCard)

  val splitter = properties.getString(SPLITTER_PROPERTY_NAME)
  val wildcard = properties.getString(WILDCARD_PROPERTY_NAME)

  override def bucket(value: io.Serializable): Map[BucketType, io.Serializable] =
    bucketTypes.map(bt => (bt, bucket(value.asInstanceOf[String], bt).asInstanceOf[Serializable])).toMap

  private def bucket(value: String, bucketType: BucketType): Seq[Serializable] = {
    bucketType match {
      case x if x == leftToRight =>
        explodeWithWildcards(value, wildcard, splitter, false, false)
      case x if x == rightToLeft =>
        explodeWithWildcards(value, wildcard, splitter, true, false)
      case x if x == leftToRightWithWildCard =>
        explodeWithWildcards(value, wildcard, splitter, false, true)
      case x if x == rightToLeftWithWildCard =>
        explodeWithWildcards(value, wildcard, splitter, true, true)
    }
  }
}

object HierarchyDimension {
  val DEFAULT_SPLITTER = "."
  val SPLITTER_PROPERTY_NAME = "splitter"

  val DEFAULT_WILDCARD = "*"
  val WILDCARD_PROPERTY_NAME = "wildcard"

  def explodeWithWildcards(
                            domain: String,
                            wildcard: String,
                            splitter: String,
                            reversed: Boolean,
                            withWildcards: Boolean
                            ): Seq[Serializable] = {
    val split = domain.split("\\Q" + splitter + "\\E").toSeq
    val domainTails = if (reversed) split.reverse.tails.toSeq else split.tails.toSeq
    val fullDomain = domainTails.head
    domainTails.map({
      case Nil => wildcard
      case l: Seq[String] if l == fullDomain => domain
      case l: Seq[String] => if (reversed) {
        if (withWildcards) l.reverse.mkString(splitter) + splitter + wildcard else l.reverse.mkString(splitter)
      } else if (withWildcards) wildcard + splitter + l.mkString(splitter) else l.mkString(splitter)
    })
  }

  val leftToRight = new BucketType("leftToRight", Some(TypeOp.ArrayString))
  val rightToLeft = new BucketType("rightToLeft", Some(TypeOp.ArrayString))
  val leftToRightWithWildCard = new BucketType("leftToRightWithWildCard", Some(TypeOp.ArrayString))
  val rightToLeftWithWildCard = new BucketType("rightToLeftWithWildCard", Some(TypeOp.ArrayString))
}
