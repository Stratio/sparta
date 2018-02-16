/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
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

package com.stratio.sparta.dg.agent.model

import play.api.libs.json.{JsObject, Json, Reads, Writes}

import com.stratio.governance.commons.agent.model.metadata._
import com.stratio.governance.commons.agent.model.metadata.lineage.InputMetadata

case class SpartaInputMetadata(
                                name: String,
                                key: String,
                                metadataPath: MetadataPath,
                                outcomingNodes: Seq[MetadataPath],
                                agentVersion: String = SpartaType.agentVersion,
                                serverVersion: String = SpartaType.serverVersion,
                                tags: List[String],
                                modificationTime: Option[Long] = Some(System.currentTimeMillis()),
                                accessTime: Option[Long] = Some(System.currentTimeMillis()),
                                operationCommandType: OperationCommandType = OperationCommandType.UNKNOWN,
                                genericType: GenericType = GenericType.INPUT,
                                customType: CustomType = SpartaType.INPUT,
                                sourceType: SourceType = SourceType.SPARTA
                              ) extends InputMetadata {

  def serialize: String =
    Json.stringify(
      JsObject(
        Json.toJson(this).as[JsObject].fieldSet.map {
          case (key, value) => (camelToUnderscores(key), value)
        }.toMap
      )
    )

  def asMap: Map[String, Any] = this.getClass.getDeclaredFields
    .map(_.getName) // all field names
    .zip(this.productIterator.to).map(t => t._2 match {
    case mt: MetadataType => (t._1, Some(mt.value))
    case None => (t._1, None)
    case Some(_) => (t._1, t._2)
    case _ => (t._1, Some(t._2))
  }).toMap.collect {
    case (key, Some(value)) => camelToUnderscores(key) -> value
  }

  def toJson: JsObject = Json.toJson(this).as[JsObject]

  def metadataCopy(operationCommandType: OperationCommandType): Metadata =
    this.copy(operationCommandType = operationCommandType)
}

object SpartaInputMetadata {

  implicit val writes: Writes[SpartaInputMetadata] = Json.writes[SpartaInputMetadata]
  implicit val reads: Reads[SpartaInputMetadata] = Json.reads[SpartaInputMetadata]
}
