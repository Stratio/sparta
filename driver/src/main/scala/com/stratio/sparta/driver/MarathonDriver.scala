/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.driver

import akka.actor.{ActorSystem, Props}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.helpers.{AggregationTimeHelper, ExceptionHelper}
import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.driver.actor.MarathonAppActor
import com.stratio.sparta.driver.actor.MarathonAppActor.StartApp
import com.stratio.sparta.serving.core.actor._
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.MarathonConstant._
import com.stratio.sparta.serving.core.exception.DriverException
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow.{ExecutionStatus, ExecutionStatusUpdate}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Properties, Success, Try}

//scalastyle:off
object MarathonDriver extends SLF4JLogging with SpartaSerializer {

  val NumberOfArguments = 1
  val ExecutionIdIndex = 0

  def main(args: Array[String]): Unit = {
    Try {
      assert(args.length == NumberOfArguments,
        s"Invalid number of arguments: ${args.length}, args: $args, expected: $NumberOfArguments")
      log.debug(s"Arguments: ${args.mkString(", ")}")
      val executionId = args(ExecutionIdIndex)
      val system = ActorSystem("MarathonApp")
      val executionService = PostgresDaoFactory.executionPgService
      val execution = Await.result(executionService.findExecutionById(executionId), Duration.Inf)

      Try {
        val executionStatusListenerActor = system.actorOf(Props(new ExecutionStatusChangeMarathonListenerActor()))
        system.actorOf(Props(new ExecutionStatusChangeMarathonPublisherActor()))
        val marathonAppActor = system.actorOf(Props(
          new MarathonAppActor(executionStatusListenerActor)), MarathonAppActorName)

        marathonAppActor ! StartApp(execution)
      } match {
        case Success(_) =>
          log.debug(s"The execution $executionId sent to MarathonAppActor")
        case Failure(exception) =>
          val information = s"Error initiating workflow app environment in Marathon driver"
          Try {
            val error = WorkflowError(
              information,
              PhaseEnum.Launch,
              exception.toString,
              ExceptionHelper.toPrintableException(exception)
            )
            val executionWithError = executionService.updateStatus(ExecutionStatusUpdate(
              executionId,
              ExecutionStatus(state = Failed, statusInfo = Option(information))
            ), error)
            log.debug(s"Updated correctly the execution status $executionId to $Failed in Marathon Driver")
            executionWithError
          } match {
            case Success(_) =>
              Thread.sleep(5000)
              throw DriverException(information, exception)
            case Failure(_) =>
              Thread.sleep(5000)
              throw DriverException(information + ". Error updating finish status in Marathon driver.", exception)
          }
      }
    } match {
      case Success(_) =>
        log.info("Workflow App environment started")
      case Failure(driverException: DriverException) =>
        log.error(driverException.msg, driverException.getCause)
        Thread.sleep(millisecondsToWaitBeforeError)
        throw driverException
      case Failure(exception) =>
        log.error(s"An error was encountered while starting the Workflow App environment", exception)
        Thread.sleep(millisecondsToWaitBeforeError)
        throw exception
    }
  }

  def millisecondsToWaitBeforeError: Int =
   Try(Properties.envOrNone(SpartaMarathonTotalTimeBeforeKill).get.toInt).toOption
      .getOrElse(AppConstant.DefaultAwaitWorkflowChangeStatusSeconds) * 1000 // s -> ms

}
