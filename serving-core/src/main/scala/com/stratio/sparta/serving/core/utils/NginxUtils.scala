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

package com.stratio.sparta.serving.core.utils

import java.io.{File, FileOutputStream, PrintWriter}
import java.nio.file.{Files, StandardCopyOption}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.stratio.tikitakka.common.util.HttpRequestUtils
import org.apache.commons.io.FileUtils

import scala.sys.process._
import scala.util.{Failure, Properties, Success, Try}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap.option2NotBlankOption
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.ResourceManagerLinkHelper
import com.stratio.sparta.serving.core.marathon.OauthTokenUtils
import com.stratio.sparta.serving.core.utils.NginxUtils._
import net.minidev.json.JSONArray

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object NginxUtils {

  import com.jayway.jsonpath.{Configuration, JsonPath, ReadContext}

  def buildSparkUI(id: String, lastExecutionMode: Option[String]): Option[String] = {
    lastExecutionMode match {
      case Some(AppConstant.ConfigLocal) =>
        ResourceManagerLinkHelper.getLink(AppConstant.ConfigLocal)
      case _ =>
        val url = for {
          monitorVhost <- Properties.envOrNone("MARATHON_APP_LABEL_HAPROXY_1_VHOST")
          serviceName <- Properties.envOrNone("MARATHON_APP_LABEL_DCOS_SERVICE_NAME")
        } yield {
          val useSsl = Properties.envOrNone("SECURITY_TLS_ENABLE") flatMap { strVal =>
            Try(strVal.toBoolean).toOption
          } getOrElse false
          monitorUrl(monitorVhost, serviceName, id, useSsl)
        }
        url.orElse(None)
    }
  }

  def monitorUrl(vhost: String, spartaInstance: String, workflowId: String, ssl: Boolean = true): String =
    s"http${if(ssl) "s" else ""}://$vhost/workflows-$spartaInstance/$workflowId/"

  case class AppParameters(appId: String, addressIP: String, port: Int)

  private class JsonPathExtractor(jsonDoc: String, isLeafToNull: Boolean) {

    val conf = {
      if (isLeafToNull)
        Configuration.defaultConfiguration().addOptions(com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL)
      else Configuration.defaultConfiguration()
    }
    private val ctx: ReadContext = JsonPath.using(conf).parse(jsonDoc)
    def query(query: String): Any = ctx.read(query)
  }

  case class NginxMetaConfig(
                              configFile: File,
                              pidFile: File,
                              instanceName: String,
                              workflowsUiVhost: String,
                              workflowsUiPort: Int,
                              useSsl: Boolean
                            )

  object NginxMetaConfig {
    def apply(configPath: String = "/etc/nginx/nginx.conf",
              pidFilePath: String = "/run/nginx.pid",
              instanceName: String = Properties.envOrElse("MARATHON_APP_LABEL_DCOS_SERVICE_NAME", "sparta"),
              workflowsUiVhost: String = Properties.envOrElse("MARATHON_APP_LABEL_HAPROXY_1_VHOST", "sparta-server"),
              workflowsUiPort: Int = Properties.envOrElse("PORT_SPARKAPI", "4040").toInt,
              useSsl: Boolean = Properties.envOrNone("SECURITY_TLS_ENABLE") flatMap { strVal =>
                Try(strVal.toBoolean).toOption
              } getOrElse false
             ): NginxMetaConfig = {
      implicit def path2file(path: String): File = new File(path)
      new NginxMetaConfig(configPath, pidFilePath, instanceName, workflowsUiVhost, workflowsUiPort, useSsl)
    }


  }

  abstract class Error protected (description: String) extends RuntimeException {
    override def getMessage: String = s"Nginx service problem: $description"
  }

  object Error {

    case class InvalidConfig(config: Option[String]) extends Error("Invalid configuration detected")
    object CouldNotStart extends Error("Couldn't start service")
    object CouldNotStop extends Error("Couldn't stop service")
    object CouldNotReload extends Error("Couldn't reload configuration")
    case class CouldNotWriteConfig(
                                    file: File,
                                    explanation: Option[Exception] = None) extends Error(
      s"Couldn't overwrite configuration file ($file)" + explanation.map(exp => s": $exp").getOrElse("")
    )
    case class CouldNotResolve(serviceName: String) extends Error(s"Couldn't retrieve $serviceName IP address")
    case class NoServiceStatus(explanation: Option[Exception] = None) extends Error(
      "Cannot retrieve workflows status" + explanation.map(exp => s": $exp").getOrElse("")
    )
    object AlreadyRunning extends Error("Can't start Nginx as it is currently running")
    object NotRunning extends Error("Can't stop Nginx as it is currently stopped")

  }


}

class NginxUtils(system: ActorSystem, materializer: ActorMaterializer, nginxMetaConfig: NginxMetaConfig)
  extends OauthTokenUtils {
  outer =>

  import nginxMetaConfig._
  import Error._

  private val oauthUtils = new HttpRequestUtils {
    override implicit val system: ActorSystem = outer.system
    override implicit val actorMaterializer: ActorMaterializer = outer.materializer
  }

  import oauthUtils._

  private val marathonApiUri = Properties.envOrNone("MARATHON_TIKI_TAKKA_MARATHON_URI").notBlank

  private def checkSyntax(file: File): Boolean = {
    val test_exit_code = Process(s"nginx -t -c ${file.getAbsolutePath}").!
    test_exit_code == 0
  }

  def startNginx(): Future[Unit] = Future {
    val maybeProblem =
      if(isNginxRunning) Some(AlreadyRunning)
      else if(Process("nginx").! != 0) Some(CouldNotStart)
      else None

    maybeProblem foreach { problem =>
      log.error(problem.getMessage)
      throw problem
    }

    log.debug("Nginx started correctly")
  }

  def stopNginx(): Future[Unit] = Future {
    val maybeProblem = if(isNginxRunning) {
      Process("nginx -s stop").!
      Thread.sleep(500)
      Some(CouldNotStop).filter(_ => isNginxRunning)
    } else None

    maybeProblem foreach { problem =>
      log.error(problem.getMessage)
      throw problem
    }

    log.debug("Nginx stopped correctly")
  }

  def reloadNginxConfig(): Future[Unit] = Future {
    val maybeProblem = Some(CouldNotReload).filter(_ => Process("nginx -s reload").! != 0)

    maybeProblem foreach { problem =>
      log.error(problem.getMessage)
      throw problem
    }

    log.debug("Nginx config reloaded")
  }

  def isNginxRunning: Boolean =
    pidFile.exists && using(scala.io.Source.fromFile(pidFile))(_.nonEmpty)

  def reloadNginx(): Future[Unit] =
    if(isNginxRunning) reloadNginxConfig()
    else startNginx()

  private def resolveHostnameMesosDNSToIP(mesosDNSservice: String): Future[String] = Future {
    java.net.InetAddress.getByName(mesosDNSservice).getHostAddress
  } recoverWith {
    case _: Exception =>
      val problem = CouldNotResolve(mesosDNSservice)
      log.error(problem.getMessage)
      Future.failed(problem)
  }

  //scalastyle:off
  def using[A <: {def close() : Unit}, B](resource: A)(f: A => B): B =
    try {
      f(resource)
    } finally {
      resource.close()
    }

  /**
    * Updates Nginx configuration file with the list of workflow processes
    *
    * @param listWorkflows
    * @param uiVirtualHost
    * @return `true` iff the update brings changes to the config file
    */
  def modifyConf(listWorkflows: Seq[AppParameters], uiVirtualHost: String): Future[Boolean] =
    for {
      tempFile <- Future(File.createTempFile(configFile.getName, null))
      res <- Future {

        val config = updatedNginxConf(listWorkflows, uiVirtualHost)

        // Detect config changes
        val diff = !configFile.exists || using(scala.io.Source.fromFile(configFile)) { source =>
          val fileLines = source.getLines().toSeq collect {
            case line if line.nonEmpty => line.trim
          }
          val generatedLines = config.split("\n").toSeq collect {
            case line if line.nonEmpty => line.trim
          }
          fileLines != generatedLines
        }

        // Avoid re-writing files when there are no changes
        if(diff) {
          using(new PrintWriter(tempFile)) {
            _.write(config)
          }

          if (!checkSyntax(tempFile))
            throw InvalidConfig(Some(config))

          Files.move(
            tempFile.toPath,
            configFile.toPath,
            StandardCopyOption.REPLACE_EXISTING
          ) : Unit
        }

        diff
      } recoverWith {
        case e: Exception =>
          Try(Files.delete(tempFile.toPath))
          val problem = CouldNotWriteConfig(configFile, Some(e))
          log.error(problem.getMessage)
          Future.failed(problem)
      }
    } yield res

  //scalastyle:on

  def updateNginx(): Future[Boolean] = {
    for {
      calicoAddresses <- retrieveIPandPorts
      res <- modifyConf(calicoAddresses, workflowsUiVhost)
    } yield {
      log.info(s"Nginx configuration correctly updated")
      res
    }
  } recoverWith { case e: Exception =>
    val problem = NoServiceStatus(Some(e))
    log.error(problem.getMessage)
    Future.failed(problem)
  }


  def updatedNginxConf(implicit listWorkflows: Seq[AppParameters], uiVirtualHost: String): String =
    s"""
       |events {
       |  worker_connections 4096;
       |}
       |
       |http {
       |
       |  server {
       |
       |    listen $workflowsUiPort${if(useSsl) " ssl" else ""};
       |    server_name $uiVirtualHost;
       |    ${if(useSsl) "ssl_certificate /tmp/nginx_cert.crt;" else ""}
       |    ${if(useSsl) s"ssl_certificate_key /tmp/$instanceName.key;" else ""}
       |    access_log /dev/stdout combined;
       |    error_log stderr info;
       |
       |    $workflowNginxLocations
       |
       |  }
       |
       |}
     """.stripMargin

  def workflowNginxLocations(implicit listWorkflows: Seq[AppParameters], uiVirtualHost: String): String =
    listWorkflows map { case AppParameters(id, ip, port) =>

      val workflowName = id.split('/').last
      val monitorEndUrl = monitorUrl(uiVirtualHost, instanceName, workflowName ,useSsl)

      s"""
         |
         |    location /workflows-$instanceName/$workflowName/ {
         |      proxy_pass        http://$ip:$port/;
         |      proxy_redirect    http://$uiVirtualHost/ $monitorEndUrl;
         |      proxy_set_header  Host             $$host;
         |      proxy_set_header  X-Real-IP        $$remote_addr;
         |      proxy_set_header  X-Forwarded-For  $$proxy_add_x_forwarded_for;
         |    }
         |
       """.stripMargin
    } mkString

  def retrieveIPandPorts: Future[Seq[AppParameters]] = {
    val groupPath = s"v2/groups/sparta/$instanceName/workflows"
    for {
      group <- doRequest[String](marathonApiUri.get, groupPath, cookies = Seq(getToken))
      appsStrings: Seq[String] <- Future.sequence {
        extractAppsId(group).get map { appId =>
          doRequest[String](marathonApiUri.get, s"v2/apps/$appId", cookies = Seq(getToken))
        }
      }
    } yield appsStrings flatMap extractAppParameters
  }

  private def queryJson[T](json: String)(query: => String): Option[T] =
    Try(new JsonPathExtractor(json, false).query(query).asInstanceOf[T]).toOption

  def extractAppsId(json: String): Option[Seq[String]] =
    queryJson[JSONArray](json)("$.apps.[*].id").map(_.toArray.map(_.toString))

  def extractAppParameters(json: String): Option[AppParameters] = {
    val queryId = "$.app.id"
    val queryIpAddress = "$.app.tasks.[0].ipAddresses.[0].ipAddress"
    val queryPort= "$.app.tasks.[0].ports.[0]"

    Try {
      val extractor = new JsonPathExtractor(json, false)
      import extractor.query
      val id = query(queryId).asInstanceOf[String]
      val ip = query(queryIpAddress).asInstanceOf[String]
      val port = query(queryPort).asInstanceOf[Int]
      AppParameters(id, ip, port)
    } toOption
  }
}