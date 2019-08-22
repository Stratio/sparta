/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.caseWhen

import java.io.{Serializable => JSerializable}

import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.junit.JUnitRunner
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}



@RunWith(classOf[JUnitRunner])
class CaseTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {
/*
  "calculateEmptyValues" should "return false in all fields if the Seq is complete" in {

      val sequenceTest: Seq[CaseModel] = Seq(CaseModel(Option("expression"), CaseValueType.VALUE, Some("newValue"), Some("String")))
      val result: Seq[(HasError, String)] = ToDelete.calculateEmptyValues(sequenceTest)

      result should contain allOf ((false, "0 Type is empty"),(false, "0 Value is empty"),(false, "0 Case expression is empty"))
    }


    "calculateEmptyValues" should "return true in every field that is empty" in {
      val sequenceTest: Seq[CaseModel] = Seq(CaseModel(None, CaseValueType.VALUE, Option("newValue"), Option("String"), Option("")))
      val result: Seq[(HasError, String)] = ToDelete.calculateEmptyValues(sequenceTest)

      result should be(List((false, "0 Type is empty"), (false, "0 Value is empty"), (true, "0 Case expression is empty")))
    }

*/
    "A CaseTransformStepBatch" should "Replace all values that match with the expression in one column" in {

      val caseExpressionsList =
        """[{"caseExpression": "Column1 == '1'",
          |"valueType": "VALUE",
          |"value": "5"
          |}
          |]""".stripMargin

      val inputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))
      val outputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))

      val dataIn: Seq[Row] =
        Seq(
          new GenericRowWithSchema(Array("1", "leonidas"), inputSchema),
          new GenericRowWithSchema(Array("1", "sparta"), inputSchema)
        ).map(_.asInstanceOf[Row])


      val dataOut = Seq(
        new GenericRowWithSchema(Array("5", "leonidas"), outputSchema),
        new GenericRowWithSchema(Array("5", "sparta"), outputSchema)
      )

      val dataSet: RDD[Row] = sc.parallelize(dataIn)
      val inputData = Map("step1" -> dataSet)
      val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

      val result = new CaseTransformStepBatch(
        "dummy",
        outputOptions,
        TransformationStepManagement(),
        None,
        sparkSession,
        Map("inputColumns" -> caseExpressionsList.asInstanceOf[JSerializable],
          "outputDataType" -> "string",
          "otherwiseExpression" -> "",
          "outputStrategy" -> "REPLACECOLUMN",
          "columnToReplace" -> "Column1")
      ).transformWithDiscards(inputData)._1

      val arrayValues = result.ds.collect()

      arrayValues.foreach { row =>
        assert(dataOut.contains(row))
        assert(outputSchema == row.schema)
      }

      assert(arrayValues.length === 2)

    }

  "A CaseTransformStepBatch" should "Replace all values that match an expression and replace " +
    "the others with otherwise expression on one existing column" in {

    val caseExpressionsList =
      """[{"caseExpression": "Column1 == '1'",
        |"valueType": "VALUE",
        |"value": "5"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType , true), StructField("Column2", StringType, true)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType, false), StructField("Column2", StringType, true)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("1", "leonidas"), inputSchema),
        new GenericRowWithSchema(Array("2", "sparta"), inputSchema)
      ).map(_.asInstanceOf[Row])


    val dataOut = Seq(
      new GenericRowWithSchema(Array("5", "leonidas"), outputSchema),
      new GenericRowWithSchema(Array("10", "sparta"), outputSchema)
    )

    val dataSet: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new CaseTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("inputColumns" -> caseExpressionsList.asInstanceOf[JSerializable],
        "outputDataType" -> "string",
        "otherwiseExpression" -> "10",
        "outputStrategy" -> "REPLACECOLUMN",
        "columnToReplace" -> "Column1")
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)
  }

  "A CaseTransformStepBatch" should "Replace all values that match different expressions and replace " +
    "the others with otherwise expression on one existing column" in {

    val caseExpressionsList =
      """[{"caseExpression": "Column1 == '1'",
        |"valueType": "VALUE",
        |"value": "10"
        |},
        |{"caseExpression": "Column1 == '2'",
        |"valueType": "VALUE",
        |"value": "20"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType, true), StructField("Column2", StringType, true)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType, false), StructField("Column2", StringType, true)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("1", "leonidas"), inputSchema),
        new GenericRowWithSchema(Array("2", "sparta"), inputSchema),
        new GenericRowWithSchema(Array("3", "sparta"), inputSchema)
      ).map(_.asInstanceOf[Row])


    val dataOut = Seq(
      new GenericRowWithSchema(Array("10", "leonidas"), outputSchema),
      new GenericRowWithSchema(Array("20", "sparta"), outputSchema),
      new GenericRowWithSchema(Array("0", "sparta"), outputSchema)
    )

    val dataSet: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new CaseTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("inputColumns" -> caseExpressionsList.asInstanceOf[JSerializable],
        "outputDataType" -> "string",
        "otherwiseExpression" -> "0",
        "outputStrategy" -> "REPLACECOLUMN",
        "columnToReplace" -> "Column1")
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 3)
  }

  "A CaseTransformStepBatch" should "Replace all values that match an expression and replace the others " +
    "with otherwise expression value in a new column" in {

    val caseExpressionsList =
      """[{"caseExpression": "Column1 == '1'",
        |"valueType": "VALUE",
        |"value": "5"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType),
      StructField("Column3", StringType, false)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("1", "leonidas"), inputSchema),
        new GenericRowWithSchema(Array("2", "sparta"), inputSchema)
      ).map(_.asInstanceOf[Row])


    val dataOut = Seq(
      new GenericRowWithSchema(Array("1", "leonidas", "5"), outputSchema),
      new GenericRowWithSchema(Array("2", "sparta", "10"), outputSchema)
    )

    val dataSet: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new CaseTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("inputColumns" -> caseExpressionsList.asInstanceOf[JSerializable],
        "outputDataType" -> "string",
        "otherwiseExpression" -> "10",
        "outputStrategy" -> "NEWCOLUMN",
        "aliasNewColumn" -> "Column3")
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)

  }

  "A CaseTransformStepBatch" should "Copy the values that match with the expression in a column and replace it in a new column" in {

    val caseExpressionsList =
      """[{"caseExpression": "Column1 == '1'",
        |"valueType": "VALUE",
        |"value": "10"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType), StructField("NewCol", StringType)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("1", "leonidas"), inputSchema),
        new GenericRowWithSchema(Array("1", "sparta"), inputSchema)
      ).map(_.asInstanceOf[Row])


    val dataOut = Seq(
      new GenericRowWithSchema(Array("1", "leonidas", "10"), outputSchema),
      new GenericRowWithSchema(Array("1", "sparta", "10"), outputSchema)
    )

    val dataSet: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new CaseTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("inputColumns" -> caseExpressionsList.asInstanceOf[JSerializable],
        "outputDataType" -> "string",
        "otherwiseExpression" -> "",
        "outputStrategy" -> "NEWCOLUMN",
        "aliasNewColumn" -> "NewCol")
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)

  }

  "A CaseTransformStepBatch" should "Replace values in one column using multiple expressions" in {

    val caseExpressionsList =
      """[{"caseExpression": "Column1 == '1'",
        |"valueType": "VALUE",
        |"value": "5"
        |},
        |{"caseExpression": "Column1 == 'leonidas'",
        |"valueType": "VALUE",
        |"value": "zeus"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("1", "leonidas"), inputSchema),
        new GenericRowWithSchema(Array("leonidas", "sparta"), inputSchema)
      ).map(_.asInstanceOf[Row])


    val dataOut = Seq(
      new GenericRowWithSchema(Array("5", "leonidas"), outputSchema),
      new GenericRowWithSchema(Array("zeus", "sparta"), outputSchema)
    )

    val dataSet: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new CaseTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("inputColumns" -> caseExpressionsList.asInstanceOf[JSerializable],
        "outputDataType" -> "string",
        "otherwiseExpression" -> "",
        "outputStrategy" -> "REPLACECOLUMN",
        "columnToReplace" -> "Column1")
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)
  }

  "A CaseTransformStepBatch" should "Create a new column replacing the values of other column using multiple expressions" in {

    val caseExpressionsList =
      """[{"caseExpression": "Column1 == '1'",
        |"valueType": "VALUE",
        |"value": "5"
        |},
        |{"caseExpression": "Column1 == 'leonidas'",
        |"valueType": "VALUE",
        |"value": "zeus"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType), StructField("Column3", StringType)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("1", "leonidas"), inputSchema),
        new GenericRowWithSchema(Array("leonidas", "sparta"), inputSchema)
      ).map(_.asInstanceOf[Row])


    val dataOut = Seq(
      new GenericRowWithSchema(Array("1", "leonidas", "5"), outputSchema),
      new GenericRowWithSchema(Array("leonidas", "sparta", "zeus"), outputSchema)
    )

    val dataSet: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new CaseTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("inputColumns" -> caseExpressionsList.asInstanceOf[JSerializable],
        "outputDataType" -> "string",
        "otherwiseExpression" -> "",
        "outputStrategy" -> "NEWCOLUMN",
        "aliasNewColumn" -> "Column3")
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)

  }

  "A CaseTransformStepBatch" should "Place null on column values when a condition doest not match the " +
    "expression an otherwise expression is empty" in {

    val caseExpressionsList =
      """[{"caseExpression": "Column1 != '1'",
        |"valueType": "VALUE",
        |"value": "5"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("1", "leonidas"), inputSchema),
        new GenericRowWithSchema(Array("1", "sparta"), inputSchema)
      ).map(_.asInstanceOf[Row])


    val dataOut = Seq(
      new GenericRowWithSchema(Array(null, "leonidas"), outputSchema),
      new GenericRowWithSchema(Array(null, "sparta"), outputSchema)
    )

    val dataSet: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new CaseTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("inputColumns" -> caseExpressionsList.asInstanceOf[JSerializable],
        "outputDataType" -> "string",
        "otherwiseExpression" -> "",
        "outputStrategy" -> "REPLACECOLUMN",
        "columnToReplace" -> "Column1")
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)

  }

  }