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

package com.stratio.sparta.serving.core.helpers

import com.stratio.sparta.serving.core.models.workflow.WorkflowModel
import com.stratio.sparta.serving.core.models.workflow.fragment.FragmentType.`type`
import com.stratio.sparta.serving.core.models.workflow.fragment.{FragmentElementModel, FragmentType}


object FragmentsHelper {

  def populateFragmentFromPolicy(policy: WorkflowModel, fragmentType: `type`): Seq[FragmentElementModel] =
    fragmentType match {
      case FragmentType.input =>
        policy.input match {
          case Some(in) => Seq(in.parseToFragment(fragmentType))
          case None => Seq.empty[FragmentElementModel]
        }
      case FragmentType.output =>
        policy.outputs.map(output => output.parseToFragment(fragmentType))
    }
}
