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

package com.stratio.sparta.driver.utils

import com.stratio.sparta.sdk.pipeline.autoCalculations.{FromFixedValue, _}
import com.stratio.sparta.serving.core.models.workflow.writer.AutoCalculatedFieldModel


trait StageUtils {

  private[driver] def getAutoCalculatedFields(autoCalculatedFields: Seq[AutoCalculatedFieldModel])
  : Seq[AutoCalculatedField] =
    autoCalculatedFields.map(model =>
      AutoCalculatedField(
        model.fromNotNullFields.map(fromNotNullFieldsModel =>
          FromNotNullFields(Field(fromNotNullFieldsModel.field.name, fromNotNullFieldsModel.field.outputType))),
        model.fromPkFields.map(fromPkFieldsModel =>
          FromPkFields(Field(fromPkFieldsModel.field.name, fromPkFieldsModel.field.outputType))),
        model.fromFields.map(fromFieldModel =>
          FromFields(Field(fromFieldModel.field.name, fromFieldModel.field.outputType), fromFieldModel.fromFields)),
        model.fromFixedValue.map(fromFixedValueModel =>
          FromFixedValue(Field(fromFixedValueModel.field.name, fromFixedValueModel.field.outputType),
            fromFixedValueModel.value))
      )
    )
}
