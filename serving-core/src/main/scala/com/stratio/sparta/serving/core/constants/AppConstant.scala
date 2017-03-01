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

  //Config keys
  val ClasspathJarFolder = "repo"
  val ConfigAppName = "sparta"
  val ConfigApi = "api"
  val ConfigHdfs = "hdfs"
  val ConfigDetail = "config"
  val ConfigAkka = "akka"
  val ConfigSpray = "spray.can.server"
  val ConfigZookeeper = "zookeeper"

  //Config Options
  val ExecutionMode = "executionMode"
  val ConfigLocal = "local"
  val ConfigStandAlone = "standalone"
  val ConfigMesos = "mesos"
  val ConfigYarn = "yarn"
  val ConfigMarathon = "marathon"
  val ConfigRememberPartitioner = "rememberPartitioner"
  val DefaultRememberPartitioner = true
  val ConfigStopGracefully = "stopGracefully"
  val DefaultStopGracefully = true
  val AwaitPolicyChangeStatus = "awaitPolicyChangeStatus"
  val DefaultAwaitPolicyChangeStatus = "120s"
  val DriverPackageLocation = "driverPackageLocation"
  val DefaultDriverPackageLocation = "/opt/sds/sparta/driver/"
  val DriverURI = "driverURI"
  val DefaultProvidedDriverURI = "http://sparta:9090/driverJar/driver-plugin.jar"
  val DefaultDriverLocation = "provided"
  val PluginsPackageLocation = "pluginPackageLocation"
  val DefaultPluginsPackageLocation = "/opt/sds/plugins/"

  //Checkpooint
  val ConfigAutoDeleteCheckpoint = "autoDeleteCheckpoint"
  val DefaultAutoDeleteCheckpoint = false
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
  val PrincipalNamePrefix = "principalNamePrefix"
  val PrincipalNameSuffix = "principalNameSuffix"
  val ReloadKeyTabTime = "reloadKeyTabTime"
  val ReloadKeyTab = "reloadKeyTab"
  val DefaultReloadKeyTab = false
  val DefaultReloadKeyTabTime = "23h"
  val SystemHadoopConfDir = "HADOOP_CONF_DIR"
  val CoreSite = "core-site.xml"
  val HDFSSite = "hdfs-site.xml"
  val SystemHadoopUserName = "HADOOP_USER_NAME"
  val SystemPrincipalName = "HADOOP_PRINCIPAL_NAME"
  val SystemKeyTabPath = "HADOOP_KEYTAB_PATH"
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
  val LocalValue = "local"
  val KillUrl = "killUrl"
  val DefaultkillUrl = "http://mesosDispatcherURL/v1/submissions/kill"

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
  val ZookeeperConnection = "connectionString"
  val DefaultZookeeperConnection = "localhost:2181"
  val ZookeeperConnectionTimeout = "connectionTimeout"
  val DefaultZookeeperConnectionTimeout = 15000
  val ZookeeperSessionTimeout = "sessionTimeout"
  val DefaultZookeeperSessionTimeout = 60000
  val ZookeeperRetryAttemps = "retryAttempts"
  val DefaultZookeeperRetryAttemps = 5
  val ZookeeperRetryInterval = "retryInterval"
  val DefaultZookeeperRetryInterval = 10000

  //Zookeeper paths
  val BaseZKPath = "stratio/sparta"
  val PoliciesBasePath = s"/$BaseZKPath/policies"
  val ContextPath = s"/$BaseZKPath/contexts"
  val FragmentsPath = s"/$BaseZKPath/fragments"
  val ErrorsZkPath = s"$BaseZKPath/error"

  //Scheduler system to schedule threads executions
  val SchedulerSystem = ActorSystem("SchedulerSystem", SpartaConfig.daemonicAkkaConfig)

  val CustomTypeKey = "modelType"
}
