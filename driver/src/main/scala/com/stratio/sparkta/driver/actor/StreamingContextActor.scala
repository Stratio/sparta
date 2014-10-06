package com.stratio.sparkta.driver.actor

import akka.actor.{Actor, ActorLogging}
import com.stratio.sparkta.driver.configuration.GeneralConfiguration
import com.stratio.sparkta.driver.dto.AggregationPoliciesDto
import com.stratio.sparkta.driver.factory.StreamingContextFactory
import grizzled.slf4j.Logger
import org.apache.spark.streaming.StreamingContext

/**
 * Created by ajnavarro on 3/10/14.
 */
case object Init

case object Status

case object Stop

class StreamingContextActor(policy: AggregationPoliciesDto, config: GeneralConfiguration) extends Actor with ActorLogging {
  private val logger = Logger[this.type]

  private var ssc: Option[StreamingContext] = None

  def receive = {
    case Init =>
      logger.debug("Init new streamingContext with name " + policy.name)
      ssc = Option(StreamingContextFactory.getStreamingContext(policy, config))
      logger.debug("StreamingContext initialized with name " + policy.name)
    case Stop => ssc.map(_.stop(true))
      ssc = None
    case Status =>
      logger.debug("Asking status for " + policy.name)
      sender ! ssc.get.sparkContext.appName + ": " + ssc.get.sparkContext.getExecutorMemoryStatus
  }
}
