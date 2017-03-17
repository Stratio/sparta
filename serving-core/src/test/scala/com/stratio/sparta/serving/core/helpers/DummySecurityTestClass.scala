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

import com.stratio.sparta.security._

class DummySecurityTestClass extends SpartaSecurityManager {

  override def start(): Unit = {}

  override def stop(): Unit = {}

  override def authorize(userId: String, resource: Resource, action: Action): Boolean =
    (userId, resource, action) match {

      case ("1234", _, _) => true
      case ("4321", Resource(InputResource, "input"), Edit) => false
      case ("4321", Resource(InputResource, "input"), View) => false
      case ("4321", Resource(OutputResource, "output"), Create) => false
      case ("4321", Resource(OutputResource, "output"), Delete) => false
      case ("1111", Resource(PolicyResource, "policy"), Create) => false
      case ("1111", Resource(PolicyResource, "policy"), _) => false
      case ("1111", Resource(PolicyResource, "plugin"), _) => false
      case (_, _, _) => true
    }

  override def audit(auditEvent: AuditEvent): Unit = {}

}
