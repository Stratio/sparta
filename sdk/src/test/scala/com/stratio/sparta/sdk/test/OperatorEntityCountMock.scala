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
package com.stratio.sparta.sdk.test

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.WriteOp._
import com.stratio.sparta.sdk.{OperatorEntityCount, WriteOp}
import org.apache.spark.sql.types.StructType

class OperatorEntityCountMock(name: String, schema: StructType, properties: Map[String, JSerializable])
  extends OperatorEntityCount(name, schema, properties) {

  override def processReduce(values: Iterable[Option[Any]]): Option[Any] = values.head

  override def writeOperation: WriteOp = WriteOp.Inc
}
