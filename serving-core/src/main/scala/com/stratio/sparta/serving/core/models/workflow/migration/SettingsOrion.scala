/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow.migration

import com.stratio.sparta.core.models.ErrorsManagement
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.serving.core.models.workflow._


case class SettingsOrion(
                     global: GlobalSettings = GlobalSettings(),
                     streamingSettings: StreamingSettings = StreamingSettings(),
                     sparkSettings: SparkSettingsOrion = SparkSettingsOrion(),
                     errorsManagement: ErrorsManagement = ErrorsManagement()
                   )

case class SparkSettingsOrion(
                          master: JsoneyString = JsoneyString("mesos://zk://master.mesos:2181/mesos"),
                          sparkKerberos: Boolean = true,
                          sparkDataStoreTls: Boolean = true,
                          sparkMesosSecurity: Boolean = true,
                          killUrl: Option[JsoneyString] = None,
                          submitArguments: SubmitArguments = SubmitArguments(),
                          sparkConf: SparkConfOrion = SparkConfOrion()
                        )


case class SparkConfOrion(
                      sparkResourcesConf: SparkResourcesConf = SparkResourcesConf(),
                      userSparkConf: Seq[SparkProperty] = Seq.empty[SparkProperty],
                      coarse: Option[Boolean] = None,
                      sparkUser: Option[JsoneyString] = None,
                      sparkLocalDir: Option[JsoneyString] = None,
                      executorDockerImage: Option[JsoneyString] = Option(JsoneyString(
                        "qa.stratio.com/stratio/spark-stratio-driver:2.2.0-2.1.0-f969ad8")),
                      sparkKryoSerialization: Option[Boolean] = None,
                      sparkSqlCaseSensitive: Option[Boolean] = None,
                      logStagesProgress: Option[Boolean] = None,
                      hdfsTokenCache: Option[Boolean] = None,
                      executorExtraJavaOptions: Option[JsoneyString] = Option(JsoneyString(
                        "-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:+UseConcMarkSweepGC"))
                    )
