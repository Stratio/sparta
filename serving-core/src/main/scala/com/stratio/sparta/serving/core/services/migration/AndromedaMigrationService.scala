/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.migration

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.parameters.{GlobalParameters, ParameterList, ParameterVariable}
import com.stratio.sparta.serving.core.models.workflow.migration.{EnvironmentAndromeda, WorkflowAndromeda}
import com.stratio.sparta.serving.core.models.workflow.{Group, TemplateElement, Workflow}
import org.json4s.jackson.Serialization.read

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

class AndromedaMigrationService() extends SLF4JLogging with SpartaSerializer {

  import com.stratio.sparta.serving.core.models.workflow.migration.MigrationModelImplicits._

  private val templateZkService = new ZkTemplateService(CuratorFactoryHolder.getInstance())
  private val groupZkService = new ZkGroupService(CuratorFactoryHolder.getInstance())
  private val environmentZkService = new ZkEnvironmentService(CuratorFactoryHolder.getInstance())

  /** TEMPLATES **/

  def andromedaTemplatesMigrated(cassiopeaTemplates: Seq[TemplateElement]): Try[Seq[TemplateElement]] = {
    log.info(s"Migrating templates from Andromeda")
    Try {
      val templates = if (CuratorFactoryHolder.existsPath(TemplatesZkPath)) {
        templateZkService.findAll.filter { template =>
          template.versionSparta.isDefined && template.versionSparta.get == AndromedaVersion
        } ++ cassiopeaTemplates
      } else cassiopeaTemplates

      templates.map { template =>
        template.copy(versionSparta = Some(version))
      }
    }
  }

  /** GROUPS **/

  def andromedaGroupsMigrated(): Try[Seq[Group]] = {
    log.info(s"Migrating groups from Andromeda")
    Try {
      if (CuratorFactoryHolder.existsPath(GroupZkPath)) {
        groupZkService.findAll
      } else Seq.empty
    }
  }

  /** WORKFLOWS **/

  def andromedaWorkflowsMigrated(cassiopeaWorkflowsToAndromeda: Seq[WorkflowAndromeda]): Try[Seq[Workflow]] = {
    log.info(s"Migrating workflows from Andromeda")
    Try {
      if (CuratorFactoryHolder.existsPath(WorkflowsZkPath)) {
        val children = CuratorFactoryHolder.getInstance().getChildren.forPath(WorkflowsZkPath)
        val andromedaWorkflows = JavaConversions.asScalaBuffer(children).toList.flatMap { id =>
          andromedaWorkflowExistsById(id)
        } ++ cassiopeaWorkflowsToAndromeda
        andromedaWorkflows.map { workflow =>
          Try {
            val orionWorkflow: Workflow = workflow
            orionWorkflow
          } match {
            case Success(orionWorkflow) =>
              orionWorkflow
            case Failure(e) =>
              log.error(s"Workflow ${workflow.name} Andromeda migration error. ${ExceptionHelper.toPrintableException(e)}", e)
              throw e
          }
        }
      } else Seq.empty
    }
  }

  private[sparta] def andromedaWorkflowExistsById(id: String): Option[WorkflowAndromeda] =
    Try {
      if (CuratorFactoryHolder.existsPath(s"$WorkflowsZkPath/$id")) {
        val data = new Predef.String(CuratorFactoryHolder.getInstance().getData.forPath(s"$WorkflowsZkPath/$id"))
        if (data.contains("versionSparta") && (data.contains("2.2.0") || data.contains("2.2.1") || data.contains("2.2.2"))) {
          val workFlow = read[WorkflowAndromeda](data)
          Option(workFlow.copy(status = None))
        } else None
      } else None
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        None
    }

  /** ENVIRONMENT **/

  //scalastyle:off
  def andromedaEnvironmentMigrated(): Try[Option[(GlobalParameters, ParameterList, ParameterList, EnvironmentAndromeda)]] = {
    log.info(s"Migrating environment from Andromeda")
    Try {
      environmentZkService.find().map { environmentAndromeda =>
        val environmentVariables = environmentAndromeda.variables.flatMap { variable =>
          if (DefaultEnvironmentParameters.exists(parameter => parameter.name == variable.name))
            Option(ParameterVariable(variable.name, Option(variable.value)))
          else None
        }
        val globalVariables = environmentAndromeda.variables.flatMap { variable =>
          if (DefaultGlobalParameters.exists(parameter => parameter.name == variable.name))
            if (variable.name == "SPARK_EXECUTOR_BASE_IMAGE" && (variable.value == "qa.stratio.com/stratio/spark-stratio-driver:2.2.0-1.0.0" || variable.value == "qa.stratio.com/stratio/stratio-spark:2.2.0.5"))
              Option(ParameterVariable(variable.name, Option("qa.stratio.com/stratio/spark-stratio-driver:2.2.0-2.0.0-ae1b428")))
            else if (variable.name == "SPARK_DRIVER_JAVA_OPTIONS" && variable.value == "-Dconfig.file=/etc/sds/sparta/spark/reference.conf -XX:+UseConcMarkSweepGC -Dlog4j.configurationFile=file:///etc/sds/sparta/log4j2.xml")
              Option(ParameterVariable(variable.name, Option("-Dconfig.file=/etc/sds/sparta/spark/reference.conf -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:+UseConcMarkSweepGC -Dlog4j.configurationFile=file:///etc/sds/sparta/log4j2.xml -Djava.util.logging.config.file=file:///etc/sds/sparta/log4j2.xml")))
            else if (variable.name == "SPARK_EXECUTOR_EXTRA_JAVA_OPTIONS")
              Option(ParameterVariable(variable.name, Option("-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:+UseConcMarkSweepGC")))
            else Option(ParameterVariable(variable.name, Option(variable.value)))
          else None
        }
        val defaultCustomVariables = environmentAndromeda.variables.flatMap { variable =>
          if (DefaultCustomExampleParameters.exists(parameter => parameter.name == variable.name))
            Option(ParameterVariable(variable.name, Option(variable.value)))
          else None
        }
        val orphanedVariables = environmentAndromeda.variables.flatMap { variable =>
          if (!DefaultEnvironmentParameters.exists(parameter => parameter.name == variable.name) &&
            !DefaultGlobalParameters.exists(parameter => parameter.name == variable.name) &&
            !DefaultCustomExampleParameters.exists(parameter => parameter.name == variable.name)
          )
            Option(ParameterVariable(variable.name, Option(variable.value)))
          else None
        }
        val environmentParameterList = ParameterList(
          id = EnvironmentParameterListId,
          name = EnvironmentParameterListName,
          parameters = environmentVariables ++ orphanedVariables
        )
        val defaultCustomParameterList = ParameterList(
          id = CustomExampleParameterListId,
          name = CustomExampleParameterList,
          parameters = defaultCustomVariables
        )

        (GlobalParameters(globalVariables), environmentParameterList, defaultCustomParameterList, environmentAndromeda)
      }
    }
  }
}
