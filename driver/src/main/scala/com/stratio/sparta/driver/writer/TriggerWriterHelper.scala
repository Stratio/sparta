/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.sparta.driver.writer

import akka.event.slf4j.SLF4JLogging
import org.apache.spark.sql.{DataFrame, Row}
import com.stratio.sparta.driver.exception.DriverException
import com.stratio.sparta.driver.factory.SparkContextFactory
import com.stratio.sparta.driver.schema.SchemaHelper
import com.stratio.sparta.driver.step.Trigger
import com.stratio.sparta.sdk.pipeline.output.Output
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.dstream.DStream

import scala.util.{Failure, Success, Try}

object TriggerWriterHelper extends SLF4JLogging {

  def writeStream(triggers: Seq[Trigger],
                  inputTableName: String,
                  outputs: Seq[Output],
                  streamData: DStream[Row],
                  schema: StructType): Unit = {
    streamData.foreachRDD(rdd => {
      val parsedDataFrame = SparkContextFactory.sparkSessionInstance.createDataFrame(rdd, schema)

      writeTriggers(parsedDataFrame, triggers, inputTableName, outputs)
    })
  }

  //scalastyle:off
  def writeTriggers(dataFrame: DataFrame,
                    triggers: Seq[Trigger],
                    inputTableName: String,
                    outputs: Seq[Output]): Unit = {
    val sparkSession = dataFrame.sparkSession
    if (triggers.nonEmpty && isCorrectTableName(inputTableName)) {
      if (!sparkSession.catalog.tableExists(inputTableName)) {
        dataFrame.createOrReplaceTempView(inputTableName)
        log.debug(s"Registering temporal table in Spark with name: $inputTableName")
      }
      val tempTables = triggers.flatMap(trigger => {
        log.debug(s"Executing query in Spark: ${trigger.sql}")
        val queryDf = Try(sparkSession.sql(trigger.sql)) match {
          case Success(sqlResult) => sqlResult
          case Failure(exception: org.apache.spark.sql.AnalysisException) =>
            log.warn("Warning running analysis in Catalyst in the query ${trigger.sql} in trigger ${trigger.name}",
              exception.message)
            throw DriverException(exception.getMessage, exception)
          case Failure(exception) =>
            log.warn(s"Warning running sql in the query ${trigger.sql} in trigger ${trigger.name}", exception.getMessage)
            throw DriverException(exception.getMessage, exception)
        }
        val extraOptions = Map(Output.TableNameKey -> trigger.name)

        if (!queryDf.rdd.isEmpty()) {
          val autoCalculatedFieldsDf = WriterHelper.write(queryDf, trigger.writerOptions, extraOptions, outputs)
          if (isCorrectTableName(trigger.name) && !sparkSession.catalog.tableExists(trigger.name)) {
            autoCalculatedFieldsDf.createOrReplaceTempView(trigger.name)
            log.debug(s"Registering temporal table in Spark with name: ${trigger.name}")
          }
          else log.warn(s"The trigger ${trigger.name} have incorrect name, is impossible to register as temporal table")

          Option(trigger.name)
        } else None
      })
      tempTables.foreach(tableName =>
        if (isCorrectTableName(tableName) && sparkSession.catalog.tableExists(tableName)) {
          sparkSession.catalog.dropTempView(tableName)
          log.debug(s"Dropping temporal table in Spark with name: $tableName")
        } else log.debug(s"Impossible to drop table in Spark with name: $tableName"))

      if (isCorrectTableName(inputTableName) && sparkSession.catalog.tableExists(inputTableName)) {
        sparkSession.catalog.dropTempView(inputTableName)
        log.debug(s"Dropping temporal table in Spark with name: $inputTableName")
      } else log.debug(s"Impossible to drop table in Spark: $inputTableName")
    } else {
      if (triggers.nonEmpty && !isCorrectTableName(inputTableName))
        log.warn(s"Incorrect table name $inputTableName and the triggers could have errors and not have been " +
          s"executed")
    }
  }

  //scalastyle:on

  private[driver] def isCorrectTableName(tableName: String): Boolean =
    tableName.nonEmpty && tableName != "" &&
      tableName.toLowerCase != "select" &&
      tableName.toLowerCase != "project" &&
      !tableName.contains("-") && !tableName.contains("*") && !tableName.contains("/")
}
