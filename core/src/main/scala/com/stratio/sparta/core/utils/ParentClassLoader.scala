/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.utils

/**
  * A class loader which makes some protected methods in ClassLoader accessible.
  */
class ParentClassLoader(parent: ClassLoader) extends ClassLoader(parent) {

  override def findClass(name: String): Class[_] = {
    super.findClass(name)
  }

  override def loadClass(name: String): Class[_] = {
    super.loadClass(name)
  }

  override def loadClass(name: String, resolve: Boolean): Class[_] = {
    super.loadClass(name, resolve)
  }

}