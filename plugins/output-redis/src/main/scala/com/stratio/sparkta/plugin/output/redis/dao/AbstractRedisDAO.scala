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

package com.stratio.sparkta.plugin.output.redis.dao

import com.redis.RedisClientPool
import com.redis.serialization.{Format, Parse}

/**
 * Trait with common operations over redis server.
 *
 * @author anistal
 */
trait AbstractRedisDAO {

  def hostname: String

  def port: Int

  def eventTimeFieldName: String = "eventTime"

  val IdSeparator: String = ":"

  val DefaultRedisPort: String = "6379"

  val DefaultRedisHostname: String = "localhost"

  protected def pool: RedisClientPool = AbstractRedisDAO.pool(hostname, port)

  def hset(key: Any, field: Any, value: Any)(implicit format: Format): Boolean =
    pool.withClient(client => client.hset(key, field, value))


  def hget[A](key: Any, field: Any)(implicit format: Format, parse: Parse[A]): Option[A] = {
    pool.withClient(client =>
      client.hget(key, field)
    )
  }
}

/**
 * Initializes singletons objects needed in the trait.
 *
 * @author anistal
 */
private object AbstractRedisDAO {

  var instance: Option[RedisClientPool] = None

  /**
   * Initializes a Redis connection pool.
   *
   * @param hostname of the redis server.
   * @param port of the redis server.
   * @return a pool of connections.
   */
  def pool(hostname: String, port: Int): RedisClientPool = {
    instance = if(instance.isEmpty) Some(new RedisClientPool(hostname, port)) else instance
    instance.get
  }
}
