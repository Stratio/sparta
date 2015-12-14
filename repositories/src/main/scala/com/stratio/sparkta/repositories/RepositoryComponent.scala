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

package com.stratio.sparkta.repositories

import scala.util.Try

/**
 * Repository DTO interface
 */
trait RepositoryComponent[K, V] {

  val repository: Repository

  trait Repository {

    trait RepositoryState
    case object Started extends RepositoryState
    case object Stopped extends RepositoryState
    case object NotStarted extends RepositoryState
    case object Unknown extends RepositoryState

    def get(id: K): Try[Option[V]]

    def getSubRepository(id: K): Try[List[K]]

    def exists(id: K): Try[Boolean]
    
    def create(id: K, element: V): Try[Boolean]

    def update(id: K, element: V): Try[Boolean]

    def delete(id: K): Try[Boolean]

    def getConfig: Map[String, Any]

    def start: Boolean

    def stop: Boolean
    
    def getState: RepositoryState

    def isStarted: Boolean = getState == Started

    def getSubRepositoryValues(id: K): Try[List[V]] =
      getSubRepository(id).map { keyList =>
        keyList.flatMap { key =>
          get(key).toOption.flatMap(x => x)
        }
      }
  }
}