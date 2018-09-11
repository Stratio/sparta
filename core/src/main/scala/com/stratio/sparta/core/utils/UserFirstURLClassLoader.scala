/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.utils

import java.net.URL
import java.util.Enumeration

import scala.collection.JavaConverters._
import scala.reflect.internal.util.ScalaClassLoader.URLClassLoader

/**
  * A mutable class loader that gives preference to its own URLs over the parent class loader
  * when loading classes and resources.
  */
case class UserFirstURLClassLoader(urls: Array[URL], parent: ClassLoader)
  extends URLClassLoader(urls, null) {

  private val parentClassLoader = new ParentClassLoader(parent)

  override def loadClass(name: String, resolve: Boolean): Class[_] = {
    try {
      super.loadClass(name, resolve)
    } catch {
      case e: ClassNotFoundException =>
        parentClassLoader.loadClass(name, resolve)
    }
  }

  override def getResource(name: String): URL = {
    val url = super.findResource(name)
    val res = if (url != null) url else parentClassLoader.getResource(name)
    res
  }

  override def getResources(name: String): Enumeration[URL] = {
    val childUrls = super.findResources(name).asScala
    val parentUrls = parentClassLoader.getResources(name).asScala
    (childUrls ++ parentUrls).asJavaEnumeration
  }

}