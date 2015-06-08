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

package com.stratio.sparkta.driver.actor

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.driver.actor.FragmentSupervisorActor_response_fragments
import com.stratio.sparkta.driver.dto.FragmentElementDto
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import org.apache.curator.framework.CuratorFramework
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import org.json4s.native.Serialization._
import spray.httpx.Json4sJacksonSupport

import scala.collection.JavaConversions
import scala.util.Try

/**
 * List of all possible akka messages used to manage fragments.
 */
case class FragmentSupervisorActor_create(fragment: FragmentElementDto)
case class FragmentSupervisorActor_findAllByType(fragmentType: String)
case class FragmentSupervisorActor_findByTypeAndName(fragmentType: String, name: String)
case class FragmentSupervisorActor_deleteByTypeAndName(fragmentType: String, name: String)
case class FragmentSupervisorActor_response_fragment(fragment: Try[FragmentElementDto])
case class FragmentSupervisorActor_response_fragments(fragments: Try[Seq[FragmentElementDto]])
case class FragmentSupervisorActor_response(status: Try[Unit])

/**
 * Implementation of supported CRUD operations over ZK needed to manage Fragments.
 * @author anistal
 */
class FragmentActor(curatorFramework: CuratorFramework) extends Actor with Json4sJacksonSupport with SLF4JLogging {

  implicit val json4sJacksonFormats = DefaultFormats +
    new EnumNameSerializer(StreamingContextStatusEnum) +
    new JsoneyStringSerializer()

  override def receive: Receive = {
    case FragmentSupervisorActor_findByTypeAndName(fragmentType, name) => doDetail(fragmentType, name)
    case FragmentSupervisorActor_create(fragment) => doCreate(fragment)
    case FragmentSupervisorActor_deleteByTypeAndName(fragmentType, name) => doDeleteByTypeAndName(fragmentType, name)
    case FragmentSupervisorActor_findAllByType(fragmentType) => doFindAllByType(fragmentType)
  }

  def doFindAllByType(fragmentType: String): Unit =
    sender ! FragmentSupervisorActor_response_fragments(Try({
      val children = curatorFramework.getChildren.forPath(FragmentActor.generateFragmentPath(fragmentType))
      JavaConversions.asScalaBuffer(children).toList.map(element =>
        read[FragmentElementDto](new String(curatorFramework.getData.forPath(
          FragmentActor.generateFragmentPath(fragmentType) +
            FragmentActor.PathSeparator + element).asInstanceOf[Array[Byte]]))).toSeq
    }))

  def doDetail(fragmentType: String, name: String): Unit =
    sender ! new FragmentSupervisorActor_response_fragment(Try({
      read[FragmentElementDto](new String(curatorFramework.getData.forPath(
        FragmentActor.generateFragmentPath(fragmentType) + FragmentActor.PathSeparator + name)
        .asInstanceOf[Array[Byte]]))
    }))
    

  def doCreate(fragment: FragmentElementDto): Unit =
    sender ! FragmentSupervisorActor_response(Try({
      curatorFramework.create().creatingParentsIfNeeded().forPath(
        FragmentActor.generateFragmentPath(fragment.fragmentType)
          + FragmentActor.PathSeparator + fragment.name, write(fragment).getBytes())
    }))

  def doDeleteByTypeAndName(fragmentType: String, name: String): Unit =
    sender ! FragmentSupervisorActor_response(Try({
      curatorFramework.delete().forPath(
        FragmentActor.generateFragmentPath(fragmentType) + FragmentActor.PathSeparator + name)
    }))
}

object FragmentActor {

  // TODO (anistal) This should be in a config file.
  val BaseZKPath: String = "/sparkta/policies"
  val PathSeparator: String = "/"

  def generateFragmentPath(fragmentType: String): String = {
    fragmentType match {
      case "input" => BaseZKPath + PathSeparator + "input"
      case "output" => BaseZKPath + PathSeparator + "output"
      case _ => throw new IllegalArgumentException("The fragment type must be input|output")
    }
  }
}