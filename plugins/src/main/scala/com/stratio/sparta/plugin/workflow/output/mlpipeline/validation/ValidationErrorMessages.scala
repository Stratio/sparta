/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation

object ValidationErrorMessages {

  // => Output mode errors
  val invalidSaveMode = "Invalid save mode value."
  // - Filesystem
  val nonDefinedPath = "The path is mandatory in Filesystem save mode."
  // - Ml-Model-Repository
  val nonDefinedMlRepoConnection = "The url and the model name are mandatory in Ml-Model-Repository save mode."
  val mlModelRepConnectionError = "Error while connecting to external Ml-Model-Repository."
  val mlModelModelName = "Model Name is mandatory in Ml-Model-Repository save mode."
  val mlModelRepModelAlreadyExistError = "There exists a model with the same name."
  val mlModelRepModelInvalidModelName = "Model Name is invalid."
  val errorUrlMessage = "It's mandatory to specify the model repository URL."

  // - Validate supported mleap stages
  def stageNotSupportedMleap(className: String): String = s"Pipeline stage ${className} is not supported by Mleap. Try to serialize it only with Spark"

  // => Json pipeline descriptor
  val emptyJsonPipelineDescriptor = "The Machine Learning Pipeline is empty."
  val invalidJsonFormatPipelineDescriptor = "Unable to build Machine Learning pipeline."
  val invalidJsonFormatPipelineGraphDescriptor = "Unable to parse pipelineGraph json descriptor."

  // => Pipeline Graph validation Errors
  val invalidPipelineGraph = "Pipeline is invalid"
  val moreThanOneEnd = "Pipeline Graph has more than one end node"
  val moreThanOneStart = "Pipeline Graph has more than one start node"
  val unconnectedNodes = "Pipeline Graph have some non-connected nodes"
  val schemaErrorInit = "Schema error on "
  def moreThanOneOutput(nodeName: String): String =
    s"node '$nodeName' in Pipeline Graph has more than one output"
  def schemaError(className: String, uid: String): String = schemaErrorInit+ s"${className}@${uid}:"

  // => Building pipeline
  val errorBuildingPipelineInstance = "The pipeline is not correctly defined (check Pipeline graph)."
}
