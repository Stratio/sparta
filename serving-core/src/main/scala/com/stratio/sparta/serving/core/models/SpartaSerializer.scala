/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models


import akka.actor.{ActorRef, ActorSystem}
import akka.event.slf4j.SLF4JLogging
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparta.sdk.properties.{EnvironmentContext, JsoneyStringSerializer}
import com.stratio.sparta.sdk.workflow.enumerators._
import com.stratio.sparta.serving.core.actor.EnvironmentListenerActor.GetEnvironment
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.models.enumerators._
import com.stratio.sparta.serving.core.models.workflow.PhaseEnum
import org.json4s.ext.{DateTimeSerializer, EnumNameSerializer}
import org.json4s.{DefaultFormats, Formats}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
  * Extends this interface if you need serialize / unserialize Sparta's enums in any class / object.
  */
trait SpartaSerializer {

  val serializerSystem: Option[ActorSystem] = None
  val environmentStateActor: Option[ActorRef] = None

  implicit def json4sJacksonFormats: Formats = {
    val environmentContext = (serializerSystem, environmentStateActor) match {
      case (Some(system), Some(envStateActor)) =>
        SpartaSerializer.getEnvironmentContext(system, envStateActor)
      case _ => None
    }

    DefaultFormats + DateTimeSerializer +
      new JsoneyStringSerializer(environmentContext) +
      new EnumNameSerializer(WorkflowStatusEnum) +
      new EnumNameSerializer(NodeArityEnum) +
      new EnumNameSerializer(ArityValueEnum) +
      new EnumNameSerializer(SaveModeEnum) +
      new EnumNameSerializer(InputFormatEnum) +
      new EnumNameSerializer(OutputFormatEnum) +
      new EnumNameSerializer(WhenError) +
      new EnumNameSerializer(WhenRowError) +
      new EnumNameSerializer(WhenFieldError) +
      new EnumNameSerializer(WorkflowExecutionEngine) +
      new EnumNameSerializer(WorkflowExecutionMode) +
      new EnumNameSerializer(DataType) +
      new EnumNameSerializer(PhaseEnum)
  }

}

object SpartaSerializer extends SLF4JLogging {

  private var environmentContext: Option[EnvironmentContext] = None

  def getEnvironmentContext(actorSystem: ActorSystem, envStateActor: ActorRef): Option[EnvironmentContext] = {
    implicit val system: ActorSystem = actorSystem
    implicit val timeout: Timeout = Timeout(Try(SpartaConfig.getDetailConfig.get.getInt("serializationTimeout"))
        .getOrElse(DefaultSerializationTimeout).milliseconds)

    Try {
      val future = envStateActor ? GetEnvironment
      Await.result(future, timeout.duration).asInstanceOf[Map[String, String]]
    } match {
      case Success(newEnvironment) =>
        environmentContext = Option(EnvironmentContext(newEnvironment))
        environmentContext
      case Failure(e) =>
        log.warn(s"No environment result, returning the last value. ${e.getLocalizedMessage}")
        environmentContext
    }
  }

}
