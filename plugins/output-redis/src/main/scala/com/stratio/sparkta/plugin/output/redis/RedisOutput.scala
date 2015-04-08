package com.stratio.sparkta.plugin.output.redis

import java.io.Serializable

import com.stratio.sparkta.plugin.output.redis.dao.AbstractRedisDAO
import com.stratio.sparkta.sdk.WriteOp._
import com.stratio.sparkta.sdk.{UpdateMetricOperation, Multiplexer, Output}
import org.apache.spark.Logging
import org.apache.spark.streaming.dstream.DStream
import collection.JavaConversions._

/**
 * Created by anistal on 4/8/15.
 */
class RedisOutput(properties: Map[String, Serializable], schema : Map[String,WriteOp])
  extends Output(properties, schema) with AbstractRedisDAO with Multiplexer with Serializable with Logging {

  override val dbName = properties.get("dbName").getOrElse("sparkta")

  override val hostname = properties.get("hostname").getOrElse("hostname")

  override def supportedWriteOps: Seq[WriteOp] = ???

  override def persist(stream: DStream[UpdateMetricOperation]): Unit = ???

  override def timeDimension: String = ???

  override def multiplexer: Boolean = ???

  override def multiplexStream(stream: DStream[UpdateMetricOperation]): DStream[UpdateMetricOperation] = ???

  override def multiplexStream(stream: DStream[UpdateMetricOperation], fixedDimension: String): DStream[UpdateMetricOperation] = ???
}
