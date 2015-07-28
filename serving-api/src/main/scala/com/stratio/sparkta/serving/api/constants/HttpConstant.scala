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

package com.stratio.sparkta.serving.api.constants

/**
 * HttpConstants used in http services mainly.
 * @author anistal
 */
object HttpConstant {

  /**
   * Serving api prefix endpoints
   */
  final val FragmentPath      = "fragment"
  final val TemplatePath      = "template"
  final val PolicyPath        = "policy"
  final val PolicyContextPath = "policyContext"
  final val SwaggerPath       = "swagger"

  final val JobServerPath = "jobServer"

  final val JobsPath = "jobs"

  final val ContextsPath = "contexts"

  /**
   * Http codes.
   */
  final val NotFound = 400
  final val BadRequest = 500

  /**
   * Http messages.
   */
  final val NotFoundMessage = "Not Found"
  final val BadRequestMessage = "Bad Request"
  final val JobServerOkMessage = "OK"
  final val JobServerHostPortExceptionMsg = "JobServer host and port is not defined in configuration file"
}
