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
package com.stratio.sparta.driver.test.stage

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.stratio.sparta.driver.stage.{LogError, RawDataStage}
import com.stratio.sparta.sdk.pipeline.autoCalculations.AutoCalculatedField
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.models.workflow.writer.{AutoCalculatedFieldModel, WriterModel}
import com.stratio.sparta.serving.core.models.workflow.WorkflowModel
import com.stratio.sparta.serving.core.models.workflow.rawData.RawDataModel
import org.junit.runner.RunWith
import org.mockito.Mockito.when
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpecLike, ShouldMatchers}

@RunWith(classOf[JUnitRunner])
class RawStageTest
  extends TestKit(ActorSystem("RawStageTest"))
    with FlatSpecLike with ShouldMatchers with MockitoSugar {

  case class TestRawData(workflow: WorkflowModel) extends RawDataStage with LogError

  def mockPolicy: WorkflowModel = {
    val policy = mock[WorkflowModel]
    when(policy.id).thenReturn(Some("id"))
    policy
  }

  "rawDataStage" should "Generate a raw data" in {
    val field = "field"
    val timeField = "time"
    val tableName = Some("table")
    val outputs = Seq("output")
    val partitionBy = Some("field")
    val autocalculateFields = Seq(AutoCalculatedFieldModel())
    val configuration = Map.empty[String, JsoneyString]

    val policy = mockPolicy
    val rawData = mock[RawDataModel]
    val writerModel = mock[WriterModel]

    when(policy.rawData).thenReturn(Some(rawData))
    when(rawData.dataField).thenReturn(field)
    when(rawData.timeField).thenReturn(timeField)
    when(rawData.writer).thenReturn(writerModel)
    when(writerModel.tableName).thenReturn(tableName)
    when(writerModel.outputs).thenReturn(outputs)
    when(writerModel.partitionBy).thenReturn(partitionBy)
    when(writerModel.autoCalculatedFields).thenReturn(autocalculateFields)
    when(rawData.configuration).thenReturn(configuration)

    val result = TestRawData(policy).rawDataStage()

    result.timeField should be(timeField)
    result.dataField should be(field)
    result.writerOptions.tableName should be(tableName)
    result.writerOptions.partitionBy should be(partitionBy)
    result.configuration should be(configuration)
    result.writerOptions.outputs should be(outputs)
  }

  "rawDataStage" should "Fail with bad table name" in {
    val field = "field"
    val timeField = "time"
    val tableName = None
    val outputs = Seq("output")
    val partitionBy = Some("field")
    val configuration = Map.empty[String, JsoneyString]

    val policy = mockPolicy
    val rawData = mock[RawDataModel]
    val writerModel = mock[WriterModel]

    when(policy.rawData).thenReturn(Some(rawData))
    when(rawData.dataField).thenReturn(field)
    when(rawData.timeField).thenReturn(timeField)
    when(rawData.writer).thenReturn(writerModel)
    when(writerModel.tableName).thenReturn(tableName)
    when(writerModel.outputs).thenReturn(outputs)
    when(writerModel.partitionBy).thenReturn(partitionBy)
    when(rawData.configuration).thenReturn(configuration)


    the[IllegalArgumentException] thrownBy {
      TestRawData(policy).rawDataStage()
    } should have message "Something gone wrong saving the raw data. Please re-check the policy."
  }

}
