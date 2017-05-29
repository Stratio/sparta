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

/**
 * Global constants of the application.
 */
object AppConstant {

  val version = "1.5.0"

  //Config keys
  val ClasspathJarFolder = "repo"
  val ConfigAppName = "sparta"
  val ConfigApi = "api"
  val ConfigHdfs = "hdfs"
  val ConfigDetail = "config"
  val ConfigOauth2 = "oauth2"
  val ConfigSpray = "spray.can.server"
  val ConfigZookeeper = "zookeeper"
  val ConfigFrontend = "config.frontend"
  val DefaultOauth2CookieName = "user"

  //Config Options
  val ExecutionMode = "executionMode"
  val ConfigLocal = "local"
  val ConfigMesos = "mesos"
  val ConfigMarathon = "marathon"
  val ConfigRememberPartitioner = "rememberPartitioner"
  val DefaultRememberPartitioner = true
  val DriverPackageLocation = "driverPackageLocation"
  val BackupsLocation = "backupsLocation"
  val DefaultDriverPackageLocation = "/opt/sds/sparta/driver/"
  val DefaultBackupsLocation = "/opt/sds/sparta/backups/"
  val DriverURI = "driverURI"
  val DefaultProvidedDriverURI = "http://0.0.0.0:9090/driver/sparta-driver.jar"
  val DefaultMarathonDriverURI = "/opt/sds/sparta/driver/sparta-driver.jar"
  val DefaultDriverLocation = "provided"
  val PluginsPackageLocation = "pluginPackageLocation"
  val DefaultPluginsPackageLocation = "/opt/sds/plugins/"
  val DefaultFrontEndTimeout = 10000

  //killing options
  val AwaitPolicyChangeStatus = "awaitPolicyChangeStatus"
  val DefaultAwaitPolicyChangeStatus = "180s"
  val PreStopMarathonDelay = "preStopMarathonDelay"
  val DefaultPreStopMarathonDelay = "10s"
  val PreStopMarathonInterval = "preStopMarathonInterval"
  val DefaultPreStopMarathonInterval = "5s"


  //Checkpoint
  val ConfigAutoDeleteCheckpoint = "autoDeleteCheckpoint"
  val DefaultAutoDeleteCheckpoint = true
  val ConfigAddTimeToCheckpointPath = "addTimeToCheckpointPath"
  val DefaultAddTimeToCheckpointPath = false
  val ConfigCheckpointPath = "checkpointPath"
  val DefaultCheckpointPath = "sparta/checkpoint"
  val DefaultCheckpointPathLocalMode = s"/tmp/$DefaultCheckpointPath"
  val DefaultCheckpointPathClusterMode = "/user/"

  //Hdfs Options
  val HadoopUserName = "hadoopUserName"
  val HdfsMaster = "hdfsMaster"
  val HdfsPort = "hdfsPort"
  val DefaultHdfsUser = "stratio"
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

  //Generic Options
  val Master = "master"
  val Supervise = "supervise"
  val DeployMode = "deployMode"
  val Name = "name"
  val PropertiesFile = "propertiesFile"
  val TotalExecutorCores = "totalExecutorCores"
  val SparkHome = "sparkHome"
  val Packages = "packages"
  val ExcludePackages = "exclude-packages"
  val Repositories = "repositories"
  val Jars = "jars"
  val ProxyUser = "proxy-user"
  val DriverJavaOptions = "driver-java-options"
  val DriverLibraryPath = "driver-library-path"
  val DriverClassPath = "driver-class-path"
  val ClusterValue = "cluster"
  val ClientValue = "client"
  val MarathonValue = "marathon"
  val LocalValue = "local"
  val KillUrl = "killUrl"
  val DefaultkillUrl = "http://127.0.0.1:7077/v1/submissions/kill"

  //Mesos Options
  val MesosMasterDispatchers = "master"

  //Yarn
  val YarnQueue = "queue"
  val NumExecutors = "numExecutors"
  val ExecutorMemory = "executorMemory"
  val ExecutorCores = "executorCores"
  val DriverMemory = "driverMemory"
  val DriverCores = "driverCores"
  val Files = "files"
  val Archives = "archives"
  val AddJars = "addJars"

  //Zookeeper
  val ZKConnection = "connectionString"
  val DefaultZKConnection = "127.0.0.1:2181"
  val ZKConnectionTimeout = "connectionTimeout"
  val DefaultZKConnectionTimeout = 15000
  val ZKSessionTimeout = "sessionTimeout"
  val DefaultZKSessionTimeout = 60000
  val ZKRetryAttemps = "retryAttempts"
  val DefaultZKRetryAttemps = 5
  val ZKRetryInterval = "retryInterval"
  val DefaultZKRetryInterval = 10000

  //Zookeeper paths
  val BaseZKPath = "/stratio/sparta"
  val PoliciesBasePath = s"$BaseZKPath/policies"
  val ContextPath = s"$BaseZKPath/contexts"
  val ExecutionsPath = s"$BaseZKPath/executions"
  val FragmentsPath = s"$BaseZKPath/fragments"
  val ErrorsZkPath = s"$BaseZKPath/error"

  //Scheduler system to schedule threads executions
  val SchedulerSystem = ActorSystem("SchedulerSystem", SpartaConfig.daemonicAkkaConfig)

  val CustomTypeKey = "modelType"
}
