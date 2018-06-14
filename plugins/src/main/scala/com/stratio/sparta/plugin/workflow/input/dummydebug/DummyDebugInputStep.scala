/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.dummydebug

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.workflow.input.avro.AvroInputStepBatch
import com.stratio.sparta.plugin.workflow.input.crossdata.CrossdataInputStepBatch
import com.stratio.sparta.plugin.workflow.input.csv.CsvInputStepBatch
import com.stratio.sparta.plugin.workflow.input.filesystem.FileSystemInputStepBatch
import com.stratio.sparta.plugin.workflow.input.json.JsonInputStepBatch
import com.stratio.sparta.plugin.workflow.input.parquet.ParquetInputStepBatch
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.models.OutputOptions
import com.stratio.sparta.sdk.workflow.step._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import com.stratio.sparta.sdk.enumerators.InputFormatEnum._
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._

abstract class DummyDebugInputStep[Underlying[Row]](
                                               name: String,
                                               outputOptions: OutputOptions,
                                               ssc: Option[StreamingContext],
                                               xDSession: XDSession,
                                               properties: Map[String, JSerializable]
                                             )
  extends InputStep[Underlying](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  def createDebugFromPath(parsedPath: Option[String]): (DistributedMonad[RDD], Option[StructType]) = {
    getSerializerForInput(parsedPath) match {
      case Some(AVRO) => new AvroInputStepBatch(name, outputOptions, ssc, xDSession, properties) {
        override lazy val path: Option[String] = parsedPath
        override lazy val schemaProvided: Option[String] = None
      }.initWithSchema()
      case Some(JSON) => new JsonInputStepBatch(name, outputOptions, ssc, xDSession, properties) {
        override lazy val path: Option[String] = parsedPath
      }.initWithSchema()
      case Some(CSV) => new CsvInputStepBatch(name, outputOptions, ssc, xDSession, properties) {
        override lazy val path: Option[String] = parsedPath
      }.initWithSchema()
      case Some(PARQUET) => new ParquetInputStepBatch(name, outputOptions, ssc, xDSession, properties) {
        override lazy val path: Option[String] = parsedPath
      }.initWithSchema()
      case _ =>
        log.info("It was not possible to infer which serializer should be used," +
          "therefore the input will be read as text file")
        new FileSystemInputStepBatch(name, outputOptions, ssc, xDSession, properties) {
          override lazy val path: Option[String] = parsedPath
        }.initWithSchema()
    }
  }

  def createDebugFromUserDefinedExample(): (DistributedMonad[RDD], Option[StructType]) = {
    lazy val outputField: String = properties.getString("outputField", DefaultRawDataField)
    val schema = StructType(StructField(outputField, StringType, nullable = true ) :: Nil)
    val arrayDebugUserProvidedExample = debugUserProvidedExample.get.split("\n")
    val sequenceOfRows = arrayDebugUserProvidedExample.map( value =>
      new GenericRowWithSchema(Array(value), schema).asInstanceOf[Row]
    )
    val debugRDD = xDSession.sparkContext
      .parallelize(sequenceOfRows)
    (debugRDD, Option(schema))
  }

  private def forceToLocalFS(path : String): String = {
    if (path.startsWith("file:///")) path
    else {
      s"file://${ if(path.head.equals('/')) "" else "/"}$path"
    }
  }

  def createDistributedMonadRDDwithSchema(): (DistributedMonad[RDD], Option[StructType]) = {
    require( debugPath.nonEmpty || debugUserProvidedExample.nonEmpty || debugQuery.nonEmpty ||
      debugFileUploaded.nonEmpty, errorDebugValidation)

    (debugPath, debugQuery, debugUserProvidedExample, debugFileUploaded) match {
      case (Some(path), _ , _,_) => createDebugFromPath(Some(path))
      case (None, Some(debQuery), _, _) =>
        new CrossdataInputStepBatch(name, outputOptions, ssc, xDSession, properties) {
          override lazy val query = debQuery
        }.initWithSchema()
      case (None, None, Some(userExample), _) => createDebugFromUserDefinedExample()
      case (None, None, None, Some(path)) => createDebugFromPath(Some(forceToLocalFS(path)))
      case (None, None, None, None) => throw new Exception("No simulated input for debugging")
    }
  }
}