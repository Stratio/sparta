/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.workflow.lineage

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.constants.SdkConstants._
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.services.HdfsService
import org.apache.hadoop.fs.{FileSystem, Path}

import scala.util.control.NonFatal
import scala.util.{Properties, Try}
import scala.xml.XML

object HdfsLineage extends SLF4JLogging {

  lazy val DomainSuffix: String = "." + Properties.envOrElse("EOS_INTERNAL_DOMAIN", "paas.labs.stratio.com")

  lazy val defaultFsScheme: String =
    Try(
      FileSystem.get(HdfsService.configuration).getScheme
    ).recover {
      case NonFatal(exception) =>
        log.warn("Unsupported scheme for default file system", exception)
        "unknown_scheme"
    }.get

}

trait HdfsLineage {

  val lineagePath : String
  val lineageResourceSuffix: Option[String]


  def getHdfsLineageProperties(stepType: String) : Map[String, String] =
    if (!isHdfsScheme) {
      Map.empty
    } else {
      val newPath = stripPrefixAndFormatPath(lineagePath)

      val resource = if(stepType.equals(OutputStep.StepType)) {
        lineageResourceSuffix.fold("") { suffix => getFileSystemResource(newPath, suffix) }
      } else {
        getFileSystemResourceFromPathOrFile(newPath)
      }

      val finalPath =
        lineageResourceSuffix
          .map(_ => newPath.replace(resource,""))
          .getOrElse(newPath)
          .stripSuffix("/")

      Map(
        ServiceKey -> getHDFSServiceName.getOrElse(""),
        PathKey -> finalPath,
        ResourceKey -> resource,
        SourceKey -> lineagePath
      )
    }

  private def getFileSystemResource(path: String, suffix: String): String =
    if (path.toLowerCase.endsWith(suffix.toLowerCase))
      path.split("/").lastOption.getOrElse("")
    else ""

  private def getFileSystemResourceFromPathOrFile(path: String): String = {
    if (path.contains("="))
      path.split("=").headOption.flatMap(_.split("/").dropRight(1).lastOption).getOrElse("")
    else
      path.split("/").lastOption.getOrElse("")
  }

  private def stripPrefixAndFormatPath(path: String): String = {
    val userName = AppConstant.spartaTenant
    val stripPrefixPath = if (path.toLowerCase.startsWith("hdfs://")) {
      "/" + path.toLowerCase.stripPrefix("hdfs://").split("/", 2).lastOption.getOrElse("")
    } else if (!path.startsWith("/")) {
      "/user/" + userName + "/" + path
    } else {
      path
    }

    if (stripPrefixPath.contains("=")) {
      val pathLastLevel = "/" + stripPrefixPath.split("=").headOption.flatMap(_.split("/").lastOption).getOrElse("")
      stripPrefixPath.split("=").headOption.map(_.stripSuffix(pathLastLevel)).getOrElse("")
    } else {
      stripPrefixPath
    }
  }

  private def getHDFSServiceName: Option[String] = {
    val hadoopConfDir = Properties.envOrElse("SPARTA_CLASSPATH_DIR","/etc/sds/sparta")
    val hdfsConfFileOpt = Option(XML.loadFile(s"$hadoopConfDir/core-site.xml"))

    hdfsConfFileOpt.flatMap { hdfsConfFile =>
      val propNames = (hdfsConfFile \\ "property" \ "name").map(_.toString.stripPrefix("<name>").stripSuffix("</name>"))
      val propValues = (hdfsConfFile \\ "property" \ "value").map(_.toString.stripPrefix("<value>").stripSuffix("</value>"))
      val mapOfProps = propNames.zip(propValues).toMap

      mapOfProps.get("fs.defaultFS").flatMap(_.toLowerCase.stripPrefix("hdfs://").split(":").headOption).map(_.stripSuffix(HdfsLineage.DomainSuffix))
    }
  }

  private[lineage] def isHdfsScheme: Boolean = {
    val schema = Option(new Path(lineagePath).toUri.getScheme)
    if (schema.isEmpty) { // relative path, thus recovering scheme from default file system
      HdfsLineage.defaultFsScheme == "hdfs"
    } else {
      schema.contains("hdfs")
    }
  }

}