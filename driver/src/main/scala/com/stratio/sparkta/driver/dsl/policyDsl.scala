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

package com.stratio.sparkta.driver.dsl

import java.io.File
import scala.language.implicitConversions
import scala.util._

import com.stratio.sparkta.driver.factory.SparkContextFactory
import com.stratio.sparkta.serving.core.models.AggregationPoliciesModel

class PolicyDslJars(self: AggregationPoliciesModel, jars: Seq[File]) {

  def getJarsAsString: Seq[String] = {
    val input = self.input.get.jarFile match {
      case Some(file) => Seq(file)
      case None => Seq()
    }
    val outputs = self.outputs.flatMap(_.jarFile)
    val transformations = self.transformations.flatMap(_.jarFile)
    val dimensions = self.cubes.flatMap(cube => cube.dimensions.map(_.jarFile)).flatten
    val operators = self.cubes.flatMap(cube => cube.operators.map(_.jarFile)).flatten
    Seq(input, outputs, transformations, operators, dimensions).flatten.distinct
  }

  def activeJarsAsString: Either[Seq[String], Seq[String]] = {
    val policyJars = getJarsAsString
    val names = jars.map(file => file.getName)
    val missing = for (name <- policyJars if !names.contains(name)) yield name
    if (missing.isEmpty) Right(policyJars)
    else Left(missing)
  }

  def activeJarFiles: Seq[File] = jars.filter(file => getJarsAsString.contains(file.getName))
}

trait PolicyDsl {

  implicit def policyDslJars(self: AggregationPoliciesModel): PolicyDslJars =
    new PolicyDslJars(self, SparkContextFactory.jars)
}

object PolicyDsl extends PolicyDsl
