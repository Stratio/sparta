/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.workflow.lineage

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.constants.SdkConstants._
import com.stratio.sparta.core.utils.RegexUtils._
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.services.HdfsService
import org.apache.hadoop.fs.{FileSystem, Path}

import scala.util.{Properties, Try}
import scala.util.control.NonFatal
import scala.xml.XML

object HdfsLineage extends SLF4JLogging {

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

  val lineagePath: String
  val lineageResourceSuffix: Option[String]


  def getHdfsLineageProperties(stepType: String): Map[String, String] =
    if (!isHdfsScheme) {
      Map.empty
    } else {
      val newPath = stripPrefixAndFormatPath(lineagePath)

      val resource = if (stepType.equals(OutputStep.StepType)) {
        lineageResourceSuffix.fold("") { suffix => getFileSystemResource(newPath, suffix) }
      } else {
        getFileSystemResourceFromPathOrFile(newPath)
      }

      val finalPath =
        lineageResourceSuffix
          .map(_ => newPath.replace(resource, ""))
          .getOrElse(newPath)
          .stripSuffix("/")

      Map(
        ServiceKey -> currentHdfs,
        PathKey -> finalPath,
        ResourceKey -> resource,
        SourceKey -> lineagePath
      )
    }

  private def getFileSystemResource(path: String, suffix: String): String =
    if (path.endsWithIgnoreCase(suffix))
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

    val stripPrefixPath = if (path.startWithIgnoreCase("hdfs://")) {
      "/" + path.stripPrefixWithIgnoreCase("hdfs://").split("/", 2).lastOption.getOrElse("")
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

  private[lineage] lazy val hdfsConfig: Map[String, String] = {
    val hadoopConfDir = Properties.envOrElse("SPARTA_CLASSPATH_DIR", "/etc/sds/sparta")
    val coreSite = Option(XML.loadFile(s"$hadoopConfDir/core-site.xml"))
    val hdfsSite = Option(XML.loadFile(s"$hadoopConfDir/hdfs-site.xml"))

    Seq(coreSite, hdfsSite).foldLeft(Map.empty[String, String]) { (mergedConf, maybeFile) =>
      val maybeConf = maybeFile.map { file =>
        val propNames = (file \\ "property" \ "name").map(_.toString.stripPrefix("<name>").stripSuffix("</name>"))
        val propValues = (file \\ "property" \ "value").map(_.toString.stripPrefix("<value>").stripSuffix("</value>"))
        propNames.zip(propValues).toMap
      }
      maybeConf.fold(Map.empty[String, String])(_ ++ mergedConf)
    }

  }

  private[lineage] lazy val currentHdfs = {
    val maybeDefaultFs = hdfsConfig.get("fs.defaultFS")
      .flatMap(_.stripPrefixWithIgnoreCase("hdfs://").split(":").headOption)

    val pattern = "hdfs://([\\w-]+)/.*".r
    val maybeCapturedHdfs = pattern.findFirstMatchIn(lineagePath).map(_.group(1))

    val maybeAllHdfs = hdfsConfig.get("dfs.nameservices").map(_.split(",", -1))

    val maybeCurrentHdfs = for {
      currentHdfs <- maybeCapturedHdfs
      allHdfs <- maybeAllHdfs
    } yield {
      (currentHdfs, allHdfs.contains(currentHdfs))
    }

    (maybeCurrentHdfs, maybeDefaultFs) match {
      case (Some((capturedHdfs, true)), _) => capturedHdfs
      case (Some((_, false)), _) => ""
      case (_, Some(default)) => default
      case _ => ""
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