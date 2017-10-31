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

package com.stratio.sparta.plugin.workflow.transformation.cube

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.workflow.transformation.cube.model._
import com.stratio.sparta.plugin.workflow.transformation.cube.sdk._
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.utils.{CastingUtils, ClasspathUtils}
import com.stratio.sparta.sdk.workflow.enumerators.WhenError.WhenError
import com.stratio.sparta.sdk.workflow.step.{ErrorCheckingDStream, ErrorCheckingOption, OutputOptions, TransformStep}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{DataType, LongType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}

class CubeTransformStep(name: String,
                        outputOptions: OutputOptions,
                        override val ssc: StreamingContext,
                        xDSession: XDSession,
                        properties: Map[String, JSerializable])
  extends TransformStep(name, outputOptions, ssc, xDSession, properties)
    with SLF4JLogging with ErrorCheckingOption with ErrorCheckingDStream {

  lazy val partitions: Option[Int] = properties.getInt("partitions", None)

  lazy val timeoutKey: Option[Int] = properties.getInt("timeoutKey", None)

  lazy val waterMarkField: Option[String] = properties.getString("waterMark", None).notBlank

  lazy val availability: Option[String] = properties.getString("availability", None).notBlank

  lazy val timeDataType: DataType = SparkTypes.get("timestamp") match {
      case Some(sparkType) => sparkType
      case None => schemaFromString("timestamp")
    }

  lazy val cubeModel: CubeModel = {
    Try {
      implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()

      read[CubeModel](
        s"""{
           |"dimensions":${properties.getString("dimensions")},
           |"operators":${properties.getString("operators")}
           |}"""".stripMargin)
    } match {
      case Success(model) => model
      case Failure(e) => throw new Exception("Impossible to get cube model from properties", e)
    }
  }

  lazy val cube: Cube = createCube

  lazy val operatorsSchema: Map[String, StructField] = {
    cubeModel.operators.map { operatorModel =>
      operatorModel.name -> StructField(
        name = operatorModel.name,
        dataType = operatorModel.`type`.notBlank match {
          case Some(userDefinedType) =>
            SparkTypes.get(userDefinedType) match {
              case Some(sparkType) => sparkType
              case None => schemaFromString(userDefinedType)
            }
          case None =>
            cube.operators.find(op => op.name == operatorModel.name)
              .getOrElse(throw new Exception(s"Error getting default type in operator: ${operatorModel.name}"))
              .defaultOutputType
        },
        nullable = operatorModel.nullable.getOrElse(true)
      )
    }.toMap
  }

  def transformFunction(inputSchema: String, inputStream: DStream[Row]): DStream[Row] = {
    val warnMessage = s"Discarding stream in Cube: $name"
    val cubeInputStream = returnDStreamFromTry(s"Error creating initial stream in Cube: $name", Option(warnMessage)) {
      Try(cube.createDStream(inputStream))
    }
    val cubeExecuted = returnDStreamFromTry(s"Error executing Cube: $name", Option(warnMessage)) {
      Try(cube.execute(cubeInputStream))
    }

    returnDStreamFromTry(s"Error creating output stream as row format in Cube: $name", Option(warnMessage)) {
      Try(cubeExecuted.flatMap { case (dimensionValues, measures) => toRow(dimensionValues, measures) })
    }
  }

  override def transform(inputData: Map[String, DStream[Row]]): DStream[Row] =
    applyHeadTransform(inputData)(transformFunction)


  /* PRIVATE METHODS */

  private[cube] def createCube: Cube = {
    Try {
      Cube(createDimensions, createOperators, whenErrorDo, partitions, timeoutKey, createWaterMarkPolicy)
    } match {
      case Success(cubeCreated) => cubeCreated
      case Failure(e) => throw new Exception("Impossible to create cube", e)
    }
  }

  private[cube] def createDimensions: Seq[Dimension] =
    cubeModel.dimensions.map(dimensionModel => Dimension(dimensionModel.name))

  private[cube] def createOperators: Seq[Operator] = {
    val classpathUtils = new ClasspathUtils
    val operatorClasses = classpathUtils.classesInClasspath(
      classes = Seq(classOf[Operator]),
      packagePath = "com.stratio.sparta.plugin",
      printClasspath = false
    )
    cubeModel.operators.map { operatorModel =>
      classpathUtils.tryToInstantiate[Operator](operatorModel.classType + Operator.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[WhenError],
          classOf[Option[String]]
        ).newInstance(operatorModel.name, whenErrorDo, operatorModel.inputField).asInstanceOf[Operator],
        operatorClasses
      )
    }
  }

  private[cube] def createWaterMarkPolicy: Option[WaterMarkPolicy] =
    (waterMarkField, availability) match {
      case (Some(timeDimensionConf), Some(timeAvailabilityConf)) =>
        Option(WaterMarkPolicy(timeDimensionConf, timeAvailabilityConf))
      case _ => None
    }

  private[cube] def toRow(dimensionValues: DimensionValues, measures: MeasuresValues): Option[Row] = {
    returnFromTry(s"Error generating row from dimensions and measures in CubeStep." +
      s" Dimensions: $dimensionValues and Measures: $measures") {
      Try {
        val measuresValues = measures.values.map { case (measureName, measureValue) =>
          val schema = operatorsSchema(measureName)
          (
            schema,
            measureValue match {
              case Some(value) =>
                CastingUtils.castingToSchemaType(schema.dataType, value)
              case None =>
                returnWhenError(new Exception(s"Wrong value in measure $measureName"))
            }
          )
        }

        dimensionValues.waterMark match {
          case None =>
            new GenericRowWithSchema(
              (dimensionValues.values.map(_.value) ++ measuresValues.values).toArray,
              StructType(dimensionValues.values.map(_.schema) ++ measuresValues.keys)
            )
          case Some(waterMark) =>
            val dimensionTime = Seq(CastingUtils.castingToSchemaType(timeDataType, waterMark.value))
            val dimensionTimeSchema = Seq(StructField(waterMark.dimension, timeDataType))
            val dimensionsWithoutTime = dimensionValues.values.filter(dimensionValue =>
              dimensionValue.dimension.name != waterMark.dimension
            )
            new GenericRowWithSchema(
              (dimensionsWithoutTime.map(_.value) ++ dimensionTime ++ measuresValues.values).toArray,
              StructType(dimensionsWithoutTime.map(_.schema) ++ dimensionTimeSchema ++ measuresValues.keys)
            )
        }
      }
    }
  }
}

