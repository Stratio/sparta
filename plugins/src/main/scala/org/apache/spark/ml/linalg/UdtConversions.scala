/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package org.apache.spark.ml.linalg

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.CatalystTypeConverters
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow
import org.apache.spark.sql.types.{StructField, StructType}

object UdtConversions {

  def convertRow(row: Row, schema: StructType): Row = {
    val toCatalystConverter = CatalystTypeConverters.createToCatalystConverter(row.schema)
    val convertedRow = toCatalystConverter(row).asInstanceOf[GenericInternalRow]
    val toScalaConverter = CatalystTypeConverters.createToScalaConverter(schema)
    toScalaConverter(convertedRow).asInstanceOf[Row]
  }

  def newSchema(schema: StructType): StructType = StructType(schema.fields.map{ field =>
    field.dataType match {
      case udt: VectorUDT =>
        StructField(field.name, udt.sqlType)
      case _ => field
    }
  })
}
