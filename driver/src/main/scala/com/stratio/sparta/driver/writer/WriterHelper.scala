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
import com.stratio.sparta.driver.schema.SchemaHelper
import com.stratio.sparta.sdk.pipeline.output.Output
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.StructType

import scala.util.{Failure, Success, Try}

object WriterHelper extends SLF4JLogging {

  def write(dataFrame: DataFrame,
            writerOptions: WriterOptions,
            extraSaveOptions: Map[String, String],
            outputs: Seq[Output]): DataFrame = {
    val saveOptions = extraSaveOptions ++
      writerOptions.tableName.fold(Map.empty[String, String]) { outputTableName =>
        Map(Output.TableNameKey -> outputTableName)
      } ++
      writerOptions.partitionBy.fold(Map.empty[String, String]) { partition =>
        Map(Output.PartitionByKey -> partition)
      } ++
      writerOptions.primaryKey.fold(Map.empty[String, String]) { key =>
        Map(Output.PrimaryKey -> key)
      }
    val outputTableName = saveOptions.getOrElse(Output.TableNameKey, "undefined")
    val autoCalculatedFieldsDf = DataFrameModifierHelper.applyAutoCalculateFields(dataFrame,
        writerOptions.autoCalculateFields,
        StructType(dataFrame.schema.fields ++ SchemaHelper.getStreamWriterPkFieldsMetadata(writerOptions.primaryKey)))

    writerOptions.outputs.foreach(outputName =>
      outputs.find(output => output.name == outputName) match {
        case Some(outputWriter) => Try {
          outputWriter.save(autoCalculatedFieldsDf, writerOptions.saveMode, saveOptions)
        } match {
          case Success(_) =>
            log.debug(s"Data stored in $outputTableName")
          case Failure(e) =>
            log.error(s"Something goes wrong. Table: $outputTableName")
            log.error(s"Schema. ${autoCalculatedFieldsDf.schema}")
            log.error(s"Head element. ${autoCalculatedFieldsDf.head}")
            log.error(s"Error message : ${e.getMessage}")
        }
        case None => log.error(s"The output added : $outputName not match in the outputs")
      })
    autoCalculatedFieldsDf
  }

}
