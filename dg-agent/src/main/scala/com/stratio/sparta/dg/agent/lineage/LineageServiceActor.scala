/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.dg.agent.lineage

import akka.actor.{Actor, ActorRef, ActorRefFactory, Props}
import akka.cluster.Cluster
import akka.event.slf4j.SLF4JLogging
import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import com.stratio.sparta.core.constants.SdkConstants._
import com.stratio.sparta.core.workflow.step.InputStep
import com.stratio.sparta.dg.agent.commons.LineageUtils
import com.stratio.sparta.dg.agent.models.{ActorMetadata, LineageWorkflow, MetadataPath}
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangeListenerActor.OnExecutionStatusesChangeDo
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
import com.stratio.sparta.serving.core.models.workflow.WorkflowExecutionStatusChange
import com.stratio.sparta.serving.core.utils.{HttpRequestUtils, SpartaClusterUtils}
import org.json4s.jackson.Serialization._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

class LineageServiceActor(executionStatusChangeListenerActor: ActorRef) extends Actor
  with SpartaClusterUtils
  with HttpRequestUtils
  with SpartaSerializer
  with SLF4JLogging {

  import LineageServiceActor._

  implicit val system = context.system

  val cluster = Cluster(context.system)
  val actorRefFactory: ActorRefFactory = context
  val actorMaterializer = ActorMaterializer()

  lazy val uri = Try(SpartaConfig.getLineageConfig().get.getString("uri"))
    .getOrElse("https://governance.labs.stratio.com/dictionary")
  lazy val postEndpoint = Try(SpartaConfig.getLineageConfig().get.getString("post.endpoint"))
    .getOrElse("v1/lineage/actor")
  lazy val getEndpoint = Try(SpartaConfig.getLineageConfig().get.getString("get.endpoint"))
    .getOrElse("/v1/lineage/actor/searchByTransactionId?transactionId=")

  lazy val actorTypeKey = "SPARTA"

  lazy val rawHeaders = Seq(RawHeader("X-TenantID", "NONE"))
  lazy val HttpStatusOK = "200 OK"
  lazy val noTenant = Some("NONE")


  override def preStart(): Unit = {
    extractStatusChanges()
  }

  override def receive: Receive = {
    case _ => log.error("Unrecognized message in LineageService Actor")
  }

  def doPostPutRequest(httpMethod: HttpMethod, metaDataId: Option[Int], newWorkflow: String, executionId: String): Unit = {
    val newEndpoint = if (httpMethod == HttpMethods.PUT)
      postEndpoint + "/" + metaDataId.getOrElse("")
    else postEndpoint

    val result = doRequest(
      uri = uri,
      resource = newEndpoint,
      method = httpMethod,
      body = Option(newWorkflow),
      cookies = Seq.empty,
      headers = rawHeaders
    )

    result.onComplete { completedAction: Try[(String, String)] =>
      completedAction match {
        case Success((status, response)) =>
          log.debug(s"Execution workflow with id $executionId correctly sent to endpoint with Status:" +
            s"$status and response: $response")
        case Failure(e) =>
          log.error(s"Error sending data to lineage API with $HttpMethod method", e)
      }
    }
  }

  //scalastyle:off
  def extractStatusChanges(): Unit =
    executionStatusChangeListenerActor ! OnExecutionStatusesChangeDo(ExecutionStatusLineageKey) { executionStatusChange =>
      Try {
        val lineageWorkflow = extractWorkflowChanges(executionStatusChange)
        val exEngine = executionStatusChange.newExecution.executionEngine.get
        val executionId = executionStatusChange.newExecution.getExecutionId

        lineageWorkflow match {
          case Some(wf) if exEngine == Streaming  =>
            val resultGet = doRequest(
              uri = uri,
              resource = getEndpoint + executionId,
              method = HttpMethods.GET,
              body = None,
              cookies = Seq.empty,
              headers = rawHeaders
            )

            resultGet.onComplete { completedAction: Try[(String, String)] =>
              completedAction match {
                case Success((statusCode, response)) =>
                  log.debug(s"Status: $statusCode and response: $response")

                  if(statusCode == HttpStatusOK) {
                    val responseWorkflow = read[LineageWorkflow](response)
                    val newWorkflow = write(LineageUtils.updateLineageWorkflow(responseWorkflow, wf))
                    doPostPutRequest(HttpMethods.PUT, Option(responseWorkflow.id), newWorkflow, executionId)
                  }
                  else{
                    val wfStreamingJson = write(wf)
                    doPostPutRequest(HttpMethods.POST, None, wfStreamingJson, executionId)
                  }
                case Failure(e) =>
                  log.error(s"Error execution GET method to lineage API for workflow with executionId: $executionId", e)
              }
            }

          case Some(wf) if exEngine == Batch =>
            val wfBatchJson = write(wf)
            doPostPutRequest(HttpMethods.POST, None, wfBatchJson, executionId)

          case _ =>
        }
      } match {
        case Success(_) =>
        case Failure(e) =>
          log.error("Error sending data to lineage API", e)
      }
    }

  //scalastyle:off
  private def extractWorkflowChanges(executionStatusChange: WorkflowExecutionStatusChange): Option[LineageWorkflow] = {

    val workflow = executionStatusChange.newExecution.getWorkflowToExecute
    val executionId = executionStatusChange.newExecution.getExecutionId

    if (LineageUtils.checkIfProcessableWorkflow(executionStatusChange)) {
      val executionProperties = LineageUtils.setExecutionProperties(executionStatusChange.newExecution)
      val lineageProperties = LineageUtils.getAllStepsProperties(workflow)
      val nodesOutGraph = LineageUtils.getOutputNodesWithWriter(workflow)
      val inputNodes = workflow.pipelineGraph.nodes.filter(_.stepType.toLowerCase == InputStep.StepType).map(_.name).toSet
      val inputNodesProperties = lineageProperties.filterKeys(inputNodes).toSeq
      val parsedLineageProperties = nodesOutGraph.map { case (outputName, nodeTableName) =>
        outputName -> lineageProperties.getOrElse(outputName, Map.empty).map { case property@(key, value) =>
          if (key.equals(ResourceKey) && value.isEmpty) {
            (ResourceKey, nodeTableName)
          } else property
        }
      } ++ inputNodesProperties

      val listStepsMetadata: Seq[ActorMetadata] = parsedLineageProperties.flatMap { case (pluginName, props) =>
        props.get(ServiceKey).map { serviceName =>
          val metaDataPath = MetadataPath(serviceName, props.get(PathKey), props.get(ResourceKey)).toString
          val dataStoreType = workflow.pipelineGraph.nodes.filter(_.name == pluginName).head.classPrettyName
          val stepType = workflow.pipelineGraph.nodes.filter(_.name == pluginName).head.stepType.toLowerCase

          ActorMetadata(
            `type` = LineageUtils.mapSparta2GovernanceStepType(stepType),
            metaDataPath = metaDataPath,
            dataStoreType = LineageUtils.mapSparta2GovernanceDataStoreType(dataStoreType),
            tenant = noTenant,
            properties = Map.empty
          )
        }
      }

      Option(LineageWorkflow(
        id = -1,
        name = workflow.name,
        description = workflow.description,
        tenant = noTenant,
        properties = executionProperties,
        transactionId = executionId,
        actorType = actorTypeKey,
        jobType = LineageUtils.mapSparta2GovernanceJobType(workflow.executionEngine),
        statusCode = LineageUtils.mapSparta2GovernanceStatuses(executionStatusChange.newExecution.lastStatus.state),
        version = AppConstant.version,
        listActorMetaData = listStepsMetadata.toList
      ))

    } else None
  }
}

object LineageServiceActor {

  def props(executionListenerActor: ActorRef): Props = Props(new LineageServiceActor(executionListenerActor))

  val ExecutionStatusLineageKey = "execution-status-lineage"
}