package com.stratio.sparkta.driver.acceptance

import java.io.File
import java.net.InetSocketAddress
import java.nio.channels.{ServerSocketChannel, SocketChannel}

import akka.event.slf4j.SLF4JLogging
import akka.util.Timeout
import com.stratio.sparkta.driver.actor.StreamingContextStatusEnum
import com.stratio.sparkta.driver.constants.AppConstant
import com.stratio.sparkta.driver.helpers.sparkta.{MockSystem, SparktaHelper}
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import com.typesafe.config.Config
import org.apache.curator.test.TestingServer
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}
import spray.client.pipelining._
import spray.http.StatusCodes._
import spray.http._
import spray.testkit.ScalatestRouteTest
import spray.json._


import spray.httpx.SprayJsonSupport._


import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source

class ISocketOMongoAT extends WordSpecLike
with ScalatestRouteTest
with SLF4JLogging with BeforeAndAfter with Matchers{
  val TestServerZKPort = 6666
  val PathToPolicy = getClass.getClassLoader.getResource("policies/ISocketOMongo.json").getPath
  var zkTestServer: TestingServer = _
  var serverSocket: ServerSocketChannel = _
  val Localhost: String = "127.0.0.1"
  implicit val json4sJacksonFormats = DefaultFormats +
    new EnumNameSerializer(StreamingContextStatusEnum) +
    new JsoneyStringSerializer()
  before {
    zkTestServer = new TestingServer(TestServerZKPort)
    zkTestServer.start()
    val serverSocket = ServerSocketChannel.open()
    serverSocket.socket.bind(new InetSocketAddress(Localhost, 10666))
  }

  after {
    serverSocket.close()
    zkTestServer.stop()
  }

  "Sparkta should" should {
    "start in 3 seconds on 9090" in {
      val sparktaConfig = SparktaHelper.initConfig(AppConstant.ConfigAppName)
      val configApi: Config = SparktaHelper.initConfig(AppConstant.ConfigApi, Some(sparktaConfig))
      val sparktaHome = SparktaHelper.initSparktaHome(new MockSystem(Map("SPARKTA_HOME" -> getSparktaHome),Map()))
      val jars = SparktaHelper.initJars(AppConstant.JarPaths, sparktaHome)
      val sparktaPort = configApi.getInt("port")

      SparktaHelper.initAkkaSystem(sparktaConfig,configApi, jars, AppConstant.ConfigAppName)

      Thread.sleep(3000)

     // Post("/", HttpEntity("application/json",policy ))





      checkPort(sparktaPort) should be (true)

      var policy = Source.fromFile(new File(PathToPolicy)).mkString // execution context for futures



      val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

      val promise: Future[HttpResponse] = pipeline(Post( s"http://$Localhost:$sparktaPort/policies",
        HttpEntity(ContentType(MediaTypes.`application/json`, HttpCharsets.`UTF-8`),policy)))

      val response: HttpResponse = Await.result(promise, Timeout(5.seconds).duration)

      response.status should be (OK)


      Thread.sleep(60000)
    }
  }

  def checkPort(port: Int) = {
    try {
      
      val socket = new java.net.Socket(Localhost, port)
      socket.close()
      println(port)
      true
    } catch {
      case _: Throwable ⇒ false
    }
  }

  /**
   * This is a workaround to find the jars either in the IDE or in a maven execution.
   * This test should be moved to acceptance tests when available
   */
  def getSparktaHome: String = {
    val fileForIde = new File(".", "plugins")

    if (fileForIde.exists()){
      new File(".").getCanonicalPath
    }else{
      new File("../.").getCanonicalPath
    }
  }
}
