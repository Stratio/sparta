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
package com.stratio.sparta.driver.stage

import com.stratio.sparta.driver.step.RawData
import com.stratio.sparta.driver.writer.{RawDataWriterHelper, WriterOptions}
import com.stratio.sparta.sdk.pipeline.output.{Output, SaveModeEnum}
import com.stratio.sparta.serving.core.models.workflow.PhaseEnum
import com.stratio.sparta.serving.core.models.workflow.rawData.RawDataModel
import org.apache.spark.sql.Row
import org.apache.spark.streaming.dstream.DStream

trait RawDataStage extends BaseStage {
  this: ErrorPersistor =>

  def saveRawData(rawModel: Option[RawDataModel], input: DStream[Row], outputs: Seq[Output]): Unit =
    if (rawModel.isDefined) {
      val rawData = rawDataStage()

      RawDataWriterHelper.writeRawData(rawData, outputs, input)
    }

  private[driver] def rawDataStage(): RawData = {
    val errorMessage = s"Something gone wrong saving the raw data. Please re-check the policy."
    val okMessage = s"RawData: created correctly."

    generalTransformation(PhaseEnum.RawData, okMessage, errorMessage) {
      require(workflow.rawData.isDefined, "You need a raw data stage defined in your policy")
      require(workflow.rawData.get.writer.tableName.isDefined, "You need a table name defined in your raw data stage")

      createRawData(workflow.rawData.get)
    }
  }

  private[driver] def createRawData(rawDataModel: RawDataModel): RawData = {
    val okMessage = s"RawData created correctly."
    val errorMessage = s"Something gone wrong creating the RawData. Please re-check the policy."
    generalTransformation(PhaseEnum.RawData, okMessage, errorMessage) {
      RawData(
        rawDataModel.dataField,
        rawDataModel.timeField,
        WriterOptions(
          rawDataModel.writer.outputs,
          SaveModeEnum.Append,
          rawDataModel.writer.tableName,
          getAutoCalculatedFields(rawDataModel.writer.autoCalculatedFields),
          rawDataModel.writer.partitionBy,
          rawDataModel.writer.primaryKey
        ),
        rawDataModel.configuration)
    }
  }
}
