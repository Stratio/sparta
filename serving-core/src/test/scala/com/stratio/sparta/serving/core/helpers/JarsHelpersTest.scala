/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.helpers

import java.io.File

import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable


@RunWith(classOf[JUnitRunner])
class JarsHelpersTest extends FlatSpec with Matchers with MockitoSugar {

  val file =  mock[File]
  when(file.exists).thenReturn(true)
  when(file.listFiles()).thenReturn(Array(
    new File("first.jar"),
    new File("second.jar"),
    new File("sparta-driver.jar")))

  it should "find the driver jar" in {

    val seqofJars = JarsHelper.findDriverByPath(
      file)

    seqofJars should be (mutable.ArraySeq(new File("sparta-driver.jar")))
  }

  val fileNoSpartaDriver = mock[File]
  when(fileNoSpartaDriver.exists).thenReturn(true)
  when(fileNoSpartaDriver.listFiles()).thenReturn(Array(
    new File("sparta.jar"),
    new File("driver.jar"),
    new File("sparta-driver.txt"))
  )

  it should "return an empty sequence" in {
    val retrievedDrivers = JarsHelper.findDriverByPath(fileNoSpartaDriver)
    retrievedDrivers should equal(Seq.empty)
  }
}
