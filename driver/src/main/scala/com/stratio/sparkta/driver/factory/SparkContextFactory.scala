/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.sparkta.driver.factory

import com.stratio.sparkta.driver.configuration.GeneralConfiguration
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Try

/**
 * Created by ajnavarro on 14/10/14.
 */
object SparkContextFactory {
  private var sc: SparkContext = null

  def sparkContextInstance(generalConfiguration: GeneralConfiguration): SparkContext = {
    synchronized {
      if (sc == null) {
        sc = new SparkContext(configToSparkConf(generalConfiguration))
      }
      sc
    }
  }

  private def configToSparkConf(generalConfiguration: GeneralConfiguration): SparkConf = {
    val conf = new SparkConf()
    conf.setMaster(generalConfiguration.master)
      .setAppName(generalConfiguration.name)

    conf.set("spark.cores.max", generalConfiguration.cpus.toString)

    // Should be a -Xmx style string eg "512m", "1G"
    conf.set("spark.executor.memory", generalConfiguration.memory)

    Try(generalConfiguration.sparkHome).foreach { home => conf.setSparkHome(generalConfiguration.sparkHome)}

    // Set the Jetty port to 0 to find a random port
    conf.set("spark.ui.port", "0")

    conf
  }
}
