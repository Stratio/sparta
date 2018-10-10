/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.performance.common

import java.io.File

import scala.sys.process._

// TODO
trait WithSSOCookie {

  println("¡Test to check cookie!")
  val fileStream = getClass.getResource("/sso.py").getPath
  println(fileStream)
  val cookieAutoFile = Process("python3 "+getClass.getResource("/sso.py").getPath+" https://stratiodatagovernance.com/dictionaryapi/v1/login admin 1234", new File("/tmp")).!!
  val output2 =cookieAutoFile.substring(0,cookieAutoFile.size-1)
  println("valor2="+output2+"fin")

}
