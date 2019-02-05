/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.pivot

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.TransformStep
import com.stratio.sparta.plugin.enumerations.SelectType
import com.stratio.sparta.plugin.enumerations.SelectType.SelectType
import com.stratio.sparta.plugin.helper.SchemaHelper.{createOrReplaceTemporalViewDf, getSchemaFromSessionOrModelOrRdd, parserInputSchema}
import com.stratio.sparta.plugin.helper.SparkStepHelper
import com.stratio.sparta.plugin.models.PropertyColumn
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{Column, Row}
import org.apache.spark.streaming.StreamingContext
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

abstract class PivotTransformStep[Underlying[Row]](
                                                    name: String,
                                                    outputOptions: OutputOptions,
                                                    transformationStepsManagement: TransformationStepManagement,
                                                    ssc: Option[StreamingContext],
                                                    xDSession: XDSession,
                                                    properties: Map[String, JSerializable]
                                                  )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  lazy val selectType: SelectType = Try {
    SelectType.withName(properties.getString("selectType", "EXPRESSION").toUpperCase())
  }.getOrElse(SelectType.EXPRESSION)

  lazy val selectExpression: Option[String] = properties.getString("selectExp", None).notBlank.map{ expression =>
    if(expression.toUpperCase.startsWith("SELECT"))
      expression.substring("SELECT".length)
    else expression
  }
  lazy val selectAllColumns: Boolean = Try(properties.getBoolean("selectAll")).getOrElse(false)
  lazy val selectColumns: Seq[String] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val cols: String = s"${properties.getString("columns", None).notBlank.fold("[]") { values => values.toString }}"
    val colsSelected = if (selectAllColumns) """ [{"name":"*"}] """ else cols

    read[Seq[PropertyColumn]](colsSelected).map(_.toSql)
  }

  lazy val groupByExpression: Option[String] = properties.getString("groupByExp", None).map{ expression =>
    if(expression.toUpperCase.startsWith("GROUP BY"))
      expression.substring("GROUP BY".length)
    else expression
  }
  lazy val groupByExpressionColFormat: Seq[Column] = groupByExpression.map { x => x.replaceAll(" ", "") }
    .get.split(",").map { x => col(x) }.toSeq
  lazy val groupByColumns: Seq[Column] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val cols = s"${properties.getString("groupByColumns", None).notBlank.fold("[]") { values => values.toString }}"

    read[Seq[PropertyColumn]](cols).map(_.toSql)
  }.map { x => col(x.replace(" ", "")) }

  lazy val aggOperations: Map[String, String] = Try(properties.getOptionsList("aggOperations", "operationType", "name"))
    .getOrElse(Map.empty)
    .map { case (key, value) =>
      if (key.toLowerCase.contains("count"))
        ("*", "count")
      else (value, key)
    }

  lazy val aggExpression: Option[String] = properties.getString("aggregateExp", None).notBlank
  lazy val aggExpressionMapped: Map[String, String] = aggExpression match {
    case Some(expression) =>
      expression
        .split(",")
        .flatMap { operation =>
          val values = operation.filterNot(")".toSet).split("\\(")
          val filteredValues = values.map(_.replaceAll(" ", ""))
          val value = Try(filteredValues(1)).toOption
          val key = Try(filteredValues(0)).toOption

          if (key.isDefined && key.get.toLowerCase.contains("count"))
            Option("*" -> "count")
          else if (key.isDefined && value.isDefined)
            Option(value.get -> key.get)
          else None
        }.toMap
    case None => Map.empty
  }

  lazy val pivotColumn: Option[String] = properties.getString("pivotColumn", None).notBlank

  lazy val sqlCommentsRegex: Regex = "--[0-9a-zA-Z-#()]+".r

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The step name $name is not valid.", name))

    //If contains schemas, validate if it can be parsed
    if (inputsModel.inputSchemas.nonEmpty) {
      inputsModel.inputSchemas.foreach { input =>
        if (parserInputSchema(input.schema).isFailure)
          validation = ErrorValidations(
            valid = false,
            messages = validation.messages :+ WorkflowValidationMessage(s"The input schema from step ${input.stepName} is not valid.", name))
      }

      inputsModel.inputSchemas.filterNot(is => SdkSchemaHelper.isCorrectTableName(is.stepName)).foreach { is =>
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ WorkflowValidationMessage(s"The input table name ${is.stepName} is not valid.", name))
      }
    }

    if (pivotColumn.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"Column name for pivot operation is not defined", name))

    if (selectType == SelectType.EXPRESSION && aggExpression.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"Column name for aggregate operation is not defined", name))

    if (selectType == SelectType.COLUMNS && aggOperations.values.exists(x => x.isEmpty))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"Aggregate operation type is not defined", name))

    if (selectType == SelectType.COLUMNS && aggOperations.values.exists(x => Seq("avg", "max", "min", "sum").contains(x)) && aggOperations.keys.exists(x => x.isEmpty))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"Column name for aggregate operation is not defined", name))

    if (selectType == SelectType.COLUMNS && groupByColumns.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"Column name for group by operation is not defined", name))

    if (selectType == SelectType.EXPRESSION && groupByExpression.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"Column name for group by operation is not defined", name))

    validation
  }

  def applyPivot(
                  rdd: RDD[Row],
                  inputStep: String
                ): (RDD[Row], Option[StructType], Option[StructType]) = {
    Try {
      require(pivotColumn.isDefined, "Column name for pivot operation is not defined")

      if (selectType == SelectType.COLUMNS) {
        require(aggOperations.values.nonEmpty, "")
        require(groupByColumns.nonEmpty, "")
      }

      if (selectType == SelectType.EXPRESSION) {
        require(selectExpression.nonEmpty, "")
        require(aggExpression.nonEmpty, "")
        require(groupByExpression.nonEmpty, "")
      }

      val inputSchema = getSchemaFromSessionOrModelOrRdd(xDSession, inputStep, inputsModel, rdd)
      createOrReplaceTemporalViewDf(xDSession, rdd, inputStep, inputSchema) match {
        case Some(_) =>

          val newDataFrame = selectType match {

            case SelectType.COLUMNS =>
              xDSession
                .sql(s"select ${sqlCommentsRegex.replaceAllIn(selectColumns.mkString(","), "")} from $inputStep")
                .groupBy(groupByColumns: _*)
                .pivot(pivotColumn.get)
                .agg(aggOperations)

            case SelectType.EXPRESSION =>
              xDSession
                .sql(s"select ${sqlCommentsRegex.replaceAllIn(selectExpression.mkString(","), "")} from $inputStep")
                .groupBy(groupByExpressionColFormat: _*)
                .pivot(pivotColumn.get)
                .agg(aggExpressionMapped)
          }

          (newDataFrame.rdd, Option(newDataFrame.schema), inputSchema)

        case None =>
          (rdd.filter(_ => false), None, inputSchema)
      }
    } match {
      case Success(sqlResult) => sqlResult
      case Failure(e) => (SparkStepHelper.failRDDWithException(rdd, e), None, None)
    }
  }
}

