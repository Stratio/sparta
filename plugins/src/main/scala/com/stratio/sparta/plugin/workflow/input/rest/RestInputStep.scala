/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.rest

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions}
import com.stratio.sparta.core.workflow.step.InputStep
import com.stratio.sparta.plugin.common.rest.RestConfig
import com.stratio.sparta.plugin.helper.ErrorValidationsHelper
import com.stratio.sparta.plugin.helper.ErrorValidationsHelper.HasError
import org.apache.spark.sql.{Row, RowFactory}
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext

abstract class RestInputStep[Underlying[Row]](name: String,
                             outputOptions: OutputOptions,
                             ssc: Option[StreamingContext],
                             xDSession: XDSession,
                             properties: Map[String, JSerializable]
                            )
  extends InputStep[Underlying](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val restConfig: RestConfig = RestConfig(properties)
  lazy val responseStringSchema = StructType(Seq(StructField(restConfig.httpOutputField, StringType)))
  val triggerRow: Row = RowFactory.create(Array("Send request"))

  override def validate(options: Map[String, String]): ErrorValidations = {

    val validationSeq = Seq[(HasError,String)](
      !SdkSchemaHelper.isCorrectTableName(name) -> s"The step name $name is not valid",
      (debugOptions.isDefined && !validDebuggingOptions) -> errorDebugValidation,
      restConfig.url.isEmpty -> "The Url cannot be empty",
      (restConfig.url.nonEmpty && RestConfig.urlRegex.findFirstIn(restConfig.url.get).isEmpty) -> "Url must be valid and start with http(s)://",
      restConfig.requestTimeout.isFailure -> "Request timeout is not valid"
    ) ++   RestConfig.validateProperties(properties)

    ErrorValidationsHelper.validate(validationSeq, name)
  }
}
