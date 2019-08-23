/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 *
 * author Mark - mark at arangodb.com
 */

package com.arangodb.spark.vpack

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.ArrayType
import org.apache.spark.sql.types.BooleanType
import org.apache.spark.sql.types.DateType
import org.apache.spark.sql.types.DecimalType
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.types.FloatType
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.types.LongType
import org.apache.spark.sql.types.MapType
import org.apache.spark.sql.types.NullType
import org.apache.spark.sql.types.ShortType
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.TimestampType
import com.arangodb.velocypack.VPackBuilder
import com.arangodb.velocypack.VPackSlice
import com.arangodb.velocypack.ValueType
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema

private[spark] object SpartaVPackUtils {

  def rowToVPack(row: Row): VPackSlice = {
    val builder = new VPackBuilder()
    builder.add(ValueType.OBJECT)
    row.schema.fields.zipWithIndex.foreach { addField(_, row, builder) }
    builder.close()
    builder.slice()
  }

  def rowToVPack(row: Row, builder: VPackBuilder): VPackSlice = {

    builder.add(ValueType.OBJECT)
    row.schema.fields.zipWithIndex.foreach { addField(_, row, builder) }
    builder.close()
    builder.slice()
  }


  //scalastyle:off
  private def addField(field: (StructField, Int), row: Row, builder: VPackBuilder): Unit = {
    val name = field._1.name
    val i = field._2
    field._1.dataType match {
      case BooleanType    => builder.add(name, java.lang.Boolean.valueOf(row.getBoolean(i)))
      case DoubleType     => builder.add(name, java.lang.Double.valueOf(row.getDouble(i)))
      case FloatType      => builder.add(name, java.lang.Float.valueOf(row.getFloat(i)))
      case LongType       => builder.add(name, java.lang.Long.valueOf(row.getLong(i)))
      case IntegerType    => builder.add(name, java.lang.Integer.valueOf(row.getInt(i)))
      case ShortType      => builder.add(name, java.lang.Short.valueOf(row.getShort(i)))
      case StringType     => builder.add(name, java.lang.String.valueOf(row.getString(i)))
      case DateType       => builder.add(name, row.getDate(i))
      case TimestampType  => builder.add(name, row.getTimestamp(i))
      case t: DecimalType => builder.add(name, row.getDecimal(i))
      case t: MapType => {
        builder.add(name, ValueType.OBJECT)
        row.getMap[String, Any](i).foreach { case (name, value) => addValue(name, value, builder) }
        builder.close()
      }
      case t: ArrayType => {
        builder.add(name, ValueType.ARRAY)
        row.getSeq[Any](i).foreach { value => addValue(null, value, builder) }

        builder.close()
      }
      case NullType           => builder.add(name, ValueType.NULL)
      case struct: StructType => builder.add(name, rowToVPack(row.getStruct(i)))
      case _                  => // TODO
    }
  }

  private def addValue(name: String, value: Any, builder: VPackBuilder): Unit = {
    value match {
      case value: Boolean            => builder.add(name, java.lang.Boolean.valueOf(value))
      case value: Double             => builder.add(name, java.lang.Double.valueOf(value))
      case value: Float              => builder.add(name, java.lang.Float.valueOf(value))
      case value: Long               => builder.add(name, java.lang.Long.valueOf(value))
      case value: Int                => builder.add(name, java.lang.Integer.valueOf(value))
      case value: Short              => builder.add(name, java.lang.Short.valueOf(value))
      case value: String             => builder.add(name, java.lang.String.valueOf(value))
      case value: java.sql.Date      => builder.add(name, value)
      case value: java.sql.Timestamp => builder.add(name, value)
      case value : Map[String, Any] => { builder.add(name, ValueType.OBJECT)
        value.asInstanceOf[Map[String, Any]].keys.foreach(key => addValue(key, value(key), builder))
        builder.close()}
      case value : GenericRowWithSchema  =>  rowToVPack(value, builder)
      case _                         => // TODO
    }
  }

}