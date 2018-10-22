/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.output.mlpipeline.deserialization

import com.stratio.sparta.core.properties.JsoneyString
import org.apache.spark.ml.param.Param
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JBool

import scala.util.Try


/** Custom json4s serializator/deserializator for boolean values threated as strings */
object BooleanToString extends CustomSerializer[String](
  _ => ( {
    case JBool(x) => x.toString
  }, {
    case x: String => JBool(x.toBoolean)
  })
)


/** Pipeline object descriptor
  *   - A Pipeline is composed by an array of PipelineStages
  *
  * @param pipeline
  */
case class PipelineDescriptor(
                               pipeline: Array[PipelineStageDescriptor]
                             )

/** PipelineStage object descriptor
  *   - A PipelineStage contains a series of parameters
  */
case class PipelineStageDescriptor(
                                    name: String,
                                    uid: String,
                                    className: String,
                                    properties: Map[String, JsoneyString]
                                  )


//noinspection ScalaStyle
object MlPipelineDeserializationUtils {

  def nullOrEmpty(value: JsoneyString): Boolean = (value == null)||(value.toString.trim == "")

  //TODO filter out only optional parameters
  def okParam(value: JsoneyString): Boolean = (value != null)&&(value.toString.trim != "")

  def decodeParamValue(param: Param[Any], value: JsoneyString = null): Try[Any] = Try {
    param.getClass().getSimpleName match {
      case "BooleanParam" => if (nullOrEmpty(value)) "Boolean" else value.toString.toBoolean
      case "LongParam" => if (nullOrEmpty(value)) "Long" else value.toString.toLong
      case "DoubleParam" => if (nullOrEmpty(value)) "Double" else value.toString.toDouble
      case "FloatParam" => if (nullOrEmpty(value)) "Float" else value.toString.toFloat
      case "IntParam" => if (nullOrEmpty(value)) "Int" else value.toString.toInt
      case "StringArrayParam" => if (nullOrEmpty(value)) "Array[String] (comma separated values)" else value.toString.split(",").map(_.trim)
      case "DoubleArrayParam" => if (nullOrEmpty(value)) "Array[Double] (comma separated values)" else value.toString.split(",").map(_.trim.toDouble)
      case "IntArrayParam" => if (nullOrEmpty(value)) "Array[Int] (comma separated values)" else value.toString.split(",").map(_.trim.toInt)
      case "Param" => if (nullOrEmpty(value)) "String" else value.toString
      case _ => throw new Exception("Unknown parameter type")
    }
  }
}