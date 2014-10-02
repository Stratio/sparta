package com.stratio.sparkta.driver.factory

import com.stratio.sparkta.driver.exception.DriverException

/**
 * Created by ajnavarro on 2/10/14.
 */
object PropertyValidationHandler {
  def validateProperty(property: String, configuration: Map[String, String], errorMessage: String = "%s property is mandatory."): String = {
    assert(configuration.contains(property), throw new DriverException(errorMessage.format(property)))
    configuration.get(property).get
  }
}
