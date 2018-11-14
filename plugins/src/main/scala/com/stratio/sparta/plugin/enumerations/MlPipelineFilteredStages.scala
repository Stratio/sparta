package com.stratio.sparta.plugin.enumerations

object MlPipelineFilteredStages extends Enumeration {

  type MlPipelineFilteredStages = Value

  val BucketedRandomProjectionLSH, ALS, LinearSVC, FPGrowth  = Value

}
