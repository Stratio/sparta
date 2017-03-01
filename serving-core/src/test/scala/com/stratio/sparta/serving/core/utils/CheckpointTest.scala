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

package com.stratio.sparta.serving.core.utils

import com.stratio.sparta.serving.core.constants.AppConstant
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CheckpointTest extends BaseUtilsTest with CheckpointUtils {

  val utils = spy(this)

  "PolicyUtils.deleteCheckpointPath" should {
    "delete path from HDFS when using not local mode" in {
      doReturn(false)
        .when(utils)
        .isExecutionType(getPolicyModel(), AppConstant.ConfigLocal)

      utils.deleteCheckpointPath(getPolicyModel())

      verify(utils, times(1)).deleteFromHDFS(getPolicyModel())
    }

    "delete path from local when using local mode" in {
      doReturn(true)
        .when(utils)
        .isExecutionType(getPolicyModel(), AppConstant.ConfigLocal)

      doReturn(false)
        .when(utils)
        .isHadoopEnvironmentDefined

      utils.deleteCheckpointPath(getPolicyModel())

      verify(utils, times(1)).deleteFromLocal(getPolicyModel())
    }
  }
}
