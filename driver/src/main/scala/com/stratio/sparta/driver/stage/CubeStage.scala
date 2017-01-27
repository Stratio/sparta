/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.sparta.driver.stage

import java.io.Serializable

import com.stratio.sparta.driver.cube.Cube
import com.stratio.sparta.driver.helper.SchemaHelper
import com.stratio.sparta.driver.helper.SchemaHelper.DefaultTimeStampTypeString
import com.stratio.sparta.driver.trigger.Trigger
import com.stratio.sparta.driver.utils.ReflectionUtils
import com.stratio.sparta.driver.writer.{CubeWriter, CubeWriterOptions}
import com.stratio.sparta.sdk.pipeline.aggregation.cube.{Dimension, DimensionType}
import com.stratio.sparta.sdk.pipeline.aggregation.operator.Operator
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.sdk.pipeline.schema.SpartaSchema
import com.stratio.sparta.sdk.pipeline.schema.TypeOp.TypeOp
import com.stratio.sparta.serving.core.models.PhaseEnum
import com.stratio.sparta.serving.core.models.policy.cube.{CubeModel, OperatorModel}
import com.stratio.sparta.serving.core.models.policy.trigger.TriggerModel
import org.apache.spark.sql.types.StructType

trait CubeStage extends BaseStage {
  this: ErrorPersistor =>

  def cubeStage(refUtils: ReflectionUtils,
                initSchema: StructType): Seq[Cube] = {
    policy.cubes.map(cube => createCube(cube, refUtils, initSchema: StructType))
  }

  private def createCube(cubeModel: CubeModel,
                         refUtils: ReflectionUtils,
                         initSchema: StructType): Cube = {
    val okMessage = s"Cube: ${cubeModel.name} created correctly."
    val errorMessage = s"Something gone wrong creating the cube: ${cubeModel.name}. Please re-check the policy."
    generalTransformation(PhaseEnum.Cube, okMessage, errorMessage) {
      val name = cubeModel.name
      val dimensions = cubeModel.dimensions.map(dimensionDto => {
        val fieldType = initSchema.find(stField => stField.name == dimensionDto.field).map(_.dataType)
        val defaultType = fieldType.flatMap(field => SchemaHelper.mapSparkTypes.get(field))

        new Dimension(dimensionDto.name,
          dimensionDto.field,
          dimensionDto.precision,
          instantiateDimensionType(dimensionDto.`type`, dimensionDto.configuration, refUtils, defaultType))
      })
      val operators = getOperators(cubeModel.operators, refUtils, initSchema)
      val expiringDataConfig = SchemaHelper.getExpiringData(cubeModel)
      val triggers = triggerStage(cubeModel.triggers)
      Cube(name, dimensions, operators, initSchema, expiringDataConfig, triggers)
    }
  }

  def getOperators(operatorsModel: Seq[OperatorModel],
                   refUtils: ReflectionUtils,
                   initSchema: StructType): Seq[Operator] =
    operatorsModel.map(operator => createOperator(operator, refUtils, initSchema))


  def triggerStage(triggers: Seq[TriggerModel]): Seq[Trigger] =
    triggers.map(trigger => createTrigger(trigger))

  private def createTrigger(trigger: TriggerModel): Trigger = {
    val okMessage = s"Trigger: ${trigger.name} created correctly."
    val errorMessage = s"Something gone wrong creating the trigger: ${trigger.name}. Please re-check the policy."
    generalTransformation(PhaseEnum.Trigger, okMessage, errorMessage) {
      Trigger(
        trigger.name,
        trigger.sql,
        trigger.writer.outputs,
        trigger.overLast,
        trigger.computeEvery,
        trigger.primaryKey,
        trigger.writer.saveMode,
        trigger.configuration)
    }
  }

  private def createOperator(model: OperatorModel,
                             refUtils: ReflectionUtils,
                             initSchema: StructType): Operator = {
    val okMessage = s"Operator: ${model.`type`} created correctly."
    val errorMessage = s"Something gone wrong creating the operator: ${model.`type`}. Please re-check the policy."
    generalTransformation(PhaseEnum.Operator, okMessage, errorMessage) {
      refUtils.tryToInstantiate[Operator](model.`type` + Operator.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[StructType],
          classOf[Map[String, Serializable]])
          .newInstance(model.name, initSchema, model.configuration).asInstanceOf[Operator])
    }
  }

  private def instantiateDimensionType(dimensionType: String,
                                       configuration: Option[Map[String, String]],
                                       refUtils: ReflectionUtils,
                                       defaultType: Option[TypeOp]): DimensionType =
    refUtils.tryToInstantiate[DimensionType](dimensionType + Dimension.FieldClassSuffix, (c) => {
      (configuration, defaultType) match {
        case (Some(conf), Some(defType)) =>
          c.getDeclaredConstructor(classOf[Map[String, Serializable]], classOf[TypeOp])
            .newInstance(conf, defType).asInstanceOf[DimensionType]
        case (Some(conf), None) =>
          c.getDeclaredConstructor(classOf[Map[String, Serializable]]).newInstance(conf).asInstanceOf[DimensionType]
        case (None, Some(defType)) =>
          c.getDeclaredConstructor(classOf[TypeOp]).newInstance(defType).asInstanceOf[DimensionType]
        case (None, None) =>
          c.getDeclaredConstructor().newInstance().asInstanceOf[DimensionType]
      }
    })


  def getCubeWriter(cubeName: String,
                    cubes: Seq[Cube],
                    schemas: Seq[SpartaSchema],
                    triggerSchemas: Seq[SpartaSchema],
                    cubeModels: Seq[CubeModel],
                    outputs: Seq[Output],
                    triggersOutputs: Seq[Output]): CubeWriter = {
    val cubeWriter = cubes.find(cube => cube.name == cubeName)
      .getOrElse(throw new Exception("Is mandatory one cube in the cube writer"))
    val schemaWriter = schemas.find(schema => schema.tableName == cubeName)
      .getOrElse(throw new Exception("Is mandatory one schema in the cube writer"))
    val cubeModel = cubeModels.find(cube => cube.name == cubeName)
      .getOrElse(throw new Exception("Is mandatory one cubeModel in the cube writer"))
    val writerOp = getCubeWriterOptions(cubeName, outputs, cubeModel)

    CubeWriter(cubeWriter, schemaWriter, writerOp, outputs, triggersOutputs, triggerSchemas)
  }

  def getCubeWriterOptions(cubeName: String, outputsWriter: Seq[Output], cubeModel: CubeModel): CubeWriterOptions = {
    val dateType = SchemaHelper.getTimeTypeFromString(cubeModel.writer.dateType.getOrElse(DefaultTimeStampTypeString))

    CubeWriterOptions(cubeModel.writer.outputs, dateType, cubeModel.writer.saveMode)
  }
}
