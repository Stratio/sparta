/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.orderBy

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{ErrorValidations, OutputOptions, TransformStep, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.functions.col
import org.apache.spark.streaming.StreamingContext

import scala.util.{Failure, Success, Try}


abstract class OrderByTransformStep[Underlying[Row]](
                                                      name: String,
                                                      outputOptions: OutputOptions,
                                                      transformationStepsManagement: TransformationStepManagement,
                                                      ssc: Option[StreamingContext],
                                                      xDSession: XDSession,
                                                      properties: Map[String, JSerializable])
                                                    (implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  lazy val orderExpression: Option[String] = properties.getString("orderExp", None).notBlank
  lazy val fieldsSeparator: String = properties.getString("delimiter", ",")

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (orderExpression.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: it's mandatory one order expression, such as colA, colB"
      )

    validation
  }

  def transformFunc(expression: String): RDD[Row] => RDD[Row] = {
    rdd =>
      Try {
        if (rdd.isEmpty()) rdd
        else {
          val schema = rdd.first().schema
          val df = xDSession.createDataFrame(rdd, schema)
          val columns = expression.split(fieldsSeparator).map(col)

          df.sort(columns: _*).rdd
        }
      } match {
        case Success(sqlResult) => sqlResult
        case Failure(e) => rdd.map(_ => Row.fromSeq(throw e))
      }
  }

}