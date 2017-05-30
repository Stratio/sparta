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

package com.stratio.sparta.serving.api.utils

import java.io.File
import java.nio.file.{Files, Paths}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.models.SpartaSerializer
import kafka.utils.ZkUtils
import org.I0Itec.zkclient.exception.ZkNoNodeException
import org.I0Itec.zkclient.{ZkClient, ZkConnection}
import org.apache.zookeeper.KeeperException.NotEmptyException
import org.apache.zookeeper.data.{ACL, Id}
import org.json4s.jackson.Serialization.write

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.collection.immutable.Queue
import scala.util.parsing.json.JSON
import scala.util.{Failure, Success, Try}

trait BackupRestoreUtils extends SLF4JLogging with SpartaSerializer {

  // Configuration
  val uri: String
  val connectionTimeout: Int
  val sessionTimeout: Int

  // Zookeeper Client

  lazy val (client, zkConnection): (ZkClient, ZkConnection) =
    ZkUtils.createZkClientAndConnection(uri, connectionTimeout, sessionTimeout)

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
      if (!client.exists(zkPath))
        log.warn(s"ZKPath $zkPath does NOT exist")
      else if (client.countChildren(zkPath) != 0) {
        log.debug(s"ZKPath $zkPath has children. Deleting recursively.")
        client.deleteRecursive(zkPath)
      } else {
        log.debug(s"ZKPath $zkPath has no children.")
        client.delete(zkPath)
      }
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
    val newQueue = (client.getChildren(path) foldLeft pending) { (q, child) =>
      q.enqueue(s"$path/$child".replace("//", "/"))
    }
    if (newQueue.nonEmpty) {
      val (child, rest): (String, Queue[String]) = newQueue.dequeue
      plainDump(child, rest, acc :+ write(getZkNode(path)))
    } else acc :+ write(getZkNode(path))
  }

  // Extract data from a Zookeeper node
  private def getZkNode(path: String): Map[String, Any] = {
    val content: String = Option(client.readData(path)).getOrElse("")
    val acls: Seq[Map[String, String]] =
      client.getAcl(path)
        .getKey
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

      if (client.exists(fullPath)) client.setAcl(fullPath, acls)
      else client.createPersistent(fullPath, true, acls)

      client.writeData(fullPath, content)
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
