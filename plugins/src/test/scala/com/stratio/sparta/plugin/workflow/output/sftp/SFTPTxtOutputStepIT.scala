/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.sftp

import java.io.File
import java.nio.file.Files
import java.sql.Timestamp
import java.time.Instant

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.workflow.input.sftp.SftpConfigSuiteWithFileOperations
import com.stratio.sparta.serving.core.models.enumerators.SftpFileTypeEnum
import org.apache.commons.io.FileUtils
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, ShouldMatchers}

import scala.util.{Random, Try}

@RunWith(classOf[JUnitRunner])
class SFTPTxtOutputStepIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll with SftpConfigSuiteWithFileOperations {

  private def fileExists(path: String): Boolean = new File(path).exists()

  val baseDir = "/tmp/sftp"
  val destinationFile = s"$baseDir/sftp_txt_test.txt"

  override def beforeAll() = {
    Try {
      createSftpDir(baseDir)
    }
  }

  trait CommonValues {

    val properties = Map(
      "path" -> baseDir,
      "fileType" -> SftpFileTypeEnum.txt.toString,
      "host" -> sftpHost,
      "port" -> sftpPort.toString,
      "sftpServerUsername" -> "foo",
      "password" -> "pass",
      "fileName" -> "sftp_txt_test",
      "tlsEnable" -> "false",
      "saveOptions" ->
        """[
          |{
          |   "saveOptionsKey": "delimiter",
          |   "saveOptionsValue": ":"
          |}
          |]""".stripMargin
    )

    val schema = StructType(Seq(
      StructField("name", StringType),
      StructField("age", IntegerType),
      StructField("year", LongType)
    ))

    val data = sparkSession.createDataFrame(sc.parallelize(Seq(
      Row("Kevin", Random.nextInt, Timestamp.from(Instant.now).getTime),
      Row("Kira", Random.nextInt, Timestamp.from(Instant.now).getTime),
      Row("Ariadne", Random.nextInt, Timestamp.from(Instant.now).getTime)
    )), schema)

  }

  trait WithEventData extends CommonValues {
    val sftpTxtOutput = new SFTPOutputStep("sftp-txt-test", sparkSession, properties)
  }

  "Given a DataFrame, a directory" should "be created with the data written inside" in new WithEventData {
    sftpTxtOutput.save(data, SaveModeEnum.Append, Map.empty[String, String])

    val tempPath = Files.createTempDirectory("sftp_txt_test")

    downloadFile(destinationFile, tempPath.toAbsolutePath.toString)
    fileExists(tempPath.toAbsolutePath.toString) should equal(true)

    val read = sparkSession.read.text(s"${tempPath.toAbsolutePath}/sftp_txt_test.txt")

    read.count should be(3)
    read should be eq data
    FileUtils.deleteDirectory(new File(tempPath.toString))
  }

}