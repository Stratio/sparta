/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.sdk

import java.io.{Serializable => JSerializable}
import scala.util._

import org.apache.spark.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.streaming.dstream.DStream

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap.map2ValidatingPropertyMap
import com.stratio.sparkta.sdk.WriteOp.WriteOp

abstract class Output(keyName: String,
                      version: Option[Int],
                      properties: Map[String, JSerializable],
                      operationTypes: Option[Map[String, (WriteOp, TypeOp)]],
                      bcSchema: Option[Seq[TableSchema]])
  extends Parameterizable(properties) with Logging {

  if (operationTypes.isEmpty) {
    log.info("Operation types is empty, you don't have aggregations defined in your policy.")
  }

  var sqlContext: SQLContext = _

  def getName: String = keyName

  def dateType: TypeOp.Value = TypeOp.Timestamp

  def fixedDimensionsType: TypeOp.Value = TypeOp.String

  val supportedWriteOps = Seq(WriteOp.FullText, WriteOp.Inc, WriteOp.IncBig, WriteOp.Set, WriteOp.Range,
    WriteOp.AccSet, WriteOp.Max, WriteOp.Min, WriteOp.Avg, WriteOp.AccAvg, WriteOp.Median,
    WriteOp.AccMedian, WriteOp.Variance, WriteOp.AccVariance, WriteOp.Stddev, WriteOp.AccStddev,
    WriteOp.WordCount, WriteOp.EntityCount, WriteOp.Mode)

  val fixedDimensions: Array[String] = properties.getString("fixedDimensions", None) match {
    case None => Array()
    case Some(fixDimensions) => fixDimensions.split(FieldsSeparator)
  }

  val fixedAgg = properties.getString("fixedAggregation", None)

  val fixedAggregation: Map[String, Option[Any]] =
    if (fixedAgg.isDefined) {
      val fixedAggSplited = fixedAgg.get.split(Output.FixedAggregationSeparator)
      Map(fixedAggSplited.head -> Some(fixedAggSplited.last))
    } else Map()

  final val FieldsSeparator = ","

  def isAutoCalculateId: Boolean = Try(properties.getString("isAutoCalculateId").toBoolean).getOrElse(false)

  def persist(streams: Seq[DStream[(DimensionValuesTime, Map[String, Option[Any]])]]): Unit = {
    sqlContext = new SQLContext(streams.head.context.sparkContext)
    setup
    streams.foreach(stream => doPersist(stream))
  }

  protected def setup: Unit = {}

  def doPersist(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    if (bcSchema.isDefined)
      persistDataFrame(stream)
    else persistMetricOperation(stream)
  }

  protected def persistMetricOperation(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit =
    stream.foreachRDD(rdd => {
      if (rdd.take(1).length > 0) {
        rdd.foreachPartition(
          ops => {
            Try(upsert(ops)) match {
              case Success(value) => value
              case Failure(exception) => {
                val error = s"Failure[Output]: ${ops.toString} | Message: ${exception.getLocalizedMessage}"
                log.error(error, exception)
              }
            }
          }
        )
      } else log.info("Empty event received")
    })

  protected def persistDataFrame(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    stream.map { case (dimensionValuesTime, aggregations) =>
      AggregateOperations.toKeyRow(
        filterDimensionValueTimeByFixedDimensions(dimensionValuesTime),
        aggregations,
        fixedAggregation,
        getFixedDimensions(dimensionValuesTime),
        isAutoCalculateId,
        dateType)
    }
      .foreachRDD(rdd => {
        if (rdd.take(1).length > 0) {
          bcSchema.get.filter(tschema => tschema.outputName == keyName).foreach(tschemaFiltered => {
            val tableSchemaTime = getTableSchemaFixedId(tschemaFiltered)
            val dataFrame = sqlContext.createDataFrame(
              extractRow(rdd.filter { case (schema, row) => schema.exists(_ == tableSchemaTime.tableName) }),
              tableSchemaTime.schema)
            Try(upsert(dataFrame, tableSchemaTime.tableName, tschemaFiltered.timeDimension)) match {
              case Success(_) => log.debug(s"Data stored in ${tableSchemaTime.tableName}")
              case Failure(_) => {
                log.error(s"Something goes wrong. Table: ${tableSchemaTime.tableName}")
                log.error(s"Schema. ${dataFrame.schema}")
                log.error(s"Head element. ${dataFrame.head}")
              }
            }
          })
        } else log.info("Empty event received")
      })
  }

  def upsert(dataFrame: DataFrame, tableName: String, timeDimension: String): Unit = {}

  def upsert(metricOperations: Iterator[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {}

  //TODO refactor for remove var types
  def getTableSchemaFixedId(tbSchema: TableSchema): TableSchema = {
    var fieldsPk = getFields(tbSchema, false)
    var modifiedSchema = false

    if (!fixedDimensions.isEmpty) {
      fixedDimensions.foreach(fxdimension => {
        if (!fieldsPk.map(stField => stField.name).contains(fxdimension)) {
          fieldsPk = fieldsPk ++ Seq(Output.getFieldType(fixedDimensionsType, fxdimension, false))
          modifiedSchema = true
        }
      })
    }

    if (isAutoCalculateId && !tbSchema.schema.fieldNames.contains(Output.Id)) {
      fieldsPk = Seq(Output.defaultStringField(Output.Id, false)) ++ fieldsPk
      modifiedSchema = true
    }

    fieldsPk = fieldsPk ++
      Seq(Output.getFieldType(dateType, tbSchema.timeDimension, false)) ++
      getFields(tbSchema, true)
    new TableSchema(tbSchema.outputName,
      tbSchema.tableName,
      StructType(fieldsPk),
      tbSchema.timeDimension)
  }

  def getFields(tbSchema: TableSchema, nullables: Boolean): Seq[StructField] =
    tbSchema.schema.fields.toSeq.filter(field =>
      !fixedDimensions.contains(field.name) && field.name != tbSchema.timeDimension && field.nullable == nullables)

  def extractRow(rdd: RDD[(Option[String], Row)]): RDD[Row] = rdd.map(rowType => rowType._2)

  def getFixedDimensions: Array[String] = fixedDimensions

  def getFixedDimensions(dimensionValuesTime: DimensionValuesTime): Option[Seq[(String, Any)]] =
    if (fixedDimensions.isEmpty) None
    else Some(fixedDimensions.flatMap(fxdimension => {
      dimensionValuesTime.dimensionValues.find(dimension => dimension.getNameDimension == fxdimension)
        .map(dimensionValue => (fxdimension, dimensionValue.value))
    }))

  def filterDimensionValueTimeByFixedDimensions(dimensionValuesTime: DimensionValuesTime)
  : DimensionValuesTime =
    if (fixedDimensions.isEmpty) dimensionValuesTime
    else dimensionValuesTime.copy(
      dimensionValues = dimensionValuesTime.dimensionValues
        .filter(dimensionValue => !fixedDimensions.contains(dimensionValue.getNameDimension))
    )

  def filterSchemaByFixedAndTimeDimensions(tbschemas: Seq[TableSchema]): Seq[TableSchema] =
    tbschemas.filter(schemaFilter => {
      val checkDimensions = getFixedDimensions ++ Array(schemaFilter.timeDimension)
      schemaFilter.outputName == keyName &&
        checkDimensions.forall({
          schemaFilter.schema.fieldNames.contains(_)
        })
    })

  def checkOperationTypes: Boolean =
    if (operationTypes.isDefined) {
      operationTypes.get.values.map(_._1).toSet.diff(supportedWriteOps.toSet).toSeq match {
        case s if s.isEmpty => true
        case badWriteOps => {
          log.info(s"The following write operators are not supported by this output: ${badWriteOps.mkString(", ")}")
          false
        }
      }
    } else false

  def versionedTableName(tableName: String): String = {
    val versionChain = version match {
      case Some(v) => s"${Output.Separator}v$v"
      case None => ""
    }
    s"$tableName$versionChain"
  }
}

object Output {

  final val ClassSuffix = "Output"
  final val Separator = "_"
  final val Id = "id"
  final val FixedAggregation = "fixedAggregation"
  final val FixedAggregationSeparator = ":"

  def getFieldType(dateTimeType: TypeOp, fieldName: String, nullable: Boolean): StructField =
    dateTimeType match {
      case TypeOp.Date | TypeOp.DateTime => defaultDateField(fieldName, nullable)
      case TypeOp.Timestamp => defaultTimeStampField(fieldName, nullable)
      case TypeOp.Long => defaultLongField(fieldName, nullable)
      case TypeOp.String => defaultStringField(fieldName, nullable)
      case _ => defaultStringField(fieldName, nullable)
    }

  def defaultTimeStampField(fieldName: String, nullable: Boolean): StructField =
    StructField(fieldName, TimestampType, nullable)

  def defaultDateField(fieldName: String, nullable: Boolean): StructField =
    StructField(fieldName, DateType, nullable)

  def defaultStringField(fieldName: String, nullable: Boolean): StructField =
    StructField(fieldName, StringType, nullable)

  def defaultGeoField(fieldName: String, nullable: Boolean): StructField =
    StructField(fieldName, ArrayType(DoubleType), nullable)

  def defaultLongField(fieldName: String, nullable: Boolean): StructField =
    StructField(fieldName, LongType, nullable)
}
