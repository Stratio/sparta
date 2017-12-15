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
package org.apache.spark.streaming.datasource.models

case class OffsetConditions(fromOffset: OffsetField,
                            limitRecords: Option[Long]) {

  override def toString: String =
    s"[FromOffsets: $fromOffset LimitRecords: ${limitRecords.getOrElse("")}]"
}

object OffsetConditions {

  /**
    * Constructor Offset conditions for monitoring tables, one field is necessary, the initial value and the operator
    * can be optionals. The user can limit the results on each query if the number of records are very big
    *
    * @param fromOffset   Field object that contains the field name, the value and the operation > or <
    * @param limitRecords Limit the number of records returned on each query
    * @return The offset conditions object
    */
  def apply(
             fromOffset: OffsetField,
             limitRecords: Long
           ): OffsetConditions = new OffsetConditions(fromOffset, Option(limitRecords))

  /**
    * Constructor Offset conditions for monitoring tables, one field is necessary, the initial value and the operator
    * can be optionals.
    *
    * @param fromOffset Field object that contains the field name, the value and the operation > or <
    * @return The offset conditions object
    */
  def apply(fromOffset: OffsetField): OffsetConditions = new OffsetConditions(fromOffset, None)
}
