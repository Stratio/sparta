/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
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
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder

import scala.io.Source

object HttpUtils   {

  /**
   * Given a policy it makes an http request to start it on Sparkta.
   * @param  policyName the policy.
   * @param  endPoint of Sparkta
   */
  def post(policyName: String, endPoint: String): Unit = {
    val policyContent = Source.fromInputStream(
      this.getClass.getClassLoader.getResourceAsStream(policyName)).mkString
    val client = HttpClientBuilder.create().build()
    val post = new HttpPost(endPoint)
    post.setHeader("Content-type", "application/json")
    post.setEntity(new StringEntity(policyContent))
    val response = client.execute(post)

   if(response.getStatusLine.getStatusCode != HttpStatus.SC_OK)
     throw new IllegalStateException(s"Sparkta status code is not OK: ${response.getStatusLine.getStatusCode}")
  }
}
