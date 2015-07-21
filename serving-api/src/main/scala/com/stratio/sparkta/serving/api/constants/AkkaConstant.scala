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
 * Akka constants with Akka's actor paths.
 * @author anistal
 */
object AkkaConstant {

  final val StreamingActorAkkaPath = s"akka://${AppConstant.ConfigAppName}/user/streamingActor"
  final val FragmentActorAkkaPath  = s"akka://${AppConstant.ConfigAppName}/user/fragmentActor"
  final val TemplateActorAkkaPath  = s"akka://${AppConstant.ConfigAppName}/user/templateActor"
  final val JobServerActorAkkaPath  = s"akka://${AppConstant.ConfigAppName}/user/jobServerActor"

  final val PolicyActorAkkaPath    = s"akka://${AppConstant.ConfigAppName}/user/$PolicyActor"
  final val PolicyActor = "policyActor"
}
