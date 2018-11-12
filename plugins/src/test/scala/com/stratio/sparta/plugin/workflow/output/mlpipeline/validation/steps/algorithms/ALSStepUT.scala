/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.enumerations.MlPipelineSaveMode
import com.stratio.sparta.plugin.workflow.output.mlpipeline.MlPipelineOutputStep
import org.apache.spark.sql.DataFrame

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, _}
import com.github.fommil.netlib.BLAS.{getInstance => blas}
import org.apache.spark.ml.recommendation.ALS.Rating

import scala.io.Source
import scala.util.{Failure, Random, Try}

@RunWith(classOf[JUnitRunner])
class ALSStepUT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  self: FlatSpec =>

  trait ReadDescriptorResource{
    def getJsonDescriptor(filename:String): String = {
      Source.fromInputStream(getClass.getResourceAsStream("/mlpipeline/singlesteps/algorithms/als/" + filename)).mkString
    }
  }

  trait WithExampleData {

    private def genFactors(
                            size: Int,
                            rank: Int,
                            random: Random,
                            a: Float = -1.0f,
                            b: Float = 1.0f): Seq[(Int, Array[Float])] = {
      require(size > 0 && size < Int.MaxValue / 3)
      require(b > a)
      val ids = mutable.Set.empty[Int]
      while (ids.size < size) {
        ids += random.nextInt()
      }
      val width = b - a
      ids.toSeq.sorted.map(id => (id, Array.fill(rank)(a + random.nextFloat() * width)))
    }

    // The assumption of the implicit feedback model is that unobserved ratings are more likely to
    // be negatives.
    val positiveFraction = 0.8
    val negativeFraction = 1.0 - positiveFraction
    val random = new Random(1234)
    val userFactors = genFactors(20, 2, random)
    val itemFactors = genFactors(40, 2, random)
    val data = ArrayBuffer.empty[Rating[Int]]
    for ((userId, userFactor) <- userFactors; (itemId, itemFactor) <- itemFactors) {
      val rating = blas.sdot(2, userFactor, 1, itemFactor, 1)
      val threshold = if (rating > 0) positiveFraction else negativeFraction
      val observed = random.nextDouble() < threshold
      if (observed) {
        val x = random.nextDouble()
        val noise = 0.01 * random.nextGaussian()
        data += Rating(userId, itemId, rating + noise.toFloat)
      }
    }
      val training: DataFrame = sparkSession.createDataFrame(sparkSession.sparkContext.parallelize(data))
  }

  trait WithFilesystemProperties {
    var properties:Map[String, JSerializable] = Map(
      "output.mode" -> JsoneyString(MlPipelineSaveMode.FILESYSTEM.toString),
      "path" -> JsoneyString("/tmp/pipeline_tests")
    )
  }

  trait WithExecuteStep{
    def executeStep(training:DataFrame, properties:Map[String, JSerializable]){
      // · Creating outputStep
      val mlPipelineOutput = new MlPipelineOutputStep("MlPipeline.out", sparkSession, properties)
      // · Executing step
      mlPipelineOutput.save(training, SaveModeEnum.Overwrite, Map.empty[String, String])
    }

    def executeStepAndUsePipeline(training:DataFrame, properties:Map[String, JSerializable]){
      // · Creating outputStep
      val mlPipelineOutput = new MlPipelineOutputStep("MlPipeline.out", sparkSession, properties)
      // · Executing step
      mlPipelineOutput.save(training, SaveModeEnum.Overwrite, Map.empty[String, String])
      // · Use pipeline object
      val pipelineModel = mlPipelineOutput.pipeline.get.fit(training)
      val df = pipelineModel.transform(training)
      df.show()
    }
  }

  trait WithValidateStep{
    def validateMlPipelineStep(properties:Map[String, JSerializable]): ErrorValidations = {
      // · Creating outputStep
      val mlPipelineOutput = new MlPipelineOutputStep("MlPipeline.out", sparkSession, properties)
      // · Executing step
      val e = mlPipelineOutput.validate()
      e.messages.foreach(x => log.info(x.message))
      e
    }
  }

  /* -------------------------------------------------------------
   => Correct Pipeline construction and execution
    ------------------------------------------------------------- */

  "ALS with default configuration values" should "provide a valid SparkMl pipeline than it can be trained in a workflow" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("als-default-values-v0.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)
      assert(validation.get.valid)

      val execution = Try{executeStepAndUsePipeline(training, properties)}
      assert(execution.isSuccess)

    }

  /* -------------------------------------------------------------
=> Wrong Pipeline construction and execution
------------------------------------------------------------- */

  "ALS with empty configuration values" should "provide a valid SparkMl pipeline than it can be trained in a workflow" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("als-empty-params-v0.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)

      val execution = Try{executeStepAndUsePipeline(training, properties)}
      assert(execution.isSuccess)
    }

  /* -------------------------------------------------------------
 => Wrong Pipeline construction and execution
  ------------------------------------------------------------- */

  "ALS with wrong configuration values" should "not provide a valid SparkMl pipeline than it can be trained in a workflow (given no valid column names)" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("als-wrong-columnname-values-v0.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)
      //TODO add expected error tye assert(validation.get.valid)
      val execution = Try{executeStepAndUsePipeline(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.toString) }
    }

  /* -------------------------------------------------------------
=> Wrong Pipeline construction and execution
------------------------------------------------------------- */

  "ALS with wrong configuration values" should "not provide a valid SparkMl pipeline than it can be trained in a workflow" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("als-wrong-column-values-v0.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)
      //TODO add expected error tye assert(validation.get.valid)
      val execution = Try{executeStepAndUsePipeline(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.toString) }
    }

}
