/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.benchmark.generator.utils

import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpDelete
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.apache.log4j.Logger
import org.json4s._
import org.json4s.native.JsonMethods._

object HttpUtil {

  implicit val formats = DefaultFormats
  private val logger = Logger.getLogger(this.getClass)

  /**
    * Given a policy it makes an http request to start it on Sparkta.
    *
    * @param  policyContent is policy definition.
    * @param  endPoint      to perform the post.
    */
  def post(policyContent: String, endPoint: String): Unit = {
    val client = HttpClientBuilder.create().build()
    val post = new HttpPost(endPoint)
    post.setHeader("Content-type", "application/json")
    post.setEntity(new StringEntity(policyContent))
    val response = client.execute(post)

    if (response.getStatusLine.getStatusCode != HttpStatus.SC_OK)
      throw new IllegalStateException(s"Sparkta status code is not OK: ${response.getStatusLine.getStatusCode}")
  }

  /**
    * Given a policy it makes an http request to stop it on Sparkta.
    *
    * @param  policyName name of policy to be stopped.
    * @param  endPoint   to perform the get.
    */
  def stop(policyName: String, endPoint: String): Unit = {
    val client = HttpClientBuilder.create().build()
    val realEndPoint = endPoint.replace("/policyContext", "")
    val http1 = new HttpGet(s"$realEndPoint/policy/findByName/$policyName")
    http1.setHeader("Content-type", "application/json")
    val response1 = client.execute(http1)
    response1.getStatusLine.getStatusCode match {
      case HttpStatus.SC_OK => {
        val jsonResponse = EntityUtils.toString(response1.getEntity(), "UTF-8")
        val policyId = extractFromJson(s"$jsonResponse", "id")
        val http2 = new HttpPut(s"$endPoint")
        http2.addHeader("content-type", "application/json")
        val params = new StringEntity(s"""{"id":"$policyId", "status":"Stopping"}""", "UTF-8")
        params.setContentType("application/json")
        http2.setEntity(params)
        val response2 = client.execute(http2)
        response2.getStatusLine.getStatusCode match {
          case HttpStatus.SC_OK => logger.error(s"policy $policyName was stopped successfully")
          case _ => logger.error(s"error stopping benchmark $policyName")
        }
      }
      case _ => logger.warn(s"Not found a policy with name: $policyName")
    }
  }

  /**
    * Given a policy it makes an http request to delete it on Sparkta, if exists.
    *
    * @param  policyName with the path of the policy.
    * @param  endPoint   to perform the get.
    */
  def delete(policyName: String, endPoint: String): Unit = {
    val client = HttpClientBuilder.create().build()
    val realEndPoint = endPoint.replace("/policyContext", "")
    val http1 = new HttpGet(s"$realEndPoint/policy/findByName/$policyName")
    http1.setHeader("Content-type", "application/json")
    val response1 = client.execute(http1)
    response1.getStatusLine.getStatusCode match {
      case HttpStatus.SC_OK => {
        val jsonResponse = EntityUtils.toString(response1.getEntity(), "UTF-8")
        val policyId = extractFromJson(s"$jsonResponse", "id")
        val http2 = new HttpDelete(s"$realEndPoint/policy/$policyId")
        val response2 = client.execute(http2)
        response2.getStatusLine.getStatusCode match {
          case HttpStatus.SC_OK => logger.error(s"policy $policyName was stopped successfully")
          case _ => logger.error(s"error stopping benchmark $policyName")
        }
      }
      case _ => logger.info(s"Not previous policies with name: $policyName")
    }
  }

  /**
    * Given a policy it makes an http request to delete it on Sparkta, if exists.
    *
    * @param  json  a string.
    * @param  field to find inside the json.
    */
  def extractFromJson(json: String, field: String): String = {
    (parse(json) \ field).extract[String]
  }
}