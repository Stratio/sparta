/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.workflow.lineage

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class HdfsLineageTest extends FlatSpec with Matchers {

  "if an absolute path is given" should "return defaultFs" in new HdfsLineage {
    val defaultFs = "hdfs-01"
    override val lineageResourceSuffix: Option[String] = None
    override val lineagePath: String = "hdfs:///path/to/file.csv"
    override lazy val hdfsConfig: Map[String, String] = Map(
      "fs.defaultFS" -> s"hdfs://$defaultFs"
    )

    isHdfsScheme shouldBe true
    currentHdfs shouldBe defaultFs
  }

  "if a relative path is given" should "return defaultFs" in new HdfsLineage {
    val defaultFs = "hdfs-01"
    override val lineageResourceSuffix: Option[String] = None
    override val lineagePath: String = "/path/to/file.csv"
    override lazy val hdfsConfig: Map[String, String] = Map(
      "fs.defaultFS" -> s"hdfs://$defaultFs"
    )

    currentHdfs shouldBe defaultFs
  }

  "if a path contains other HDFS scheme" should "check if its defined in nameservices" in new HdfsLineage {
    val serviceKey = "hdfs-02"
    override val lineageResourceSuffix: Option[String] = None
    override val lineagePath: String = s"hdfs://$serviceKey/path/to/file.csv"
    override lazy val hdfsConfig: Map[String, String] = Map(
      "fs.defaultFS" -> "hdfs://hdfs-01",
      "dfs.nameservices" -> s"hdfs-01,$serviceKey"
    )

    isHdfsScheme shouldBe true
    currentHdfs shouldBe serviceKey
  }

  "if a path contains other HDFS scheme not in nameservices" should "not be used by lineage" in new HdfsLineage {
    val serviceKey = "hdfs-03"
    override val lineageResourceSuffix: Option[String] = None
    override val lineagePath: String = s"hdfs://$serviceKey/path/to/file.csv"
    override lazy val hdfsConfig: Map[String, String] = Map(
      "fs.defaultFS" -> "hdfs://hdfs-01",
      "dfs.nameservices" -> "hdfs-01,hdfs-02"
    )

    isHdfsScheme shouldBe true
    currentHdfs shouldBe ""
  }

  "if a path contains a relative path without hdfs-site and core-site" should "not be used by lineage" in new HdfsLineage {
    override val lineageResourceSuffix: Option[String] = None
    override val lineagePath: String = "/path/to/file.csv"
    override lazy val hdfsConfig: Map[String, String] = Map.empty[String, String]

    val config = new Configuration()
    FileSystem.get(config).getScheme should not be "hdfs"
    config.set("fs.defaultFS", "hdfs://localhost:8020")
    FileSystem.get(config).getScheme shouldBe "hdfs"

    isHdfsScheme shouldBe false
    currentHdfs shouldBe ""
  }

  "if a path contains a local file path scheme" should "not be used by lineage" in new HdfsLineage {
    override val lineageResourceSuffix: Option[String] = None
    override val lineagePath: String = "file:///path/to/file.csv"
    override lazy val hdfsConfig: Map[String, String] = Map.empty[String, String]

    isHdfsScheme shouldBe false
    currentHdfs shouldBe ""
  }

}