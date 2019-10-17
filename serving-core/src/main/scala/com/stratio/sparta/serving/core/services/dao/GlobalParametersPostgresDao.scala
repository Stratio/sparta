/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.dao

import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.dao.GlobalParametersDao
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.parameters.{GlobalParameters, ParameterList, ParameterVariable}
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection
import slick.jdbc.PostgresProfile

import scala.concurrent.Future
import scala.util.{Failure, Success}

//scalastyle:off
class GlobalParametersPostgresDao extends GlobalParametersDao {

  override val profile = PostgresProfile
  override val db = JdbcSlickConnection.getDatabase

  import profile.api._

  override def initializeData(): Unit = {
    val globalParametersFuture = find()

    globalParametersFuture.onFailure { case _ =>
      log.debug("Initializing global parameters")
      for {
        _ <- updateGlobalParameters(GlobalParameters(DefaultGlobalParameters))
      } yield {
        log.debug("The global parameters initialization has been completed")
      }
    }

    globalParametersFuture.onSuccess { case globalParameters =>
      val variablesNames = globalParameters.variables.map(_.name)
      val variablesToAdd = DefaultGlobalParameters.filter { variable =>
        !variablesNames.contains(variable.name)
      }
      val newVariablesMap = DefaultGlobalParametersMap ++ ParameterList.parametersToMap(globalParameters.variables)
      log.debug(s"Variables not present in the actual global parameters: $variablesToAdd")
      updateGlobalParameters(globalParameters.copy(
        variables = newVariablesMap.values.toSeq
      ))
    }

  }

  def createGlobalParameters(globalParameters: GlobalParameters): Future[GlobalParameters] = {
   val quotedGlobalParameters = findAndEscapeGlobalParameters(globalParameters)
    for {
      _ <- createAndReturnList(quotedGlobalParameters.variables)
    } yield quotedGlobalParameters
  }

  def updateGlobalParameters(globalParameters: GlobalParameters): Future[GlobalParameters] = {
    val quotedGlobalParameters = findAndEscapeGlobalParameters(globalParameters)
    for {
      _ <- upsertList(quotedGlobalParameters.variables)
    } yield quotedGlobalParameters
  }

  def upsertParameterVariable(globalParametersVariable: ParameterVariable): Future[ParameterVariable] = {
    val quotedGlobalParameters = globalParametersVariable.findAndEscapeQuotes()
    for {
      _ <- upsert(quotedGlobalParameters)
    } yield quotedGlobalParameters
  }


  def createParameterVariable(globalParametersVariable: ParameterVariable): Future[ParameterVariable] = {
    val escapedVariable = globalParametersVariable.findAndEscapeQuotes()
    log.debug(s"Creating global parameter variable ${escapedVariable.name}")
    for {
      parameter <- findByID(escapedVariable.name)
    } yield {
      if (parameter.isDefined)
        throw new ServerException(s"The global parameter variable ${escapedVariable.name} already exist")
      else {
        createAndReturn(escapedVariable)
        escapedVariable
      }
    }
  }

  def deleteGlobalParameters(): Future[Boolean] = {
    log.debug(s"Deleting global parameters")
    for {
      result <- db.run(table.delete.transactionally.asTry)
    } yield {
      result match {
        case Success(_) =>
          log.info(s"Deleted global parameters")
          true
        case Failure(e) =>
          throw e
      }
    }
  }

  def deleteGlobalParameterVariable(name: String): Future[GlobalParameters] = {
    log.debug(s"Deleting global parameter variable $name")
    for {
      _ <- deleteByID(name)
      variables <- findAll()
    } yield GlobalParameters(variables)
  }

  def find(): Future[GlobalParameters] = {
    log.debug("Finding global parameters")
    for {
      variables <- findAll()
    } yield {
      if (variables.nonEmpty)
        GlobalParameters(variables)
      else throw new ServerException(s"No global parameters found")
    }
  }

  def findGlobalParameterVariable(name: String): Future[ParameterVariable] =
    for {
      variable <- findByID(name)
    } yield variable.getOrElse(throw new ServerException(s"The global parameter variable $name doesn't exist"))


  def findAndEscapeGlobalParameters(request: GlobalParameters): GlobalParameters= {
    val escapedValues = request.variables.map(x => x.findAndEscapeQuotes())

    request.copy(variables = escapedValues)
  }

}