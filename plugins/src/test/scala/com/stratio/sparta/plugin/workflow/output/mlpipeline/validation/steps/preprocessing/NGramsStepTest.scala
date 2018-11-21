/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.preprocessing

import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.GenericPipelineStepTest
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class NGramsStepTest extends GenericPipelineStepTest {

  override def stepName: String = "ngrams"

  override def resourcesPath: String = "/mlpipeline/singlesteps/preprocessing/ngrams/"

  override def trainingDf: DataFrame = sparkSession.createDataFrame(Seq(
    (0, Array("Hi", "I", "heard", "about", "Spark")),
    (1, Array("I", "wish", "Java", "could", "use", "case", "classes")),
    (2, Array("Logistic", "regression", "models", "are", "neat"))
  )).toDF("id", "words")
}
