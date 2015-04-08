package com.stratio.sparkta.plugin.output.redis.dao

import java.io.Closeable

import com.redis.RedisClient
import org.apache.spark.streaming.dstream.DStream


/**
 * Created by anistal on 4/8/15.
 */
trait AbstractRedisDAO {

  def hostname : String
  def dbName : String
  def eventTimeFieldName = "eventTime"
  def idFieldName = "_id"
  def idSeparator = "__"

  protected def client: RedisClient = AbstractRedisDAO.client(hostname)

  def insert(dbName: String, collName: String, events: Iterator[DStream]): Unit = {
    
  }
}

private object AbstractRedisDAO {

  private def client(clientUri: String): RedisClient = {
    val client = new RedisClient(clientUri, 6379)
    return client
  }


}
