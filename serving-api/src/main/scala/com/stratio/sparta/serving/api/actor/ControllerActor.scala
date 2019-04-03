/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{ActorContext, ActorRef, _}
import akka.event.slf4j.SLF4JLogging
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.security.SpartaSecurityManager
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.headers.{CacheSupport, CorsSupport, HeadersAuthSupport}
import com.stratio.sparta.serving.api.oauth.OauthClient
import com.stratio.sparta.serving.api.service.handler.CustomExceptionHandler._
import com.stratio.sparta.serving.api.service.http._
import com.stratio.sparta.serving.core.actor.ParametersListenerActor
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparta.serving.core.models.authorization.{GosecUser, LoggedUser}
import com.typesafe.config.Config
import spray.http.StatusCodes._
import spray.httpx.encoding.Gzip
import spray.routing._

import scala.concurrent.duration._
import scala.util.{Properties, Try}

class ControllerActor()(implicit secManager: Option[SpartaSecurityManager])
  extends HttpServiceActor with SLF4JLogging with CorsSupport with CacheSupport with OauthClient with HeadersAuthSupport{

  override implicit def actorRefFactory: ActorContext = context

  override def supervisorStrategy: SupervisorStrategy = AllForOneStrategy(){
    case _ => Restart
  }

  override def postStop(): Unit = {
    log.warn(s"Stopped ControllerActor at time ${System.currentTimeMillis()}")
  }

  private val apiTimeout = Try(SpartaConfig.getDetailConfig().get.getInt("timeout"))
    .getOrElse(AppConstant.DefaultApiTimeout) - 1
  implicit val timeout: Timeout = Timeout(apiTimeout.seconds)

  log.debug("Initializing actors in Controller Actor")

  lazy val clusterSessionActor = context.actorOf(Props[ClusterSessionActor])

  val parametersListenerActor = context.actorOf(Props[ParametersListenerActor])
  val templateActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new TemplateActor())), TemplateActorName)
  val localLauncherActor = context.actorOf(Props(new LocalLauncherActor()), LocalLauncherActorName)
  val debugLauncherActor = context.actorOf(Props(new DebugLauncherActor()), DebugLauncherActorName)
  val launcherActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new LauncherActor(parametersListenerActor, localLauncherActor, debugLauncherActor))), LauncherActorName)
  val workflowActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new WorkflowActor(launcherActor, parametersListenerActor))), WorkflowActorName)
  val executionActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new ExecutionActor(launcherActor))), ExecutionActorName)
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
  val scheduledWorkflowTaskActor = context.actorOf(RoundRobinPool(DefaultInstances)
    .props(Props(new ScheduledWorkflowTaskActor())), ScheduledWorkflowTaskActorName)

  context.actorOf(Props(new ScheduledWorkflowTaskExecutorActor(launcherActor)), ScheduledWorkflowTaskExecutorActorName)


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
    MlModelsActorName -> mlModelActor,
    ScheduledWorkflowTaskActorName -> scheduledWorkflowTaskActor
  )

  private val serviceRoutes: ServiceRoutes = new ServiceRoutes(actorsMap, context)
  private val oauthConfig: Option[Config] = SpartaConfig.getOauth2Config()
  private val oauthEnabled: Boolean = Try(oauthConfig.get.getString("enable").toBoolean).getOrElse(false)
  private val headersAuthEnabled: Boolean = headersAuthConfig.exists(_.enabled)

  require(
    !oauthEnabled || !headersAuthEnabled,
    "Authentication cannot be enabled to multiple options: oauth and headers"
  )

  def receive: Receive = runRoute(handleExceptions(exceptionHandler)(getRoutes))

  lazy val getRoutes: Route = cors {
    redirectToRoot ~
      pathPrefix(HttpConstant.SpartaRootPath) {
        logRoute ~ staticRoutes ~ dynamicRoutes
      } ~ logRoute ~ staticRoutes ~ dynamicRoutes
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
    if (oauthEnabled) {
      secured (_ => webRoutes)
    } else if (headersAuthEnabled) {
      authorizeHeaders(_ => webRoutes)
    } else {
      webRoutes
    }
  }

  lazy val dynamicRoutes: Route = {
    val authRoutes: Option[LoggedUser] => Route =
      userAuth => allServiceRoutes(userAuth)


    if (oauthEnabled) {
      authorized(user => authRoutes(GosecUser.jsonToDto(user)))
    } else if (headersAuthEnabled) {
      authorizeHeaders(user => authRoutes(Some(user)))
    } else {
      allServiceRoutes(None)
    }
  }

  private def allServiceRoutes(user: Option[LoggedUser]): Route = {
    compressResponse(Gzip) {
      serviceRoutes.templateRoute(user) ~ serviceRoutes.executionRoute(user) ~
        serviceRoutes.workflowRoute(user) ~ serviceRoutes.appStatusRoute ~
        serviceRoutes.pluginsRoute(user) ~ serviceRoutes.swaggerRoute ~
        serviceRoutes.serviceInfoRoute(user) ~
        serviceRoutes.configRoute(user) ~ serviceRoutes.crossdataRoute(user) ~
        serviceRoutes.globalParametersRoute(user) ~ serviceRoutes.groupRoute(user) ~
        serviceRoutes.debugRoutes(user) ~ serviceRoutes.parameterListRoute(user) ~
        serviceRoutes.mlModelsRoutes(user) ~ serviceRoutes.scheduledWorkflowTasksRoutes(user)
    }
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

  def scheduledWorkflowTasksRoutes(user: Option[LoggedUser]): Route = scheduledWorkflowTasksService.routes(user)

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

  private val scheduledWorkflowTasksService = new ScheduledWorkflowTaskHttpService {
    override implicit val actors: Map[String, ActorRef] = actorsMap
    override val supervisor: ActorRef = actorsMap(AkkaConstant.ScheduledWorkflowTaskActorName)
    override val actorRefFactory: ActorRefFactory = context
  }

  private val swaggerService = new SwaggerService {
    override implicit def actorRefFactory: ActorRefFactory = context

    override def baseUrl: String = {
      val marathonLBPath = for {
        marathonLB_host <- AppConstant.virtualHost
        marathonLB_path <- AppConstant.virtualPath
      } yield {
        val protocol = if (AppConstant.securityTLSEnable) "https" else "http"
        s"$protocol://$marathonLB_host$marathonLB_path"
      }

      marathonLBPath.getOrElse("/")
    }
  }
}
