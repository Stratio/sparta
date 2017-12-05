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

package com.stratio.sparta.serving.core.models.workflow

case class Settings(
                     global: GlobalSettings,
                     streamingSettings: StreamingSettings,
                     sparkSettings: SparkSettings
                   )

case class GlobalSettings(
                           executionMode: String = "marathon",
                           userPluginsJars: Seq[UserJar] = Seq.empty[UserJar],
                           initSqlSentences: Seq[SqlSentence] = Seq.empty[SqlSentence],
                           addAllUploadedPlugins: Boolean = true,
                           mesosConstraint: Option[String] = None,
                           mesosConstraintOperator: Option[String] = None
                         )

case class CheckpointSettings(
                               checkpointPath: String = "sparta/checkpoint",
                               enableCheckpointing: Boolean = true,
                               autoDeleteCheckpoint: Boolean = true,
                               addTimeToCheckpointPath: Boolean = false
                             )

case class StreamingSettings(
                              window: String = "2s",
                              remember: Option[String] = None,
                              backpressure: Option[Boolean] = None,
                              backpressureInitialRate: Option[String] = None,
                              checkpointSettings: CheckpointSettings
                            )

case class SparkSettings(
                          master: String = "mesos://leader.mesos:5050",
                          sparkKerberos: Boolean = false,
                          sparkDataStoreTls: Boolean = false,
                          sparkMesosSecurity: Boolean = false,
                          killUrl: Option[String] = None,
                          submitArguments: SubmitArguments,
                          sparkConf: SparkConf
                        )

case class SubmitArguments(
                            userArguments: Seq[UserSubmitArgument] = Seq.empty[UserSubmitArgument],
                            deployMode: Option[String] = Option("client"),
                            supervise: Option[Boolean] = Option(false),
                            jars: Option[String] = None,
                            propertiesFile: Option[String] = None,
                            packages: Option[String] = None,
                            excludePackages: Option[String] = None,
                            repositories: Option[String] = None,
                            proxyUser: Option[String] = None,
                            driverJavaOptions: Option[String] = Option("-XX:+UseConcMarkSweepGC"),
                            driverLibraryPath: Option[String] = None,
                            driverClassPath: Option[String] = None
                          )

case class SparkConf(
                      sparkResourcesConf: SparkResourcesConf,
                      sparkDockerConf: SparkDockerConf,
                      sparkMesosConf: SparkMesosConf,
                      userSparkConf: Seq[SparkProperty] = Seq.empty[SparkProperty],
                      coarse: Option[Boolean] = Option(true),
                      stopGracefully: Option[Boolean] = Option(true),
                      stopGracefulTimeout: Option[String] = None,
                      sparkUser: Option[String] = None,
                      sparkLocalDir: Option[String] = None,
                      parquetBinaryAsString: Option[Boolean] = None,
                      executorExtraJavaOptions: Option[String] = Option("-XX:+UseConcMarkSweepGC")
                    )

case class SparkResourcesConf(
                               coresMax: Option[String] = Option("2"),
                               executorMemory: Option[String] = Option("2G"),
                               executorCores: Option[String] = Option("1"),
                               driverCores: Option[String] = Option("1"),
                               driverMemory: Option[String] = Option("2G"),
                               mesosExtraCores: Option[String] = None,
                               localityWait: Option[String] = Option("100"),
                               taskMaxFailures: Option[String] = Option("8"),
                               sparkMemoryFraction: Option[String] = Option("0.6")
                             )

case class SparkDockerConf(
                            executorDockerImage: Option[String] =
                            Option("qa.stratio.com/stratio/stratio-spark:2.1.0.4"),
                            executorDockerVolumes: Option[String] =
                            Option("/opt/mesosphere/packages/:/opt/mesosphere/packages/:ro," +
                              "/opt/mesosphere/lib/:/opt/mesosphere/lib/:ro"),
                            executorForcePullImage: Option[Boolean] = Option(false)
                          )

case class SparkMesosConf(
                           mesosNativeJavaLibrary: Option[String] = Option("/opt/mesosphere/lib/libmesos.so"),
                           mesosHDFSConfURI: Option[String] = None
                         )

case class UserSubmitArgument(
                               submitArgument: String,
                               submitValue: String
                             )

case class UserJar(jarPath: String)

case class SqlSentence(sentence: String)

case class SparkProperty(sparkConfKey: String, sparkConfValue: String)