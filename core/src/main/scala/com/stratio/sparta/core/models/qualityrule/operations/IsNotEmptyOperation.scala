/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.core.models.qualityrule.operations

import com.stratio.sparta.core.models.SpartaQualityRulePredicate
import com.stratio.sparta.core.models.qualityrule.Operation
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructType}

class IsNotEmptyOperation[T, U](ordering: Ordering[T])(implicit predicate : SpartaQualityRulePredicate, schemaDF: StructType) extends Operation with Serializable {

  override val spartaPredicate: SpartaQualityRulePredicate = predicate
  override val schema: StructType = schemaDF

 override def operation[_]: Row => Boolean = nullPointerExceptionHandler((row: Row)  => {
   if ( fieldType != StringType ) throw new RuntimeException(s"Cannot apply a isNotEmpty operand to field $field whose type is $fieldType : the only allowed type is StringType")
   else {
     row.getString(row.fieldIndex(field)).trim.nonEmpty
   }
  })
}
