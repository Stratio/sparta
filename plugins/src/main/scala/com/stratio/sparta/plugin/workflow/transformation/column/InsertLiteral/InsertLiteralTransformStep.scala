/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.column.InsertLiteral

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.TransformStep
import com.stratio.sparta.plugin.helper.SchemaHelper.{createOrReplaceTemporalViewDf, getSchemaFromSessionOrModelOrRdd, parserInputSchema}
import com.stratio.sparta.plugin.helper.SparkStepHelper
import com.stratio.sparta.plugin.models.PropertyColumnToInsertLiteral
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.json4s.jackson.Serialization.read

import scala.util.{Failure, Success, Try}

//scalastyle:off
abstract class InsertLiteralTransformStep[Underlying[Row]](
                                                            name: String,
                                                            outputOptions: OutputOptions,
                                                            transformationStepsManagement: TransformationStepManagement,
                                                            ssc: Option[StreamingContext],
                                                            xDSession: XDSession,
                                                            properties: Map[String, JSerializable]
                                                          )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  import com.stratio.sparta.plugin.models.SerializationImplicits._

  lazy val columnsToinsertLiteral: Seq[PropertyColumnToInsertLiteral] = {
    val colsToFormat = properties.getString("columnsToInsertLiteral", None).notBlank.getOrElse("[]")
    read[Seq[PropertyColumnToInsertLiteral]](colsToFormat)
  }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    //If contains schemas, validate if it can be parsed
    val inputSchemaValidationErrors: Seq[(HasError, String)] = inputsModel.inputSchemas.flatMap { input =>
      Seq(
        parserInputSchema(input.schema).isFailure -> s"The input schema from step ${input.stepName} is not valid.",
        !SdkSchemaHelper.isCorrectTableName(input.stepName) -> s"The input table name ${input.stepName} is not valid."
      )
    }

    val emptyValues: Seq[(HasError, String)] = columnsToinsertLiteral.zipWithIndex.flatMap({ case (element: PropertyColumnToInsertLiteral, index: Int) =>
      Seq(
        element.name.isEmpty -> s"Element ${index + 1}: Name of column to insert literal is empty.",
        element.value.isEmpty -> s"Element ${index + 1}: Value to insert is empty.",
        Option(element.offset).isEmpty -> s"Element ${index + 1}: Offset is empty.",
        element.offsetFrom.isEmpty -> s"Element ${index + 1}: Offset from is empty."
      )
    })

    val validationSeq = Seq[(HasError, String)](
      !SdkSchemaHelper.isCorrectTableName(name) -> s"The step name $name is not valid",
      columnsToinsertLiteral.isEmpty -> "It's mandatory to add at least one column to insert a literal"
    ) ++ inputSchemaValidationErrors ++ emptyValues

    ErrorValidationsHelper.validate(validationSeq, name)
  }


  def applyInsertLiteral(rdd: RDD[Row], inputStep: String): (RDD[Row], Option[StructType], Option[StructType]) = {
    Try {
      val inputSchema = getSchemaFromSessionOrModelOrRdd(xDSession, inputStep, inputsModel, rdd)
      createOrReplaceTemporalViewDf(xDSession, rdd, inputStep, inputSchema) match {
        case Some(df) =>
          import org.apache.spark.sql.functions._

          val newDataFrame = columnsToinsertLiteral.foldLeft(df)({
            case (df2, columnToOperate: PropertyColumnToInsertLiteral) =>

              val colLengthMax = 150
              val initialPosition = 0
              val posAux = 1
              val colLength = length(col(columnToOperate.name))

              columnToOperate.offsetFrom match {
                case "INSERT_LEFT" =>

                  df2.withColumn(columnToOperate.name,
                    when(colLength.geq(columnToOperate.offset),
                      concat(
                        substring(col(columnToOperate.name), initialPosition, columnToOperate.offset.toInt),
                        lit(columnToOperate.value),
                        substring(col(columnToOperate.name), columnToOperate.offset.toInt + posAux, colLengthMax)
                      )
                    ).otherwise(
                      when(col(columnToOperate.name).isNotNull,
                        concat(col(columnToOperate.name), lit(columnToOperate.value))
                      ).otherwise(lit(columnToOperate.value))))

                case "INSERT_RIGHT" =>
                  df2.withColumn(columnToOperate.name,
                    when(colLength.geq(columnToOperate.offset),
                      concat(col(columnToOperate.name).substr(lit(initialPosition), colLength - columnToOperate.offset),
                        lit(columnToOperate.value),
                        col(columnToOperate.name).substr(colLength - columnToOperate.offset + posAux, lit(columnToOperate.offset))))
                      .otherwise(when(col(columnToOperate.name).isNotNull, concat(lit(columnToOperate.value), col(columnToOperate.name)))
                        .otherwise(lit(columnToOperate.value))))

                case _ => df2
              }

          })
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


