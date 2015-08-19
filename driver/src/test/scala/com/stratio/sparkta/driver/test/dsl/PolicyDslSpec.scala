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

package com.stratio.sparkta.driver.test.dsl

import java.io.File
import scala.io.Source

import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.driver.dsl.PolicyDsl._
import com.stratio.sparkta.driver.factory.SparkContextFactory
import com.stratio.sparkta.driver.util.PolicyUtils

@RunWith(classOf[JUnitRunner])
class PolicyDslSpec extends FlatSpec with ShouldMatchers {

  trait ValidData {

    val policyFile = getClass.getClassLoader.getResource("policies/IKafka-OPrint.json").getPath
    val policy = PolicyUtils.parseJson(Source.fromFile(new File(policyFile)).mkString)
    val jars = List(
      "input-kafka-plugin.jar",
      "output-print-plugin.jar",
      "parser-morphlines-plugin.jar",
      "operator-count-plugin.jar")
    val jarFiles = jars.map(new File(_))
    SparkContextFactory.jars = jarFiles
  }

  trait WrongData extends ValidData {

    val invalidJars = jars.drop(1)
    val invalidjarFiles = invalidJars.map(new File(_))
    val missingJars = List("input-kafka-plugin.jar")
    SparkContextFactory.jars = invalidjarFiles
  }

  "PolicyDslSpec" should "retrieve jars list from policy" in new ValidData {
    policy.getJarsAsString should contain theSameElementsAs (jars)
  }

  it should "validate policy jars" in new ValidData {
    policy.activeJarsAsString.isRight should be(true)
    policy.activeJarsAsString.right.get should contain theSameElementsAs (jars)
  }

  it should "retrieve jars list from policy, 1 missing" in new WrongData {
    policy.getJarsAsString should not contain theSameElementsAs(invalidJars)
  }

  it should "validate policy jars, wrong data" in new WrongData {
    val validateResult = policy.activeJarsAsString
    validateResult.isLeft should be(true)
    validateResult.left.get should contain theSameElementsAs (missingJars)
  }

  it should "filter jars files referenced in policy" in new ValidData {
    policy.activeJarFiles should contain theSameElementsAs (jarFiles)
  }
}
