package com.stratio.sparkta.driver.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparkta.driver.configuration.GeneralConfiguration
import com.stratio.sparkta.driver.dto.AggregationPoliciesDto
import grizzled.slf4j.Logger

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by ajnavarro on 3/10/14.
 */
case object Print

class SupervisorActor extends Actor with ActorLogging {

  private val logger = Logger[this.type]

  private var contextActors: Seq[ActorRef] = Seq()

  override def receive = {
    case x: (AggregationPoliciesDto, GeneralConfiguration) =>
      var streamingContextActor = context.actorOf(Props(new StreamingContextActor(x._1, x._2)))
      contextActors :+= streamingContextActor
      streamingContextActor ! Init
    case Print =>
      implicit val timeout = Timeout(10 seconds)
      contextActors.map(actor => {
        val future = actor ? Status
        val result = Await.result(future, timeout.duration).asInstanceOf[String]
        logger.info(result)
      })
  }
}
