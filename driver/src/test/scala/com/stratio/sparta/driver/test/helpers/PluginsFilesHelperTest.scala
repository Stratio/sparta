/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparta.driver.test.helpers

import java.io._
import java.net._
import org.apache.hadoop.fs.FileSystem
import com.stratio.sparta.driver.helpers.PluginFilesHelper
import com.stratio.sparta.serving.core.services.HdfsService
import org.apache.hadoop.fs.{FileUtil, Path}
import org.apache.hadoop.hdfs.{HdfsConfiguration, MiniDFSCluster}
import org.apache.hadoop.security.UserGroupInformation
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.postgresql.util.ReaderInputStream
import org.scalatest.{BeforeAndAfterAll, FlatSpec, ShouldMatchers}
import org.mockito.BDDMockito._
import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class PluginsFilesHelperTest extends FlatSpec with BeforeAndAfterAll with ShouldMatchers with MockitoSugar{

  var hdfsCluster: MiniDFSCluster = _
  var fileSystem: FileSystem = _
  var hdfsURI: String = _
  var baseDir: File = _
  var httpUrlStreamHandler: HttpUrlStreamHandler = _
  var urlConnection : URLConnection  = _

  /**
    * To run our tests we must create a fake HDFS and to mock HTTP connections
    * in order to check respectively PluginsFilesHelper.downloadFromHdfs and
    * PluginsFilesHelper.downloadFromHttp
    */
  override protected def beforeAll(): Unit = {
    // Building of a fake HDFS cluster
    baseDir = new File(".testDir").getAbsoluteFile
    FileUtil.fullyDelete(baseDir)
    val conf = new HdfsConfiguration(true)
    conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, baseDir.getAbsolutePath)
    val builder: MiniDFSCluster.Builder = new MiniDFSCluster.Builder(conf)
    hdfsCluster = builder.build()
    fileSystem = hdfsCluster.getFileSystem
    hdfsURI = s"hdfs://${hdfsCluster.getNameNode.getHostAndPort}/"

    // Mocking HTTP-related calls
    urlConnection = mock[URLConnection]
    val urlStreamHandlerFactory: URLStreamHandlerFactory  = mock[URLStreamHandlerFactory]
    URL.setURLStreamHandlerFactory(urlStreamHandlerFactory)
    httpUrlStreamHandler = new HttpUrlStreamHandler()
    given(urlStreamHandlerFactory.createURLStreamHandler("http")).willReturn(httpUrlStreamHandler)
    val fakeInput: String = "All work and no play makes Jack a dull boy"
    val reader: StringReader = new StringReader(fakeInput)
    val fakeStream : InputStream = new ReaderInputStream(reader)
    given(urlConnection.getInputStream).willReturn(fakeStream)
  }

  /**
    * After our tests have ended, shutdown the HDFS cluster and clean temporary files associated to it
    */
  override protected def afterAll(): Unit = {
    hdfsCluster.shutdown
    FileUtil.fullyDelete(baseDir)
  }

  private def getMockUrl(filename: String): URL = {
    val url = new URL(s"http://www.fake.com/$filename")
    httpUrlStreamHandler.addConnection(url, urlConnection)
    url
  }

  "The PluginsFilesHelper" should
    "download desired plugins from HDFS into local paths" in {
    val testName = Seq("plugin1","plugin2","plugin3")
    testName.foreach(test => fileSystem.createNewFile(new Path(s"/$test")))
    val fileURI: Seq[String] = testName.map(test => hdfsURI + test)
    val actualResult = new TestPluginFilesHelper(fileSystem, None).downloadPlugins(fileURI, false)
    val expectedResult = Seq("/tmp/sparta/plugins/plugin1","/tmp/sparta/plugins/plugin2","/tmp/sparta/plugins/plugin3")
    actualResult should equal(expectedResult)
  }

  "The PluginsFilesHelper" should
    "return local paths iff plugins are stored locally" in {
    val testName = Seq("file://test/plugin1","file://test/plugin2","file://test/plugin3")
    val actualResult = PluginFilesHelper.downloadPlugins(testName, false)
    actualResult should equal(testName)
  }

  "The PluginsFilesHelper" should
    "download desired plugins from HTTP into local paths" in {
    val testName: Seq[String] = Seq("plugin1","plugin2","plugin3")
    val testURLs = testName.map(getMockUrl(_).toString)
    val actualResult = PluginFilesHelper.downloadPlugins(testURLs, false)
    val expectedResult = Seq("/tmp/sparta/plugins/plugin1","/tmp/sparta/plugins/plugin2","/tmp/sparta/plugins/plugin3")
    actualResult should equal(expectedResult)
  }
}

// The following classes will be used as mocks: most methods have been especially tailored for our tests

class TestPluginFilesHelper(dfs: FileSystem, ugiOption: Option[UserGroupInformation]) extends PluginFilesHelper {
  override protected lazy val hdfsService = new HdfsService(dfs, ugiOption)
}

class HttpUrlStreamHandler extends URLStreamHandler {
  private val connections: mutable.Map[URL, URLConnection] = scala.collection.mutable.Map[URL, URLConnection]()

  override protected def openConnection(url: URL): URLConnection = connections(url)

  def resetConnections(): Unit = connections.clear()

  def addConnection(url: URL, urlConnection: URLConnection) : Option[URLConnection] =
    connections.put(url, urlConnection)
}
