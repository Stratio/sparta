/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.models.governance

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.enumerators.QualityRuleTypeEnum._
import com.stratio.sparta.core.models.SpartaQualityRule._
import com.stratio.sparta.core.models._
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat

import scala.util.{Failure, Success, Try}

object QualityRuleParser extends SLF4JLogging{

  implicit class Parse2QualityRule(governanceQualityRule: GovernanceQualityRule) {

    def parse(stepName: String, outputName: String): Seq[SpartaQualityRule] = {
      governanceQualityRule.content.map(x => {
        val id = x.id
        val metadataPath = x.metadataPath
        val qrName = x.name
        val qrScope = x.catalogAttributeType
        val logicalOp = x.parameters.filter.map(_.`type`)
        val enabled = x.active
        val threshold = x.resultUnit.value.toDouble
        val thresholdOperation = x.resultOperation
        val thresholdOperationType = x.resultOperationType
        val thresholdActionType = SpartaQualityRuleThresholdActionType(`type` = x.resultAction.`type`)
        val qrType: QualityRuleType = x.resultExecute.`type`
        val tenantName = Option(x.tenant)
        val creationDateValue = dateParser(x.createdAt)
        val modificationDateValue = dateParser(x.modifiedAt)

        val thresholdValue = SpartaQualityRuleThreshold(threshold, thresholdOperation, thresholdOperationType, thresholdActionType)

        val predicates: Seq[SpartaQualityRulePredicate] = x.parameters.filter.fold(Seq.empty[SpartaQualityRulePredicate]){_.cond.map(x => {

          SpartaQualityRulePredicate(
            x.`type`,
            x.order,
            x.param.map{ seqNameValue => seqNameValue.map(_.value)}.getOrElse(Seq.empty[String]),
            x.attribute,
            x.operation)
        })}

        SpartaQualityRule(
          id = id,
          metadataPath = metadataPath,
          name = qrName,
          qualityRuleScope = qrScope,
          logicalOperator = logicalOp,
          enable = enabled,
          threshold = thresholdValue,
          predicates = predicates,
          stepName = stepName,
          outputName = outputName,
          qualityRuleType = qrType,
          tenant = tenantName,
          creationDate = creationDateValue,
          modificationDate = modificationDateValue)
      })
    }

    def parse(): Seq[SpartaQualityRule] = {
      governanceQualityRule.content.map(x => {
        val id = x.id
        val metadataPath = x.metadataPath
        val qrName = x.name
        val qrScope = x.catalogAttributeType
        val logicalOp = x.parameters.filter.map(_.`type`)
        val enabled = x.active
        val thresholdValue = x.resultUnit.value.toDouble
        val thresholdOperation = x.resultOperation
        val thresholdOperationType = x.resultOperationType
        val thresholdActionType = SpartaQualityRuleThresholdActionType(`type` = x.resultAction.`type`)
        val stepNameWf = x.parameters.advanced.fold(DefaultPlannedQRInputStepName){res => inputStepNameForPlannedQR(res.resources.resource)}
        val outputNameWf = "metrics"
        val qrType: QualityRuleType = x.resultExecute.`type`
        val initDateValue = x.resultExecute.config.map(_.scheduling.initialization)
        val periodValue = x.resultExecute.config.map(_.scheduling.period)
        val sparkResourcesSizeValue = Some(x.resultExecute.config.fold("S")(_.executionOptions.size))
        val plannedQueryValue = x.parameters.advanced.map{ adv =>
          PlannedQuery(
            query = adv.query,
            queryReference = adv.queryReference,
            resource = adv.resources.resource,
            metadatapathResource= adv.resources.metadataPath,
            urlResource = adv.resources.url
          )
        }
        val tenantValue = Option(x.tenant)
        val creationDateValue = dateParser(x.createdAt)
        val modificationDateValue = dateParser(x.modifiedAt)

        val threshold = SpartaQualityRuleThreshold(thresholdValue, thresholdOperation, thresholdOperationType, thresholdActionType)

        val predicates: Seq[SpartaQualityRulePredicate] = x.parameters.filter.fold(Seq.empty[SpartaQualityRulePredicate]){_.cond.map(x => {

          SpartaQualityRulePredicate(
            x.`type`,
            x.order,
            x.param.map{ seqNameValue => seqNameValue.map(_.value)}.getOrElse(Seq.empty[String]),
            x.attribute,
            x.operation)
        })}

        SpartaQualityRule(
          id = id,
          metadataPath = metadataPath,
          name = qrName,
          qualityRuleScope = qrScope,
          logicalOperator = logicalOp,
          enable = enabled,
          threshold = threshold,
          predicates = predicates,
          stepName = stepNameWf,
          outputName = outputNameWf,
          qualityRuleType = qrType,
          plannedQuery = plannedQueryValue,
          tenant = tenantValue,
          creationDate = creationDateValue,
          modificationDate = modificationDateValue,
          initDate = initDateValue,
          period = periodValue,
          sparkResourcesSize = sparkResourcesSizeValue)
      })
    }
  }

  private def dateParser(date: String): Option[Long] = {
    val formatter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC)
    Try{
      formatter.parseDateTime(date).getMillis
    } match {
      case Success(v) => Some(v)
      case Failure(ex) =>
        log.error(s"Impossible to parse given date: $date. Error: ${ex.getLocalizedMessage}")
        None
    }
  }
}