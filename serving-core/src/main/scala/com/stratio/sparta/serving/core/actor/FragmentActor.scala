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

import java.util.UUID

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.actor.FragmentActor._
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.{ErrorModel, FragmentElementModel, SpartaSerializer}
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.KeeperException.NoNodeException
import org.json4s.jackson.Serialization.{read, write}
import spray.httpx.Json4sJacksonSupport

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

class FragmentActor(curatorFramework: CuratorFramework)
  extends Actor
    with Json4sJacksonSupport
    with SLF4JLogging
    with SpartaSerializer {

  //scalastyle:off
  override def receive: Receive = {
    case FindAllFragments() => findAll
    case FindByType(fragmentType) => findByType(fragmentType)
    case FindByTypeAndId(fragmentType, id) => findByTypeAndId(fragmentType, id)
    case FindByTypeAndName(fragmentType, name) => findByTypeAndName(fragmentType, name.toLowerCase())
    case DeleteAllFragments() => deleteAllFragments()
    case DeleteByType(fragmentType) => deleteByType(fragmentType)
    case DeleteByTypeAndId(fragmentType, id) => deleteByTypeAndId(fragmentType, id)
    case DeleteByTypeAndName(fragmentType, name) => deleteByTypeAndName(fragmentType, name)
    case Create(fragment) => create(fragment)
    case Update(fragment) => update(fragment)
  }

  //scalastyle:on

  def findAll: Unit =
    sender ! ResponseFragments(
      Try {
        findAllFragments
      }.recover {
        case e: NoNodeException =>
          throw new ServingCoreException(ErrorModel.toString(
            new ErrorModel(ErrorModel.CodeErrorGettingAllFragments, s"Error while getting all fragments.")
          ))
      })

  def findByType(fragmentType: String): Unit = {
    val fragments = ResponseFragments(
      Try {
        val children = curatorFramework.getChildren.forPath(FragmentActor.fragmentPathType(fragmentType))
        JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[FragmentElementModel](new String(curatorFramework.getData.forPath(
            s"${FragmentActor.fragmentPathType(fragmentType)}/$element")))
        )
      }.recover {
        case e: NoNodeException => Seq.empty[FragmentElementModel]
      })
    sender ! fragments
  }

  def findByTypeAndId(fragmentType: String, id: String): Unit =
    sender ! new ResponseFragment(
      Try {
        log.info(s"> Retrieving information for path: ${FragmentActor.fragmentPathType(fragmentType)}/$id)")
        read[FragmentElementModel](new String(curatorFramework.getData.forPath(
          s"${FragmentActor.fragmentPathType(fragmentType)}/$id")))
      }.recover {
        case e: NoNodeException =>
          throw new ServingCoreException(ErrorModel.toString(
            new ErrorModel(ErrorModel.CodeNotExistsFragmentWithId, s"No fragment of type $fragmentType with id $id.")
          ))
      })

  def findByTypeAndName(fragmentType: String, name: String): Unit =
    sender ! ResponseFragment(
      Try {
        val children = curatorFramework.getChildren.forPath(FragmentActor.fragmentPathType(fragmentType))
        JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[FragmentElementModel](new String(curatorFramework.getData.forPath(
            s"${FragmentActor.fragmentPathType(fragmentType)}/$element"))))
          .filter(fragment => fragment.name == name).head
      }.recover {
        case e: NoNodeException =>
          throw new ServingCoreException(ErrorModel.toString(
            new ErrorModel(ErrorModel.CodeNotExistsFragmentWithName,
              s"No fragment of type $fragmentType with name $name.")
          ))
        case e: NoSuchElementException =>
          throw new ServingCoreException(ErrorModel.toString(
            new ErrorModel(ErrorModel.CodeNotExistsPolicyWithName,
              s"No fragment of type $fragmentType with name $name")
          ))
      })

  def createNewFragment(fragment: FragmentElementModel): FragmentElementModel = {
    val newFragment = fragment.copy(
      id = Option(UUID.randomUUID.toString),
      name = fragment.name.toLowerCase
    )
    curatorFramework.create().creatingParentsIfNeeded().forPath(
      s"${FragmentActor.fragmentPathType(newFragment.fragmentType)}/${newFragment.id.get}",
      write(newFragment).getBytes()
    )

    newFragment
  }

  def create(fragment: FragmentElementModel): Unit =
    sender ! ResponseFragment(Try {
      if (existsByTypeAndName(fragment.fragmentType, fragment.name.toLowerCase))
        getExistingFragment(fragment).getOrElse(createNewFragment(fragment))
      else createNewFragment(fragment)
    })

  def getExistingFragment(fragment: FragmentElementModel): Option[FragmentElementModel] =
    listByPath(fragmentPathType(fragment.fragmentType))
      .dropWhile(currentFragment => !fragment.equals(currentFragment))
      .headOption
      .orElse(throw new ServingCoreException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeExistsFragmentWithName,
          s"Fragment of type ${fragment.fragmentType} with name ${fragment.name} already exists.")))
      )

  def update(fragment: FragmentElementModel): Unit =
    sender ! Response(
      Try {
        if (existsByTypeAndName(fragment.fragmentType, fragment.name.toLowerCase, fragment.id)) {
          throw new ServingCoreException(ErrorModel.toString(
            new ErrorModel(ErrorModel.CodeExistsFragmentWithName,
              s"Fragment of type ${fragment.fragmentType} with name ${fragment.name} exists.")
          ))
        }
        val fragmentS = fragment.copy(name = fragment.name.toLowerCase)

        curatorFramework.setData().forPath(
          s"${FragmentActor.fragmentPathType(fragmentS.fragmentType)}/${fragment.id.get}", write(fragmentS).getBytes)
      }.recover {
        case e: NoNodeException =>
          throw new ServingCoreException(ErrorModel.toString(
            new ErrorModel(ErrorModel.CodeNotExistsFragmentWithId,
              s"No fragment of type ${fragment.fragmentType} with id ${fragment.id.get}.")
          ))
      })

  def deleteAllFragments(): Unit =
    sender ! ResponseFragments(
      Try {
        val fragmentsFound = findAllFragments

        fragmentsFound.foreach(fragment => {
          val id = fragment.id.getOrElse {
            throw new ServingCoreException(ErrorModel.toString(
              new ErrorModel(ErrorModel.CodeErrorDeletingAllFragments, s"Fragment without id: ${fragment.name}.")
            ))
          }

          curatorFramework.delete().forPath(s"${FragmentActor.fragmentPathType(fragment.fragmentType)}/$id")
        })
        fragmentsFound
      }.recover {
        case e: NoNodeException =>
          throw new ServingCoreException(ErrorModel.toString(
            new ErrorModel(
              ErrorModel.CodeErrorDeletingAllFragments,
              s"Error while deleting all fragments"
            )
          ))
      })

  def deleteByType(fragmentType: String): Unit =
    sender ! Response(
      Try {

        val children = curatorFramework.getChildren.forPath(FragmentActor.fragmentPathType(fragmentType))
        val fragmentsFound = JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[FragmentElementModel](new String(curatorFramework.getData.forPath(
            s"${FragmentActor.fragmentPathType(fragmentType)}/$element")))
        )
        fragmentsFound.foreach(fragment => {
          val id = fragment.id.getOrElse {
            throw new ServingCoreException(ErrorModel.toString(
              new ErrorModel(ErrorModel.CodeNotExistsFragmentWithType, s"Fragment without id: ${fragment.name}.")
            ))
          }

          curatorFramework.delete().forPath(s"${FragmentActor.fragmentPathType(fragmentType)}/$id")
        })
      }.recover {
        case e: NoNodeException =>
          throw new ServingCoreException(ErrorModel.toString(
            new ErrorModel(
              ErrorModel.CodeNotExistsFragmentWithType,
              s"No fragment of type $fragmentType."
            )
          ))
      })

  def deleteByTypeAndId(fragmentType: String, id: String): Unit =
    sender ! Response(
      Try {
        curatorFramework.delete().forPath(s"${FragmentActor.fragmentPathType(fragmentType)}/$id")
      }.recover {
        case e: NoNodeException =>
          throw new ServingCoreException(ErrorModel.toString(
            new ErrorModel(ErrorModel.CodeNotExistsFragmentWithId, s"No fragment of type $fragmentType with id $id.")
          ))
      })

  def deleteByTypeAndName(fragmentType: String, name: String): Unit =
    sender ! Response(
      Try {
        val children = curatorFramework.getChildren.forPath(FragmentActor.fragmentPathType(fragmentType))
        val fragmentFound = JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[FragmentElementModel](new String(curatorFramework.getData.forPath(
            s"${FragmentActor.fragmentPathType(fragmentType)}/$element"))))
          .find(fragment => fragment.name == name)

        if (fragmentFound.isDefined && fragmentFound.get.id.isDefined) {
          curatorFramework.delete()
            .forPath(s"${FragmentActor.fragmentPathType(fragmentType)}/${fragmentFound.get.id.get}")
        } else {
          throw new ServingCoreException(ErrorModel.toString(
            new ErrorModel(
              ErrorModel.CodeNotExistsFragmentWithName,
              s"Fragment without id: $name."
            )
          ))
        }
      }.recover {
        case e: NoNodeException =>
          throw new ServingCoreException(ErrorModel.toString(
            new ErrorModel(
              ErrorModel.CodeNotExistsFragmentWithId, s"No fragment of type $fragmentType with name $name.")
          ))
      })

  private def existsByTypeAndName(fragmentType: String, name: String, id: Option[String] = None): Boolean = {
    Try {
      val fragmentLocation = fragmentPathType(fragmentType)
      if (CuratorFactoryHolder.existsPath(fragmentLocation)) {
        val children = curatorFramework.getChildren.forPath(fragmentLocation)
        JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[FragmentElementModel](new String(curatorFramework.getData.forPath(s"$fragmentLocation/$element")))
        ).exists(fragment =>
          if (id.isDefined) fragment.name == name && fragment.id.get != id.get else fragment.name == name
        )
      } else false
    } match {
      case Success(result) =>
        result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        false
    }
  }

  private def listByPath(fragmentLocation: String): List[FragmentElementModel] = {
    Try {
      if (CuratorFactoryHolder.existsPath(fragmentLocation)) {
        val children = curatorFramework.getChildren.forPath(fragmentLocation)
        JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[FragmentElementModel](new String(curatorFramework.getData.forPath(s"$fragmentLocation/$element")))
        )
      } else List.empty[FragmentElementModel]
    }.getOrElse(List.empty[FragmentElementModel])
  }

  private def findAllFragments: List[FragmentElementModel] = {
    val children = curatorFramework.getChildren.forPath(s"${AppConstant.FragmentsPath}")

    JavaConversions.asScalaBuffer(children).toList.flatMap(fragmentType => {
      val fragmentId = curatorFramework.getChildren.forPath(FragmentActor.fragmentPathType(fragmentType))

      JavaConversions.asScalaBuffer(fragmentId).toList.map(element =>
        read[FragmentElementModel](new String(curatorFramework.getData.forPath(
          s"${FragmentActor.fragmentPathType(fragmentType)}/$element")))
      )
    })
  }
}

object FragmentActor {

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

  def fragmentPathType(fragmentType: String): String = {
    fragmentType match {
      case "input" => s"${AppConstant.FragmentsPath}/input"
      case "output" => s"${AppConstant.FragmentsPath}/output"
      case _ => throw new IllegalArgumentException("The fragment type must be input|output")
    }
  }
}