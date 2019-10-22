/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import akka.actor.{Actor, Props}
import akka.cluster.Cluster
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.model.{MediaRange, MediaTypes}
import akka.http.scaladsl.model.headers.{Accept, RawHeader}
import akka.stream.ActorMaterializer
import com.stratio.sparta.core.enumerators.{QualityRuleResourceTypeEnum, QualityRuleTypeEnum}
import com.stratio.sparta.core.models.{MetadataPath, PlannedQuery, ResourcePlannedQuery, SpartaQualityRule}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.utils.QualityRulesUtils
import com.stratio.sparta.serving.api.actor.PlannedQualityRuleActor.RetrievePlannedQualityRulesTick
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.governance.GovernanceQualityRule
import com.stratio.sparta.serving.core.models.governanceDataAsset.GovernanceDataAssetResponse
import com.stratio.sparta.serving.core.utils.{HttpRequestUtils, PlannedQualityRulesUtils}
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.native.Serialization.read

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class PlannedQualityRuleActor extends Actor
  with HttpRequestUtils
  with SpartaSerializer{

  import SpartaQualityRule._
  import com.stratio.sparta.serving.core.models.governance.QualityRuleParser._


  val EpochTime = 0L
  val DefaultPeriodInterval = 60000L

  val retrievePlannedQualityRulesPeriodicity = Try(SpartaConfig.getGovernanceConfig().
    get.getLong("qualityrules.planned.period.interval")).getOrElse(DefaultPeriodInterval)

  val plannedQualityRulePgService = PostgresDaoFactory.plannedQualityRulePgService
  val plannedQualityRulesUtils = PlannedQualityRulesUtils

  implicit val executionContext: ExecutionContext = context.dispatcher
  implicit val system = context.system
  implicit val actorMaterializer = ActorMaterializer()

  val cluster = Cluster(system)

  lazy val uri = Try(SpartaConfig.getGovernanceConfig().get.getString("http.uri"))
    .getOrElse("https://governance.labs.stratio.com/dictionary")
  lazy val getPlannedQREndpoint = Try(SpartaConfig.getGovernanceConfig().get.getString("qualityrules.planned.http.get.endpoint"))
    .getOrElse("user/quality/v1/quality/searchProactiveByModifiedAt?modifiedAt=")
  lazy val getDetailsDataAssetsEndpoint = Try(SpartaConfig.getGovernanceConfig().get.getString("qualityrules.planned.dataasset.http.get.endpoint")).getOrElse("user/catalog/v1/dataAsset/searchByMetadataPathLike?metadataPathLike=")
  lazy val noTenant = Some("NONE")
  lazy val current_tenant= AppConstant.EosTenant.orElse(noTenant)
  lazy val rawHeaders: Seq[RawHeader] = Seq(RawHeader("X-TenantID", current_tenant.getOrElse("NONE")))

  lazy val epochTimeDatetime : String = new DateTime(EpochTime, DateTimeZone.UTC).toString()
  val initialPageSize: Long = Try(SpartaConfig.getGovernanceConfig().get.getLong("qualityrules.planned.initial.pagesize")).getOrElse(200)
  val defaultPageSize: Long =  Try(SpartaConfig.getGovernanceConfig().get.getLong("qualityrules.planned.default.pagesize")).getOrElse(40)

  override def preStart(): Unit = {
    context.system.scheduler.schedule(1 minutes, Duration.create(retrievePlannedQualityRulesPeriodicity, MILLISECONDS),
      self, RetrievePlannedQualityRulesTick)
    self ! RetrievePlannedQualityRulesTick
  }

  override def receive: Receive = {
    case RetrievePlannedQualityRulesTick =>
      log.debug(s"Received RetrievePlannedQualityRulesTick")

      val plannedQualityRules: Future[Seq[SpartaQualityRule]] =
        for{
          latestModificationDate <- plannedQualityRulePgService.getLatestModificationDate()
          result <- retrievePlannedQualityRules(parseMillisToDate(latestModificationDate))
          enrichedQR <- getDetailsFromGovernance(result)
        } yield enrichedQR

      plannedQualityRules.onComplete{
        case Success(value) =>
          val filterGood: Seq[SpartaQualityRule] = value.filter { qr =>
            qr.validSpartaQR match {
              case Success(_) =>  qr.qualityRuleType == QualityRuleTypeEnum.Planned
              case Failure(ex) =>
                log.warn(s"Discarding QR ${qr.id}: ${ex.getLocalizedMessage}. QR Details: ${qr.toString}")
                false
            }
          }

          filterGood.foreach{ filteredQRs =>
            val scheduledQr = plannedQualityRulesUtils.createOrUpdateTaskPlanning(filteredQRs)
            scheduledQr onComplete {
              case Success((_, qualityR)) =>
                plannedQualityRulePgService.createOrUpdate(qualityR)
              case Failure(exception) =>
                log.error("error", exception)
            }
          }

        case Failure(ex) =>
          log.error(ex.getLocalizedMessage, ex)
      }
  }

  override def postStop(): Unit = {
    super.postStop()
  }

  private def parseMillisToDate(value: Option[Long]): String = {
    new DateTime(value.getOrElse(EpochTime), DateTimeZone.UTC).toString()
  }

  private def retrievePlannedQualityRules(modTime: String): Future[Seq[SpartaQualityRule]] = {
    for {
      responsePlannedQR <- getPlannedQualityRulesFromApi(modTime)
    } yield {
      val seqParsedAsGovernance = read[GovernanceQualityRule](responsePlannedQR)
      seqParsedAsGovernance.parse()
    }
  }

  private def getDetailsFromGovernance(spartaQualityRules: Seq[SpartaQualityRule]): Future[Seq[SpartaQualityRule]] = {
    Future.sequence(spartaQualityRules.map(qr => getEnrichedQR(qr)))
  }

  private def getEnrichedQR(qr: SpartaQualityRule): Future[SpartaQualityRule] = {
    //Find the needed resources
    val resourcesToParse: Seq[ResourcePlannedQuery] = QualityRulesUtils.sequenceMetadataResources(qr)
    //Look for their details: HDFS or Postgres
    val resourcesToReplace: Future[Seq[ResourcePlannedQuery]] = Future.sequence(resourcesToParse.map {
      resource =>
        val metadataPathResource = MetadataPath.fromMetadataPathString(resource.metadataPath)
        resource.typeResource match {
          case QualityRuleResourceTypeEnum.HDFS =>
            for {
              dataStoreDetailsString <- sendRequestDataAsset(metadataPathResource.service)
              fileDetailsString <- sendRequestDataAsset(resource.metadataPath, absolutePath = true)
            } yield {
              if (Option(dataStoreDetailsString).notBlank.isDefined && Option(fileDetailsString).notBlank.isDefined) {
                val dataStoreDetails = read[GovernanceDataAssetResponse](dataStoreDetailsString)
                val fileDetails = read[GovernanceDataAssetResponse](fileDetailsString).retrieveSchemaFromFile
                val resourceAllDetails = dataStoreDetails.retrieveConnectionDetailsFromDatastore ++ dataStoreDetails.retrieveDetailsFromDatastore ++ fileDetails
                log.debug(s"Retrieved these details for resource ${resource.resource} with id ${resource.id}: [${resourceAllDetails.map(_.toString()).mkString(",")}]")
                resource.copy(listProperties = resourceAllDetails)
              } else {
                log.warn(s"Cannot retrieve from Governance API the mandatory details for the resource ${resource.metadataPath}")
                resource
              }
            }
          case QualityRuleResourceTypeEnum.JDBC => for {
            dataStoreDetailsString <- sendRequestDataAsset(metadataPathResource.service)
          } yield {
            if (Option(dataStoreDetailsString).notBlank.isDefined) {
              val dataStoreDetails = read[GovernanceDataAssetResponse](dataStoreDetailsString)
              val resourceAllDetails = dataStoreDetails.retrieveConnectionDetailsFromDatastore ++ dataStoreDetails.retrieveDetailsFromDatastore
              log.debug(s"Retrieved these details for resource ${resource.resource} with id ${resource.id}: [${resourceAllDetails.map(_.toString()).mkString(",")}]")
              resource.copy(listProperties = resourceAllDetails)
            } else {
              log.warn(s"Cannot retrieve from Governance API the mandatory details for the resource ${resource.metadataPath}")
              resource
            }
          }
        }
    })
    //Replace them in the quality rule
    replaceResources(qr, resourcesToReplace)
  }

  def replaceResources(qr: SpartaQualityRule,
                       updatedResourcesFuture : Future[Seq[ResourcePlannedQuery]]): Future[SpartaQualityRule] =
    for {
      seq <- updatedResourcesFuture
    } yield {
      if (seq.isEmpty) qr
      else {
        val globalResource: Option[ResourcePlannedQuery] = seq.find(_.id == defaultPlannedQRMainResourceID)
        val resourcesInsidePlannedQueryToUpdate: Seq[ResourcePlannedQuery] = seq.filterNot(_.id == defaultPlannedQRMainResourceID)
        val hdfsResource: Option[String] = seq.find(_.typeResource == QualityRuleResourceTypeEnum.HDFS).fold(None: Option[String]) { res => res.listProperties.find(_.key == qrConnectionURI).map(_.value) }

        // If the id is -1 therefore the quality rule is planned but simple: we need to update the fields:
        // metadataPathResourceType: Option[QualityRuleResourceType] = None,
        // metadataPathResourceExtraParams : Seq[PropertyKeyValue] = Seq.empty[PropertyKeyValue]
        val qualityRuleWithGlobalResource = globalResource match {
          case None => qr
          case Some(res) => qr.copy(
            metadataPathResourceType = Option(res.typeResource),
            metadataPathResourceExtraParams = res.listProperties
          )
        }

        val setOfUpdatedId = qr.plannedQuery.fold(Set.empty[Long]) { pq => pq.resources.map(_.id).toSet }
        val resourcesInsidePlannedQueryNotToUpdate = qr.plannedQuery.fold(Seq.empty[ResourcePlannedQuery]) { pq =>
          pq.resources.filterNot(res => setOfUpdatedId.contains(res.id))
        }

        // If the id is the one from Governance we need to find inside the resources in the planned query
        // to which resource it belongs to
        val qualityRuleWithUpdatedResourcesInsidePlannedQuery =
        if (resourcesInsidePlannedQueryToUpdate.isEmpty)
          qualityRuleWithGlobalResource else {
          val updatedPlannedQuery: Option[PlannedQuery] =
            qualityRuleWithGlobalResource.plannedQuery.map(pq =>
              pq.copy(resources = resourcesInsidePlannedQueryToUpdate ++ resourcesInsidePlannedQueryNotToUpdate))

          qualityRuleWithGlobalResource.copy(
            plannedQuery =
              if (qualityRuleWithGlobalResource.plannedQuery.isEmpty) None
              else updatedPlannedQuery
          )
        }

        val newHadoopConfigUri =
          if (qualityRuleWithUpdatedResourcesInsidePlannedQuery.hadoopConfigUri.isDefined)
            qualityRuleWithUpdatedResourcesInsidePlannedQuery.hadoopConfigUri
          else hdfsResource

        qualityRuleWithUpdatedResourcesInsidePlannedQuery.copy(hadoopConfigUri = newHadoopConfigUri)
      }
    }

  private def sendRequestDataAsset(metadatapath: String, absolutePath: Boolean = false): Future[String] = {
    val metadataPathString = if(absolutePath) metadatapath else metadatapath + ":"
    val metadataPathLike = URLEncoder.encode(metadataPathString , StandardCharsets.UTF_8.toString)
    val resultGet = doRequest(
      uri = uri,
      resource = getDetailsDataAssetsEndpoint.concat(metadataPathLike),
      body = None,
      cookies = Seq.empty,
      headers = rawHeaders,
      forceContentAsJson = true
    )
    resultGet.map{
      case(status,details) => log.debug(s"status ${status.value} and response $details")
      details
    }
  }


  private def getPlannedQualityRulesFromApi(modTime: String): Future[String] = {
    val pageSizeQuery = s"&page=0&size=${if(modTime.equals(epochTimeDatetime)) initialPageSize else defaultPageSize}"
    //val query = URLEncoder.encode(modTime, StandardCharsets.UTF_8.toString) + pageSizeQuery
    val query = modTime + pageSizeQuery

    val resultGet = doRequest(
      uri = uri,
      resource = getPlannedQREndpoint.concat(query),
      body = None,
      cookies = Seq.empty,
      headers =  rawHeaders,
      forceContentAsJson = true
    )

    resultGet.map{ case(status,rules) =>
      log.debug(s"Quality rule request for modification date $EpochTime received with " +
        s"status ${status.value} and response $rules")
      rules
    }
  }
}

object PlannedQualityRuleActor {

  def props: Props = Props[PlannedQualityRuleActor]

  case object RetrievePlannedQualityRulesTick

}
