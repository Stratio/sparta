/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.dao

import javax.cache.Cache

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.utils.JdbcSlickUtils
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.lang.{IgniteFuture, IgniteInClosure}
import slick.ast.BaseTypedType
import slick.jdbc.meta.MTable
import slick.relational.RelationalProfile

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

//scalastyle:off
trait DaoUtils extends JdbcSlickUtils with SLF4JLogging with SpartaSerializer {

  implicit val exc = PostgresDaoFactory.pgExecutionContext

  import profile.api._

  type Id // The type of the PK
  type SpartaEntity // The type of the record inside the table
  type SpartaTable <: RelationalProfile#Table[SpartaEntity] // The table TYPE whose schema was defined through a case class

  def $id(table: SpartaTable): Rep[Id] // Method that returns the PrimaryKey from table

  val table: TableQuery[SpartaTable] // The table itself
  private lazy val tableName = table.baseTableRow.tableName

  val initializationOrder = 1

  // The method finds automagically a BaseTypedType[Id]  in the context...
  def baseTypedType: BaseTypedType[Id]

  // whereas the val provides the implicit transformation for mapping it in the def filterById
  private[dao] implicit lazy val btt: BaseTypedType[Id] = baseTypedType

  def filterById(id: Id): Query[SpartaTable, SpartaEntity, Seq] = {
    table.filter($id(_) === id)
  }

  // Schema creation

  def createSchema(): Unit = {
    dbSchemaName.foreach { schemaName =>
      val createSchemaSql = s"""create schema if not exists "$schemaName";"""
      Try(Await.result(db.run(sqlu"#$createSchemaSql"), AppConstant.DefaultApiTimeout seconds)) match {
        case Success(_) =>
          log.debug(s"Schema $schemaName created if not exists")
        case Failure(e) =>
          throw e
      }
    }
  }

  // Table creation

  def tableCreation(name: String, schemaName: Option[String]): Unit = {
    Try(Await.result(db.run(MTable.getTables), AppConstant.DefaultApiTimeout seconds)) match {
      case Success(result) =>
        val exists = result.exists(mTable => mTable.name.name == name && mTable.name.schema == schemaName)
        if (exists)
          log.info(s"Table $name already exists: skipping creation")
        else doCreateTable(name)
      case Failure(ex) =>
        log.error(ex.getLocalizedMessage, ex)
        throw ex
    }
  }

  def doCreateTable(name: String): Unit = {
    log.info(s"Creating table $name")
    Try(Await.result(db.run(txHandler(table.schema.create)), AppConstant.DefaultApiTimeout seconds)) match {
      case Success(_) =>
        log.debug(s"Table $name created correctly")
      case Failure(e) =>
        throw e
    }
  }

  def createTableSchema(): Unit = {
    tableCreation(tableName, dbSchemaName)
  }

  // Table data initialization

  def initializeData(): Unit = {}

  // CRUD Operations

  def count: Future[Int] =
    db.run(table.size.result)

  def upsert(item: SpartaEntity): Future[_] = {
    val dbioAction = DBIO.seq(upsertAction(item)).transactionally

    db.run(txHandler(dbioAction))
  }

  def upsertAction(item: SpartaEntity) = table.insertOrUpdate(item)

  def findAll(): Future[List[SpartaEntity]] =
    db.run(table.sortBy($id(_).desc).result).map(_.toList)

  def findByID(id: Id): Future[Option[SpartaEntity]] =
    db.run(filterById(id).result.headOption)

  def create(item: SpartaEntity): Future[_] =
    db.run(table += item)

  def createAndReturn(item: SpartaEntity): Future[SpartaEntity] =
    db.run(table returning table += item)

  def createAndReturnList(items: Seq[SpartaEntity]): Future[_] = {
    val dbioAction = (table returning table ++= items).transactionally

    db.run(dbioAction)
  }

  def deleteByID(id: Id): Future[_] = {
    val dbioAction = DBIO.seq(deleteByIDAction(id)).transactionally
    db.run(txHandler(dbioAction))
  }

  def deleteByIDAction(id: Id) = filterById(id).delete

  def createList(items: Seq[SpartaEntity]): Future[_] = {
    val dbioAction = DBIO.seq(table ++= items).transactionally

    db.run(dbioAction)
  }

  def upsertList(items: Seq[SpartaEntity]): Future[_] = {
    val itemsToUpsert = items.map(item => table.insertOrUpdate(item))
    val dbioAction = DBIO.sequence(itemsToUpsert).transactionally

    db.run(dbioAction)
  }

  def deleteList(items: Seq[Id]): Future[_] = {
    val dbioAction = DBIO.seq(table.filter($id(_) inSet items).delete).transactionally

    db.run(dbioAction)
  }

  def deleteAll(): Future[Unit] =
    db.run(txHandler(DBIO.seq(table.delete).transactionally))

  def executeTxDBIO(dbioActions: DBIOAction[Unit, NoStream, Effect.All with Effect.Transactional]*): Future[Unit] = {
    db.run(txHandler(DBIO.seq(dbioActions: _*).transactionally))
  }

  def txHandler(dbioAction: DBIOAction[Unit, NoStream, Effect.All with Effect.Transactional]) = {
    dbioAction.asTry.flatMap {
      case Success(s) => DBIO.successful(s)
      case Failure(e) => DBIO.failed(e)
    }
  }

  def txHandler(dbioAction: Seq[DBIOAction[Unit, NoStream, Effect.All with Effect.Transactional]]) = {
    dbioAction.map(action => action.asTry.flatMap {
      case Success(s) => DBIO.successful(s)
      case Failure(e) => DBIO.failed(e)
    })
  }

  /** Cache implicits methods */

  val cacheEnabled = Try(SpartaConfig.getIgniteConfig().get.getBoolean(AppConstant.IgniteEnabled)).getOrElse(false)

  implicit val cache: IgniteCache[String, SpartaEntity]

  def initialCacheLoad() =
    if (cacheEnabled)
      findAll().map(list =>
        cache.putAll(list.map(element => (getSpartaEntityId(element), element)).toMap[String, SpartaEntity].asJava)
      )

  def cacheById(id: String)(f: => Future[SpartaEntity]): Future[SpartaEntity] = cache.getAsync(id).toScalaFuture.recoverWith { case _ => f }

  //Method to be override in every daos
  def getSpartaEntityId(entity: SpartaEntity): String = "N/A"

  //Generic predicate execution over ignite cache, based in scanQuery defined in every method that performs queries over cache values
  def predicateHead(s: ScanQuery[String, SpartaEntity])(ps: => Future[SpartaEntity]) = {
    Future(cache.query(s).iterator().asScala.map(_.getValue).toList.headOption.getOrElse(throw new RuntimeException("Not found in cache")))
      .recoverWith { case _ => ps }
  }

  def predicateList(s: ScanQuery[String, SpartaEntity])(ps: => Future[Seq[SpartaEntity]]) = {
    Future(cache.query(s).iterator().asScala.map(_.getValue).toList)
      .recoverWith { case _ => ps }
  }

  implicit class CacheIterator[K, V](iterator: java.util.Iterator[Cache.Entry[K, V]]) {

    import scala.collection.JavaConverters._

    def allAsScala() = Future(iterator.asScala.map(_.getValue).toList)
  }

  /** *
    * Implicit class used in daos on insert/upsert or delete, for refresh cache entry
    */

  implicit class FutureToIgnite[A](daoFuture: Future[A]) {

    def cached(): Future[A] = {
      if (cacheEnabled) {
        for {
          daoResult <- daoFuture
        } yield {
          Try {
            val result = daoResult match {
              case vectorResult if vectorResult.isInstanceOf[Vector[A]] => vectorResult.asInstanceOf[Vector[A]].toList
              case singleResult => Seq(singleResult)
            }
            val entities = result.flatten(Seq(_).asInstanceOf[List[SpartaEntity]]).map { entity =>
              getSpartaEntityId(entity) -> entity
            }.toMap
            cache.putAll(entities.asJava)
          } match {
            case Success(_) => daoResult
            case Failure(e) => throw new Exception(s"Error updating cache keys with error: ${ExceptionHelper.toPrintableException(e)}", e)
          }
        }
      } else daoFuture
    }

    def removeInCache(id: String*): Future[A] = {
      if (cacheEnabled) {
        for {
          result <- daoFuture
        } yield {
          Try(cache.removeAll(id.toSet.asJava)) match {
            case Success(_) => result
            case Failure(e) => throw new Exception(s"Error deleting cache keys with error: ${ExceptionHelper.toPrintableException(e)}", e)
          }
        }
      } else daoFuture
    }
  }

  /**
    * Ignite async methods return java IgniteFuture, this class convert to Scala future
    */
  implicit class IgniteFutureUtils[T](igniteFuture: IgniteFuture[T]) {

    def toScalaFuture() = {
      val promise = Promise[T]()
      igniteFuture.listen(new IgniteInClosure[IgniteFuture[T]] {
        override def apply(e: IgniteFuture[T]): Unit =
          promise.tryComplete(Try {
            Option(e.get).getOrElse(throw new RuntimeException("Not found in cache"))
          })
      })
      promise.future
    }
  }

}