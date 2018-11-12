/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import java.io.{Serializable => JSerializable}

import breeze.optimize.linear.PowerMethod.BDV
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.enumerations.MlPipelineSaveMode
import com.stratio.sparta.plugin.workflow.output.mlpipeline.MlPipelineOutputStep
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.linalg.{DenseVector, SparseVector, Vector, Vectors}
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, _}
import org.apache.spark.sql.functions.udf

import scala.io.Source
import scala.util.{Failure, Random, Try}

@RunWith(classOf[JUnitRunner])
class LinearSVCStepUT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  self: FlatSpec =>

  @transient var smallBinaryDataset: Dataset[_] = _
  @transient var smallValidationDataset: Dataset[_] = _
  @transient var binaryDataset: Dataset[_] = _

  @transient var smallSparseBinaryDataset: Dataset[_] = _
  @transient var smallSparseValidationDataset: Dataset[_] = _


  trait ReadDescriptorResource{
    def getJsonDescriptor(filename:String): String = {
      Source.fromInputStream(getClass.getResourceAsStream("/mlpipeline/singlesteps/algorithms/linearsvc/" + filename)).mkString
    }
  }

  trait WithExampleData {
    // Generate noisy input of the form Y = signum(x.dot(weights) + intercept + noise)
    def generateSVMInput(
                          intercept: Double,
                          weights: Array[Double],
                          nPoints: Int,
                          seed: Int): Seq[LabeledPoint] = {
      val rnd = new Random(seed)
      val weightsMat = new BDV(weights)
      val x = Array.fill[Array[Double]](nPoints)(
        Array.fill[Double](weights.length)(rnd.nextDouble() * 2.0 - 1.0))
      val y = x.map { xi =>
        val yD = new BDV(xi).dot(weightsMat) + intercept + 0.01 * rnd.nextGaussian()
        if (yD > 0) 1.0 else 0.0
      }
      y.zip(x).map(p => LabeledPoint(p._1, Vectors.dense(p._2)))
    }

    // NOTE: Intercept should be small for generating equal 0s and 1s
    val A = 0.01
    val B = -1.5
    val C = 1.0
    smallBinaryDataset = sparkSession.createDataFrame(sparkSession.sparkContext.parallelize(generateSVMInput(A, Array[Double](B, C), 50, 42)))
    smallValidationDataset = sparkSession.createDataFrame(sparkSession.sparkContext.parallelize(generateSVMInput(A, Array[Double](B, C), 50, 17)))
    binaryDataset = sparkSession.createDataFrame(sparkSession.sparkContext.parallelize(generateSVMInput(1.0, Array[Double](1.0, 2.0, 3.0, 4.0), 10000, 42)))

    // Dataset for testing SparseVector
    val toSparse: Vector => SparseVector = _.asInstanceOf[DenseVector].toSparse
    val sparse = udf(toSparse)
    //smallSparseBinaryDataset = smallBinaryDataset.withColumn("features", sparse('features))
    //smallSparseValidationDataset = smallValidationDataset.withColumn("features", sparse('features))
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

  "LinearSVC default configuration values" should "provide a valid SparkMl pipeline than it can be trained in a workflow (given valid user and item columns)" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("linearsvc-default-values-v0.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)
      assert(validation.get.valid)

      executeStepAndUsePipeline(smallValidationDataset.toDF, properties)
    }

  /* -------------------------------------------------------------
=> Wrong Pipeline construction and execution
------------------------------------------------------------- */

  "LinearSVC with empty configuration values" should "provide a valid SparkMl pipeline than it can be trained in a workflow (given default user and item columns)" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("linearsvc-empty-params-v0.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)

      executeStepAndUsePipeline(smallValidationDataset.toDF, properties)
    }

  /* -------------------------------------------------------------
 => Wrong Pipeline construction and execution
  ------------------------------------------------------------- */

  "LinearSVC with empty configuration values" should "not provide a valid SparkMl pipeline than it can be trained in a workflow (given no valid user and item columns)" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("linearsvc-wrong-columnname-values-v0.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)
      //TODO add expected error tye assert(validation.get.valid)
      val execution = Try{executeStepAndUsePipeline(smallValidationDataset.toDF, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.toString) }
    }

}
