/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import java.util.UUID

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.parameters.{ParameterList, ParameterListFromWorkflow, ParameterVariable}
import org.apache.curator.framework.CuratorFramework
import org.joda.time.DateTime
import org.json4s.jackson.Serialization._
import com.stratio.sparta.core.properties.ValidatingPropertyMap._

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

class ParameterListService(curatorFramework: CuratorFramework) extends SpartaSerializer with SLF4JLogging {

  def findByName(name: String): Try[ParameterList] =
    Try {
      val parameterListPath = s"${AppConstant.ParameterListZkPath}/$name"

      if (CuratorFactoryHolder.existsPath(parameterListPath))
        read[ParameterList](new String(curatorFramework.getData.forPath(parameterListPath)))
      else throw new ServerException(s"No parameter list with name $name")
    }

  def findByParent(parent: String): Try[Seq[ParameterList]] =
    findAll().map(parameterLists =>
      parameterLists.filter(parameterList => parameterList.parent.contains(parent))
    )

  def findAll(): Try[Seq[ParameterList]] =
    Try {
      val parameterListPath = s"${AppConstant.ParameterListZkPath}"

      if (CuratorFactoryHolder.existsPath(parameterListPath)) {
        val children = curatorFramework.getChildren.forPath(parameterListPath)
        val parameterLists = JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[ParameterList](new String(curatorFramework.getData.forPath(
            s"${AppConstant.ParameterListZkPath}/$element")))
        )
        parameterLists
      } else Seq.empty[ParameterList]
    }

  def create(parameterList: ParameterList): Try[ParameterList] = {
    val parameterListPath = s"${AppConstant.ParameterListZkPath}/${parameterList.name}"

    if (CuratorFactoryHolder.existsPath(parameterListPath)) {
      update(parameterList)
    } else {
      Try {
        val parameterListWithFields = addId(addCreationDate(parameterList))
        log.info(s"Creating parameterList with name: ${parameterListWithFields.name}")
        curatorFramework.create.creatingParentsIfNeeded.forPath(
          parameterListPath, write(parameterListWithFields).getBytes
        )
        parameterListWithFields
      }
    }
  }

  def createFromWorkflow(parameterListFromWorkflow: ParameterListFromWorkflow): Try[ParameterList] = {
    Try{
      val parametersInWorkflow = WorkflowService.getParametersUsed(parameterListFromWorkflow.workflow)
      val parametersVariables = parametersInWorkflow.flatMap{ parameter =>
        if(!parameter.contains("env."))
          Option(ParameterVariable(parameter))
        else if(parameterListFromWorkflow.includeEnvironmentVariables)
          Option(ParameterVariable(parameter))
        else None
      }
      val newName = parameterListFromWorkflow.name.getOrElse{
        val dateCreation = new DateTime()
        s"AutoCreatedList_${parameterListFromWorkflow.workflow.name}_" +
          s"${dateCreation.toString("yyyy-MM-dd-HH:mm:ss:SSS")}"
      }
      val newParameterList = ParameterList(
        name = newName,
        description = parameterListFromWorkflow.description,
        tags = parameterListFromWorkflow.tags,
        parameters = parametersVariables
      )

      create(newParameterList) match {
        case Success(newParamList) => newParamList
        case Failure(e) => throw e
      }
    }
  }

  def update(parameterList: ParameterList): Try[ParameterList] = {
    Try {
      val parameterListPath = s"${AppConstant.ParameterListZkPath}/${parameterList.name}"

      if (CuratorFactoryHolder.existsPath(parameterListPath)) {
        val parameterListWithFields = addUpdateDate(parameterList)
        curatorFramework.setData().forPath(parameterListPath, write(parameterListWithFields).getBytes)
        parameterListWithFields
      } else create(parameterList).getOrElse(throw new ServerException(
        s"Unable to create parameterList with name: ${parameterList.name}."))
    }
  }

  def deleteByName(name: String): Try[Unit] =
    Try {
      val parameterListPath = s"${AppConstant.ParameterListZkPath}/$name"

      if (CuratorFactoryHolder.existsPath(parameterListPath)) {
        log.info(s"Deleting parameterList with name: $name")
        curatorFramework.delete().forPath(parameterListPath)
      } else throw new ServerException(s"No parameterList with name $name")
    }

  def deleteAll(): Try[Unit] =
    Try {
      val parameterListPath = s"${AppConstant.ParameterListZkPath}"

      if (CuratorFactoryHolder.existsPath(parameterListPath)) {
        val children = curatorFramework.getChildren.forPath(parameterListPath)

        JavaConversions.asScalaBuffer(children).toList.foreach(element =>
          curatorFramework.delete().forPath(s"${AppConstant.ParameterListZkPath}/$element"))
      }
    }

  private[services] def addId(parameterList: ParameterList): ParameterList =
    if (parameterList.id.notBlank.isEmpty || parameterList.id.notBlank.isDefined)
      parameterList.copy(id = Some(UUID.randomUUID.toString))
    else parameterList

  private[services] def addCreationDate(parameterList: ParameterList): ParameterList =
    parameterList.creationDate match {
      case None => parameterList.copy(creationDate = Some(new DateTime()))
      case Some(_) => parameterList
    }

  private[services] def addUpdateDate(parameterList: ParameterList): ParameterList =
    parameterList.copy(lastUpdateDate = Some(new DateTime()))
}
