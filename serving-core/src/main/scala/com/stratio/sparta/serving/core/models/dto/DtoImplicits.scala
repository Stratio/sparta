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

package com.stratio.sparta.serving.core.models.dto

/**
  * Base Trait for all dto´s classes. This trait is used by the generic list implicit conversion
  */
trait Dto

object DtoImplicits {

  /**
    * Generic Implicit transformation from Seq[G] to Seq[T] (the related implicit transformation from G to T, are
    * defined in each G type Actor class
    */
  implicit def toDtoList[G, T <: Dto](elem: Seq[G])(implicit converter: G => T): Seq[T] = {
    for {
      e <- elem
    } yield (converter(e))
  }
}
