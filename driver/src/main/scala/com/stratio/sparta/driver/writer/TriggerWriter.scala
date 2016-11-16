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
import com.stratio.sparta.driver.trigger.Trigger
import com.stratio.sparta.sdk.{Output, TableSchema}
import org.apache.spark.sql.DataFrame
import com.stratio.sparta.driver.exception.DriverException

import scala.util.{Failure, Success, Try}

trait TriggerWriter extends DataFrameModifier with SLF4JLogging {

  //scalastyle:off
  def writeTriggers(dataFrame: DataFrame,
                    triggers: Seq[Trigger],
                    inputTableName: String,
                    tableSchemas: Seq[TableSchema],
                    outputs: Seq[Output]) : Unit = {
    val sqlContext = dataFrame.sqlContext

    dataFrame.registerTempTable(inputTableName)

    val tempTables = triggers.flatMap(trigger => {
      val queryDataFrame = Try(sqlContext.sql(trigger.sql)) match {
        case Success(sqlResult) => sqlResult
        case Failure(exception: org.apache.spark.sql.AnalysisException) =>
          log.warn("Warning running analysis in Catalyst in the query ${trigger.sql} in trigger ${trigger.name}",
            exception.message)
          throw DriverException(exception.getMessage, exception)
        case Failure(exception) =>
          log.warn(s"Warning running sql in the query ${trigger.sql} in trigger ${trigger.name}", exception.getMessage)
          throw DriverException(exception.getMessage, exception)
      }
      val tableSchema = tableSchemas.find(_.tableName == trigger.name)
      val upsertOptions = tableSchema.fold(Map.empty[String, String]) { schema =>
        Map(Output.TableNameKey -> schema.tableName)
      }

      if(queryDataFrame.take(1).length > 0) {
        val queryDataFrameWithAutoCalculatedFields = tableSchema match {
          case Some(schema) => applyAutoCalculateFields(queryDataFrame, schema.autoCalculateFields)
          case None => queryDataFrame
        }

        queryDataFrameWithAutoCalculatedFields.registerTempTable(trigger.name)
        trigger.outputs.foreach(outputName =>
          outputs.find(output => output.name == outputName) match {
            case Some(outputWriter) => Try{
              outputWriter.upsert(queryDataFrameWithAutoCalculatedFields, upsertOptions)
            } match {
              case Success(_) =>
                log.debug(s"Trigger data stored in ${trigger.name}")
              case Failure(e) =>
                log.error(s"Something goes wrong. Table: ${trigger.name}")
                log.error(s"Schema. ${queryDataFrameWithAutoCalculatedFields.schema}")
                log.error(s"Head element. ${queryDataFrameWithAutoCalculatedFields.head}")
                log.error(s"Error message : ${e.getMessage}")
            }
            case None => log.error(s"The output in the trigger : $outputName not match in the outputs")
          })
        Option(trigger.name)
      } else None
    })
    tempTables.foreach(tableName => if(isCorrectTableName(tableName)) sqlContext.dropTempTable(tableName))
    if(isCorrectTableName(inputTableName)) sqlContext.dropTempTable(inputTableName)
  }
//scalastyle:on
  private def isCorrectTableName(tableName : String) : Boolean =
    tableName.nonEmpty && tableName != "" && tableName.toLowerCase != "select" && tableName.toLowerCase != "project"
}
