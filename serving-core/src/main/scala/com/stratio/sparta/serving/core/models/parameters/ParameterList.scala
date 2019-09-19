/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.models.parameters

import com.stratio.sparta.serving.core.models.EntityAuthorization
import org.joda.time.DateTime
import com.stratio.sparta.core.properties.ValidatingPropertyMap._

case class ParameterList(
                          name: String,
                          id: Option[String] = None,
                          parameters: Seq[ParameterVariable] = Seq.empty,
                          tags: Seq[String] = Seq.empty,
                          description: Option[String] = None,
                          creationDate: Option[DateTime] = None,
                          lastUpdateDate: Option[DateTime] = None,
                          parent: Option[String] = None,
                          versionSparta: Option[String] = None
                        ) extends EntityAuthorization {

  def authorizationId: String = name

  def mapOfParameters: Map[String, String] = parameters.flatMap { param =>
    param.value.notBlank.map(value => param.name -> value)
  }.toMap

  def mapOfParametersWithPrefix: Map[String, String] = mapOfParameters.flatMap { case (key, value) =>
    Seq(s"$name.$key" -> value) ++
      parent.fold(Seq.empty[(String, String)]) { parentList => Seq(s"$parentList.$key" -> value) }
  }

  def getParameterValue(name: String): Option[String] =
    parameters.find(variable => variable.name == name).flatMap(_.value)

  import ParameterList._

  def +(that: ParameterVariable): ParameterList = {
    val oldParams =  parametersToMap(this.parameters)
    val newParams: Map[String, ParameterVariable] = oldParams ++ Map(that.name -> that)
    this.copy(parameters = newParams.values.toSeq)
  }

  def ++(that: Seq[ParameterVariable]): ParameterList = {
    val oldParams =  parametersToMap(this.parameters)
    val newParams: Map[String, ParameterVariable] = oldParams ++ parametersToMap(that)
    this.copy(parameters = newParams.values.toSeq)
  }

  def ++(that: Map[String,ParameterVariable]): ParameterList = {
    val oldParams =  parametersToMap(this.parameters)
    val newParams: Map[String, ParameterVariable] = oldParams ++ that
    this.copy(parameters = newParams.values.toSeq)
  }
}

object ParameterList {

  def parametersToMap(parameters: Seq[ParameterVariable]): Map[String, ParameterVariable] =
    parameters.map(parameter => parameter.name -> parameter).toMap
}
