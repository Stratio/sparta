/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.common.rest

import java.io.{Serializable => JSerializable}

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.plugin.common.rest.BodyFormat.BodyFormat
import com.stratio.sparta.plugin.enumerations.FieldsPreservationPolicy
import com.stratio.sparta.plugin.helper.ErrorValidationsHelper.HasError
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import scala.collection.immutable
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.Try


object RestConfig{

  // Incoming properties from frontend
  private[rest] val Url = "url"
  private[rest] val HttpMethod = "httpMethod"
  private[rest] val HttpBody = "HTTP_Body"
  private[rest] val HttpBodyFormat = "HTTP_Body_Format"
  private[rest] val HttpOutputField = "httpOutputField"
  private[rest] val RequestTimeout = "requestTimeout"
  private[rest] val KeyAkkaOptionsGroupProperty = "akkaHttpOptions"
  private[rest] val KeyAkkaOptionsKeyProperty= "akkaHttpOptionsKey"
  private[rest] val KeyAkkaOptionsValueProperty = "akkaHttpOptionsValue"
  private[rest] val KeyWhitelistProperty = "statusCodeWhiteList"
  private[rest] val KeyHeaderOptionsGroupProperty = "HTTP_Header"
  private[rest] val KeyHeaderOptionsKeyProperty= "headerOptionsKey"
  private[rest] val KeyHeaderOptionsValueProperty = "headerOptionsValue"
  private[rest] val DefaultHttpOutputField = "http"
  private[rest] val FieldsPreservationProperty = "fieldsPreservationPolicy"

  val urlRegex = "\\b(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]".r



  def validateProperties(properties: Map[String, JSerializable]): Seq[(HasError,String)] = {
    val errorsWhitelist = returnErrorCodesWhitelist(properties)
    val errorAkkaProperty = returnInvalidAkkaProperties(properties)

    Seq[(HasError,String)](
      errorsWhitelist.nonEmpty ->
        s"The following status codes were discarded: ${errorsWhitelist.mkString(",")}",
      errorAkkaProperty.nonEmpty ->
        s"The following properties do not start with akka.* or have been left empty: ${errorAkkaProperty.mkString(",")} "
    )
  }

  def returnInvalidAkkaProperties(properties: Map[String, JSerializable]) : Seq[String] =
    (for {
      (k,v) <- properties.getOptionsList(KeyAkkaOptionsGroupProperty,
        KeyAkkaOptionsKeyProperty, KeyAkkaOptionsValueProperty)
        .filter{ case (key,value) => key.nonEmpty && ! key.startsWith("akka") || value.trim.nonEmpty }
    } yield s"$k = $v").toSeq


  def returnErrorCodesWhitelist(properties: Map[String, JSerializable]): Seq[String] =
    properties.getString(KeyWhitelistProperty, None).notBlank
      .fold(Seq.empty[String]) { stringFromProperties =>
        val (ranges, statuses) = stringFromProperties.split(",").partition(_.contains('-'))
        statuses.filter(optIntFromString(_).isEmpty) ++
          ranges.filter { range =>
            val splitted = range.split('-')
            splitted.length != 2 ||
              (for {
                start <- optIntFromString(splitted(0))
                end <- optIntFromString(splitted(1))
              } yield start >= end).fold(true){ v => v }
          }
      }


  def apply(properties: Map[String, JSerializable]): RestConfig = {

    val url: Option[String] = properties.getString(Url, None).notBlank

    val httpOutputFieldName: String = properties.getString(HttpOutputField, DefaultHttpOutputField)

    val requestTimeout: Try[FiniteDuration] = Try {
      val fakeKey = "k"
      val javaDuration = ConfigFactory.empty().withValue(
        fakeKey,
        ConfigValueFactory.fromAnyRef(properties.getString(RequestTimeout))
      ).getDuration(fakeKey)
      Duration.fromNanos(javaDuration.toNanos)
    }

    val preservationPolicy: FieldsPreservationPolicy.Value = FieldsPreservationPolicy.withName(
      properties.getString(FieldsPreservationProperty, "APPEND").toUpperCase)

    val akkaProperties: Map[String, String] =
      properties.getOptionsList(KeyAkkaOptionsGroupProperty,
        KeyAkkaOptionsKeyProperty, KeyAkkaOptionsValueProperty)
        .filter{case (k,_) => k.nonEmpty}

    val headerProperties: Map[String, String] =
      properties.getOptionsList(KeyHeaderOptionsGroupProperty,
        KeyHeaderOptionsKeyProperty, KeyHeaderOptionsValueProperty)
        .filter{case (k,_) => k.nonEmpty}


    val bodyFormat: Option[BodyFormat] = properties.getString(HttpBodyFormat, None)
      .flatMap(format => Try(BodyFormat.withName(format.toUpperCase)).toOption)

    val bodyString: Option[String] = properties.getString(HttpBody, None).notBlank


    val methodHttp: HttpMethod =
      properties.getString(HttpMethod, "get").toLowerCase match {
        case "post" => HttpMethods.POST
        case "put" => HttpMethods.PUT
        case "delete" => HttpMethods.DELETE
        case "get" | _ => HttpMethods.GET
      }

    val headersHttp: immutable.Seq[HttpHeader] = {
      val iterableHeaders = for {
        (headerName, headerValue) <- headerProperties
      } yield RawHeader(headerName, headerValue)
      immutable.Seq(iterableHeaders.toSeq:_*)
    }

    val statusCodeList: Seq[StatusCode] =
      properties.getString(KeyWhitelistProperty, None).notBlank
        .fold(Seq(StatusCode.int2StatusCode(200))){parseStatusesCode}

    new RestConfig(url, methodHttp, requestTimeout, httpOutputFieldName, preservationPolicy, bodyString, bodyFormat,
      headersHttp, akkaProperties, statusCodeList)
  }

  protected[rest] def optIntFromString(s : String): Option[Int] =
    Try(s.trim.toInt).toOption.filter(numb => numb >= 100 && numb <= 999).orElse(None)

  private def extractRange(start: String, end: String) : Seq[Int] =
    (for {
      startInt <- optIntFromString(start)
      endInt <- optIntFromString(end)
    } yield startInt to endInt).fold(Seq.empty[Int]){list => list.toList}


  private def parseStatusesCode(string: String) : Seq[StatusCode] = {
    val regexRanges = "^(\\d{3})-(\\d{3})".r
    val (ranges, status) =
      string.split(",").partition(_.contains('-'))
    val listOfPorts: Seq[Int] = status.flatMap(optIntFromString) ++:
      ranges.flatMap {
        case regexRanges(start, end) => extractRange(start, end)
        case _ => Seq.empty[Int]
      }.toSeq
    Try(listOfPorts.map(StatusCode.int2StatusCode))
      .getOrElse(Seq(StatusCode.int2StatusCode(200)))
  }
}

case class RestConfig(
                       url: Option[String],
                       method: HttpMethod,
                       requestTimeout: Try[FiniteDuration],
                       httpOutputField: String = RestConfig.DefaultHttpOutputField,
                       preservationPolicy: FieldsPreservationPolicy.Value,
                       bodyString: Option[String] = None,
                       bodyFormat: Option[BodyFormat] = Option(BodyFormat.STRING),
                       headers: immutable.Seq[HttpHeader] = immutable.Seq.empty,
                       akkaHttpProperties: Map[String, String] = Map.empty,
                       statusCodeWhitelist: Seq[StatusCode] = Seq(StatusCodes.OK)
                     )