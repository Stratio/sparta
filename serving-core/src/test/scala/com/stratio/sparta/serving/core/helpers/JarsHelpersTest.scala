/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
