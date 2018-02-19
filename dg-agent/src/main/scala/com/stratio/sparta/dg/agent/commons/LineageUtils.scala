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

package com.stratio.sparta.dg.agent.commons

import scalax.collection.GraphEdge.DiEdge
import com.stratio.governance.commons.agent.model.metadata.MetadataPath
import com.stratio.sparta.dg.agent.model.{SpartaInputMetadata, SpartaTenantMetadata}
import com.stratio.sparta.serving.core.models.workflow.{NodeGraph, Workflow}

import scalax.collection._
import org.joda.time.DateTime

import scala.util.Properties

/**
  * Utilitary object for dg-workflows methods
  */
object LineageUtils {

  private[commons] def workflowMetadataPathString(workflow: Workflow): String = s"${workflow.group.name}/${
    workflow.name}/${workflow.version}/${workflow.lastUpdateDate.getOrElse(DateTime.now()).getMillis}"

  def inputMetadataLineage(workflow: Workflow, graph: Graph[NodeGraph, DiEdge]): List[SpartaInputMetadata] = {
    val metadataPath = workflowMetadataPathString(workflow)
    workflow.pipelineGraph.nodes.filter(node => node.stepType.equals("Input")).map(
      n => SpartaInputMetadata(
        name = n.name,
        key = n.classPrettyName,
        metadataPath = MetadataPath(metadataPath),
        outcomingNodes = graph.get(n).diSuccessors.map(s => MetadataPath(s"$metadataPath/${s.name}")).toSeq,
        tags = workflow.tag.toList,
        modificationTime = workflow.lastUpdateDate.map(_.getMillis))
    ).toList
  }

  def tenantMetadataLineage() : List[SpartaTenantMetadata] = {
    val tenantName = Properties.envOrElse("MARATHON_APP_LABEL_DCOS_SERVICE_NAME", "sparta")

    val tenantList = List(SpartaTenantMetadata(
      name = tenantName,
      key = tenantName,
      metadataPath = MetadataPath(s"$tenantName"),
      tags = List(),
      oauthEnable = Properties.envOrElse("SECURITY_OAUTH2_ENABLE", "false").toBoolean,
      gosecEnable = Properties.envOrElse("ENABLE_GOSEC_AUTH", "false").toBoolean,
      xdCatalogEnable = Properties.envOrElse("CROSSDATA_CORE_ENABLE_CATALOG", "false").toBoolean,
      mesosHostnameConstraint = Properties.envOrElse("MESOS_HOSTNAME_CONSTRAINT", ""),
      mesosAttributeConstraint = Properties.envOrElse("MESOS_ATTRIBUTE_CONSTRAINT", "")
    ))

    tenantList
  }
}
