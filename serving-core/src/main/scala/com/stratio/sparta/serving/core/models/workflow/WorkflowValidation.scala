/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.core.ContextBuilder.ContextBuilderImplicits
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.models.WorkflowValidationMessage
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.serving.core.error.PostgresErrorImpl
import com.stratio.sparta.serving.core.helpers.{JarsHelper, WorkflowHelper}
import com.stratio.sparta.serving.core.models.enumerators.ArityValueEnum.{ArityValue, _}
import com.stratio.sparta.serving.core.models.enumerators.DeployMode
import com.stratio.sparta.serving.core.models.enumerators.NodeArityEnum.{NodeArity, _}
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.services.dao.WorkflowPostgresDao
import com.stratio.sparta.serving.core.workflow.SpartaWorkflow
import org.apache.spark.sql.Dataset
import org.apache.spark.streaming.dstream.DStream

import scala.util.Try
import scalax.collection.Graph
import scalax.collection.GraphTraversal.Visitor
import scalax.collection.edge.LDiEdge

case class WorkflowValidation(valid: Boolean, messages: Seq[WorkflowValidationMessage])
  extends DistributedMonadImplicits with ContextBuilderImplicits {

  def this(valid: Boolean) = this(valid, messages = Seq.empty[WorkflowValidationMessage])

  def this() = this(valid = true)

  val InputMessage = "input"
  val OutputMessage = "output"

  /** A group name is correct if and only if:
    * 1) there are no two or more consecutives /
    * 2) it starts with /home (the default root)
    * 3) the group name has no Uppercase letters or other special characters (except / and -)
    */
  val regexGroups = "^(?!.*[/]{2}.*$)(^(/home)+(/)*([a-z0-9-/]*)$)"

  // A Workflow name should not contain special characters and Uppercase letters (because of DCOS deployment)
  val regexName = "^[a-z0-9-]*"

  def validateInvalidGroupParameters(implicit workflow: Workflow): WorkflowValidation = {
    val groupParametersInWorkflow = WorkflowPostgresDao.getGroupsParametersUsed(workflow)
    val invalidGroupsParameters = groupParametersInWorkflow.filter { groupParameter =>
      !workflow.settings.global.parametersLists.contains(groupParameter._1)
    }
    val invalidGroups = invalidGroupsParameters.map(_._1)
    val invalidParameters = invalidGroupsParameters.map(_._2)

    if (invalidGroupsParameters.nonEmpty)
      copy(
        valid = false,
        messages = messages :+ WorkflowValidationMessage(s"There are parameters with invalid " +
          s"groups(${invalidGroups.mkString(",")}): ${invalidParameters.mkString(",")}")
      )
    else this
  }

  def validateDeployMode(implicit workflow: Workflow): WorkflowValidation = {
    if (workflow.settings.global.executionMode == marathon &&
      workflow.settings.sparkSettings.submitArguments.deployMode.isDefined &&
      workflow.settings.sparkSettings.submitArguments.deployMode.get != DeployMode.client)
      copy(
        valid = false,
        messages = messages :+ WorkflowValidationMessage(s"The selected execution mode is Marathon and the deploy mode is not client")
      )
    else if (workflow.settings.global.executionMode == dispatcher &&
      workflow.settings.sparkSettings.submitArguments.deployMode.isDefined &&
      workflow.settings.sparkSettings.submitArguments.deployMode.get != DeployMode.cluster)
      copy(
        valid = false,
        messages = messages :+ WorkflowValidationMessage(s"The selected execution mode is Mesos and the deploy mode is not cluster")
      )
    else this
  }

  def validateSparkCores(implicit workflow: Workflow): WorkflowValidation = {
    if ((workflow.settings.global.executionMode == marathon ||
      workflow.settings.global.executionMode == dispatcher) &&
      workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.coresMax.notBlank.isDefined &&
      workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.executorCores.notBlank.isDefined &&
      workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.coresMax.get.toString.toDouble <
        workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.executorCores.get.toString.toDouble)
      copy(
        valid = false,
        messages = messages :+ WorkflowValidationMessage(s"The total number of executor cores (max cores) should be greater than" +
          s" the number of executor cores")
      )
    else this
  }

  def validateCheckpointCubes(implicit workflow: Workflow): WorkflowValidation =
    workflow.pipelineGraph.nodes.find(node => node.className == "CubeTransformStep") match {
      case Some(_) => if (!workflow.settings.streamingSettings.checkpointSettings.enableCheckpointing)
        copy(
          valid = false,
          messages = messages :+ WorkflowValidationMessage(s"The workflow contains Cubes and the checkpoint is not enabled")
        )
      else this
      case None => this
    }

  def validateGroupName(implicit workflow: Workflow): WorkflowValidation = {
    if (workflow.group.name.matches(regexGroups))
      this
    else {
      val msg = messages :+ WorkflowValidationMessage("The workflow group is invalid")
      copy(valid = false, messages = msg)
    }
  }

  def validateMesosConstraints(implicit workflow: Workflow): WorkflowValidation = {
    if (workflow.settings.global.mesosConstraint.notBlank.isDefined && !workflow.settings.global.
      mesosConstraint.get.toString.contains(":"))
      copy(
        valid = false,
        messages = messages :+ WorkflowValidationMessage(s"The Mesos constraints must be two alphanumeric strings separated by a colon")
      )
    else if (workflow.settings.global.mesosConstraint.notBlank.isDefined &&
      workflow.settings.global.mesosConstraint.get.toString.contains(":") &&
      workflow.settings.global.mesosConstraint.get.toString.count(_ == ':') > 1)
      copy(
        valid = false,
        messages = messages :+ WorkflowValidationMessage(s"The colon may appear only once in the Mesos constraint definition")
      )
    else if (workflow.settings.global.mesosConstraint.notBlank.isDefined &&
      workflow.settings.global.mesosConstraint.get.toString.contains(":") &&
      (workflow.settings.global.mesosConstraint.get.toString.indexOf(":") == 0 ||
        workflow.settings.global.mesosConstraint.get.toString.indexOf(":") ==
          workflow.settings.global.mesosConstraint.get.toString.length))
      copy(
        valid = false,
        messages = messages :+ WorkflowValidationMessage(s"The colon cannot be situated at the edges of the Mesos constraint definition")
      )
    else this
  }

  def validatePlugins(implicit workflow: Workflow): WorkflowValidation = {
    val pluginsValidations = if (workflow.executionEngine == Streaming) {
      val plugins = JarsHelper.localUserPluginJars(workflow)
      val errorManager = PostgresErrorImpl(workflow)
      val spartaWorkflow = SpartaWorkflow[DStream](workflow, errorManager, plugins)
      spartaWorkflow.stages(execute = false)
      spartaWorkflow.validate()
    } else if (workflow.executionEngine == Batch) {
      val plugins = JarsHelper.localUserPluginJars(workflow)
      val errorManager = PostgresErrorImpl(workflow)
      val spartaWorkflow = SpartaWorkflow[Dataset](workflow, errorManager, plugins)
      spartaWorkflow.stages(execute = false)
      spartaWorkflow.validate()
    } else Seq.empty

    pluginsValidations.map(errorValidation => WorkflowValidation(errorValidation.valid, errorValidation.messages))
      .foldLeft(this) { case (lastValidation, newValidation) =>
        combineWithAnd(lastValidation, newValidation)
      }
  }

  def validateErrorOutputs(implicit workflow: Workflow): WorkflowValidation = {
    val errorOutputNodes = workflow.pipelineGraph.nodes.filter { node =>
      val isSinkOutput = Try(node.configuration(WorkflowHelper.OutputStepErrorProperty).toString.toBoolean)
        .getOrElse(false)
      node.stepType.toLowerCase == OutputStep.StepType && isSinkOutput
    }.map(_.name)

    val saveErrors = workflow.settings.errorsManagement.transactionsManagement.sendToOutputs.flatMap { action =>
      if (!errorOutputNodes.contains(action.outputStepName)) {
        Option(action.outputStepName)
      } else None
    }

    if (saveErrors.nonEmpty) {
      copy(
        valid = false,
        messages = messages :+ WorkflowValidationMessage(s"The workflow has 'Error outputs' defined" +
          s"that don't exist as nodes. ${saveErrors.mkString(", ")}")
      )
    } else this
  }

  def validateName(implicit workflow: Workflow): WorkflowValidation = {
    if (workflow.name.nonEmpty && workflow.name.matches(regexName)) this
    else copy(valid = false, messages = messages :+ WorkflowValidationMessage("The workflow name is empty or invalid"))
  }

  def validateNonEmptyNodes(implicit workflow: Workflow): WorkflowValidation =
    if (workflow.pipelineGraph.nodes.size >= 2) this
    else copy(valid = false, messages = messages :+ WorkflowValidationMessage("The workflow must contain at least two nodes"))

  def validateNonEmptyEdges(implicit workflow: Workflow): WorkflowValidation =
    if (workflow.pipelineGraph.edges.nonEmpty) this
    else copy(valid = false, messages = messages :+ WorkflowValidationMessage("The workflow must contain at least one relation"))

  def validateEdgesNodesExists(implicit workflow: Workflow): WorkflowValidation = {
    val nodesNames = workflow.pipelineGraph.nodes.map(_.name)
    val wrongEdges = workflow.pipelineGraph.edges.flatMap(edge =>
      if (nodesNames.contains(edge.origin) && nodesNames.contains(edge.destination)) None
      else Option(edge)
    )

    if (wrongEdges.isEmpty || workflow.pipelineGraph.edges.isEmpty) this
    else copy(
      valid = false,
      messages = messages :+ WorkflowValidationMessage(s"The workflow has relations that don't exist as nodes: ${wrongEdges.mkString(" , ")}")
    )
  }

  def validateGraphIsAcyclic(implicit workflow: Workflow, graph: Graph[NodeGraph, LDiEdge]): WorkflowValidation = {
    val cycle = graph.findCycle

    if (cycle.isEmpty || workflow.pipelineGraph.edges.isEmpty) this
    else copy(
      valid = false,
      messages = messages :+ WorkflowValidationMessage(s"The workflow contains one or more cycles" + {
        if (cycle.isDefined)
          s"${": " + cycle.get.nodes.toList.map(node => node.value.asInstanceOf[NodeGraph].name).mkString(",")}"
        else "!"
      })
    )
  }

  def validateExistenceCorrectPath(implicit workflow: Workflow, graph: Graph[NodeGraph, LDiEdge]): WorkflowValidation = {

    def node(outer: NodeGraph): graph.NodeT = (graph get outer).asInstanceOf[graph.NodeT]

    val inputNodes: Seq[graph.NodeT] = workflow.pipelineGraph.nodes
      .filter(node => node.stepType.equals("Input")).map(node(_))
    val outputNodes: Seq[graph.NodeT] = workflow.pipelineGraph.nodes
      .filter(node => node.stepType.equals("Output")).map(node(_))

    val path = {
      for {in <- inputNodes.toStream
           out <- outputNodes.toStream
      } yield {
        in.pathTo(out)(Visitor.empty)
      }
    } exists (_.isDefined)


    if (path) this else this.copy(
      valid = false,
      messages = messages :+ WorkflowValidationMessage(s"The workflow has no I->O path")
    )
  }

  def validateDuplicateNames(implicit workflow: Workflow, graph: Graph[NodeGraph, LDiEdge]): WorkflowValidation = {
    val nodes: Seq[NodeGraph] = workflow.pipelineGraph.nodes
    nodes.groupBy(_.name).find(listName => listName._2.size > 1) match {
      case Some(duplicate) =>
        this.copy(
          valid = false,
          messages = messages :+ WorkflowValidationMessage(s"The workflow has two nodes with the same name: ${duplicate._1}")
        )
      case None => this
    }
  }


  def validateArityOfNodes(implicit workflow: Workflow, graph: Graph[NodeGraph, LDiEdge]): WorkflowValidation = {
    workflow.pipelineGraph.nodes.foldLeft(this) { case (lastValidation, node) =>
      val nodeInGraph = graph.get(node)
      val inDegree = nodeInGraph.inDegree
      val outDegree = nodeInGraph.outDegree
      val validation = {
        if (node.arity.nonEmpty)
          node.arity.foldLeft(new WorkflowValidation(valid = false)) { case (lastArityValidation, arity) =>
            combineWithOr(lastArityValidation, validateArityDegrees(node, inDegree, outDegree, arity))
          }
        else new WorkflowValidation()
      }

      combineWithAnd(lastValidation, validation)
    }
  }

  def combineWithAnd(first: WorkflowValidation, second: WorkflowValidation): WorkflowValidation =
    if (first.valid && second.valid) new WorkflowValidation()
    else WorkflowValidation(valid = false, messages = first.messages ++ second.messages)

  def combineWithOr(first: WorkflowValidation, second: WorkflowValidation): WorkflowValidation =
    if (first.valid || second.valid) new WorkflowValidation()
    else WorkflowValidation(valid = false, messages = first.messages ++ second.messages)

  private[workflow] def validateArityDegrees(
                                              nodeGraph: NodeGraph,
                                              inDegree: Int,
                                              outDegree: Int,
                                              arity: NodeArity
                                            ): WorkflowValidation =
    arity match {
      case NullaryToNary =>
        combineWithAnd(
          validateDegree(inDegree, Nullary, InvalidMessage(nodeGraph.name, InputMessage)),
          validateDegree(outDegree, Nary, InvalidMessage(nodeGraph.name, OutputMessage))
        )
      case UnaryToUnary =>
        combineWithAnd(
          validateDegree(inDegree, Unary, InvalidMessage(nodeGraph.name, InputMessage)),
          validateDegree(outDegree, Unary, InvalidMessage(nodeGraph.name, OutputMessage))
        )
      case UnaryToNary =>
        combineWithAnd(
          validateDegree(inDegree, Unary, InvalidMessage(nodeGraph.name, InputMessage)),
          validateDegree(outDegree, Nary, InvalidMessage(nodeGraph.name, OutputMessage))
        )
      case BinaryToNary =>
        combineWithAnd(
          validateDegree(inDegree, Binary, InvalidMessage(nodeGraph.name, InputMessage)),
          validateDegree(outDegree, Nary, InvalidMessage(nodeGraph.name, OutputMessage))
        )
      case NaryToNullary =>
        combineWithAnd(
          validateDegree(inDegree, Nary, InvalidMessage(nodeGraph.name, InputMessage)),
          validateDegree(outDegree, Nullary, InvalidMessage(nodeGraph.name, OutputMessage))
        )
      case NaryToNary =>
        combineWithAnd(
          validateDegree(inDegree, Nary, InvalidMessage(nodeGraph.name, InputMessage)),
          validateDegree(outDegree, Nary, InvalidMessage(nodeGraph.name, OutputMessage))
        )
      case NullaryToNullary =>
        combineWithAnd(
          validateDegree(inDegree, Nullary, InvalidMessage(nodeGraph.name, InputMessage)),
          validateDegree(outDegree, Nullary, InvalidMessage(nodeGraph.name, OutputMessage))
        )
    }

  private[workflow] def validateDegree(
                                        degree: Int,
                                        arityDegree: ArityValue,
                                        invalidMessage: InvalidMessage
                                      ): WorkflowValidation =
    arityDegree match {
      case Nullary =>
        validateDegreeValue(degree, 0, invalidMessage)
      case Unary =>
        validateDegreeValue(degree, 1, invalidMessage)
      case Binary =>
        validateDegreeValue(degree, 2, invalidMessage)
      case Nary =>
        if (degree > 0) new WorkflowValidation()
        else WorkflowValidation(
          valid = false,
          messages = Seq(WorkflowValidationMessage(s"Invalid number of relations, the node ${invalidMessage.nodeName} has $degree" +
            s" ${invalidMessage.relationType} relations and support 1 to N"))
        )
    }

  private[workflow] def validateDegreeValue(
                                             degree: Int,
                                             arityDegree: Int,
                                             invalidMessage: InvalidMessage
                                           ): WorkflowValidation =
    if (degree == arityDegree) new WorkflowValidation()
    else WorkflowValidation(
      valid = false,
      messages = Seq(WorkflowValidationMessage(s"Invalid number of relations, the node ${invalidMessage.nodeName} has $degree" +
        s" ${invalidMessage.relationType} relations and support $arityDegree"))
    )

  case class InvalidMessage(nodeName: String, relationType: String)

}