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
package com.stratio.sparkta.sdk.exception

/**
 * This class it's used to create custom exceptions that will be mostly used in tests
 * To throw a MockException: new MockException(mockErrorMessage)
 * @author gschiavon
 */
class MockException(msg: String) extends RuntimeException(msg)

object MockException {
  def create(msg: String): MockException = new MockException(msg)

  def create(msg: String, cause: Throwable): Throwable = new MockException(msg).initCause(cause)
}
