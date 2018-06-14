/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.avro

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.InputStep
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import com.databricks.spark.avro._
import com.stratio.sparta.plugin.helper.SchemaHelper
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper
import com.stratio.sparta.sdk.models.{ErrorValidations, OutputOptions, WorkflowValidationMessage}
import org.apache.avro.Schema
import org.apache.spark.sql.Row

class AvroInputStepBatch(
                          name: String,
                          outputOptions: OutputOptions,
                          ssc: Option[StreamingContext],
                          xDSession: XDSession,
                          properties: Map[String, JSerializable]
                        )
  extends InputStep[RDD](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val pathKey = "path"
  lazy val schemaKey = "schema.provided"
  lazy val path: Option[String] = properties.getString(pathKey, None)
  lazy val schemaProvided: Option[String] = properties.getString(schemaKey, None)
  lazy val avroSchema: Option[Schema] = schemaProvided.map(schema => AvroInputStepBatch.getAvroSchema(schema))

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The step name $name is not valid.", name)
      )

    if (path.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"the input path cannot be empty", name)
      )

    if(debugOptions.isDefined && !validDebuggingOptions)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"$errorDebugValidation", name)
      )

    validation
  }

  //Dummy function on batch inputs that generates DataSets with schema
  def init(): DistributedMonad[RDD] = {
    throw new Exception("Not used on inputs that generates DataSets with schema")
  }

  override def initWithSchema(): (DistributedMonad[RDD], Option[StructType]) = {
    require(path.nonEmpty, "The input path cannot be empty")

    val userOptions = propertiesWithCustom.flatMap { case (key, value) =>
      if (key != pathKey && key != schemaKey)
        Option(key -> value.toString)
      else None
    } ++ avroSchema.fold(Map.empty[String, String]) { schema => Map("avroSchema" -> schema.toString) }
    val df = xDSession.read.options(userOptions).avro(path.get)

    (df.rdd, Option(df.schema))
  }
}

object AvroInputStepBatch {

  private var schema: Option[Schema] = None

  def getAvroSchema(schemaProvided: String): Schema =
    schema.getOrElse {
      val newSchema = SchemaHelper.getAvroSchemaFromString(schemaProvided)
      schema = Option(newSchema)
      newSchema
    }
}


