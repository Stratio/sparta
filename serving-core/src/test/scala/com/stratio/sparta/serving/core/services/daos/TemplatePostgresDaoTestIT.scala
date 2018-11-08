/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.daos

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.dao.TemplateDao
import com.stratio.sparta.serving.core.factory.PostgresFactory
import com.stratio.sparta.serving.core.models.workflow.{TemplateElement, TemplateType}
import com.stratio.sparta.serving.core.services.dao.TemplatePostgresDao
import com.stratio.sparta.serving.core.utils.JdbcSlickHelper
import com.typesafe.config.Config
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.time.{Milliseconds, Span}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import slick.jdbc.PostgresProfile

@RunWith(classOf[JUnitRunner])
class TemplatePostgresDaoTestIT extends DAOConfiguration
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll
  with SLF4JLogging
  with JdbcSlickHelper
  with ScalaFutures {

  val profile = PostgresProfile
  import profile.api._
  var db1: profile.api.Database = _
  val queryTimeout: Int = 2000
  val postgresConf: Config = SpartaConfig.getPostgresConfig().get

  val templateElement_1 = TemplateElement(
    templateType = TemplateType.InputValue,
    name = "templateInput",
    description = Some("Input template example"),
    className = "TestInputStep",
    classPrettyName = "Test"
  )

  val templateElement_2 = TemplateElement(
    templateType = TemplateType.TransformationValue,
    name = "templateTransformation",
    description = Some("Transformation template example"),
    className = "CastingTransformStep",
    classPrettyName = "Casting"
  )

  val templateElement_3 = TemplateElement(
    templateType = TemplateType.OutputValue,
    name = "templateOutput",
    description = Some("Output template example 1"),
    className = "ParquetOutputStep",
    classPrettyName = "Parquet"
  )

  val templateElement_4 = TemplateElement(
    templateType = TemplateType.OutputValue,
    name = "templateOutput2",
    description = Some("Output template example 2"),
    className = "PostgresOutputStep",
    classPrettyName = "Postgres"
  )

  val templateElement_5 = TemplateElement(
    templateType = TemplateType.InputValue,
    name = "templateInput2",
    description = Some("Input template example 2"),
    className = "AvroInputStep",
    classPrettyName = "Avro"
  )

  val templatePostgresDao = new TemplatePostgresDao()

  PostgresFactory.invokeInitializationMethods()
  PostgresFactory.invokeInitializationDataMethods()

  trait TemplateDaoTrait extends TemplateDao {

    override val profile = PostgresProfile
    override val db: profile.api.Database = db1
  }

  override def beforeAll(): Unit = {

    db1 = Database.forConfig("", properties)
  }

  "A template or templates " must {
    "be created" in new TemplateDaoTrait {

      whenReady(templatePostgresDao.createTemplate(templateElement_1), timeout(Span(queryTimeout, Milliseconds))) {
        _ => {
          val result = db.run(table.filter(_.name === templateElement_1.name).result).map(_.toList).futureValue

          result.head.name shouldBe templateElement_1.name

          if (result.nonEmpty) {
            templatePostgresDao.createTemplate(templateElement_2)
            templatePostgresDao.createTemplate(templateElement_3)
            templatePostgresDao.createTemplate(templateElement_4)
            templatePostgresDao.createTemplate(templateElement_5)
          }
        }
      }
    }

    "be found and returned if they have the type specified" in new TemplateDaoTrait {
      whenReady(templatePostgresDao.findByType(TemplateType.InputValue)) {
        result => {
          result.head.name shouldBe templateElement_1.name
          result.size shouldBe 2
        }
      }
    }

    "be found and returned if its name and type matches the ones specified in the method" in new TemplateDaoTrait {

      whenReady(templatePostgresDao.findByTypeAndName(TemplateType.TransformationValue, templateElement_2.name)) {
        result => {
          result.name shouldBe templateElement_2.name
        }
      }
    }

    "be found and returned if its id and type matches the ones specified in the method" in new TemplateDaoTrait {

      val templateId = templatePostgresDao.findByTypeAndName(templateElement_2.templateType,
        templateElement_2.name).futureValue.id
      whenReady(templatePostgresDao.findByTypeAndId(TemplateType.TransformationValue, templateId.get)) {
        result =>
          result.name shouldBe templateElement_2.name
      }
    }

    "be returned if the findAll method is called" in new TemplateDaoTrait {

      val result = templatePostgresDao.findAllTemplates().futureValue

      result.size shouldBe 5
    }

    "be updated correctly if a valid template is passed" in new TemplateDaoTrait {

      val templateElement_1_modified = TemplateElement(
        id = templatePostgresDao.findByTypeAndName(templateElement_1.templateType,
          templateElement_1.name).futureValue.id,
        templateType = TemplateType.InputValue,
        name = "templateInputModified",
        description = Some("Input template example has been changed"),
        className = "TestInputStep",
        classPrettyName = "Test"
      )

      whenReady(templatePostgresDao.updateTemplate(templateElement_1_modified), timeout(Span(queryTimeout, Milliseconds))) {
        _ =>
        {
          whenReady(templatePostgresDao.findByTypeAndName(templateElement_1_modified.templateType,
            templateElement_1_modified.name)) {
            res =>
              res.name shouldBe templateElement_1_modified.name
              res.description shouldBe templateElement_1_modified.description
          }
        }
      }
    }

    "be deleted if its type and id matches with the ones specified in the method" in new TemplateDaoTrait {

      val templateID = templatePostgresDao.findByTypeAndName(templateElement_2.templateType,
        templateElement_2.name).futureValue.id
      whenReady(templatePostgresDao.deleteByTypeAndId(TemplateType.TransformationValue, templateID.get),
        timeout(Span(queryTimeout, Milliseconds))) {
        res =>
          if (res) {
            whenReady(db.run(table.filter(_.templateType === templateElement_2.templateType).result).map(_.toList),
              timeout(Span(queryTimeout, Milliseconds))) {
              finalRes => finalRes.isEmpty shouldBe true
            }
          }
      }
    }

    "be deleted if its type and name matches with the ones specified in the method" in new TemplateDaoTrait {

      whenReady(templatePostgresDao.deleteByTypeAndName(TemplateType.OutputValue, templateElement_3.name),
        timeout(Span(queryTimeout, Milliseconds))) {
        res =>
          if (res) {
            whenReady(db.run(table.filter(_.name === templateElement_3.name).result).map(_.toList),
              timeout(Span(queryTimeout, Milliseconds))) {
              finalRes => finalRes.isEmpty shouldBe true
            }
          }
      }
    }

    "be deleted if its type matches with the one specified in the method" in new TemplateDaoTrait {
      whenReady(templatePostgresDao.deleteByType(TemplateType.OutputValue),
        timeout(Span(queryTimeout, Milliseconds))) {
        res =>
          if (res) {
            whenReady(db.run(table.filter(_.templateType === TemplateType.OutputValue).result).map(_.toList),
              timeout(Span(queryTimeout, Milliseconds))) {
              finalRes => finalRes.isEmpty shouldBe true
            }
          }
      }
    }
  }

  "All templates" must {
    "be deleted disregarding its type" in new TemplateDaoTrait {
      whenReady(templatePostgresDao.deleteAllTemplates(),
        timeout(Span(queryTimeout, Milliseconds))) { res =>
        if (res) {
          whenReady(db.run(table.result).map(_.toList)) {
            query => query.isEmpty shouldBe true
          }
        }
      }
    }
  }

  override def afterAll(): Unit = {
    db1.close()
  }
}