/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.helpers

import java.io.Serializable

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.utils.ClasspathUtils
import com.stratio.sparta.serving.core.constants.{AppConstant, MarathonConstant}
import com.stratio.sparta.serving.core.constants.MarathonConstant._
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine.ExecutionEngine
import com.stratio.sparta.serving.core.models.workflow.{NodeGraph, Workflow, WorkflowExecution}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._

import scala.util.{Failure, Properties, Success, Try}

object WorkflowHelper extends SLF4JLogging {

  lazy val classpathUtils = new ClasspathUtils

  val OutputStepErrorProperty = "errorSink"

  def getConfigurationsFromObjects(workflow: Workflow, methodName: String): Map[String, String] = {
    log.debug("Initializing reflection ...")
    workflow.pipelineGraph.nodes.flatMap { node =>
      Try {
        val className = getClassName(node, workflow.executionEngine)
        if (!className.matches("CustomLite[\\w]*Step")) {
          val classType = node.configuration.getOrElse(AppConstant.CustomTypeKey, className).toString
          val clazzToInstance = classpathUtils.defaultStepsInClasspath.getOrElse(classType, node.className)
          val clazz = Class.forName(clazzToInstance)
          clazz.getMethods.find(p => p.getName == methodName) match {
            case Some(method) =>
              method.setAccessible(true)
              method.invoke(clazz, node.configuration.asInstanceOf[Map[String, Serializable]])
                .asInstanceOf[Seq[(String, String)]]
            case None =>
              Seq.empty[(String, String)]
          }
        } else Seq.empty[(String, String)]
      } match {
        case Success(configurations) =>
          configurations
        case Failure(_) =>
          Seq.empty[(String, String)]
      }
    }.toMap
  }

  def getClassName(node: NodeGraph, executionEngine: ExecutionEngine): String =
    node.executionEngine match {
      case Some(nodeExEngine) =>
        if (node.className.endsWith(nodeExEngine.toString))
          node.className
        else node.className + nodeExEngine
      case None =>
        if (node.className.endsWith(executionEngine.toString))
          node.className
        else node.className + executionEngine.toString
    }

  private[serving] def retrieveGroup(group: String): String = {
    val reg = "(?!^/)(.*)(?<!/$)".r
    Try(reg.findAllMatchIn(group).next.matched).getOrElse(group)
  }

  def getExecutionDeploymentId(workflowExecution: WorkflowExecution): String = {
    val workflow = workflowExecution.getWorkflowToExecute

    s"${retrieveGroup(workflow.group.name)}/${workflow.name}/${workflow.name}-v${workflow.version}" +
      s"/${workflowExecution.getExecutionId}"
  }

  def getMarathonId(workflowExecution: WorkflowExecution): String =
    s"$getMarathonBaseId/${getExecutionDeploymentId(workflowExecution)}"

  def getMarathonBaseId: String = {
    val companyLabelPrefix = Properties.envOrNone(DcosServiceCompanyLabelPrefix)
    val basicApplicationPath = {
      val baseApplicationPath = Properties.envOrElse(DcosServiceBaseApplicationPath, "sparta")
      s"$baseApplicationPath/$instanceNameHttpService/workflows"
    }

    if(companyLabelPrefix.isDefined) {
      (
        Properties.envOrNone(s"$MarathonLabelPrefixEnv${companyLabelPrefix.get}_$ProductLabelEnv"),
        Properties.envOrNone(s"$MarathonLabelPrefixEnv${companyLabelPrefix.get}_$ApplicationLabelEnv")
      ) match {
        case (Some(productPath), Some(appPath)) =>
          val baseApplicationPath = Properties.envOrElse(DcosServiceBaseApplicationPath, "project")
          s"$baseApplicationPath/$productPath/$appPath/$instanceNameHttpService/workflows"
        case _ =>
          basicApplicationPath
      }
    } else basicApplicationPath
  }

  def getReverseProxyLocation(workflowLocation: String): String = {
    if(isReverseProxyConfigured)
      s"/$workflowLocation/"
    else getProxyLocation(workflowLocation)
  }

  def isReverseProxyConfigured: Boolean = {
    Properties.envOrNone(NginxMarathonLBProxyPassPathEnv).isDefined &&
    Properties.envOrNone(NginxMarathonLBFrontendAclWithPathEnv).isDefined &&
    Try(Properties.envOrNone(NginxMarathonLBRemovePathLocationEnv).map(_.toBoolean).get).getOrElse(false)
  }

  def getProxyLocation(workflowLocation: String): String = s"$getVirtualPath/$workflowLocation/"

  def getVirtualPath: String = {
    Properties.envOrElse(
      MarathonConstant.NginxMarathonLBUserPathEnv,
      Properties.envOrElse(MarathonConstant.NginxMarathonLBPathEnv, s"/workflows-${AppConstant.instanceNameHttpService}")
    )
  }

  def getVirtualHost: String = {
    Properties.envOrElse(
      MarathonConstant.NginxMarathonLBUserHostEnv,
      Properties.envOrElse(MarathonConstant.NginxMarathonLBHostEnv, "sparta.stratio.com")
    )
  }

  def isMarathonLBConfigured: Boolean =
    (Properties.envOrNone(MarathonConstant.NginxMarathonLBHostEnv).notBlank.isDefined &&
      Properties.envOrNone(MarathonConstant.NginxMarathonLBPathEnv).notBlank.isDefined) ||
      (Properties.envOrNone(MarathonConstant.NginxMarathonLBUserHostEnv).notBlank.isDefined &&
        Properties.envOrNone(MarathonConstant.NginxMarathonLBUserPathEnv).notBlank.isDefined)

  def isCalicoEnabled: Boolean = {
    val calicoEnabled = Properties.envOrNone(CalicoEnableEnv)
    val calicoNetwork = Properties.envOrNone(CalicoNetworkEnv).notBlank
    if (calicoEnabled.isDefined && calicoEnabled.get.equals("true") && calicoNetwork.isDefined) true else false
  }
}
