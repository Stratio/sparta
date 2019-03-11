/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.column

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
import com.stratio.sparta.plugin.models.{PropertyQuery, SelectExpressionModel}
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}

//scalastyle:off
abstract class AddColumnsTransformStep[Underlying[Row]](
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
    SelectType.withName(properties.getString("selectType", "COLUMNS").toUpperCase())
  }.getOrElse(SelectType.EXPRESSION)


  lazy val expressionList: Seq[SelectExpressionModel] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val expression = s"${properties.getString("addColumnExpressionList", None).notBlank.fold("[]") { values => values.toString }}"

    read[Seq[SelectExpressionModel]](expression)
  }


  lazy val columns: Seq[PropertyQuery] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val cols = s"${properties.getString("columns", None).notBlank.fold("[]") { values => values.toString }}"

    read[Seq[PropertyQuery]](cols)
  }


  lazy val newColumnsSchemaTypes: Map[String, Any] = columns.map { outputField =>
    val outputType = outputField.`type`.notBlank.getOrElse("string")
    val schema = StructField(
      name = outputField.field,
      dataType = SparkTypes.get(outputType) match {
        case Some(sparkType) => sparkType
        case None => schemaFromString(outputType)
      },
      nullable = true
    )
    outputField.field -> castingToOutputSchema(schema, outputField.query)
  }.toMap


  def validateExpression: Seq[(Boolean, String)] = {

    expressionList.collect {
      case selectExpression if Try(expr(selectExpression.addColumnExpression)).isFailure =>
        true -> s"$selectExpression expression is not valid."
    }
  }


  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {

    //If contains schemas, validate if it can be parsed
    val schemaValidations = inputsModel.inputSchemas.headOption.map { _ =>
      inputsModel
        .inputSchemas
        .flatMap(input => Seq(
          !SdkSchemaHelper.isCorrectTableName(input.stepName) -> s"The input table name ${input.stepName} is not valid.",
          parserInputSchema(input.schema).isFailure -> s"The input schema from step ${input.stepName} is not valid."
        )
        )
    }.getOrElse(Seq.empty)

    val emptyColumnsValidation: Seq[(HasError, String)] = columns.map(c => c.query.isEmpty -> "New column value is not valid.")
    val columnNameValidation: Seq[(HasError, String)] = columns.map(c => (c.field.isEmpty || c.field.contains(" ")) -> "New column name is not valid.")

    val validationSeq: Seq[(HasError, String)] = Seq(
      !SdkSchemaHelper.isCorrectTableName(name) -> s"The step name $name is not valid.",
      (selectType == SelectType.COLUMNS && columns.isEmpty) -> "It's mandatory to specify at least one column.",
      Try(newColumnsSchemaTypes.nonEmpty).isFailure -> "Column value and type do not match"
    ) ++ emptyColumnsValidation ++ columnNameValidation ++ schemaValidations ++ validateExpression

    ErrorValidationsHelper.validate(validationSeq, name)
  }

  def applyValues(rdd: RDD[Row], inputStep: String): (RDD[Row], Option[StructType], Option[StructType]) = {
    Try {
      val inputSchema = getSchemaFromSessionOrModelOrRdd(xDSession, inputStep, inputsModel, rdd)
      createOrReplaceTemporalViewDf(xDSession, rdd, inputStep, inputSchema) match {
        case Some(df) =>
          val newDataFrame = selectType match {

            case SelectType.COLUMNS =>
              columns.foldLeft(df) { (df, defValue) =>
                import org.apache.spark.sql.functions._
                df.withColumn(defValue.field, lit(newColumnsSchemaTypes.getOrElse(defValue.field, "")))
              }

            case SelectType.EXPRESSION =>
              expressionList.foldLeft(df) { (dfState, columnToAdd) =>
                import org.apache.spark.sql.functions._
                df.withColumn(columnToAdd.addColumnAlias, expr(columnToAdd.addColumnExpression))
              }


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