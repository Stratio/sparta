/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.workflow.transformation.cube.model._
import com.stratio.sparta.plugin.workflow.transformation.cube.sdk._
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.utils.{CastingUtils, ClasspathUtils}
import com.stratio.sparta.sdk.workflow.enumerators.WhenError.WhenError
import com.stratio.sparta.sdk.workflow.enumerators.WhenFieldError.WhenFieldError
import com.stratio.sparta.sdk.workflow.enumerators.WhenRowError.WhenRowError
import com.stratio.sparta.sdk.workflow.step._
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{DataType, DoubleType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}

class CubeTransformStepStreaming(
                                  name: String,
                                  outputOptions: OutputOptions,
                                  transformationStepsManagement: TransformationStepManagement,
                                  override val ssc: Option[StreamingContext],
                                  xDSession: XDSession,
                                  properties: Map[String, JSerializable]
                                )
  extends TransformStep[DStream](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
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

  lazy val avgOperators: Seq[String] = cubeModel.operators.filter(operator => operator.classType == "Avg").map(_.name)

  def transformFunction(inputSchema: String, inputStream: DistributedMonad[DStream]): DistributedMonad[DStream] = {
    val warnMessage = s"Discarding stream in Cube: $name"
    val cubeInputStream = returnDStreamFromTry(s"Error creating initial stream in Cube: $name", Option(warnMessage)) {
      Try(cube.createDStream(inputStream.ds))
    }
    val cubeExecuted = returnDStreamFromTry(s"Error executing Cube: $name", Option(warnMessage)) {
      Try(cube.execute(cubeInputStream))
    }

    returnDStreamFromTry(s"Error creating output stream as row format in Cube: $name", Option(warnMessage)) {
      Try(cubeExecuted.flatMap { case (dimensionValues, measures) => toRow(dimensionValues, measures) })
    }
  }


  override def transform(inputData: Map[String, DistributedMonad[DStream]]): DistributedMonad[DStream] =
    applyHeadTransform(inputData)(transformFunction)


  /* PRIVATE METHODS */

  private[cube] def createCube: Cube = {
    Try {
      Cube(
        createDimensions,
        createOperators,
        whenRowErrorDo,
        whenFieldErrorDo,
        partitions,
        timeoutKey,
        createWaterMarkPolicy
      )
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
          classOf[WhenRowError],
          classOf[WhenFieldError],
          classOf[Option[String]]
        ).newInstance(operatorModel.name, whenRowErrorDo, whenFieldErrorDo, operatorModel.inputField).asInstanceOf[Operator],
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
    returnRowFromTry(s"Error generating row from dimensions and measures in CubeStep." +
      s" Dimensions: $dimensionValues and Measures: $measures") {
      Try {
        val measuresValues = measures.values.map { case (measureName, measureValue) =>
          val schema = if(avgOperators.contains(measureName))
            operatorsSchema(measureName).copy(dataType = DoubleType)
          else operatorsSchema(measureName)
          (
            schema,
            measureValue match {
              case Some(value) =>
                if(avgOperators.contains(measureName))
                  CastingUtils.castingToSchemaType(schema.dataType, value.asInstanceOf[Map[String, Double]]("mean"))
                else CastingUtils.castingToSchemaType(schema.dataType, value)
              case None =>
                returnWhenFieldError(new Exception(s"Wrong value in measure $measureName"))
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

