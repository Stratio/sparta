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

package com.stratio.sparta.plugin.workflow.input.crossdata

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{InputStep, OutputOptions}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext

import scala.language.implicitConversions

class CrossdataInputStepBatch(
                               name: String,
                               outputOptions: OutputOptions,
                               ssc: Option[StreamingContext],
                               xDSession: XDSession,
                               properties: Map[String, JSerializable]
                             )
  extends InputStep[RDD](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val query = properties.getString("query")

  def init(): DistributedMonad[RDD] = {
    xDSession.sql(query).rdd
  }

}

object CrossdataInputStepBatch {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }

}
