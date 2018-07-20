/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.serving.core.models.EntityAuthorization
import org.joda.time.DateTime
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._

case class TemplateElement(
                            id: Option[String] = None,
                            templateType: String,
                            name: String,
                            description: Option[String],
                            className: String,
                            classPrettyName: String,
                            configuration: Map[String, JsoneyString] = Map(),
                            creationDate: Option[DateTime] = None,
                            lastUpdateDate: Option[DateTime] = None,
                            supportedEngines: Seq[ExecutionEngine] = Seq.empty[ExecutionEngine],
                            executionEngine: Option[ExecutionEngine] = Option(Streaming),
                            versionSparta: Option[String] = None
                          ) extends EntityAuthorization {

  def authorizationId: String = name

}

object TemplateType extends Enumeration {

  type TemplateType = Value

  val InputValue = "input"
  val OutputValue = "output"
  val TransformationValue = "transformation"
  val input = Value(InputValue)
  val output = Value(OutputValue)
  val transformation = Value(TransformationValue)

  val AllowedTypes = Seq(input, output, transformation)
}
