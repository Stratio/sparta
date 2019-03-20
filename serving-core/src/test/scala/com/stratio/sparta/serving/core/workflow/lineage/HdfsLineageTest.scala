/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.workflow.lineage

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}


@RunWith(classOf[JUnitRunner])
class HdfsLineageTest extends FlatSpec with Matchers {

  "if a path contains HDFS scheme" should "be used by lineage" in new HdfsLineage {
    override val lineageResourceSuffix: Option[String] = None
    override val lineagePath: String = "hdfs://path/to/file.csv"

    isHdfsScheme shouldBe true
  }

  "if a path contains a relative path without hdfs-site and core-site" should "not be used by lineage" in new HdfsLineage {
    override val lineageResourceSuffix: Option[String] = None
    override val lineagePath: String = "/path/to/file.csv"

    val hdfsConfig = new Configuration()
    FileSystem.get(hdfsConfig).getScheme should not be "hdfs"
    hdfsConfig.set("fs.defaultFS", "hdfs://localhost:8020")
    FileSystem.get(hdfsConfig).getScheme shouldBe "hdfs"

    isHdfsScheme shouldBe false
  }

  "if a path contains a local file path scheme" should "not be used by lineage" in new HdfsLineage {
    override val lineageResourceSuffix: Option[String] = None
    override val lineagePath: String = "file:///path/to/file.csv"

    isHdfsScheme shouldBe false
  }

}