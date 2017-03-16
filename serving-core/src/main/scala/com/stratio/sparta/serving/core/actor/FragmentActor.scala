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

package com.stratio.sparta.serving.core.actor

import akka.actor.Actor
import com.stratio.sparta.serving.core.actor.FragmentActor._
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, ResponsePolicy}
import com.stratio.sparta.serving.core.models.policy.fragment.FragmentElementModel
import com.stratio.sparta.serving.core.utils.FragmentUtils
import org.apache.curator.framework.CuratorFramework
import spray.httpx.Json4sJacksonSupport

import scala.util.Try

class FragmentActor(val curatorFramework: CuratorFramework) extends Actor with Json4sJacksonSupport with FragmentUtils {

  //scalastyle:off
  override def receive: Receive = {
    case FindAllFragments() => findAll()
    case FindByType(fragmentType) => findByType(fragmentType)
    case FindByTypeAndId(fragmentType, id) => findByTypeAndId(fragmentType, id)
    case FindByTypeAndName(fragmentType, name) => findByTypeAndName(fragmentType, name.toLowerCase())
    case DeleteAllFragments() => deleteAll()
    case DeleteByType(fragmentType) => deleteByType(fragmentType)
    case DeleteByTypeAndId(fragmentType, id) => deleteByTypeAndId(fragmentType, id)
    case DeleteByTypeAndName(fragmentType, name) => deleteByTypeAndName(fragmentType, name)
    case Create(fragment) => create(fragment)
    case Update(fragment) => update(fragment)
    case PolicyWithFragments(policy) => policyWithFragments(policy)
    case _ => log.info("Unrecognized message in Fragment Actor")
  }

  //scalastyle:on

  def findAll(): Unit =
    sender ! ResponseFragments(Try(findAllFragments))

  def findByType(fragmentType: String): Unit =
    sender ! ResponseFragments(Try(findFragmentsByType(fragmentType)))

  def findByTypeAndId(fragmentType: String, id: String): Unit =
    sender ! ResponseFragment(Try(findFragmentByTypeAndId(fragmentType, id)))

  def findByTypeAndName(fragmentType: String, name: String): Unit =
    sender ! ResponseFragment(Try(findFragmentByTypeAndName(fragmentType, name)
      .getOrElse(throw new ServingCoreException(ErrorModel.toString(new ErrorModel(
        ErrorModel.CodeNotExistsPolicyWithName, s"No fragment of type $fragmentType with name $name"))))))

  def create(fragment: FragmentElementModel): Unit =
    sender ! ResponseFragment(Try(createFragment(fragment)))

  def update(fragment: FragmentElementModel): Unit =
    sender ! Response(Try(updateFragment(fragment)))

  def deleteAll(): Unit =
    sender ! ResponseFragments(Try(deleteAllFragments()))

  def deleteByType(fragmentType: String): Unit =
    sender ! Response(Try(deleteFragmentsByType(fragmentType)))

  def deleteByTypeAndId(fragmentType: String, id: String): Unit =
    sender ! Response(Try(deleteFragmentByTypeAndId(fragmentType, id)))

  def deleteByTypeAndName(fragmentType: String, name: String): Unit =
    sender ! Response(Try(deleteFragmentByTypeAndName(fragmentType, name)))

  def policyWithFragments(policyModel: PolicyModel) : Unit =
    sender ! ResponsePolicy(Try(getPolicyWithFragments(policyModel)))

}

object FragmentActor {

  case class PolicyWithFragments(policy: PolicyModel)

  case class Create(fragment: FragmentElementModel)

  case class Update(fragment: FragmentElementModel)

  case class FindAllFragments()

  case class FindByType(fragmentType: String)

  case class FindByTypeAndId(fragmentType: String, id: String)

  case class FindByTypeAndName(fragmentType: String, name: String)

  case class DeleteAllFragments()

  case class DeleteByType(fragmentType: String)

  case class DeleteByTypeAndId(fragmentType: String, id: String)

  case class DeleteByTypeAndName(fragmentType: String, name: String)

  case class ResponseFragment(fragment: Try[FragmentElementModel])

  case class ResponseFragments(fragments: Try[Seq[FragmentElementModel]])

  case class Response(status: Try[_])

}