/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.sftp

import java.util.Properties

import akka.event.slf4j.SLF4JLogging
import com.jcraft.jsch.{ChannelSftp, JSch}
import com.springml.sftp.client.SFTPClient
import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success, Try}


trait SftpConfigSuiteWithFileOperations extends SLF4JLogging {


  val config = ConfigFactory.load()

  val sftpHost = {
    Try(config.getString("sftp.host")) match {
      case Success(configHost) =>
        log.info(s"SFTP host retrieved from config: $configHost")
        s"$configHost"
      case Failure(_) =>
        log.info(s"SFTP host set to default value: localhost")
        "localhost"
    }
  }

  val sftpPort = {
    Try(config.getString("sftp.port").toInt) match {
      case Success(configPort) =>
        log.info(s"SFTP port retrieved from config: $configPort")
        configPort
      case Failure(_) =>
        log.info(s"SFTP port set to default value: 22")
        22
    }
  }

  def sftpClient = new SFTPClient(null, "foo", "pass", sftpHost, sftpPort)

  def channelSftp: ChannelSftp = {
    val jsch = new JSch
    val session = jsch.getSession("foo", sftpHost, sftpPort)
    session.setPassword("pass")
    val config = new Properties
    config.put("StrictHostKeyChecking", "no")
    session.setConfig(config)
    session.connect()
    val channel = session.openChannel("sftp") // Open SFTP Channel
    channel.connect
    channel.asInstanceOf[ChannelSftp]
  }

  def uploadFile(fromFilePath: String, targetFilePath: String) =
    sftpClient.copyToFTP(fromFilePath, targetFilePath)

  def deleteSftpFile(targetPath: String) = channelSftp.rm(targetPath)

  def createSftpDir(targetPath: String) = channelSftp.mkdir(targetPath)

  def chmodSftpFile(targetPath: String) = channelSftp.chmod(7777, targetPath)

  def checkFileExists(targetPath: String, fileName: String) = {
    val filesInPath = channelSftp.ls(targetPath).toArray()
    filesInPath.exists(x => x.toString.contains(fileName))
  }

  def downloadFile(readFrom: String, downloadTo: String) = channelSftp.get(readFrom, downloadTo)

}