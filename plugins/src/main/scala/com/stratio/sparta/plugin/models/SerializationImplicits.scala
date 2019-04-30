/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.models

import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.plugin.enumerations._
import org.json4s.ext.EnumNameSerializer
import org.json4s.{DefaultFormats, Formats}

object SerializationImplicits {

  implicit val json4sJacksonFormats: Formats = DefaultFormats +
    new JsoneyStringSerializer() +
    new EnumNameSerializer(CaseValueType) +
    new EnumNameSerializer(TableSide) +
    new EnumNameSerializer(JoinTypes) +
    new EnumNameSerializer(OrderByType) +
    new EnumNameSerializer(TrimType) +
    new EnumNameSerializer(CaseLetter) +
    new EnumNameSerializer(CommaDelimiterDirectionModel)

}
