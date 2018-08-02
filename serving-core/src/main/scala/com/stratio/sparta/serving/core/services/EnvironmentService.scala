/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
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
      find() match {
        case Success(env) =>
          log.debug("The environment is already created, adding default variables if not exists")
          val variablesNames = env.variables.map(_.name)
          val variablesToAdd = DefaultEnvironment.filter(variable => !variablesNames.contains(variable.name))
          log.debug(s"Variables not present in the actual environment: $variablesToAdd")
          update(Environment(variablesToAdd ++ env.variables))
        case Failure(_) =>
          log.debug("The environment is empty, creating default environment variables")
          create(Environment(DefaultEnvironment))
      }
      log.debug("The environment initialization has been completed")
    }
  }
  
  def find(): Try[Environment] = {
    log.debug("Finding environment")
    Try {
      if (CuratorFactoryHolder.existsPath(EnvironmentZkPath))
        read[Environment](new String(curatorFramework.getData.forPath(EnvironmentZkPath)))
      else throw new ServerException(s"No environment found")
    }
  }

  def create(environment: Environment): Try[Environment] = {
    log.debug("Creating environment")
    val environmentSorted = environment.copy(variables = environment.variables.sortBy(_.name))
    if (CuratorFactoryHolder.existsPath(EnvironmentZkPath)) {
      log.debug(s"The environment already exists, updating it")
      update(environmentSorted)
    } else {
      Try {
        curatorFramework.create.creatingParentsIfNeeded.forPath(
          EnvironmentZkPath,
          write(environmentSorted).getBytes
        )
        environmentSorted
      }
    }
  }

  def update(environment: Environment): Try[Environment] = {
    log.debug(s"Updating environment")
    Try {
      val environmentSorted = environment.copy(variables = environment.variables.sortBy(_.name))
      if (CuratorFactoryHolder.existsPath(EnvironmentZkPath)) {
        curatorFramework.setData().forPath(EnvironmentZkPath, write(environmentSorted).getBytes)
        environmentSorted
      } else create(environment) match {
        case Success(env) => env
        case Failure(e) => throw e
      }
    }
  }

  def delete(): Try[Unit] = {
    log.debug(s"Deleting environment")
    Try {
      if (CuratorFactoryHolder.existsPath(EnvironmentZkPath)) {
        log.debug(s"Deleting environment")
        curatorFramework.delete().forPath(EnvironmentZkPath)
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
              log.debug(s"The variable: ${environmentVariable.name} already exists, replacing it")
              environment.variables.map(variable =>
                if (variable.name == environmentVariable.name)
                  environmentVariable
                else variable
              )
            } else environment.variables :+ environmentVariable
          }.sortBy(_.name))
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
            log.debug(s"The variable ${environmentVariable.name} already exists, replacing it")
            val newEnvironment = environment.copy(
              variables = environment.variables.map(variable =>
                if (variable.name == environmentVariable.name)
                  environmentVariable
                else variable
              ).sortBy(_.name))
            update(newEnvironment).map(_ => environmentVariable)
              .getOrElse(throw new ServerException(s"Impossible to update variable"))
          } else throw new ServerException(s"The environment variable doesn't exist")
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
            .getOrElse(throw new ServerException(s"The environment variable doesn't exist"))
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
              log.info("Restoring data process after environment import completed successfully")
              None
            case Failure(exception: Exception) =>
              log.error("Restoring data process after environment import has failed." +
                " The data may be corrupted. Contact the technical support", exception)
              Option(exception.getLocalizedMessage)
          }
          throw new Exception(s"Error importing environment data." +
            s"${if(detailMsg.isDefined) s" Restoring error: ${detailMsg.get}" else ""}", e)
      }
    }
  }
}
