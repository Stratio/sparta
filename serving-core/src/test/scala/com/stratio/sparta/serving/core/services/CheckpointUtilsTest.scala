/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.utils.CheckpointUtils
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import com.stratio.sparta.serving.core.constants.AppConstant.DefaultGroup



@RunWith(classOf[JUnitRunner])
class CheckpointUtilsTest extends WordSpec with ShouldMatchers with CheckpointUtils {
  val emptyPipeGraph = PipelineGraph(Seq.empty[NodeGraph], Seq.empty[EdgeGraph])

  val checkpointSettingsMocked = CheckpointSettings(JsoneyString("sparta/checkpoint"),
    enableCheckpointing = true,
    autoDeleteCheckpoint = true,
    addTimeToCheckpointPath = false,
    keepSameCheckpoint = Option(true))

  val mockedSettings = Settings(streamingSettings = StreamingSettings(checkpointSettings = checkpointSettingsMocked))

  val mockWf = Workflow(
    id = Option("1234"),
    settings = mockedSettings,
    name = "test",
    description = "whatever",
    executionId = Option("execution1"),
    pipelineGraph = emptyPipeGraph
  )


  "CheckpointUtils.checkpointPathFromWorkflow" should {
    "retrieve a correct path without executionID from the settings" in {

      val actualPath = checkpointPathFromWorkflow(mockWf)
      val expectedPath = s"sparta/checkpoint${DefaultGroup.name}/test/1234"
      actualPath shouldBe expectedPath
    }

    "retrieve a correct path with executionID from the settings" in {

      val checkpointSettingsMocked = CheckpointSettings(JsoneyString("sparta/checkpoint"),
        enableCheckpointing = true,
        autoDeleteCheckpoint = true,
        addTimeToCheckpointPath = false,
        keepSameCheckpoint = Option(false))

      val mockedSettings = Settings(streamingSettings = StreamingSettings(checkpointSettings = checkpointSettingsMocked))

      val mockWf = Workflow(
        id = Option("1234"),
        settings = mockedSettings,
        name = "test",
        description = "whatever",
        executionId = Option("execution1"),
        pipelineGraph = emptyPipeGraph
      )

      val actualPath = checkpointPathFromWorkflow(mockWf)
      val expectedPath = s"sparta/checkpoint${DefaultGroup.name}/test/1234/execution1"
      actualPath shouldBe expectedPath
    }
  }
}