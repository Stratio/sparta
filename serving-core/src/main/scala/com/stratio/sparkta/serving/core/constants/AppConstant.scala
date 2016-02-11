/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

package com.stratio.sparkta.serving.core.constants

import com.stratio.sparkta.sdk.Parser

/**
 * Global constants of the application.
 */
object AppConstant {

  final val JarPluginsFolder = "plugins"
  final val ClasspathJarFolder = "repo"
  final val ClusterExecutionJarFolder = "driver"
  final val ExecutionMode = "executionMode"
  final val ConfigAppName = "sparkta"
  final val ConfigApi = "api"
  final val ConfigHdfs = "hdfs"
  final val ConfigDetail = "config"
  final val ConfigLocal = "local"
  final val ConfigStandAlone = "standalone"
  final val ConfigMesos = "mesos"
  final val ConfigYarn = "yarn"
  final val ConfigAkka = "akka"
  final val ConfigSwagger = "swagger"
  final val ConfigZookeeper = "zookeeper"
  final val BaseZKPath = "stratio/sparkta"
  final val ConfigZkPath = s"$BaseZKPath/config"
  final val PoliciesBasePath = s"/$BaseZKPath/policies"
  final val ContextPath = s"/$BaseZKPath/contexts"
  final val ErrorsZkPath = s"$BaseZKPath/error"
  final val ConfigRememberPartitioner = "rememberPartitioner"
  final val DefaultRememberPartitioner = true
  final val ConfigStopGracefully = "stopGracefully"
  final val StopTimeout = "stopTimeout"
  final val DefaultStopTimeout = 30000

  // ZK id's
  final val HdfsID = "hdfs"

  //Hdfs Options
  final val HadoopUserName = "hadoopUserName"
  final val HadoopConfDir = "hadoopConfDir"
  final val HdfsMaster = "hdfsMaster"
  final val PluginsFolder = "pluginsFolder"
  final val ClasspathFolder = "classpathFolder"
  final val ExecutionJarFolder = "executionJarFolder"

  //Generic Options
  final val DeployMode = "deployMode"
  final val NumExecutors = "numExecutors"
  final val TotalExecutorCores = "totalExecutorCores"
  final val ExecutorMemory = "executorMemory"
  final val ExecutorCores = "executorCores"
  final val SparkHome = "sparkHome"

  //Mesos Options
  final val MesosMasterDispatchers = "masterDispatchers"

  //StandAlone
  final val StandAloneSupervise = "supervise"
  //Yarn
  final val YarnQueue = "queue"

  // Common
  final val Master = "master"

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

  final val pluginExtension = "-plugin.jar"
  final val jarsFilesMap = Map(
    "ArrayText" -> s"field-arrayText$pluginExtension",
    "DateTime" -> s"field-dateTime$pluginExtension",
    "Default" -> s"field-default$pluginExtension",
    "GeoHash" -> s"field-geoHash$pluginExtension",
    "Hierarchy" -> s"field-hierarchy$pluginExtension",
    "Tag" -> s"field-tag$pluginExtension",
    "TwitterStatus" -> s"field-twitter-status$pluginExtension",
    "Flume" -> s"input-flume$pluginExtension",
    "Kafka" -> s"input-kafka$pluginExtension",
    "Flume" -> s"input-flume$pluginExtension",
    "RabbitMQ" -> s"input-rabbitMQ$pluginExtension",
    "Socket" -> s"input-socket$pluginExtension",
    "Twitter" -> s"input-twitter$pluginExtension",
    "TwitterJson" -> s"input-twitter$pluginExtension",
    "WebSocket" -> s"input-websocket$pluginExtension",
    "Accumulator" -> s"operator-accumulator$pluginExtension",
    "Avg" -> s"operator-avg$pluginExtension",
    "Count" -> s"operator-count$pluginExtension",
    "EntityCount" -> s"operator-entitiyCount$pluginExtension",
    "FirsValue" -> s"operator-firstValue$pluginExtension",
    "FullText" -> s"operator-fullText$pluginExtension",
    "LastValue" -> s"operator-lastValue$pluginExtension",
    "Max" -> s"operator-max$pluginExtension",
    "Median" -> s"operator-median$pluginExtension",
    "Min" -> s"operator-min$pluginExtension",
    "Mode" -> s"operator-mode$pluginExtension",
    "Range" -> s"operator-range$pluginExtension",
    "Stddev" -> s"operator-stddev$pluginExtension",
    "Sum" -> s"operator-sum$pluginExtension",
    "TotalEntityCount" -> s"operator-totalEntityCount$pluginExtension",
    "Variance" -> s"operator-variance$pluginExtension",
    "Cassandra" -> s"output-cassandra$pluginExtension",
    "ElasticSearch" -> s"output-elasticsearch$pluginExtension",
    "MongoDb" -> s"output-mongodb$pluginExtension",
    "Parquet" -> s"output-parquet$pluginExtension",
    "Print" -> s"output-print$pluginExtension",
    "Redis" -> s"output-redis$pluginExtension",
    s"DateTime${Parser.ClassSuffix}" -> s"parser-datetime$pluginExtension",
    s"Morphlines${Parser.ClassSuffix}" -> s"parser-morphlines$pluginExtension",
    s"Ingestion${Parser.ClassSuffix}" -> s"parser-ingestion$pluginExtension",
    s"Type${Parser.ClassSuffix}" -> s"parser-type$pluginExtension"
  )
}
