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
                            fromBeginning: Boolean,
                            forcedBeginning: Option[Boolean],
                            limitRecords: Option[Long]) {

  override def toString: String =
    s"[FromOffsets: $fromOffset" +
      s"FromBeginning: $fromBeginning" +
      s"forcedFromBeginning: ${forcedBeginning.getOrElse("")}" +
      s"LimitRecords: ${limitRecords.getOrElse("")}]"
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
           ): OffsetConditions = new OffsetConditions(fromOffset, false, None, Option(limitRecords))

  /**
    * Constructor Offset conditions for monitoring tables, one field is necessary, the initial value and the operator
    * can be optionals. The user can limit the results on each query if the number of records are very big
    *
    * @param fromOffset    Field object that contains the field name, the value and the operation > or <
    * @param fromBeginning Start process with the fromOffset field ignoring the saved offsets
    * @param limitRecords  Limit the number of records returned on each query
    * @return The offset conditions object
    */
  def apply(
             fromOffset: OffsetField,
             fromBeginning: Boolean,
             limitRecords: Long
           ): OffsetConditions = new OffsetConditions(fromOffset, fromBeginning, None, Option(limitRecords))

  /**
    * Constructor Offset conditions for monitoring tables, one field is necessary, the initial value and the operator
    * can be optionals. The user can limit the results on each query if the number of records are very big
    *
    * @param fromOffset    Field object that contains the field name, the value and the operation > or <
    * @param fromBeginning Start process with the fromOffset field ignoring the saved offsets
    * @param forcedFromBeginning Start process with the fromOffset field ignoring the saved offsets in ZK
    * @param limitRecords  Limit the number of records returned on each query
    * @return The offset conditions object
    */
  def apply(
             fromOffset: OffsetField,
             fromBeginning: Boolean,
             forcedFromBeginning: Boolean,
             limitRecords: Long
           ): OffsetConditions =
    new OffsetConditions(fromOffset, fromBeginning, Option(forcedFromBeginning), Option(limitRecords))

  /**
    * Constructor Offset conditions for monitoring tables, one field is necessary, the initial value and the operator
    * can be optionals and from beginning options
    *
    * @param fromOffset    Field object that contains the field name, the value and the operation > or <
    * @param fromBeginning Start process with the fromOffset field ignoring the saved offsets
    * @param forcedFromBeginning Start process with the fromOffset field ignoring the saved offsets in ZK
    * @return The offset conditions object
    */
  def apply(
             fromOffset: OffsetField,
             fromBeginning: Boolean,
             forcedFromBeginning: Boolean
           ): OffsetConditions = new OffsetConditions(fromOffset, fromBeginning, Option(forcedFromBeginning), None)

  /**
    * Constructor Offset conditions for monitoring tables, one field is necessary, the initial value and the operator
    * can be optionals. The user can limit the results on each query if the number of records are very big
    *
    * @param fromOffset    Field object that contains the field name, the value and the operation > or <
    * @param fromBeginning Start process with the fromOffset field ignoring the saved offsets
    * @return The offset conditions object
    */
  def apply(
             fromOffset: OffsetField,
             fromBeginning: Boolean
           ): OffsetConditions = new OffsetConditions(fromOffset, fromBeginning, None, None)

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
             limitRecords: Option[Long]
           ): OffsetConditions = new OffsetConditions(fromOffset, false, None, limitRecords)

  /**
    * Constructor Offset conditions for monitoring tables, one field is necessary, the initial value and the operator
    * can be optionals.
    *
    * @param fromOffset Field object that contains the field name, the value and the operation > or <
    * @return The offset conditions object
    */
  def apply(fromOffset: OffsetField): OffsetConditions = new OffsetConditions(fromOffset, false, None, None)
}
