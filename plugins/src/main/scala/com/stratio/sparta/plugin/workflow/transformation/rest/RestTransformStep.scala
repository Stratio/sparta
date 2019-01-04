/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.rest

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.workflow.step.TransformStep
import com.stratio.sparta.plugin.common.rest._
import com.stratio.sparta.plugin.enumerations.FieldsPreservationPolicy
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StringType, _}
import org.apache.spark.streaming.StreamingContext


abstract class RestTransformStep[Underlying[Row]](
                                                   name: String,
                                                   outputOptions: OutputOptions,
                                                   transformationStepsManagement: TransformationStepManagement,
                                                   ssc: Option[StreamingContext],
                                                   xDSession: XDSession,
                                                   properties: Map[String, JSerializable]
                                                 )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  lazy val restConfig: RestConfig = RestConfig(properties)

  val urlUnwrapped: String = restConfig.url.get

  lazy val restFieldSchema = StructType(StructField(restConfig.httpOutputField, StringType, true) :: Nil)

  override def validate(options: Map[String, String] = Map.empty): ErrorValidations = {
    val validationSeq = Seq[(HasError,String)](
      !SdkSchemaHelper.isCorrectTableName(name) -> s"The step name $name is not valid",
      restConfig.url.isEmpty -> "The Url cannot be empty",
      (restConfig.url.nonEmpty && RestConfig.urlRegex.findFirstIn(restConfig.url.get).isEmpty) -> "Url must be valid and start with http(s)://",
      restConfig.requestTimeout.isFailure -> "Request timeout is not valid",
      (restConfig.preservationPolicy == FieldsPreservationPolicy.REPLACE) ->
        "Cannot use replace as preservation policy for this transformation"
    ) ++   RestConfig.validateProperties(properties)

    ErrorValidationsHelper.validate(validationSeq, name)
  }
}