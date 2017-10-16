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
package com.stratio.sparta.plugin.workflow.input.filesystem

import java.io.{File, PrintWriter}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source

@RunWith(classOf[JUnitRunner])
class FileSystemInputStepIT extends TemporalSparkContext with Matchers {

  val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName")

  val resourcePath = getClass().getResource("/origin.txt")
  val lines = Source.fromURL(resourcePath).getLines().toList
  val parentDir = new File(resourcePath.getPath).getParent

  def createTestFile(name: String): File = {
    val file = new File(s"$parentDir/$name")
    val out = new PrintWriter(file)
    lines.foreach(out.println)
    out.close()
    file
  }

  var existingFile: File = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    existingFile = createTestFile("existing.txt")
  }


  "Events counted" should "match the lines in both the created file and an existing one" in {

    val properties = Map(
      "directory" -> s"file://$parentDir",
      "newFilesOnly" -> "false"
    )

    val input = new FileSystemInputStep("name", outputOptions, ssc, sparkSession, properties)
    val dstream = input.initStream
    val totalEvents = ssc.sparkContext.longAccumulator

    dstream foreachRDD { rdd =>
      val count = rdd.count()
      totalEvents.add(count)
    }

    ssc.start()

    val createdFile = Future(createTestFile("output.txt"))

    val atMost = 3 seconds

    ssc.awaitTerminationOrTimeout(atMost.toMillis)
    Await.result(createdFile, atMost).delete()

    totalEvents.value shouldBe lines.size*2


  }

  it should "just match the lines of not filtered files" in {

    val properties = Map(
      "directory" -> s"file://$parentDir",
      "newFilesOnly" -> "false",
      "filterString" -> "filtered,existing"
    )

    val input = new FileSystemInputStep("name", outputOptions, ssc, sparkSession, properties)
    val dstream = input.initStream
    val totalEvents = ssc.sparkContext.longAccumulator

    dstream foreachRDD { rdd =>
      val count = rdd.count()
      totalEvents.add(count)
    }

    ssc.start()

    val fileCreationFutures = Seq("output.txt", "output.filtered.txt") map {
      name => Future(createTestFile(name))
    }

    val atMost = 3 seconds

    ssc.awaitTerminationOrTimeout(atMost.toMillis)

    fileCreationFutures foreach (Await.result(_, atMost).delete())

    totalEvents.value shouldBe lines.size
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    existingFile.delete()
  }

}