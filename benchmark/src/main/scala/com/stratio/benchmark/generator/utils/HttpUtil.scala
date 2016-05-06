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
package com.stratio.benchmark.generator.utils

import org.apache.http.HttpStatus
import org.apache.http.client.methods.{HttpDelete, HttpGet, HttpPost, HttpPut}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.apache.log4j.Logger
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._

import scala.io.Source

trait HttpUtil   {

  private val logger = Logger.getLogger(this.getClass)

  /**
   * Given a policy it makes an http request to start it on Sparta.
   * @param  policyContent with the policy in string format.
   * @param  endpoint to perform the post.
   */
  def createPolicy(policyContent: String, endpoint: String)(implicit defaultFormats: DefaultFormats): String = {

    val policyName = (parse(policyContent) \ "name").extract[String]

    // If the policy exists when it launches the benchmark, it should stop and delete it.
    getPolicyId(policyName, endpoint) match {
      case Some(id) =>
        stopPolicy(id, endpoint)
        deletePolicy(id, endpoint)
      case None => logger.debug(s"No policy with name $policyName exists in Sparta yet.")
    }

    val client = HttpClientBuilder.create().build()
    val post = new HttpPost(s"$endpoint/policyContext")
    post.setHeader("Content-type", "application/json")
    post.setEntity(new StringEntity(policyContent))
    val response = client.execute(post)

   if(response.getStatusLine.getStatusCode != HttpStatus.SC_OK)
     throw new IllegalStateException(s"Sparta status code is not OK: ${response.getStatusLine.getStatusCode}")
   else {
     val entity = response.getEntity
     val policyId = (parse(EntityUtils.toString(entity)) \ "policyId").extract[String]
     policyId
   }
  }

  def getPolicyId(name: String, endpoint: String)(implicit defaultFormats: DefaultFormats): Option[String] = {
    val client = HttpClientBuilder.create().build()
    val get = new HttpGet(s"$endpoint/policy/findByName/$name")

    val response = client.execute(get)

    response.getStatusLine.getStatusCode match {
      case HttpStatus.SC_OK =>
        Option((parse(EntityUtils.toString(response.getEntity)) \ "id").extract[String])
      case _ => None
    }
  }

  def stopPolicy(id: String, endpoint: String): Unit = {
    val client = HttpClientBuilder.create().build()
    val put = new HttpPut(s"$endpoint/policyContext")
    put.setHeader("Content-Type", "application/json")
    val entity = new StringEntity(s"""{"id":"$id", "status":"Stopping"}""")
    put.setEntity(entity)
    val response = client.execute(put)

    if(response.getStatusLine.getStatusCode != HttpStatus.SC_CREATED) {
      logger.info(Source.fromInputStream(response.getEntity.getContent).mkString(""))
      logger.info(s"Sparta status code is not OK: ${response.getStatusLine.getStatusCode}")
    }
  }

  def deletePolicy(id: String, endpoint: String): Unit = {
    val client = HttpClientBuilder.create().build()
    val delete = new HttpDelete(s"$endpoint/policy/$id")
    val response = client.execute(delete)

    if(response.getStatusLine.getStatusCode != HttpStatus.SC_OK)
      logger.info(s"Sparta status code is not OK: ${response.getStatusLine.getStatusCode}")
  }
}
