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

import com.stratio.sparta.sdk.properties.JsoneyString

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
                           mesosConstraint: Option[JsoneyString] = None,
                           mesosConstraintOperator: Option[JsoneyString] = None
                         )

case class CheckpointSettings(
                               checkpointPath: JsoneyString = JsoneyString("sparta/checkpoint"),
                               enableCheckpointing: Boolean = true,
                               autoDeleteCheckpoint: Boolean = true,
                               addTimeToCheckpointPath: Boolean = false
                             )

case class StreamingSettings(
                              window: JsoneyString = JsoneyString("2s"),
                              remember: Option[JsoneyString] = None,
                              backpressure: Option[Boolean] = None,
                              backpressureInitialRate: Option[JsoneyString] = None,
                              stopGracefully: Option[Boolean] = None,
                              stopGracefulTimeout: Option[JsoneyString] = None,
                              checkpointSettings: CheckpointSettings
                            )

case class SparkSettings(
                          master: JsoneyString =  JsoneyString("mesos://leader.mesos:5050"),
                          sparkKerberos: Boolean = false,
                          sparkDataStoreTls: Boolean = false,
                          sparkMesosSecurity: Boolean = false,
                          killUrl: Option[JsoneyString] = None,
                          submitArguments: SubmitArguments,
                          sparkConf: SparkConf
                        )

case class SubmitArguments(
                            userArguments: Seq[UserSubmitArgument] = Seq.empty[UserSubmitArgument],
                            deployMode: Option[String] = Option("client"),
                            supervise: Option[Boolean] = None,
                            jars: Option[JsoneyString] = None,
                            propertiesFile: Option[JsoneyString] = None,
                            packages: Option[JsoneyString] = None,
                            excludePackages: Option[JsoneyString] = None,
                            repositories: Option[JsoneyString] = None,
                            proxyUser: Option[JsoneyString] = None,
                            driverJavaOptions: Option[JsoneyString] = Option(JsoneyString("-XX:+UseConcMarkSweepGC -Dlog4j.configurationFile=file:///etc/sds/sparta/log4j2.xml")),
                            driverLibraryPath: Option[JsoneyString] = None,
                            driverClassPath: Option[JsoneyString] = None
                          )

case class SparkConf(
                      sparkResourcesConf: SparkResourcesConf,
                      sparkDockerConf: SparkDockerConf,
                      sparkMesosConf: SparkMesosConf,
                      userSparkConf: Seq[SparkProperty] = Seq.empty[SparkProperty],
                      coarse: Option[Boolean] = None,
                      sparkUser: Option[JsoneyString] = None,
                      sparkLocalDir: Option[JsoneyString] = None,
                      sparkKryoSerialization: Option[Boolean] = None,
                      sparkSqlCaseSensitive: Option[Boolean] = None,
                      parquetBinaryAsString: Option[Boolean] = None,
                      logStagesProgress: Option[Boolean] = None,
                      executorExtraJavaOptions: Option[JsoneyString] = Option(JsoneyString("-XX:+UseConcMarkSweepGC"))
                    )

case class SparkResourcesConf(
                               coresMax: Option[JsoneyString] = Option(JsoneyString("2")),
                               executorMemory: Option[JsoneyString] = Option(JsoneyString("2G")),
                               executorCores: Option[JsoneyString] = Option(JsoneyString("1")),
                               driverCores: Option[JsoneyString] = Option(JsoneyString("1")),
                               driverMemory: Option[JsoneyString] = Option(JsoneyString("2G")),
                               mesosExtraCores: Option[JsoneyString] = None,
                               localityWait: Option[JsoneyString] = Option(JsoneyString("100")),
                               taskMaxFailures: Option[JsoneyString] = Option(JsoneyString("8")),
                               sparkMemoryFraction: Option[JsoneyString] = Option(JsoneyString("0.6")),
                               sparkParallelism: Option[JsoneyString] = None
                             )

case class SparkDockerConf(
                            executorDockerImage: Option[JsoneyString] =
                            Option(JsoneyString("qa.stratio.com/stratio/stratio-spark:2.2.0.4")),
                            executorDockerVolumes: Option[JsoneyString] =
                            Option(JsoneyString("/opt/mesosphere/packages/:/opt/mesosphere/packages/:ro," +
                              "/opt/mesosphere/lib/:/opt/mesosphere/lib/:ro," +
                              "/etc/pki/ca-trust/extracted/java/cacerts/:" +
                              "/usr/lib/jvm/jre1.8.0_112/lib/security/cacerts:ro," +
                              "/etc/resolv.conf:/etc/resolv.conf:ro")),
                            executorForcePullImage: Option[Boolean] = None
                          )

case class SparkMesosConf(
                           mesosNativeJavaLibrary: Option[JsoneyString] = Option(
                             JsoneyString("/opt/mesosphere/lib/libmesos.so")),
                           mesosHDFSConfURI: Option[JsoneyString] = None
                         )

case class UserSubmitArgument(
                               submitArgument: JsoneyString,
                               submitValue: JsoneyString
                             )

case class UserJar(jarPath: JsoneyString)

case class SqlSentence(sentence: JsoneyString)

case class SparkProperty(sparkConfKey: JsoneyString, sparkConfValue: JsoneyString)