/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.helper

import akka.event.slf4j.SLF4JLogging
import com.databricks.spark.avro.SchemaConverters
import com.stratio.sparta.plugin.enumerations.FieldsPreservationPolicy._
import com.stratio.sparta.plugin.enumerations.SchemaInputMode._
import com.stratio.sparta.plugin.enumerations.{FieldsPreservationPolicy, SchemaInputMode}
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.PropertySchemasInput
import org.apache.avro.Schema
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.catalyst.parser.LegacyTypeStringParser
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.json.RowJsonHelper
import org.apache.spark.sql.json.RowJsonHelper.extractSchemaFromJson
import org.apache.spark.sql.types.{DataType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row}

import scala.util.{Failure, Success, Try}

object SchemaHelper extends SLF4JLogging {

  def getJsonSparkSchema(
                          useRowSchema: Boolean,
                          schemaInputMode: SchemaInputMode.Value,
                          schemaProvided: Option[String],
                          jsonOptions: Map[String, String] = Map.empty[String, String]
                        ): Option[StructType] =
    schemaProvided.flatMap { schemaStr =>
      if (useRowSchema) None
      else {
        Try {
          schemaInputMode match {
            case EXAMPLE => extractSchemaFromJson(schemaStr, jsonOptions)
            case SPARKFORMAT => getSparkSchemaFromString(schemaStr)
            case _ => throw new Exception("Invalid input mode in json schema extractor")
          }
        }.recoverWith {
          case e =>
            log.warn(s"Impossible to parse the schema: $schemaStr, the system infer it from each event." +
              s" ${e.getLocalizedMessage}")
            Failure(e)
        }.toOption
      }
    }

  def getAvroSparkSchema(useRowSchema: Boolean, schemaProvided: Option[String]): Option[StructType] =
    schemaProvided flatMap { schemaStr =>
      if (useRowSchema) None
      else Try(getSparkSchemaFromAvroSchema(getAvroSchemaFromString(schemaStr))).recoverWith { case e =>
        log.warn(s"Impossible to parse the schema: $schemaStr, the system infer it from each event" +
          s" ${e.getLocalizedMessage}")
        Failure(e)
      }.toOption
    }

  def getAvroSchema(useRowSchema: Boolean, schemaProvided: Option[String]): Option[Schema] =
    schemaProvided flatMap { schemaStr =>
      if (useRowSchema) None
      else Try(getAvroSchemaFromString(schemaStr)).recoverWith { case e =>
        log.warn(s"Impossible to parse the schema: $schemaStr, the system infer it from each event")
        Failure(e)
      }.toOption
    }

  def getAvroSchemaFromString(schemaStr: String): Schema = {
    val parser = new Schema.Parser()

    Try(parser.parse(schemaStr)) match {
      case Success(schema) =>
        schema
      case Failure(e) =>
        log.warn(s"Error creating Avro schema from string: $schemaStr. ${e.getLocalizedMessage}")
        throw e
    }
  }

  def getSparkSchemaFromAvroSchema(avroSchema: Schema): StructType =
    SchemaConverters.toSqlType(avroSchema).dataType match {
      case t: StructType => t
      case _ => throw new Exception(
        s"Avro schema cannot be converted to a Spark SQL StructType: ${avroSchema.toString(true)}")
    }

  def getSparkSchemaFromString(schemaStr: String): StructType = {
    Try {
      DataType.fromJson(schemaStr)
    } match {
      case Success(jsonSchema) =>
        jsonSchema.asInstanceOf[StructType]
      case Failure(jsonException) =>
        Try(LegacyTypeStringParser.parse(schemaStr)) match {
          case Success(stringSchema) =>
            stringSchema.asInstanceOf[StructType]
          case Failure(stringException) =>
            throw new Exception(
              s"StringEx: ${stringException.getLocalizedMessage}.\nJsonEx: ${jsonException.getLocalizedMessage}")
        }
    }
  }


  def parserInputSchema(schema: String): Try[StructType] =
    Try {
      Try(getSparkSchemaFromString(schema)) match {
        case Success(structType) =>
          structType
        case Failure(f) =>
          log.warn(s"Error parsing input schema $schema in SparkSchemaFromString. ${f.getLocalizedMessage}")
          Try(RowJsonHelper.extractSchemaFromJson(schema, Map())) match {
            case Success(structType) =>
              structType
            case Failure(e) =>
              log.warn(s"Error parsing input schema $schema in SchemaFromJson. ${e.getLocalizedMessage}")
              throw new Exception(
                s"Error parsing input schema.\nSparkSchemaFromString: ${f.getLocalizedMessage}." +
                  s"\nSchemaFromJson: ${e.getLocalizedMessage}")
          }
      }
    }

  def getNewOutputSchema(
                          inputSchema: StructType,
                          preservationPolicy: FieldsPreservationPolicy.Value,
                          providedSchema: Seq[StructField],
                          inputField: String
                        ): StructType = {
    preservationPolicy match {
      case APPEND =>
        StructType(inputSchema.fields ++ providedSchema)
      case REPLACE =>
        val inputFieldIdx = inputSchema.indexWhere(_.name == inputField)
        assert(inputFieldIdx > -1, s"$inputField should be a field in the input row")
        val (leftInputFields, rightInputFields) = inputSchema.fields.splitAt(inputFieldIdx)
        val outputFields = leftInputFields ++ providedSchema ++ rightInputFields.tail

        StructType(outputFields)
      case _ =>
        StructType(providedSchema)
    }
  }

  def getNewOutputSchema(
                          sourceSchema: Option[StructType],
                          preservationPolicy: FieldsPreservationPolicy.Value,
                          providedSchema: Seq[StructField],
                          inputField: String
                        ): Option[StructType] = {
    preservationPolicy match {
      case APPEND =>
        sourceSchema.map(sc => StructType(sc.fields ++ providedSchema))
      case REPLACE =>
        sourceSchema.map { sc =>
          val inputFieldIdx = sc.indexWhere(_.name == inputField)
          assert(inputFieldIdx > -1, s"$inputField should be a field in the input row")
          val (leftInputFields, rightInputFields) = sc.fields.splitAt(inputFieldIdx)
          val outputFields = leftInputFields ++ providedSchema ++ rightInputFields.tail

          StructType(outputFields)
        }
      case _ =>
        Option(StructType(providedSchema))
    }
  }


  def getNewOutputSchema(
                          sourceSchema: Option[StructType],
                          extractedSchema: StructType,
                          preservationPolicy: FieldsPreservationPolicy.Value,
                          inputField: String
                        ): Option[StructType] =
    preservationPolicy match {
      case APPEND =>
        sourceSchema.map(sc => StructType(sc.fields ++ extractedSchema.fields))
      case REPLACE =>
        sourceSchema.map { sc =>
          val inputFieldIdx = sc.indexWhere(_.name == inputField)
          assert(inputFieldIdx > -1, s"$inputField should be a field in the input row")
          val (leftInputFields, rightInputFields) = sc.fields.splitAt(inputFieldIdx)
          val outputFields = leftInputFields ++ extractedSchema.fields ++ rightInputFields.tail

          StructType(outputFields)
        }
      case _ =>
        Option(extractedSchema)
    }

  def updateRow(
                 source: Row,
                 extracted: Row,
                 inputFieldIdx: Int,
                 preservationPolicy: FieldsPreservationPolicy.Value
               ): Row =
    preservationPolicy match {
      case APPEND =>
        val values = (source.toSeq ++ extracted.toSeq).toArray
        val schema = StructType(source.schema ++ extracted.schema)
        new GenericRowWithSchema(values, schema)

      case REPLACE =>
        val (leftInputFields, rightInputFields) = source.schema.fields.splitAt(inputFieldIdx)
        val (leftValues, rightValues) = source.toSeq.toArray.splitAt(inputFieldIdx)

        val outputFields = leftInputFields ++ extracted.schema.fields ++ rightInputFields.tail
        val outputValues = leftValues ++ extracted.toSeq.toArray[Any] ++ rightValues.tail

        new GenericRowWithSchema(outputValues, StructType(outputFields))

      case _ =>
        extracted
    }

  def getSchemaFromSessionOrModelOrRdd(
                                        xDSession: XDSession,
                                        tableName: String,
                                        inputsModel: PropertySchemasInput,
                                        rdd: RDD[Row]
                                      ): Option[StructType] =
    getSchemaFromSessionOrModel(xDSession, tableName, inputsModel).orElse(getSchemaFromRdd(rdd))

  def getSchemaFromSessionOrModel(
                                   xDSession: XDSession,
                                   tableName: String,
                                   inputsModel: PropertySchemasInput
                                 ): Option[StructType] =
    SdkSchemaHelper.getSchemaFromSession(xDSession, tableName).orElse {
      inputsModel.inputSchemas.filter(is => is.stepName == tableName) match {
        case Nil => None
        case x :: Nil => parserInputSchema(x.schema).toOption
      }
    }

  def getSchemaFromSessionOrRdd(
                                 xDSession: XDSession,
                                 tableName: String,
                                 rdd: RDD[Row]
                               ): Option[StructType] =
    SdkSchemaHelper.getSchemaFromSession(xDSession, tableName).orElse(getSchemaFromRdd(rdd))

  def getSchemaFromRdd(rdd: RDD[Row]): Option[StructType] = if (!rdd.isEmpty()) Option(rdd.first().schema) else None

  def createOrReplaceTemporalView(
                                   xDSession: XDSession,
                                   rdd: RDD[Row],
                                   tableName: String,
                                   schema: Option[StructType],
                                   registerWithEmptySchema: Boolean
                                 ): Boolean =
    schema match {
      case Some(s) =>
        log.debug(s"Registering temporal table in Spark with name: $tableName")
        xDSession.createDataFrame(rdd, s).createOrReplaceTempView(tableName)
        true
      case None =>
        if (registerWithEmptySchema) {
          log.debug(s"Registering empty temporal table with name: $tableName")
          xDSession.createDataFrame(rdd, StructType(Nil)).createOrReplaceTempView(tableName)
          true
        } else false
    }

  def createOrReplaceTemporalViewDf(
                                     xDSession: XDSession,
                                     rdd: RDD[Row],
                                     tableName: String,
                                     schema: Option[StructType]
                                   ): Option[DataFrame] =
    schema.map { s =>
      log.debug(s"Registering temporal table in Spark with name: $tableName")
      val df = xDSession.createDataFrame(rdd, s)
      df.createOrReplaceTempView(tableName)
      df
    }

  /**
    * Validate inputSchema names with names of input steps, also validate the input schemas
    *
    * @param inputSteps
    */
  def validateSchemas(step: String, inputsModel: PropertySchemasInput, inputSteps: Seq[String]): Unit = {
    if (inputsModel.inputSchemas.nonEmpty) {
      require(!inputsModel.inputSchemas.exists(input => parserInputSchema(input.schema).isFailure),
        s"$step input schemas contains errors")
    }
  }
}
