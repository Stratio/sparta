/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.join

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.enumerations.{JoinReturn, JoinTypes, TableSide}
import com.stratio.sparta.plugin.helper.SchemaHelper.parserInputSchema
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.TransformStep
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}

abstract class JoinTransformStep[Underlying[Row]](
                                                   name: String,
                                                   outputOptions: OutputOptions,
                                                   transformationStepsManagement: TransformationStepManagement,
                                                   ssc: Option[StreamingContext],
                                                   xDSession: XDSession,
                                                   properties: Map[String, JSerializable]
                                                 )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  lazy val leftTable: Option[String] = properties.getString("leftTable", None).notBlank
  lazy val rightTable: Option[String] = properties.getString("rightTable", None).notBlank
  lazy val joinReturn: JoinReturn.Value = Try {
    JoinReturn.withName(properties.getString("joinReturn", "all").toUpperCase)
  }.getOrElse(JoinReturn.ALL)
  lazy val joinType: JoinTypes.Value = Try {
    JoinTypes.withName(properties.getString("joinType", "inner").toUpperCase)
  }.getOrElse(JoinTypes.INNER)
  lazy val joinConditions: Seq[JoinCondition] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val conditions =
      s"${properties.getString("joinConditions", None).notBlank.fold("[]") { values => values.toString }}"

    read[Seq[JoinCondition]](conditions)
  }
  lazy val joinReturnColumns: Seq[JoinReturnColumn] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer() +
      new EnumNameSerializer(TableSide)
    val columns =
      s"${properties.getString("joinReturnColumns", None).notBlank.fold("[]") { values => values.toString }}"

    read[Seq[JoinReturnColumn]](columns)
  }
  lazy val returnColumns: String = joinReturnColumns.map(_.toSql(leftTable.get, rightTable.get)).mkString(",")
  lazy val conditions: String = joinType match {
    case JoinTypes.CROSS => ""
    case _ => s" ON ${joinConditions.map(_.toSql(leftTable.get, rightTable.get)).mkString(" AND ")}"
  }
  lazy val whereConditions : String = joinType match {
    case JoinTypes.LEFT_RIGHT_ONLY =>
      s"WHERE ${joinConditions.map(condition =>
        s"${leftTable.get}.${condition.leftField} IS NULL OR ${rightTable.get}.${condition.rightField} IS NULL")
        .mkString(" AND ")}"
    case _ => ""
  }
  lazy val selectFields: String = {
    if (joinType == JoinTypes.LEFT_ONLY || joinType == JoinTypes.RIGHT_ONLY)
      joinReturn match {
        case JoinReturn.COLUMNS => returnColumns
        case _ => "*"
      }
    else joinReturn match {
      case JoinReturn.ALL => "*"
      case JoinReturn.LEFT => s"${leftTable.get}.*"
      case JoinReturn.RIGHT => s"${rightTable.get}.*"
      case JoinReturn.COLUMNS => returnColumns
    }
  }
  lazy val leftTableSql: String = joinType match {
    case JoinTypes.RIGHT_ONLY => rightTable.get
    case _ => leftTable.get
  }
  lazy val rightTableSql: String = joinType match {
    case JoinTypes.RIGHT_ONLY => leftTable.get
    case _ => rightTable.get
  }
  lazy val sql: String = s"SELECT $selectFields FROM $leftTableSql ${JoinTypes.joinTypeToSql(joinType)} JOIN" +
    s" $rightTableSql $conditions $whereConditions"

  def requireValidateSql(): Unit = {
    require(leftTable.nonEmpty, "The left table cannot be empty")
    require(rightTable.nonEmpty, "The right table cannot be empty")
    require(validateSql, "The join expression is invalid")
  }

  def validateSql: Boolean =
    Try {
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
        messages = validation.messages :+ WorkflowValidationMessage(s"The step name $name is not valid", name))

    if (Try(joinConditions.nonEmpty).isFailure) {
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The join conditions are not valid", name))
    }

    if (Try(joinReturnColumns.nonEmpty).isFailure) {
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The join return columns are not valid.", name))
    }

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

    if (leftTable.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"It's mandatory to specify the left table.", name)
      )

    if (rightTable.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"It's mandatory to specify the right table.", name)
      )

    if (joinType != JoinTypes.CROSS && joinConditions.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"It's mandatory to specify a join condition.", name)
      )

    if (joinReturn == JoinReturn.COLUMNS && joinReturnColumns.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"It's mandatory to specify the resulting columns to be returned.", name)
      )

    if (leftTable.nonEmpty && rightTable.nonEmpty && !validateSql)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The join expression is invalid.", name)
      )

    validation
  }
}

