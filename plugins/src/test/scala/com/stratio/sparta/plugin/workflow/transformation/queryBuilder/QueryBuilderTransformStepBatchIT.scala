/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.queryBuilder

import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class QueryBuilderTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A QueryBuilderTransformStepBatch" should "make sql over one RDD" in {
    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 1.2), schema1).asInstanceOf[Row]
    )
    val inputRdd = sc.parallelize(data1)
    val inputData = Map("step1" -> inputRdd)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val visualClause =
      """{
        |   "selectClauses": [
        |     {"expression": "color", "alias": "color2"},
        |     {"expression": "price", "alias": "price2"}
        |   ],
        |   "fromClause": {"tableName": "step1", "alias": "st1"},
        |   "whereClause": "price > 12",
        |   "orderByClauses": [
        |     {"field": "price", "order": "ASC"}
        |   ]
        |}
        | """.stripMargin
    val result = new QueryBuilderTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("visualQuery" -> visualClause)
    ).transformWithDiscards(inputData)

    //Test filtered events
    val triggerData = result._1
    val streamingEvents = triggerData.ds.count()
    val streamingRegisters = triggerData.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(data1.contains(row)))

    assert(streamingEvents === 2)
  }

  "A QueryBuilderTransformStepBatch" should "make sql over one RDD with discards" in {
    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 1.2), schema1).asInstanceOf[Row]
    )
    val inputRdd = sc.parallelize(data1)
    val inputData = Map("step1" -> inputRdd)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val discardConditions =
      """[
        |{
        |   "previousField":"color",
        |   "transformedField":"color2"
        |},
        |{
        |   "previousField":"price",
        |   "transformedField":"price2"
        |}
        |]
        | """.stripMargin
    val visualClause =
      """{
        |   "selectClauses": [
        |     {"expression": "color", "alias": "color2"},
        |     {"expression": "price", "alias": "price2"}
        |   ],
        |   "fromClause": {"tableName": "step1", "alias": "st1"},
        |   "whereClause": "price > 12",
        |   "orderByClauses": [
        |     {"field": "price", "order": "ASC"}
        |   ]
        |}
        | """.stripMargin
    val result = new QueryBuilderTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("visualQuery" -> visualClause, "discardConditions" -> discardConditions)
    ).transformWithDiscards(inputData)

    //Test filtered events
    val triggerData = result._1
    val streamingEvents = triggerData.ds.count()
    val streamingRegisters = triggerData.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(data1.contains(row)))

    assert(streamingEvents === 2)

    //Test discarded events
    val discardedData = result._3.get
    val discardedEvents = discardedData.ds.count()
    val discardedRegisters = discardedData.ds.collect()

    if (discardedRegisters.nonEmpty)
      discardedRegisters.foreach(row => assert(data1.contains(row)))

    assert(discardedEvents === 1)
  }

  "A QueryBuilderTransformStepBatch" should "make join over two RDD" in {
    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val schema2 = StructType(Seq(StructField("color", StringType),
      StructField("company", StringType), StructField("name", StringType)))
    val schemaResult = StructType(Seq(
      StructField("color2", StringType),
      StructField("company2", StringType),
      StructField("name2", StringType),
      StructField("price2", DoubleType)
    ))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 1.2), schema1).asInstanceOf[Row]
    )
    val data2 = Seq(
      new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee"), schema2).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee"), schema2).asInstanceOf[Row]
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputRdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> inputRdd1, "step2" -> inputRdd2)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val visualClause =
      """{
        |   "selectClauses": [
        |     {"expression": "st1.color", "alias": "color"},
        |     {"expression": "st2.company", "alias": "company"},
        |     {"expression": "st2.name", "alias": "name"},
        |     {"expression": "st1.price", "alias": "price"}
        |   ],
        |   "joinClause": {
        |                   "leftTable": {"tableName": "step1", "alias": "st1"},
        |                   "rightTable": {"tableName": "step2", "alias": "st2"},
        |                   "joinTypes": "LEFT",
        |                   "joinConditions": [
        |                       {"leftField": "color", "rightField": "color"}
        |                   ]
        |                 },
        |   "whereClause": "st1.price > 12",
        |   "orderByClauses": [
        |     {"field": "st2.name", "order": "ASC", "position":3},
        |     {"field": "st1.price", "order": "ASC", "position":2}
        |   ]
        |}
        | """.stripMargin
    val queryBuilder = new QueryBuilderTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("visualQuery" -> visualClause)
    )
    val transformedData = queryBuilder.transformWithDiscards(inputData)
    val result = transformedData._1
    val sql = queryBuilder.sql

    log.info(s"Executing SQL: $sql")

    val queryData = Seq(
      new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee", 12.1), schemaResult),
      new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee", 12.2), schemaResult))
    val batchEvents = result.ds.count()
    val batchRegisters = result.ds.collect()

    batchRegisters.foreach(row =>
      queryData.contains(row) should be(true)
    )

    batchEvents should be(2)
  }

}