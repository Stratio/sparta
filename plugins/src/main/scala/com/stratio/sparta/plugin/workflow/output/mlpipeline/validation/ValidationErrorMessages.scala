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
  val mlModelRepModelAlreadyExistError = "There exists a model with the same name."
  val errorUrlMessage = "It's mandatory to specify the model repository URL."

  // => Json pipeline descriptor
  val emptyJsonPipelineDescriptor = "The pipeline descriptor is mandatory."
  val invalidJsonFormatPipelineDescriptor = "Unable to parse AI pipeline json descriptor."
  val invalidJsonFormatPipelineGraphDescriptor = "Unable to parse pipelineGraph json descriptor."

  // => Pipeline Graph validation Errors
  val invalidPipelineGraph = "Pipeline is invalid"


  // => Building pipeline
  val errorBuildingPipelineInstance = "The pipeline has not been built correctly."
}
