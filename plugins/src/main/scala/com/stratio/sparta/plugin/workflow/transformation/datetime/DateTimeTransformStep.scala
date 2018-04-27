/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.datetime

import java.io.{Serializable => JSerializable}
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.{Date, Locale, TimeZone}

import com.github.nscala_time.time.Imports.DateTime
import com.stratio.sparta.plugin.enumerations.DateFormatEnum._
import com.stratio.sparta.plugin.enumerations.FieldsPreservationPolicy._
import com.stratio.sparta.plugin.enumerations.{DateFormatEnum, FieldsPreservationPolicy}
import com.stratio.sparta.plugin.helper.SchemaHelper.parserInputSchema
import com.stratio.sparta.plugin.workflow.transformation.datetime.DateTimeTransformStep._
import com.stratio.sparta.plugin.workflow.transformation.datetime.models.{DateTimeItem, DateTimeItemModel}
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper
import com.stratio.sparta.sdk.models.DiscardCondition
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.utils.AggregationTimeUtils._
import com.stratio.sparta.sdk.workflow.step._
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.collection.mutable.ArrayBuffer
import scala.util.Try

abstract class DateTimeTransformStep[Underlying[Row]](
                                                       name: String,
                                                       outputOptions: OutputOptions,
                                                       transformationStepsManagement: TransformationStepManagement,
                                                       ssc: Option[StreamingContext],
                                                       xDSession: XDSession,
                                                       properties: Map[String, JSerializable]
                                                     )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  lazy val fieldsModel: Seq[DateTimeItemModel] = {
    if (properties.getString("fieldsDatetime", None).isDefined) {
      implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
      read[Seq[DateTimeItemModel]](
        s"""${properties.get("fieldsDatetime").fold("[]") { values => values.toString }}""""
      )
    }
    else Seq.empty
  }
  lazy val itemsDatetime: Seq[DateTimeItem] = fieldsModel.map(transformToDateTimeItem)
  lazy val validEmptyTimeGranularity: Set[String] = Set("millisecond", "second",
    "minute", "hour", "day", "week", "month", "year")

  //scalastyle:off
  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the step name $name is not valid")

    if (Try(fieldsModel.nonEmpty).isFailure) {
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the input fields are not valid")
    }

    //If contains schemas, validate if it can be parsed
    if (inputsModel.inputSchemas.nonEmpty) {
      inputsModel.inputSchemas.foreach { input =>
        if (parserInputSchema(input.schema).isFailure)
          validation = ErrorValidations(
            valid = false,
            messages = validation.messages :+ s"$name: the input schema from step ${input.stepName} is not valid")
      }

      inputsModel.inputSchemas.filterNot(is => SdkSchemaHelper.isCorrectTableName(is.stepName)).foreach { is =>
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ s"$name: the input table name ${is.stepName} is not valid")
      }
    }

    if (fieldsModel.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: item fields cannot be empty"
      )

    if (fieldsModel.nonEmpty)
      itemsDatetime.foreach(dateTimeItem => {

        if (dateTimeItem.formatFrom.equals(DateFormatEnum.AUTOGENERATED) &&
          !dateTimeItem.preservationPolicy.equals(FieldsPreservationPolicy.APPEND))
          validation = ErrorValidations(
            valid = false,
            messages = validation.messages :+ s"$name: auto-generated format only valid with 'Appending extracted'" +
              s" preservation policy"
          )

        if (dateTimeItem.formatFrom.equals(DateFormatEnum.AUTOGENERATED) &&
          dateTimeItem.preservationPolicy.equals(FieldsPreservationPolicy.APPEND)
          && dateTimeItem.outputFieldType.equals("long")
          && !dateTimeItem.outputFormatFrom.equals(DateFormatEnum.DEFAULT))
          validation = ErrorValidations(
            valid = false,
            messages = validation.messages :+ s"$name: auto-generated format with Long output type only valid with Default" +
              s" timestamp as output format"
          )

        if (dateTimeItem.formatFrom.equals(DateFormatEnum.STANDARD) ||
          dateTimeItem.formatFrom.equals(DateFormatEnum.USER)) {

          if (dateTimeItem.inputField.isEmpty)
            validation = ErrorValidations(
              valid = false,
              messages = validation.messages :+ s"$name: Input field cannot be empty"
            )

          if (dateTimeItem.formatFrom.equals(DateFormatEnum.STANDARD) && dateTimeItem.standardFormat.isEmpty)
            validation = ErrorValidations(
              valid = false,
              messages = validation.messages :+ s"$name: Standard format cannot be empty"
            )

          if (dateTimeItem.formatFrom.equals(DateFormatEnum.USER) && dateTimeItem.userFormat.isEmpty)
            validation = ErrorValidations(
              valid = false,
              messages = validation.messages :+ s"$name: User format cannot be empty"
            )

          if (dateTimeItem.outputFormatFrom.equals(DateFormatEnum.STANDARD) && !dateTimeItem.outputFieldType.equals("string"))
            validation = ErrorValidations(
              valid = false,
              messages = validation.messages :+ s"$name: Standard output format field only valid with output field type " +
                s"set to String type"
            )

          if (dateTimeItem.outputFormatFrom.equals(DateFormatEnum.USER) && !dateTimeItem.outputFieldType.equals("string"))
            validation = ErrorValidations(
              valid = false,
              messages = validation.messages :+ s"$name: User-defined output format only valid with output field type " +
                s"needs to be String"
            )
        }

        if (dateTimeItem.outputFieldName.isEmpty)
          validation = ErrorValidations(
            valid = false,
            messages = validation.messages :+ s"$name: Output field name cannot be empty"
          )
      }
      )
    validation
  }

  def transformFunc(inputData: Map[String, DistributedMonad[Underlying]]): DistributedMonad[Underlying] =
    applyHeadTransform(inputData) { (inputSchema, inputStream) =>
      inputStream.flatMap(data => parse(data))
    }

  def applyOutputFormatToDate(date: Any)(implicit dateTimeItem: DateTimeItem): Any = {
    (date, dateTimeItem.outputFormatFrom) match {
      case (d: DateTime, USER) => setDateFormat(d.toDate, dateTimeItem.outputUserFormat.get)
      case (d: DateTime, STANDARD) => setDateFormat(d.toDate, dateTimeItem.outputStandardFormat.get)
      case (d: Long, USER) => setDateFormat(new Date(d), dateTimeItem.outputUserFormat.get)
      case (d: Long, STANDARD) => setDateFormat(new Date(d), dateTimeItem.outputStandardFormat.get)
      case _ => date
    }
  }

  def parse(row: Row): Seq[Row] =
    returnSeqDataFromRow {
      val inputSchema = row.schema
      val outputSchema = getNewOutputSchema(inputSchema)
      val mapValues = itemsDatetime.map {
        implicit itemDateTime: DateTimeItem =>
          val outputFieldStruct = retrieveStructType(itemDateTime)
          itemDateTime.formatFrom match {
            case AUTOGENERATED =>
              (itemDateTime.outputFieldName, castingToOutputSchema(outputFieldStruct, applyOutputFormatToDate(applyGranularity(new DateTime()))))
            case _ =>
              val inputValue = Option(row.get(inputSchema.fieldIndex(itemDateTime.inputField.get)))
              inputValue match {
                case Some(value) =>
                  Try {
                    (itemDateTime.outputFieldName, castingToOutputSchema(outputFieldStruct, applyOutputFormatToDate(applyGranularity(parseDate(value)))))
                  }.getOrElse(returnWhenFieldError(new Exception(s"Impossible to parse outputField: ${itemDateTime.outputFieldName}")))
                case None =>
                  returnWhenFieldError(new Exception(
                    s"Impossible to parse because the field ${outputFieldStruct.name} value is empty"))
              }
          }
      }.toMap


      val newValues = outputSchema.map { outputField =>
        mapValues.get(outputField.name) match {
          case Some(valueParsed) => valueParsed
          case _ =>
            Try(row.get(inputSchema.fieldIndex(outputField.name))).getOrElse(returnWhenFieldError(
              new Exception(s"Impossible to parse outputField: $outputField in the schema")))
        }
      }
      new GenericRowWithSchema(newValues.toArray, outputSchema)
    }

  def parseDate(inputValue: Any)(implicit dateTimeItem: DateTimeItem): DateTime = {
    dateTimeItem.formatFrom match {
      case STANDARD =>
        dateTimeItem.standardFormat match {
          case Some("unix") =>
            new DateTime(inputValue.toString.toLong * 1000L)
          case Some("unixMillis") =>
            new DateTime(inputValue.toString.toLong)
          case Some("hive") =>
            new DateTime(getDateFromFormat(inputValue.toString))
          case Some(format) =>
            val formats = formatMethods
            if (formats.contains(format))
              formats(format).invoke(None).asInstanceOf[DateTimeFormatter].parseDateTime(inputValue.toString)
            else throw new Exception(s"The specified date format is not valid")
          case None =>
            throw new Exception(s"The specified date format is not valid")
        }
      case USER =>
        dateTimeItem.userFormat match {
          case Some(format) => new DateTime(getDateFromFormat(inputValue.toString, format, dateTimeItem.localeTime))
          case None => throw new Exception(s"The user date format is not valid")
        }
      case _ =>
        throw new Exception(s"The format is not supported")
    }
  }

  //scalastyle:on

  def applyGranularity(inputValue: DateTime)(implicit dateTimeItem: DateTimeItem): Any =
    dateTimeItem.granularity match {
      case Some(granularity) => truncateDate(inputValue, granularity)
      case None => inputValue
    }

  def getDateFromFormat(inputDate: String,
                        format: String = "yyyy-MM-dd HH:mm:ss",
                        locale: Locale = Locale.ENGLISH): Date = {
    val sdf = new SimpleDateFormat(format, locale)
    if (!format.contains("T") && !format.contains("z") && !format.contains("Z"))
      sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
    sdf.parse(inputDate)
  }

  def setDateFormat(inputDate: Date,
                    format: String = "yyyy-MM-dd HH:mm:ss",
                    locale: Locale = Locale.ENGLISH): String = {
    val writeFormat = new SimpleDateFormat(format, locale)
    val out = writeFormat.format(inputDate)
    out
  }

  private def retrieveStructType(dateTimeItem: DateTimeItem): StructField =
    StructField(
      name = dateTimeItem.outputFieldName,
      dataType = SparkTypes.get(dateTimeItem.outputFieldType) match {
        case Some(sparkType) => sparkType
        case None => schemaFromString(dateTimeItem.outputFieldType)
      },
      nullable = dateTimeItem.nullable.getOrElse(true)
    )

  def getNewOutputSchema(inputSchema: StructType): StructType = {
    val newOutputSchema = ArrayBuffer[StructField]() ++ inputSchema
    itemsDatetime.foreach {
      itemDateTime: DateTimeItem =>
        val providedSchema: StructField = retrieveStructType(itemDateTime)
        itemDateTime.preservationPolicy match {
          case APPEND =>
            newOutputSchema += providedSchema
          case REPLACE =>
            val index = newOutputSchema.indexWhere(oldOut => oldOut.name.equals(itemDateTime.inputField.get))
            if (index != -1)
              newOutputSchema.update(index, providedSchema)
            else
              returnWhenFieldError(throw new
                  Exception(s"The field specified as input does not exists in the current Row"))
          case JUST_EXTRACTED => Nil
        }
    }
    StructType(newOutputSchema)
  }


  def getGranularity(inputItem: DateTimeItemModel): Option[String] =
    (inputItem.granularityNumber.notBlank, inputItem.granularityTime.notBlank) match {
      case (Some(number), Some(time)) if !validEmptyTimeGranularity.contains(time) && number.trim.nonEmpty =>
        Some(number.concat(time))
      case (_, Some(time)) if validEmptyTimeGranularity.contains(time) => Option(time)
      case _ => None
    }

  def transformToDateTimeItem(dateTimeItemModel: DateTimeItemModel): DateTimeItem = {
    val formatFrom: DateFormat =
      Try(DateFormatEnum.withName(dateTimeItemModel.formatFrom.toUpperCase))
        .getOrElse(AUTOGENERATED)

    val preservation: FieldsPreservationPolicy =
      Try(FieldsPreservationPolicy.withName(dateTimeItemModel.fieldsPreservationPolicy.toUpperCase))
        .getOrElse(if (formatFrom.equals(AUTOGENERATED)) APPEND else REPLACE)

    val outputFormatFrom: DateFormat =
      Try(DateFormatEnum.withName(dateTimeItemModel.outputFormatFrom.toUpperCase))
        .getOrElse(DEFAULT)

    val granularity = getGranularity(dateTimeItemModel)

    val locale = dateTimeItemModel.localeTime.fold(Locale.ENGLISH) { x =>
      localeMapping.getOrElse(x, Locale.ENGLISH)
    }

    val item = DateTimeItem(dateTimeItemModel.inputField.notBlank,
      formatFrom,
      dateTimeItemModel.userFormat.notBlank,
      dateTimeItemModel.standardFormat.notBlank,
      locale,
      granularity,
      preservation,
      dateTimeItemModel.outputFieldName,
      dateTimeItemModel.outputFieldType,
      dateTimeItemModel.nullable,
      outputFormatFrom,
      dateTimeItemModel.outputUserFormat.notBlank,
      dateTimeItemModel.outputStandardFormat.notBlank
    )
    item
  }
}

object DateTimeTransformStep {

  val formatMethods: Map[String, Method] = classOf[ISODateTimeFormat].getMethods.toSeq.map(x => (x.getName, x)).toMap

  val localeMapping: Map[String, Locale] = Map(
    "ENGLISH" -> Locale.ENGLISH,
    "SPANISH" -> new Locale("es", "es"),
    "FRENCH" -> Locale.FRENCH,
    "GERMAN" -> Locale.GERMAN,
    "ITALIAN" -> Locale.ITALIAN,
    "JAPANESE" -> Locale.JAPANESE,
    "KOREAN" -> Locale.KOREAN,
    "CHINESE" -> Locale.CHINESE,
    "GREEK" -> new Locale("el", "el"),
    "PORTUGUESE" -> new Locale("pt", "pt"),
    "RUSSIAN" -> new Locale("ru", "ru")
  )

}

