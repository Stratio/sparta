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

package com.stratio.sparkta.driver

import com.stratio.sparkta.driver.SparktaJob._
import com.stratio.sparkta.driver.factory.SparkContextFactory
import com.stratio.sparkta.driver.util.PolicyUtils
import com.stratio.sparkta.serving.core.models.AggregationPoliciesModel
import com.stratio.sparkta.serving.core.{AppConstant, CuratorFactoryHolder, SparktaConfig}
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.{Failure, Success, Try}

object SparktaClusterJob {

  def main(args: Array[String]): Unit = {
    val aggregationPoliciesModel: AggregationPoliciesModel = getPolicyFromZookeeper(args(0))

    val sc = new SparkContext(new SparkConf().setAppName(s"SPARKTA-${aggregationPoliciesModel.name}"))
    SparkContextFactory.setSparkContext(sc)
    SparktaJob.runSparktaJob(sc, aggregationPoliciesModel)
    SparkContextFactory.sparkStreamingInstance.get.start
    SparkContextFactory.sparkStreamingInstance.get.awaitTermination
  }

  def getPolicyFromZookeeper(policyName: String): AggregationPoliciesModel = {
    Try({
      val configSparkta = SparktaConfig.initConfig(AppConstant.ConfigAppName)
      val curatorFramework = CuratorFactoryHolder.getInstance(configSparkta).get

      PolicyUtils.parseJson(new Predef.String(curatorFramework.getData.forPath(
        s"${AppConstant.PoliciesBasePath}/${policyName}")))
    }) match {
      case Success(policy) => policy
      case Failure(e) => log.error(s"Cannot load policy $policyName", e); throw e
    }
  }

}
