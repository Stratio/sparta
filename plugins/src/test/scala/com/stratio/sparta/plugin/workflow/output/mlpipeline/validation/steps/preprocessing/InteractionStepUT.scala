/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.preprocessing

import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.{GenericPipelineStepTest, ValidationErrorMessages}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class InteractionStepUT extends GenericPipelineStepTest {

  override def wrongParamsAvailable: Boolean = false

  override def emptyParamsAvailable: Boolean = false

  override def wrongInputColumnAvailable: Boolean = false

  override def stepName: String = "interaction"

  override def resourcesPath: String = "/mlpipeline/singlesteps/preprocessing/interaction/"

  override def trainingDf: DataFrame = {
    val df = sparkSession.createDataFrame(Seq(
      (1, 1, 2, 3, 8, 4, 5),
      (2, 4, 3, 8, 7, 9, 8),
      (3, 6, 1, 9, 2, 3, 6),
      (4, 10, 8, 6, 9, 4, 5),
      (5, 9, 2, 7, 10, 7, 3),
      (6, 1, 1, 4, 2, 8, 4)
    )).toDF("id1", "id2", "id3", "id4", "id5", "id6", "id7")

    val assembler1 = new VectorAssembler().
      setInputCols(Array("id2", "id3", "id4")).
      setOutputCol("vec1")

    val assembled1 = assembler1.transform(df)

    val assembler2 = new VectorAssembler().
      setInputCols(Array("id5", "id6", "id7")).
      setOutputCol("vec2")

    val assembled2 = assembler2.transform(assembled1).select("id1", "vec1", "vec2")
    assembled2
  }

  /* -------------------------------------------------------------
   => Wrong Pipeline construction with input column not present in training DF
  ------------------------------------------------------------- */

  s"$stepName with invalid input columns" should "provide an invalid SparkMl pipeline" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties {

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor(stepWrongInputColumnPath)))

      // Validation step mut be done correctly
      val validation = Try {
        validateMlPipelineStep(properties)
      }
      assert(validation.isSuccess)

      val execution = Try {
        executeStepAndUsePipeline(generateInputDf(), properties)
      }
      assert(execution.isFailure)
      assert(execution.failed.get.getMessage.startsWith("Field"))
      assert(execution.failed.get.getMessage.contains("does not exist"))

      log.info(execution.failed.get.toString)
    }

}
