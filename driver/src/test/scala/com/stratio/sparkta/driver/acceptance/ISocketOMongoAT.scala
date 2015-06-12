package com.stratio.sparkta.driver.acceptance

import java.io.{OutputStream, File, PrintStream}
import java.net.{InetAddress, InetSocketAddress, Socket}
import java.nio.channels.ServerSocketChannel

import akka.event.slf4j.SLF4JLogging
import akka.util.Timeout
import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import com.mongodb.casbah.MongoClientURI
import com.stratio.sparkta.driver.constants.AppConstant
import com.stratio.sparkta.driver.helpers.sparkta.{MockSystem, SparktaHelper}
import com.typesafe.config.Config
import org.apache.curator.test.TestingServer
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}
import spray.client.pipelining._
import spray.http.StatusCodes._
import spray.http._
import spray.testkit.ScalatestRouteTest

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source

class ISocketOMongoAT extends WordSpecLike
with ScalatestRouteTest
with SLF4JLogging with BeforeAndAfter with Matchers with MongoEmbedDatabase {
  val TestServerZKPort = 6666
  val TestMongoPort = 60000
  val PathToPolicy = getClass.getClassLoader.getResource("policies/ISocket-OMongo.json").getPath
  val PathToCsv = getClass.getClassLoader.getResource("fixtures/ISocket-OMongo.csv").getPath
  var zkTestServer: TestingServer = _
  var serverSocket: ServerSocketChannel = _
  var SparktaPort:Int=_
  var mongoProps: MongodProps = _
  val Localhost: String = "127.0.0.1"
  var out:PrintStream =_

  before {
    zkTestServer = new TestingServer(TestServerZKPort)
    zkTestServer.start()
    serverSocket = ServerSocketChannel.open()
    serverSocket.socket.bind(new InetSocketAddress(Localhost, 10666))


    mongoProps = mongoStart(TestMongoPort)
  }

  after {
    serverSocket.close()
    zkTestServer.stop()
    mongoStop(mongoProps)
  }

  def sendDataToSparkta = {

    val start = System.currentTimeMillis()

    val source = Source.fromFile(PathToCsv).getLines()
    out = new PrintStream(serverSocket.socket().accept().getOutputStream())
    while(System.currentTimeMillis() - start < 120000) {
      for (x <- 0 to 3) {
        val currentLine = source.next()
        log.info(s"> Current line from CSV: $currentLine")
        out.println(currentLine)


      }
      out.flush()

      Thread.sleep(30000)
   }
  }

  "Sparkta should" should {

    "start in 3 seconds on 9090" in {
      startMongodb
      startSparkta
      sendPolicy
      sendDataToSparkta

      Thread.sleep(120000)
      checkMongoData
    }

    def checkMongoData(): Unit = {
      val mongoClientURI = MongoClientURI(s"mongodb://$Localhost:$TestMongoPort/csvtest.product")
      mongoClientURI.collection should be(Some("product"))
    }

    def startMongodb: Unit = {
      val mongoClientURI = MongoClientURI(s"mongodb://$Localhost:$TestMongoPort/local")
      mongoClientURI.database should be(Some("local"))
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
      SparktaPort=sparktaPort
    }
    def sendPolicy(): Unit = {
      var policy = Source.fromFile(new File(PathToPolicy)).mkString // execution context for futures
      val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
      val promise: Future[HttpResponse] = pipeline(Post(s"http://$Localhost:$SparktaPort/policies",
        HttpEntity(ContentType(MediaTypes.`application/json`, HttpCharsets.`UTF-8`), policy)))

      val response: HttpResponse = Await.result(promise, Timeout(5.seconds).duration)

      response.status should be(OK)
      Thread.sleep(25000)
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
