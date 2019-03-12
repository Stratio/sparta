/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.caseWhen

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, TransformationStepManagement}
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.TransformStep
import com.stratio.sparta.plugin.enumerations.CaseOutputStrategy.CaseOutputStrategy
import com.stratio.sparta.plugin.enumerations.{CaseOutputStrategy, CaseValueType}
import com.stratio.sparta.plugin.helper.SchemaHelper._
import com.stratio.sparta.plugin.helper.SparkStepHelper
import com.stratio.sparta.plugin.models.CaseModel
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.sql.{Column, DataFrame, Row}
import org.apache.spark.streaming.StreamingContext
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}

//scalastyle:off
abstract class CaseTransformStep[Underlying[Row]](
                                                   name: String,
                                                   outputOptions: OutputOptions,
                                                   transformationStepsManagement: TransformationStepManagement,
                                                   ssc: Option[StreamingContext],
                                                   xDSession: XDSession,
                                                   properties: Map[String, JSerializable]
                                                 )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  lazy val caseExpressionsList: Seq[CaseModel] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats +
      new JsoneyStringSerializer() +
      new EnumNameSerializer(CaseValueType)
    val inputColumn = s"${properties.getString("inputColumns", None).notBlank.fold("[]") { values => values.toString }}"
    read[Seq[CaseModel]](inputColumn)
  }

  lazy val otherwiseExpression: Option[String] = properties.getString("otherwiseExpression", None)


  lazy val outputDataType: StructField = {
    val outputType = properties.getString("outputDataType", "string")
    StructField(
      name = "dummy",
      dataType = SparkTypes.get(outputType) match {
        case Some(sparkType) => sparkType
        case None => schemaFromString(outputType)
      },
      nullable = true
    )
  }

  lazy val userOutputStrategy: Option[String] = properties.getString("outputStrategy", None).notBlank

  lazy val outputStrategy: CaseOutputStrategy = Try {
    CaseOutputStrategy.withName(userOutputStrategy.getOrElse(("REPLACECOLUMN")).toUpperCase())
  }.getOrElse(CaseOutputStrategy.REPLACECOLUMN)

  lazy val aliasNewColumn: Option[String] = properties.getString("aliasNewColumn", None)

  lazy val columnToReplace: Option[String] = properties.getString("columnToReplace", None)

  lazy val emptyValues: Seq[(HasError, String)] = caseExpressionsList.zipWithIndex.flatMap({ case (element: CaseModel, index: Int) =>

    if (element.valueType == CaseValueType.VALUE) {
      Seq(
        element.value.notBlank.isEmpty -> s"Input column ${index + 1}: Value is empty",
        element.caseExpression.notBlank.isEmpty -> s"Input column ${index + 1}: Case expression is empty")
    } else {
      Seq(
        element.column.notBlank.isEmpty -> s"Input column ${index + 1}: Column is empty",
        element.caseExpression.notBlank.isEmpty -> s"Input column ${index + 1}: Case expression is empty")
    }
  })


  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {

    //If contains schemas, validate if it can be parsed
    val inputSchemaValidationErrors: Seq[(HasError, String)] = inputsModel.inputSchemas.flatMap { input =>
      Seq(
        parserInputSchema(input.schema).isFailure -> s"The input schema from step ${input.stepName} is not valid.",
        !SdkSchemaHelper.isCorrectTableName(input.stepName) -> s"The input table name ${input.stepName} is not valid."
      )
    }

    val validationSeq: Seq[(HasError, String)] = Seq(
      !SdkSchemaHelper.isCorrectTableName(name) -> s"The step name $name is not valid",
      caseExpressionsList.isEmpty -> "It's mandatory to specify at least one column to be replaced",
      userOutputStrategy.isEmpty -> "It's mandatory to specify an output strategy",
      (outputStrategy.equals(CaseOutputStrategy.NEWCOLUMN) && aliasNewColumn.isEmpty) -> "It's mandatory to specify the name for the new column",
      (outputStrategy.equals(CaseOutputStrategy.REPLACECOLUMN) && columnToReplace.isEmpty) -> "It's mandatory to specify one column to replace data"
    ) ++ inputSchemaValidationErrors ++ emptyValues

    ErrorValidationsHelper.validate(validationSeq, name)
  }


  def applyCase(rdd: RDD[Row], inputStep: String): (RDD[Row], Option[StructType], Option[StructType]) = {
    Try {
      val validateResult = validate()

      require(validateResult.valid, validateResult.messages.map(_.message).mkString(". "))

      val inputSchema = getSchemaFromSessionOrModelOrRdd(xDSession, inputStep, inputsModel, rdd)

      val columnName: String = {
        if (outputStrategy == CaseOutputStrategy.NEWCOLUMN && aliasNewColumn.isDefined) {
          aliasNewColumn.get.toString
        } else if (outputStrategy == CaseOutputStrategy.REPLACECOLUMN && columnToReplace.isDefined) {
          columnToReplace.get.toString
        } else throw new Exception("Alias for new column or column to replace must be specified")
      }


      createOrReplaceTemporalViewDf(xDSession, rdd, inputStep, inputSchema) match {
        case Some(df) =>
          import org.apache.spark.sql.functions._
          val newDataFrame: DataFrame = {
            val newColumn = caseExpressionsList.foldLeft(Option.empty[Column]) { (prevColumn, defValue) =>
              val nextColumn = {
                val colValue = if (defValue.valueType == CaseValueType.COLUMN)
                  col(defValue.column.get)
                else {
                  val value = castingToOutputSchema(outputDataType, defValue.value.get)
                  lit(value)
                }
                val condition = expr(defValue.caseExpression.get)

                prevColumn match {
                  case Some(column) =>
                    column.when(condition, colValue)
                  case None =>
                    when(condition, colValue)
                }
              }
              Option(nextColumn)
            }
            newColumn.fold(df) { column =>

              val colOther = otherwiseExpression match {
                case Some(text) => column.otherwise(castingToOutputSchema(outputDataType, text))
                case None => column
              }
              val colWithAlias = colOther.alias(columnName)
              val newDf = df.withColumn(columnName, colWithAlias)
              newDf.rdd.foreach(row => println(row.toString()))
              newDf
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
