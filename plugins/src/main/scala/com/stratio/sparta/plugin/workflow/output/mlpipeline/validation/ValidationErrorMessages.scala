/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation

object ValidationErrorMessages {

  // => Output mode errors
  val nonDefinedSaveMode = "'output.mode' property is mandatory."
  val invalidSaveMode = "Invalid 'output.mode' property value."
  // - Filesystem
  val nonDefinedPath = "'path' property is mandatory in Filesystem output mode."
  // - Ml-Model-Repository
  val nonDefinedMlRepoConnection = "Ml-model-repository DCOS service Id, port and model name properties " +
  "are mandatory in Ml-Model-Repository output mode."
  val mlModelRepConnectionError = "Error while connecting to external Ml-Model-Repository."
  val mlModelRepModelAlreadyExistError = "There exists a model with the same name in external Ml-Model-Repository."
  val mlModelRepInvalidPortValue = "Ml-Model-Repository port has an invalid value."

  // => Json pipeline descriptor
  val emptyJsonPipelineDescriptor = "'pipeline' property (with a non-empty JSON pipeline descriptor) is mandatory."
  val invalidJsonFormatPipelineDescriptor = "Unable to parse pipeline json descriptor."

  // => Building pipeline
  val errorBuildingPipelineInstance = "The pipeline has not been built correctly."
}
