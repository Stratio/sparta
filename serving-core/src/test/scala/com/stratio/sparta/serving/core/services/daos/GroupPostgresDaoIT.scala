/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.daos

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.dao.GroupDao
import com.stratio.sparta.serving.core.factory.PostgresFactory
import com.stratio.sparta.serving.core.models.workflow.Group
import com.stratio.sparta.serving.core.services.dao.GroupPostgresDao
import com.stratio.sparta.serving.core.utils.JdbcSlickHelper
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.time.{Milliseconds, Span}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import slick.jdbc.PostgresProfile

import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class GroupPostgresDaoIT extends DAOConfiguration
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll
  with SLF4JLogging
  with JdbcSlickHelper
  with ScalaFutures {

  val profile = PostgresProfile

  import profile.api._

  var db1: profile.api.Database = _

  val groupTest1 = Group(Some("id1"), "/home/testgroup")
  val groupTest2 = Group(Some("id2"), "/home/testgroup2")
  val groupTest3 = Group(Some("id3"), "/home/testgroup/child")
  val groupTest1Updated = Group(Some("id1"), "/home/groupmodified")

  val groupPostgresDao = new GroupPostgresDao()

  trait GroupDaoTrait extends GroupDao {

    override val profile = PostgresProfile
    override val db: profile.api.Database = db1
  }

  override def beforeAll(): Unit = {

    db1 = Database.forConfig("", properties)

    val actions = DBIO.seq(
      sqlu"DROP TABLE IF EXISTS spartatest.groups CASCADE;"
    )
    Await.result(db1.run(actions), queryTimeout millis)

    PostgresFactory.invokeInitializationMethods()
    Thread.sleep(3000)
    PostgresFactory.invokeInitializationDataMethods()
    Thread.sleep(1000)
  }

  "An Sparta group" must {
    "be created in the Group table" in new GroupDaoTrait {

      import profile.api._

      groupPostgresDao.createFromGroup(groupTest2)

      whenReady(groupPostgresDao.createFromGroup(groupTest1), timeout(Span(queryTimeout, Milliseconds))) { _ =>
        whenReady(db.run(table.filter(_.groupId === groupTest1.id.get).result).map(_.toList)) { result =>
          result.size shouldBe 1
          groupPostgresDao.createFromGroup(groupTest3)
        }
      }
    }

    "be updated along with its child group in the Group table" in new GroupDaoTrait {

      import profile.api._

      whenReady(groupPostgresDao.update(groupTest1Updated), timeout(Span(queryTimeout, Milliseconds))) {
        _ =>
          whenReady(db.run(table.filter(_.groupId === groupTest1Updated.id.get).result).map(_.toList)) { result =>
            result.head.name shouldBe "/home/groupmodified"
          }

          whenReady(db.run(table.filter(_.groupId === groupTest3.id.get).result).map(_.toList)) { result =>
            result.head.name shouldBe "/home/groupmodified/child"
          }
      }
    }

    "be found if an existing id is given" in new GroupDaoTrait {

      whenReady(groupPostgresDao.findGroupByName("/home/groupmodified"), timeout(Span(queryTimeout, Milliseconds))) { result =>
        result.name shouldBe "/home/groupmodified"
      }
    }

    "be found if an existing name is given" in new GroupDaoTrait {
      whenReady(groupPostgresDao.findGroupById("id1"), timeout(Span(queryTimeout, Milliseconds))) { result =>
        result.id.get shouldBe "id1"
      }
    }

    "be deleted along with its child groups if an existing id is given" in new GroupDaoTrait {

      groupPostgresDao.deleteById("id1").futureValue shouldBe true
    }

    "be deleted along with its child groups if an existing name is given" in new GroupDaoTrait {
      groupPostgresDao.deleteByName("/home/testgroup2").futureValue shouldBe true
    }
  }

  "All groups" must {
    "be deleted" in new GroupDaoTrait {

      import profile.api._

      val result = groupPostgresDao.deleteAllGroups().futureValue
      if (result)
        whenReady(db.run(table.result).map(_.toList), timeout(Span(queryTimeout, Milliseconds))) { result =>
          result.size shouldBe 1
        }

      assert(result === true)
    }
  }

  override def afterAll(): Unit = {

    val actions = DBIO.seq(sqlu"DROP TABLE IF EXISTS spartatest.groups CASCADE;")
    Await.result(db1.run(actions), queryTimeout millis)

    db1.close()
  }
}