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


object SparkConstant {

  // Properties mapped to Spark Configuration
  val SpartaDriverClass = "com.stratio.sparta.driver.SparkDriver"
  val SubmitDeployMode = "--deploy-mode"
  val SubmitName = "--name"
  val SubmitNameConf = "spark.app.name"
  val SubmitTotalExecutorCores = "--total-executor-cores"
  val SubmitTotalExecutorCoresConf = "spark.cores.max"
  val SubmitPackages = "--packages"
  val SubmitPackagesConf = "spark.jars.packages"
  val SubmitJars = "--jars"
  val SubmitJarsConf = "spark.jars"
  val SubmitDriverJavaOptions = "--driver-java-options"
  val SubmitDriverJavaOptionsConf = "spark.driver.extraJavaOptions"
  val SubmitDriverLibraryPath = "--driver-library-path"
  val SubmitDriverLibraryPathConf = "spark.driver.extraLibraryPath"
  val SubmitDriverClassPath = "--driver-class-path"
  val SubmitDriverClassPathConf = "spark.driver.extraClassPath"
  val SubmitExecutorClassPathConf = "spark.executor.extraClassPath"
  val SubmitExcludePackages = "--exclude-packages"
  val SubmitExcludePackagesConf = "spark.jars.excludes"
  val SubmitDriverCores = "--driver-cores"
  val SubmitDriverCoresConf = "spark.driver.cores"
  val SubmitDriverMemory = "--driver-memory"
  val SubmitDriverMemoryConf = "spark.driver.memory"
  val SubmitExecutorCores = "--executor-cores"
  val SubmitExecutorCoresConf = "spark.executor.cores"
  val SubmitExecutorMemory = "--executor-memory"
  val SubmitExecutorMemoryConf = "spark.executor.memory"
  val SubmitDriverCalicoNetworkConf = "spark.mesos.driver.docker.network.name"
  val SubmitExecutorCalicoNetworkConf = "spark.mesos.executor.docker.network.name"
  val SubmitGracefullyStopConf = "spark.streaming.stopGracefullyOnShutdown"
  val SubmitGracefullyStopTimeoutConf = "spark.streaming.gracefulStopTimeout"
  val SubmitAppNameConf = "spark.app.name"
  val SubmitSparkUserConf = "spark.mesos.driverEnv.SPARK_USER"
  val SubmitCoarseConf = "spark.mesos.coarse"
  val SubmitSerializerConf = "spark.serializer"
  val SubmitExecutorUriConf = "spark.executor.uri"
  val SubmitBinaryStringConf = "spark.sql.parquet.binaryAsString"
  val SubmitExtraCoresConf = "mesos.extra.cores"
  val SubmitLocalityWaitConf = "spark.locality.wait"
  val SubmitTaskMaxFailuresConf = "spark.task.maxFailures"
  val SubmitBlockIntervalConf = "spark.streaming.blockInterval"
  val SubmitConcurrentJobsConf = "spark.streaming.concurrentJobs"
  val SubmitExecutorDockerVolumeConf = "spark.mesos.executor.docker.volumes"
  val SubmitExecutorDockerForcePullConf = "spark.mesos.executor.docker.forcePullImage"
  val SubmitExecutorDockerImageConf = "spark.mesos.executor.docker.image"
  val SubmitMesosNativeLibConf = "spark.executorEnv.MESOS_NATIVE_JAVA_LIBRARY"
  val SubmitExecutorHomeConf = "spark.mesos.executor.home"
  val SubmitHdfsUriConf = "spark.mesos.driverEnv.HDFS_CONF_URI"
  val SubmitMesosPrincipalConf = "spark.mesos.principal"
  val SubmitMesosSecretConf = "spark.mesos.secret"
  val SubmitMesosRoleConf = "spark.mesos.role"
  val SubmitMasterConf = "spark.master"


  // Properties only available in spark-submit
  val SubmitPropertiesFile = "--properties-file"
  val SubmitRepositories = "--repositories"
  val SubmitProxyUser = "--proxy-user"
  val SubmitYarnQueue = "--queue"
  val SubmitFiles = "--files"
  val SubmitArchives = "--archives"
  val SubmitAddJars = "--addJars"
  val SubmitNumExecutors = "--num-executors"
  val SubmitPrincipal = "--principal"
  val SubmitKeyTab = "--keytab"
  val SubmitSupervise = "--supervise"

}
