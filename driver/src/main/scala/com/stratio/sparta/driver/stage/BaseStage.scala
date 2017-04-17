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
import com.stratio.sparta.driver.utils.StageUtils
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum.NotDefined
import com.stratio.sparta.serving.core.models.policy.{PhaseEnum, PolicyErrorModel, PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.utils.PolicyStatusUtils
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}


trait ErrorPersistor {
  def persistError(error: PolicyErrorModel): Unit
}

trait ZooKeeperError extends ErrorPersistor with PolicyStatusUtils {

  val curatorFramework: CuratorFramework

  def policy: PolicyModel

  def persistError(error: PolicyErrorModel): Unit =
    updateStatus(PolicyStatusModel(policy.id.get, NotDefined, None, None, lastError = Some(error)))

  def clearError(): Unit =
    clearLastError(policy.id.get)
}

trait LogError extends ErrorPersistor with SLF4JLogging {
  def persistError(error: PolicyErrorModel): Unit = log.error(s"This error was not saved to ZK : $error")
}

trait BaseStage extends SLF4JLogging with StageUtils {
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

  def logAndCreateEx(code: PhaseEnum.Value,
                      ex: Throwable,
                      policy: PolicyModel,
                      message: String
                    ): IllegalArgumentException = {
    val originalMsg = ex.getCause match {
      case _: ClassNotFoundException => "The component couldn't be found in classpath. Please check the type."
      case exception: Throwable => exception.toString
      case _ => ex.toString
    }
    val policyError = PolicyErrorModel(message, code, originalMsg)
    log.error("An error was detected : {}", policyError)
    Try {
      persistError(policyError)
    } recover {
      case e => log.error(s"Error while persisting error: $policyError", e)
    }
    new IllegalArgumentException(message, ex)
  }

}
