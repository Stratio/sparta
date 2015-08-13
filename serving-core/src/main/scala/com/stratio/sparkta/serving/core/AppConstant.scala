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

package com.stratio.sparkta.serving.core

/**
 * Global constants of the application.
 * @author anistal
 */
object AppConstant {

  final val JarPaths = Seq( "plugins", "sdk", "aggregator")
  final val ConfigAppName = "sparkta"
  final val ConfigApi = "api"
  final val ConfigJobServer = "jobServer"
  final val ConfigAkka = "akka"
  final val ConfigSwagger = "swagger"
  final val BaseZKPath      = "/stratio/sparkta"
  final val PoliciesBasePath = s"${AppConstant.BaseZKPath}/policies"

}
