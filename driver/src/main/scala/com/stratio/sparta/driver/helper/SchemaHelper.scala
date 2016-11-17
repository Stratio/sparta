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
import com.stratio.sparta.sdk.TypeOp.TypeOp
import com.stratio.sparta.sdk._
import com.stratio.sparta.serving.core.helpers.DateOperationsHelper
import com.stratio.sparta.serving.core.models._
import org.apache.spark.sql.types._

object SchemaHelper {

  final val Default_Precision = 10
  final val Default_Scale = 0
  final val Nullable = true
  final val NotNullable = false
  final val DefaultTimeStampTypeString = "timestamp"
  final val DefaultTimeStampType = TypeOp.Timestamp
  private val MetadataBuilder = new MetadataBuilder
  final val MeasureMetadata = MetadataBuilder.putBoolean(Output.MeasureMetadataKey, true).build()
  final val PkMetadata = MetadataBuilder.putBoolean(Output.PrimaryKeyMetadataKey, true).build()

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
        val fields = schemas.values.flatMap(structType => structType.fields) ++ schema.map(_._2)
        val recursiveSchema = Map(transformationModel.order.toString -> StructType(fields.toSeq))

        if (transformationsModel.size == 1)
          schemas ++ recursiveSchema
        else schemas ++ searchSchemasFromParsers(transformationsModel.drop(1), recursiveSchema)
      case None =>
        schemas
    }
  }

  def getSchemaWithoutRaw(schemas: Map[String, StructType]): StructType =
    StructType(schemas.values.last.filter(_.name != Input.RawDataKey))

  def getSchemasFromCubes(cubes: Seq[Cube],
                          cubeModels: Seq[CubeModel]): Seq[TableSchema] = {
    for {
      (cube, cubeModel) <- cubes.zip(cubeModels)
      measuresMerged = measuresFields(cube.operators).sortWith(_.name < _.name)
      timeDimension = getExpiringData(cubeModel).map(config => config.timeDimension)
      dimensions = filterDimensionsByTime(cube.dimensions.sorted, timeDimension)
      dimensionsF = dimensionsFields(dimensions)
      dateType = getTimeTypeFromString(cubeModel.writer.dateType.getOrElse(DefaultTimeStampTypeString))
      structFields = dimensionsF ++ timeDimensionFieldType(timeDimension, dateType) ++ measuresMerged
      schema = StructType(structFields)
      outputs = cubeModel.writer.outputs
      autoCalculatedFields = cubeModel.writer.autoCalculatedFields.map(model =>
        AutoCalculatedField(
          model.fromNotNullFields.map(fieldModel => Field(fieldModel.name, fieldModel.outputType)),
          model.fromPkFields.map(fieldModel => Field(fieldModel.name, fieldModel.outputType)),
          model.fromFields.map(fromFieldModel =>
            FromField(Field(fromFieldModel.field.name, fromFieldModel.field.outputType), fromFieldModel.fromFields)),
          model.fromFixedValue.map(fromFixedValueModel =>
            FromFixedValue(Field(fromFixedValueModel.field.name, fromFixedValueModel.field.outputType),
              fromFixedValueModel.value))
        )
      )
    } yield TableSchema(outputs, cube.name, schema, timeDimension, dateType, autoCalculatedFields)
  }

  def getExpiringData(cubeModel: CubeModel): Option[ExpiringDataConfig] = {
    val timeDimension = cubeModel.dimensions
      .find(dimensionModel => dimensionModel.computeLast.isDefined)

    timeDimension match {
      case Some(dimensionModelValue) =>
        Option(ExpiringDataConfig(
          dimensionModelValue.name,
          dimensionModelValue.precision,
          DateOperationsHelper.parseValueToMilliSeconds(dimensionModelValue.computeLast.get)))
      case _ => None
    }
  }

  def getSchemasFromTriggers(triggers: Seq[TriggerModel], outputModels: Seq[PolicyElementModel]): Seq[TableSchema] = {
    for {
      trigger <- triggers
      structFields = trigger.primaryKey.map(field => Output.defaultStringField(field, false))
      schema = StructType(structFields)
      autoCalculatedFields = trigger.writer.autoCalculatedFields.map(model =>
        AutoCalculatedField(
          model.fromNotNullFields.map(fieldModel => Field(fieldModel.name, fieldModel.outputType)),
          model.fromPkFields.map(fieldModel => Field(fieldModel.name, fieldModel.outputType)),
          model.fromFields.map(fromFieldModel =>
            FromField(Field(fromFieldModel.field.name, fromFieldModel.field.outputType), fromFieldModel.fromFields)),
          model.fromFixedValue.map(fromFixedValueModel =>
            FromFixedValue(Field(fromFixedValueModel.field.name, fromFixedValueModel.field.outputType),
              fromFixedValueModel.value))
        )
      )
    } yield TableSchema(
      outputs = trigger.writer.outputs,
      tableName = trigger.name,
      schema = schema,
      timeDimension = None,
      dateType = TypeOp.Timestamp,
      autoCalculatedFields
    )
  }

  def getSchemasFromCubeTrigger(cubeModels: Seq[CubeModel], outputModels: Seq[PolicyElementModel]): Seq[TableSchema] = {
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

  private def timeDimensionFieldType(timeDimension: Option[String], dateType: TypeOp.Value): Seq[StructField] = {
    timeDimension match {
      case None => Seq.empty[StructField]
      case Some(timeDimensionName) => Seq(Output.getTimeFieldType(dateType, timeDimensionName, NotNullable))
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

  private def measuresFields(operators: Seq[Operator]): Seq[StructField] =
    operators.map(operator =>
      StructField(operator.key, rowTypeFromOption(operator.returnType), NotNullable, MeasureMetadata))

  private def dimensionsFields(fields: Seq[Dimension]): Seq[StructField] =
    fields.map(field => StructField(field.name, rowTypeFromOption(field.precision.typeOp), NotNullable, PkMetadata))

  private def rowTypeFromOption(optionType: TypeOp): DataType = mapTypes.getOrElse(optionType, StringType)

}
