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

package com.stratio.sparkta.sdk

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class BucketerSpec extends WordSpec with Matchers {

  "Bucketer object" should {

    "getIdentity must be " in {
      val identity = Bucketer.getIdentity(None, TypeOp.Int)
      identity.typeOp should be(TypeOp.Int)
      identity.id should be(Bucketer.IdentityName)
      val identity2 = Bucketer.getIdentity(Some(TypeOp.String), TypeOp.Int)
      identity2.typeOp should be(TypeOp.String)
    }

    "getIdentityField must be " in {
      val identity = Bucketer.getIdentityField(None, TypeOp.Int)
      identity.typeOp should be(TypeOp.Int)
      identity.id should be(Bucketer.IdentityFieldName)
      val identity2 = Bucketer.getIdentityField(Some(TypeOp.String), TypeOp.Int)
      identity2.typeOp should be(TypeOp.String)
    }

    "getTimestamp must be " in {
      val identity = Bucketer.getTimestamp(None, TypeOp.Int)
      identity.typeOp should be(TypeOp.Int)
      identity.id should be(Bucketer.TimestampName)
      val identity2 = Bucketer.getTimestamp(Some(TypeOp.String), TypeOp.Int)
      identity2.typeOp should be(TypeOp.String)
    }
  }
}