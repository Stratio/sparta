/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.models

import com.stratio.sparta.core.enumerators.QualityRuleResourceTypeEnum
import com.stratio.sparta.core.enumerators.QualityRuleResourceTypeEnum._
import com.stratio.sparta.core.models.SpartaQualityRule.{mandatoryOptionsHDFS, mandatoryOptionsJDBC}
import com.stratio.sparta.core.models.qualityrule.ValidationQR

import scala.util.{Success, Try}

case class ResourcePlannedQuery(id: Long,
                                metadataPath: String,
                                resource: String,
                                typeResource: QualityRuleResourceType = QualityRuleResourceTypeEnum.XD,
                                listProperties: Seq[PropertyKeyValue] = Seq.empty[PropertyKeyValue]){
  override def toString: String =
    s"""id: $id, metadataPath: $metadataPath, resource : $resource, typeResource: $typeResource, listProperties: [${listProperties.map(elem => s"${elem.key}" ->  s"${elem.value}").mkString(",")}]"""

}

object ResourcePlannedQuery extends ValidationQR {

  def allDetailsForSingleResource(res : ResourcePlannedQuery): Try[Boolean] = {
    res.typeResource match {
      case QualityRuleResourceTypeEnum.XD => Success(true)
      case QualityRuleResourceTypeEnum.HDFS =>
        val missingMandatory = mandatoryOptionsHDFS.filterNot(keyProperty => res.listProperties.exists(prop => prop.key == keyProperty))
        if (missingMandatory.isEmpty) Success(true) else errorAsFailure(s"The resource with ${res.metadataPath} has not the following mandatory details: ${missingMandatory.mkString(",")}")
      case QualityRuleResourceTypeEnum.JDBC =>
        val missingMandatory = mandatoryOptionsJDBC.filterNot(keyProperty => res.listProperties.exists(prop => prop.key == keyProperty))
        if (missingMandatory.isEmpty) Success(true) else errorAsFailure(s"The resource with ${res.metadataPath} has not the following mandatory details: ${missingMandatory.mkString(",")}")
    }
  }
}
