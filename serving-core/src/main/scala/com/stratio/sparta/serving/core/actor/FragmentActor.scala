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
import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.actor.FragmentActor._
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, ResponsePolicy}
import com.stratio.sparta.serving.core.models.policy.fragment.{FragmentElementModel, FragmentType}
import com.stratio.sparta.serving.core.utils.{ActionUserAuthorize, FragmentUtils}
import org.apache.curator.framework.CuratorFramework
import spray.httpx.Json4sJacksonSupport
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper._
import com.stratio.sparta.serving.core.models.dto.LoggedUser

import scala.util.Try

class FragmentActor(val curatorFramework: CuratorFramework, val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with Json4sJacksonSupport with FragmentUtils with ActionUserAuthorize {


  //scalastyle:off
  override def receive: Receive = {
    case FindAllFragments(user) => findAll(user)
    case FindByType(fragmentType, user) => findByType(fragmentType, user)
    case FindByTypeAndId(fragmentType, id, user) => findByTypeAndId(fragmentType, id, user)
    case FindByTypeAndName(fragmentType, name, user) => findByTypeAndName(fragmentType, name.toLowerCase(), user)
    case DeleteAllFragments(user) => deleteAll(user)
    case DeleteByType(fragmentType, user) => deleteByType(fragmentType, user)
    case DeleteByTypeAndId(fragmentType, id, user) => deleteByTypeAndId(fragmentType, id, user)
    case DeleteByTypeAndName(fragmentType, name, user) => deleteByTypeAndName(fragmentType, name, user)
    case CreateFragment(fragment, user) => create(fragment, user)
    case Update(fragment, user) => update(fragment, user)
    case PolicyWithFragments(policy) => policyWithFragments(policy)
    case _ => log.info("Unrecognized message in Fragment Actor")
  }

  def findAll(user:  Option[LoggedUser]): Unit =
    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        if (secManager.authorize(userLogged.id, FragmentType.InputValue, View) &&
          secManager.authorize(userLogged.id, FragmentType.OutputValue, View))
            sender ! ResponseFragments(Try(findAllFragments)) // [T]
        else
          sender ! errorResponseAuthorization(userLogged.id, View)
      case (Some(secManager), None) => sender ! errorNoUserFound(View)
      case (None, _) => sender ! ResponseFragments(Try(findAllFragments))
    }

  def findByType(fragmentType: String, user:  Option[LoggedUser]): Unit =
    securityActionAuthorizer(secManagerOpt, user, fragmentType, View,
      ResponseFragments(Try(findFragmentsByType(fragmentType))))

  def findByTypeAndId(fragmentType: String, id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(secManagerOpt, user, fragmentType, View,
      ResponseFragment(Try(findFragmentByTypeAndId(fragmentType, id))))


  def findByTypeAndName(fragmentType: String, name: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(secManagerOpt, user, fragmentType, View,
      ResponseFragment(Try(findFragmentByTypeAndName(fragmentType, name)
      .getOrElse(errorFragmentNotFound(fragmentType,name)))))


  def create(fragment: FragmentElementModel, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(secManagerOpt, user, fragment.fragmentType, Create,
      ResponseFragment(Try(createFragment(fragment))))


  def update(fragment: FragmentElementModel, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(secManagerOpt, user, fragment.fragmentType, Edit,
      Response(Try(updateFragment(fragment))))


  def deleteAll(user: Option[LoggedUser]): Unit =
    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        if (secManager.authorize(userLogged.id, FragmentType.InputValue, Delete) &&
          secManager.authorize(userLogged.id, FragmentType.OutputValue, Delete))
            sender ! ResponseFragments(Try(deleteAllFragments()))
        else
          sender ! errorResponseAuthorization(userLogged.id, Delete)
      case (Some(secManager), None) => sender ! errorNoUserFound(Delete)
      case (None, _) => sender ! ResponseFragments(Try(deleteAllFragments()))
    }

  def deleteByType(fragmentType: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(secManagerOpt, user, fragmentType, Delete,
      Response(Try(deleteFragmentsByType(fragmentType))))


  def deleteByTypeAndId(fragmentType: String, id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(secManagerOpt, user, fragmentType, Delete,
      Response(Try(deleteFragmentByTypeAndId(fragmentType, id))))


  def deleteByTypeAndName(fragmentType: String, name: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(secManagerOpt, user, fragmentType, Delete,
      Response(Try(deleteFragmentByTypeAndName(fragmentType, name))))


  def policyWithFragments(policyModel: PolicyModel): Unit =
    sender ! ResponsePolicy(Try(getPolicyWithFragments(policyModel)))


  //PRIVATE METHODS

  private def errorFragmentNotFound(fragmentType: String, name : String): Nothing = {
    throw new ServingCoreException(ErrorModel.toString(new ErrorModel(
      ErrorModel.CodeNotExistsPolicyWithName, s"No fragment of type $fragmentType with name $name")))
  }

}

object FragmentActor {

  case class PolicyWithFragments(policy: PolicyModel)

  case class CreateFragment(fragment: FragmentElementModel, user: Option[LoggedUser])

  case class Update(fragment: FragmentElementModel, user: Option[LoggedUser])

  case class FindAllFragments(user: Option[LoggedUser])

  case class FindByType(fragmentType: String, user: Option[LoggedUser])

  case class FindByTypeAndId(fragmentType: String, id: String, user: Option[LoggedUser])

  case class FindByTypeAndName(fragmentType: String, name: String, user: Option[LoggedUser])

  case class DeleteAllFragments(user: Option[LoggedUser])

  case class DeleteByType(fragmentType: String, user: Option[LoggedUser])

  case class DeleteByTypeAndId(fragmentType: String, id: String, user: Option[LoggedUser])

  case class DeleteByTypeAndName(fragmentType: String, name: String, user: Option[LoggedUser])

  case class ResponseFragment(fragment: Try[FragmentElementModel])

  case class ResponseFragments(fragments: Try[Seq[FragmentElementModel]])

  case class Response(status: Try[_])

}