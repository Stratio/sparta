/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.core.models

import com.stratio.sparta.core.enumerators.QualityRuleResourceTypeEnum
import com.stratio.sparta.core.enumerators.QualityRuleResourceTypeEnum.QualityRuleResourceType
import com.stratio.sparta.core.enumerators.QualityRuleTypeEnum.QualityRuleType
import com.stratio.sparta.core.enumerators.QualityRuleTypeEnum._
import com.stratio.sparta.core.models.qualityrule.ValidationQR
import com.stratio.sparta.core.utils.QualityRulesUtils
import com.stratio.sparta.core.utils.RegexUtils._
import org.joda.time.{DateTime, DateTimeZone}
import MetadataPath._

import scala.util.{Failure, Success, Try}

case class SpartaQualityRule(id: Long,
                             metadataPath: String,
                             name: String,
                             qualityRuleScope: String, //Table or Resource
                             logicalOperator: Option[String],
                             metadataPathResourceType: Option[QualityRuleResourceType] = None,
                             metadataPathResourceExtraParams : Seq[PropertyKeyValue] = Seq.empty[PropertyKeyValue],
                             enable: Boolean,
                             threshold: SpartaQualityRuleThreshold,
                             predicates: Seq[SpartaQualityRulePredicate],
                             stepName: String,
                             outputName: String,
                             executionId: Option[String] = None,
                             qualityRuleType: QualityRuleType = Reactive, //Planned or Reactive
                             plannedQuery: Option[PlannedQuery] = None,
                             tenant: Option[String] = None,
                             creationDate: Option[Long]= None,
                             modificationDate: Option[Long] = None,
                             schedulingDetails : Option[SchedulingDetails] = None,
                             taskId: Option[String] = None,
                             hadoopConfigUri: Option[String] = None
                            ) {

  import SpartaQualityRule._

  override def toString: String = {
    def planningDetails(schedulingDetails: SchedulingDetails): String =
      s"""
         | to be executed each ${schedulingDetails.period.getOrElse("NaN")} seconds
         | starting from ${schedulingDetails.initDate.fold("NaN "){ date => new DateTime(date, DateTimeZone.UTC).toString()}}
         | with cluster resources size ${schedulingDetails.sparkResourcesSize.fold("NotDefined: set to default PlannedQRSettings"){ size => size}}
         |""".stripMargin.removeAllNewlines

    def printListPropertyKeyValue(listPropertyKeyValue:  Seq[PropertyKeyValue]) =
      s"""[${listPropertyKeyValue.map(elem => s"${elem.key}" ->  s"${elem.value}").mkString(",")}]"""

    s"id: $id, metadataPath: $metadataPath, " +
      s"enable: $enable, stepName: $stepName, " +
      s"outputName: $outputName, " +
      s"lastUpdate: ${modificationDate.getOrElse("NaN")}, " +
      s"metadataPathResourceType: ${metadataPathResourceType.fold("None"){x => x.toString}}, " +
      s"metadataPathResourceExtraParams: ${printListPropertyKeyValue(metadataPathResourceExtraParams)}, " +
      s"hadoopConfigUri: ${hadoopConfigUri.fold("None"){x => x.toString}}, " +
      s"${
        if (predicates.nonEmpty) "predicates: " + s"${predicates.mkString(s" ${logicalOperator.get} ")}"
        else "advancedQuery: " + s"${plannedQuery.fold("Empty plannedQuery") { pq => pq.prettyPrint }}"
      }, " +
      s"threshold: $threshold, " + s"${executionId.fold("") { exId => s" executionId: $exId" }}" +
      s"type: $qualityRuleType " + s"${
      if (qualityRuleType == Planned)
        s"${schedulingDetails.fold(""){x => planningDetails(x)}} ${taskId.fold("") { taskID => s"and related to the taskId $taskID " }}"
      else ""
    }"
  }

  def retrieveQueryForInputStepForPlannedQR: String = {
    /** If there is a queryReference, use it once the placeholders have been replaced.
      * If there isn't and it's a simple QR it should be a select (*) from table name
      */

    val dummyQuery = "select 1 as id"

    lazy val advancedPlannedQuery: Option[String] = this.plannedQuery.map(pq =>
      replacePlaceholdersWithResources(pq.queryReference, pq.resources))

    lazy val simplePlannedQuery: Option[String] = {
      val metadataQR = MetadataPath.fromMetadataPathString(metadataPath)
      for {
        typeResource <- metadataPathResourceType
      } yield {
        typeResource match {
          case QualityRuleResourceTypeEnum.XD =>
            val tableName = getTableName(metadataPath, metadataPathResourceType)
            val database = metadataQR.path.fold(""){ db =>
              s"${db.stripPrefixWithIgnoreCase("/")}."}
            s"select * from $database$tableName"
          case QualityRuleResourceTypeEnum.JDBC =>
            val tableName = getTableName(metadataPath, metadataPathResourceType)
            s"select * from $tableName"
          case QualityRuleResourceTypeEnum.HDFS =>
            val tableName = getTableName(metadataPath, metadataPathResourceType)
            s"select * from $tableName"
        }
      }
    }
    advancedPlannedQuery.getOrElse(simplePlannedQuery.getOrElse(dummyQuery))
  }

}

object SpartaQualityRule extends ValidationQR {

  // These are the key we expect to find as details of the resources
  val qrConnectionURI = "connectionURI"
  val qrTlsEnabled = "tlsEnabled"
  val qrSecurity = "securityType"
  val qrDriver = "driver"
  val qrSchemaFile = "schema"
  val qrVendor = "vendor"

  val defaultPlannedQRMainResourceID = -1L

  val mandatoryOptions = Set(qrConnectionURI, qrTlsEnabled, qrDriver, qrSchemaFile, qrVendor, qrSecurity)
  val mandatoryOptionsHDFS = Seq(qrSchemaFile, qrConnectionURI)
  val mandatoryOptionsJDBC = Seq(qrConnectionURI, qrVendor)


  def filterQRMandatoryOptions(opt : Seq[PropertyKeyValue]) : Seq[PropertyKeyValue] =
    opt.filterNot{ property => mandatoryOptions.contains(property.key)}

  val inputStepNamePlannedQR = "plannedQR_input"
  val outputStepNamePlannedQR = "metrics"

  val availablePlannedQRSparkResourcesSize = Seq("PlannedQRSettings","S","M","L","XL")

  implicit class SpartaQualityRuleOps(instance: SpartaQualityRule) {

    import instance._

    def validSpartaQR: Try[Boolean] =
      checkValidity(cannotHaveBothPredicatesAndQuery ::
        isEitherAnAdvancedOrASimpleValidQR :: isValidPlannedOrProactive :: hasModificationAndCreationTime :: allDetailsForResources :: Nil)

    /**
      * Inside a workflow, no step name can match any existing table inside the Catalog.
      * Still, since we need to operate on that table, we will use the inputStepNameForPlannedQR (resource name + "_input")
      * that is set as stepName inside the QR and as the writer tableName property inside the Input of the PlannedQR workflow.
      * This means that we should change the provided query too when executing the QR.
      **/
    def retrieveQueryReplacedResources: String = {
      val replacedQuery = plannedQuery.fold("") { pq =>
        replacePlaceholdersWithResources(pq.query, pq.resources)
      }
      if (replacedQuery.isEmpty || containsPlaceholders(replacedQuery))
        throw new RuntimeException("Impossible to execute the planned QR query: some placeholders could not be replaced with the related resource")
      else replacedQuery
    }

    private def checkValidity(seqTry: Seq[Try[Boolean]]): Try[Boolean] = {
      //Merge together all the Failures
      val seqFailures = seqTry.filter(elem => elem.isFailure)
      if (seqFailures.isEmpty) Success(true)
      else {
        val errorAsMsg = seqFailures.flatMap {
          case Failure(e) => Some(e.getLocalizedMessage)
          case Success(_) => None
        }.mkString("\n")
        errorAsFailure(errorAsMsg)
      }
    }

    private def allDetailsForResources: Try[Boolean] = {
      val seqResources: Seq[ResourcePlannedQuery] = QualityRulesUtils.sequenceMetadataResources(instance)
      val checkResources = seqResources.map { res => ResourcePlannedQuery.allDetailsForSingleResource(res) }
      checkValidity(checkResources)
    }


    private def validQuery: Try[Boolean] =
      plannedQuery.fold(Try(predicates.nonEmpty)) { pq =>
        val mustStartWith = "Select count"
        val queryTrimmedAndSpaceNormalized = pq.query.trimAndNormalizeString
        val nonEmptyQuery = queryTrimmedAndSpaceNormalized.nonEmpty
        val validPrefixQuery = queryTrimmedAndSpaceNormalized.startWithIgnoreCase(mustStartWith)
        val queryContainsTableResource = !containsPlaceholders(replacePlaceholdersWithResources(pq.query, pq.resources))
        (nonEmptyQuery, validPrefixQuery, queryContainsTableResource) match {
          case (true, true, true) => Success(true)
          case (false, _, _) => errorAsFailure(s"The query specified for the planned quality rule with id $id is empty")
          case (true, false, _) => errorAsFailure(s"The query specified for the planned quality rule with id $id does not start with 'select count(*) from'")
          case (true, true, false) => errorAsFailure(s"The query specified for the planned quality rule with id $id does not contain the specified resource")
        }
      }

    private def isEitherAnAdvancedOrASimpleValidQR: Try[Boolean] = {
      (isValidSimpleQR, isValidAdvancedQR) match {
        case (Success(true), Failure(_)) => Success(true)
        case (Failure(_), Success(true)) => Success(true)
        case (Success(true), Success(true)) =>
          errorAsFailure(s"The quality rule with id $id is invalid: it is both a simple and an advanced quality rule")
        case (Failure(exceptionSimple), Failure(exceptionAdvanced)) =>
          errorAsFailure(s"The quality rule with id $id is neither a simple nor an advanced type. ${exceptionSimple.getLocalizedMessage}. ${exceptionAdvanced.getLocalizedMessage}")
        case (_, _) => errorAsFailure(s"The quality rule with id $id is neither a simple nor an advanced type.")
      }
    }

    private def isValidAdvancedQR: Try[Boolean] = {
      if (qualityRuleType == Planned && plannedQuery.isDefined) {
        checkValidity(validQuery :: (
          if (plannedQuery.get.resources.nonEmpty) Success(true)
          else
            errorAsFailure(s"One or more resources must be specified in the Planned advanced query")
          ) :: Nil)
      } else errorAsFailure(s"An advanced quality rule cannot be Proactive")

    }

    private def isValidSimpleQR: Try[Boolean] = {
      if (logicalOperator.isEmpty) errorAsFailure(s"The quality rule with id $id has no defined operator")
      else {
        val validOperator = logicalOperator.fold(false) { op => op.equalsIgnoreCase("OR") || op.equalsIgnoreCase("AND") }
        (validOperator, predicates.nonEmpty) match {
          case (true, true) => Success(true)
          case (false, _) => errorAsFailure(s"The operator defined for the quality rule with id $id is not supported ${logicalOperator.get}")
          case (_, false) => errorAsFailure(s"A simple quality rule (id $id) must have some predicates")
        }
      }
    }

    private def hasModificationAndCreationTime: Try[Boolean] =
      if (modificationDate.isDefined && creationDate.isDefined) Success(true)
      else errorAsFailure(s"The quality rule with id $id is invalid: it is mandatory to have the fields creationDate " +
        "and modificationDate defined")

    private def cannotHaveBothPredicatesAndQuery: Try[Boolean] =
      if (!(predicates.nonEmpty && plannedQuery.isDefined)) Success(true)
      else errorAsFailure(s"The quality rule with id $id is invalid: it defines both predicates and an advanced query")

    private def isValidPlannedOrProactive: Try[Boolean] =
      (qualityRuleType, hasValidPlannedInfo) match {
        case (Planned, Success(true)) => Success(true)
        case (Planned, Failure(exception)) =>
          errorAsFailure(s"The quality rule with id $id is a Planned quality rule but its scheduling information is invalid: ${exception.getLocalizedMessage}")
        case (Planned, _) =>
          errorAsFailure(s"The quality rule with id $id is a Planned quality rule but its scheduling information is invalid")
        case (Reactive, Success(true)) =>
          errorAsFailure(s"The quality rule is Proactive but scheduling information was provided too")
        case (Reactive, _) => Success(true)
      }

    private def hasValidPlannedInfo: Try[Boolean] = {
      schedulingDetails.fold(errorAsFailure("No scheduling details were provided")) {
        details =>
          val validInitDate = details.initDate
            .fold(errorAsFailure(s"No initDate was set for the planned quality rule with id $id")) { inDate =>
              if (inDate >= 0L) Success(true)
              else
                errorAsFailure(s"The selected periodicity for the planned quality rule with id $id is not valid")
            }
          //Can be empty, but if it's not empty it should be > 1
          val validPeriod = details.period
            .fold(Try(true)) { p =>
              if (p > 1L) Success(true)
              else errorAsFailure(s"The selected periodicity for the planned quality rule with id $id is not valid")
            }
          val validSparkSize: Try[Boolean] =
            if (details.sparkResourcesSize.isDefined) {
              if (availablePlannedQRSparkResourcesSize.contains(details.sparkResourcesSize.get)) Success(true)
              else errorAsFailure(s"The selected Spark resource size in the qualityRule with id $id is not available in Sparta")
            } else errorAsFailure(s"No Spark resource size for the planned quality rule with id $id was passed")
          checkValidity(validInitDate :: validPeriod :: validSparkSize :: Nil)
      }
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
    val leaveUnchanged = Set("IS NOT NULL", "IS NULL", "IN", "NOT IN", "LIKE", "NOT LIKE", "REGEX", "IS DATE", "IS NOT DATE", "IS TIMESTAMP", "IS NOT TIMESTAMP", "IS EMPTY", "IS NOT EMPTY")

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


case class SchedulingDetails(initDate: Option[Long] = None, period: Option[Long] = None, sparkResourcesSize: Option[String] = None)