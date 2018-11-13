/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.preprocessing

import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.GenericPipelineStepTest
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IdfStepUT extends GenericPipelineStepTest {

  override def stepName: String = "idf"

  override def resourcesPath: String = "/mlpipeline/singlesteps/preprocessing/idf/"

  override def trainingDf: DataFrame = {
    val sparkValToImportImplicits = sparkSession
    import sparkValToImportImplicits.implicits._

    val numOfFeatures = 4
    Seq(
      (1, Vectors.sparse(numOfFeatures, Array(1, 3), Array(1.0, 2.0))),
      (2, Vectors.dense(0.0, 1.0, 2.0, 3.0)),
      (3, Vectors.sparse(numOfFeatures, Array(1), Array(1.0)))
    ).toDF("id", "features")

  }

}
