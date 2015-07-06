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

package com.stratio.sparkta.driver.service.http

import akka.pattern.ask
import com.stratio.sparkta.driver.actor._
import com.stratio.sparkta.driver.constants.HttpConstant
import com.wordnik.swagger.annotations._
import spray.routing.Route

import scala.concurrent.Await
import scala.util.{Failure, Success}

@Api(value = HttpConstant.FragmentPath, description = "Operations about templates. One template will have an abstract" +
  " element that represents a validation, a tip, an icon over it.", position = 0)
trait TemplateHttpService extends BaseHttpService {

  override def routes: Route = findByType ~ findByTypeAndName

  def findByType: Route = {
    path(HttpConstant.TemplatePath / Segment) { (t) =>
      get {
        complete {
          val future = supervisor ? new TemplateSupervisorActor_findByType(t)
          Await.result(future, timeout.duration) match {
            case TemplateSupervisorActor_response_templates(Failure(exception)) => throw exception
            case TemplateSupervisorActor_response_templates(Success(templates)) => templates
          }
        }
      }
    }
  }

  def findByTypeAndName: Route = {
    path(HttpConstant.TemplatePath / Segment / Segment ) { (t, name) =>
      get {
        complete {
          val future = supervisor ? new TemplateSupervisorActor_findByTypeAndName(t, name)
          Await.result(future, timeout.duration) match {
            case TemplateSupervisorActor_response_template(Failure(exception)) => throw exception
            case TemplateSupervisorActor_response_template(Success(template)) => template
          }
        }
      }
    }
  }
}
