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

package com.stratio.sparkta.serving.api.helpers

import java.io.File
import java.lang.reflect.Method
import java.net.{URL, URLClassLoader}

import akka.actor.{ActorSystem, Props}
import akka.event.slf4j.SLF4JLogging
import akka.io.IO
import akka.routing.RoundRobinPool
import com.stratio.sparkta.driver.factory.SparkContextFactory
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.serving.api.actor._
import com.stratio.sparkta.serving.api.constants.AkkaConstant
import com.stratio.sparkta.serving.core._
import com.typesafe.config.Config
import spray.can.Http

import scala.util.Try

/**
 * Helper with common operations used to create a Sparkta context used to run the application.
 * @author anistal
 */
object SparktaHelper extends SLF4JLogging {

  implicit var system: ActorSystem = _

  /**
   * Initializes Sparkta's base path.
   * @return the object described above.
   */
  def initSparktaHome(system: System = new SparktaSystem): String = {
    val sparktaHome: Option[String] = system.getenv("SPARKTA_HOME").orElse({
      val sparktaHomeDefault = system.getProperty("user.dir", "./")
      log.warn("SPARKTA_HOME environment variable is not set, setting to default value")
      sparktaHomeDefault
    })
    assert(sparktaHome.isDefined, "Fatal error: sparktaHome not found.")
    log.info(s"> Setting configuration path to ${sparktaHome.get}")
    sparktaHome.get
  }

  /**
   * With the aim of having a pluggable system of plugins and given  a list of relative paths that contain jars (our
   * plugins). It tries to instance jars located in this paths and to load them in the classpath.
   * @param relativeJarPaths that contains jar plugins.
   * @param sparktaHome with Sparkta's base path.
   * @return a list of loaded jars.
   */
  def initJars(relativeJarPaths: Seq[String], sparktaHome: String): Seq[File] =
    relativeJarPaths.flatMap(path => {
      log.info(s"> Loading jars from $sparktaHome/$path")
      findJarsByPathAndAddToClasspath(new File(sparktaHome, path))
    })

  /**
   * Initializes base configuration.
   * @param currentConfig if it is setted the function tries to load a node from a loaded config.
   * @param node with the node needed to load the configuration.
   * @return the optional loaded configuration.
   */
  def initOptionalConfig(node: String,
                         currentConfig: Option[Config] = None,
                         configFactory: ConfigFactory = new SparktaConfigFactory): Option[Config] = {
    log.info(s"> Loading $node configuration")
    Try(
      currentConfig match {
        case Some(config) => Some(config.getConfig(node))
        case _ => configFactory.getConfig(node)
      }
    ).getOrElse(None)
  }

  /**
   * Initializes Sparkta's akka system running an embedded http server with the REST API.
   * @param configSparkta with Sparkta's global configuration.
   * @param configApi with http server's configuration.
   * @param jars that will be loaded.
   * @param appName with the name of the application.
   */
  def initAkkaSystem(configSparkta: Config,
                     configApi: Config,
                     jars: Seq[File],
                     appName: String): Unit = {
    val streamingContextService = new StreamingContextService(configSparkta, jars)
    val curatorFramework = CuratorFactoryHolder.getInstance(configSparkta).get
    log.info("> Initializing akka actors")
    system = ActorSystem(appName)
    val jobServerConfig = configSparkta.getConfig(AppConstant.ConfigJobServer)
    val jobServerConfigOp = Try(if (!jobServerConfig.isEmpty &&
      Try(jobServerConfig.getString("host")).isSuccess &&
      Try(jobServerConfig.getString("host")).get != "" &&
      Try(jobServerConfig.getInt("port")).isSuccess &&
      Try(jobServerConfig.getInt("port")).get > 0)
      Some(jobServerConfig)
    else None).getOrElse(None)
    val akkaConfig = configSparkta.getConfig(AppConstant.ConfigAkka)
    val swaggerConfig = configSparkta.getConfig(AppConstant.ConfigSwagger)
    val controllerInstances = if (!akkaConfig.isEmpty) akkaConfig.getInt(AkkaConstant.ControllerActorInstances)
    else AkkaConstant.DefaultControllerActorInstances
    val streamingActorInstances = if (!akkaConfig.isEmpty) akkaConfig.getInt(AkkaConstant.ControllerActorInstances)
    else AkkaConstant.DefaultControllerActorInstances
    val jobServerActor = if (jobServerConfigOp.isDefined)
      Some(system.actorOf(Props(new JobServerActor(jobServerConfig.getString("host"),
        jobServerConfig.getInt("port"))), AkkaConstant.JobServerActor)) else None
    val supervisorContextActor = system.actorOf(
      Props(new SupervisorContextActor), AkkaConstant.SupervisorContextActor)
    implicit val actors = Map(
      AkkaConstant.FragmentActor ->
        system.actorOf(Props(new FragmentActor(curatorFramework)), AkkaConstant.FragmentActor),
      AkkaConstant.TemplateActor ->
        system.actorOf(Props(new TemplateActor()), AkkaConstant.TemplateActor),
      AkkaConstant.PolicyActor ->
        system.actorOf(Props(new PolicyActor(curatorFramework)), AkkaConstant.PolicyActor),
      AkkaConstant.StreamingActor -> system.actorOf(RoundRobinPool(streamingActorInstances).props(Props(
        new StreamingActor(streamingContextService, jobServerActor, jobServerConfigOp, supervisorContextActor))),
        AkkaConstant.StreamingActor)
    ) ++ {
      if (jobServerActor.isDefined) Map(AkkaConstant.JobServerActor -> jobServerActor.get) else Map()
    }
    val controllerActor = system.actorOf(
      Props(new ControllerActor(streamingContextService, curatorFramework, actors)), AkkaConstant.ControllerActor)
//    TODO: change this when swagger will be fixed.
//     val controllerActor = system.actorOf(RoundRobinPool(controllerInstances)
//      .props(Props(new ControllerActor(streamingContextService, curatorFramework, actors))),
//      AkkaConstant.ControllerActor)
//    val swaggerActor = system.actorOf(Props(new SwaggerActor), AkkaConstant.SwaggerActor)
    IO(Http) ! Http.Bind(controllerActor, interface = configApi.getString("host"), port = configApi.getInt("port"))
//  IO(Http) ! Http.Bind(swaggerActor, interface = swaggerConfig.getString("host"), port = swaggerConfig.getInt("port"))
    log.info("> Actors System UP!")
  }

  ///////////////////////////////////////////  XXX Protected methods ///////////////////////////////////////////////////

  /**
   * Finds files that end with the sufix *-plugin.jar and load them in the classpath of the application.
   * @param path base path when it starts to scan in order to find plugins.
   * @return a list of loaded jars.
   */
  protected def findJarsByPathAndAddToClasspath(path: File): Seq[File] = {
    val these = path.listFiles()
    val good = these.filter(f => {
      if (f.getName.endsWith("-plugin.jar")) {
        addToClasspath(f)
        log.debug("File " + f.getName + " added")
        true
      } else {
        false
      }
    })
    good ++ these.filter(_.isDirectory).flatMap(findJarsByPathAndAddToClasspath)
  }

  /**
   * Adds a file to the classpath of the application.
   * @param file to add in the classpath.
   */
  protected def addToClasspath(file: File): Unit = {
    val method: Method = classOf[URLClassLoader].getDeclaredMethod("addURL", classOf[URL])
    method.setAccessible(true)
    method.invoke(ClassLoader.getSystemClassLoader, file.toURI.toURL);
  }

  /**
   * Destroys Spark's context.
   */
  def shutdown: Unit = {
    SparkContextFactory.destroySparkContext
    system.shutdown
  }
}
