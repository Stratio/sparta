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

package com.stratio.sparta.plugin.workflow.transformation.datetime.models

import java.util.Locale

import com.stratio.sparta.plugin.enumerations.DateFormatEnum._
import com.stratio.sparta.plugin.enumerations.FieldsPreservationPolicy._

case class DateTimeItem(inputField: Option[String] = None,
                        formatFrom: DateFormat,
                        userFormat: Option[String]= None,
                        standardFormat: Option[String]= None,
                        localeTime: Locale,
                        granularity: Option[String],
                        preservationPolicy: FieldsPreservationPolicy,
                        outputFieldName: String,
                        outputFieldType: String,
                        nullable : Option[Boolean] = None,
                        outputFormatFrom: DateFormat,
                        outputUserFormat: Option[String]= None,
                        outputStandardFormat: Option[String]= None
                       )