/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.catalog

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.TransformStep
import com.stratio.sparta.plugin.helper.SchemaHelper.{createOrReplaceTemporalViewDf, getSchemaFromSessionOrModelOrRdd}
import com.stratio.sparta.plugin.helper.SparkStepHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.json4s.native.Serialization.read

import scala.util.{Failure, Success, Try}


abstract class CatalogTransformStep[Underlying[Row]](name: String,
                                                     outputOptions: OutputOptions,
                                                     transformationStepsManagement: TransformationStepManagement,
                                                     ssc: Option[StreamingContext],
                                                     xDSession: XDSession,
                                                     properties: Map[String, JSerializable])
                                                    (implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  val CatalogFromCsv = "CATALOG_FROM_CSV"
  val CatalogFromDictionary = "CATALOG_FROM_DICTIONARY"

  import com.stratio.sparta.plugin.models.SerializationImplicits._

  lazy val columnNameOption = properties.getString("columnName", None)
  lazy val catalogStrategy = properties.getString("catalogStrategy")
  lazy val catalogFromCsvOption = properties.getString("catalogFromCsv", None)
  lazy val catalogFromDictionary = properties.getString("catalogFromDictionary", None)

  lazy val maybeCachedMap: Try[Map[String, String]] =
    Try {
      catalogStrategy match {
        case CatalogFromCsv =>
          val csvPath = properties.getString("catalogFromCsv")
          xDSession.read.format("csv")
            .option("header", "false")
            .option("inferSchema", "true").load(csvPath)
            .rdd
            .map(r => (r(0).toString, r(1).toString))
            .collect.toMap

        case CatalogFromDictionary =>
          val dictionaryJson = s"${properties.getString("catalogFromDictionary", None).notBlank.fold("[]") { values => values.toString }}"
          read[Seq[CatalogDictionary]](dictionaryJson).map((entry => entry.dictionaryKey -> entry.dictionaryValue)).toMap

      }
    }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations =
    ErrorValidationsHelper.validate(
      Seq(
        (columnNameOption.isEmpty) -> "Column is mandatory",
        (catalogStrategy == CatalogFromCsv && catalogFromCsvOption.isEmpty) ->
          "Csv path is mandatory",
        (catalogStrategy == CatalogFromDictionary && catalogFromDictionary == "[]") ->
          "Almost one entry in the dictionary is mandatory",
        (catalogStrategy == CatalogFromCsv && catalogFromCsvOption.isDefined && maybeCachedMap.isFailure) ->
          s"Error loading the CSV file: ${if (maybeCachedMap.isFailure) maybeCachedMap.failed.get.getLocalizedMessage}"
      ), name)

    def applyCatalog(rdd: RDD[Row], inputStep: String): (RDD[Row], Option[StructType], Option[StructType]) =
      Try {
        val columnName = columnNameOption.getOrElse(throw new RuntimeException("Not defined column name"))
        maybeCachedMap match {
          case Success(cachedMap) =>
            val inputSchema = getSchemaFromSessionOrModelOrRdd(xDSession, inputStep, inputsModel, rdd)
            createOrReplaceTemporalViewDf(xDSession, rdd, inputStep, inputSchema) match {
              case Some(df) =>
                val newDataFrame = df.na.replace(columnName, cachedMap)
                (newDataFrame.rdd, Option(newDataFrame.schema), inputSchema)
              case None =>
                (rdd.filter(_ => false), None, inputSchema)
            }
          case Failure(ex) =>
            throw ex
        }
      } match {
        case Success(sqlResult) => sqlResult
        case Failure(e) => (SparkStepHelper.failRDDWithException(rdd, e), None, None)
      }
  }

  case class CatalogDictionary(dictionaryKey: String, dictionaryValue: String)