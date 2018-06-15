/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.gosec.dyplon.plugins.sparta

import com.stratio.gosec.dyplon.model._
import com.stratio.gosec.dyplon.model.metadata.Scope
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class ManifestTest extends FlatSpec with Matchers{

  import com.stratio.gosec.dyplon.model.dsl.PluginInstanceDsl._

  trait Data extends GosecSerializer {

    val path = "manifest.json"
    val manifest = Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(path)).mkString
    val file = Scope("file", Seq(View,Create,Delete))
    val folder = file.copy(`type`= "folder")
    //Simple test for metadata parsing
    val plugin = PluginInstance("SpartaTest","1", Seq(file, folder), None, None)
  }

  "Manifest" should "be parsed from file" in new Data {
    val pluginRead = manifest.toPlugin

    pluginRead should be(plugin)
  }
}
