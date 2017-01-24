/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
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
package com.stratio.sparta.serving.core.constants

/**
 * Akka constants with Akka's actor paths.
 *
 * @author anistal
 */
object AkkaConstant {

  val StatusActor = "statusActor"
  val FragmentActor = "fragmentActor"
  val TemplateActor = "templateActor"
  val PolicyActor = "policyActor"
  val SparkStreamingContextActor = "sparkStreamingContextActor"
  val PluginActor = "pluginActor"
  val ControllerActor = "controllerActor"
  val SwaggerActor = "swaggerActor"
  val PolicyStatusActor = "supervisorContextActor"
  val ControllerActorInstances = "controllerActorInstances"
  val DefaultControllerActorInstances = 5

  val DefaultTimeout = 15

  def cleanActorName(initialName: String) : String = initialName.replace(" ", "_")

}
