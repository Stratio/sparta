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

package com.stratio.sparta.serving.core.services

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.env.{Environment, EnvironmentData, EnvironmentVariable}
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

import scala.util.{Failure, Success, Try}

class EnvironmentService(curatorFramework: CuratorFramework) extends SpartaSerializer with SLF4JLogging {

  private val workflowService = new WorkflowService(curatorFramework)
  private val templateService = new TemplateService(curatorFramework)
  private val groupService = new GroupService(curatorFramework)

  def initialize(): Try[Unit] = {
    log.debug("Initializing environment")
    Try {
      if (find().toOption.isEmpty) {
        create(Environment(AppConstant.DefaultEnvironment))
        log.debug("The environment initialization have been completed")
      } else log.debug("The environment is already created")
    }
  }
  
  def find(): Try[Environment] = {
    log.debug("Finding environment")
    Try {
      if (CuratorFactoryHolder.existsPath(AppConstant.EnvironmentZkPath))
        read[Environment](new String(curatorFramework.getData.forPath(AppConstant.EnvironmentZkPath)))
      else throw new ServerException(s"No environment found")
    }
  }

  def create(environment: Environment): Try[Environment] = {
    log.debug("Creating environment")
    if (CuratorFactoryHolder.existsPath(AppConstant.EnvironmentZkPath)) {
      log.debug(s"The environment exists, updating it")
      update(environment)
    } else {
      Try {
        curatorFramework.create.creatingParentsIfNeeded.forPath(
          AppConstant.EnvironmentZkPath,
          write(environment).getBytes
        )
        environment
      }
    }
  }

  def update(environment: Environment): Try[Environment] = {
    log.debug(s"Updating environment")
    Try {
      if (CuratorFactoryHolder.existsPath(AppConstant.EnvironmentZkPath)) {
        curatorFramework.setData().forPath(AppConstant.EnvironmentZkPath, write(environment).getBytes)
        environment
      } else throw new ServerException(s"Unable to create environment")
    }
  }

  def delete(): Try[Unit] = {
    log.debug(s"Deleting environment")
    Try {
      if (CuratorFactoryHolder.existsPath(AppConstant.EnvironmentZkPath)) {
        log.debug(s"Deleting environment")
        curatorFramework.delete().forPath(AppConstant.EnvironmentZkPath)
      } else throw new ServerException(s"No environment available to delete")
    }
  }

  def createVariable(environmentVariable: EnvironmentVariable): Try[EnvironmentVariable] = {
    log.debug(s"Creating environment variable ${environmentVariable.name}")
    Try {
      find() match {
        case Success(environment) =>
          val newEnvironment = environment.copy(variables = {
            if (environment.variables.exists(presentEnv => presentEnv.name == environmentVariable.name)) {
              log.debug(s"The variable: ${environmentVariable.name} exists, replacing it")
              environment.variables.map(variable =>
                if (variable.name == environmentVariable.name)
                  environmentVariable
                else variable
              )
            } else environment.variables :+ environmentVariable
          })
          update(newEnvironment).map(_ => environmentVariable)
            .getOrElse(throw new ServerException(s"Impossible to create variable"))
        case Failure(e) =>
          create(Environment(Seq(environmentVariable))).map(_ => environmentVariable).getOrElse(
            throw new ServerException(s"Impossible to create environment with the variable, ${e.getLocalizedMessage}"))
      }
    }
  }

  def updateVariable(environmentVariable: EnvironmentVariable): Try[EnvironmentVariable] = {
    log.debug(s"Updating environment variable ${environmentVariable.name}")
    Try {
      find() match {
        case Success(environment) =>
          if (environment.variables.exists(presentEnv => presentEnv.name == environmentVariable.name)) {
            log.debug(s"The variable ${environmentVariable.name} exists, replacing it")
            val newEnvironment = environment.copy(
              variables = environment.variables.map(variable =>
                if (variable.name == environmentVariable.name)
                  environmentVariable
                else variable
              ))
            update(newEnvironment).map(_ => environmentVariable)
              .getOrElse(throw new ServerException(s"Impossible to update variable"))
          } else throw new ServerException(s"The environment variable not exists")
        case Failure(e) =>
          throw new ServerException(s"Impossible to update variable, ${e.getLocalizedMessage}")
      }
    }
  }

  def deleteVariable(name: String): Try[Environment] = {
    log.debug(s"Deleting environment variable $name")
    Try {
      find() match {
        case Success(environment) =>
          val newEnvironment = environment.copy(
            variables = environment.variables.filter(variable => variable.name != name)
          )
          update(newEnvironment)
            .getOrElse(throw new ServerException(s"Impossible to delete variable"))
        case Failure(e) =>
          throw new ServerException(s"Impossible to delete variable, ${e.getLocalizedMessage}")
      }
    }
  }

  def findVariable(name: String): Try[EnvironmentVariable] = {
    log.debug(s"Finding environment variable $name")
    Try {
      find() match {
        case Success(environment) =>
          environment.variables.find(variable => variable.name == name)
            .getOrElse(throw new ServerException(s"The environment variable not exists"))
        case Failure(e) =>
          throw new ServerException(s"Impossible to find variable, ${e.getLocalizedMessage}")
      }
    }
  }

  def exportData(): Try[EnvironmentData] = {
    log.debug(s"Exporting environment data")
    find().map(environment =>
      EnvironmentData(
        workflowService.findAll,
        templateService.findAll,
        groupService.findAll,
        environment.variables.map(_.name)
      ))
  }

  def importData(data: EnvironmentData): Try[EnvironmentData] = {
    log.debug(s"Importing environment data")
    Try {
      val initialTemplates = templateService.findAll
      val initialWorkflows = workflowService.findAll
      val initialGroups = groupService.findAll
      val initialEnvVariables: Environment = find() match {
        case Success(environment) => environment
        case Failure(e) => log.info("No previous environment found during import")
          Environment(Seq.empty[EnvironmentVariable])
      }
      val initialEnvVariablesNames = initialEnvVariables.variables.map(_.name)

      try {
        groupService.deleteAll()
        workflowService.deleteAll()
        templateService.deleteAll()
        groupService.createList(data.groups)
        templateService.createList(data.templates)
        workflowService.createList(data.workflows)
        data.envVariables.foreach(variable =>
          if (!initialEnvVariablesNames.contains(variable))
            createVariable(EnvironmentVariable(variable, ""))
        )
        data
      } catch {
        case e: Exception =>
          log.error("Error importing data. All groups, workflows, env. variables and templates will be rolled back", e)
          val detailMsg = Try {
            groupService.deleteAll()
            workflowService.deleteAll()
            templateService.deleteAll()
            delete()
            initialGroups.foreach(groupService.create)
            initialTemplates.foreach(templateService.create)
            initialWorkflows.foreach(workflow => workflowService.create(workflow))
            initialEnvVariables.variables.foreach(createVariable)
          } match {
            case Success(_) =>
              log.info("Restoring data process after environment import completed successful")
              None
            case Failure(exception: Exception) =>
              log.error("Restoring data process after environment import has failed." +
                " The data maybe corrupt. Contact with the support.", exception)
              Option(exception.getLocalizedMessage)
          }
          throw new Exception(s"Error importing environment data." +
            s"${if(detailMsg.isDefined) s" Restoring error: ${detailMsg.get}" else ""}", e)
      }
    }
  }
}
