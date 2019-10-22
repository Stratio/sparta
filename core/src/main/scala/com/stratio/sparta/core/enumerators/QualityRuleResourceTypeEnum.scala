/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.enumerators

object QualityRuleResourceTypeEnum extends Enumeration {

  type QualityRuleResourceType = Value

  val XD = Value("XD")
  val JDBC = Value("JDBC")
  val HDFS = Value("HDFS")

  implicit def extractQRResourceType(resourceType: String): QualityRuleResourceType =
    resourceType match {
      case s if s matches "(i?)XD" => XD
      case s if s matches "(i?)(SQL|JDBC)" => JDBC
      case s if s matches "(i?)HDFS" => HDFS
      case _ => XD
    }
}
