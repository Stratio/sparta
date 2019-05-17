/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.common.kafka

import java.io.IOException
import java.util

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.SSLHelper
import io.confluent.kafka.schemaregistry.client.rest.RestService
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException
import io.confluent.kafka.schemaregistry.client.{CachedSchemaRegistryClient, SchemaRegistryClient}
import org.apache.avro.Schema

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.{Map => MutableMap}

object SchemaRegistryClientFactory extends SLF4JLogging {

  type RegistryClientConfig = String

  private val schemaRegistryClientMap: MutableMap[RegistryClientConfig, SchemaRegistryClient] = MutableMap.empty

  def getOrCreate(schemaRegistryUrl: String, tlsEnabled: Boolean, configs: util.Map[String, _]): SchemaRegistryClient =
    synchronized {
      schemaRegistryClientMap.getOrElse(schemaRegistryUrl,
        {
          val restService = new RestService(schemaRegistryUrl)
          if (tlsEnabled) {
            log.debug(s"Creating ssl context for Schema Registry client with options ${configs}")
            val socketFactory = SSLHelper.getSSLContextV2(tlsEnabled, configs.toMap.mapValues(_.toString)).getSocketFactory
            restService.setSslSocketFactory(socketFactory)
          }
          val schemaRegClient = new SpartaCachedSchemaRegistryClient(restService, 1000)
          schemaRegistryClientMap.put(schemaRegistryUrl, schemaRegClient)
          schemaRegClient
        }
      )
    }

}
