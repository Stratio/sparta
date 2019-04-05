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

import com.databricks.spark.avro._
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.workflow.input.sftp.SftpConfigSuiteWithFileOperations
import com.stratio.sparta.serving.core.models.enumerators.SftpFileTypeEnum
import org.apache.commons.io.FileUtils
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, ShouldMatchers}

import scala.util.{Random, Try}

@RunWith(classOf[JUnitRunner])
class SFTPAvroOutputStepIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll with SftpConfigSuiteWithFileOperations {

  private def fileExists(path: String): Boolean = new File(path).exists()

  val baseDir = "/tmp/sftp"
  val destinationFile = s"$baseDir/sftp_avro_test.avro"

  override def beforeAll() = {
    Try {
      createSftpDir(baseDir)
    }
  }

  override def afterAll() = {
    //deleteSftpFile(destinationFile)
  }

  trait CommonValues {

    val properties = Map(
      "path" -> baseDir,
      "fileType" -> SftpFileTypeEnum.avro.toString,
      "host" -> sftpHost,
      "port" -> sftpPort.toString,
      "sftpServerUsername" -> "foo",
      "password" -> "pass",
      "tlsEnable" -> "false"
    )

    val schema = StructType(Seq(
      StructField("name", StringType),
      StructField("age", IntegerType),
      StructField("minute", LongType)
    ))

    val data =
      sparkSession.createDataFrame(sc.parallelize(Seq(
        Row("Kevin", Random.nextInt, Timestamp.from(Instant.now).getTime),
        Row("Kira", Random.nextInt, Timestamp.from(Instant.now).getTime),
        Row("Ariadne", Random.nextInt, Timestamp.from(Instant.now).getTime)
      )), schema)
  }

  trait WithEventData extends CommonValues {
    val avroOutput = new SFTPOutputStep("sftp-avro-test", sparkSession, properties)
  }

  it should "save a dataframe " in new WithEventData {

    avroOutput.save(data, SaveModeEnum.Append, Map("tableName" -> "sftp_avro_test"))

    val tempPath = Files.createTempDirectory("sftp_avro_test")

    downloadFile(destinationFile, tempPath.toAbsolutePath.toString)
    fileExists(tempPath.toAbsolutePath.toString) should equal(true)

    val read: DataFrame = sparkSession.read.avro(s"${tempPath.toAbsolutePath}/sftp_avro_test.avro")

    read.count should be(3)
    read should be eq data
    FileUtils.deleteDirectory(new File(tempPath.toString))
  }
}