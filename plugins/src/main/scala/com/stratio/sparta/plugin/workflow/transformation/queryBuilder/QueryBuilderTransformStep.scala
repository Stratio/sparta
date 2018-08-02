/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.queryBuilder

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.TransformStep
import com.stratio.sparta.plugin.enumerations.{JoinTypes, OrderByType}
import com.stratio.sparta.plugin.helper.SchemaHelper.parserInputSchema
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}

abstract class QueryBuilderTransformStep[Underlying[Row]](
                                                          name: String,
                                                          outputOptions: OutputOptions,
                                                          transformationStepsManagement: TransformationStepManagement,
                                                          ssc: Option[StreamingContext],
                                                          xDSession: XDSession,
                                                          properties: Map[String, JSerializable]
                                                        )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  lazy val executeSqlWhenEmpty: Boolean = Try(properties.getBoolean("executeSqlWhenEmpty", default = true))
    .getOrElse(true)
  lazy val visualQuery: VisualQuery = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer() +
      new EnumNameSerializer(JoinTypes) +  new EnumNameSerializer(OrderByType)

    read[VisualQuery](properties.getString("visualQuery"))
  }
  lazy val sql: String = visualQuery.toSql

  def requireValidateSql(inputDataSize : Int): Unit = {
    require(Try(visualQuery).isSuccess, "The input model in the sql builder it is invalid")
    require(visualQuery.fromClause.nonEmpty || visualQuery.joinClause.nonEmpty,
      "It is mandatory to define either a FROM or a JOIN expression")
    require(visualQuery.joinClause.isEmpty || (visualQuery.joinClause.isDefined && inputDataSize == 2),
      "When the query contains joins, it is mandatory to specify two input steps")
    require(validateSql, "The sql expression generated is invalid")
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

    if (Try(sql).isSuccess && sql.nonEmpty && !validateSql)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The input sql query is invalid.", name)
      )

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

    validation
  }
}

