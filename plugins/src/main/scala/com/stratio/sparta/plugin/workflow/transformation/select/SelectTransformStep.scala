/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.select

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.enumerations.SelectType
import com.stratio.sparta.plugin.enumerations.SelectType.SelectType
import com.stratio.sparta.plugin.helper.SchemaHelper._
import com.stratio.sparta.plugin.helper.SparkStepHelper
import com.stratio.sparta.plugin.models.PropertyColumn
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper
import com.stratio.sparta.sdk.models.{DiscardCondition, ErrorValidations, OutputOptions, TransformationStepManagement}
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.TransformStep
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.Serialization.read

import scala.util.{Failure, Success, Try}

abstract class SelectTransformStep[Underlying[Row]](
                                                     name: String,
                                                     outputOptions: OutputOptions,
                                                     transformationStepsManagement: TransformationStepManagement,
                                                     ssc: Option[StreamingContext],
                                                     xDSession: XDSession,
                                                     properties: Map[String, JSerializable]
                                                   )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  lazy val selectExpression: Option[String] = properties.getString("selectExp", None).notBlank
  lazy val columns: Seq[String] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val cols = s"${properties.getString("columns", None).notBlank.fold("[]") { values => values.toString }}"

    read[Seq[PropertyColumn]](cols).map(_.toSql)
  }
  lazy val selectType: SelectType = Try {
    SelectType.withName(properties.getString("selectType", "EXPRESSION").toUpperCase())
  }.getOrElse(SelectType.EXPRESSION)

  def requireValidateSql(): Unit = {
    val sql = s"select ${selectExpression.getOrElse("dummyCol")} from dummyTable"
    require(sql.nonEmpty, "The input query can not be empty")
    require(validateSql, "The input query is invalid")
  }

  def validateSql: Boolean =
    Try {
      val expression = selectType match {
        case SelectType.COLUMNS => columns.mkString(",")
        case SelectType.EXPRESSION => selectExpression.getOrElse("*")
      }
      val sql = s"select $expression from dummyTable"
      xDSession.sessionState.sqlParser.parsePlan(sql)
    } match {
      case Success(_) =>
        true
      case Failure(e) =>
        log.warn(s"$name invalid sql. ${e.getLocalizedMessage}")
        false
    }

  //scalastyle:off
  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the step name $name is not valid")

    //If contains schemas, validate if it can be parsed
    if (inputsModel.inputSchemas.nonEmpty) {
      inputsModel.inputSchemas.foreach { input =>
        if (parserInputSchema(input.schema).isFailure)
          validation = ErrorValidations(
            valid = false,
            messages = validation.messages :+ s"$name: the input schema from step ${input.stepName} is not valid")
      }

      inputsModel.inputSchemas.filterNot(is => SdkSchemaHelper.isCorrectTableName(is.stepName)).foreach { is =>
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ s"$name: the input table name ${is.stepName} is not valid")
      }
    }

    if (selectType == SelectType.COLUMNS && columns.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: it's mandatory to specify almost one column"
      )

    if (selectType == SelectType.EXPRESSION && selectExpression.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: it's mandatory one select expression, such as colA, abs(colC)"
      )

    if (selectType == SelectType.EXPRESSION && selectExpression.nonEmpty && !validateSql)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the select expression is invalid"
      )

    if (selectType == SelectType.COLUMNS && columns.nonEmpty && !validateSql)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the select columns are invalid"
      )

    validation
  }

  //scalastyle:on

  def applySelect(rdd: RDD[Row], inputStep: String): (RDD[Row], Option[StructType], Option[StructType]) = {
    Try {
      val inputSchema = getSchemaFromSessionOrModelOrRdd(xDSession, inputStep, inputsModel, rdd)
      createOrReplaceTemporalViewDf(xDSession, rdd, inputStep, inputSchema) match {
        case Some(_) =>
          val expression = selectType match {
            case SelectType.COLUMNS => columns.mkString(",")
            case SelectType.EXPRESSION => selectExpression.getOrElse("*")
          }
          val newDataFrame = xDSession.sql(s"select $expression from $inputStep")
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

