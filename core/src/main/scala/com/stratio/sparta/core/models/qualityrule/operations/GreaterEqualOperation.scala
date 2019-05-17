/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.models.qualityrule.operations

import com.stratio.sparta.core.helpers.CastingHelper._
import com.stratio.sparta.core.models.SpartaQualityRulePredicate
import com.stratio.sparta.core.models.qualityrule.BinaryOperation
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

class GreaterEqualOperation[T,U](ordering: Ordering[T])(implicit predicate : SpartaQualityRulePredicate, schemaDF: StructType) extends BinaryOperation with Serializable {

  override val spartaPredicate: SpartaQualityRulePredicate = predicate
  override val schema: StructType = schemaDF

  override def operation[_]: Row => Boolean = (row: Row) => {
    ordering.gteq(row.getAs[T](row.fieldIndex(field)),
      castingToSchemaType(fieldType, secondOperand).asInstanceOf[T])
  }

}