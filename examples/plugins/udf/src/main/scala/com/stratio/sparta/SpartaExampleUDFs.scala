/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.sdk.lite.common

import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.types.StringType

import scala.util.Try

case class ToUpperCaseUDF() extends SpartaUDF {

  val name = "uppercaseSparta"

  val upper: String => String = _.toUpperCase

  val userDefinedFunction: UserDefinedFunction =
    UserDefinedFunction(upper , StringType, Option(Seq(StringType)))
}

case class ConcatUDF() extends SpartaUDF {

  val name = "concatSparta"

  val upper: (String, String) => String =  { case (str1, str2) =>
    s"$str1/$str2"
  }

  val userDefinedFunction: UserDefinedFunction =
    UserDefinedFunction(upper , StringType, Option(Seq(StringType, StringType)))
}

case class ToUpperCaseWithReflectionUDF() extends SpartaUDF {

  val name = "upperCaseReflect"

  val upper: String => String = _.toUpperCase

  val userDefinedFunction: UserDefinedFunction = {
    val inputTypes = Try(ScalaReflection.schemaFor(ScalaReflection.localTypeOf[String]).dataType :: Nil).toOption
    UserDefinedFunction(upper , ScalaReflection.schemaFor(ScalaReflection.localTypeOf[String]).dataType, inputTypes)
  }
}