/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class Word2VecStepUT extends GenericPipelineStepTest {

  override def stepName: String = "word2vec"

  override def resourcesPath: String = "/mlpipeline/singlesteps/preprocessing/word2vec/"

  override def trainingDf: DataFrame = {
    val sentence = "a b " * 100 + "a c " * 10
    sparkSession.createDataFrame(
      Seq(sentence, sentence)
        .map(_.split(" "))
        .map(Tuple1.apply)
    ).toDF("text")
  }

}
