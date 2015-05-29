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

import java.io.{Serializable => JSerializable}

import HierarchyDimension._
import akka.event.slf4j.SLF4JLogging

import com.stratio.sparkta.plugin.bucketer.hierarchy.HierarchyBucketer._
import com.stratio.sparkta.sdk.TypeOp.TypeOp
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk._

case class HierarchyDimension(props: Map[String, JSerializable]) extends Bucketer with SLF4JLogging {

  def this() {
    this(Map())
  }

  override val properties: Map[String, JSerializable] = props ++ {
    if (!props.contains(SplitterPropertyName)) Map(SplitterPropertyName -> DefaultSplitter) else Map()
  } ++ {
    if (!props.contains(WildCardPropertyName)) Map(WildCardPropertyName -> DefaultWildCard) else Map()
  }

  final val LeftToRight = getLeftToRight(getTypeOperation(LeftToRightName), defaultTypeOperation)
  final val RightToLeft = getRightToLeft(getTypeOperation(RightToLeftName), defaultTypeOperation)
  final val LeftToRightWithWildCard =
    getLeftToRightWithWildCard(getTypeOperation(LeftToRightWithWildCardName), defaultTypeOperation)
  final val RightToLeftWithWildCard =
    getRightToLeftWithWildCard(getTypeOperation(RightToLeftWithWildCardName), defaultTypeOperation)

  override val bucketTypes: Seq[BucketType] =
    Seq(LeftToRight, RightToLeft, LeftToRightWithWildCard, RightToLeftWithWildCard)

  val splitter = properties.getString(SplitterPropertyName)
  val wildcard = properties.getString(WildCardPropertyName)

  override def bucket(value: JSerializable): Map[BucketType, JSerializable] =
    bucketTypes.map(bt => (bt, bucket(value.asInstanceOf[String], bt).asInstanceOf[JSerializable])).toMap

  private def bucket(value: String, bucketType: BucketType): Seq[JSerializable] = {
    bucketType match {
      case x if x == LeftToRight =>
        explodeWithWildcards(value, wildcard, splitter, false, false)
      case x if x == RightToLeft =>
        explodeWithWildcards(value, wildcard, splitter, true, false)
      case x if x == LeftToRightWithWildCard =>
        explodeWithWildcards(value, wildcard, splitter, false, true)
      case x if x == RightToLeftWithWildCard =>
        explodeWithWildcards(value, wildcard, splitter, true, true)
    }
  }

  override val defaultTypeOperation = TypeOp.String
}

object HierarchyDimension {

  private final val DefaultSplitter = "."
  private final val SplitterPropertyName = "splitter"
  private final val DefaultWildCard = "*"
  private final val WildCardPropertyName = "wildcard"
  private final val LeftToRightName = "leftToRight"
  private final val RightToLeftName = "rightToLeft"
  private final val LeftToRightWithWildCardName = "leftToRightWithWildCard"
  private final val RightToLeftWithWildCardName = "rightToLeftWithWildCard"

  def explodeWithWildcards(
                            domain: String,
                            wildcard: String,
                            splitter: String,
                            reversed: Boolean,
                            withWildcards: Boolean
                            ): Seq[JSerializable] = {
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

  def getLeftToRight(typeOperation: Option[TypeOp], default: TypeOp): BucketType =
    new BucketType(LeftToRightName, typeOperation.orElse(Some(default)))

  def getRightToLeft(typeOperation: Option[TypeOp], default: TypeOp): BucketType =
    new BucketType(RightToLeftName, typeOperation.orElse(Some(default)))

  def getLeftToRightWithWildCard(typeOperation: Option[TypeOp], default: TypeOp): BucketType =
    new BucketType(LeftToRightWithWildCardName, typeOperation.orElse(Some(default)))

  def getRightToLeftWithWildCard(typeOperation: Option[TypeOp], default: TypeOp): BucketType =
    new BucketType(RightToLeftWithWildCardName, typeOperation.orElse(Some(default)))
}
