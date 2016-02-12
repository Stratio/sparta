/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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
import scala.util.Either
import scala.util.Left
import scala.util.Right

import akka.event.slf4j.SLF4JLogging
import org.json4s._
import org.json4s.jackson.JsonMethods._

import com.stratio.sparkta.serving.core.models.CommonPoliciesModel
import com.stratio.sparkta.serving.core.models.SparktaSerializer

/**
 * Utils for policies.
 *
 * @author sgomezg
 */
object PolicyUtils extends SparktaSerializer with SLF4JLogging {

  /**
   * Method to parse CommonPoliciesModel from JSON string
   *
   * @param json The policy as JSON string
   * @return CommonPoliciesModel
   */
  def parseJson(json: String): CommonPoliciesModel = parse(json).extract[CommonPoliciesModel]

  def jarsFromPolicy(apConfig: CommonPoliciesModel): Seq[String] = {
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

  def activeJars(apConfig: CommonPoliciesModel, jars: Seq[File]): Either[Seq[String], Seq[String]] = {
    val policyJars = jarsFromPolicy(apConfig)
    val names = jars.map(file => file.getName)
    val missing = for (name <- policyJars if !names.contains(name)) yield name
    if (missing.isEmpty) Right(policyJars)
    else Left(missing)
  }

  def activeJarFiles(policyJars: Seq[String], jars: Seq[File]): Seq[File] =
    jars.filter(file => policyJars.contains(file.getName))
}
