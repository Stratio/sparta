/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.dummydebug

import java.io.File
import java.net.URL

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.models.OutputOptions

import scala.io.Source

trait DummyDebugTestUtils {

  val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName")
  val fileResourcePath: URL = getClass().getResource("/origin.txt")
  val lines = Source.fromURL(fileResourcePath).getLines().toList
  val parentDir = new File(fileResourcePath.getPath).getParent


  val exampleUserDefined =
    """Lorem ipsum dolor sit amet, consectetur adipiscing elit.
      |Sed ac nibh eu lectus dignissim vestibulum et in sem.
      |Integer commodo diam ac commodo blandit.
      |Nulla sit amet rhoncus ex.
      |Morbi viverra interdum aliquam.
      |Integer ac eleifend urna, in hendrerit ex.
      |Nunc convallis lacus nibh, eu rhoncus est maximus sed.
      |Etiam accumsan ex id tempus vehicula.
      |Duis est metus, facilisis interdum fringilla sit amet, commodo vitae risus.
      |Maecenas suscipit justo id leo pharetra, vel finibus ex tempus.
      |Aliquam aliquam ante nisi, vel pellentesque justo blandit a.
      |Cras vel risus vitae mi porta auctor eget quis tortor.
      |Nullam elementum turpis sed eros aliquet dapibus.
      |Pellentesque id sapien quam.
      |Vivamus posuere justo et lorem tincidunt blandit.
      |Sed magna felis, accumsan a vestibulum nec, faucibus nec augue.
      |Cras arcu neque, gravida ut purus id, sollicitudin mattis eros.
      |Aliquam non lectus ac tortor pretium vulputate.
      |Maecenas tristique odio id tellus feugiat mollis sit amet a nulla.
      |Nunc ex erat, tristique ac congue sit amet, scelerisque et lectus.
      |Sed quis erat eget nisi faucibus varius.
      |Integer ut enim urna.""".stripMargin

  object TestJsonUtil {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    def toJson(value: Any): String = {
      mapper.writeValueAsString(value)
    }

    def fromJson[T](json: String)(implicit m : Manifest[T]): T = {
      mapper.readValue[T](json)
    }
  }
}
