/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.common.rest


import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.stratio.sparta.core.helpers.SSLHelper
import com.typesafe.config.{Config, ConfigFactory}
import javax.net.ssl.SSLContext
import org.apache.http.ssl.SSLContextBuilder
import org.apache.spark.sql.Row

import scala.collection.JavaConversions._
import scala.collection.mutable.{Map => MutableMap}
import scala.util.Try

object SparkExecutorRestUtils extends SLF4JLogging {

  private[this] var actorSystem: Option[SparkExecutorRestUtils] = None

  private[rest] def configFromProperties(properties: Map[String, String]): Config =
    ConfigFactory.parseMap(properties).withFallback(ConfigFactory.load())


  def getOrCreate(akkaProps: Map[String, String] = Map.empty, props: Map[String,String]): SparkExecutorRestUtils = synchronized {
    actorSystem = actorSystem.orElse {
      Some(new SparkExecutorRestUtils(ActorSystem("spark-system", configFromProperties(akkaProps)), props))
    }
    actorSystem.get
  }

  object TemporalSSLContextUtils {

    val SparkSslDatastorePrefix = "spark.ssl.datastore."

    def httpsConnContext(props: Map[String,String]): SSLContext = {
      log.debug("Trying to create SSLContext with spark.ssl.datastore.* variables")
      val keyStoreAndPassword = (props("spark.ssl.datastore.keyStore"), props("spark.ssl.datastore.keyStorePassword"))
      val trustStoreAndPassword = (props("spark.ssl.datastore.trustStore"), props("spark.ssl.datastore.trustStorePassword"))
      SSLHelper.getSSLContextV2(keyStoreAndPassword, trustStoreAndPassword)
    }
  }

  class SparkExecutorRestUtils(private val actorSystem: ActorSystem, props: Map[String,String]) extends SLF4JLogging {

    private val pools: MutableMap[String, Flow[(HttpRequest, Row), (Try[HttpResponse], Row), HostConnectionPool]] = MutableMap.empty

    object Implicits {
      implicit val aSystem: ActorSystem = actorSystem
      implicit val aMaterializer: ActorMaterializer = ActorMaterializer() // TODO create instances
    }

    import Implicits._

    private val http = Http()

    private lazy val httpsConnContext: HttpsConnectionContext =
      Try(SSLHelper.getSSLContextV2(withHttps = true))
        .recover { case ex => // This is done due to debug mode
          log.debug(s"Impossible to create getSSLContextV2 with sparta.ssl.* variables: ${ex.getMessage}")
          TemporalSSLContextUtils.httpsConnContext(props)
        }.map(x => ConnectionContext.https(x)
      ).getOrElse {
        log.debug("Cannot retrieve CAs. Using default SSL context without keystore or truestores")
        ConnectionContext.https(new SSLContextBuilder().useProtocol("TLSv1.2").build())
      }

    def getOrCreatePool(
                         host: String,
                         port: Int,
                         isHttps: Boolean = false,
                         settings: Map[String, String] = Map.empty
                       ): Flow[(HttpRequest, Row), (Try[HttpResponse], Row), HostConnectionPool] = synchronized {

      def buildKey(h: String, p: Int, https: Boolean): String = s"$h$p$https" // TODO use settings?

      pools
        .getOrElseUpdate(
          buildKey(host, port, isHttps),
          if (isHttps)
            http.cachedHostConnectionPoolHttps(host, port, connectionContext = httpsConnContext)
          else http.cachedHostConnectionPool(host, port)
        )
    }

    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      def run() {
        http.shutdownAllConnectionPools()
        import scala.concurrent.ExecutionContext.Implicits.global
        aSystem.whenTerminated onFailure {
          case _ => aSystem.terminate
        }
      }
    }))
  }
}