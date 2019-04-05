/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.sftp

import java.io.File
import java.nio.file.Files

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.workflow.input.sftp.SftpConfigSuiteWithFileOperations
import com.stratio.sparta.serving.core.models.enumerators.SftpFileTypeEnum
import org.apache.commons.io.FileUtils
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, ShouldMatchers}

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class SFTPJsonOutputStepIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll with SftpConfigSuiteWithFileOperations {

  private def fileExists(path: String): Boolean = new File(path).exists()

  val baseDir = "/tmp/sftp"
  val destinationFile = s"$baseDir/sftp_json_test.json"

  override def beforeAll() = {
    Try {
      createSftpDir(baseDir)
    }
  }

  override def afterAll() = {
    deleteSftpFile(destinationFile)
  }

  trait CommonValues {
    val properties = Map(
      "path" -> baseDir,
      "fileType" -> SftpFileTypeEnum.json.toString,
      "host" -> sftpHost,
      "port" -> sftpPort.toString,
      "sftpServerUsername" -> "foo",
      "password" -> "pass",
      "tlsEnable" -> "false"
    )
    val fields = StructType(
      StructField("name", StringType, false) ::
        StructField("age", IntegerType, false) ::
        StructField("year", IntegerType, true) :: Nil)
    val jsonStep = new SFTPOutputStep("sftp-json-test", sparkSession, properties)
    val dataRDD = sc.parallelize(List(
      ("user1", 23, 1993),
      ("user2", 26, 1990),
      ("user3", 21, 1995)
    )).map { case (name, age, year) => Row(name, age, year) }
    val inputDataFrame = sparkSession.createDataFrame(dataRDD, fields)
  }

  "Given a DataFrame, a directory" should "be created with the data inside in JSON format" in new CommonValues {
    jsonStep.save(inputDataFrame, SaveModeEnum.Append, Map("tableName" -> "sftp_json_test"))

    val tempPath = Files.createTempDirectory("sftp_json_test")

    downloadFile(destinationFile, tempPath.toAbsolutePath.toString)
    fileExists(tempPath.toAbsolutePath.toString) should equal(true)

    val read = sparkSession.read.json(s"${tempPath.toAbsolutePath}/sftp_json_test.json")

    read.count should be(3)
    read should be eq inputDataFrame
    FileUtils.deleteDirectory(new File(tempPath.toString))
  }

}
