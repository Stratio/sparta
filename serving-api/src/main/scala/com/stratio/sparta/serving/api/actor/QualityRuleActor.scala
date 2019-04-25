/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import akka.actor.{Actor, Cancellable, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import com.stratio.sparta.core.ContextBuilder.ContextBuilderImplicits
import com.stratio.sparta.core.constants.SdkConstants.{PathKey, ResourceKey, ServiceKey}
import com.stratio.sparta.core.models.SpartaQualityRule
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.dg.agent.commons.LineageUtils
import com.stratio.sparta.dg.agent.models.MetadataPath
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangePublisherActor
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangePublisherActor.ExecutionStatusChange
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.error.PostgresNotificationManagerImpl
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.helpers.GraphHelper.createGraph
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.governance.{GovernanceQualityResult, GovernanceQualityRule, QualityRuleResult}
import com.stratio.sparta.serving.core.models.workflow.{NodeGraph, Workflow}
import com.stratio.sparta.serving.core.utils.{HttpRequestUtils, SpartaClusterUtils}
import com.stratio.sparta.serving.core.workflow.SpartaWorkflow
import org.apache.spark.sql.Dataset
import org.apache.spark.streaming.dstream.DStream
import org.json4s.jackson.Serialization.write
import scalax.collection.Graph
import scalax.collection.edge.LDiEdge

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Failure, Success, Try}


class QualityRuleActor extends Actor
  with HttpRequestUtils with SpartaClusterUtils {

  import QualityRuleActor._
  import com.stratio.sparta.serving.core.models.governance.QualityRuleParser._

  implicit val executionContext: ExecutionContext = context.dispatcher
  implicit val system = context.system
  implicit val actorMaterializer = ActorMaterializer()
  val cluster = Cluster(context.system)

  implicit val ec = context.system.dispatchers.lookup("sparta-actors-dispatcher")

  val mediator = DistributedPubSub(context.system).mediator

  val qualityRuleResultsService = PostgresDaoFactory.qualityRuleResultPgService

  private lazy val governancePushTickTask: Cancellable = context.system.scheduler.schedule(1 minutes, GovernancePushDuration, self, GovernancePushTick)

  lazy val uri = Try(SpartaConfig.getGovernanceConfig().get.getString("http.uri"))
    .getOrElse("https://governance.labs.stratio.com/dictionary")
  lazy val postEndpoint = Try(SpartaConfig.getGovernanceConfig().get.getString("qualityrules.http.post.endpoint"))
    .getOrElse("v1/quality/metrics")
  lazy val getEndpoint = Try(SpartaConfig.getGovernanceConfig().get.getString("qualityrules.http.get.endpoint"))
    .getOrElse("v1/quality/quality/searchByMetadataPathLike")
  val noTenant = Some("NONE")


  override def preStart(): Unit = {
    mediator ! Subscribe(ExecutionStatusChangePublisherActor.ClusterTopicExecutionStatus, self)
    governancePushTickTask
  }

  override def receive: Receive = {

    case RetrieveQualityRules(workflow) => {
      log.debug(s"Received RetrieveQualityRules($workflow)")
      val currentSender = sender()

      val qualityRules: Future[Seq[SpartaQualityRule]] = retrieveQualityRules(workflow)
      qualityRules.onComplete {
        case Success(value) =>
          currentSender ! value
        case Failure(ex) =>
          log.error(ex.getLocalizedMessage, ex)
          currentSender ! Seq.empty[SpartaQualityRule]
      }

    }
    case GovernancePushTick =>
      log.info("Received GovernancePushTick")
      governancePushRest
    case workflowExecutionStatusChange: ExecutionStatusChange =>
      if (
        (workflowExecutionStatusChange.executionChange.newExecution.lastStatus.state == StoppedByUser ||
          workflowExecutionStatusChange.executionChange.newExecution.lastStatus.state == Finished) && workflowExecutionStatusChange.executionChange.originalExecution.lastStatus.state != workflowExecutionStatusChange.executionChange.newExecution.lastStatus.state
      ) governancePushRest
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe(ExecutionStatusChangePublisherActor.ClusterTopicExecutionStatus, self)
    governancePushTickTask.cancel()
    super.postStop()
  }

  def sendResultsToApi(qualityRuleResult: QualityRuleResult): Future[(String, Boolean)] = {

    val qualityRuleGovernance: GovernanceQualityResult =
      GovernanceQualityResult.parseSpartaResult(qualityRuleResult, noTenant)

    val qualityRuleResultJson = write(qualityRuleGovernance)

    if (isThisNodeClusterLeader(cluster)) {
      val resultPost = doRequest(
        uri = uri,
        resource = postEndpoint,
        method = HttpMethods.POST,
        body = Option(qualityRuleResultJson),
        cookies = Seq.empty,
        headers = rawHeaders
      )

      val result = for {
        (status, response) <- resultPost
        _ <- qualityRuleResultsService.upsert(qualityRuleResult.copy(sentToApi = true, warning = false))
      } yield {
        log.debug(s"Quality rule sent with requestBody $qualityRuleResultJson and receive status: $status and response: $response")
        response
      }

      result.onComplete { completedAction: Try[String] =>
        completedAction match {
          case Success(response) =>
            log.info(s"Sent results for quality rule ${qualityRuleResult.id.getOrElse("Without id!")} with response: $response")
          case Failure(e) =>
            log.error(s"Error sending data for quality rule ${qualityRuleResult.id.getOrElse("Without id!")} to API with POST method", e)
        }
      }
      result.map(_ => (qualityRuleResult.id.getOrElse("Without id!"), true))
    } else Future.successful(("None", true))
  }

  case class StepOutputRule(stepName: String, outputName: String, rule: String)


  def getQualityRulesfromApi(stepName: String,
                             outputName: String,
                             metadataPath: MetadataPath): Future[StepOutputRule] = {

    val metadataPathString = s"${metadataPath.toString}%"
    val query = URLEncoder.encode(metadataPathString, StandardCharsets.UTF_8.toString).toLowerCase

    val resultGet = doRequest(
      uri = uri,
      resource = getEndpoint.concat(query),
      method = HttpMethods.GET,
      body = None,
      cookies = Seq.empty,
      headers = rawHeaders
    )

    resultGet.map{ case (status, response)  =>
      log.debug(s"Quality rule request for metadatapath ${metadataPath.toString} received with status $status and response $response")
      StepOutputRule(stepName, outputName, response)
    }
  }

  def retrieveQualityRules(workflow: Workflow): Future[Seq[SpartaQualityRule]] = {
    val inputOutputGraphNodes: Seq[(NodeGraph, Map[String, String])] = retrieveInputOutputGraphNodes(workflow)
    val graphOutputPredecessorsWithTableName: Seq[Map[NodeGraph, (NodeGraph, String)]] = getOutputPredecessorsWithTableName(workflow)
    val graphOutputPredecessorsWithTableNameAndProperties: Seq[(String, (String, String, Map[String, String]))] =
      retrieveGraphOutputPredecessorsWithTableNameAndProperties(graphOutputPredecessorsWithTableName, inputOutputGraphNodes)
    val predecessorsMetadataPaths: Seq[Map[String, (String, MetadataPath)]] =
      retrievePredecessorsMetadataPaths(graphOutputPredecessorsWithTableNameAndProperties, workflow)

    val resultF: Seq[Future[Seq[SpartaQualityRule]]] = for {
      predecessorsMetadataPath <- predecessorsMetadataPaths
    } yield {retrieveQualityRulesFromGovernance(predecessorsMetadataPath)}

    Future.sequence(resultF).map(_.flatten)
  }



  def retrieveQualityRulesFromGovernance(metadataPaths: Map[String, (String,MetadataPath)]): Future[Seq[SpartaQualityRule]] = {
    import org.json4s.native.Serialization.read

    val rulesFromApi: Seq[Future[StepOutputRule]] = metadataPaths.toSeq.map{
      case (step, (output, meta)) => getQualityRulesfromApi(step, output, meta)}

    val fromSeqFutureToFutureSeq: Future[Seq[StepOutputRule]] = Future.sequence(rulesFromApi)

    val seqQualityRules: Future[Seq[SpartaQualityRule]] = for {
      sequenceRules <- fromSeqFutureToFutureSeq

    } yield {
      sequenceRules.filter(_.rule.trim.nonEmpty).flatMap(stepOutputRule =>
        read[GovernanceQualityRule](stepOutputRule.rule).parse(stepOutputRule.stepName, stepOutputRule.outputName))
    }
    seqQualityRules
  }

  def governancePushRest: Unit = {
    val finalResult: Future[List[String]] = for {
      unsentRules <- qualityRuleResultsService.findAllUnsent()
      result <- Future.sequence(
        unsentRules.map(rule => sendResultsToApi(rule))
      )
    } yield {
      result.map( res => res._1)
    }

    finalResult.onSuccess {
      case list => log.debug(s"Correctly sent results for quality rules: ${list.mkString(",")}")
    }
  }

}

object QualityRuleActor extends ContextBuilderImplicits
  with SpartaSerializer {

  def props: Props =  Props[QualityRuleActor]
  val name = "QualityRuleActor"

  val ExecutionStatusQualityRuleKey = "execution-status-qualityRule"

  val AllowedDataGovernanceOutputs = Seq("Postgres", "Jdbc", "Avro", "Csv", "FileSystem", "Parquet", "Xml", "Json", "Text")

  val GovernancePushDuration: FiniteDuration = 15 minute

  case class RetrieveQualityRules(workflow: Workflow)

  case object GovernancePushTick

  lazy val actorTypeKey = "SPARTA"

  val rawHeaders = Seq(RawHeader("X-TenantID", "NONE"))
  val HttpStatusOK = "200 OK"
  val noTenant = Some("NONE")

  def retrieveInputOutputGraphNodes(workflow: Workflow) : Seq[(NodeGraph, Map[String, String])] = {
    import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
    val errorManager = PostgresNotificationManagerImpl(workflow)

    if (workflow.executionEngine == Streaming) {
      val spartaWorkflow = SpartaWorkflow[DStream](workflow, errorManager)
      spartaWorkflow.stages(execute = false)
      spartaWorkflow.inputOutputGraphNodesWithLineageProperties(workflow)
    } else if (workflow.executionEngine == Batch) {
      val spartaWorkflow = SpartaWorkflow[Dataset](workflow, errorManager)
      spartaWorkflow.stages(execute = false)
      spartaWorkflow.inputOutputGraphNodesWithLineageProperties(workflow)
    } else Seq.empty[(NodeGraph, Map[String,String])]
  }

  def retrievePredecessorsMetadataPaths(graphOutputPredecessorsWithTableNameAndProperties: Seq[(String, (String, String, Map[String, String]))],
                                        workflow: Workflow): Seq[Map[String, (String, MetadataPath)]] =
  // Step, Out, Metadata
  {
    graphOutputPredecessorsWithTableNameAndProperties
      .filter {case ((_,(_,nodePrettyName,_))) =>  AllowedDataGovernanceOutputs.contains(nodePrettyName)}
      .flatMap { case (pluginName, (nodeName, _, props)) =>
        props.get(ServiceKey).map { serviceName =>
          val stepType = workflow.pipelineGraph.nodes.filter(_.name == pluginName).head.stepType.toLowerCase
          val tableNameType = Option(workflow.pipelineGraph.nodes.filter(_.name == nodeName).head.writer.tableName.
            fold(nodeName){ x => x.toString}).notBlank
          val dataStoreType = workflow.pipelineGraph.nodes.filter(_.name == pluginName).head.classPrettyName
          val extraPath = props.get(PathKey)
            .map(_ ++ LineageUtils.extraPathFromFilesystemOutput(stepType, dataStoreType, props.get(PathKey), tableNameType))
          Map(nodeName ->( pluginName, MetadataPath(serviceName, extraPath, tableNameType)))
        }
      }
  }

  def getOutputPredecessorsWithTableName(workflow: Workflow): Seq[Map[NodeGraph, (NodeGraph, String)]] = {
    val graph: Graph[NodeGraph, LDiEdge] = createGraph(workflow)

    workflow.pipelineGraph.nodes.filter(_.stepType.toLowerCase == OutputStep.StepType)
      .flatMap { outputNode =>
        val outNodeGraph = graph.get(outputNode)
        val predecessors = outNodeGraph.diPredecessors.toList
        predecessors.map { predecessor =>
          val writerName = predecessor.writer.tableName.map(_.toString).getOrElse("")
          val tableName = if (writerName.nonEmpty) writerName else predecessor.name

          workflow.pipelineGraph.nodes.find(x => x.name == predecessor.name)
            .map(predecessorNode => {
              Map(outputNode -> (predecessorNode, tableName))
            }).getOrElse(Map.empty[NodeGraph, (NodeGraph, String)])
        }
      }
  }


  def retrieveGraphOutputPredecessorsWithTableNameAndProperties(graphOutputPredecessorsWithTableName: Seq[Map[NodeGraph, (NodeGraph, String)]],
                                                                inputOutputGraphNodes: Seq[(NodeGraph, Map[String,String])]):
  Seq[(String, (String, String, Map[String, String]))] =
  //outputName, (stepName, outputPrettyName, Properties)
    graphOutputPredecessorsWithTableName.flatMap { a =>
      a.map { case(output, (predecessor, _)) =>
        (output.name, (predecessor.name, output.classPrettyName, inputOutputGraphNodes.find(_._1.name == output.name)
          .headOption.map {case (_, properties) => properties}
          .getOrElse(Map.empty[String, String])))
      }
    }
}
