/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.sftp

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.OutputOptions
import com.stratio.sparta.plugin.TemporalSparkContext
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.scalatest._

class SFTPXmlInputBatchIT extends TemporalSparkContext with Matchers with SftpConfigSuiteWithFileOperations {

  val resourcePath = getClass().getResource("/test.xml").getFile
  val targetPath = "/upload/test.xml"

  override def beforeAll() = {
    uploadFile(resourcePath, targetPath)
  }

  val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName")

  "SFTPXmlInputStep" should "match the number of events and the content" in {
    val properties = Map("path" -> targetPath, "fileType" -> "xml", "host" -> sftpHost,
      "port" -> sftpPort.toString, "username" -> "foo", "password" -> "pass", "rowTag" -> "book")
    val input = new SFTPInputStepBatch("name", outputOptions, Option(ssc), sparkSession, properties)
    val expectedSchema = StructType(Seq(StructField("_id", StringType, true),
      StructField("author", StringType, true),
      StructField("description", StringType, true),
      StructField("genre", StringType, true),
      StructField("price", DoubleType, true),
      StructField("publish_date", StringType, true),
      StructField("title", StringType, true)
    ))
    val rdd = input.initWithSchema()._1
    val schema = rdd.ds.first().schema
    val count = rdd.ds.count()

    count shouldBe 12

    schema should be(expectedSchema)
  }

  override def afterAll() = {
    // delete the file created during test in sftp server
    deleteSftpFile(targetPath)
  }

}