/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.{ActorContext, ActorRef, _}
import akka.event.slf4j.SLF4JLogging
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.security.SpartaSecurityManager
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.headers.{CacheSupport, CorsSupport}
import com.stratio.sparta.serving.api.service.handler.CustomExceptionHandler._
import com.stratio.sparta.serving.api.service.http._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.spray.oauth2.client.OauthClient
import com.typesafe.config.Config
import spray.http.StatusCodes._
import spray.routing._

import scala.concurrent.duration._
import scala.util.{Properties, Try}

class ControllerActor(
                       executionStListenerActor: ActorRef,
                       envListenerActor: ActorRef
                     )(implicit secManager: Option[SpartaSecurityManager])
  extends HttpServiceActor with SLF4JLogging with CorsSupport with CacheSupport with OauthClient {

  override implicit def actorRefFactory: ActorContext = context

  private val apiTimeout = Try(SpartaConfig.getDetailConfig().get.getInt("timeout"))
    .getOrElse(AppConstant.DefaultApiTimeout) - 1
  implicit val timeout: Timeout = Timeout(apiTimeout.seconds)

  log.debug("Initializing actors in Controller Actor")

  val templateActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new TemplateActor())), TemplateActorName)
  val localLauncherActor = context.actorOf(Props(new LocalLauncherActor()), LocalLauncherActorName)
  val debugLauncherActor = context.actorOf(Props(new DebugLauncherActor()), DebugLauncherActorName)
  val launcherActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new LauncherActor(executionStListenerActor, envListenerActor, localLauncherActor, debugLauncherActor))), LauncherActorName)
  val workflowActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new WorkflowActor(launcherActor, envListenerActor))), WorkflowActorName)
  val executionActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new ExecutionActor())), ExecutionActorName)
  val pluginActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new PluginActor())), PluginActorName)
  val configActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new ConfigActor())), ConfigActorName)
  val globalParametersActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new GlobalParametersActor())), GlobalParametersActorName)
  val parameterListActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new ParameterListActor())), ParameterListActorName)
  val groupActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new GroupActor())), GroupActorName)
  val crossdataActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new CrossdataActor())), CrossdataActorName)
  val debugActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new DebugWorkflowActor(launcherActor))), DebugWorkflowActorName)
  val mlModelActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new MlModelActor())), MlModelsActorName)

  val actorsMap = Map(
    TemplateActorName -> templateActor,
    WorkflowActorName -> workflowActor,
    LauncherActorName -> launcherActor,
    PluginActorName -> pluginActor,
    ExecutionActorName -> executionActor,
    ConfigActorName -> configActor,
    CrossdataActorName -> crossdataActor,
    GlobalParametersActorName -> globalParametersActor,
    GroupActorName -> groupActor,
    DebugWorkflowActorName -> debugActor,
    ParameterListActorName -> parameterListActor,
    MlModelsActorName -> mlModelActor
  )

  val serviceRoutes: ServiceRoutes = new ServiceRoutes(actorsMap, context)
  val oauthConfig: Option[Config] = SpartaConfig.getOauth2Config()
  val enabledSecurity: Boolean = Try(oauthConfig.get.getString("enable").toBoolean).getOrElse(false)
  val cookieName: String = Try(oauthConfig.get.getString("cookieName")).getOrElse(AppConstant.DefaultOauth2CookieName)

  def receive: Receive = runRoute(handleExceptions(exceptionHandler)(getRoutes))

  lazy val getRoutes: Route = cors {
    redirectToRoot ~
      pathPrefix(HttpConstant.SpartaRootPath) {
        secRoute ~ staticRoutes ~ dynamicRoutes
      } ~ secRoute ~ staticRoutes ~ dynamicRoutes
  }

  lazy val redirectToRoot: Route =
    path(HttpConstant.SpartaRootPath) {
      get {
        requestUri { uri =>
          redirect(s"${uri.toString}/", Found)
        }
      }
    }

  lazy val staticRoutes: Route = {
    if (enabledSecurity) {
      secured { userAuth =>
        val user: Option[LoggedUser] = userAuth
        webRoutes
      }
    } else webRoutes
  }

  lazy val dynamicRoutes: Route = {
    if (enabledSecurity) {
      authorized { userAuth =>
        val user: Option[LoggedUser] = userAuth
        allServiceRoutes(user)
      }
    } else allServiceRoutes(None)
  }

  private def allServiceRoutes(user: Option[LoggedUser]): Route = {
    serviceRoutes.templateRoute(user) ~ serviceRoutes.executionRoute(user) ~
      serviceRoutes.workflowRoute(user) ~ serviceRoutes.appStatusRoute ~
      serviceRoutes.pluginsRoute(user) ~ serviceRoutes.swaggerRoute ~
      serviceRoutes.serviceInfoRoute(user) ~
      serviceRoutes.configRoute(user) ~ serviceRoutes.crossdataRoute(user) ~
      serviceRoutes.globalParametersRoute(user) ~ serviceRoutes.groupRoute(user) ~
      serviceRoutes.debugRoutes(user) ~ serviceRoutes.parameterListRoute(user) ~
      serviceRoutes.mlModelsRoutes(user)
  }

  lazy val webRoutes: Route =
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

class ServiceRoutes(actorsMap: Map[String, ActorRef], context: ActorContext) {

  def templateRoute(user: Option[LoggedUser]): Route = templateService.routes(user)

  def workflowRoute(user: Option[LoggedUser]): Route = workflowService.routes(user)

  def executionRoute(user: Option[LoggedUser]): Route = executionService.routes(user)

  def appStatusRoute: Route = appStatusService.routes()

  def pluginsRoute(user: Option[LoggedUser]): Route = pluginsService.routes(user)

  def configRoute(user: Option[LoggedUser]): Route = configService.routes(user)

  def globalParametersRoute(user: Option[LoggedUser]): Route = globalParametersService.routes(user)

  def parameterListRoute(user: Option[LoggedUser]): Route = parameterListService.routes(user)

  def groupRoute(user: Option[LoggedUser]): Route = groupService.routes(user)

  def serviceInfoRoute(user: Option[LoggedUser]): Route = serviceInfoService.routes(user)

  def crossdataRoute(user: Option[LoggedUser]): Route = crossdataService.routes(user)

  def debugRoutes(user: Option[LoggedUser]): Route = debugService.routes(user)

  def mlModelsRoutes(user: Option[LoggedUser]): Route = mlModelsService.routes(user)

  def swaggerRoute: Route = swaggerService.routes

  private val templateService = new TemplateHttpService {
    implicit val actors = actorsMap
    override val supervisor = actorsMap(AkkaConstant.TemplateActorName)
    override val actorRefFactory: ActorRefFactory = context
  }

  private val workflowService = new WorkflowHttpService {
    implicit val actors = actorsMap
    override val supervisor = actorsMap(AkkaConstant.WorkflowActorName)
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
  }

  private val pluginsService = new PluginsHttpService {
    override implicit val actors: Map[String, ActorRef] = actorsMap
    override val supervisor: ActorRef = actorsMap(AkkaConstant.PluginActorName)
    override val actorRefFactory: ActorRefFactory = context
  }

  private val configService = new ConfigHttpService {
    override implicit val actors: Map[String, ActorRef] = actorsMap
    override val supervisor: ActorRef = actorsMap(AkkaConstant.ConfigActorName)
    override val actorRefFactory: ActorRefFactory = context
  }

  private val globalParametersService = new GlobalParametersHttpService {
    override implicit val actors: Map[String, ActorRef] = actorsMap
    override val supervisor: ActorRef = actorsMap(AkkaConstant.GlobalParametersActorName)
    override val actorRefFactory: ActorRefFactory = context
  }

  private val parameterListService = new ParameterListHttpService {
    override implicit val actors: Map[String, ActorRef] = actorsMap
    override val supervisor: ActorRef = actorsMap(AkkaConstant.ParameterListActorName)
    override val actorRefFactory: ActorRefFactory = context
  }

  private val groupService = new GroupHttpService {
    override implicit val actors: Map[String, ActorRef] = actorsMap
    override val supervisor: ActorRef = actorsMap(AkkaConstant.GroupActorName)
    override val actorRefFactory: ActorRefFactory = context
  }

  private val serviceInfoService = new AppInfoHttpService {
    override implicit val actors: Map[String, ActorRef] = actorsMap
    override val supervisor: ActorRef = context.self
    override val actorRefFactory: ActorRefFactory = context
  }

  private val crossdataService = new CrossdataHttpService {
    override implicit val actors: Map[String, ActorRef] = actorsMap
    override val supervisor: ActorRef = actorsMap(AkkaConstant.CrossdataActorName)
    override val actorRefFactory: ActorRefFactory = context
  }

  private val debugService = new DebugWorkflowHttpService {
    override implicit val actors: Map[String, ActorRef] = actorsMap
    override val supervisor: ActorRef = actorsMap(AkkaConstant.DebugWorkflowActorName)
    override val actorRefFactory: ActorRefFactory = context
  }

  private val mlModelsService = new MlModelsHttpService {
    override implicit val actors: Map[String, ActorRef] = actorsMap
    override val supervisor: ActorRef = actorsMap(AkkaConstant.MlModelsActorName)
    override val actorRefFactory: ActorRefFactory = context
  }

  private val swaggerService = new SwaggerService {
    override implicit def actorRefFactory: ActorRefFactory = context

    override def baseUrl: String = {
      val marathonLBPath = for {
        marathonLB_host <- Properties.envOrNone("MARATHON_APP_LABEL_HAPROXY_0_VHOST").notBlank
        marathonLB_path <- Properties.envOrNone("MARATHON_APP_LABEL_HAPROXY_0_PATH").notBlank
      } yield {
        val ssl = Properties.envOrElse("SECURITY_TLS_ENABLE", "false").toBoolean
        s"http${if (ssl) "s" else ""}:" + s"//${marathonLB_host + marathonLB_path}"
      }

      marathonLBPath match {
        case Some(marathonLBpath) => marathonLBpath
        case None => "/"
      }
    }
  }
}
