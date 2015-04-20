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

trait AbstractRedisDAO {

  def hostname : String
  def port : Int
  def idSeparator: String = ":"

  def client: RedisClient = AbstractRedisDAO.client(hostname, port)
}

private object AbstractRedisDAO {
  var _client : Option[RedisClient] = None

  def client(clientUri: String, port: Int): RedisClient = {
    _client = this._client match {
      case None => Some(new RedisClient(clientUri, port))
      case _ => this._client
    }
    _client.get
  }
}
