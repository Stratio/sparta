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
import com.stratio.sparta.serving.core.models.workflow.{WorkflowModel, ResponseWorkflow}
import com.stratio.sparta.serving.core.models.workflow.fragment.{FragmentElementModel, FragmentType}
import com.stratio.sparta.serving.core.utils.{ActionUserAuthorize, FragmentUtils}
import org.apache.curator.framework.CuratorFramework
import spray.httpx.Json4sJacksonSupport
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper._
import com.stratio.sparta.serving.core.models.dto.LoggedUser

import scala.util.Try

class FragmentActor(val curatorFramework: CuratorFramework, val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with Json4sJacksonSupport with FragmentUtils with ActionUserAuthorize {

  val PolicyResource = "policy"

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
    case PolicyWithFragments(policy, user) => policyWithFragments(policy, user)
    case _ => log.info("Unrecognized message in Fragment Actor")
  }

  def findAll(user:  Option[LoggedUser]): Unit =
    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        if (secManager.authorize(userLogged.id, FragmentType.InputValue, View) &&
          secManager.authorize(userLogged.id, FragmentType.OutputValue, View))
            sender ! Left(ResponseFragments(Try(findAllFragments))) // [T]
        else
          sender ! Right(errorResponseAuthorization(userLogged.id, PolicyResource))
      case (Some(secManager), None) => sender ! Right(errorNoUserFound(Seq(View)))
      case (None, _) => sender ! Left(ResponseFragments(Try(findAllFragments)))
    }

  def findByType(fragmentType: String, user:  Option[LoggedUser]): Unit = {
    def callback() = ResponseFragments(Try(findFragmentsByType(fragmentType)))

    securityActionAuthorizer[ResponseFragments](secManagerOpt, user, Map(fragmentType -> View), callback)
  }

  def findByTypeAndId(fragmentType: String, id: String, user: Option[LoggedUser]): Unit = {
    def callback() = ResponseFragment(Try(findFragmentByTypeAndId(fragmentType, id)))

    securityActionAuthorizer[ResponseFragment](secManagerOpt, user, Map(fragmentType -> View), callback)
  }


  def findByTypeAndName(fragmentType: String, name: String, user: Option[LoggedUser]): Unit = {
    def callback() = ResponseFragment(Try(findFragmentByTypeAndName(fragmentType, name)
      .getOrElse(errorFragmentNotFound(fragmentType, name))))

    securityActionAuthorizer[ResponseFragment](secManagerOpt, user, Map(fragmentType -> View), callback)
  }


  def create(fragment: FragmentElementModel, user: Option[LoggedUser]): Unit = {
    def callback() = ResponseFragment(Try(createFragment(fragment)))

    securityActionAuthorizer[ResponseFragment](secManagerOpt, user, Map(fragment.fragmentType -> Create), callback)
  }

  def update(fragment: FragmentElementModel, user: Option[LoggedUser]): Unit = {
    def callback() = Response(Try(updateFragment(fragment)))
    securityActionAuthorizer[Response](secManagerOpt,
      user,
      Map(fragment.fragmentType -> Edit, PolicyResource -> View, PolicyResource -> Edit),
      callback
      )
  }

  def deleteAll(user: Option[LoggedUser]): Unit =
    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        if (secManager.authorize(userLogged.id, FragmentType.InputValue, Delete) &&
          secManager.authorize(userLogged.id, FragmentType.OutputValue, Delete) &&
          secManager.authorize(userLogged.id, PolicyResource, View) &&
          secManager.authorize(userLogged.id, PolicyResource, Delete)
        )
            sender ! Left(ResponseFragments(Try(deleteAllFragments())))
        else
          sender ! Right(errorResponseAuthorization(userLogged.id, PolicyResource))
      case (Some(secManager), None) => sender ! Right(errorNoUserFound(Seq(Delete)))
      case (None, _) => sender ! Left(ResponseFragments(Try(deleteAllFragments())))
    }

  def deleteByType(fragmentType: String, user: Option[LoggedUser]): Unit = {
    def callback() = Response(Try(deleteFragmentsByType(fragmentType)))

    securityActionAuthorizer[Response](secManagerOpt,
      user,
      Map(fragmentType -> Delete, PolicyResource -> Delete, PolicyResource -> View),
      callback
      )
  }


  def deleteByTypeAndId(fragmentType: String, id: String, user: Option[LoggedUser]): Unit = {
    def callback() = Response(Try(deleteFragmentByTypeAndId(fragmentType, id)))

    securityActionAuthorizer[Response](secManagerOpt,
      user,
      Map(fragmentType -> Delete, PolicyResource -> Delete, PolicyResource -> View),
      callback)
  }


  def deleteByTypeAndName(fragmentType: String, name: String, user: Option[LoggedUser]): Unit = {
    def callback() = Response(Try(deleteFragmentByTypeAndName(fragmentType, name)))

    securityActionAuthorizer[Response](secManagerOpt,
      user,
      Map(fragmentType -> Delete, PolicyResource -> Delete, PolicyResource -> View),
      callback
    )
  }


  def policyWithFragments(policyModel: WorkflowModel, user: Option[LoggedUser]): Unit = {
    def callback() = ResponseWorkflow(Try(getPolicyWithFragments(policyModel)))

    securityActionAuthorizer[ResponseWorkflow](secManagerOpt,
      user,
      Map(FragmentType.InputValue -> View, FragmentType.OutputValue -> View),
      callback
      )
  }

  //PRIVATE METHODS

  private def errorFragmentNotFound(fragmentType: String, name : String): Nothing = {
    throw new ServingCoreException(ErrorModel.toString(new ErrorModel(
      ErrorModel.CodeNotExistsPolicyWithName, s"No fragment of type $fragmentType with name $name")))
  }

}

object FragmentActor {

  case class PolicyWithFragments(policy: WorkflowModel, user: Option[LoggedUser])

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