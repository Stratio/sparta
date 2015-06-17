
package com.stratio.sparkta.testat.embedded

object ElasticThread {

  def main (args: Array[String]) {
    ElasticsearchEmbeddedServer.cleanData
    ElasticsearchEmbeddedServer.start
  }

}
