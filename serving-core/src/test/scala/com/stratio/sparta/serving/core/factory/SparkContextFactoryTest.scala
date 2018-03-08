/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.factory

import com.typesafe.config.ConfigFactory
import org.apache.spark.streaming.Duration
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FlatSpec, _}

@RunWith(classOf[JUnitRunner])
class SparkContextFactoryTest extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {
  self: FlatSpec =>

  override def afterAll {
    SparkContextFactory.stopSparkContext()
  }

  trait WithConfig {

    val wrongConfig = ConfigFactory.empty
    val seconds = 6
    val batchDuraction = Duration(seconds)
    val specificConfig = Map("spark.driver.allowMultipleContexts" -> "true",
    "spark.master" -> "local[*]",
    "spark.app.name" -> "SPARTA")
  }

  "SparkContextFactorySpec" should "fails when properties is missing" in new WithConfig {
    an[Exception] should be thrownBy SparkContextFactory.getOrCreateClusterSparkContext(
      Map.empty[String, String], Seq())
  }

  it should "create and reuse same context" in new WithConfig {
    val sc = SparkContextFactory.getOrCreateClusterSparkContext(specificConfig, Seq())
    val otherSc = SparkContextFactory.getOrCreateClusterSparkContext(specificConfig, Seq())
    sc should be equals (otherSc)
    SparkContextFactory.stopSparkContext()
  }

  it should "create and reuse same XDSession" in new WithConfig {
    val sc = SparkContextFactory.getOrCreateClusterSparkContext(specificConfig, Seq())
    val xdSession = SparkContextFactory.getOrCreateXDSession()
    xdSession shouldNot be equals (null)
    val otherSqc = SparkContextFactory.getOrCreateXDSession()
    xdSession should be equals (otherSqc)
    SparkContextFactory.stopSparkContext()
  }

  it should "create and reuse same SparkStreamingContext" in new WithConfig {
    val checkpointDir = Some("checkpoint/SparkContextFactorySpec")
    val sc = SparkContextFactory.getOrCreateClusterSparkContext(specificConfig, Seq())
    val ssc = SparkContextFactory.getOrCreateStreamingContext(batchDuraction, checkpointDir, None)
    ssc shouldNot be equals (None)
    val otherSsc = SparkContextFactory.getOrCreateStreamingContext(batchDuraction, checkpointDir, None)
    ssc should be equals (otherSsc)
  }
}
