/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.filesystem

import java.io.{File, PrintWriter}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.models.OutputOptions
import com.stratio.sparta.core.enumerators.SaveModeEnum
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source

@RunWith(classOf[JUnitRunner])
class FileSystemInputStepStreamingIT extends TemporalSparkContext with Matchers {

  val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName")

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
      "path" -> s"file://$parentDir",
      "newFilesOnly" -> "false"
    )

    val input = new FileSystemInputStepStreaming("name", outputOptions, Option(ssc), sparkSession, properties)
    val dstream = input.init
    val totalEvents = ssc.sparkContext.longAccumulator

    dstream.ds foreachRDD { rdd =>
      val count = rdd.count()
      totalEvents.add(count)
    }

    ssc.start()

    val createdFile = Future(createTestFile("output.txt"))

    val atMost = 1 seconds

    ssc.awaitTerminationOrTimeout(atMost.toMillis)
    Await.result(createdFile, atMost).delete()

    totalEvents.value shouldBe lines.size*2


  }

  it should "just match the lines of not filtered files" in {

    val properties = Map(
      "path" -> s"file://$parentDir",
      "newFilesOnly" -> "false",
      "filterString" -> "filtered,existing"
    )

    val input = new FileSystemInputStepStreaming("name", outputOptions, Option(ssc), sparkSession, properties)
    val dstream = input.init
    val totalEvents = ssc.sparkContext.longAccumulator

    dstream.ds foreachRDD { rdd =>
      val count = rdd.count()
      totalEvents.add(count)
    }

    ssc.start()

    val fileCreationFutures = Seq("output.txt", "output.filtered.txt") map {
      name => Future(createTestFile(name))
    }

    val atMost = 1 seconds

    ssc.awaitTerminationOrTimeout(atMost.toMillis)

    fileCreationFutures foreach (Await.result(_, atMost).delete())

    totalEvents.value shouldBe lines.size
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    existingFile.delete()
  }

}