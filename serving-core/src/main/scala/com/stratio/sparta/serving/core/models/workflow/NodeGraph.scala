/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions}
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.serving.core.models.dto.Dto
import com.stratio.sparta.serving.core.models.enumerators.DataType.DataType
import com.stratio.sparta.serving.core.models.enumerators.NodeArityEnum.NodeArity
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
import com.stratio.sparta.core.properties.ValidatingPropertyMap._

import scala.util.Try

case class NodeGraph(
                      name: String,
                      stepType: String,
                      className: String,
                      classPrettyName: String,
                      arity: Seq[NodeArity],
                      writer: Option[WriterGraph], //TODO remove in future versions
                      description: Option[String] = None,
                      uiConfiguration: Option[NodeUiConfiguration] = None,
                      configuration: Map[String, JsoneyString] = Map(),
                      nodeTemplate: Option[NodeTemplateInfo] = None,
                      supportedEngines: Seq[ExecutionEngine] = Seq.empty[ExecutionEngine],
                      executionEngine: Option[ExecutionEngine] = Option(Streaming),
                      supportedDataRelations: Option[Seq[DataType]] = None,
                      lineageProperties: Seq[NodeLineageProperty] = Seq.empty[NodeLineageProperty],
                      outputsWriter: Seq[OutputWriter] = Seq.empty[OutputWriter],
                      errorTableName: Option[String] = None
                    ) {

  def priority: Int = Try(configuration.mapValues(_.toString).getOrElse("priority", "0").toInt).getOrElse(0)

  def outputOptions: OutputOptions = {
    OutputOptions(
      outputWriterOptions = {
        val outputWriterOptions = outputsWriter.map(_.toOutputWriterOptions(name, outputErrorTableName))

        if (outputWriterOptions.nonEmpty) {
          outputWriterOptions
        } else {
          writer.fold(Seq(OutputWriterOptions.defaultOutputWriterOptions(name))) { writerGraph =>
            Seq(writerGraph.toOutputWriterOptions(name))
          }
        }
      })
  }

  def outputTableName(outputStepName : String): String = {
    outputsWriter.find(_.outputStepName == outputStepName)
      .flatMap(_.tableName.notBlank)
      .orElse(writer.flatMap(_.tableName.notBlank))
      .getOrElse(name)
  }

  def outputDiscardTableName(outputStepName : String): Option[String] = {
    outputsWriter.find(_.outputStepName == outputStepName)
      .flatMap(_.discardTableName.notBlank)
      .orElse(writer.flatMap(_.discardTableName.notBlank))
  }

  def outputErrorTableName: String = errorTableName.getOrElse(name)

}

/**
  * Wrapper class used by the api consumers
  */
case class NodeGraphDto(name: String, stepType: String) extends Dto

case class NodeLineageProperty(name: String)
