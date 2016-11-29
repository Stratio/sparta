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
package com.stratio.sparta.serving.api.actor

import java.io.{File, InputStreamReader}
import java.net.{URI, URL}

import scala.util.Success
import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import org.apache.commons.io.IOUtils
import org.json4s.jackson.Serialization._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.policy.fragment.TemplateModel
import com.stratio.sparta.serving.core.models.{ErrorModel, SpartaSerializer}

@RunWith(classOf[JUnitRunner])
class TemplateActorTest extends TestKit(ActorSystem("TemplateActorSpec"))
with DefaultTimeout
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with MockitoSugar with SpartaSerializer {

  trait TestData {

    val templateModelJson =
      """
       {
        |  "name": "templateName",
        |  "modelType": "input",
        |  "description": {
        |    "short": "short"
        |  },
        |  "icon": {
        |    "url": "logo.png"
        |  },
        |  "properties": [
        |    {
        |      "propertyId": "templatePropertyId",
        |      "propertyName": "templatePropertyName",
        |      "propertyType": "templatePropertyType",
        |      "regexp": "*",
        |      "default": "localhost:2181",
        |      "required": true,
        |      "tooltip": ""
        |    }
        |  ]
        |}
      """.stripMargin

    val ServingException = new ServingCoreException(
      ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsTemplateWithName,
          "No template of type input  with name templateName.json")))

    val templateModel = read[TemplateModel](templateModelJson)

    val templateActor = system.actorOf(Props(new TemplateActor() {
      override protected def getResource(resource: String): URL =
        new File("file.json").toURI.toURL

      override protected def getInputStreamFromResource(resource: String): InputStreamReader =
        new InputStreamReader(IOUtils.toInputStream(templateModelJson))

      override protected def getFilesFromURI(uri: URI): Seq[File] =
        Seq(new File("file.json"))
    }))

    val wrongTemplateActor = system.actorOf(Props(new TemplateActor() {
      override protected def getInputStreamFromResource(resource: String): InputStreamReader =
        throw new NullPointerException("expected null pointer exception")

      override protected def getResource(resource: String): URL =
        throw new NullPointerException("expected null pointer exception")
    }))
  }

  override def afterAll: Unit = shutdown()

  "TemplateActor" must {

    // XXX findByType
    "findByType: returns all templates by type" in new TestData {
      templateActor ! TemplateActor.FindByType("input")

      expectMsg(new TemplateActor.ResponseTemplates(Success(Seq(templateModel))))
    }

    "findByType: return a empty list when a null pointer exception is thrown" in new TestData {
      wrongTemplateActor ! TemplateActor.FindByType("input")

      expectMsg(new TemplateActor.ResponseTemplates(Success(Seq())))
    }

    // XXX findByTypeAndName
    "findByTypeAndName: returns all templates by type and name" in new TestData {
      templateActor ! TemplateActor.FindByTypeAndName("input", "templateName")

      expectMsg(new TemplateActor.ResponseTemplate(Success(templateModel)))
    }

    "findByTypeAndName: return a empty list when a null pointer exception is thrown" in new TestData {
      wrongTemplateActor ! TemplateActor.FindByTypeAndName("input", "templateName")

      expectMsgAnyClassOf(classOf[TemplateActor.ResponseTemplate])
    }
  }
}
