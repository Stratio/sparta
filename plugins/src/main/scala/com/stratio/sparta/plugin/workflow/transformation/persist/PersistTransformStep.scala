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

package com.stratio.sparta.plugin.workflow.transformation.persist

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformStep}
import org.apache.spark.sql.Row
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext

import scala.util.Try

abstract class PersistTransformStep[Underlying[Row]](name: String,
                                                     outputOptions: OutputOptions,
                                                     ssc: Option[StreamingContext],
                                                     xDSession: XDSession,
                                                     properties: Map[String, JSerializable])
                                                    (implicit dsMonadEvidence: Underlying[Row] =>
                                                      DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, ssc, xDSession, properties) {

  lazy val storageLevel: Option[StorageLevel] = properties.getString("storageLevel", None).notBlank
    .flatMap(level => Try(StorageLevel.fromString(level)).toOption)

  def transformFunc: RDD[Row] => RDD[Row] = {
    case rdd1 =>
      rdd1.persist(storageLevel.getOrElse(StorageLevel.MEMORY_ONLY_SER))
  }
}
