/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.common.kafka

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.models.PropertyHostPort
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

trait KafkaBase extends SLF4JLogging {

  val properties: Map[String, JSerializable]

  /** HOSTS and PORT extractions **/

  def getBootstrapServers(bootstrapServers: String): Map[String, String] = {
    val connection = try {
      if (properties.contains(bootstrapServers)) {
        implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
        val propertyHostsPorts =
          s"${properties.getString(bootstrapServers, None).notBlank.fold("[]") { values => values.toString }}"
        val hostsPortsModel = read[Seq[PropertyHostPort]](propertyHostsPorts)

        if (hostsPortsModel.nonEmpty &&
          hostsPortsModel.forall(model => model.host.nonEmpty && model.port.nonEmpty))
          Option(hostsPortsModel.map(hostHortModel =>
            s"${hostHortModel.host}:${hostHortModel.port}").mkString(",")
          )
        else {
          log.warn(s"Hosts ports extracted is empty or have incorrect host or port. Model: $hostsPortsModel")
          None
        }
      } else {
        log.warn(s"The properties do not contain the $bootstrapServers")
        None
      }
    } catch {
      case e: Exception =>
        log.warn(s"Error extracting kafka connection chain. Error: ${e.getLocalizedMessage}")
        None
    }

    connection match {
      case Some(connectionKey) => Map(bootstrapServers -> connectionKey)
      case None => Map.empty
    }
  }

}
