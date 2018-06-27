/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.core.models.ErrorsManagement
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.serving.core.models.enumerators.DeployMode.DeployMode
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode.WorkflowExecutionMode
import com.stratio.sparta.serving.core.models.enumerators.{DeployMode, WorkflowExecutionMode}

case class Settings(
                     global: GlobalSettings = GlobalSettings(),
                     streamingSettings: StreamingSettings = StreamingSettings(),
                     sparkSettings: SparkSettings = SparkSettings(),
                     errorsManagement: ErrorsManagement = ErrorsManagement()
                   )

case class GlobalSettings(
                           executionMode: WorkflowExecutionMode = WorkflowExecutionMode.marathon,
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
                              backpressureMaxRate: Option[JsoneyString] = None,
                              blockInterval: Option[JsoneyString] = Option(JsoneyString("100ms")),
                              stopGracefully: Option[Boolean] = None,
                              stopGracefulTimeout: Option[JsoneyString] = None,
                              checkpointSettings: CheckpointSettings = CheckpointSettings()
                            )

case class SparkSettings(
                          master: JsoneyString = JsoneyString("mesos://leader.mesos:5050"),
                          sparkKerberos: Boolean = true,
                          sparkDataStoreTls: Boolean = true,
                          sparkMesosSecurity: Boolean = true,
                          killUrl: Option[JsoneyString] = None,
                          submitArguments: SubmitArguments = SubmitArguments(),
                          sparkConf: SparkConf = SparkConf()
                        )

case class SubmitArguments(
                            userArguments: Seq[UserSubmitArgument] = Seq.empty[UserSubmitArgument],
                            deployMode: Option[DeployMode] = Option(DeployMode.client),
                            driverJavaOptions: Option[JsoneyString] = Option(JsoneyString(
                              "-Dconfig.file=/etc/sds/sparta/spark/reference.conf -XX:+UseConcMarkSweepGC -Dlog4j.configurationFile=file:///etc/sds/sparta/log4j2.xml"))
                          )

case class SparkConf(
                      sparkResourcesConf: SparkResourcesConf = SparkResourcesConf(),
                      userSparkConf: Seq[SparkProperty] = Seq.empty[SparkProperty],
                      coarse: Option[Boolean] = None,
                      sparkUser: Option[JsoneyString] = None,
                      sparkLocalDir: Option[JsoneyString] = None,
                      executorDockerImage: Option[JsoneyString] = Option(JsoneyString(
                        "qa.stratio.com/stratio/spark-stratio-driver:2.2.0-1.0.0")),
                      sparkKryoSerialization: Option[Boolean] = None,
                      sparkSqlCaseSensitive: Option[Boolean] = None,
                      logStagesProgress: Option[Boolean] = None,
                      hdfsTokenCache: Option[Boolean] = None,
                      executorExtraJavaOptions: Option[JsoneyString] = Option(JsoneyString(
                        "-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:+UseConcMarkSweepGC"))
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

case class UserSubmitArgument(
                               submitArgument: JsoneyString,
                               submitValue: JsoneyString
                             )

case class UserJar(jarPath: JsoneyString)

case class SqlSentence(sentence: JsoneyString)

case class SparkProperty(sparkConfKey: JsoneyString, sparkConfValue: JsoneyString)
