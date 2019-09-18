/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.rest

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.plugin.common.rest.RestUtils.ReplaceableFields
import com.stratio.sparta.plugin.common.rest.SparkExecutorRestUtils.SparkExecutorRestUtils
import com.stratio.sparta.plugin.common.rest.{RestGraph, RestUtils, SparkExecutorRestUtils}
import com.stratio.sparta.plugin.enumerations.FieldsPreservationPolicy._
import com.stratio.sparta.plugin.helper.SchemaHelper
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

import scala.concurrent.Await
import scala.concurrent.duration.Duration



class RestTransformStepBatch(
                              name: String,
                              outputOptions: OutputOptions,
                              transformationStepsManagement: TransformationStepManagement,
                              ssc: Option[StreamingContext],
                              xDSession: XDSession,
                              properties: Map[String, JSerializable]
                            )
  extends RestTransformStep[RDD](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  val conf = xDSession.conf.getAll

  /**
    *
    * @param inputData Input steps data that the function receive. The key is the name of the step and the value is
    *                  the collection ([[DistributedMonad]])
    * @return (transformedData, transformed schema, discarded data, discarded schema)
    */
  override def transformWithDiscards(
                                      inputData: Map[String, DistributedMonad[RDD]]
                                    ): (DistributedMonad[RDD], Option[StructType], Option[DistributedMonad[RDD]], Option[StructType]) = {

    val (rddDiscarded, rdd) = applyHeadTransformWithDiscards(inputData) { (inputTableName, inputStream) =>

      implicit val inputRDD = inputStream.ds

      val replaceableFields = {
        val schema = SchemaHelper.getSchemaFromSessionOrRdd(xDSession, inputTableName, inputRDD)
        schema.map(RestUtils.preProcessingInputFields(urlUnwrapped, restConfig.bodyString, restConfig.bodyFormat, _))
          .getOrElse(ReplaceableFields.empty) // TODO test empty RDDs
      }

      val correctRDD = inputRDD.mapPartitions { rowsIterator =>
        implicit val restUtils: SparkExecutorRestUtils =
          SparkExecutorRestUtils.getOrCreate(restConfig.akkaHttpProperties, conf)

        import restUtils.Implicits._

        // We wait for the future containing our rows & results to complete ...
        val seqSolRow =
          Await.result(
            RestGraph(restConfig, restUtils).createTransformationGraph(
              rowsIterator, replaceableFields.uri, replaceableFields.body, Some(transformationStepsManagement)).run(), Duration.Inf).toIterator

        /** ... we create the output RDD with the rows according to the chosen preservation policy preserving
          * the old row because mapPartitions has as output only a RDD[T]
          * so we create a RDD[(Row, Row)] ~> RDD[(ProcessedRow, InputRow)]
          * */
        seqSolRow.map { case (response, oldRow) =>
          restConfig.preservationPolicy match {
            case JUST_EXTRACTED =>
              (new GenericRowWithSchema(Array(response), restFieldSchema):Row, oldRow)
            case _ =>
              (new GenericRowWithSchema(oldRow.toSeq.toArray ++ Array(response),
                StructType(oldRow.schema.fields ++ restFieldSchema)):Row, oldRow)
          }
        }
      }

      /** CorrectRDD has a 2-tuple (processedRow, inputRow) for every processed element
        * so the discarded values are InputRDD - CorrectRDD._2 whereas the correct ones
        * are CorrectRDD._1
        * */
      correctRDD.cache() // TODO cache 3 vs 1 (only with discard? it's being done always...)

      val transformedRDD = correctRDD.map{ case (processedRow, _) => processedRow}
      SchemaHelper.getSchemaFromSessionOrRdd(xDSession, name, transformedRDD)
        .foreach(transformedRDD.registerAsTable(xDSession, _, name))

      val discardedRDD = inputRDD.subtract(correctRDD.map{ case (_, inputRow) => inputRow})
      (discardedRDD, transformedRDD)
    }

    val finalSchema = SchemaHelper.getSchemaFromSessionOrRdd(xDSession, name, rdd.ds)
    val inputSchema = SchemaHelper.getSchemaFromSessionOrRdd(xDSession, inputData.head._1, inputData.head._2.ds)

    (rdd, finalSchema, Option(rddDiscarded), inputSchema)
  }
}
