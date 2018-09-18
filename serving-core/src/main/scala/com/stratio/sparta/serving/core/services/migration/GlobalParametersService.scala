/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.migration

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.parameters.{GlobalParameters, ParameterVariable}
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

import scala.util.{Failure, Success, Try}

//TODO fix with EnvironmentService Andromeda and GlobalParameters service in 2.3
class GlobalParametersService(curatorFramework: CuratorFramework) extends SpartaSerializer with SLF4JLogging {

  def initialize(): Try[Unit] = {
    log.debug("Initializing global parameters")
    Try {
      find() match {
        case Success(env) =>
          log.debug("The global parameters is already created, adding default variables if not exists")
          val variablesNames = env.variables.map(_.name)
          val variablesToAdd = DefaultGlobalParameters.filter(variable => !variablesNames.contains(variable.name))
          log.debug(s"Variables not present in the actual global parameters: $variablesToAdd")
          update(GlobalParameters(variablesToAdd ++ env.variables))
        case Failure(_) =>
          log.debug("The global parameters is empty, creating default global parameters variables")
          create(GlobalParameters(DefaultGlobalParameters))
      }
      log.debug("The global parameters initialization has been completed")
    }
  }
  
  def find(): Try[GlobalParameters] = {
    log.debug("Finding global parameters")
    Try {
      if (CuratorFactoryHolder.existsPath(GlobalParametersZkPath))
        read[GlobalParameters](new String(curatorFramework.getData.forPath(GlobalParametersZkPath)))
      else throw new ServerException(s"No global parameters found")
    }
  }

  def create(globalParameters: GlobalParameters): Try[GlobalParameters] = {
    log.debug("Creating global parameters")
    val globalParametersSorted = globalParameters.copy(variables = globalParameters.variables.sortBy(_.name))
    if (CuratorFactoryHolder.existsPath(GlobalParametersZkPath)) {
      log.debug(s"The global parameters already exists, updating it")
      update(globalParametersSorted)
    } else {
      Try {
        curatorFramework.create.creatingParentsIfNeeded.forPath(
          GlobalParametersZkPath,
          write(globalParametersSorted).getBytes
        )
        globalParametersSorted
      }
    }
  }

  def update(globalParameters: GlobalParameters): Try[GlobalParameters] = {
    log.debug(s"Updating global parameters")
    Try {
      val globalParametersSorted = globalParameters.copy(variables = globalParameters.variables.sortBy(_.name))
      if (CuratorFactoryHolder.existsPath(GlobalParametersZkPath)) {
        curatorFramework.setData().forPath(GlobalParametersZkPath, write(globalParametersSorted).getBytes)
        globalParametersSorted
      } else create(globalParameters) match {
        case Success(env) => env
        case Failure(e) => throw e
      }
    }
  }

  def delete(): Try[Unit] = {
    log.debug(s"Deleting global parameters")
    Try {
      if (CuratorFactoryHolder.existsPath(GlobalParametersZkPath)) {
        log.debug(s"Deleting global parameters")
        curatorFramework.delete().forPath(GlobalParametersZkPath)
      } else throw new ServerException(s"No global parameters available to delete")
    }
  }

  def createVariable(globalParametersVariable: ParameterVariable): Try[ParameterVariable] = {
    log.debug(s"Creating global parameters variable ${globalParametersVariable.name}")
    Try {
      find() match {
        case Success(globalParameters) =>
          val newEnvironment = globalParameters.copy(variables = {
            if (globalParameters.variables.exists(presentEnv => presentEnv.name == globalParametersVariable.name)) {
              log.debug(s"The variable: ${globalParametersVariable.name} already exists, replacing it")
              globalParameters.variables.map(variable =>
                if (variable.name == globalParametersVariable.name)
                  globalParametersVariable
                else variable
              )
            } else globalParameters.variables :+ globalParametersVariable
          }.sortBy(_.name))
          update(newEnvironment).map(_ => globalParametersVariable)
            .getOrElse(throw new ServerException(s"Impossible to create variable"))
        case Failure(e) =>
          create(GlobalParameters(Seq(globalParametersVariable))).map(_ => globalParametersVariable).getOrElse(
            throw new ServerException(
              s"Impossible to create global parameters with the variable, ${e.getLocalizedMessage}"))
      }
    }
  }

  def updateVariable(globalParametersVariable: ParameterVariable): Try[ParameterVariable] = {
    log.debug(s"Updating global parameters variable ${globalParametersVariable.name}")
    Try {
      find() match {
        case Success(globalParameters) =>
          if (globalParameters.variables.exists(presentEnv => presentEnv.name == globalParametersVariable.name)) {
            log.debug(s"The variable ${globalParametersVariable.name} already exists, replacing it")
            val newEnvironment = globalParameters.copy(
              variables = globalParameters.variables.map(variable =>
                if (variable.name == globalParametersVariable.name)
                  globalParametersVariable
                else variable
              ).sortBy(_.name))
            update(newEnvironment).map(_ => globalParametersVariable)
              .getOrElse(throw new ServerException(s"Impossible to update variable"))
          } else throw new ServerException(s"The global parameters variable doesn't exist")
        case Failure(e) =>
          throw new ServerException(s"Impossible to update variable, ${e.getLocalizedMessage}")
      }
    }
  }

  def deleteVariable(name: String): Try[GlobalParameters] = {
    log.debug(s"Deleting global parameters variable $name")
    Try {
      find() match {
        case Success(globalParameters) =>
          val newEnvironment = globalParameters.copy(
            variables = globalParameters.variables.filter(variable => variable.name != name)
          )
          update(newEnvironment)
            .getOrElse(throw new ServerException(s"Impossible to delete variable"))
        case Failure(e) =>
          throw new ServerException(s"Impossible to delete variable, ${e.getLocalizedMessage}")
      }
    }
  }

  def findVariable(name: String): Try[ParameterVariable] = {
    log.debug(s"Finding global parameters variable $name")
    Try {
      find() match {
        case Success(globalParameters) =>
          globalParameters.variables.find(variable => variable.name == name)
            .getOrElse(throw new ServerException(s"The global parameters variable doesn't exist"))
        case Failure(e) =>
          throw new ServerException(s"Impossible to find variable, ${e.getLocalizedMessage}")
      }
    }
  }
}
