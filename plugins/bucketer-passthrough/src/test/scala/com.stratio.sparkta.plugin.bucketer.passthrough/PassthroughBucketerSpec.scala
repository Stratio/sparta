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

package com.stratio.sparkta.plugin.bucketer.passthrough

import java.io.{Serializable => JSerializable}

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import com.stratio.sparkta.sdk.{Bucketer, TypeOp}

@RunWith(classOf[JUnitRunner])
class PassthroughBucketerSpec extends WordSpecLike with Matchers with BeforeAndAfter with BeforeAndAfterAll {

  var passthroughBucketer: PassthroughBucketer = null
  before {
    passthroughBucketer = new PassthroughBucketer(Map("typeOp" -> "int"))
  }

  after {
    passthroughBucketer = null
  }

  "A PassthroughBucketer" should {
    "In default implementation, get one buckets for a specific time" in {
      val buckets = passthroughBucketer.bucket("foo".asInstanceOf[JSerializable]).map(_._1.id)

      buckets.size should be(1)

      buckets should contain(Bucketer.IdentityName)
    }

    "The precision must be int" in {
      passthroughBucketer.bucketTypes(Bucketer.IdentityName).typeOp should be(TypeOp.Int)
    }

  }
}
