/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.core.models.qualityrule.operations

import com.stratio.sparta.core.helpers.CastingHelper.castingToSchemaType
import com.stratio.sparta.core.models.SpartaQualityRulePredicate
import com.stratio.sparta.core.models.qualityrule.NaryOperation
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

class NotInOperation[T, U](ordering: Ordering[T])(implicit predicate : SpartaQualityRulePredicate, schemaDF: StructType) extends NaryOperation with Serializable {

  override val spartaPredicate: SpartaQualityRulePredicate = predicate
  override val schema: StructType = schemaDF

 override def operation[_]: Row => Boolean = (row: Row)  => {
     val element = row.getAs[T](row.fieldIndex(field))
     val castedOperands = operands.map(castingToSchemaType(fieldType, _).asInstanceOf[T])
     !castedOperands.contains(element)
  }
}
