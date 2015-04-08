package com.stratio.sparkta.plugin.output.redis

import com.redis.RedisClient
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by anistal on 4/8/15.
 */
class RedisOutputSpec extends FlatSpec with Matchers {

  "When an element is setted in redis" should "to have the element in the memory" in {
    val redisClient = new RedisClient("localhost", 6379)
    redisClient.set("Some key", "Some value")
    redisClient.get("Some key") should be (Some("Some value"))
  }

}
