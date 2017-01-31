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
package com.stratio.sparta.serving.core.models

import com.stratio.sparta.sdk.pipeline.output.SaveModeEnum
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.policy.PhaseEnum
import org.json4s.ext.EnumNameSerializer
import org.json4s.{DefaultFormats, Formats}

/**
 * Extends this class if you need serialize / unserialize Sparta's enums in any class / object.
 */
trait SpartaSerializer {

  implicit val json4sJacksonFormats: Formats =
    DefaultFormats +
      new JsoneyStringSerializer() +
      new EnumNameSerializer(PolicyStatusEnum) +
      new EnumNameSerializer(SaveModeEnum) +
      new EnumNameSerializer(PhaseEnum)

}
