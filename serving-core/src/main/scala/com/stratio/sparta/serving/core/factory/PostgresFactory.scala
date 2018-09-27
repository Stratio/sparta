/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.factory

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.serving.core.constants.AppConstant

import scala.collection.JavaConversions._
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import com.stratio.sparta.serving.core.dao.DaoUtils

import scala.concurrent.blocking
import scala.util.{Failure, Success, Try}

object PostgresFactory extends SLF4JLogging {

  val schemaMethod = "createSchema"
  val tableSchemaMethod = "createTableSchema"
  val initializeMethod = "initializeData"

  def invokeInitializationMethods(): Unit = {
    Try(invokeDaoMethod(AppConstant.PostgresDaos, schemaMethod)) match {
      case Success(_) =>
        log.info("Sparta Postgres schemas created correctly")
      case Failure(e) =>
        log.error(s"Impossible to initialize Sparta Postgres schemas, shutting down Application" +
          s" with exception ${ExceptionHelper.toPrintableException(e)}", e)
        System.exit(-1)
    }


    Try(invokeDaoMethod(AppConstant.PostgresDaos, tableSchemaMethod)) match {
      case Success(_) =>
        log.info("Sparta Postgres table schemas created correctly")
      case Failure(e) =>
        log.error(s"Impossible to initialize Sparta Postgres table schemas, shutting down Application" +
          s" with exception ${ExceptionHelper.toPrintableException(e)}", e)
        System.exit(-1)
    }


    Try(invokeDaoMethod(AppConstant.PostgresDaos, initializeMethod)) match {
      case Success(_) =>
        log.info("Sparta Postgres data created correctly")
      case Failure(e) =>
        log.error(s"Impossible to initialize Sparta Postgres data, shutting down Application" +
          s" with exception ${ExceptionHelper.toPrintableException(e)}", e)
        System.exit(-1)
    }

  }

  private def invokeDaoMethod(packageDao: String, method: String): Unit = {
    val reflections = new Reflections(
      ClasspathHelper.forPackage(packageDao),
      new SubTypesScanner()
    )
    reflections.getSubTypesOf(classOf[DaoUtils]).filterNot(_.isInterface)
      .foreach { daoClazz =>
        val postgresDao = daoClazz.newInstance()
        daoClazz.getMethod(method).invoke(postgresDao)
      }
  }
}
