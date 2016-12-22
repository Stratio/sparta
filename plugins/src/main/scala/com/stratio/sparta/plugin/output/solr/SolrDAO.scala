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
package com.stratio.sparta.plugin.output.solr

import java.io.{Closeable, File}
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.{OutputKeys, TransformerFactory}

import com.stratio.sparta.sdk.pipeline.output.Output
import org.apache.commons.io.FileUtils
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.{CloudSolrClient, HttpSolrClient}
import org.apache.solr.client.solrj.request.CoreAdminRequest
import org.apache.solr.client.solrj.request.CoreAdminRequest.Create
import org.apache.solr.common.params.CoreAdminParams
import org.apache.spark.Logging
import org.apache.spark.sql.types.{DataType, StructType}

trait SolrDAO extends Closeable with Logging {

  final val DefaultNode = "localhost"
  final val DefaultPort = "8983"
  final val SolrConfigFile = "solrconfig.xml"
  final val SolrSchemaFile = "schema.xml"
  final val SolrConfigurationError = "Invalid configuration parameters, check isCloud and cloudDataDir/localDataDir"

  def idField: Option[String]

  def connection: String

  def createSchema: Boolean

  def isCloud: Boolean

  def tokenizedFields: Boolean

  def dataDir: Option[String]

  def validConfiguration: Boolean = dataDir.isDefined

  def getConfig(host: String, collection: String): Map[String, String] =
    Map("zkhost" -> host, "collection" -> collection, "skip_default_index" -> "true")

  def createCoreAccordingToSchema(solrClients: Map[String, SolrClient],
                                  tableName: String,
                                  schema: StructType): Unit = {

    val core = tableName
    val tempDataPath = s"/tmp/solr/$core/data"
    val tempConfPath = s"/tmp/solr/$core/conf"
    val solrClient = solrClients(core)
    val createCore = new Create

    createDir(tempDataPath)
    createDir(tempConfPath)
    createSolrConfig(tempConfPath)
    createSolrSchema(schema, tempConfPath)
    createCore.setCoreName(core)
    createCore.setSchemaName(SolrSchemaFile)
    createCore.setConfigName(SolrConfigFile)
    val dataPath = s"${dataDir.get}/$core/data"
    if (isCloud) {
      createCore.setDataDir(dataPath)
      createCore.setInstanceDir(s"${dataDir.get}/$core")
      solrClient.asInstanceOf[CloudSolrClient].uploadConfig(Paths.get(tempConfPath), core)
    } else if (dataDir.isDefined) {
      val localConfPath = s"${dataDir.get}/$core/conf"
      createDir(dataPath)
      createDir(localConfPath)
      createCore.setDataDir(dataPath)
      createCore.setInstanceDir(s"${dataDir.get}/$core")
      FileUtils.copyFile(new File(s"$tempConfPath/$SolrConfigFile"), new File(s"$localConfPath/$SolrConfigFile"))
      FileUtils.copyFile(new File(s"$tempConfPath/$SolrSchemaFile"), new File(s"$localConfPath/$SolrSchemaFile"))
    }

    solrClient.request(createCore)
  }

  private def createDir(dirPath: String): Unit = new File(dirPath).mkdir

  private def createSolrConfig(confPath: String) {
    val origin = getClass.getClassLoader.getResource(s"$SolrConfigFile")
    val dest = new File(confPath + s"/$SolrConfigFile")
    new File(confPath).mkdirs()
    FileUtils.copyURLToFile(origin, dest)
  }

  private def createSolrSchema(schema: StructType, path: String) {
    val domFactory = DocumentBuilderFactory.newInstance
    domFactory.setIgnoringComments(true)
    val builder = domFactory.newDocumentBuilder
    val schemaFile = getClass.getClassLoader().getResourceAsStream(s"$SolrSchemaFile")
    val doc = builder.parse(schemaFile)
    val nodes = doc.getElementsByTagName("schema")

    for (structField <- schema.iterator) {
      val field = doc.createElement("field")
      field.setAttribute("name", structField.name)
      field.setAttribute("type", getSolrFieldType(structField.dataType))
      field.setAttribute("indexed", "true")
      field.setAttribute("stored", "true")
      field.setAttribute("required", structField.metadata.contains(Output.PrimaryKeyMetadataKey).toString)
      nodes.item(0).appendChild(field)
    }

    val transformerFactory = TransformerFactory.newInstance
    val transformer = transformerFactory.newTransformer
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    val source = new DOMSource(doc)
    val streamResult = new StreamResult(new File(path + s"/$SolrSchemaFile"))
    transformer.transform(source, streamResult)
  }

  //scalastyle:off
  private def getSolrFieldType(dataType: DataType): String = {
    dataType match {
      case org.apache.spark.sql.types.LongType => "long"
      case org.apache.spark.sql.types.DoubleType => "double"
      case org.apache.spark.sql.types.DecimalType() => "double"
      case org.apache.spark.sql.types.IntegerType => "int"
      case org.apache.spark.sql.types.BooleanType => "boolean"
      case org.apache.spark.sql.types.DateType => "dateTime"
      case org.apache.spark.sql.types.TimestampType => "dateTime"
      case org.apache.spark.sql.types.ArrayType(_, _) => "string"
      case org.apache.spark.sql.types.StringType => if (tokenizedFields) "text" else "string"
      case _ => "string"
    }
  }

  //scalastyle:on

  override def close(): Unit = {}

  def getSolrServer(zkHost: String, isCloud: Boolean): SolrClient = {
    if (isCloud)
      new CloudSolrClient(zkHost)
    else
      new HttpSolrClient("http://" + zkHost + "/solr")
  }

  def getCoreList(zkHost: String, isCloud: Boolean): Seq[String] = {
    val solrClient = getSolrServer(zkHost, isCloud)
    val coreAdminRequest: CoreAdminRequest = new CoreAdminRequest()
    coreAdminRequest.setAction(CoreAdminParams.CoreAdminAction.STATUS)
    val cores = coreAdminRequest.process(solrClient)

    for (i <- 0 until cores.getCoreStatus.size())
      yield cores.getCoreStatus().getName(i)
  }
}
