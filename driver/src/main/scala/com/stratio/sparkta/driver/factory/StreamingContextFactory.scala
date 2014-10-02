package com.stratio.sparkta.driver.factory

import com.stratio.sparkta.driver.configuration.{AggregationPoliciesConfiguration, GeneralConfiguration}
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.driver.factory.PropertyValidationHandler._
import com.stratio.sparkta.driver.factory.SparkConfHandler._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming._
import org.apache.spark.streaming.flume.FlumeUtils
import org.apache.spark.streaming.kafka.KafkaUtils

/**
 * Builder used to transform a configuration file into a initialized StreamingContext.
 * It also can be used programatically.
 */
object StreamingContextFactory {

  def getStreamingContext(aggregationPoliciesConfiguration: AggregationPoliciesConfiguration, generalConfiguration: GeneralConfiguration): StreamingContext = {
    val ssc = new StreamingContext(configToSparkConf(generalConfiguration, aggregationPoliciesConfiguration.name), new Duration(generalConfiguration.duration))
    val properties = aggregationPoliciesConfiguration.receiverConfiguration
    val receiver = aggregationPoliciesConfiguration.receiver match {
      case "kafka" => {
        KafkaUtils.createStream(ssc,
          validateProperty("zkQuorum", properties),
          validateProperty("groupId", properties),
          validateProperty("topics", properties).split(",").map(s => (s.trim, validateProperty("partitions", properties).toInt)).toMap,
          StorageLevel.fromString(validateProperty("storageLevel", properties))
        )
      }
      case "flume" => {
        FlumeUtils.createPollingStream(ssc, validateProperty("hostname", properties), validateProperty("port", properties).toInt)
      }
      case "socket" => {
        ssc.socketTextStream(
          validateProperty("hostname", properties),
          validateProperty("port", properties).toInt,
          StorageLevel.fromString(validateProperty("storageLevel", properties)))
      }
      case _ => {
        throw new DriverException("Receiver " + aggregationPoliciesConfiguration.receiver + " not supported.")
      }
    }
    //TODO add transformations and actions to dstream
    receiver.print()

    ssc
  }


}
