/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.elasticsearch

import com.stratio.sparta.core.models.OutputWriterOptions
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import org.junit.runner.RunWith
import org.scalatest.ShouldMatchers
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class ElasticSearchInputStepTest extends TemporalSparkContext
  with ShouldMatchers {

  trait BaseValues {

    final val localPort = 9200
    final val remotePort = 9300
    val input = getInstance("localhost", localPort, remotePort)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
    val outputMultipleNodes = new ElasticSearchInputStepBatch(
      "ES-out", outputOptions, Option(ssc), sparkSession,
      Map("nodes" -> JsoneyString(
        s"""
           |[{
           |  "node":"host-a",
           |  "httpPort":"$localPort",
           |  "tcpPort":"$remotePort"
           |  },
           |  {
           |  "node":"host-b",
           |  "httpPort":"9201",
           |  "tcpPort":"9301"
           |}]
         """.stripMargin)
      ))

    def getInstance(host: String, httpPort: Int, tcpPort: Int): ElasticSearchInputStepBatch =
      new ElasticSearchInputStepBatch(
        "ES-out", outputOptions, Option(ssc), sparkSession,
        Map("nodes" -> JsoneyString(
          s"""
             |[{
             |  "node":"$host",
             |  "httpPort":"$httpPort",
             |  "tcpPort":"$tcpPort"
             |}]
           """.stripMargin)
        ))
  }

  trait NodeValues extends BaseValues {
    val ipOutput = getInstance("127.0.0.1", localPort, remotePort)
    val ipv6Output = getInstance("0:0:0:0:0:0:0:1", localPort, remotePort)
    val remoteOutput = getInstance("dummy", localPort, remotePort)
  }

  "ElasticSearchInputStep" should "format the properties" in new NodeValues {
    assertResult(input.httpNodes)(Seq(("localhost", 9200)))
    assertResult(outputMultipleNodes.httpNodes)(Seq(("host-a", 9200), ("host-b", 9201)))
  }

  it should "return a Seq of  tuples with a (host,port) format" in new NodeValues {
    assertResult(ipOutput.getHostPortConf("nodes", "localhost", "9200", "node", "httpPort"))(Seq(("127.0.0.1", 9200)))
    assertResult(
      ipv6Output.getHostPortConf("nodes", "localhost", "9200", "node", "httpPort"))(Seq(("0:0:0:0:0:0:0:1", 9200)))
    assertResult(outputMultipleNodes.getHostPortConf("nodes", "localhost", "9200", "node", "httpPort")
    )(Seq(("host-a", 9200), ("host-b", 9201)))
  }
}
