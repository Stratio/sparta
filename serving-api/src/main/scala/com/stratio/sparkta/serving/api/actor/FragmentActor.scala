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

package com.stratio.sparkta.serving.api.actor

import java.util.UUID

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import com.stratio.sparkta.serving.api.actor.FragmentActor._
import com.stratio.sparkta.serving.core.AppConstant
import com.stratio.sparkta.serving.core.models.{FragmentElementModel, StreamingContextStatusEnum}
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.KeeperException.NoNodeException
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import org.json4s.native.Serialization._
import spray.httpx.Json4sJacksonSupport

import scala.collection.JavaConversions
import scala.util.Try

class FragmentActor(curatorFramework: CuratorFramework) extends Actor with Json4sJacksonSupport with SLF4JLogging {

  implicit val json4sJacksonFormats = DefaultFormats +
    new EnumNameSerializer(StreamingContextStatusEnum) +
    new JsoneyStringSerializer()

  override def receive: Receive = {
    case FindByTypeAndId(fragmentType, id) => findByTypeAndId(fragmentType, id)
    case Create(fragment) => create(fragment)
    case Update(fragment) => update(fragment)
    case DeleteByTypeAndId(fragmentType, id) => deleteByTypeAndId(fragmentType, id)
    case FindByType(fragmentType) => findByType(fragmentType)
  }

  def findByType(fragmentType: String): Unit =
    sender ! ResponseFragments(Try({
      val children = curatorFramework.getChildren.forPath(FragmentActor.generateFragmentPath(fragmentType))
      JavaConversions.asScalaBuffer(children).toList.map(element =>
        read[FragmentElementModel](new String(curatorFramework.getData.forPath(
          s"${FragmentActor.generateFragmentPath(fragmentType)}/$element")))).toSeq
    }).recover {
      case e: NoNodeException => Seq()
    })

  def findByTypeAndId(fragmentType: String, id: String): Unit =
    sender ! new ResponseFragment(Try({
      log.info(s"> Retrieving information for path: ${FragmentActor.generateFragmentPath(fragmentType)}/$id)")
      read[FragmentElementModel](new String(curatorFramework.getData.forPath(
        s"${FragmentActor.generateFragmentPath(fragmentType)}/$id")))
    }))

  def create(fragment: FragmentElementModel): Unit =
    sender ! Response(Try({
      val fragmentS = fragment.copy(id = Some(s"${UUID.randomUUID.toString}"))
      curatorFramework.create().creatingParentsIfNeeded().forPath(
        s"${FragmentActor.generateFragmentPath(
          fragmentS.fragmentType)}/${fragmentS.id.get}", write(fragmentS).getBytes())
    }))

  def update(fragment: FragmentElementModel): Unit =
    sender ! Response(Try({
      curatorFramework.setData.forPath(
        s"${FragmentActor.generateFragmentPath(fragment.fragmentType)}/${fragment.id.get}", write(fragment).getBytes)
    }))

  def deleteByTypeAndId(fragmentType: String, id: String): Unit =
    sender ! Response(Try({
      curatorFramework.delete().forPath(s"${FragmentActor.generateFragmentPath(fragmentType)}/$id")
    }))
}

object FragmentActor {

  case class Create(fragment: FragmentElementModel)

  case class Update(fragment: FragmentElementModel)

  case class FindByType(fragmentType: String)

  case class FindByTypeAndId(fragmentType: String, id: String)

  case class DeleteByTypeAndId(fragmentType: String, id: String)

  case class ResponseFragment(fragment: Try[FragmentElementModel])

  case class ResponseFragments(fragments: Try[Seq[FragmentElementModel]])

  case class Response(status: Try[Unit])

  def generateFragmentPath(fragmentType: String): String = {
    fragmentType match {
      case "input" => s"${AppConstant.BaseZKPath}/fragments/input"
      case "output" => s"${AppConstant.BaseZKPath}/fragments/output"
      case _ => throw new IllegalArgumentException("The fragment type must be input|output")
    }
  }
}