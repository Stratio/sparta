/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.plugin.output.solr

import java.io.{File, IOException, Serializable => JSerializable}
import java.net.URISyntaxException
import java.nio.file.Paths
import javax.xml.parsers.{DocumentBuilder, DocumentBuilderFactory, ParserConfigurationException}
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.{Transformer, TransformerException, TransformerFactory}

import com.lucidworks.spark.SolrRelation
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import org.apache.commons.io.FileUtils
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.{CloudSolrClient, HttpSolrClient}
import org.apache.solr.client.solrj.request.CoreAdminRequest
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.{DataType, StructType}
import org.apache.spark.streaming.dstream.DStream
import org.w3c.dom.{Document, Element, NodeList}
import org.xml.sax.SAXException

import scala.util.Try

class SolrOutput(keyName: String,
                          properties: Map[String, JSerializable],
                          @transient sparkContext: SparkContext,
                          operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
                          bcSchema: Option[Broadcast[Seq[TableSchema]]])
  extends Output(keyName, properties, sparkContext, operationTypes, bcSchema) {

  final val DefaultNode = "localhost"
  final val DefaultPort = "8983"

  val zkHost = properties.getString("zkHost", s"$DefaultNode:$DefaultPort")

  val createSchema = Try(properties.getString("createSchema").toBoolean).getOrElse(false)

  val isCloud = Try(properties.getString("isCloud").toBoolean).getOrElse(true)

  val dataDir = properties.getString("dataDir")

  private val solrClients: Map[String, SolrClient] = {
    bcSchema.get.value.filter(tschema => (tschema.outputName == keyName)).map(tschemaFiltered => {
      if (isCloud)
        tschemaFiltered.tableName -> new CloudSolrClient(zkHost)
      else
        tschemaFiltered.tableName -> new HttpSolrClient("http://" + zkHost + "/solr")
    }).toMap
  }

  override def setup: Unit = createCores

  private def createCores : Unit = {
    bcSchema.get.value.filter(tschema => (tschema.outputName == keyName)).foreach(tschemaFiltered => {
      val tableSchemaTime = getTableSchemaFixedId(tschemaFiltered)
      createCoreAccordingToSchema(tableSchemaTime.tableName, tableSchemaTime.schema)
    })
  }

  private def createCoreAccordingToSchema(tableName: String, schema: StructType) = {
    val core: String = tableName
    val dataPath: String = dataDir + '/' + core + "/data"
    val confPath: String = dataDir + '/' + core + "/conf"
    createDirs(dataPath, confPath)
    createSolrConfig(confPath)
    createSolrSchema(schema, confPath)
    val solrClient: SolrClient = solrClients(core)
    val createCore: CoreAdminRequest.Create = new CoreAdminRequest.Create
    createCore.setDataDir(dataPath)
    createCore.setInstanceDir(dataDir + '/' + core)
    createCore.setCoreName(core)
    createCore.setSchemaName("schema.xml")
    createCore.setConfigName("solrconfig.xml")
    if (solrClient.isInstanceOf[CloudSolrClient]) {
      (solrClient.asInstanceOf[CloudSolrClient]).uploadConfig(Paths.get(confPath), core)
    }
    solrClient.request(createCore)
  }

  def createDirs(dataPath: String, confPath: String) {
    val dataDir: File = new File(dataPath)
    val dir: File = new File(this.dataDir)
    val confDir: File = new File(confPath)
    dir.mkdirs
    dataDir.mkdirs
    confDir.mkdirs
  }

  @throws(classOf[URISyntaxException])
  @throws(classOf[IOException])
  def createSolrConfig(confPath: String) {
    FileUtils.copyFile(
      new File(ClassLoader.getSystemResource("./solr-config/solrconfig.xml").toURI),
      new File(confPath + "/solrconfig.xml")
    )
  }

  @throws(classOf[ParserConfigurationException])
  @throws(classOf[URISyntaxException])
  @throws(classOf[IOException])
  @throws(classOf[SAXException])
  @throws(classOf[TransformerException])
  def createSolrSchema(schema: StructType, confpath: String) {
    val domFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance
    domFactory.setIgnoringComments(true)
    val builder: DocumentBuilder = domFactory.newDocumentBuilder
    val doc: Document = builder.parse(new File(ClassLoader.getSystemResource("./solr-config/schema.xml").toURI))
    val nodes: NodeList = doc.getElementsByTagName("schema")
    for (structField <- schema.iterator) {
      val field: Element = doc.createElement("field")
      field.setAttribute("name", structField.name)
      field.setAttribute("type", getSolrFieldType(structField.dataType))
      field.setAttribute("indexed", "true")
      field.setAttribute("stored", "true")
      nodes.item(0).appendChild(field)
    }
    val transformerFactory: TransformerFactory = TransformerFactory.newInstance
    val transformer: Transformer = transformerFactory.newTransformer
    val source: DOMSource = new DOMSource(doc)
    val streamResult: StreamResult = new StreamResult(new File(confpath + "/schema.xml"))
    transformer.transform(source, streamResult)
  }

  override val isAutoCalculateId = Try(properties.getString("isAutoCalculateId").toBoolean).getOrElse(true)

  val idField = properties.getString("idField", None)

  override def doPersist(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    persistDataFrame(stream)
  }

  override def upsert(dataFrame: DataFrame, tableName: String, timeDimension: String): Unit = {
    val slrRelation = new SolrRelation(sqlContext, getConfig(zkHost, tableName))
    slrRelation.insert(dataFrame, true)
  }

  private def getConfig(host: String, collection: String): Map[String, String] =
    Map("zkhost" -> host, "collection" -> collection)

  //scalastyle:off
  def getSolrFieldType(dataType: DataType): String = {
    dataType match {
      case org.apache.spark.sql.types.LongType => "long"
      case org.apache.spark.sql.types.DoubleType => "double"
      case org.apache.spark.sql.types.DecimalType() => "double"
      case org.apache.spark.sql.types.IntegerType => "int"
      case org.apache.spark.sql.types.BooleanType => "boolean"
      case org.apache.spark.sql.types.DateType => "dateTime"
      case org.apache.spark.sql.types.TimestampType => "dateTime"
      case org.apache.spark.sql.types.ArrayType(_, _) => "string"
      case org.apache.spark.sql.types.StringType => "string"
      case _ => "string"
    }
  }
  //scalastyle:on
}
