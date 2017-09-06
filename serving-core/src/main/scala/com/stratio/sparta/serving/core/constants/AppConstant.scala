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

import akka.actor.ActorSystem
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.utils.ZookeeperUtils

import scala.util.Properties

/**
 * Global constants of the application.
 */
object AppConstant extends ZookeeperUtils {

  val version = "1.8.0-SNAPSHOT"
  val ConfigAppName = "sparta"
  val ConfigApi = "api"
  val ConfigHdfs = "hdfs"
  val ConfigDetail = "config"
  val ConfigOauth2 = "oauth2"
  val ConfigSpray = "spray.can.server"
  val ConfigZookeeper = "zookeeper"
  val ConfigFrontend = "config.frontend"
  val DefaultOauth2CookieName = "user"
  val ConfigLocal = "local"
  val ConfigMesos = "mesos"
  val ConfigMarathon = "marathon"
  val DefaultRememberPartitioner = true
  val DriverPackageLocation = "driverPackageLocation"
  val BackupsLocation = "backupsLocation"
  val DefaultDriverPackageLocation = "/opt/sds/sparta/driver"
  val DefaultBackupsLocation = "/opt/sds/sparta/backups"
  val DriverURI = "driverURI"
  val DefaultMarathonDriverURI = "/opt/sds/sparta/driver/sparta-driver.jar"
  val DefaultDriverLocation = "provided"
  val PluginsPackageLocation = "pluginPackageLocation"
  val DefaultPluginsPackageLocation = "/opt/sds/sparta/plugins"
  val DefaultFrontEndTimeout = 20000
  val ConfigSecurity = "security"
  val AwaitPolicyChangeStatus = "awaitPolicyChangeStatus"
  val DefaultAwaitPolicyChangeStatus = "360s"
  val PreStopMarathonDelay = "preStopMarathonDelay"
  val DefaultPreStopMarathonDelay = "10s"
  val PreStopMarathonInterval = "preStopMarathonInterval"
  val DefaultPreStopMarathonInterval = "5s"
  val DefaultkillUrl = "http://127.0.0.1:7077/v1/submissions/kill"

  //Hdfs Options
  val HadoopUserName = "hadoopUserName"
  val HdfsMaster = "hdfsMaster"
  val HdfsPort = "hdfsPort"
  val DefaultHdfsUser = "sparta"
  val KeytabPath = "keytabPath"
  val PrincipalName = "principalName"
  val ReloadKeyTabTime = "reloadKeyTabTime"
  val ReloadKeyTab = "reloadKeyTab"
  val DefaultReloadKeyTab = false
  val DefaultReloadKeyTabTime = "23h"
  val SystemHadoopConfDir = "HADOOP_CONF_DIR"
  val CoreSite = "core-site.xml"
  val HDFSSite = "hdfs-site.xml"
  val SystemHadoopUserName = "HADOOP_USER_NAME"
  val SystemPrincipalName = "SPARTA_PRINCIPAL_NAME"
  val SystemKeyTabPath = "SPARTA_KEYTAB_PATH"
  val SystemHostName = "HOSTNAME"

  //Zookeeper
  val ZKConnection = "connectionString"
  val DefaultZKConnection = "127.0.0.1:2181"
  val ZKConnectionTimeout = "connectionTimeout"
  val DefaultZKConnectionTimeout = 19000
  val ZKSessionTimeout = "sessionTimeout"
  val DefaultZKSessionTimeout = 60000
  val ZKRetryAttemps = "retryAttempts"
  val DefaultZKRetryAttemps = 5
  val ZKRetryInterval = "retryInterval"
  val DefaultZKRetryInterval = 10000
  val DefaultZKPath = "/stratio/sparta/sparta"

  //Zookeeper paths
  val instanceName = Properties.envOrNone("MARATHON_APP_LABEL_DCOS_SERVICE_NAME")
  lazy val BaseZkPath: String = (retrievePathFromEnv, instanceName, retrieveFromConf) match {
    case (Some(path), _, _ ) if checkIfValidPath(path) => path
    case (_, Some(instance), _)=> s"/stratio/sparta/$instance"
    case (_, _, Some(confPath)) if checkIfValidPath(confPath) => confPath
    case _ => DefaultZKPath
  }

  lazy val WorkflowsZkPath = s"$BaseZkPath/workflows"
  lazy val WorkflowStatusesZkPath = s"$BaseZkPath/workflowStatuses"
  lazy val WorkflowExecutionsZkPath = s"$BaseZkPath/workflowExecutions"
  lazy val TemplatesZkPath = s"$BaseZkPath/templates"
  lazy val ErrorsZkPath = s"$BaseZkPath/error"

  //Scheduler system to schedule threads executions
  val SchedulerSystem = ActorSystem("SchedulerSystem", SpartaConfig.daemonicAkkaConfig)
  val CustomTypeKey = "modelType"
}
