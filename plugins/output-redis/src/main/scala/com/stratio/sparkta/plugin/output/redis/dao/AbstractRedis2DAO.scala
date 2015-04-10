/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
/**
* Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.plugin.output.redis.dao

import com.redis.RedisClient
import com.stratio.sparkta.sdk.Event
import org.apache.spark.streaming.dstream.DStream


/**
* Created by anistal on 4/8/15.
*/
trait AbstractRedis2DAO {

  def hostname : String
  def dbName : String
  def eventTimeFieldName: String = "eventTime"
  def idFieldName: String = "_id"
  def idSeparator: String = "__"

  protected def client: RedisClient = AbstractRedis2DAO.client(hostname)

  def insert(event: Event): Unit = {
    client.set("testing", "testing")
  }
}

private object AbstractRedis2DAO {

  private def client(clientUri: String): RedisClient = {
    val DefaultRedisPort: Integer = 6379
    val client = new RedisClient(clientUri, DefaultRedisPort)
    client
  }


}
