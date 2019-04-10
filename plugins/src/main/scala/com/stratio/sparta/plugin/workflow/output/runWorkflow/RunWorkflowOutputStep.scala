/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.output.runWorkflow

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.{GraphStep, OutputStep}
import com.stratio.sparta.plugin.enumerations.RunWorkflowWhen
import com.stratio.sparta.plugin.models.{RunWithContext, RunWithVariable, RunWorkflowAction}
import com.stratio.sparta.plugin.workflow.output.runWorkflow.RunWorkflowOutputStep._
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.MarathonConstant
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.parameters.ParameterVariable
import com.stratio.sparta.serving.core.models.workflow.{ExecutionContext, RunExecutionSettings, WorkflowIdExecutionContext}
import org.apache.spark.sql._
import org.apache.spark.sql.crossdata.XDSession
import org.json4s.jackson.Serialization.{read, write}
import org.json4s.{DefaultFormats, Formats}

import scala.util.matching.Regex
import scala.util.{Failure, Properties, Success, Try}

class RunWorkflowOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  val runWorkflowWhen: RunWorkflowWhen.Value = Try {
    RunWorkflowWhen.withName(properties.getString("runWorkflowWhen", "RECEIVE_DATA").toUpperCase)
  }.getOrElse(RunWorkflowWhen.RECEIVE_DATA)

  val uniqueInstance: Boolean = Try{
    properties.getString("uniqueInstance", "false").toBoolean
  }.getOrElse(false)

  lazy val runWorkflowProperty: Option[RunWorkflowAction] = Try {

    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()

    val workflowId = properties.getString("workflowId")
    val contextsInput = s"${properties.getString("contexts", None).notBlank.fold("[]") { values => values.toString }}"
    val contexts = read[Seq[RunWithContext]](contextsInput)
    val variablesInput = s"${properties.getString("variables", None).notBlank.fold("[]") { values => values.toString }}"
    val variables = read[Seq[RunWithVariable]](variablesInput)

    RunWorkflowAction(
      workflowId = workflowId,
      contexts = contexts,
      variables = variables,
      uniqueInstance = uniqueInstance
    )
  }.toOption

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    if (runWorkflowWhen == RunWorkflowWhen.RECEIVE_DATA) {
      dataFrame.collect().foreach { row =>
        runWorkflowProperty.foreach { runWorkflowProperty =>
          val workflowId = replaceValueWithRowValues(runWorkflowProperty.workflowId, row)
          val contexts = runWorkflowProperty.contexts.map { runContext =>
            val context = replaceValueWithRowValues(runContext.contextName, row)
            runContext.copy(contextName = context)
          }
          val variables = runWorkflowProperty.variables.map { runVariable =>
            val variable = replaceValueWithRowValues(runVariable.value, row)
            runVariable.copy(value = variable)
          }
          val runWorkflowPropertyReplaced = runWorkflowProperty.copy(
            workflowId = workflowId,
            contexts = contexts,
            variables = variables
          )

          runWorkflow(runWorkflowPropertyReplaced)
        }
      }
    }
  }

  override def cleanUp(options: Map[String, String] = Map.empty[String, String]): Unit = {
    if (runWorkflowWhen == RunWorkflowWhen.AFTER_WORKFLOW_ENDS && !options.contains(GraphStep.FailedKey)) {
      runWorkflowProperty.foreach(runWorkflow)
    }
  }

  private def runWorkflow(runWorkflowProperty: RunWorkflowAction): Unit = {
    Try {

      implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()

      val workflowIdExecutionContext = runWorkflowToWorkflowIdExecutionContext(runWorkflowProperty)
      val workflowIdExecutionContextStr = write(workflowIdExecutionContext)
      if (CuratorFactoryHolder.existsPath(RunWorkflowZkPath))
        CuratorFactoryHolder.getInstance().setData()
          .forPath(RunWorkflowZkPath, workflowIdExecutionContextStr.getBytes)
      else CuratorFactoryHolder.getInstance().create().creatingParentsIfNeeded()
        .forPath(RunWorkflowZkPath, workflowIdExecutionContextStr.getBytes)
      workflowIdExecutionContextStr
    } match {
      case Success(runWorkflowSaved) =>
        log.info(s"Workflow run notified in Zookeeper for workflow ${runWorkflowProperty.workflowId}" +
          s" with properties ${runWorkflowProperty.variables}" +
          s" and contexts ${runWorkflowProperty.contexts}. The object saved is $runWorkflowSaved")
      case Failure(e) =>
        log.error(s"Error notifying run workflow in Zookeeper for workflow ${runWorkflowProperty.workflowId}" +
          s" with properties ${runWorkflowProperty.variables}" +
          s" and contexts ${runWorkflowProperty.contexts}", e)
        CuratorFactoryHolder.resetInstance()
        throw e
    }
  }

  private def runWorkflowToWorkflowIdExecutionContext(runWorkflowProperty: RunWorkflowAction): WorkflowIdExecutionContext = {
    WorkflowIdExecutionContext(
      workflowId = runWorkflowProperty.workflowId,
      executionContext = ExecutionContext(
        extraParams = runWorkflowProperty.variables.map(variable => ParameterVariable(variable.name, Option(variable.value))),
        paramsLists = runWorkflowProperty.contexts.map(_.contextName)
      ),
      executionSettings = Option(RunExecutionSettings(
        userId = Properties.envOrNone(MarathonConstant.UserNameEnv).orElse(Option(xDSession.user)),
        uniqueInstance = Option(runWorkflowProperty.uniqueInstance)
      ))
    )
  }

  private def replaceValueWithRowValues(value: String, row: Row): String = {
    val variableReplaceableFileds = findReplaceableFields(value).map { field =>
      if (field.contains("."))
        field.split(".").last
      else field
    }
    val replaceableFieldsInSchema = variableReplaceableFileds.forall(field => Try(row.fieldIndex(field)).isSuccess)

    require(replaceableFieldsInSchema, "All the replaceable fieds must be contained in the dataframe schema")

    if (variableReplaceableFileds.nonEmpty) {
      variableReplaceableFileds.map(field => row.get(row.fieldIndex(field)).toString).mkString("")
    } else value
  }
}

object RunWorkflowOutputStep {

  val ReplaceableFiledRegex: Regex = regexEnvelope("[a-zA-Z0-9_]+")

  private def regexEnvelope(internalPattern: String): Regex = ("\\$\\{" + internalPattern + "\\}").r

  def findReplaceableFields(text: String): Set[String] = {
    val matches = ReplaceableFiledRegex.findAllIn(text).toSet
    matches.map(str => str.substring(2, str.length - 1))
  }
}


