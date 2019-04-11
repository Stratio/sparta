/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.row

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.helpers.{CastingHelper, SdkSchemaHelper}
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions}
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.InputStep
import com.stratio.sparta.plugin.models.RowModel
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

//scalastyle:off
abstract class RowGeneratorInputStep[Underlying[Row]](
                                                name: String,
                                                outputOptions: OutputOptions,
                                                ssc: Option[StreamingContext],
                                                xDSession: XDSession,
                                                properties: Map[String, JSerializable]
                                              )
  extends InputStep[Underlying](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val rowsModel: Seq[RowModel] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()

    properties.getString("rows", None).notBlank.fold(Seq.empty[RowModel]) { rows =>
      read[Seq[RowModel]](s"""$rows"""")
    }
  }
  lazy val outputSchema: StructType = {
    rowsModel.headOption match {
      case Some(rowModel) =>
        val fields = rowModel.fields.map { field =>
          val outputType = SparkTypes.get(field.`type`) match {
            case Some(sparkType) => sparkType
            case None => schemaFromString(field.`type`)
          }
          StructField(field.fieldName, outputType, nullable = true)
        }
        StructType(fields)
      case None =>
        StructType(Seq.empty)
    }
  }
  lazy val rowsToGenerate: Seq[Row] =
    rowsModel.map { rowModel =>
      val values = rowModel.fields.map { field =>
        val fieldSchema = outputSchema.fields.find(schemaField => field.fieldName == schemaField.name)
          .getOrElse(throw new Exception(s"Field: ${field.fieldName} not found in the schema"))

        CastingHelper.castingToSchemaType(fieldSchema.dataType, field.fieldValue.orNull.asInstanceOf[Any])
      }.toArray
      new GenericRowWithSchema(values, outputSchema)
    }

  def rowsWithTheSameFields: Boolean = {
    rowsModel.forall(rows => rowsModel.contains(rows))
  }

  override def validate(options: Map[String, String]): ErrorValidations = {
    val validationSeq = Seq[(HasError, String)](
      !SdkSchemaHelper.isCorrectTableName(name) -> s"The step name $name is not valid.",
      rowsModel.isEmpty -> "It's mandatory to add at least one row.",
      !rowsWithTheSameFields -> "All rows must have the same fields.",
      (debugOptions.isDefined && !validDebuggingOptions) -> errorDebugValidation
    )

    ErrorValidationsHelper.validate(validationSeq, name)
  }
}

class RowGeneratorInputStepBatch(
                           name: String,
                           outputOptions: OutputOptions,
                           ssc: Option[StreamingContext],
                           xDSession: XDSession,
                           properties: Map[String, JSerializable]
                         ) extends RowGeneratorInputStep[RDD](name, outputOptions, ssc, xDSession, properties) {

  //Dummy function on batch inputs that generates DataSets with schema
  def init(): DistributedMonad[RDD] = {
    throw new Exception("Not used on inputs that generates DataSets with schema")
  }

  override def initWithSchema(): (DistributedMonad[RDD], Option[StructType]) = {
    val inputRDD = xDSession.sparkContext.parallelize(rowsToGenerate)

    (inputRDD, Option(outputSchema))
  }
}
