/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.output.kafka

import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.spark.sql.Row

import scala.util.Try


/**
  * Adapted from [[KafkaWriteTask]]
  */
class KafkaWriteTask(
                      var producer: KafkaProducer[String, Row],
                      tableName: String,
                      partitionKey: Option[String],
                      keySeparator: String
                    ) {

  // used to synchronize with Kafka callbacks
  @volatile private var failedWrite: Exception = null

  /**
    * Writes key value data out to topics.
    */
  def execute(iterator: Iterator[Row]): Unit =
    while (iterator.hasNext && failedWrite == null) {
      val row = iterator.next()

      val recordToSend = partitionKey.map(_ => extractKeyValues(row, partitionKey))
        .map(new ProducerRecord[String, Row](tableName, _, row))
        .getOrElse(new ProducerRecord[String, Row](tableName, row))

      val callback = new Callback() {
        override def onCompletion(recordMetadata: RecordMetadata, e: Exception): Unit = {
          if (failedWrite == null && e != null) {
            failedWrite = e
          }
        }
      }
      producer.send(recordToSend, callback)
    }

  def close(): Unit = {
    checkForErrors()
    if (producer != null) {
      producer.flush()
      checkForErrors()
      producer = null
    }
  }

  private def checkForErrors(): Unit = {
    if (failedWrite != null) {
      throw failedWrite
    }
  }

  private[kafka] def extractKeyValues(row: Row, partitionKey: Option[String]): String =
    partitionKey.get.split(",").flatMap { key =>
      Try(row.get(row.fieldIndex(key)).toString).toOption
    }.mkString(keySeparator)


}