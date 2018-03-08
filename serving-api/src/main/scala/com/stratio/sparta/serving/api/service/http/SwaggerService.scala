/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
    typeOf[GroupHttpService],
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
