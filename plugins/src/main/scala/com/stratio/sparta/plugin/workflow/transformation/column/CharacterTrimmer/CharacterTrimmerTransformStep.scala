/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.column.CharacterTrimmer

import java.io.{Serializable => JSerializable}
import java.util.regex.Pattern

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, TransformationStepManagement}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.TransformStep
import com.stratio.sparta.plugin.enumerations.TrimType
import com.stratio.sparta.plugin.helper.SchemaHelper.{createOrReplaceTemporalViewDf, getSchemaFromSessionOrModelOrRdd, parserInputSchema}
import com.stratio.sparta.plugin.helper.SparkStepHelper
import com.stratio.sparta.plugin.models.CharacterTrimmerModel
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.json4s.jackson.Serialization.read

import scala.util._

abstract class CharacterTrimmerTransformStep[Underlying[Row]](
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

  lazy val columnsToOperate: Seq[CharacterTrimmerModel] = {

    val columnsToTrim = properties.getString("columnsToTrim", None).notBlank.getOrElse("[]")

    read[Seq[CharacterTrimmerModel]](columnsToTrim)
  }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    //If contains schemas, validate if it can be parsed
    val inputSchemaValidationErrors: Seq[(HasError, String)] = inputsModel.inputSchemas.flatMap { input =>
      Seq(
        parserInputSchema(input.schema).isFailure -> s"The input schema from step ${input.stepName} is not valid.",
        !SdkSchemaHelper.isCorrectTableName(input.stepName) -> s"The input table name ${input.stepName} is not valid."
      )
    }
    val emptyValues: Seq[(HasError, String)] = columnsToOperate.zipWithIndex.flatMap({ case (element: CharacterTrimmerModel, index: Int) =>
      Seq(
        element.name.isEmpty -> s"Element ${index + 1}: Name is empty"
      )
    })
    val validationSeq = Seq[(HasError, String)](
      !SdkSchemaHelper.isCorrectTableName(name) -> s"The step name $name is not valid",
      columnsToOperate.isEmpty -> "It's mandatory to choose at least one column to modify"
    ) ++ inputSchemaValidationErrors ++ emptyValues

    ErrorValidationsHelper.validate(validationSeq, name)
  }


  def applyTrim(rdd: RDD[Row], inputStep: String): (RDD[Row], Option[StructType], Option[StructType]) = {
    Try {
      val inputSchema = getSchemaFromSessionOrModelOrRdd(xDSession, inputStep, inputsModel, rdd)
      createOrReplaceTemporalViewDf(xDSession, rdd, inputStep, inputSchema) match {
        case Some(df) =>
          import org.apache.spark.sql.functions._

          val newDataFrame = columnsToOperate.foldLeft(df)({
            case (df2, columnToOperate: CharacterTrimmerModel) =>

              columnToOperate.trimType match {
                case TrimType.TRIM_LEFT =>
                  df2.withColumn(
                    columnToOperate.name,
                    regexp_replace(col(columnToOperate.name), "^" + Pattern.quote(columnToOperate.characterToTrim), "")
                    )
                case TrimType.TRIM_RIGHT =>
                  df2.withColumn(
                    columnToOperate.name,
                    regexp_replace(col(columnToOperate.name),  Pattern.quote(columnToOperate.characterToTrim) + "$", "")
                  )
                case TrimType.TRIM_BOTH =>
                df2.withColumn(
                  columnToOperate.name,
                  regexp_replace(regexp_replace(col(columnToOperate.name), "^" + Pattern.quote(columnToOperate.characterToTrim), ""),
                    Pattern.quote(columnToOperate.characterToTrim) + "$", "")
                )
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