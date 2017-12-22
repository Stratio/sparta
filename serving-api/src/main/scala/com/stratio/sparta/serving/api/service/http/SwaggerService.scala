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

package com.stratio.sparta.serving.api.service.http

import com.gettyimages.spray.swagger.SwaggerHttpService
import com.wordnik.swagger.model.ApiInfo

import scala.reflect.runtime.universe._

trait SwaggerService extends SwaggerHttpService {

  override def apiTypes: Seq[Type] = Seq(
    typeOf[TemplateHttpService],
    typeOf[WorkflowHttpService],
    typeOf[WorkflowStatusHttpService],
    typeOf[PluginsHttpService],
    typeOf[DriverHttpService],
    typeOf[AppStatusHttpService],
    typeOf[ExecutionHttpService],
    typeOf[AppInfoHttpService],
    typeOf[ConfigHttpService],
    typeOf[CrossdataHttpService],
    typeOf[EnvironmentHttpService],
    typeOf[MetadataHttpService]
  )

  override def apiVersion: String = "1.0"

  // let swagger-ui determine the host and port
  override def docsPath: String = "api-docs"

  override def apiInfo: Option[ApiInfo] = Some(ApiInfo(
    "Sparta",
    "A data pipelines build tool full Spark based.",
    "",
    "sparta@stratio.com",
    "Apache V2",
    "http://www.apache.org/licenses/LICENSE-2.0"
  ))

}
