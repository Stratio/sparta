/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.utils

import java.io.{File, PrintWriter}
import java.nio.file.{Files, StandardCopyOption}

import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.stream.ActorMaterializer
import com.stratio.sparta.serving.core.constants.{AppConstant, MarathonConstant, SparkConstant}
import com.stratio.sparta.serving.core.helpers.WorkflowHelper
import com.stratio.sparta.serving.core.utils.MarathonAPIUtils._
import com.stratio.sparta.serving.core.utils.NginxUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.sys.process._
import scala.util.{Properties, Try}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._

object NginxUtils {

  def buildSparkUI(id: String): Option[String] = {
    if (
      Properties.envOrNone(MarathonConstant.NginxMarathonLBHostEnv).notBlank.isDefined &&
        Properties.envOrNone(MarathonConstant.NginxMarathonLBPathEnv).notBlank.isDefined
    ) {
      val useSsl = Properties.envOrNone(MarathonConstant.SpartaTLSEnableEnv) flatMap { strVal =>
          Try(strVal.toBoolean).toOption
      } getOrElse false
      Option(monitorUrl(WorkflowHelper.getVirtualHost, WorkflowHelper.getVirtualPath, id, useSsl))
    } else None
  }

  private def monitorUrl(vhost: String, vpath: String, workflowId: String, ssl: Boolean = true): String = {
    val nameWithoutRoot =
      if (workflowId.startsWith("/")) workflowId.substring(1) else workflowId
    s"http${if (ssl) "s" else ""}://$vhost$vpath/$nameWithoutRoot/"
  }

  case class NginxMetaConfig(
                              configFile: File,
                              pidFile: File,
                              instanceName: String,
                              securityFolder: String,
                              workflowsUiVhost: String,
                              workflowsUiVpath: String,
                              workflowsUiPort: Int,
                              useSsl: Boolean
                            )

  object NginxMetaConfig {
    def apply(configPath: String = "/etc/nginx/nginx.conf",
              pidFilePath: String = "/run/nginx.pid",
              instanceName: String = AppConstant.spartaTenant,
              securityFolder: String = Properties.envOrElse(MarathonConstant.SpartaSecretFolderEnv, "/etc/sds/sparta/security"),
              workflowsUiVhost: String = WorkflowHelper.getVirtualHost,
              workflowsUiVpath: String = WorkflowHelper.getVirtualPath,
              workflowsUiPort: Int = Properties.envOrElse(SparkConstant.SparkUiPortEnv, SparkConstant.DefaultUIPort.toString).toInt,
              useSsl: Boolean = AppConstant.securityTLSEnable
             ): NginxMetaConfig = {
      implicit def path2file(path: String): File = new File(path)

      new NginxMetaConfig(
        configPath,
        pidFilePath,
        instanceName,
        securityFolder,
        workflowsUiVhost,
        workflowsUiVpath,
        workflowsUiPort,
        useSsl
      )
    }


  }

  abstract class NginxError protected(description: String) extends RuntimeException {
    override def getMessage: String = s"Nginx service problem: $description"
  }

  object NginxError {

    case class InvalidConfig(config: Option[String]) extends NginxError("Invalid configuration detected")

    object CouldNotStart extends NginxError("Couldn't start service")

    object CouldNotStop extends NginxError("Couldn't stop service")

    object CouldNotReload extends NginxError("Couldn't reload configuration")

    case class UnExpectedError(ex: Exception) extends NginxError(s"Unexpected error with message: ${ex.toString}")

    case class CouldNotWriteConfig(
                                    file: File,
                                    explanation: Option[Exception] = None) extends NginxError(
      s"Couldn't overwrite configuration file ($file)" + explanation.map(exp => s": $exp").getOrElse("")
    )

    case class CouldNotResolve(serviceName: String) extends NginxError(s"Couldn't retrieve $serviceName IP address")

    case class NoServiceStatus(explanation: Option[Exception] = None) extends NginxError(
      "Cannot retrieve workflows status" + explanation.map(exp => s": $exp").getOrElse("")
    )

    object AlreadyRunning extends NginxError("Can not start Nginx as it is currently running")

    object NotRunning extends NginxError("Can not stop Nginx as it is currently stopped")

  }

}

case class NginxUtils(system: ActorSystem, materializer: ActorMaterializer, nginxMetaConfig: NginxMetaConfig)
  extends MarathonAPIUtils(system, materializer)
    with SLF4JLogging {

  import NginxError._
  import nginxMetaConfig._

  val crossdataLocalDeploymentWithUI =
    Try(Properties.envOrNone(SparkConstant.CrossdataSparkUiEnabled).get.toBoolean).getOrElse(true)
  val crossdataItem = AppParameters(
    appId = "crossdata-sparkUI",
    addressIP = "127.0.0.1",
    port = Try(Properties.envOrNone(SparkConstant.CrossdataSparkUiPort).get.toInt)
      .getOrElse(SparkConstant.DefaultUIPort)
  )

  def startNginx(): Future[Unit] = Future {
    val maybeProblem =
      if (isNginxRunning) Some(AlreadyRunning)
      else if (Process("nginx").! != 0) Some(CouldNotStart)
      else None

    maybeProblem foreach { problem =>
      log.error(problem.getMessage)
      throw problem
    }

    log.debug("Nginx started correctly")
  }

  def stopNginx(): Future[Unit] = Future {
    val maybeProblem = if (isNginxRunning) {
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
    if (isNginxRunning) reloadNginxConfig()
    else startNginx()

  //scalastyle:off

  def updateNginx(): Future[Boolean] = {
    for {
      calicoAddresses <- retrieveIPandPorts
      res <- modifyConf(calicoAddresses, workflowsUiVhost, workflowsUiVpath)
    } yield {
      log.debug(s"Nginx configuration correctly updated")
      res
    }
  } recoverWith { case e: Exception =>
    val problem = NoServiceStatus(Some(e))
    log.error(problem.getMessage)
    Future.failed(problem)
  }

  private[utils] def using[A <: {def close() : Unit}, B](resource: A)(f: A => B): B =
    try {
      f(resource)
    } finally {
      resource.close()
    }

  private def checkSyntax(file: File): Boolean = {
    val test_exit_code = Process(s"nginx -t -c ${file.getAbsolutePath}").!
    test_exit_code == 0
  }

  /**
    * Updates Nginx configuration file with the list of workflow processes
    *
    * @param listWorkflows
    * @param uiVirtualHost
    * @return `true` iff the update brings changes to the config file
    */
  private[utils] def modifyConf(
                                 listWorkflows: Seq[AppParameters],
                                 uiVirtualHost: String,
                                 uiVirtualPath: String
                               ): Future[Boolean] =
    for {
      tempFile <- Future(File.createTempFile(configFile.getName, null))
      res <- Future {
        val fullListOfWorkflows = if (crossdataLocalDeploymentWithUI)
          listWorkflows.+:(crossdataItem)
        else listWorkflows
        val config = updatedNginxConf(fullListOfWorkflows, uiVirtualHost, uiVirtualPath)

        // Detect config changes
        val diff = !configFile.exists || using(scala.io.Source.fromFile(configFile)) { source =>
          val fileLines = source.getLines().toSeq collect {
            case line if line.nonEmpty => line.trim
          }
          val generatedLines = config.split("\n").toSeq collect {
            case line if line.nonEmpty => line.trim
          }

          if (generatedLines.nonEmpty)
            fileLines != generatedLines
          else false
        }

        // Avoid re-writing files when there are no changes
        if (diff) {
          using(new PrintWriter(tempFile)) {
            _.write(config)
          }

          if (!checkSyntax(tempFile))
            throw InvalidConfig(Some(config))

          Files.move(tempFile.toPath, configFile.toPath, StandardCopyOption.REPLACE_EXISTING)
        } else Files.deleteIfExists(tempFile.toPath)

        diff
      } recoverWith {
        case e: Exception =>
          Try(Files.deleteIfExists(tempFile.toPath))
          val problem = CouldNotWriteConfig(configFile, Some(e))
          log.error(problem.getMessage)
          Future.failed(problem)
      }
    } yield res

  //scalastyle:on

  private[utils] def updatedNginxConf(listWorkflows: Seq[AppParameters], uiVirtualHost: String, uiVirtualPath: String): String =
    s"""
       |events {
       |  worker_connections 4096;
       |}
       |
       |http {
       |
       |  server {
       |
       |    listen $workflowsUiPort${if (useSsl) " ssl" else ""};
       |    server_name $uiVirtualHost;
       |    ${if (useSsl) s"ssl_certificate $securityFolder/nginx_cert.crt;" else ""}
       |    ${if (useSsl) s"ssl_certificate_key $securityFolder/$instanceName.key;" else ""}
       |    access_log /dev/stdout combined;
       |    error_log stderr info;
       |
       |    ${workflowNginxLocations(listWorkflows, uiVirtualHost, uiVirtualPath)}
       |
       |  }
       |
       |}
     """.stripMargin

  private[utils] def workflowNginxLocations(listWorkflows: Seq[AppParameters], uiVirtualHost: String, uiVirtualPath: String): String =
    listWorkflows map { case AppParameters(id, ip, port) =>

      val workflowName = Try(id.substring(id.indexOf("home"))).getOrElse(id.split('/').last)
      val monitorEndUrl = monitorUrl(uiVirtualHost, uiVirtualPath, workflowName, useSsl)
      val nginxLocation = WorkflowHelper.getProxyLocation(workflowName)

      s"""
         |
         |    location $nginxLocation {
         |      proxy_pass        http://$ip:$port/;
         |      proxy_redirect    http://$uiVirtualHost/ $monitorEndUrl;
         |      proxy_set_header  Host             $$host;
         |      proxy_set_header  X-Real-IP        $$remote_addr;
         |      proxy_set_header  X-Forwarded-For  $$proxy_add_x_forwarded_for;
         |    }
         |
       """.stripMargin
    } mkString

}