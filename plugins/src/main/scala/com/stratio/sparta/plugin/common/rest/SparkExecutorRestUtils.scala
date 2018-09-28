/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.common.rest


import java.io.File
import java.nio.file.Paths
import javax.net.ssl.SSLContext

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpsConnectionContext
import akka.http.scaladsl.{ConnectionContext, Http}
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.stratio.sparta.core.helpers.SSLHelper
import com.stratio.sparta.plugin.common.rest.SparkExecutorRestUtils.TemporalSSLContextUtils
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.http.ssl.SSLContextBuilder
import org.apache.spark.security.{ConfigSecurity, HTTPHelper}
import org.apache.spark.sql.Row

import scala.collection.mutable.{Map => MutableMap}
import scala.collection.JavaConversions._
import scala.util.{Failure, Try}

object SparkExecutorRestUtils {

  private[this] var actorSystem: Option[SparkExecutorRestUtils] = None

  private[rest] def configFromProperties(properties: Map[String,String]): Config =
    ConfigFactory.parseMap(properties).withFallback(ConfigFactory.load())


  def getOrCreate(props: Map[String, String] = Map.empty): SparkExecutorRestUtils = synchronized {
    actorSystem = actorSystem.orElse{
      Some(new SparkExecutorRestUtils(ActorSystem("spark-system", configFromProperties(props))))
    }
    actorSystem.get
  }


  // TODO remove this code when Spark make SSL context available
  object TemporalSSLContextUtils {

    lazy val sslContext: SSLContext = {
      val (caFileName, caPass) = getAllCaAndPassword.get
      //log.debug("Retrieved correctly CAs and passwords from vault")
      generateSecureClient(caFileName, caPass)
    }

    def generateSecureClient(caFileName: String, caPass: String): SSLContext = {
      val caFile = new File(caFileName)
      SSLContextBuilder.create().loadTrustMaterial(caFile, caPass.toCharArray).build()
    }


    private def getCAPass: Try[String] = {

      val requestUrl = s"${ConfigSecurity.vaultURI.get}/v1/ca-trust/passwords/?list=true"

      HTTPHelper.executeGet(
        requestUrl,
        "data",
        Some(Seq(("X-Vault-Token", ConfigSecurity.vaultToken.get)))
      ).flatMap{ listResponse =>

        val passPath = listResponse("keys").asInstanceOf[List[String]].head
        val requestPassUrl = s"${ConfigSecurity.vaultURI.get}/v1/ca-trust/" +
          s"passwords/${passPath.replaceAll("/", "")}/keystore"

        //logDebug(s"Requesting ca Pass from Vault: $requestPassUrl")

        HTTPHelper.executeGet(
          requestPassUrl,
          "data", Some(Seq(("X-Vault-Token", ConfigSecurity.vaultToken.get)))
        ).map { passResponse =>
          passResponse(s"pass").asInstanceOf[String]
        }
      } recoverWith {
        case e: Exception =>
          Failure(new Exception("Error retrieving CA password from Vault", e))
      }
    }

    private def getAllCaAndPassword: Try[(String, String)] =
      (
        for {
          caPass <- getCAPass
        } yield {

          val fileName = "trustStore.jks"
          val dir = new File(s"${ConfigSecurity.secretsFolder}/ca-trust")

          (Paths.get(dir.getAbsolutePath, fileName).toString, caPass)

        }) recoverWith {

        case e: Exception =>
          Failure(new Exception("Error retrieving CAs/passwords from vault", e))
      }
  }

}

class SparkExecutorRestUtils(private val actorSystem: ActorSystem){

  private val pools: MutableMap[String, Flow[(HttpRequest, Row), (Try[HttpResponse], Row), HostConnectionPool]] = MutableMap.empty

  object Implicits {
    implicit val aSystem: ActorSystem = actorSystem
    implicit val aMaterializer: ActorMaterializer = ActorMaterializer() // TODO create instances
  }

  import Implicits._

  private val http = Http()

  private lazy val httpsConnContext: HttpsConnectionContext =
    Try(SSLHelper.getSSLContextV2(withHttps = true)) // This is done due to debug mode}
      .recover { case unloggedException => TemporalSSLContextUtils.sslContext} // TODO log exception
      .map(ConnectionContext.https(_))
      .get

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
        if (isHttps) http.cachedHostConnectionPoolHttps(host, port, connectionContext = httpsConnContext) else http.cachedHostConnectionPool(host, port)
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