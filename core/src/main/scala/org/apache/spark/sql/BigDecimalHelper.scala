/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.sql

import org.apache.spark.sql.types.{Decimal, DecimalType}

object BigDecimalHelper {

  def convertToDecimal(scalaValue: Any)(decimalType: DecimalType): Decimal = {
    val decimal = scalaValue match {
      case d: BigDecimal => Decimal(d)
      case d: BigInt => Decimal(d)
      case d: java.math.BigDecimal => Decimal(d)
      case d: java.math.BigInteger => Decimal(d)
      case d: Decimal => d
      case d: String => Decimal(d)
      case d: Double => Decimal(d)
      case d: Long => Decimal(d)
      case d: Int => Decimal(d)
      case _ => throw new RuntimeException(s"Cannot convert value ${scalaValue.toString} to Decimal")
    }
    decimal.toPrecision(decimalType.precision, decimalType.scale).orNull
  }

  val BigIntDecimalType = DecimalType.BigIntDecimal
}
