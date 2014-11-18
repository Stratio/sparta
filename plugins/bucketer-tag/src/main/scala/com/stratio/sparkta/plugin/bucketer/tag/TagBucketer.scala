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
package com.stratio.sparkta.plugin.bucketer.tag

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.plugin.bucketer.tag.TagBucketer._
import com.stratio.sparkta.sdk.{BucketType, Bucketer}

case class TagBucketer() extends Bucketer {

  override val bucketTypes: Seq[BucketType] = Seq(allTags, lastTag, firstTag)

  override def bucket(value: JSerializable): Map[BucketType, JSerializable] =
    bucketTypes.map(bt =>
      bt -> TagBucketer.bucket(value.asInstanceOf[Iterable[JSerializable]], bt)
    ).toMap

}

object TagBucketer {
  private def bucket(value: Iterable[JSerializable], bucketType: BucketType): JSerializable =
    bucketType match {
      case `firstTag` => value.head
      case `lastTag` => value.last
      case `allTags` => value.toSeq.asInstanceOf[JSerializable]
    }

  val firstTag = new BucketType("firstTag")
  val lastTag = new BucketType("lastTag")
  val allTags = new BucketType("allTags")
}
