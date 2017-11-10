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
package com.stratio.sparta.serving.core.service

import java.io.{FileNotFoundException, InputStream}

import com.stratio.sparta.serving.core.services.HdfsService
import org.apache.hadoop.fs.{FileSystem, _}
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

import scala.util.{Failure, Try}

@RunWith(classOf[JUnitRunner])
class HdfsServiceTest extends FlatSpec with ShouldMatchers with MockitoSugar {

  val fileSystem: FileSystem = mock[FileSystem]

  val utils = new HdfsService(fileSystem, None)

  "hdfs utils" should "getfiles from a path" in {
    val expected = Array(mock[FileStatus])
    when(fileSystem.listStatus(new Path("myTestPath"))).thenReturn(expected)
    val result = utils.getFiles("myTestPath")
    result should be(expected)
  }

  it should "return single file as inputStream" in {
    val expected: InputStream = mock[FSDataInputStream]
    when(fileSystem.open(new Path("testFile"))).thenReturn(expected.asInstanceOf[FSDataInputStream])
    val result: InputStream = utils.getFile("testFile")
    result should be(expected)
  }

  it should "write" in {
    val result = Try(utils.write("from", "to", true)) match {
      case Failure(ex: Throwable) => ex
    }
    result.isInstanceOf[FileNotFoundException] should be(true)
  }

  it should "write without override" in {
    val result = Try(utils.write("from", "to", false)) match {
      case Failure(ex: Throwable) => ex
    }
    result.isInstanceOf[FileNotFoundException] should be(true)
  }
}


