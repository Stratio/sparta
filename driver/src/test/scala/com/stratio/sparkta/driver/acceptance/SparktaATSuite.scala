package com.stratio.sparkta.driver.acceptance

import java.io.{PrintStream, File}
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel

import akka.event.slf4j.SLF4JLogging
import akka.util.Timeout
import com.stratio.sparkta.driver.constants.AppConstant
import com.stratio.sparkta.driver.helpers.sparkta.{MockSystem, SparktaHelper}
import com.typesafe.config.Config
import org.apache.curator.test.TestingServer
import org.scalatest.{Matchers, BeforeAndAfter, WordSpecLike}

import spray.client.pipelining._
import spray.http.StatusCodes._
import spray.http._
import spray.testkit.ScalatestRouteTest

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source

/**
 * Created by arincon on 15/06/15.
 */
trait SparktaATSuite extends WordSpecLike
with ScalatestRouteTest
with SLF4JLogging with BeforeAndAfter with Matchers {
  val Localhost: String = "127.0.0.1"
  var SparktaPort: Int = _
  val TestServerZKPort = 6666
  var zkTestServer: TestingServer = _
  var serverSocket: ServerSocketChannel = _
  var out: PrintStream = _
  
  def zookeeperStart: Unit = {
    zkTestServer = new TestingServer(TestServerZKPort)
    zkTestServer.start()
  }
  def socketStart: Unit = {
    serverSocket = ServerSocketChannel.open()
    serverSocket.socket.bind(new InetSocketAddress(Localhost, 10666))
  }
  def startSparkta = {
    val sparktaConfig = SparktaHelper.initConfig(AppConstant.ConfigAppName)
    val configApi: Config = SparktaHelper.initConfig(AppConstant.ConfigApi, Some(sparktaConfig))
    val sparktaHome = SparktaHelper.initSparktaHome(new MockSystem(Map("SPARKTA_HOME" -> getSparktaHome), Map()))
    val jars = SparktaHelper.initJars(AppConstant.JarPaths, sparktaHome)
    val sparktaPort = configApi.getInt("port")

    SparktaHelper.initAkkaSystem(sparktaConfig, configApi, jars, AppConstant.ConfigAppName)

    Thread.sleep(3000)
    checkPort(sparktaPort) should be(true)
    SparktaPort = sparktaPort
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

    if (fileForIde.exists()) {
      new File(".").getCanonicalPath
    } else {
      new File("../.").getCanonicalPath
    }
  }

  def sendPolicy(path : String): Unit = {
    val policy = Source.fromFile(new File(path)).mkString // execution context for futures
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    val promise: Future[HttpResponse] = pipeline(Post(s"http://$Localhost:$SparktaPort/policies",
      HttpEntity(ContentType(MediaTypes.`application/json`, HttpCharsets.`UTF-8`), policy)))

    val response: HttpResponse = Await.result(promise, Timeout(5.seconds).duration)

    response.status should be(OK)
    Thread.sleep(25000)
  }
  def sendDataToSparkta(path :String) = {
    val start = System.currentTimeMillis()
    val source = Source.fromFile(path).getLines()
    out = new PrintStream(serverSocket.socket().accept().getOutputStream())
    for (x <- 0 to 15) {
      val currentLine = source.next()
      log.info(s"> Current line from CSV: $currentLine")
      out.println(currentLine)
    }
    out.flush()
  }

}
