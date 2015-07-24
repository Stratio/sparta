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
package com.stratio.sparkta.serving.api.actor

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging

abstract class InstrumentedActor extends Actor with SLF4JLogging {
  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error("About to restart actor due to exception:", reason)
    super.preRestart(reason, message)
  }

  override def postStop() {
    log.warn("Shutting down {}", getClass.getName)
  }
}
