/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.TransformStep
import com.stratio.sparta.plugin.helper.SchemaHelper._
import com.stratio.sparta.plugin.helper.SparkStepHelper
import com.stratio.sparta.plugin.workflow.transformation.cube.models.CubeModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

import scala.util.{Failure, Success, Try}

class CubeTransformStepBatch(
                              name: String,
                              outputOptions: OutputOptions,
                              transformationStepsManagement: TransformationStepManagement,
                              ssc: Option[StreamingContext],
                              xDSession: XDSession,
                              properties: Map[String, JSerializable]
                            )
  extends TransformStep[RDD](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  lazy val cubeModel: Try[CubeModel] = {
    Try(CubeModel.getCubeModel(properties.getString("dimensions"), properties.getString("operators")))
  }

  //scalastyle:off
  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The step name $name is not valid.", name))

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

    if (cubeModel.isSuccess && (cubeModel.get.dimensions.isEmpty || cubeModel.get.operators.isEmpty))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"It's mandatory to specify almost one dimension and one operator.", name)
      )

    if (cubeModel.isFailure)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The cube definition is invalid.", name)
      )

    validation
  }

  override def transformWithSchema(
                                    inputData: Map[String, DistributedMonad[RDD]]
                                  ): (DistributedMonad[RDD], Option[StructType], Option[StructType]) = {
    val (data, schema, inputSchema) = applyHeadTransformSchema(inputData) { case (stepName, inputDistributedMonad) =>
      val rdd = inputDistributedMonad.ds
      Try {
        val inputSchema = getSchemaFromSessionOrModelOrRdd(xDSession, stepName, inputsModel, rdd)
        createOrReplaceTemporalViewDf(xDSession, rdd, stepName, inputSchema) match {
          case Some(_) =>
            val dimensions = cubeModel.get.dimensions.map(dimension => dimension.name).mkString(",")
            val operators = cubeModel.get.operators.map { operator =>
              operator.inputField.notBlank match {
                case Some(field) => s"${operator.classType.toLowerCase}($field) as ${operator.name}"
                case None =>
                  if(operator.classType.toLowerCase == "count")
                    s"${operator.classType.toLowerCase}(*) as ${operator.name}"
                  else throw new Exception(s"Invalid operator: $operator")
              }
            }.mkString(",")
            val newDataFrame = xDSession.sql(s"select $dimensions , $operators from $stepName group by $dimensions")

            (newDataFrame.rdd, Option(newDataFrame.schema), inputSchema)
          case None =>
            (rdd.filter(_ => false), None, inputSchema)
        }
      } match {
        case Success(sqlResult) => (sqlResult._1, sqlResult._2, sqlResult._3)
        case Failure(e) => (SparkStepHelper.failRDDWithException(rdd, e), None, None)
      }
    }

    (data, schema, inputSchema)
  }
}

