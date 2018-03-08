/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.filesystem

import java.io.{File, PrintWriter}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source

@RunWith(classOf[JUnitRunner])
class FileSystemInputStepBatchIT extends TemporalSparkContext with Matchers {

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
      "path" -> s"file://$parentDir/existing.txt"
    )
    val input = new FileSystemInputStepBatch("name", outputOptions, Option(ssc), sparkSession, properties)
    val rdd = input.init
    val count = rdd.ds.count()

    count shouldBe lines.size

    rdd.ds.collect().toList.map(_.mkString("")) should be(lines)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    existingFile.delete()
  }

}