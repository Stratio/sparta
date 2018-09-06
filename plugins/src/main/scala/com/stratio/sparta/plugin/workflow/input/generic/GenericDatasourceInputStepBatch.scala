/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.generic

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.InputStep
import com.stratio.sparta.plugin.helper.ErrorValidationsHelper
import com.stratio.sparta.plugin.helper.ErrorValidationsHelper.HasError
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext



class GenericDatasourceInputStepBatch(
                             name: String,
                             outputOptions: OutputOptions,
                             ssc: Option[StreamingContext],
                             xDSession: XDSession,
                             properties: Map[String, JSerializable]
                           )
  extends InputStep[RDD](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  // TODO datasource alternatives should be recovered from Classpath in order to avoid user errors
  lazy val datasource: Option[String] =
    properties.getString("datasource", None)


  override def validate(options: Map[String, String] = Map.empty): ErrorValidations = {

    val validationSeq = Seq[(HasError,String)](
      !SdkSchemaHelper.isCorrectTableName(name) -> s"The step name $name is not valid",
      (debugOptions.isDefined && !validDebuggingOptions) -> errorDebugValidation,
      datasource.isEmpty -> "The custom class field cannot be empty"
    )

    ErrorValidationsHelper.validate(validationSeq, name)
  }

  //Dummy function on batch inputs that generates DataSets with schema
  def init(): DistributedMonad[RDD] = {
    throw new Exception("Not used on inputs that generates DataSets with schema")
  }

  override def initWithSchema(): (DistributedMonad[RDD], Option[StructType]) = {

    val source: String =
      datasource.getOrElse(throw new RuntimeException("The input datasource format is mandatory"))

    val userOptions = propertiesWithCustom.mapValues(_.toString)
    val df = xDSession.read.format(source).options(userOptions).load()
    (df.rdd, Option(df.schema))
  }
}




