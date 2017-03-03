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

package com.stratio.sparta.serving.core.utils

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.submit.SubmitRequest
import com.stratio.sparta.serving.core.models.{ErrorModel, SpartaSerializer}
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util.Try

trait ExecutionUtils extends SpartaSerializer with SLF4JLogging {

  val curatorFramework: CuratorFramework

  def createRequest(request: SubmitRequest): Try[SubmitRequest] = {
    val requestPath = s"${AppConstant.ExecutionsPath}/${request.id}"
    if (CuratorFactoryHolder.existsPath(requestPath)) {
      updateRequest(request)
    } else {
      Try {
        log.info(s"Creating execution with id ${request.id}")
        curatorFramework.create.creatingParentsIfNeeded.forPath(requestPath, write(request).getBytes)
        request
      }
    }
  }

  def updateRequest(request: SubmitRequest): Try[SubmitRequest] = {
    Try {
      val requestPath = s"${AppConstant.ExecutionsPath}/${request.id}"
      if (CuratorFactoryHolder.existsPath(requestPath)) {
        curatorFramework.setData().forPath(requestPath, write(request).getBytes)
        request
      } else createRequest(request).getOrElse(throw new ServingCoreException(
        ErrorModel.toString(new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId,
          s"Is not possible to create execution with id ${request.id}."))))
    }
  }

  def findAllRequests(): Try[Seq[SubmitRequest]] =
    Try {
      val requestPath = s"${AppConstant.ExecutionsPath}"
      if (CuratorFactoryHolder.existsPath(requestPath)) {
        val children = curatorFramework.getChildren.forPath(requestPath)
        val policiesRequest = JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[SubmitRequest](new String(curatorFramework.getData.forPath(s"${AppConstant.ExecutionsPath}/$element")))
        )
        policiesRequest
      } else Seq.empty[SubmitRequest]
    }

  def findRequestById(id: String): Try[SubmitRequest] =
    Try {
      val requestPath = s"${AppConstant.ExecutionsPath}/$id"
      if (CuratorFactoryHolder.existsPath(requestPath))
        read[SubmitRequest](new String(curatorFramework.getData.forPath(requestPath)))
      else throw new ServingCoreException(
        ErrorModel.toString(new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No execution context with id $id")))
    }

  def deleteAllRequests(): Try[_] =
    Try {
      val requestPath = s"${AppConstant.ExecutionsPath}"
      if (CuratorFactoryHolder.existsPath(requestPath)) {
        val children = curatorFramework.getChildren.forPath(requestPath)
        val policiesRequest = JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[SubmitRequest](new String(curatorFramework.getData.forPath(s"${AppConstant.ExecutionsPath}/$element")))
        )

        policiesRequest.foreach(request => deleteRequest(request.id))
      }
    }

  def deleteRequest(id: String): Try[_] =
    Try {
      val requestPath = s"${AppConstant.ExecutionsPath}/$id"
      if (CuratorFactoryHolder.existsPath(requestPath)) {
        log.info(s"Deleting execution with id $id")
        curatorFramework.delete().forPath(requestPath)
      } else throw new ServingCoreException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No execution with id $id")))
    }
}
