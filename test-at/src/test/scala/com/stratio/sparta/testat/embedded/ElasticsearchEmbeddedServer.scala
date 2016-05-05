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
package com.stratio.sparta.testat.embedded

import java.io.File

import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.node.Node
import org.elasticsearch.node.NodeBuilder.nodeBuilder

object ElasticsearchEmbeddedServer {

  val DefaultDataDirectory = "target/elasticsearch-data"
  val ClusterName = "sparta-elasticsearch"
  var node: Node = _


  def start: Unit= {
    val elasticsearchSettings: ImmutableSettings.Builder = ImmutableSettings.settingsBuilder()
      .put("cluster.name", ClusterName)
      .put("node.name", "nodeAT")
      .put("http.cors.enabled", "true")
      .put("http.enabled", "true")
      .put("index.number_of_shards","1")
      .put("index.number_of_replicas","0")
      .put("node.master","true")
      .put("index.cache.field.type", "soft")
      .put("index.cache.field.max_size", "10000")
      .put("threadpool.index.queue_size", "300")
      .put("path.data", DefaultDataDirectory)
      .put("discovery.zen.ping.multicast.enabled", "false")

    node = nodeBuilder()
      .local(true)
      .settings(elasticsearchSettings.build())
      .node()

    getClient.admin.cluster.prepareHealth().setWaitForGreenStatus.setTimeout(TimeValue.timeValueMinutes(1))
      .execute.actionGet
  }

  def getClient : Client = node.client()

  def shutdown: Unit = {
    node.close
  }

  def cleanData :Unit ={
    val folders = Seq("data", ElasticsearchEmbeddedServer.DefaultDataDirectory)
    folders.foreach(folder => {
      val file = new File(folder)
      deleteFiles(file)
    })
  }

  private def deleteFiles(file: File): Unit = {
    if (file.exists() && file.isDirectory)
      Option(file.listFiles).map(_.toList).getOrElse(Nil).foreach(deleteFiles(_))
    file.delete()
  }

}
