/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.workflow.lineage


import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.utils.JdbcSlickHelper
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}
import com.typesafe.config.{Config, ConfigFactory}



@RunWith(classOf[JUnitRunner])
class JdbcLineageTest extends FlatSpec with Matchers {

  object testSlickUtils extends JdbcSlickHelper

  "If the URL includes a user if TLS is disabled" should "be used by connectionURL" in new JdbcLineage {

    override val lineageResource: String = ""
    override val lineageUri: String = "jdbc:postgresql://poolpostgrestls.marathon.mesos:5432/strange_database?user=sparta-strange-name&password=stratio&prepareThreshold=0"
    override val tlsEnable: Boolean = false

    val actualURL = testSlickUtils.slickConnectionProperties(config).getProperty("url")
    actualURL shouldBe lineageUri
  }

  "If the URL includes a user but TLS is enabled" should "be overwritten by sparta instance config" in new JdbcLineage {

    override val lineageResource: String = ""
    override val lineageUri: String = "jdbc:postgresql://poolpostgrestls.marathon.mesos:5432/strange_database?user=sparta-strange-name&strangeoption=strangevalue&prepareThreshold=0"
    override val tlsEnable: Boolean = true

    val configString =
      """
        |host = "localhost:5432"
        |database = "postgres"
        |extraParams = "prepareThreshold=0&leakDetectionThreshold=10000&connectTimeout=20"
        |schemaName = "spartatest"
        |user = "sparta-instance"
        |driver = org.postgresql.Driver
        |connectionPool = "HikariCP"
        |initializationFailFast = true
        |poolName = "spartaPostgresPool"
        |keepAliveConnection = true
        |executionContext.parallelism = 4
        |sslenabled = false
        |sslcert = "/tmp/mycert"
        |sslkey = "/tmp/mykey"
        |sslrootcert = "/tmp/myRootCert"
        |numThreads = 2
        |queueSize = 3000
        |leakDetectionThreshold = 10000
        |maxConnections = 2
        |minConnections = 2
        |connectionTimeout = 20000
        |validationTimeout = 3000
      """.stripMargin

    override lazy val instancePostgresConfig = ConfigFactory.parseString(configString)

    val actualURL = testSlickUtils.slickConnectionProperties(config).getProperty("url")

    val expectedURL= "jdbc:postgresql://poolpostgrestls.marathon.mesos:5432/strange_database?user=sparta-instance&ssl=true&sslmode=verify-full&sslcert=/tmp/mycert&sslkey=/tmp/mykey&sslrootcert=/tmp/myRootCert&strangeoption=strangevalue&prepareThreshold=0"

    actualURL shouldBe expectedURL
  }

}