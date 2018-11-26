/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.sftp

import java.io.File
import java.nio.file.Files

import com.github.nscala_time.time.Imports.DateTime
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.workflow.input.sftp.SftpConfigSuiteWithFileOperations
import com.stratio.sparta.plugin.workflow.output.parquet.Person
import com.stratio.sparta.serving.core.models.enumerators.SftpFileTypeEnum
import org.apache.commons.io.FileUtils
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, ShouldMatchers}

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class SFTPParquetOutputStepIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll with SftpConfigSuiteWithFileOperations {

  private def fileExists(path: String): Boolean = new File(path).exists()

  val baseDir = "/tmp/sftp"
  val destinationFile = s"$baseDir/sftp_parquet_test.parquet"

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
      "fileType" -> SftpFileTypeEnum.parquet.toString,
      "host" -> sftpHost,
      "port" -> sftpPort.toString,
      "sftpServerUsername" -> "foo",
      "password" -> "pass",
      "fileName" -> "sftp_parquet_test",
      "tlsEnable" -> "false"
    )

    val time = DateTime.now.getMillis

    val data = sparkSession.createDataFrame(sc.parallelize(
      Seq(Person("Kevin", 18, time), Person("Kira", 21, time), Person("Ariadne", 26, time))))

  }

  trait WithEventData extends CommonValues {

    val parquetOutput = new SFTPOutputStep("sftp-parquet-test", sparkSession, properties)

  }

  trait WithoutGranularity extends CommonValues {

    val datePattern = "yyyy/MM/dd"
    val parquetOutput = new SFTPOutputStep("sftp-parquet-test", sparkSession, properties ++ Map("datePattern" -> datePattern))
    val expectedPath = "/0"
  }

  "ParquetOutputStepIT" should "save a dataFrame" in new WithEventData {
    parquetOutput.save(data, SaveModeEnum.Append, Map.empty[String, String])

    val tempPath = Files.createTempDirectory("sftp_parquet_test")

    downloadFile(destinationFile, tempPath.toAbsolutePath.toString)
    fileExists(tempPath.toAbsolutePath.toString) should equal(true)

    val read = sparkSession.read.parquet(s"${tempPath.toAbsolutePath}/sftp_parquet_test.parquet")

    read.count should be(3)
    read should be eq data
    FileUtils.deleteDirectory(new File(tempPath.toString))
  }

}