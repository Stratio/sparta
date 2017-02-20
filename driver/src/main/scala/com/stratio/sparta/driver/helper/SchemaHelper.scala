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

package com.stratio.sparta.driver.helper

import com.stratio.sparta.driver.cube.Cube
import com.stratio.sparta.sdk.pipeline.schema.TypeOp._
import com.stratio.sparta.sdk.pipeline.aggregation.cube.{Dimension, ExpiringData}
import com.stratio.sparta.sdk.pipeline.aggregation.operator.Operator
import com.stratio.sparta.sdk.pipeline.autoCalculations._
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.sdk.pipeline.schema.{SpartaSchema, TypeOp}
import com.stratio.sparta.sdk.utils.AggregationTime
import com.stratio.sparta.serving.core.models.policy.cube.CubeModel
import com.stratio.sparta.serving.core.models.policy.trigger.TriggerModel
import com.stratio.sparta.serving.core.models.policy.{PolicyElementModel, TransformationsModel}
import org.apache.spark.sql.types._

import scala.util.Try



object SchemaHelper {

  val Default_Precision = 10
  val Default_Scale = 0
  val Nullable = true
  val NotNullable = false
  val DefaultTimeStampTypeString = "timestamp"
  val DefaultTimeStampType = TypeOp.Timestamp
  private val MetadataBuilder = new MetadataBuilder
  val MeasureMetadata = MetadataBuilder.putBoolean(Output.MeasureMetadataKey, true).build()
  val PkMetadata = MetadataBuilder.putBoolean(Output.PrimaryKeyMetadataKey, true).build()
  val PkTimeMetadata = MetadataBuilder.putBoolean(Output.PrimaryKeyMetadataKey, true)
    .putBoolean(Output.TimeDimensionKey, true).build()

  val mapTypes = Map(
    TypeOp.Long -> LongType,
    TypeOp.Double -> DoubleType,
    TypeOp.BigDecimal -> DecimalType(Default_Precision, Default_Scale),
    TypeOp.Int -> IntegerType,
    TypeOp.Boolean -> BooleanType,
    TypeOp.Date -> DateType,
    TypeOp.DateTime -> TimestampType,
    TypeOp.Timestamp -> TimestampType,
    TypeOp.ArrayDouble -> ArrayType(DoubleType),
    TypeOp.ArrayString -> ArrayType(StringType),
    TypeOp.String -> StringType,
    TypeOp.MapStringLong -> MapType(StringType, LongType),
    TypeOp.MapStringDouble -> MapType(StringType, DoubleType, false)
  )

  val mapSparkTypes: Map[DataType, TypeOp] = Map(
    LongType -> TypeOp.Long,
    DoubleType -> TypeOp.Double,
    DecimalType(Default_Precision, Default_Scale) -> TypeOp.BigDecimal,
    IntegerType -> TypeOp.Int,
    BooleanType -> TypeOp.Boolean,
    DateType -> TypeOp.Date,
    TimestampType -> TypeOp.Timestamp,
    ArrayType(DoubleType) -> TypeOp.ArrayDouble,
    ArrayType(StringType) -> TypeOp.ArrayString,
    StringType -> TypeOp.String,
    MapType(StringType, LongType) -> TypeOp.MapStringLong,
    MapType(StringType, DoubleType, false) -> TypeOp.MapStringDouble
  )

  val mapStringSparkTypes = Map(
    "long" -> LongType,
    "double" -> DoubleType,
    "int" -> IntegerType,
    "integer" -> IntegerType,
    "bool" -> BooleanType,
    "boolean" -> BooleanType,
    "date" -> DateType,
    "datetime" -> TimestampType,
    "timestamp" -> TimestampType,
    "string" -> StringType,
    "arraydouble" -> ArrayType(DoubleType),
    "arraystring" -> ArrayType(StringType),
    "text" -> StringType
  )

  def getSchemasFromParsers(transformationsModel: Seq[TransformationsModel],
                            initSchema: Map[String, StructType]): Map[String, StructType] = {
    initSchema ++ searchSchemasFromParsers(transformationsModel.sortBy(_.order), initSchema)
  }

  private def searchSchemasFromParsers(transformationsModel: Seq[TransformationsModel],
                                       schemas: Map[String, StructType]): Map[String, StructType] = {
    transformationsModel.headOption match {
      case Some(transformationModel) =>
        val schema = transformationModel.outputFieldsTransformed.map(outputField =>
          outputField.name -> StructField(outputField.name,
            mapStringSparkTypes.getOrElse(outputField.`type`.toLowerCase, StringType),
            Nullable
          )
        )

        val inputFields = schemas.values.flatMap(structType => structType.fields)
        val fieldsFiltered = {
          if (Try(transformationModel.configuration.getBoolean("removeInputField")).getOrElse(false) &&
            transformationModel.inputField.isDefined)
            inputFields.filter(stField => stField.name != transformationModel.inputField.get)
          else inputFields
        }

        val fields = fieldsFiltered ++ schema.map(_._2)
        val recursiveSchema = Map(transformationModel.order.toString -> StructType(fields.toSeq))

        if (transformationsModel.size == 1)
          schemas ++ recursiveSchema
        else schemas ++ searchSchemasFromParsers(transformationsModel.drop(1), recursiveSchema)
      case None =>
        schemas
    }
  }


  //scalastyle:off
  def getSchemasFromCubes(cubes: Seq[Cube],
                          cubeModels: Seq[CubeModel]): Seq[SpartaSchema] = {
    for {
      (cube, cubeModel) <- cubes.zip(cubeModels)
      measuresMerged = measuresFields(cube.operators, cubeModel.avoidNullValues).sortWith(_.name < _.name)
      timeDimension = getExpiringData(cubeModel).map(config => config.timeDimension)
      dimensions = filterDimensionsByTime(cube.dimensions.sorted, timeDimension)
      dimensionsF = dimensionsFields(dimensions, cubeModel.avoidNullValues)
      dateType = getTimeTypeFromString(cubeModel.writer.dateType.getOrElse(DefaultTimeStampTypeString))
      structFields = dimensionsF ++ timeDimensionFieldType(timeDimension, dateType, cubeModel.avoidNullValues) ++ measuresMerged
      schema = StructType(structFields)
      outputs = cubeModel.writer.outputs
      autoCalculatedFields = cubeModel.writer.autoCalculatedFields.map(model =>
        AutoCalculatedField(
          model.fromNotNullFields.map(fromNotNullFieldsModel =>
            FromNotNullFields(Field(fromNotNullFieldsModel.field.name, fromNotNullFieldsModel.field.outputType))),
          model.fromPkFields.map(fromPkFieldsModel =>
            FromPkFields(Field(fromPkFieldsModel.field.name, fromPkFieldsModel.field.outputType))),
          model.fromFields.map(fromFieldModel =>
            FromFields(Field(fromFieldModel.field.name, fromFieldModel.field.outputType), fromFieldModel.fromFields)),
          model.fromFixedValue.map(fromFixedValueModel =>
            FromFixedValue(Field(fromFixedValueModel.field.name, fromFixedValueModel.field.outputType),
              fromFixedValueModel.value))
        )
      )
    } yield SpartaSchema(outputs, cube.name, schema, timeDimension, dateType, autoCalculatedFields)
  }

  //scalastyle:on

  def getExpiringData(cubeModel: CubeModel): Option[ExpiringData] = {
    val timeDimension = cubeModel.dimensions
      .find(dimensionModel => dimensionModel.computeLast.isDefined)

    timeDimension match {
      case Some(dimensionModelValue) =>
        Option(ExpiringData(
          dimensionModelValue.name,
          dimensionModelValue.precision,
          dimensionModelValue.computeLast.get))
      case _ => None
    }
  }

  def getSchemasFromTriggers(triggers: Seq[TriggerModel], outputModels: Seq[PolicyElementModel]): Seq[SpartaSchema] = {
    for {
      trigger <- triggers
      structFields = trigger.primaryKey.map(field => Output.defaultStringField(field, NotNullable, PkMetadata))
      schema = StructType(structFields)
      autoCalculatedFields = trigger.writer.autoCalculatedFields.map(model =>
        AutoCalculatedField(
          model.fromNotNullFields.map(fromNotNullFieldsModel =>
            FromNotNullFields(Field(fromNotNullFieldsModel.field.name, fromNotNullFieldsModel.field.outputType))),
          model.fromPkFields.map(fromPkFieldsModel =>
            FromPkFields(Field(fromPkFieldsModel.field.name, fromPkFieldsModel.field.outputType))),
          model.fromFields.map(fromFieldModel =>
            FromFields(Field(fromFieldModel.field.name, fromFieldModel.field.outputType), fromFieldModel.fromFields)),
          model.fromFixedValue.map(fromFixedValueModel =>
            FromFixedValue(Field(fromFixedValueModel.field.name, fromFixedValueModel.field.outputType),
              fromFixedValueModel.value))
        )
      )
    } yield SpartaSchema(
      outputs = trigger.writer.outputs,
      tableName = trigger.name,
      schema = schema,
      timeDimension = None,
      dateType = TypeOp.Timestamp,
      autoCalculatedFields
    )
  }

  def getSchemasFromCubeTrigger(cubeModels: Seq[CubeModel],
                                outputModels: Seq[PolicyElementModel]): Seq[SpartaSchema] = {
    val tableSchemas = for {
      cube <- cubeModels
      tableSchemas = getSchemasFromTriggers(cube.triggers, outputModels)
    } yield tableSchemas
    tableSchemas.flatten
  }

  // XXX Private methods.

  private def filterDimensionsByTime(dimensions: Seq[Dimension], timeDimension: Option[String]): Seq[Dimension] =
    timeDimension match {
      case Some(timeName) => dimensions.filter(dimension => dimension.name != timeName)
      case None => dimensions
    }

  private def timeDimensionFieldType(timeDimension: Option[String],
                                     dateType: TypeOp.Value,
                                     avoidNullValues: Boolean): Seq[StructField] = {
    timeDimension match {
      case None =>
        Seq.empty[StructField]
      case Some(timeDimensionName) =>
        Seq(Output.getTimeFieldType(dateType, timeDimensionName, !avoidNullValues, Some(PkTimeMetadata)))
    }
  }

  def getTimeTypeFromString(timeType: String): TypeOp =
    timeType.toLowerCase match {
      case "timestamp" => TypeOp.Timestamp
      case "date" => TypeOp.Date
      case "datetime" => TypeOp.DateTime
      case "long" => TypeOp.Long
      case _ => TypeOp.String
    }

  private def measuresFields(operators: Seq[Operator], avoidNullValues: Boolean): Seq[StructField] =
    operators.map(operator =>
      StructField(operator.key, rowTypeFromOption(operator.returnType), !avoidNullValues, MeasureMetadata))

  private def dimensionsFields(fields: Seq[Dimension], avoidNullValues: Boolean): Seq[StructField] =
    fields.map(field =>
      StructField(field.name, rowTypeFromOption(field.precision.typeOp), !avoidNullValues, PkMetadata)
    )

  private def rowTypeFromOption(optionType: TypeOp): DataType = mapTypes.getOrElse(optionType, StringType)
}