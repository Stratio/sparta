/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.sftp

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.OutputOptions
import com.stratio.sparta.plugin.TemporalSparkContext
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.scalatest._

class SFTPCsvInputStepBatchIT extends TemporalSparkContext with Matchers with SftpConfigSuiteWithFileOperations {

  val resourcePath = getClass().getResource("/test.csv").getFile
  val targetPath = "/upload/test.csv"

  override def beforeAll() = {
    uploadFile(resourcePath, targetPath)
  }

  val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName")

  "Events in sftp csv file" should "match the number of events and the content" in {
    val properties = Map("path" -> targetPath, "fileType" -> "csv", "host" -> sftpHost,
      "port" -> sftpPort.toString, "username" -> "foo", "password" -> "pass", "header" -> "true")
    val input = new SFTPInputStepBatch("name", outputOptions, Option(ssc), sparkSession, properties)
    val outputSchema = StructType(Seq(StructField("name", StringType)))
    val dataOut = Seq(new GenericRowWithSchema(Array("sparta"), outputSchema)).toArray

    val rdd = input.initWithSchema()._1

    val count = rdd.ds.count()
    val data = rdd.ds.collect()
    count shouldBe 1
    rdd.ds.collect().toSeq should be(dataOut)

  }
}
