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
package com.stratio.sparta.serving.core.helpers

import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel

import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import com.stratio.sparta.serving.core.SpartaConfig

@RunWith(classOf[JUnitRunner])
class ResourceManagerLinkIT extends FlatSpec with
  ShouldMatchers with Matchers with BeforeAndAfter {

  var serverSocket: ServerSocketChannel = _

  after {
    serverSocket.close()
  }

  it should "return local Spark UI link" in {
    serverSocket = ServerSocketChannel.open()
    val localhostName = java.net.InetAddress.getLocalHost().getHostName()
    serverSocket.socket.bind(new InetSocketAddress(localhostName, 4040))
    val config = ConfigFactory.parseString(
      """
        |sparta{
        |  config {
        |    executionMode = local
        |  }
        |}
      """.stripMargin)
    SpartaConfig.initMainConfig(Option(config))

    ResourceManagerLink.getLink should be(Some(s"http://${localhostName}:4040"))
  }

  it should "return Mesos UI link" in {
    serverSocket = ServerSocketChannel.open()
    serverSocket.socket.bind(new InetSocketAddress("127.0.0.1",5050))
    val config = ConfigFactory.parseString(
      """
        |sparta{
        |  config {
        |    executionMode = mesos
        |  }
        |
        |  mesos {
        |    master = "mesos://127.0.0.1:7077"
        |  }
        |}
      """.stripMargin)
    SpartaConfig.initMainConfig(Option(config))

    ResourceManagerLink.getLink should be(Some("http://127.0.0.1:5050"))
  }

  it should "return YARN UI link" in {
    serverSocket = ServerSocketChannel.open()
    serverSocket.socket.bind(new InetSocketAddress("localhost",8088))
    val config = ConfigFactory.parseString(
      """
        |sparta{
        |  config {
        |    executionMode = yarn
        |  }
        |
        |  hdfs {
        |    hadoopUserName = stratio
        |    hdfsMaster = localhost
        |    hdfsPort = 8020
        |    pluginsFolder = "plugins"
        |    executionJarFolder = "driver"
        |    classpathFolder = "classpath"
        |  }
        |
        |  yarn {
        |    master = yarn-cluster
        |  }
        |}
      """.stripMargin)
    SpartaConfig.initMainConfig(Option(config))

    ResourceManagerLink.getLink should be(Some("http://localhost:8088"))
  }

  it should "return Spark Standalone UI link" in {
    serverSocket = ServerSocketChannel.open()
    serverSocket.socket.bind(new InetSocketAddress("localhost",8080))
    val config = ConfigFactory.parseString(
      """
        |sparta{
        |  config {
        |    executionMode = standalone
        |  }
        |
        |  standalone {
        |    master = "spark://localhost:4040"
        |  }
        |}
      """.stripMargin)
    SpartaConfig.initMainConfig(Option(config))

    ResourceManagerLink.getLink should be(Some("http://localhost:8080"))
  }

}
