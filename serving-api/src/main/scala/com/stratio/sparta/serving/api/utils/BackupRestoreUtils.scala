/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.utils

import java.io.File
import java.nio.file.{Files, Paths}
import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.collection.immutable.Queue
import scala.util.parsing.json.JSON
import scala.util.{Failure, Success, Try}

import akka.event.slf4j.SLF4JLogging
import org.I0Itec.zkclient.exception.ZkNoNodeException
import org.apache.zookeeper.KeeperException.NotEmptyException
import org.apache.zookeeper.data.{ACL, Id}
import org.json4s.jackson.Serialization.write

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer

//scalastyle:off
trait BackupRestoreUtils extends SLF4JLogging with SpartaSerializer {

  // Configuration
  val uri: String
  val connectionTimeout: Int
  val sessionTimeout: Int

  // Zookeeper Client

  lazy val client = CuratorFactoryHolder.getInstance()

  /**
    * Create a backup from a Zookeeper node in a json file
    *
    * @param zkPath Origin path of Zookeeper's backup
    * @param file   Path of the json file
    */
  def dump(zkPath: String, file: String): Unit = {
    val json = jsonPretify(zkPath)
    Try(writer(json, file)) match {
      case Success(_) => log.info("Backup completed")
      case Failure(ex) => throw ex
    }
  }

  /**
    * Import data from a json file
    *
    * @param zkPath Path where the data will be imported
    * @param file   Path of the json file
    * @param clean  flag to clean data before apply the import
    */
  def importer(zkPath: String = "", file: String, clean: Boolean): Unit = {
    val jsonMaped: Seq[Map[String, Any]] = jsonParser(file)
    val rootRelativePath = jsonMaped.head("node").asInstanceOf[String]
    val cleanPath = (zkPath + rootRelativePath).replace("//", "/")

    if (clean) cleanZk(cleanPath)

    Try(zkWriter(zkPath, jsonMaped)) match {
      case Success(_) => log.info("Restore completed")
      case Failure(ex) => throw ex
    }
  }

  /**
    * Clean data from a Zookeeper node recursively
    *
    * @param zkPath Origin path to clean data
    */
  def cleanZk(zkPath: String): Unit =
    Try {
      if (client.checkExists().forPath(zkPath) == null)
        log.warn(s"ZKPath $zkPath does NOT exist")
      else
        client.delete().deletingChildrenIfNeeded().forPath(zkPath)
    } match {
      case Success(_) => log.info("Clean completed")
      case Failure(ex: NotEmptyException) => throw ex
      case Failure(ex: ZkNoNodeException) => throw ex
      case Failure(ex) => throw ex
    }

  /**
    * Close the connection
    */
  def stop(): Unit =
    client.close()

  // Extract data from all Zookeeper nodes from a specific path
  @tailrec
  private def plainDump(path: String, pending: Queue[String], acc: Seq[String]): Seq[String] = {
    val newQueue = (client.getChildren.forPath(path) foldLeft pending) { (q, child) =>
      q.enqueue(s"$path/$child".replace("//", "/"))
    }
    if (newQueue.nonEmpty) {
      val (child, rest): (String, Queue[String]) = newQueue.dequeue
      plainDump(child, rest, acc :+ write(getZkNode(path)))
    } else acc :+ write(getZkNode(path))
  }

  // Extract data from a Zookeeper node
  private def getZkNode(path: String): Map[String, Any] = {
    val content: String = Option(new String(client.getData.forPath(path))).getOrElse("")
    val acls: Seq[Map[String, String]] =
      client.getACL.forPath(path)
        .map { acl =>
          Map(
            "principal" -> acl.getId.getId,
            "type" -> acl.getId.getScheme,
            "perms" -> acl.getPerms.toString
          )
        }
    Map("node" -> path, "content" -> content, "acls" -> acls)
  }

  // Write a Json String in a file
  private def writer(json: String, fileName: String): Unit = {
    new File(fileName).getParentFile.mkdirs
    Files.write(Paths.get(fileName), json.getBytes)
  }

  // Create a Json String with all the data from a Zookeeper's path
  private def jsonPretify(zkPath: String): String =
    s"[" + plainDump(zkPath, Queue.empty[String], Seq.empty[String]).mkString(",") + "]"
      .replace("\\", "")
      .replace("}{", "},\n{")
      .replace("}\"}", "}}")
      .replace(":\"{", ":{")

  // Write a collection of nodes in Zookeeper
  private def zkWriter(zkPath: String, jsonMaped: Seq[Map[String, Any]]): Unit =
    jsonMaped.foreach { nodeElement =>
      val path: String = nodeElement("node").asInstanceOf[String].replace("\\", "")
      val content: String = nodeElement("content").asInstanceOf[String]

      val acls = nodeElement("acls").asInstanceOf[Seq[Map[String, Any]]].map { acl =>
        val principal = acl("principal").asInstanceOf[String]
        val _type = acl("type").asInstanceOf[String]
        val perms = acl("perms").asInstanceOf[String]
        new ACL(perms.toInt, new Id(_type, principal))
      }

      val fullPath = (zkPath + path).replace("//", "/")
      val reg = "(/stratio/sparta/sparta.*?(?=/))".r
      val restorePath = reg.replaceAllIn(fullPath, AppConstant.BaseZkPath)
      if (client.checkExists.forPath(restorePath) != null) {
        client.setACL().withACL(acls).forPath(restorePath)
      }
      else {
        client.create().creatingParentContainersIfNeeded().forPath(restorePath, content.getBytes())
        client.setACL().withACL(acls).forPath(restorePath)
      }
      client.setData().forPath(restorePath, content.getBytes())
    }

  // Deserialize json file in a collection of Maps.
  protected def jsonParser(file: String): Seq[Map[String, Any]] = {
    val json = JSON.parseFull(
      scala.io.Source.fromFile(file)
        .getLines()
        .mkString("")
    ).get
    json.asInstanceOf[Seq[Map[String, Any]]]
  }
}
