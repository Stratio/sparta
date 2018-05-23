/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.checkpointing

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SchemaHelper.{getSchemaFromSessionOrModel, getSchemaFromSessionOrModelOrRdd, parserInputSchema}
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper
import com.stratio.sparta.sdk.models.{DiscardCondition, ErrorValidations, OutputOptions, TransformationStepManagement}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.utils.AggregationTimeUtils
import com.stratio.sparta.sdk.workflow.step.TransformStep
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Duration, Milliseconds, StreamingContext}

import scala.util.Try

class CheckpointTransformStepStreaming(
                                        name: String,
                                        outputOptions: OutputOptions,
                                        transformationStepsManagement: TransformationStepManagement,
                                        ssc: Option[StreamingContext],
                                        xDSession: XDSession,
                                        properties: Map[String, JSerializable]
                                      )
  extends TransformStep[DStream](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  lazy val interval: Option[Duration] = Try(properties.getString("interval", None).notBlank.map(time =>
    Milliseconds(AggregationTimeUtils.parseValueToMilliSeconds(time)))).toOption.flatten

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the step name $name is not valid")

    //If contains schemas, validate if it can be parsed
    if (inputsModel.inputSchemas.nonEmpty) {
      inputsModel.inputSchemas.foreach { input =>
        if (parserInputSchema(input.schema).isFailure)
          validation = ErrorValidations(
            valid = false,
            messages = validation.messages :+ s"$name: the input schema from step ${input.stepName} is not valid")
      }

      inputsModel.inputSchemas.filterNot(is => SdkSchemaHelper.isCorrectTableName(is.stepName)).foreach { is =>
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ s"$name: the input table name ${is.stepName} is not valid")
      }
    }

    if (interval.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the interval time is invalid"
      )

    validation
  }

  override def transform(inputData: Map[String, DistributedMonad[DStream]]): DistributedMonad[DStream] =
    applyHeadTransform(inputData) { (_, inputStream) =>
      interval match {
        case Some(time) => inputStream.ds.checkpoint(time)
        case None => inputStream
      }
    }.ds.transform { rdd =>
      getSchemaFromSessionOrModel(xDSession, name, inputsModel)
        .orElse(getSchemaFromSessionOrModelOrRdd(xDSession, inputData.head._1, inputsModel, rdd.ds))
        .foreach(schema => xDSession.createDataFrame(rdd, schema).createOrReplaceTempView(name))
      rdd
    }

}