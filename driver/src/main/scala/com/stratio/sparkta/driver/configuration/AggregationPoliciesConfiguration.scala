package com.stratio.sparkta.driver.configuration

/**
 * Created by ajnavarro on 2/10/14.
 */
case class AggregationPoliciesConfiguration(name: String = "default",
                                            receiver: String = "kafka",
                                            receiverConfiguration: Map[String, String],
                                            eventParser: Option[String],
                                            persistence: Option[String]
                                             )
