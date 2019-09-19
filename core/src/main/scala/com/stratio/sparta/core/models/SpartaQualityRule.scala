/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.core.models

import com.stratio.sparta.core.enumerators.QualityRuleTypeEnum.QualityRuleType
import com.stratio.sparta.core.enumerators.QualityRuleTypeEnum._
import com.stratio.sparta.core.utils.RegexUtils._

import scala.util.{Failure, Success, Try}

case class SpartaQualityRule(id: Long,
                             metadataPath: String,
                             name: String,
                             qualityRuleScope: String,
                             logicalOperator: Option[String],
                             enable: Boolean,
                             threshold: SpartaQualityRuleThreshold,
                             predicates: Seq[SpartaQualityRulePredicate],
                             stepName: String,
                             outputName: String,
                             executionId: Option[String] = None,
                             qualityRuleType: QualityRuleType = Reactive,
                             plannedQuery: Option[PlannedQuery] = None,
                             tenant: Option[String] = None,
                             creationDate: Option[Long]= None,
                             modificationDate: Option[Long] = None,
                             initDate: Option[Long] = None,
                             period: Option[Long] = None,
                             sparkResourcesSize: Option[String] = None,
                             taskId: Option[String] = None
                            ) {
  override def toString: String = {
    s"id: $id, metadataPath: $metadataPath, " +
      s"enable: $enable, stepName: $stepName, " +
      s"outputName: $outputName, " +
      s"lastUpdate: ${modificationDate.getOrElse("NaN")}, "+
      s"${
        if (predicates.nonEmpty) "predicates: " + s"${predicates.mkString(s" ${logicalOperator.get} ")}"
        else "advancedQuery: " + s"${plannedQuery.fold("Empty query") { pq => pq.query }}"}, " +
      s"threshold: $threshold, " + s"${executionId.fold(""){ exId => s" executionId: $exId" }}" +
      s"type: $qualityRuleType " + s"${
      if (qualityRuleType == Planned)
        s"to be executed each ${period.getOrElse("NaN")} seconds with cluster resources size ${sparkResourcesSize.getOrElse("NotDefined: set to default PlannedQRSettings")} ${taskId.fold(""){ taskID => s"and related to the taskId $taskID "}}"
      else ""}"
  }
}

object SpartaQualityRule {

  val DefaultPlannedQRInputStepName = "plannedQR_input"

  val availablePlannedQRSparkResourcesSize = Seq("PlannedQRSettings","S","M","L","XL")

  def inputStepNameForPlannedQR(resource: String): String = {
    s"${resource}_input"
  }

  implicit class SpartaQualityRuleOps(instance: SpartaQualityRule) {
    import instance._

    def validSpartaQR: Boolean =
      cannotHaveBothPredicatesAndQuery && isEitherAnAdvancedOrASimpleValidQR && isValidPlannedOrProactive && hasModificationAndCreationTime

    def inputStepNameForPlannedQR: String = {
      plannedQuery.fold(DefaultPlannedQRInputStepName)(pq => s"${pq.resource}_input")
    }

    /**
      * Inside a workflow, no step name can match any existing table inside the Catalog.
      * Still, since we need to operate on that table, we will use the inputStepNameForPlannedQR (resource name + "_input")
      * that is set as stepName inside the QR and as the writer tableName property inside the Input of the PlannedQR workflow.
      * This means that we should change the provided query too when executing the QR.
      * */
    def retrieveQueryReplacedResource: String = {
      plannedQuery.map(pq =>
        replaceWholeWord(inputText = pq.query, wordToReplace = pq.resource, replacement = inputStepNameForPlannedQR))
        .getOrElse(throw new RuntimeException("Impossible to execute the planned QR query: the query does not contain the specified resource"))
    }

    private def validQuery: Boolean =
      plannedQuery.fold(predicates.nonEmpty) { pq =>
        val mustStartWith = "Select count(*) from"
        val queryTrimmedAndSpaceNormalized = pq.query.trimAndNormalizeString
        val nonEmptyQuery = queryTrimmedAndSpaceNormalized.nonEmpty
        val validPrefixQuery= queryTrimmedAndSpaceNormalized.startWithIgnoreCase(mustStartWith)
        val queryContainsTableResource =containsWholeWord(inputText = queryTrimmedAndSpaceNormalized, wordToFind = pq.resource)
        (nonEmptyQuery, validPrefixQuery, queryContainsTableResource) match {
          case (true, true, true) => true
          case (false, _, _ ) => throw new RuntimeException("The query specified for the planned quality rule is empty")
          case (true,false,_) => throw new RuntimeException("The query specified for the planned quality rule does not start with 'select count(*) from'")
          case (true, true, false) => throw new RuntimeException("The query specified for the planned quality rule does not contain the specified resource")
        }
      }

    private def isEitherAnAdvancedOrASimpleValidQR: Boolean = {
      val advanced = Try(isValidAdvancedQR)
      val simple = Try(isValidSimpleQR)
      (simple, advanced) match {
        case (Success(true), Failure(_)) => true
        case (Failure(_), Success(true)) => true
        case (Success(true), Success(true)) =>
          throw new RuntimeException("The quality rule is invalid: it is both a simple and an advanced quality rule")
        case (Failure(exceptionSimple),Failure(exceptionAdvanced)) =>
          throw new RuntimeException(s"The quality rule is neither a simple nor an advanced type. ${exceptionSimple.getLocalizedMessage}. ${exceptionAdvanced.getLocalizedMessage}")
        case (_,_) => throw new RuntimeException(s"The quality rule is neither a simple nor an advanced type.")
      }
    }

    private def isValidAdvancedQR: Boolean = {
      if (qualityRuleType == Planned && plannedQuery.isDefined) {
        validQuery && (
            if (plannedQuery.get.resource.trim.nonEmpty) true
            else
              throw new RuntimeException(s"A resource table must be specified in the Planned advanced query")
          )
      } else throw new RuntimeException(s"An advanced quality rule cannot be Proactive")
    }

    private def isValidSimpleQR: Boolean = {
      if (logicalOperator.isEmpty) throw new RuntimeException("The quality rule has no defined operator")
      else {
        val validOperator = logicalOperator.fold(false){op => op.equalsIgnoreCase("OR") || op.equalsIgnoreCase("AND")}
        (validOperator, predicates.nonEmpty) match {
          case (true, true) => true
          case (false, _) => throw new RuntimeException(s"The operator defined for the quality rule is not supported ${logicalOperator.get}")
          case (_, false) => throw new RuntimeException(s"A simple quality rule must have some predicates")
        }
      }
    }

    private def hasModificationAndCreationTime: Boolean =
      if (modificationDate.isDefined && creationDate.isDefined) true
      else throw new RuntimeException("The quality rule is invalid: it is mandatory to have the fields creationDate " +
        "and modificationDate defined")

    private def cannotHaveBothPredicatesAndQuery: Boolean =
      if (!(predicates.nonEmpty && plannedQuery.isDefined)) true
      else throw new RuntimeException("The quality rule is invalid: it defines both predicates and an advanced query")

    private def isValidPlannedOrProactive: Boolean =
      (qualityRuleType, Try(hasValidPlannedInfo)) match {
        case (Planned, Success(true)) => true
        case (Planned, Failure(exception)) =>
          throw new RuntimeException(s"The quality rule is a Planned quality rule but its scheduling information is invalid: ${exception.getLocalizedMessage}")
        case (Planned, _) =>
          throw new RuntimeException(s"The quality rule is a Planned quality rule but its scheduling information is invalid")
        case (Reactive, Success(true)) => throw new RuntimeException(s"The quality rule is Proactive but scheduling information was provided too")
        case (Reactive, _) => true
      }

    private def hasValidPlannedInfo: Boolean = {
      val validInitDate = initDate
        .fold(throw new RuntimeException(s"No initDate was set for this planned quality rule")) { inDate =>
          if (inDate >= 0L) true
          else
            throw new RuntimeException(s"The selected periodicity for this planned quality rule is not valid")}
      val validPeriod = period
        .fold(throw new RuntimeException(s"No periodicity was set for this planned quality rule")){ p =>
          if (p > 1L) true
          else throw new RuntimeException(s"The selected periodicity for this planned quality rule is not valid")}
      val validSparkSize =
        if (sparkResourcesSize.isDefined) {
          if (availablePlannedQRSparkResourcesSize.contains(sparkResourcesSize.get)) true
          else throw new RuntimeException(s"The selected Spark resource size is not available in Sparta")
        } else throw new RuntimeException(s"No Spark resource size for this planned quality rule was passed")
      validInitDate && validPeriod && validSparkSize
    }
  }

}

case class SpartaQualityRulePredicate(`type`: Option[String] = None,
                                      order: Int,
                                      operands: Seq[String],
                                      field: String,
                                      operation: String){
  override def toString: String = {
    s"IF column ($field) ${prettyPrintOperation(operation)} ${operands.mkString(",")}"
  }

  private def prettyPrintOperation(operation: String) : String = {
    val leaveUnchanged = Set("IS NOT NULL", "IS NULL", "IN", "NOT IN", "LIKE", "NOT LIKE", "REGEX", "IS DATE", "IS NOT DATE", "IS TIMESTAMP", "IS NOT TIMESTAMP")

    operation match{
      case op if leaveUnchanged.contains(op.toUpperCase) => op.toLowerCase
      case "=" => "is equal to"
      case ">" => "is greater than"
      case ">=" => "is greater than or equal to"
      case "<" => "is less than"
      case "<=" => "is less than or equal to"
      case "<>" => "is not equal to"
      case op => s"$op (this operator is not allowed in Sparta. It will be discarded)"
    }
  }
}

case class SpartaQualityRuleThreshold(value: Double,
                                      operation: String,
                                      `type`: String,
                                      actionType: SpartaQualityRuleThresholdActionType){
  override def toString: String = {
    s"passedEvents $operation $value"
  }
}

case class SpartaQualityRuleThresholdActionType(`type`: String, path: Option[String] = None)



