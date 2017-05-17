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

package com.stratio.sparta.serving.api.actor

import akka.actor.{ActorContext, ActorRef, _}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.headers.{CacheSupport, CorsSupport}
import com.stratio.sparta.serving.api.service.handler.CustomExceptionHandler._
import com.stratio.sparta.serving.api.service.http._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.spray.oauth2.client.OauthClient
import com.typesafe.config.Config
import org.apache.curator.framework.CuratorFramework
import spray.http.StatusCodes._
import spray.routing._
import scala.util.Properties


import scala.util.Try

class ControllerActor(actorsMap: Map[String, ActorRef], curatorFramework: CuratorFramework) extends HttpServiceActor
  with SLF4JLogging
  with SpartaSerializer
  with CorsSupport
  with CacheSupport
  with OauthClient {

  override implicit def actorRefFactory: ActorContext = context

  val serviceRoutes: ServiceRoutes = new ServiceRoutes(actorsMap, context, curatorFramework)

  val oauthConfig: Option[Config] = SpartaConfig.initConfig("oauth2")
  val enabledSecurity: Boolean = Try(oauthConfig.get.getString("enable").toBoolean).getOrElse(false)
  val cookieName: String = Try(oauthConfig.get.getString("cookieName")).getOrElse("user")

  def receive: Receive = runRoute(handleExceptions(exceptionHandler)(getRoutes))

  def getRoutes: Route = cors{
    redirectToRoot ~
      pathPrefix(HttpConstant.SpartaRootPath){
        secRoute ~ staticRoutes ~ dynamicRoutes
      } ~ secRoute ~ staticRoutes ~ dynamicRoutes
  }

  private def redirectToRoot: Route =
  path(HttpConstant.SpartaRootPath){
    get{
      requestUri{ uri =>
        redirect(s"${uri.toString}/", Found)
      }
    }
  }

  private def staticRoutes: Route = {
    if (enabledSecurity) {
      secured { userAuth =>
        val user: Option[LoggedUser] = userAuth
        user match {
          case Some(parsedUser) =>
            authorize(parsedUser.isAuthorized(enabledSecurity)) {
              webRoutes
            }
          case None => complete(Unauthorized)
        }
      }
    } else webRoutes
  }

  private def dynamicRoutes: Route = {
    if (enabledSecurity) {
      authorized { userAuth =>
        val user: Option[LoggedUser] = userAuth
        user match {
          case Some(parsedUser) =>
            authorize(parsedUser.isAuthorized(enabledSecurity)) {
              allServiceRoutes(Some(parsedUser))
            }
          case None => complete(Unauthorized)
        }
      }
    } else allServiceRoutes(None)
  }

  private def allServiceRoutes(user: Option[LoggedUser]): Route = {
    serviceRoutes.fragmentRoute(user) ~ serviceRoutes.policyContextRoute(user) ~
      serviceRoutes.executionRoute(user) ~ serviceRoutes.policyRoute(user) ~ serviceRoutes.appStatusRoute ~
      serviceRoutes.pluginsRoute(user) ~ serviceRoutes.driversRoute(user) ~
      serviceRoutes.swaggerRoute
  }

  private def webRoutes: Route =
    get {
      pathPrefix(HttpConstant.SwaggerPath) {
        pathEndOrSingleSlash {
          getFromResource("swagger-ui/index.html")
        }
      } ~ getFromResourceDirectory("swagger-ui") ~
        pathPrefix("") {
          pathEndOrSingleSlash {
            getFromResource("classes/web/index.html")
          }
        } ~ getFromResourceDirectory("classes/web") ~
        pathPrefix("") {
          pathEndOrSingleSlash {
            getFromResource("web/index.html")
          }
        } ~ getFromResourceDirectory("web")
    }
}

class ServiceRoutes(actorsMap: Map[String, ActorRef], context: ActorContext, curatorFramework: CuratorFramework) {

  def fragmentRoute(user: Option[LoggedUser]): Route = fragmentService.routes(user)

  def policyRoute(user: Option[LoggedUser]): Route = policyService.routes(user)

  def policyContextRoute(user: Option[LoggedUser]): Route = policyContextService.routes(user)

  def executionRoute(user: Option[LoggedUser]): Route = executionService.routes(user)

  def appStatusRoute: Route = appStatusService.routes()

  def pluginsRoute(user: Option[LoggedUser]): Route = pluginsService.routes(user)

  def driversRoute(user: Option[LoggedUser]): Route = driversService.routes(user)

  def swaggerRoute: Route = swaggerService.routes

  def getMarathonLBPath: Option[String] = {
    val marathonLB_host = Properties.envOrElse("MARATHON_APP_LABEL_HAPROXY_0_VHOST","")
    val marathonLB_path = Properties.envOrElse("MARATHON_APP_LABEL_HAPROXY_0_PATH", "")

    if(marathonLB_host.nonEmpty && marathonLB_path.nonEmpty)
      Some("https://" + marathonLB_host + marathonLB_path)
    else None
  }

  private val fragmentService = new FragmentHttpService {
    implicit val actors = actorsMap
    override val supervisor = actorsMap(AkkaConstant.FragmentActorName)
    override val actorRefFactory: ActorRefFactory = context
  }

  private val policyService = new PolicyHttpService {
    implicit val actors = actorsMap
    override val supervisor = actorsMap(AkkaConstant.PolicyActorName)
    override val actorRefFactory: ActorRefFactory = context
  }

  private val policyContextService = new PolicyContextHttpService {
    implicit val actors = actorsMap
    override val supervisor = actorsMap(AkkaConstant.LauncherActorName)
    override val actorRefFactory: ActorRefFactory = context
  }

  private val executionService = new ExecutionHttpService {
    implicit val actors = actorsMap
    override val supervisor = actorsMap(AkkaConstant.ExecutionActorName)
    override val actorRefFactory: ActorRefFactory = context
  }

  private val appStatusService = new AppStatusHttpService {
    override implicit val actors: Map[String, ActorRef] = actorsMap
    override val supervisor: ActorRef = context.self
    override val actorRefFactory: ActorRefFactory = context
    override val curatorInstance = curatorFramework
  }

  private val pluginsService = new PluginsHttpService {
    override implicit val actors: Map[String, ActorRef] = actorsMap
    override val supervisor: ActorRef = actorsMap(AkkaConstant.PluginActorName)
    override val actorRefFactory: ActorRefFactory = context
  }

  private val driversService = new DriverHttpService {
    override implicit val actors: Map[String, ActorRef] = actorsMap
    override val supervisor: ActorRef = actorsMap(AkkaConstant.DriverActorName)
    override val actorRefFactory: ActorRefFactory = context
  }

  private val swaggerService = new SwaggerService {
    override implicit def actorRefFactory: ActorRefFactory = context
    override def baseUrl: String = getMarathonLBPath match {
      case Some(marathonLBpath) => marathonLBpath
      case None => "/"
    }
  }
}
