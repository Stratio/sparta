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

package com.stratio.sparkta.driver.test.util

import java.io.File

import com.stratio.sparkta.driver.util.PolicyUtils
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class PolicyUtilsTest extends FlatSpec with ShouldMatchers {

  trait ValidData {

    val policyFile = getClass.getClassLoader.getResource("policies/IKafka-OMongo-Common.json").getPath
    val policy = PolicyUtils.parseJson(Source.fromFile(new File(policyFile)).mkString)
    val jars = List(
      "input-kafka-plugin.jar",
      "output-mongodb-plugin.jar",
      "parser-morphlines-plugin.jar",
      "parser-datetime-plugin.jar",
      "operator-max-plugin.jar",
      "field-default-plugin.jar",
      "field-dateTime-plugin.jar")

    val jarFiles = jars.map(new File(_))

    val policyFileWithNoJars = getClass.getClassLoader.getResource("policies/IKafka-OMongo-Common.json").getPath
  }

  trait WrongData extends ValidData {

    val invalidJars = jars.drop(1)
    val invalidjarFiles = invalidJars.map(new File(_))
    val missingJars = List("input-kafka-plugin.jar")
     val wrongJars = List(
       "output-print-plugin.jar",
       "parser-morphlines-plugin.jar",
       "operator-max-plugin.jar",
       "operator-min-plugin.jar",
       "operator-sum-plugin.jar",
       "field-default-plugin.jar")
  }

  "PolicyUtilsSpec" should "retrieve jars list from policy" in new ValidData {
    PolicyUtils.jarsFromPolicy(policy) should contain theSameElementsAs (jars)
  }

//  "PolicyUtilsSpec" should "retrieve jars list empty" in new WrongData {
//    val policyWithNoJars = policy.copy(input = Some(policy.input.get.copy( `type` = "fakeInput")))
//    PolicyUtils.jarsFromPolicy(policyWithNoJars) should contain theSameElementsAs (wrongJars)
//  }
//
//  it should "validate policy jars" in new ValidData {
//    PolicyUtils.activeJars(policy, jarFiles).isRight should be(true)
//    PolicyUtils.activeJars(policy, jarFiles).right.get should contain theSameElementsAs (jars)
//  }
//
//  it should "retrieve jars list from policy, 1 missing" in new WrongData {
//    PolicyUtils.jarsFromPolicy(policy) should not contain theSameElementsAs(invalidJars)
//  }
//
//  it should "validate policy jars, wrong data" in new WrongData {
//    val validateResult = PolicyUtils.activeJars(policy, invalidjarFiles)
//    validateResult.isLeft should be(true)
//    validateResult.left.get should contain theSameElementsAs (missingJars)
//  }
//
//  it should "filter jars files referenced in policy" in new ValidData {
//    PolicyUtils.activeJarFiles(jars, jarFiles) should contain theSameElementsAs (jarFiles)
//  }
}
