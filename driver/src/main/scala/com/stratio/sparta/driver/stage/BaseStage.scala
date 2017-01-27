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
package com.stratio.sparta.driver.stage

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.dao.ErrorDAO
import com.stratio.sparta.serving.core.models.policy.PolicyModel
import com.stratio.sparta.serving.core.models.{PhaseEnum, PolicyErrorModel}

import scala.util.{Failure, Success, Try}


trait ErrorPersistor {
  def persistError(error: PolicyErrorModel): PolicyErrorModel
}

trait ZooKeeperError extends ErrorPersistor with SLF4JLogging {
  def persistError(error: PolicyErrorModel): PolicyErrorModel = ErrorDAO.getInstance.upsert(error)

  def clearError(id: Option[String]): Unit = {
    Try {
      id.foreach(id => {
        if (ErrorDAO.getInstance.dao.exists(id)) {
          ErrorDAO.getInstance.dao.delete(id)
        }
      })
    } recover {
      //Log the error but continue the execution
      case e => log.error(s"Error while deleting $id from ZK.", e)
    }
  }
}

trait LogError extends ErrorPersistor with SLF4JLogging {
  def persistError(error: PolicyErrorModel): PolicyErrorModel = {
    log.error(s"This error was not saved to ZK : $error")
    error
  }
}

trait BaseStage extends SLF4JLogging {
  this: ErrorPersistor =>

  def policy: PolicyModel

  def generalTransformation[T](code: PhaseEnum.Value, okMessage: String, errorMessage: String)
                              (f: => T): T = {
    Try(f) match {
      case Success(result) =>
        log.info(okMessage)
        result
      case Failure(ex) => throw logAndCreateEx(code, ex, policy, errorMessage)
    }
  }

  def logAndCreateEx(
                      code: PhaseEnum.Value,
                      ex: Throwable,
                      policy: PolicyModel,
                      message: String
                    ): IllegalArgumentException = {
    val originalMsg = ex.getCause match {
      case _: ClassNotFoundException => "The component couldn't be found in classpath. Please check the type."
      case exception: Exception => exception.toString
      case _ => "No more detail provided"
    }
    val policyError = PolicyErrorModel(policy.id.getOrElse("unknown"), message, code, originalMsg)
    log.error("An error was detected : {}", policyError)
    Try {
      persistError(policyError)
    } recover {
      case e => log.error(s"Error while persisting error: $policyError", e)
    }
    new IllegalArgumentException(message, ex)
  }

}
