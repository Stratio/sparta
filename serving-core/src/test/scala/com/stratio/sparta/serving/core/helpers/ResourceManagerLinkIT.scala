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

package com.stratio.sparta.serving.core.helpers

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ResourceManagerLinkIT extends FlatSpec with ShouldMatchers with Matchers {

  val sparkUIPort = 4040
  val mesosPort = 5050
  val localhost = "127.0.0.1"

  it should "return local Spark UI link" in {
    val localhostName = java.net.InetAddress.getLocalHost().getHostName()
    ResourceManagerLinkHelper.getLink("local", Option("local[*]"), None, false) should be(Some
    (s"http://${localhostName}:${sparkUIPort}"))
  }

  it should "return Mesos UI link" in {
    ResourceManagerLinkHelper.getLink("mesos", Option("mesos://127.0.0.1:7077"), None, false) should be(
      Some(s"http://$localhost:$mesosPort"))
  }
}
