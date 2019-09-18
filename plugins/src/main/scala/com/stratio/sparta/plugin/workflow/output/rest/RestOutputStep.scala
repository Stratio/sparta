/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.rest

import java.io.{Serializable => JSerializable}

import akka.http.scaladsl.model.HttpMethods
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.plugin.common.rest.SparkExecutorRestUtils.SparkExecutorRestUtils
import com.stratio.sparta.plugin.common.rest.{RestConfig, RestGraph, RestUtils, SparkExecutorRestUtils}
import com.stratio.sparta.plugin.enumerations.FieldsPreservationPolicy
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class RestOutputStep(
                      name: String,
                      xDSession: XDSession,
                      properties: Map[String, JSerializable]
                    ) extends OutputStep(name, xDSession, properties) {

  val conf = xDSession.conf.getAll

  lazy val restConfig: RestConfig = RestConfig(properties)

  val urlUnwrapped: String = restConfig.url.get

  lazy val restFieldSchema = StructType(StructField(restConfig.httpOutputField, StringType, true) :: Nil)

  override def supportedSaveModes: Seq[SaveModeEnum.Value] = Seq(SaveModeEnum.Append)

  /**
    * Save function that implements the plugins.
    *
    * @param dataFrame The dataFrame to save
    * @param saveMode  The sparta save mode selected
    * @param options   Options to save the data (partitionBy, primaryKey ... )
    */
  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    validateSaveMode(saveMode)

    val replaceableFields =
      RestUtils.preProcessingInputFields(urlUnwrapped, restConfig.bodyString, restConfig.bodyFormat, dataFrame.schema)

    dataFrame.rdd.foreachPartition { rowIterator =>
      val restUtils: SparkExecutorRestUtils =
        SparkExecutorRestUtils.getOrCreate(restConfig.akkaHttpProperties, conf)

      import restUtils.Implicits._

      // We wait for the future containing our rows & results to complete ...
      Await.result(
        RestGraph(restConfig, restUtils)
          .createOutputGraph(rowIterator, replaceableFields.uri, replaceableFields.body, None).run(), Duration.Inf)
    }
  }

  override def validate(options: Map[String, String] = Map.empty): ErrorValidations = {
    val validationSeq = Seq[(HasError,String)](
      !SdkSchemaHelper.isCorrectTableName(name) -> s"The step name $name is not valid",
      restConfig.url.isEmpty -> "The Url cannot be empty",
      (restConfig.method == HttpMethods.GET) -> "Cannot use GET as HTTP Method in the output step",
      (restConfig.url.nonEmpty && RestConfig.urlRegex.findFirstIn(restConfig.url.get).isEmpty) -> "Url must be valid and start with http(s)://",
      restConfig.requestTimeout.isFailure -> "Request timeout is not valid",
      (restConfig.preservationPolicy == FieldsPreservationPolicy.REPLACE) ->
        "Cannot use replace as preservation policy for this transformation"
    ) ++   RestConfig.validateProperties(properties)

    ErrorValidationsHelper.validate(validationSeq, name)
  }
}
