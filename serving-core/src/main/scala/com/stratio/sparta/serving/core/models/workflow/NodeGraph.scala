/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.models.enumerators.NodeArityEnum.NodeArity
import com.stratio.sparta.serving.core.models.dto.Dto
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._

case class NodeGraph(
                      name: String,
                      stepType: String,
                      className: String,
                      classPrettyName: String,
                      arity: Seq[NodeArity],
                      writer: WriterGraph,
                      description: Option[String] = None,
                      uiConfiguration: Option[NodeUiConfiguration] = None,
                      configuration: Map[String, JsoneyString] = Map(),
                      nodeTemplate: Option[NodeTemplateInfo] = None,
                      supportedEngines: Seq[ExecutionEngine] = Seq.empty[ExecutionEngine],
                      executionEngine: Option[ExecutionEngine] = Option(Streaming)
                    )

/**
  * Wrapper class used by the api consumers
  */
case class NodeGraphDto(name: String, stepType: String) extends Dto
