/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.testat.outputs

//import org.apache.solr.SolrTestCaseJ4
//import org.apache.solr.client.solrj.embedded.{EmbeddedSolrServer, JettySolrRunner}
//import org.apache.solr.common.SolrInputDocument
//import org.apache.solr.core.{SolrConfig, CoreContainer}
//import org.apache.solr.util.TestHarness
//import org.apache.solr.cloud.MiniSolrCloudCluster
//import org.apache.solr.client.solrj.impl.CloudSolrClient

import com.stratio.sparkta.testat.SparktaATSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * Acceptance test:
 * [Input]: Socket.
 * [Output]: Solr.
 * [Operators]: accumulator, avg, count, firsValue, fullText, lastValue, max,
 * median, min, range, stddev, sum, variance.
 */
@RunWith(classOf[JUnitRunner])
class ISocketOSolrOperatorsIT extends SparktaATSuite {

  override val PolicyEndSleep = 60000
  val NumExecutors = 4

  override val policyFile = "policies/ISocket-OSolr-operators.json"
  override val PathToCsv = getClass.getClassLoader.getResource("fixtures/at-data-operators.csv").getPath

  val NumEventsExpected: Int = 8


//  val SolrTempPath = "/temp/test-at/solr"
//  val solrXml = new File(ClassLoader.getSystemResource(s"./solr/solr.xml").toURI)
//  val solrHomeDir = solrXml.getParentFile()
//
//  val port = 8984
//  val context = "/solr"
//  // use org.apache.solr.client.solrj.embedded.JettySolrRunner
//  val jettySolr = new JettySolrRunner(solrHomeDir.getAbsolutePath(), context, port)
//
//  jettySolr.start
//
//  jettySolr.stop()

//  val solrSchema = new File(ClassLoader.getSystemResource(s"./solr/schema.xml").toURI)
//  System.setProperty("solr.directoryFactory", "solr.RAMDirectoryFactory")
//  val dataDir =
//    new java.io.File(System.getProperty("java.io.tmpdir"), getClass().getName() + "-" + System.currentTimeMillis())
//  dataDir.mkdirs()
//  //System.setProperty("solr.solr.home", SolrTestCaseJ4.TEST_HOME())
//  val solrConfig =
//    TestHarness.createConfig("/solr", ClassLoader.getSystemResource(s"./solr/solrconfig.xml").getFile)
//  val h: TestHarness = new TestHarness(dataDir.getAbsolutePath(), solrConfig, "schema.xml")
//  val lrf = h.getRequestFactory("standard", 0, 20, "version", "2.2")
//  var solr = new EmbeddedSolrServer(h.getCoreContainer(), h.getCore().getName())
//
//  val document = new SolrInputDocument()
//  document.addField("id", "hola")
//  solr.add(document)
//
//  val asd = solr.getById("hola")

//
//  val solrXml = new File("src/test/resources/solr.xml")
//  val targetDir = new File("target")
//  val extraServlets : util.SortedMap[ServletHolder,String] = new util.TreeMap[ServletHolder,String]
//  val solrSchemaRestApi = new ServletHolder("SolrSchemaRestApi", new ServerServlet)
//  solrSchemaRestApi.setInitParameter("org.restlet.application", "org.apache.solr.rest.SolrSchemaRestApi")
//  extraServlets.put(solrSchemaRestApi, "/schema/*")
//
//  val cluster = new MiniSolrCloudCluster(1, null, targetDir, solrXml, extraServlets, null, null);
//
//  val cloudSolrServer = new CloudSolrClient(cluster.getZkServer().getZkAddress(), true)
//  cloudSolrServer.connect()

  "Sparkta" should {
    "starts and executes a policy that reads from a socket and writes in Solr" in {
      sparktaRunner
      checkData
    }

    def checkData: Unit = {
//      val productA = getData("producta")
//      productA("acc_price") should be(
//        Seq("10", "500", "1000", "500", "1000", "500", "1002", "600"))
//      productA("avg_price") should be(639.0d)
//      productA("sum_price") should be(5112.0d)
//      productA("count_price") should be(NumEventsExpected)
//      productA("first_price") should be("10")
//      productA("last_price") should be("600")
//      productA("max_price") should be(1002.0d)
//      productA("min_price") should be(10.0d)
//      productA("fulltext_price") should be("10 500 1000 500 1000 500 1002 600")
//      productA("stddev_price") should be(347.9605889013459d)
//      productA("variance_price") should be(121076.57142857143d)
//      productA("range_price") should be(992.0d)
//      val productB = getData("productb")
//      productB("acc_price") should be(
//        Seq("15", "1000", "1000", "1000", "1000", "1000", "1001", "50"))
//      productB("avg_price") should be(758.25d)
//      productB("sum_price") should be(6066.0d)
//      productB("count_price") should be(NumEventsExpected)
//      productB("first_price") should be("15")
//      productB("last_price") should be("50")
//      productB("max_price") should be(1001.0d)
//      productB("min_price") should be(15.0d)
//      productB("fulltext_price") should be("15 1000 1000 1000 1000 1000 1001 50")
//      productB("stddev_price") should be(448.04041590655d)
//      productB("variance_price") should be(200740.2142857143d)
//      productB("range_price") should be(986.0d)
    }

    def getData(productName: String): Map[String, Any] = {
      Map()
    }
  }


  override def extraBefore: Unit = {}

  override def extraAfter: Unit = {}
}
