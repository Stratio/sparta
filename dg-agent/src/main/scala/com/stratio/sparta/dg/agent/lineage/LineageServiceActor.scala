/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.dg.agent.lineage

import akka.actor.{Actor, ActorRef, ActorRefFactory, Props}
import akka.cluster.Cluster
import akka.event.slf4j.SLF4JLogging
import akka.http.scaladsl.model.{HttpMethod, HttpMethods, StatusCode, StatusCodes}
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

  lazy val uri = Try(SpartaConfig.getGovernanceConfig().get.getString("http.uri"))
    .getOrElse("https://governance.labs.stratio.com/dictionary")
  lazy val postEndpoint = Try(SpartaConfig.getGovernanceConfig().get.getString("lineage.http.post.endpoint"))
    .getOrElse("v1/lineage/actor")
  lazy val getEndpoint = Try(SpartaConfig.getGovernanceConfig().get.getString("lineage.http.get.endpoint"))
    .getOrElse("v1/lineage/actor/searchByTransactionId?transactionId=")

  lazy val actorTypeKey = "SPARTA"

  lazy val rawHeaders = Seq(RawHeader("X-TenantID", "NONE"))
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

    log.debug(s"Sending lineage data to metadata id [$metaDataId] and execution id [$executionId] with data: $newWorkflow")

    val result = doRequest(
      uri = uri,
      resource = newEndpoint,
      method = httpMethod,
      body = Option(newWorkflow),
      cookies = Seq.empty,
      headers = rawHeaders
    )

    result.onComplete { completedAction: Try[(StatusCode, String)] =>
      completedAction match {
        case Success((status, response)) =>
          log.debug(s"Execution workflow with id $executionId correctly sent to endpoint with Status:" +
            s"${status.value} and response: $response")
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

            resultGet.onComplete { completedAction: Try[(StatusCode, String)] =>
              completedAction match {
                case Success((statusCode, response)) =>
                  log.debug(s"Status: ${statusCode.value} and response: $response")

                  if(statusCode == StatusCodes.OK) {
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
      val parsedLineageProperties =
        LineageUtils.addTableNameFromWriterToOutput(nodesOutGraph, lineageProperties) ++ inputNodesProperties

      // TODO nistal (QR)
      val listStepsMetadata: Seq[ActorMetadata] = parsedLineageProperties.flatMap { case (pluginName, props) =>
        props.get(ServiceKey).map { serviceName =>
          val stepType = workflow.pipelineGraph.nodes.filter(_.name == pluginName).head.stepType.toLowerCase
          val dataStoreType = workflow.pipelineGraph.nodes.filter(_.name == pluginName).head.classPrettyName
          val extraPath = props.get(PathKey).map(_ ++ LineageUtils.extraPathFromFilesystemOutput(stepType, dataStoreType, props.get(PathKey), props.get(ResourceKey)))
          val metaDataPath = MetadataPath(serviceName, extraPath, props.get(ResourceKey)).toString

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