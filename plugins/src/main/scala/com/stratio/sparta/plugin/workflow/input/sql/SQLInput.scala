/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.sql

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.InputStep
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext

import scala.util.{Failure, Success, Try}


abstract class SQLInput[Underlying[Row]](
                                          name: String,
                                          outputOptions: OutputOptions,
                                          ssc: Option[StreamingContext],
                                          xDSession: XDSession,
                                          properties: Map[String, JSerializable]
                                        )
  extends InputStep[Underlying](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {


  lazy val query: String = properties.getString("query", "").trim

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    val validationSeq = Seq[(HasError, String)](
      !SdkSchemaHelper.isCorrectTableName(name) -> s"The step name $name is not valid.",
      query.isEmpty -> "The input query cannot be empty",
      (query.nonEmpty && !isValidQuery(query)) -> "The input query is not a valid SQL",
      (debugOptions.isDefined && !validDebuggingOptions) -> errorDebugValidation
    )
    ErrorValidationsHelper.validate(validationSeq, name)
  }

  protected def isValidQuery(q: String): Boolean =
    Try(xDSession.sessionState.sqlParser.parsePlan(q)) match {
      case Success(_) =>
        true
      case Failure(e) =>
        log.error(s"$name invalid sql: $q", e)
        false
    }

}
