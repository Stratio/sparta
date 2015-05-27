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
package com.stratio.sparkta.plugin.dimension.tag

import java.io.{Serializable => JSerializable}

import TagDimension._
import com.stratio.sparkta.sdk.{BucketType, Bucketer}
import com.stratio.sparkta.plugin.bucketer.tag.TagBucketer._
import com.stratio.sparkta.sdk.TypeOp
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk._

case class TagDimension() extends Bucketer {

  override val bucketTypes: Seq[BucketType] = Seq(
    getFirstTag(getTypeOperation, defaultTypeOperation),
    getLastTag(getTypeOperation, defaultTypeOperation),
    getAllTags(getTypeOperation, defaultTypeOperation))

  override def bucket(value: JSerializable): Map[BucketType, JSerializable] =
    bucketTypes.map(bt => bt -> TagDimension.bucket(value.asInstanceOf[Iterable[JSerializable]], bt)).toMap

}

object TagDimension {

  private final val FirstTagName = "fistTag"
  private final val LastTagName = "lastTag"
  private final val AllTagsName = "allTags"
  
  private def bucket(value: Iterable[JSerializable], bucketType: BucketType): JSerializable =
    bucketType.id match {
      case name if name == FirstTagName => value.head
      case name if name == LastTagName => value.last
      case name if name == AllTagsName => value.toSeq.asInstanceOf[JSerializable]
    }

  def getFirstTag(typeOperation: Option[TypeOp], default : TypeOp): BucketType =
    new BucketType(FirstTagName, Some(TypeOp.String))
  def getLastTag(typeOperation: Option[TypeOp], default : TypeOp): BucketType =
    new BucketType(LastTagName, Some(TypeOp.String))
  def getAllTags(typeOperation: Option[TypeOp], default : TypeOp): BucketType =
    new BucketType(AllTagsName, Some(TypeOp.String))
}
