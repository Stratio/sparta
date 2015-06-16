
package com.stratio.sparkta.testat.embeddedes

object ElasticThread {

  def main (args: Array[String]) {
    ElasticsearchEmbeddedServer.cleanData
    ElasticsearchEmbeddedServer.start
  }

}
