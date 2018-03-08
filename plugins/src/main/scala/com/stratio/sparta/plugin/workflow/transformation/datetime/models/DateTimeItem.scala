/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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