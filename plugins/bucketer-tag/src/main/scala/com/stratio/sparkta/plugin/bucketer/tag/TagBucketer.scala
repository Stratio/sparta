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

import java.io

import com.stratio.sparkta.plugin.bucketer.tag.TagBucketer._
import com.stratio.sparkta.sdk.{BucketType, Bucketer}

/**
 * Created by ajnavarro on 27/10/14.
 */
case class TagBucketer(override val bucketTypes: Seq[BucketType] =
                       Seq(allTags)) extends Bucketer {

  override def bucketForWrite(value: io.Serializable): Map[BucketType, Seq[io.Serializable]] = {
    bucketTypes.map(bt => bt -> bucket(value.asInstanceOf[Iterable[_]], bt)).toMap
  }

}

object TagBucketer {
  private def bucket(value: Iterable[_], bucketType: BucketType): Seq[io.Serializable] = {
    (bucketType match {
      case x if x == firstTag => Seq(value.seq.head.asInstanceOf[io.Serializable])
      case x if x == lastTag => Seq(value.seq.tail.asInstanceOf[io.Serializable])
      case x if x == allTags => value.seq.map(_.asInstanceOf[io.Serializable]).toSeq
    })
  }

  val firstTag = new BucketType("firstTag")
  val lastTag = new BucketType("lastTag")
  val allTags = new BucketType("allTags")
}
