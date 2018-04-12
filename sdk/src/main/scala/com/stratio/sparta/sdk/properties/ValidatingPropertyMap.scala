/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.properties

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.models.PropertyFields
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization._

import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}


class ValidatingPropertyMap[K, V](val m: Map[K, V]) extends SLF4JLogging {

  def getString(key: K): String =
    m.get(key) match {
      case Some(value: String) => value
      case Some(value) => value.toString
      case None =>
        throw new IllegalStateException(s"$key is mandatory")
    }

  def getPropertiesFields(key: K): PropertyFields = {
    implicit val json4sJacksonFormats: Formats =
      DefaultFormats +
        new JsoneyStringSerializer()

    read[PropertyFields](
      s"""{"fields": ${m.get(key).fold("[]") { values => values.toString }}}"""
    )
  }

  //scalastyle:off
  def getMapFromArrayOfValues(key: K): Seq[Map[String, String]] = {
    m.get(key) match {
      case Some(value) =>

        val parsed = parse(s"""{"children": ${value.toString} }"""")

        val result: List[Map[String, String]] =
          for {
            JArray(element) <- parsed \ "children"
            JObject(list) <- element
          } yield {
            (for {
              (key, value) <- list
            } yield {
              val valueToReturn = value match {
                case value : JString => value.s
                case value : JInt => value.num.toString
                case value : JDouble => value.num.toString
                case value : JBool => value.value.toString
                case value : JDecimal => value.num.toString
                case _ => compact(render(value))
              }
              (key, valueToReturn)
            }).toMap
          }
        if (result.isEmpty)
          throw new IllegalStateException(s"$key is mandatory")
        else result
      case None => throw new IllegalStateException(s"$key is mandatory")
    }
  }

  def getString(key: K, default: String): String = {
    m.get(key) match {
      case Some(value: String) => if (value.isEmpty) default else value
      case Some(null) => default
      case Some(value) => if (value.toString.isEmpty) default else value.toString
      case None => default
    }
  }

  def getString(key: K, default: Option[String]): Option[String] = {
    m.get(key) match {
      case Some(value: String) => if (value.isEmpty) default else Option(value)
      case Some(null) => default
      case Some(value) => if (value.toString.isEmpty) default else Option(value.toString)
      case None => default
    }
  }

  //scalastyle:off
  def getBoolean(key: K): Boolean =
      m.get(key) match {
      case Some(value: String) => value.toBoolean
      case Some(value: Int) =>
        value.asInstanceOf[Int] match {
          case 1 => true
          case 0 => false
          case _ => throw new IllegalStateException(s"$value is not parsable as boolean")
        }
      case Some(value: Boolean) => value
      case Some(value) =>
        Try(value.toString.toBoolean) match {
          case Success(v) => v
          case Failure(ex) => throw new IllegalStateException(s"Bad value for $key", ex)
        }
      case None => throw new IllegalStateException(s"$key is mandatory")
    }

  def getBoolean(key: K, default: Boolean): Boolean =
    m.get(key) match {
      case Some(value: String) => value.toBoolean
      case Some(value: Int) =>
        value match {
          case 1 => true
          case 0 => false
          case _ => default
        }
      case Some(value: Boolean) => value
      case Some(value) => Try(value.toString.toBoolean).getOrElse(default)
      case None => default
    }

  def getBoolean(key: K, default: Option[Boolean]): Option[Boolean] =
    m.get(key) match {
      case Some(value: String) => Option(value.toBoolean)
      case Some(value: Int) =>
        value match {
          case 1 => Option(true)
          case 0 => Option(false)
          case _ => default
        }
      case Some(value: Boolean) => Option(value)
      case Some(value) => Try(value.toString.toBoolean) match {
        case Success(x) => Option(x)
        case Failure(_) => default
      }
      case None => default
    }

  def getInt(key: K): Int =
    m.get(key) match {
      case Some(value: String) =>
        Try(value.toInt) match {
          case Success(v) => v
          case Failure(ex) => throw new IllegalStateException(s"Bad value for $key", ex)
        }
      case Some(value: Int) => value
      case Some(value: Long) => value.toInt
      case Some(value) =>
        Try(value.toString.toInt) match {
          case Success(v) => v
          case Failure(ex) => throw new IllegalStateException(s"Bad value for $key", ex)
        }
      case None =>
        throw new IllegalStateException(s"$key is mandatory")
    }

  def getInt(key: K, default: Int): Int =
    m.get(key) match {
      case Some(value: String) => Try(value.toInt).getOrElse(default)
      case Some(value: Int) => value
      case Some(value: Long) => Try(value.toInt).getOrElse(default)
      case Some(value) => Try(value.toString.toInt).getOrElse(default)
      case None => default
    }

  def getInt(key: K, default: Option[Int]): Option[Int] =
    m.get(key) match {
      case Some(value: String) => Try(value.toInt) match {
        case Success(x) => Option(x)
        case Failure(_) => default
      }
      case Some(value: Int) =>
        Option(value)
      case Some(value: Long) => Try(value.toInt) match {
        case Success(x) => Option(x)
        case Failure(_) => default
      }
      case Some(value) => Try(value.toString.toInt) match {
        case Success(x) => Option(x)
        case Failure(_) => default
      }
      case None => default
    }

  def getLong(key: K): Long =
    m.get(key) match {
      case Some(value: String) =>
        Try(value.toLong) match {
          case Success(v) => v
          case Failure(ex) => throw new IllegalStateException(s"Bad value for $key", ex)
        }
      case Some(value: Long) =>
        value
      case Some(value: Int) =>
        Try(value.toLong) match {
        case Success(v) => v
        case Failure(ex) => throw new IllegalStateException(s"Bad value for $key", ex)
      }
      case Some(value) =>
        Try(value.toString.toLong) match {
          case Success(v) => v
          case Failure(ex) => throw new IllegalStateException(s"Bad value for $key", ex)
        }
      case None =>
        throw new IllegalStateException(s"$key is mandatory")
    }

  def getLong(key: K, default: Long): Long =
    m.get(key) match {
      case Some(value: String) => Try(value.toLong).getOrElse(default)
      case Some(value: Long) => value
      case Some(value: Int) => value.toInt
      case Some(value) => Try(value.toString.toLong).getOrElse(default)
      case None => default
    }

  def getLong(key: K, default: Option[Long]): Option[Long] =
    m.get(key) match {
      case Some(value: String) => Try(value.toLong) match {
        case Success(x) => Option(x)
        case Failure(_) => default
      }
      case Some(value: Long) =>
        Option(value)
      case Some(value: Int) => Try(value.toLong) match {
        case Success(x) => Option(x)
        case Failure(_) => default
      }
      case Some(value) => Try(value.toString.toLong) match {
        case Success(x) => Option(x)
        case Failure(_) => default
      }
      case None => default
    }

  def getOptionsList(key: K,
                     propertyKey: String,
                     propertyValue: String): Map[String, String] =
    Try(getMapFromArrayOfValues(key)).getOrElse(Seq.empty[Map[String, String]])
      .map(c =>
        (c.get(propertyKey) match {
          case Some(value) => value.toString
          case None => throw new IllegalStateException(s"The field $propertyKey is mandatory")
        },
          c.get(propertyValue) match {
            case Some(value) => value.toString
            case None => throw new IllegalStateException(s"The field $propertyValue is mandatory")
          })).toMap

  def hasKey(key: K): Boolean = m.get(key).isDefined
}

class NotBlankOption(s: Option[String]) {
  def notBlankWithDefault(default: String): String = notBlank.getOrElse(default)

  def notBlank: Option[String] = s.map(_.trim).filterNot(_.isEmpty)
}

class NotBlankOptionJString(s: Option[JsoneyString]) {
  def notBlankWithDefault(default: String): String = notBlank.getOrElse(default)

  def notBlank: Option[String] = s.map(_.toString.trim).filterNot(_.isEmpty)
}

object ValidatingPropertyMap {

  implicit def jsoneyStringToString(str: JsoneyString): String = str.toString

  implicit def stringToJsoneyString(str: String): JsoneyString = JsoneyString(str)

  implicit def map2ValidatingPropertyMap[K, V](m: Map[K, V]): ValidatingPropertyMap[K, V] =
    new ValidatingPropertyMap[K, V](m)

  implicit def option2NotBlankOption(s: Option[String]): NotBlankOption = new NotBlankOption(s)

  implicit def option2NotBlankOptionJString(s: Option[JsoneyString]): NotBlankOptionJString = new NotBlankOptionJString(s)

}