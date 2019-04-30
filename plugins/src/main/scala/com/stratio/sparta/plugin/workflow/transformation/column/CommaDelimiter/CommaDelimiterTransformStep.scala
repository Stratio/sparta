/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.column.CommaDelimiter

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, TransformationStepManagement}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.TransformStep
import com.stratio.sparta.plugin.enumerations.CommaDelimiterDirectionModel
import com.stratio.sparta.plugin.enumerations.CommaDelimiterDirectionModel._
import com.stratio.sparta.plugin.helper.SchemaHelper.{createOrReplaceTemporalViewDf, getSchemaFromSessionOrModelOrRdd, parserInputSchema}
import com.stratio.sparta.plugin.helper.SparkStepHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types._
import org.apache.spark.streaming.StreamingContext
import org.json4s.jackson.Serialization.read
import com.stratio.sparta.plugin.models.CommaDelimiterModel

import scala.util._

class CommaDelimiterTransformStep[Underlying[Row]](
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

  lazy val power = 10

  lazy val columnsToDelimit: Seq[CommaDelimiterModel] = {
    val delimitColumns = properties.getString("columnsToDelimit", None).notBlank.getOrElse("[]")
    read[Seq[CommaDelimiterModel]](delimitColumns)
  }



  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    //If contains schemas, validate if it can be parsed
    val inputSchemaValidationErrors: Seq[(HasError, String)] = inputsModel.inputSchemas.flatMap { input =>
      Seq(
        parserInputSchema(input.schema).isFailure -> s"The input schema from step ${input.stepName} is not valid.",
        !SdkSchemaHelper.isCorrectTableName(input.stepName) -> s"The input table name ${input.stepName} is not valid."
      )
    }
    val emptyValues: Seq[(HasError, String)] = columnsToDelimit
      .zipWithIndex.
      flatMap({ case (element: CommaDelimiterModel, index: Int) =>
      Seq(
        element.name.isEmpty -> s"Element ${index + 1}: Name is empty",
        element.decimalLength.isEmpty -> s"Element ${index + 1}: Decimal length is empty",
        element.decimalSeparator.isEmpty -> s"Element ${index + 1}: Decimal separator is empty"
        //!element.displacementDirection.equals(Case) -> "hola"
      )
    })

    val validationSeq = Seq[(HasError, String)](
      !SdkSchemaHelper.isCorrectTableName(name) -> s"The step name $name is not valid",
      columnsToDelimit.isEmpty -> "It's mandatory to choose at least one column to modify"
    ) ++ inputSchemaValidationErrors ++ emptyValues

    ErrorValidationsHelper.validate(validationSeq, name)
  }

  //scalastyle:off
  def applyDelimiter(rdd: RDD[Row], inputStep: String): (RDD[Row], Option[StructType], Option[StructType]) = {

    Try {
      val inputSchema = getSchemaFromSessionOrModelOrRdd(xDSession, inputStep, inputsModel, rdd)
      createOrReplaceTemporalViewDf(xDSession, rdd, inputStep, inputSchema) match {
        case Some(inputDataFrame) =>
          import org.apache.spark.sql.functions._

          val newDataFrame = columnsToDelimit.foldLeft(inputDataFrame)({
            case (stateDataFrame, columnToOperate: CommaDelimiterModel) =>

              val delimiter = scala.math.pow(power, columnToOperate.decimalLength.toInt)

              val structType: StructType = stateDataFrame.schema
              val column = structType.find(_.name == columnToOperate.name)
              val colDataType = column.get.dataType

              val dataFrameToDouble = if (colDataType == StringType) {
                stateDataFrame.withColumn(columnToOperate.name, col(columnToOperate.name).cast(DoubleType))
              } else stateDataFrame

              val dataFrameDelimited = if (CommaDelimiterDirectionModel.COMMA_LEFT == columnToOperate.displacementDirection) {
                dataFrameToDouble.withColumn(columnToOperate.name, col(columnToOperate.name) / delimiter)
              } else if (CommaDelimiterDirectionModel.COMMA_RIGHT == columnToOperate.displacementDirection) {
                dataFrameToDouble.withColumn(columnToOperate.name, col(columnToOperate.name) * delimiter)
              } else dataFrameToDouble

              val castedDataframe = if(colDataType == FloatType){
                dataFrameDelimited.withColumn(columnToOperate.name, col(columnToOperate.name).cast(FloatType))
              }else dataFrameDelimited

              if (colDataType == StringType && columnToOperate.decimalSeparator == ".") {
                dataFrameDelimited.withColumn(columnToOperate.name, regexp_replace(col(columnToOperate.name), "[,]", "."))
              } else if (colDataType == StringType && columnToOperate.decimalSeparator == ",") {
                dataFrameDelimited.withColumn(columnToOperate.name, regexp_replace(col(columnToOperate.name), "[.]", ","))
              } else castedDataframe
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
