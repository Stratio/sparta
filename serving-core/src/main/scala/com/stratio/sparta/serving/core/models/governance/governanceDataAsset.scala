/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models

import com.stratio.sparta.core.models.PropertyKeyValue
import com.stratio.sparta.core.models.SpartaQualityRule._

import scala.util.{Failure, Success, Try}

package object governanceDataAsset {

  case class GovernanceDataAssetResponse(dataAssets: MatrioskaDataAsset){

    def getDataAssetType: Option[String] = dataAssets.dataAssets.headOption.map(_.`type`)

    def retrieveDetailsFromDatastore: Seq[PropertyKeyValue] = {
      Try{
        val vendor = dataAssets.dataAssets.head.vendor
        val datastore = dataAssets.dataAssets.head.properties.dataStore.get

        import datastore._
        val security: String = dataSources.head.security.`type`
        val driver: String = dataSources.head.driver

        val tlsEnabled = security.equalsIgnoreCase("TLS")

        Seq(
          PropertyKeyValue(key = qrTlsEnabled, value = tlsEnabled.toString),
          PropertyKeyValue(key = qrSecurity, value = security),
          PropertyKeyValue(key = qrDriver, value = driver),
          PropertyKeyValue(key = qrVendor, value = vendor)
        )
      }} match {
      case Success(seq) => seq
      case Failure(_) => Seq.empty[PropertyKeyValue]
    }

    /** This will retrieve for an hdfs where to find the hdfs-site and core-site (e.g. http://hostname:8080/krb5.conf")
      * and for a JDBC the connection detail without the database that will be added during the parsing
      * */
    def retrieveConnectionDetailsFromDatastore: Seq[PropertyKeyValue] = {
      Try {
        val datastore = dataAssets.dataAssets.head.properties.dataStore.get
        val datastoreType = getDataAssetType.get
        val urls: Seq[String] = datastore.dataSources.head.urls.map(_.url)

        datastoreType match {
          // "url": "jdbc:postgresql://127.0.0.1:5432/-db-"
          case dsType if dsType matches "(i?)(JDBC|SQL)" =>
            Seq(PropertyKeyValue(key = qrConnectionURI, value = urls.head.stripSuffix("-db-")))

          //"url": "http://hostname:8080/krb5.conf"
          //"url": "http://hostname:8080/core-site.conf"
          //"url": "http://hostname:8080/hdfs-site.conf"
          case _ =>
            val suffixes = Seq("/krb5.conf","/core-site.xml","/hdfs-site.xml")
            val parsedURI = urls.map{ suffixes.foldLeft(_) { (string, suffix) => string.stripSuffix(suffix)}}
            if (parsedURI.forall(el => el.equals(parsedURI.head)))
              Seq(PropertyKeyValue(key = qrConnectionURI, value = parsedURI.head))
            else Seq.empty[PropertyKeyValue]
        }
      } match {
        case Success(seq) => seq
        case Failure(_) => Seq.empty[PropertyKeyValue]
      }
    }

    def retrieveSchemaFromFile: Seq[PropertyKeyValue] = {
      val schemaFromResponse = dataAssets.dataAssets.head.properties.hdfsFile.map(hdfsFile => hdfsFile.schema)
      schemaFromResponse.fold(Seq.empty[PropertyKeyValue]){schema => Seq(PropertyKeyValue(qrSchemaFile, schema))}
    }
  }

  case class MatrioskaDataAsset(dataAssets: Seq[GovernanceDataAsset])

  case class GovernanceDataAsset(id: String,
                                 `type`: String,
                                 properties: GovernanceProperties,
                                 metadataPath: String,
                                 vendor: String
                                )

  case class GovernanceDatastore(url: String, dataSources: Seq[GovernanceDataSources])

  case class GovernanceDataSources(security: GovernanceSecurity, driver: String, urls: Seq[GovernanceURL])

  case class GovernanceSecurity(`type`: String)

  case class GovernanceProperties(dataStore: Option[GovernanceDatastore],
                                  hdfsFile: Option[HdfsFile])

  case class GovernanceURL(url: String)

  case class HdfsFile(schema: String)

}
