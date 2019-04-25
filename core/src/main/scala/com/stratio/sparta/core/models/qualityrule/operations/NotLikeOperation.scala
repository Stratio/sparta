/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.core.models.qualityrule.operations

import java.util.regex.Pattern

import com.stratio.sparta.core.models.SpartaQualityRulePredicate
import com.stratio.sparta.core.models.qualityrule.BinaryOperation
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.util.StringUtils
import org.apache.spark.sql.types.{StringType, StructType}

class NotLikeOperation[T, U](ordering: Ordering[T])(implicit predicate : SpartaQualityRulePredicate, schemaDF: StructType) extends BinaryOperation with Serializable {

  import NotLikeOperation._

  override val spartaPredicate: SpartaQualityRulePredicate = predicate
  override val schema: StructType = schemaDF

 override def operation[_]: Row => Boolean = (row: Row)  => {
    if ( fieldType != StringType ) throw new RuntimeException(s"Cannot apply a Like operand to field $field whose type is $fieldType : the only allowed type is StringType")
    else {
      !matches(createPattern(secondOperand), row.getString(row.fieldIndex(field)))
    }
  }
}


// Check inside the package org.apache.spark.sql.catalyst.expressions
// case class Like(left: Expression, right: Expression) extends StringRegexExpression
object NotLikeOperation {
  def escape(v: String): String = StringUtils.escapeLikeRegex(v)
  def createPattern(p: String): Pattern = Pattern.compile(escape(p))
  def matches(regex: Pattern, str: String): Boolean = regex.matcher(str).matches()
}
