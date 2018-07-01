/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube.models

import com.stratio.sparta.core.properties.JsoneyStringSerializer
import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.Serialization.read

import scala.util.{Failure, Success, Try}

case class CubeModel(dimensions: Seq[DimensionModel], operators: Seq[OperatorModel])

object CubeModel {

  def getCubeModel(dimensions: String, operators: String): CubeModel = {
    Try {
      implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()

      read[CubeModel](
        s"""{
           |"dimensions":$dimensions,
           |"operators":$operators
           |}"""".stripMargin)
    } match {
      case Success(model) => model
      case Failure(e) => throw new Exception("Impossible to get cube model from properties", e)
    }
  }
}
