/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.driver.util

import java.io.File

import com.stratio.sparkta.sdk.JsoneyStringSerializer
import com.stratio.sparkta.serving.core.models.{SparktaSerializer, AggregationPoliciesModel, StreamingContextStatusEnum}
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.JsonMethods._
import org.json4s.native.Serialization._
import org.json4s.{DefaultFormats, _}

import scala.util.{Either, Left, Right}

/**
 * Utils for policies.
 * @author sgomezg
 */
object PolicyUtils extends SparktaSerializer {

  /**
   * Method to parse AggregationPoliciesModel from JSON string
   * @param json The policy as JSON string
   * @return AggregationPoliciesModel
   */
  def parseJson(json: String): AggregationPoliciesModel = parse(json).extract[AggregationPoliciesModel]

  def toJson(policy: AggregationPoliciesModel): String = write(policy)

  def jarsFromPolicy(apConfig: AggregationPoliciesModel): Seq[String] = {
    val input = apConfig.input.get.jarFile match {
      case Some(file) => Seq(file)
      case None => Seq()
    }
    val outputs = apConfig.outputs.flatMap(_.jarFile)
    val transformations = apConfig.transformations.flatMap(_.jarFile)
    val operators = apConfig.cubes.flatMap(cube => cube.operators.map(_.jarFile)).flatten
    val dimensionsType = apConfig.cubes.flatMap(cube => cube.dimensions.map(_.jarFile)).flatten
    Seq(input, outputs, transformations, operators, dimensionsType).flatten.distinct
  }

  def activeJars(apConfig: AggregationPoliciesModel, jars: Seq[File]): Either[Seq[String], Seq[String]] = {
    val policyJars = jarsFromPolicy(apConfig)
    val names = jars.map(file => file.getName)
    val missing = for (name <- policyJars if !names.contains(name)) yield name
    if (missing.isEmpty) Right(policyJars)
    else Left(missing)
  }

  def activeJarFiles(policyJars: Seq[String], jars: Seq[File]): Seq[File] =
    jars.filter(file => policyJars.contains(file.getName))
}
