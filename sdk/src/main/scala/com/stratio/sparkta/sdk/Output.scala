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
import java.sql.Timestamp

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.{Logging, SparkContext}
import org.joda.time.DateTime

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.WriteOp.WriteOp

abstract class Output(keyName: String,
                      properties: Map[String, JSerializable],
                      @transient sparkContext: SparkContext,
                      operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
                      bcSchema: Option[Broadcast[Seq[TableSchema]]],
                      timeName: String)
  extends Parameterizable(properties) with Multiplexer with Logging {

  if (operationTypes.isEmpty) {
    log.info("Operation types is empty, you don't have aggregations defined in your policy.")
  }

  val sqlContext: SQLContext = new SQLContext(sparkContext)

  def getName: String = keyName

  def dateType: TypeOp.Value = TypeOp.Timestamp

  def fixedPrecisionsType: TypeOp.Value = TypeOp.String

  def supportedWriteOps: Seq[WriteOp]

  def multiplexer: Boolean = false

  def fixedPrecisions: Array[String] = Array()

  def fixedAggregation: Map[String, Option[Any]] = Map()

  def fieldsSeparator: String = ","

  def isAutoCalculateId: Boolean = false

  def persist(streams: Seq[DStream[(DimensionValuesTime, Map[String, Option[Any]])]]): Unit = {
    streams.foreach(stream => doPersist(stream))
  }

  def doPersist(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    if (bcSchema.isDefined)
      persistDataFrame(stream)
    else persistMetricOperation(stream)
  }

  protected def persistMetricOperation(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit =
    getStreamsFromOptions(stream, multiplexer, getFixedPrecisionsAndTimePrecision)
      .foreachRDD(rdd => rdd.foreachPartition(ops => upsert(ops)))

  protected def persistDataFrame(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    getStreamsFromOptions(stream, multiplexer, getFixedPrecisionsAndTimePrecision)
      .map { case (dimensionValueTime, aggregations) =>
      AggregateOperations.toKeyRow(filterDimensionValueTimeByFixedPrecisions(dimensionValueTime),
        aggregations,
        fixedAggregation,
        getFixedPrecisions(dimensionValueTime),
        isAutoCalculateId,
        timeName)
    }
      .foreachRDD(rdd => {
      bcSchema.get.value.filter(tschema => (tschema.outputName == keyName)).foreach(tschemaFiltered => {
        val tableSchemaTime = getTableSchemaFixedId(tschemaFiltered)
        upsert(sqlContext.createDataFrame(
          extractRow(rdd.filter { case (schema, row) => schema.exists(_ == tableSchemaTime.tableName) }),
          tableSchemaTime.schema),
          tableSchemaTime.tableName)
      })
    })
  }

  def upsert(dataFrame: DataFrame, tableName: String): Unit = {}

  def upsert(metricOperations: Iterator[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {}

  def getFixedPrecisionsAndTimePrecision: Array[String] = fixedPrecisions ++ Array(timeName)

  protected def getFixedPrecisions(dimensionValuesTime: DimensionValuesTime): Option[Seq[(String, Any)]] =
    if (fixedPrecisions.isEmpty) None
    else {
      Some(fixedPrecisions.flatMap(precision => {
        dimensionValuesTime.dimensionValues.find(dimension => dimension.getNameDimension == precision)
          .map(dimensionValue => (precision, dimensionValue.value))
      }))
    }

  //TODO refactor for remove var types
  protected def getTableSchemaFixedId(tbSchema: TableSchema): TableSchema = {
    var tableName = tbSchema.tableName.split(Output.Separator)
      .filter(name => !fixedPrecisions.contains(name) && name != timeName)
    var fieldsPk = getFields(tbSchema, false)
    var modifiedSchema = false

    if (!fixedPrecisions.isEmpty) {
      fixedPrecisions.foreach(precision => {
        tableName = tableName ++ Array(precision)
        fieldsPk = fieldsPk ++ Seq(Output.getFieldType(fixedPrecisionsType, precision, false))
        modifiedSchema = true
      })
    }

    if (isAutoCalculateId && !tbSchema.schema.fieldNames.contains(Output.Id)) {
      tableName = Array(Output.Id) ++ tableName
      fieldsPk = Seq(Output.defaultStringField(Output.Id, false)) ++ fieldsPk
      modifiedSchema = true
    }

    if (!timeName.isEmpty) {
      tableName = tableName ++ Array(timeName)
      fieldsPk = fieldsPk ++ Seq(Output.getFieldType(dateType, timeName, false))
      modifiedSchema = true
    }

    if (modifiedSchema){
      fieldsPk = fieldsPk ++ getFields(tbSchema, true)
      new TableSchema(tbSchema.outputName, tableName.mkString(Output.Separator), StructType(fieldsPk))
    } else tbSchema
  }

  protected def getFields(tbSchema: TableSchema, nullables: Boolean) : Seq[StructField] =
    tbSchema.schema.fields.toSeq.filter(field =>
    !fixedPrecisions.contains(field.name) && field.name != timeName && field.nullable == nullables)

  protected def genericRowSchema(rdd: RDD[(Option[String], Row)]): (Option[String], RDD[Row]) =
    (Some(rdd.map(rowType => rowType._1.get.split(Output.Separator))
      .reduce((names1, names2) => if (names1.length > names2.length) names1 else names2).mkString(Output.Separator)),
      extractRow(rdd))

  protected def extractRow(rdd: RDD[(Option[String], Row)]): RDD[Row] = rdd.map(rowType => rowType._2)

  protected def filterDimensionValueTimeByFixedPrecisions(dimensionValuesTime: DimensionValuesTime)
  : DimensionValuesTime =
    if (fixedPrecisions.isEmpty) dimensionValuesTime
    else {
      DimensionValuesTime(dimensionValuesTime.dimensionValues.filter(dimensionValue =>
        !fixedPrecisions.contains(dimensionValue.dimensionPrecision.getNameDimension)), dimensionValuesTime.time)
    }

  protected def filterSchemaByKeyAndField: Seq[TableSchema] =
    if (bcSchema.isDefined) {
      bcSchema.get.value.filter(schemaFilter => schemaFilter.outputName == keyName &&
        getFixedPrecisionsAndTimePrecision.forall(schemaFilter.schema.fieldNames.contains(_) &&
          schemaFilter.schema.filter(!_.nullable).length >= 1))
    } else Seq()

  protected def checkOperationTypes: Boolean = {
    if (operationTypes.isDefined) {
      operationTypes.get.value.values.map(_._1).toSet.diff(supportedWriteOps.toSet).toSeq match {
        case s if s.size == 0 => true
        case badWriteOps => {
          log.info(s"The following write operators are not supported by this output: ${badWriteOps.mkString(", ")}")
          false
        }
      }
    } else false
  }
}

object Output {

  final val Separator = "_"
  final val Id = "id"
  final val FixedAggregation = "fixedAggregation"
  final val FixedAggregationSeparator = ":"
  final val Multiplexer = "multiplexer"

  def getFieldType(dateTimeType: TypeOp, fieldName: String, nullable: Boolean): StructField =
    dateTimeType match {
      case TypeOp.Date | TypeOp.DateTime => defaultDateField(fieldName, nullable)
      case TypeOp.Timestamp => defaultTimeStampField(fieldName, nullable)
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
}
