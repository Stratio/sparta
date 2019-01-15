/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.workflow.lineage

import com.stratio.sparta.core.constants.SdkConstants.{PathKey, ResourceKey, ServiceKey}

import scala.util.Properties
import scala.xml.XML

trait HdfsLineage {

  val lineagePath : String
  val lineageResourceSuffix: Option[String]

  def getHdfsLineageProperties : Map[String, String] = {
    val newPath = stripPrefixAndFormatPath(lineagePath)
    val resource = lineageResourceSuffix.fold("") {suffix => getFileSystemResource(newPath, suffix) }

    Map(
      ServiceKey -> getHDFSServiceName.getOrElse(""),
      PathKey -> lineagePath.replace(resource,"").stripSuffix("/"),
      ResourceKey -> resource)
  }

  private def getFileSystemResource(path: String, suffix: String): String =
    if (path.toLowerCase.endsWith(suffix.toLowerCase))
      path.split("/").lastOption.getOrElse("")
    else ""

  private def stripPrefixAndFormatPath(path: String): String = {
    val userName = Properties.envOrElse("MARATHON_APP_LABEL_DCOS_SERVICE_NAME",
      Properties.envOrElse("TENANT_NAME", "sparta"))

    if (path.toLowerCase.startsWith("hdfs://"))
      "/" + path.toLowerCase.stripPrefix("hdfs://").split("/",2).last
    else if (!path.startsWith("/"))
      "/user/" + userName + "/" + path
    else
      path
  }

  private def getHDFSServiceName: Option[String] = {
    val hadoopConfDir = Properties.envOrElse("SPARTA_CLASSPATH_DIR","/etc/sds/sparta")
    val hdfsConfFile = Option(XML.loadFile(s"$hadoopConfDir/core-site.xml"))

    if(hdfsConfFile.isDefined) {
      val propNames = (hdfsConfFile.get \\ "property" \ "name").map(_.toString.stripPrefix("<name>").stripSuffix("</name>"))
      val propValues = (hdfsConfFile.get \\ "property" \ "value").map(_.toString.stripPrefix("<value>").stripSuffix("</value>"))
      val mapOfProps = propNames.zip(propValues).toMap

      mapOfProps.get("fs.defaultFS").flatMap(_.toLowerCase.stripPrefix("hdfs://").split("\\.").lift(0))
    } else
      None
  }

}
