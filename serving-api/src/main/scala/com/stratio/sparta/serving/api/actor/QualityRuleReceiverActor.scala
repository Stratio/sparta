/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import akka.actor.Actor
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import com.stratio.sparta.core.ContextBuilder.ContextBuilderImplicits
import com.stratio.sparta.core.constants.SdkConstants.{DefaultSchemaKey, PathKey, ServiceKey}
import com.stratio.sparta.core.models.SpartaQualityRule
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.dg.agent.commons.LineageUtils
import com.stratio.sparta.dg.agent.models.MetadataPath
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.error.PostgresNotificationManagerImpl
import com.stratio.sparta.serving.core.helpers.GraphHelper.createGraph
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.governance.GovernanceQualityRule
import com.stratio.sparta.serving.core.models.workflow.{NodeGraph, Workflow}
import com.stratio.sparta.serving.core.utils.HttpRequestUtils
import com.stratio.sparta.serving.core.workflow.SpartaWorkflow
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scalax.collection.Graph
import scalax.collection.edge.LDiEdge


class QualityRuleReceiverActor extends Actor with HttpRequestUtils {

  import QualityRuleReceiverActor._
  import com.stratio.sparta.serving.core.models.governance.QualityRuleParser._

  case class StepOutputRule(stepName: String, outputName: String, rule: String)

  implicit val executionContext: ExecutionContext = context.dispatcher
  implicit val system = context.system
  implicit val actorMaterializer = ActorMaterializer()

  lazy val enabled = Try(SpartaConfig.getDetailConfig().get.getString("lineage.enable").toBoolean).getOrElse(false)
  lazy val uri = Try(SpartaConfig.getGovernanceConfig().get.getString("http.uri"))
    .getOrElse("https://governance.labs.stratio.com/dictionary")
  lazy val getQREndpoint = Try(SpartaConfig.getGovernanceConfig().get.getString("qualityrules.http.get.endpoint"))
    .getOrElse("user/quality/v1/quality/searchReactiveByMetadataPathLike?metadataPathLike=")
  lazy val getXdQREndpoint = Try(SpartaConfig.getGovernanceConfig().get.getString("qualityrules.http.get.crossdata.endpoint"))
    .getOrElse("user/quality/v1/quality/searchReactiveFederationByMetadataPathLike?metadataPathLike=")
  lazy val noTenant = Some("NONE")
  lazy val current_tenant= AppConstant.EosTenant.orElse(noTenant)
  lazy val rawHeaders = Seq(RawHeader("X-TenantID", current_tenant.getOrElse("NONE")))

  override def receive: Receive = {
    case RetrieveQualityRules(workflow, loggedUser) =>
      log.debug(s"Received RetrieveQualityRules($workflow) and LINEAGE_ENABLED is set to $enabled")
      val currentSender = sender()

      val qualityRules: Future[Seq[SpartaQualityRule]] =
        if (enabled)
          retrieveQualityRules(workflow, loggedUser)
        else Future(Seq.empty[SpartaQualityRule])

      qualityRules.onComplete {
        case Success(value) =>
          currentSender ! value
        case Failure(ex) =>
          log.error(ex.getLocalizedMessage, ex)
          currentSender ! Seq.empty[SpartaQualityRule]
      }
  }

  private def getQualityRulesFromApi(stepName: String,
                             outputName: String,
                             metadataPath: String): Future[StepOutputRule] = {

    val metadataPathString = metadataPath + "%"
    val query = URLEncoder.encode(metadataPathString, StandardCharsets.UTF_8.toString)

    val resultGet = doRequest(
      uri = uri,
      resource = getQREndpoint.concat(query),
      method = HttpMethods.GET,
      body = None,
      cookies = Seq.empty,
      headers = rawHeaders
    )

    resultGet.map { case (status, response) =>
      log.debug(s"Quality rule request for metadatapath ${metadataPath.toString} received with status ${status.value} and response $response")
      StepOutputRule(stepName, outputName, response)
    }
  }

  private def getXDQualityRulesFromApi(stepName: String,
                               outputName: String,
                               metadataPath: String): Future[StepOutputRule] = {

    val metadataPathString = metadataPath + "%"
    val query = URLEncoder.encode(metadataPathString, StandardCharsets.UTF_8.toString)

    val resultGet = doRequest(
      uri = uri,
      resource = getXdQREndpoint.concat(query),
      method = HttpMethods.GET,
      body = None,
      cookies = Seq.empty,
      headers = rawHeaders
    )

    resultGet.map { case (status, response) =>
      log.debug(s"Quality rule request for metadatapath ${metadataPath.toString} received with status ${status.value} and response $response")
      StepOutputRule(stepName, outputName, response)
    }
  }

  private def retrieveQualityRules(workflow: Workflow, loggedUser: Option[String]): Future[Seq[SpartaQualityRule]] = {
    val inputOutputGraphNodes: Seq[(NodeGraph, Map[String, String])] = retrieveInputOutputGraphNodes(workflow)
    val graphOutputPredecessorsWithTableName: Seq[Map[NodeGraph, (NodeGraph, String)]] = getOutputPredecessorsWithTableName(workflow)
    val graphOutputPredecessorsWithTableNameAndProperties: Seq[(String, (String, String, Map[String, String]))] =
      retrieveGraphOutputPredecessorsWithTableNameAndProperties(graphOutputPredecessorsWithTableName, inputOutputGraphNodes)
    val predecessorsMetadataPaths: Seq[Map[String, (String, String)]] =
      retrievePredecessorsMetadataPaths(graphOutputPredecessorsWithTableNameAndProperties, workflow)

    val resultF: Seq[Future[Seq[SpartaQualityRule]]] = for {
      predecessorsMetadataPath <- predecessorsMetadataPaths
    } yield {
      retrieveQualityRulesFromGovernance(predecessorsMetadataPath, false)
    }

    val resultFXD: Seq[Future[Seq[SpartaQualityRule]]] = for {
      xdPredecessorsMetadataPath <- retrieveXDOutputMetadataPaths(workflow, loggedUser)
    } yield {
      retrieveQualityRulesFromGovernance(xdPredecessorsMetadataPath, true)
    }

    Future.sequence(resultF ++ resultFXD).map(_.flatten)
  }


  private def retrieveQualityRulesFromGovernance(metadataPaths: Map[String, (String, String)],
                                                 isXDMetadapaths: Boolean): Future[Seq[SpartaQualityRule]] = {
    import org.json4s.native.Serialization.read

    val rulesFromApi: Seq[Future[StepOutputRule]] = metadataPaths.toSeq.map {
      case (step, (output, meta)) => if (isXDMetadapaths)
        getXDQualityRulesFromApi(step, output, meta)
        else
        getQualityRulesFromApi(step, output, meta)
    }

    val fromSeqFutureToFutureSeq: Future[Seq[StepOutputRule]] = Future.sequence(rulesFromApi)

    val seqQualityRules: Future[Seq[SpartaQualityRule]] = for {
      sequenceRules <- fromSeqFutureToFutureSeq
    } yield {
      sequenceRules.filter(_.rule.trim.nonEmpty).flatMap(stepOutputRule =>
        read[GovernanceQualityRule](stepOutputRule.rule).parse(stepOutputRule.stepName, stepOutputRule.outputName))
    }
    seqQualityRules
  }
}

object QualityRuleReceiverActor extends ContextBuilderImplicits with SpartaSerializer {

  case class RetrieveQualityRules(workflow: Workflow, loggedUser: Option[String])

  val AllowedDataGovernanceOutputs = Seq("Postgres", "Jdbc", "Avro", "Csv", "FileSystem", "Parquet", "Xml", "Json", "Text")

  def retrieveInputOutputGraphNodes(workflow: Workflow): Seq[(NodeGraph, Map[String, String])] = {
    import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
    val errorManager = PostgresNotificationManagerImpl(workflow)

    if (workflow.executionEngine == Streaming) {
      val spartaWorkflow = SpartaWorkflow[DStream](workflow, errorManager)
      spartaWorkflow.stages(execute = false)
      spartaWorkflow.inputOutputGraphNodesWithLineageProperties(workflow)
    } else if (workflow.executionEngine == Batch) {
      val spartaWorkflow = SpartaWorkflow[RDD](workflow, errorManager)
      spartaWorkflow.stages(execute = false)
      spartaWorkflow.inputOutputGraphNodesWithLineageProperties(workflow)
    } else Seq.empty[(NodeGraph, Map[String, String])]
  }

  def retrieveXDOutputMetadataPaths(workflow: Workflow, loggedUser: Option[String]): Seq[Map[String, (String, String)]] = {
    import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
    val errorManager = PostgresNotificationManagerImpl(workflow)
    val outputNodesWithWriter = LineageUtils.getOutputNodeLineageEntities(workflow)
    val xdOutNodesWithWriter = LineageUtils.getXDOutputNodesWithWriter(workflow, outputNodesWithWriter)

    if (workflow.executionEngine == Streaming) {
      val spartaWorkflow = SpartaWorkflow[DStream](workflow, errorManager, userId = loggedUser)

      spartaWorkflow.stages(execute = false)
      spartaWorkflow.getStepsWithXDOutputNodesAndProperties(xdOutNodesWithWriter)
    } else if (workflow.executionEngine == Batch) {
      val spartaWorkflow = SpartaWorkflow[RDD](workflow, errorManager, userId = loggedUser)

      spartaWorkflow.stages(execute = false)
      spartaWorkflow.getStepsWithXDOutputNodesAndProperties(xdOutNodesWithWriter)
    } else
      Seq.empty[(Map[String, (String,String)])]
  }

  def retrievePredecessorsMetadataPaths(graphOutputPredecessorsWithTableNameAndProperties: Seq[(String, (String, String, Map[String, String]))],
                                        workflow: Workflow): Seq[Map[String, (String, String)]] =
  // Step, Out, Metadata
  {
    graphOutputPredecessorsWithTableNameAndProperties
      .filter { case ((_, (_, nodePrettyName, _))) => AllowedDataGovernanceOutputs.contains(nodePrettyName) }
      .flatMap { case (pluginName, (nodeName, _, props)) =>
        props.get(ServiceKey).map { serviceName =>
          val transformationStep = workflow.pipelineGraph.nodes.filter(_.name == nodeName).head
          val outputStep = workflow.pipelineGraph.nodes.filter(_.name == pluginName).head
          val stepType = outputStep.stepType.toLowerCase
          val tableNameType = getTableNameWithSchema(transformationStep, outputStep, props)
          val dataStoreType = outputStep.classPrettyName
          val extraPath = props.get(PathKey)
            .map(_ ++ LineageUtils.extraPathFromFilesystemOutput(stepType, dataStoreType, props.get(PathKey), tableNameType))
          Map(nodeName -> (pluginName, MetadataPath(serviceName, extraPath, tableNameType).toString))
        }
      }
  }

  private def getTableNameWithSchema(
                                      transformationStep: NodeGraph,
                                      outputStep: NodeGraph,
                                      props: Map[String, String]
                                    ): Option[String] = {
    val tableName = transformationStep.outputTableName(outputStep.name)
    val schema = props.get(DefaultSchemaKey).notBlank.getOrElse("public")
    //The schema must be added only if it is a postgres or a jdbc output and if it was not specified by the user
    if ((outputStep.classPrettyName.equalsIgnoreCase("Postgres") || outputStep.classPrettyName.equalsIgnoreCase("Jdbc"))
      && !tableName.contains(".")) Option(s"$schema.$tableName")
    else Option(tableName)
  }


  def getOutputPredecessorsWithTableName(workflow: Workflow): Seq[Map[NodeGraph, (NodeGraph, String)]] = {
    val graph: Graph[NodeGraph, LDiEdge] = createGraph(workflow)

    workflow.pipelineGraph.nodes.filter(_.stepType.toLowerCase == OutputStep.StepType)
      .flatMap { outputNode =>
        val outNodeGraph = graph.get(outputNode)
        val predecessors = outNodeGraph.diPredecessors.toList
        predecessors.map { predecessor =>
          val writerName = predecessor.outputTableName(outputNode.name)
          val tableName = if (writerName.nonEmpty) writerName else predecessor.name

          workflow.pipelineGraph.nodes.find(x => x.name == predecessor.name)
            .map(predecessorNode => {
              Map(outputNode -> (predecessorNode, tableName))
            }).getOrElse(Map.empty[NodeGraph, (NodeGraph, String)])
        }
      }
  }


  def retrieveGraphOutputPredecessorsWithTableNameAndProperties(
                                                                 graphOutputPredecessorsWithTableName: Seq[Map[NodeGraph, (NodeGraph, String)]],
                                                                 inputOutputGraphNodes: Seq[(NodeGraph, Map[String, String])]
                                                               ):
  Seq[(String, (String, String, Map[String, String]))] =
  //outputName, (stepName, outputPrettyName, Properties)
    graphOutputPredecessorsWithTableName.flatMap { a =>
      a.map { case (output, (predecessor, _)) =>
        (output.name, (predecessor.name, output.classPrettyName, inputOutputGraphNodes.find(_._1.name == output.name)
          .headOption.map { case (_, properties) => properties }
          .getOrElse(Map.empty[String, String])))
      }
    }
}


