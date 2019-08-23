/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.arangodb.spark

import akka.event.slf4j.SLF4JLogging
import com.arangodb.model.DocumentImportOptions
import com.arangodb.model.DocumentImportOptions.OnDuplicate
import com.arangodb.spark.vpack.SpartaVPackUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row}

import scala.collection.JavaConverters.{seqAsJavaListConverter, _}

object SpartaArangoSpark extends SLF4JLogging {


  def saveDF(dataframe: DataFrame, collection: String, options: WriteOptions, failOnConflict: Boolean): Unit =
    saveRDD[Row](dataframe.rdd, collection, options, failOnConflict, (x: Iterator[Row]) => x.map { y => SpartaVPackUtils.rowToVPack(y) })

  private def saveRDD[T](rdd: RDD[T], collection: String, options: WriteOptions, failOnConflict: Boolean, map: Iterator[T] => Iterator[Any]): Unit = {
    val writeOptions = createWriteOptions(options, rdd.sparkContext.getConf)
    rdd.foreachPartition { p =>
      if (p.nonEmpty) {
        val arangoDB = createArangoBuilder(writeOptions).build()
        val col = arangoDB.db(writeOptions.database).collection(collection)
        val insertResult = col.insertDocuments(map(p).toList.asJava)

        if (failOnConflict) {
          val errors = insertResult.getErrors.asScala
          if (errors.nonEmpty) {
            errors.foreach(entity =>
              log.error(s"Error on insert document with message: ${entity.getErrorMessage}," +
                s" code: ${entity.getCode}" +
                s" and exception: ${entity.getException}")
            )

            throw new RuntimeException(s"Error inserting documents into ArangoDB collection $collection, review logs for more information.")
          }
        }
        arangoDB.shutdown()
      }
    }
  }

  def upsertDF(dataframe: DataFrame, collection: String, options: WriteOptions): Unit =
    spartaSaveRDD[Row](dataframe.rdd, collection, options, (x: Iterator[Row]) => x.map { y => SpartaVPackUtils.rowToVPack(y) })

  private def spartaSaveRDD[T](rdd: RDD[T], collection: String, options: WriteOptions, map: Iterator[T] => Iterator[Any]): Unit = {
    val writeOptions = createWriteOptions(options, rdd.sparkContext.getConf)
    rdd.foreachPartition { p =>
      if (p.nonEmpty) {
        val arangoDB = createArangoBuilder(writeOptions).build()
        val col = arangoDB.db(writeOptions.database).collection(collection)
        val documents = map(p).toList.asJava
        val res = col.importDocuments(documents, new DocumentImportOptions().onDuplicate(OnDuplicate.update))
        arangoDB.shutdown()
      }
    }
  }

}

case class SpartaArangoSparkOptions()
