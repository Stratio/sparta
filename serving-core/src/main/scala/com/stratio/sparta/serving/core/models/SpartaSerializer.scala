/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models


import com.stratio.sparta.core.enumerators.{PhaseEnum, _}
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.serving.core.models.enumerators._
import org.json4s.ext.{DateTimeSerializer, EnumNameSerializer}
import org.json4s.{DefaultFormats, Formats}

/**
  * Extends this interface if you need serialize / unserialize Sparta's enums in any class / object.
  */
trait SpartaSerializer {

  implicit def json4sJacksonFormats: Formats = {
    DefaultFormats + DateTimeSerializer +
      new JsoneyStringSerializer() +
      new EnumNameSerializer(WorkflowStatusEnum) +
      new EnumNameSerializer(NodeArityEnum) +
      new EnumNameSerializer(ArityValueEnum) +
      new EnumNameSerializer(SaveModeEnum) +
      new EnumNameSerializer(InputFormatEnum) +
      new EnumNameSerializer(OutputFormatEnum) +
      new EnumNameSerializer(WhenError) +
      new EnumNameSerializer(WhenRowError) +
      new EnumNameSerializer(WhenFieldError) +
      new EnumNameSerializer(WorkflowExecutionEngine) +
      new EnumNameSerializer(WorkflowExecutionMode) +
      new EnumNameSerializer(DataType) +
      new EnumNameSerializer(DateGranularity) +
      new EnumNameSerializer(PhaseEnum)
  }

}
