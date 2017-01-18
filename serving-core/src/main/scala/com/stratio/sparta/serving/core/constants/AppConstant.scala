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
  final val ClasspathJarFolder = "repo"
  final val ConfigAppName = "sparta"
  final val ConfigApi = "api"
  final val ConfigHdfs = "hdfs"
  final val ConfigDetail = "config"
  final val ConfigAkka = "akka"
  final val ConfigSpray = "spray.can.server"
  final val ConfigSwagger = "swagger"
  final val ConfigZookeeper = "zookeeper"

  //Config Options
  final val ExecutionMode = "executionMode"
  final val ConfigLocal = "local"
  final val ConfigStandAlone = "standalone"
  final val ConfigMesos = "mesos"
  final val ConfigYarn = "yarn"
  final val ConfigRememberPartitioner = "rememberPartitioner"
  final val DefaultRememberPartitioner = true
  final val ConfigStopGracefully = "stopGracefully"
  final val DefaultStopGracefully = true
  final val SparkGracefullyStopProperty = "spark.streaming.stopGracefullyOnShutdown"
  final val AwaitStreamingContextStop = "awaitStreamingContextStop"
  final val AwaitSparkContextStop = "awaitSparkContextStop"
  final val AwaitPolicyChangeStatus = "awaitPolicyChangeStatus"
  final val DefaultAwaitStreamingContextStop = "120s"
  final val DefaultAwaitSparkContextStop = "60s"
  final val DefaultAwaitPolicyChangeStatus = "120s"
  final val ConfigAutoDeleteCheckpoint = "autoDeleteCheckpoint"
  final val DefaultAutoDeleteCheckpoint = true
  final val ConfigCheckpointPath = "checkpointPath"
  final val DefaultCheckpointPath = "sparta/checkpoint"
  final val DefaultCheckpointPathLocalMode = s"/tmp/$DefaultCheckpointPath"
  final val DefaultCheckpointPathClusterMode = "/user/"
  final val DriverPackageLocation = "driverPackageLocation"
  final val DefaultDriverPackageLocation = "/opt/sds/sparta/driver/"
  final val DefaultDriverFolder = "driver"
  final val DriverLocation = "driverLocation"
  final val DriverURI = "driverURI"
  final val DefaultProvidedDriverURI = "http://sparta:9090/driverJar/driver-plugin.jar"
  final val DefaultDriverLocation = "provided"
  final val PluginsLocation = "pluginsLocation"
  final val ProvidedPluginsLocation = "provided"
  final val LocalPluginsLocation = "local"

  //Hdfs Options
  final val HadoopUserName = "hadoopUserName"
  final val HdfsMaster = "hdfsMaster"
  final val HdfsPort = "hdfsPort"
  final val DefaultHdfsUser = "stratio"
  final val KeytabPath = "keytabPath"
  final val PrincipalName = "principalName"
  final val PrincipalNamePrefix = "principalNamePrefix"
  final val PrincipalNameSuffix = "principalNameSuffix"
  final val ReloadKeyTabTime = "reloadKeyTabTime"
  final val DefaultReloadKeyTabTime = "23h"
  final val SystemHadoopConfDir = "HADOOP_CONF_DIR"
  final val SystemHadoopUserName = "HADOOP_USER_NAME"
  final val SystemPrincipalName = "HADOOP_PRINCIPAL_NAME"
  final val SystemKeyTabPath = "HADOOP_KEYTAB_PATH"
  final val SystemHostName = "HOSTNAME"


  //Generic Options
  final val Master = "master"
  final val Supervise = "supervise"
  final val DeployMode = "deployMode"
  final val Name = "name"
  final val PropertiesFile = "propertiesFile"
  final val TotalExecutorCores = "totalExecutorCores"
  final val SparkHome = "sparkHome"
  final val Packages = "packages"
  final val ExcludePackages = "exclude-packages"
  final val Repositories = "repositories"
  final val Jars = "jars"
  final val ProxyUser = "proxy-user"
  final val DriverJavaOptions = "driver-java-options"
  final val DriverLibraryPath = "driver-library-path"
  final val DriverClassPath = "driver-class-path"

  //Mesos Options
  final val MesosMasterDispatchers = "master"

  //Yarn
  final val YarnQueue = "queue"
  final val NumExecutors = "numExecutors"
  final val ExecutorMemory = "executorMemory"
  final val ExecutorCores = "executorCores"
  final val DriverMemory = "driverMemory"
  final val DriverCores = "driverCores"
  final val Files = "files"
  final val Archives = "archives"
  final val AddJars = "addJars"

  //Zookeeper
  final val ZookeeperConnection = "connectionString"
  final val DefaultZookeeperConnection = "localhost:2181"
  final val ZookeeperConnectionTimeout = "connectionTimeout"
  final val DefaultZookeeperConnectionTimeout = 15000
  final val ZookeeperSessionTimeout = "sessionTimeout"
  final val DefaultZookeeperSessionTimeout = 60000
  final val ZookeeperRetryAttemps = "retryAttempts"
  final val DefaultZookeeperRetryAttemps = 5
  final val ZookeeperRetryInterval = "retryInterval"
  final val DefaultZookeeperRetryInterval = 10000

  //Zookeeper paths
  final val BaseZKPath = "stratio/sparta"
  final val ConfigZkPath = s"$BaseZKPath/config"
  final val PoliciesBasePath = s"/$BaseZKPath/policies"
  final val ContextPath = s"/$BaseZKPath/contexts"
  final val FragmentsPath = s"/$BaseZKPath/fragments"
  final val ErrorsZkPath = s"$BaseZKPath/error"

  //Scheduler system to schedule threads executions
  val SchedulerSystem = ActorSystem("SchedulerSystem", SpartaConfig.daemonicAkkaConfig)

}