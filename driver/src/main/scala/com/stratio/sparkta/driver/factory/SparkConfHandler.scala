package com.stratio.sparkta.driver.factory

import com.stratio.sparkta.driver.configuration.GeneralConfiguration
import org.apache.spark.SparkConf

import scala.util.Try

/**
 * Created by ajnavarro on 2/10/14.
 */
object SparkConfHandler {
  def configToSparkConf(generalConfiguration: GeneralConfiguration, appName: String): SparkConf = {
    val conf = new SparkConf()
    conf.setMaster(generalConfiguration.master)
      .setAppName(appName)

    conf.set("spark.cores.max", generalConfiguration.cpus.toString)

    // Should be a -Xmx style string eg "512m", "1G"
    conf.set("spark.executor.memory", generalConfiguration.memory)

    Try(generalConfiguration.sparkHome).foreach { home => conf.setSparkHome(generalConfiguration.sparkHome)}

    // Set the Jetty port to 0 to find a random port
    conf.set("spark.ui.port", "0")

    conf
  }
}
