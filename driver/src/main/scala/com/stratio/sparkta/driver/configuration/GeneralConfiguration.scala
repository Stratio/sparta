package com.stratio.sparkta.driver.configuration

/**
 * Created by ajnavarro on 2/10/14.
 */
case class GeneralConfiguration(master: String, sparkHome: String, cpus: Int = 2, memory: String = "512m", duration: Int = 2000)