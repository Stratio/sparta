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

  def dateType: TypeOp.Value = TypeOp.Timestamp

  def fixedBucketsType: TypeOp.Value = TypeOp.String

  def supportedWriteOps: Seq[WriteOp]

  def multiplexer: Boolean = false

  def fixedBuckets: Array[String] = Array()

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
    getStreamsFromOptions(stream, multiplexer, getFixedBucketsAndTimeBucket)
      .foreachRDD(rdd => rdd.foreachPartition(ops => upsert(ops)))

  protected def persistDataFrame(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    getStreamsFromOptions(stream, multiplexer, getFixedBucketsAndTimeBucket)
      .map { case (dimensionValueTime, aggregations) =>
      AggregateOperations.toKeyRow(filterDimensionValueTimeByFixedBuckets(dimensionValueTime),
        aggregations,
        getFixedBuckets(dimensionValueTime),
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

  def getFixedBucketsAndTimeBucket: Array[String] = fixedBuckets ++ Array(timeName)

  protected def getFixedBuckets(dimensionValuesTime: DimensionValuesTime): Option[Seq[(String, Any)]] =
    if (fixedBuckets.isEmpty) None
    else {
      Some(fixedBuckets.flatMap(bucket => {
        dimensionValuesTime.dimensionValues.find(dimension => dimension.getNameDimension == bucket)
          .map(dimensionValue => (bucket, dimensionValue.value))
      }))
    }

  protected def getTableSchemaFixedId(tbSchema: TableSchema): TableSchema = {
    var tableName = tbSchema.tableName.split(Output.SEPARATOR)
      .filter(name => !fixedBuckets.contains(name) && name != timeName).mkString(Output.SEPARATOR)
    var fields = tbSchema.schema.fields.toSeq.filter(field =>
      !fixedBuckets.contains(field.name) && field.name != timeName)
    var modifiedSchema = false

    if (!fixedBuckets.isEmpty) {
      fixedBuckets.foreach(bucket => {
        tableName += Output.SEPARATOR + bucket
        fields = fields ++ Seq(Output.getFieldType(fixedBucketsType, bucket, false))
        modifiedSchema = true
      })
    }

    if (!timeName.isEmpty) {
      tableName += Output.SEPARATOR + timeName
      fields = fields ++ Seq(Output.getFieldType(dateType, timeName, false))
    }

    if (isAutoCalculateId && !tbSchema.schema.fieldNames.contains(Output.ID)) {
      tableName += Output.SEPARATOR + Output.ID
      fields = fields ++ Seq(Output.defaultStringField(Output.ID, false))
      modifiedSchema = true
    }

    if (modifiedSchema) new TableSchema(tbSchema.outputName, tableName, StructType(fields)) else tbSchema
  }

  protected def genericRowSchema(rdd: RDD[(Option[String], Row)]): (Option[String], RDD[Row]) =
    (Some(rdd.map(rowType => rowType._1.get.split(Output.SEPARATOR))
      .reduce((names1, names2) => if (names1.length > names2.length) names1 else names2).mkString(Output.SEPARATOR)),
      extractRow(rdd))

  protected def extractRow(rdd: RDD[(Option[String], Row)]): RDD[Row] = rdd.map(rowType => rowType._2)

  protected def filterDimensionValueTimeByFixedBuckets(dimensionValuesTime: DimensionValuesTime): DimensionValuesTime =
    if (fixedBuckets.isEmpty) dimensionValuesTime
    else {
      DimensionValuesTime(dimensionValuesTime.dimensionValues.filter(dimensionValue =>
        !fixedBuckets.contains(dimensionValue.dimensionBucket.getNameDimension)), dimensionValuesTime.time)
    }

  protected def filterSchemaByKeyAndField: Seq[TableSchema] =
    if (bcSchema.isDefined) {
      bcSchema.get.value.filter(schemaFilter => schemaFilter.outputName == keyName &&
        getFixedBucketsAndTimeBucket.forall(schemaFilter.schema.fieldNames.contains(_) &&
          schemaFilter.schema.filter(!_.nullable).length > 1))
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

  final val SEPARATOR = "_"
  final val ID = "id"

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
