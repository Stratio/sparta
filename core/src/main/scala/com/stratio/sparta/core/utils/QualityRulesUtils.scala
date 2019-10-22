/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.utils

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.enumerators._
import com.stratio.sparta.core.models.MetadataPath._
import com.stratio.sparta.core.models.PropertyKeyValue._
import com.stratio.sparta.core.models.qualityrule.resources._
import com.stratio.sparta.core.models.{MetadataPath, PropertyKeyValue, ResourcePlannedQuery, SpartaQualityRule}

import scala.util.{Failure, Success, Try}

object QualityRulesUtils extends ResourceQRUtils with SLF4JLogging{

  import SpartaQualityRule._

  case class DetailedResourcesMetadataPath(metadataPath: String, id: Long, listProperties: Seq[PropertyKeyValue] = Seq.empty[PropertyKeyValue])

  def extractQueriesFromPlannedQRResources(qr: SpartaQualityRule, xdSessionConf: Map[String, String]): Seq[String] = {
    extractResources(qr).map(res => res.getSQLQuery(xdSessionConf))
  }

  def extractResources(qr: SpartaQualityRule): Seq[ResourceQR] = {
    val simpleQRResource: Option[ResourceQR] =
      if (qr.qualityRuleType == QualityRuleTypeEnum.Planned) {
            Try(createResourceAccordingToGovernanceType(
              ResourcePlannedQuery(id = defaultPlannedQRMainResourceID,
              metadataPath = qr.metadataPath,
              resource = getTableName(qr.metadataPath, qr.metadataPathResourceType),
              typeResource = qr.metadataPathResourceType.get,
              listProperties = qr.metadataPathResourceExtraParams), changeName = false)) match {
              case Success(res) => res
              case Failure(exception) =>
                log.error(s"Cannot create a Resource: $exception")
                None
            }
      } else None

    val seqSimpleQR = simpleQRResource.fold(Seq.empty[ResourceQR]) { simpleQR => Seq(simpleQR) }

    /** If it's planned it can either be simple (the return will be seqSimpleQR because the plannedQuery will be empty)
      * or described via a planned query (the result of the flatMap)
      */
    qr.plannedQuery.fold(seqSimpleQR) {
      pq =>
        pq.resources.flatMap { resource => createResourceAccordingToGovernanceType(resource) }
    }
  }

  /**
    * This method retrieves the resources we need to ask details about, therefore it will never return
    * a resource whose type is QualityRuleResourceTypeEnum.XD
    * */
  def sequenceMetadataResources(spartaQualityRule: SpartaQualityRule): Seq[ResourcePlannedQuery] = {
    val seqResourcesDetailed: Seq[ResourcePlannedQuery] =
      spartaQualityRule.plannedQuery.map(pq => pq.resources.filterNot(_.typeResource == QualityRuleResourceTypeEnum.XD)).toList.flatten

    val resourcePlannedSimpleQuery: Seq[ResourcePlannedQuery] =
      if (spartaQualityRule.metadataPathResourceType.isEmpty ||
        (spartaQualityRule.metadataPathResourceType.isDefined && spartaQualityRule.metadataPathResourceType.get.equals(QualityRuleResourceTypeEnum.XD))) Seq.empty[ResourcePlannedQuery]
      else {
        Seq(ResourcePlannedQuery(
          id = defaultPlannedQRMainResourceID,
          metadataPath = spartaQualityRule.metadataPath,
          resource = MetadataPath.fromMetadataPathString(spartaQualityRule.metadataPath).resource.get,
          typeResource = spartaQualityRule.metadataPathResourceType.get,
          listProperties = spartaQualityRule.metadataPathResourceExtraParams
        ))
      }
    resourcePlannedSimpleQuery ++ seqResourcesDetailed

  }

  def createResourceAccordingToGovernanceType(resource: ResourcePlannedQuery, changeName: Boolean = true): Option[ResourceQR] = {
    resource.typeResource match {
      case QualityRuleResourceTypeEnum.XD => CrossdataResource.apply(resource, changeName = false)
      case QualityRuleResourceTypeEnum.JDBC =>
        resource.listProperties.seqToMap.getOrElse(SpartaQualityRule.qrVendor, "POSTGRES") match {
          case vendor if vendor.equalsIgnoreCase("postgres") =>
            PostgresResource.apply(resource, changeName)
          case vendor if vendor.equalsIgnoreCase("oracle") =>
            OracleResource.apply(resource, changeName)
          case vendor if vendor.equalsIgnoreCase("jdbc") =>
            JDBCResource.apply(resource, changeName)
        }
      case QualityRuleResourceTypeEnum.HDFS =>
        resource.listProperties.seqToMap.getOrElse(SpartaQualityRule.qrSchemaFile, "parquet") match {
          case schema if schema.equalsIgnoreCase("parquet") =>
            ParquetResource.apply(resource, changeName)
          case schema if schema.equalsIgnoreCase("avro") =>
            AvroResource.apply(resource, changeName)
        }
    }
  }
}
