/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline

object StaticData {

  // => MlPipeline output save modes
  val SAVE_MODE_FILESYSTEM = "FILESYSTEM"
  val SAVE_MODE_MLMODELREP = "MODELREP"
  val SAVE_MODES = Array(SAVE_MODE_FILESYSTEM, SAVE_MODE_MLMODELREP)

  // => Stratio Ml-models-repository
  val SPARK_NATIVE_SER_LIB = "SPARK"
  val SPARK_MLEAP_SER_LIB = "MLEAP"
  val SPARK_BOTH_SER_LIB = "SPARK_AND_MLEAP"
  val SPARK_SERIALIZATION_LIBS = Array(SPARK_NATIVE_SER_LIB, SPARK_MLEAP_SER_LIB, SPARK_BOTH_SER_LIB)
  val INCLUDES_SPARK_SER_LIB = Array(SPARK_BOTH_SER_LIB, SPARK_NATIVE_SER_LIB)
  val INCLUDES_MLEAP_SER_LIB = Array(SPARK_BOTH_SER_LIB, SPARK_MLEAP_SER_LIB)

}
