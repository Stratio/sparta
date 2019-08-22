/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.sftp

import com.stratio.sparta.core.models.OutputWriterOptions
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.serving.core.models.enumerators.SftpFileTypeEnum
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.scalatest._


class SFTPParquetInputStepBatchIT extends TemporalSparkContext with Matchers with SftpConfigSuiteWithFileOperations {

  val resourcePath = getClass().getResource("/test.parquet").getFile
  val targetPath = "/tmp/sftp/test.parquet"

  override def beforeAll() = {
    uploadFile(resourcePath, targetPath)
  }

  val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

  "Events in sftp parquet file" should "match the number of events and the content" in {
    val properties = Map("path" -> targetPath, "fileType" -> SftpFileTypeEnum.parquet.toString, "host" -> sftpHost,
      "port" -> sftpPort.toString, "username" -> "foo", "password" -> "pass")
    val input = new SFTPInputStepBatch("name", outputOptions, Option(ssc), sparkSession, properties)
    val outputSchema = StructType(Seq(StructField("name", StringType)))
    val dataOut = Seq(new GenericRowWithSchema(Array("jc"), outputSchema)).toArray

    val rdd = input.initWithSchema()._1

    val count = rdd.ds.count()
    val data = rdd.ds.collect()
    count shouldBe 1
    rdd.ds.collect().toSeq should be(dataOut)

  }
}