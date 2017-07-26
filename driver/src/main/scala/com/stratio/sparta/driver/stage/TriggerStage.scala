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

import com.stratio.sparta.driver.step.Trigger
import com.stratio.sparta.driver.writer.{TriggerWriterHelper, WriterOptions}
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.sdk.utils.AggregationTime
import com.stratio.sparta.serving.core.models.workflow.PhaseEnum
import com.stratio.sparta.serving.core.models.workflow.trigger.TriggerModel
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.Milliseconds
import org.apache.spark.streaming.dstream.DStream

trait TriggerStage extends BaseStage {
  this: ErrorPersistor =>

  def triggersStreamStage(initSchema: StructType,
                          inputData: DStream[Row],
                          outputs: Seq[Output],
                          window: Long): Unit = {
    val triggersStage = triggerStage(workflow.streamTriggers)
    val errorMessage = s"An error was encountered while executing the triggers stream for: ${workflow.input.get.name}."
    val okMessage = s"Triggers Stream successfully executed"
    generalTransformation(PhaseEnum.TriggerStream, okMessage, errorMessage) {
      triggersStage
        .groupBy(trigger => (trigger.overLast, trigger.computeEvery))
        .foreach { case ((overLast, computeEvery), triggers) =>
          val groupedData = (overLast, computeEvery) match {
            case (None, None) => inputData
            case (Some(overL), Some(computeE))
              if (AggregationTime.parseValueToMilliSeconds(overL) == window) &&
                (AggregationTime.parseValueToMilliSeconds(computeE) == window) => inputData
            case _ => inputData.window(
              Milliseconds(
                overLast.fold(window) { over => AggregationTime.parseValueToMilliSeconds(over) }),
              Milliseconds(
                computeEvery.fold(window) { computeEvery => AggregationTime.parseValueToMilliSeconds(computeEvery) }))
          }
          TriggerWriterHelper.writeStream(triggers,
            workflow.settings.global.streamTemporalTable,
            outputs,
            groupedData, initSchema)
        }
    }
  }

  def triggerStage(triggers: Seq[TriggerModel]): Seq[Trigger] =
    triggers.map(trigger => createTrigger(trigger))

  private[driver] def createTrigger(trigger: TriggerModel): Trigger = {
    val okMessage = s"Trigger: ${trigger.name} successfully created"
    val errorMessage = s"An error was encountered while creating the trigger: ${trigger.name}. " +
      s"Please re-check the policy."
    generalTransformation(PhaseEnum.Trigger, okMessage, errorMessage) {
      Trigger(
        trigger.name,
        trigger.sql,
        trigger.overLast,
        trigger.computeEvery,
        WriterOptions(
          trigger.writer.outputs,
          trigger.writer.saveMode,
          trigger.writer.tableName,
          getAutoCalculatedFields(trigger.writer.autoCalculatedFields),
          trigger.writer.partitionBy,
          trigger.writer.primaryKey
        ),
        trigger.configuration)
    }
  }
}
