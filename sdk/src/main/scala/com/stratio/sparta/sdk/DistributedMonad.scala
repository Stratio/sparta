/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparta.sdk

import java.sql.Timestamp
import java.util.Calendar

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.ContextBuilder.ContextBuilderImplicits
import com.stratio.sparta.sdk.DistributedMonad.{TableNameKey, saveOptionsFromOutputOptions}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.{SaveModeEnum, WhenError}
import com.stratio.sparta.sdk.workflow.step._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset, Encoder, Row}
import org.apache.spark.streaming.dstream.DStream

import scala.util.{Failure, Success, Try}

/**
  * This is a typeclass interface whose goal is to abstract over DStreams, RDD, Datasets and whichever
  * distributed collection of rows may come in the future.
  *
  * Concrete implementations of the type class are provided by [[DistributedMonad.DistributedMonadImplicits]] for
  * [[DStream]], [[RDD]] and [[Dataset]]. These are implicit classes which, wherever they are visible, allow using
  * [[DStream]]s, [[RDD]]s and [[Dataset]]s indistinctively thus providing a delayed (after type definition) level of
  * polymorphism.
  *
  * @tparam Underlying Collection of [[Row]]s wrapped to be used through the [[DistributedMonad]] interface.
  */
trait DistributedMonad[Underlying[Row]] extends SLF4JLogging with Serializable {

  val ds: Underlying[Row] // Wrapped collection

  val processedKey = "REDIRECTED"
  val defaultRedirectDateName = "redirectDate"

  case class RedirectContext(
                              rdd: RDD[Row],
                              outputOptions: OutputOptions,
                              outputsToSend: Seq[OutputStep[Underlying]],
                              errorOutputActions: Seq[ErrorOutputAction],
                              currentDate: Timestamp
                            )

  // Common interface:

  def map(func: Row => Row): Underlying[Row]

  def flatMap(func: Row => TraversableOnce[Row]): Underlying[Row]

  def toEmpty: DistributedMonad[Underlying]

  def setStepName(name: String, forced: Boolean): Unit

  /**
    * Write operation, note this is a public interface for users to call,
    * its implementation should be provided by [[writeTemplate]]. The reason
    * for this convoluted approach (compared to just offering an unimplemented method
    * for subclasses to implement) is that `xDSession` needs to be captured
    * as a transient variable in order to be able to serialize the whole [[DistributedMonad]]
    * implementation.
    *
    * @param outputOptions Options for the write operation.
    * @param xDSession     Crossdata session potentially used in the write operation.
    * @param save          Write operation implementation (it'll be executed at the end of each window).
    */
  final def write(
                   outputOptions: OutputOptions,
                   xDSession: XDSession,
                   errorsManagement: ErrorsManagement,
                   errorOutputs: Seq[OutputStep[Underlying]],
                   predecessors: Seq[String]
                 )(save: (DataFrame, SaveModeEnum.Value, Map[String, String]) => Unit): Unit = {
    xdSession = xDSession
    writeTemplate(outputOptions, errorsManagement, errorOutputs, predecessors, save)
  }

  /**
    * Use this template method to implement [[write]], this is required in order
    * to be able to use xdSession within functions which should be serialized to work with Spark.
    */
  protected def writeTemplate(
                               outputOptions: OutputOptions,
                               errorsManagement: ErrorsManagement,
                               errorOutputs: Seq[OutputStep[Underlying]],
                               predecessors: Seq[String],
                               save: (DataFrame, SaveModeEnum.Value, Map[String, String]) => Unit
                             ): Unit

  @transient protected var xdSession: XDSession = _

  private def redirectDependencies(redirectContext: RedirectContext, predecessors: Seq[String]): Unit = {
    import redirectContext._

    rdd.dependencies.filter { dependency =>
      redirectDependencies(
        RedirectContext(
          dependency.rdd.asInstanceOf[RDD[Row]],
          outputOptions,
          outputsToSend,
          errorOutputActions,
          currentDate
        ),
        predecessors
      )
      if (Option(dependency.rdd.name).notBlank.isDefined) {
        val isCurrentRdd = Option(rdd.name).notBlank.isDefined && rdd.name == dependency.rdd.name
        val pendingToSend = predecessors.exists(pName => dependency.rdd.name.contains(pName)) &&
          !dependency.rdd.name.contains(processedKey)

        !isCurrentRdd && pendingToSend && !dependency.rdd.isEmpty()
      } else false
    }.foreach { dependencyStepRdd =>
      val inputRdd = dependencyStepRdd.rdd.asInstanceOf[RDD[Row]]
      val tableName = inputRdd.name.split("#").find(pName => predecessors.exists(iName => pName.contains(iName)))
        .getOrElse(throw new Exception(s"The RDD name (${inputRdd.name}) is not present in ${predecessors.mkString}"))
        .replace(s"${InputStep.StepType}-", "")
        .replace(s"${TransformStep.StepType}-", "")

      redirectToOutput(
        RedirectContext(inputRdd, outputOptions, outputsToSend, errorOutputActions, currentDate),
        Map(TableNameKey -> tableName)
      )
    }
  }

  private def redirectToOutput(redirectContext: RedirectContext, saveOptions: Map[String, String]): Unit = {
    import redirectContext._

    val schema = rdd.first().schema
    val dataFrame = xdSession.createDataFrame(rdd, schema)

    rdd.setName(s"${rdd.name}#$processedKey")

    outputsToSend.foreach { output =>
      val (addDate, omitSaveErrors, dateField) = errorOutputActions.find(_.outputStepName == output.name) match {
        case Some(toOutput) => (
          toOutput.addRedirectDate,
          toOutput.omitSaveErrors,
          toOutput.redirectDateColName.notBlank.getOrElse(defaultRedirectDateName)
        )
        case None => (false, true, defaultRedirectDateName)
      }
      val dataFrameToSave = if (addDate) {
        import org.apache.spark.sql.functions._
        dataFrame.withColumn(dateField, lit(currentDate))
      } else dataFrame

      Try(output.save(dataFrameToSave, outputOptions.saveMode, saveOptions)) match {
        case Success(_) =>
          log.debug(s"Data saved correctly into table ${saveOptions(TableNameKey)} in the output ${output.name}")
        case Failure(exception) =>
          if (omitSaveErrors)
            log.debug(s"Error saving data into table ${saveOptions(TableNameKey)} in the output ${output.name}." +
              s" ${exception.getLocalizedMessage}")
          else throw exception
      }
    }
  }

  //scalastyle:off

  protected def writeRDDTemplate(
                        rdd: RDD[Row],
                        outputOptions: OutputOptions,
                        errorsManagement: ErrorsManagement,
                        errorOutputs: Seq[OutputStep[Underlying]],
                        predecessors: Seq[String],
                        save: (DataFrame, SaveModeEnum.Value, Map[String, String]) => Unit
                      ): Unit = {
    Try {
      if (!rdd.isEmpty()) {
        val schema = rdd.first().schema
        val dataFrame = xdSession.createDataFrame(rdd, schema)
        val saveOptions = saveOptionsFromOutputOptions(outputOptions)

        save(dataFrame, outputOptions.saveMode, saveOptions)
      }
    } match {
      case Success(_) =>
        log.debug(s"Input data saved correctly into ${outputOptions.tableName}")
      case Failure(e) =>
        Try {
          import errorsManagement.transactionsManagement._
          val sendToOutputsNames = sendToOutputs.map(_.outputStepName)
          val outputsToSend = errorOutputs.filter { output =>
            sendToOutputsNames.contains(output.name)
          }
          val currentDate = new Timestamp(Calendar.getInstance().getTimeInMillis)

          if (sendInputData)
            redirectDependencies(
              RedirectContext(rdd, outputOptions, outputsToSend, sendToOutputs, currentDate),
              Seq(InputStep.StepType)
            )
          if (sendPredecessorsData)
            redirectDependencies(
              RedirectContext(rdd, outputOptions, outputsToSend, sendToOutputs, currentDate),
              predecessors
            )
          if (sendStepData && Option(rdd.name).notBlank.isDefined && rdd.name != processedKey && !rdd.isEmpty())
            redirectToOutput(
              RedirectContext(rdd, outputOptions, outputsToSend, sendToOutputs, currentDate),
              Map(TableNameKey -> outputOptions.errorTableName.getOrElse(outputOptions.tableName))
            )
        } match {
          case Success(_) =>
            log.debug(s"Error management executed correctly in ${outputOptions.tableName}")
            if (errorsManagement.genericErrorManagement.whenError == WhenError.Error)
              throw e
          case Failure(exception) =>
            log.debug(s"Error management executed with errors in ${outputOptions.tableName}." +
              s" ${exception.getLocalizedMessage}")
            if (errorsManagement.genericErrorManagement.whenError == WhenError.Error)
              throw new Exception(s"Main exception: ${e.getLocalizedMessage}." +
                s" Error management exception: ${exception.getLocalizedMessage}", e)
        }
    }
  }

  //scalastyle:on
}

object DistributedMonad {

  val PrimaryKey = "primaryKey"
  val TableNameKey = "tableName"
  val PartitionByKey = "partitionBy"

  //scalastyle:off
  trait DistributedMonadImplicits {

    implicit def rowEncoder(schema: StructType): Encoder[Row] = RowEncoder(schema)

    /**
      * Type class instance for [[DStream[Row]]]
      * This is an implicit class. Therefore, whenever a [[DStream]] is passed to a function
      * expecting a [[DistributedMonad]] being this class visible, the compiler will wrapp that [[DStream]] using
      * the constructor of this class.
      *
      * @param ds [[DStream[Row]]] to be wrapped.
      */
    implicit class DStreamAsDistributedMonad(val ds: DStream[Row]) extends DistributedMonad[DStream] {

      override def map(func: Row => Row): DStream[Row] =
        ds.map(func)

      override def flatMap(func: Row => TraversableOnce[Row]): DStream[Row] =
        ds.flatMap(func)

      override def toEmpty: DistributedMonad[DStream] =
        ds.transform(rdd => rdd.sparkContext.emptyRDD[Row])

      override def setStepName(name: String, forced: Boolean): Unit =
        ds.foreachRDD { rdd =>
          if (Option(rdd.name).notBlank.isDefined && !forced)
            rdd.setName(s"${rdd.name}#$name")
          else rdd.setName(name)
        }

      override def writeTemplate(
                                  outputOptions: OutputOptions,
                                  errorsManagement: ErrorsManagement,
                                  errorOutputs: Seq[OutputStep[DStream]],
                                  predecessors: Seq[String],
                                  save: (DataFrame, SaveModeEnum.Value, Map[String, String]) => Unit
                                ): Unit = {
        ds.foreachRDD { rdd =>
          writeRDDTemplate(rdd, outputOptions, errorsManagement, errorOutputs, predecessors, save)
        }
      }
    }

    /**
      * Type class instance for [[Dataset[Row]]]
      * This is an implicit class. Therefore, whenever a [[Dataset]] is passed to a function
      * expecting a [[DistributedMonad]] being this class visible, the compiler will wrapp that [[Dataset]] using
      * the constructor of this class.
      *
      * @param ds [[Dataset[Row]] to be wrapped.
      */
    implicit class DatasetDistributedMonad(val ds: Dataset[Row]) extends DistributedMonad[Dataset] {

      override def map(func: Row => Row): Dataset[Row] = {
        val newSchema = if (ds.rdd.isEmpty()) StructType(Nil) else func(ds.first()).schema
        ds.map(func)(RowEncoder(newSchema))
      }

      override def flatMap(func: Row => TraversableOnce[Row]): Dataset[Row] = {
        val newSchema = if (ds.rdd.isEmpty()) StructType(Nil)
        else {
          val firstValue = func(ds.first()).toSeq
          if (firstValue.nonEmpty) firstValue.head.schema else StructType(Nil)
        }
        ds.flatMap(func)(RowEncoder(newSchema))
      }

      override def toEmpty: DistributedMonad[Dataset] = {
        xdSession.emptyDataset(RowEncoder(StructType(Nil)))
      }

      override def setStepName(name: String, forced: Boolean): Unit =
        if (Option(ds.rdd.name).notBlank.isDefined && !forced)
          ds.rdd.setName(s"${ds.rdd.name}#$name")
        else ds.rdd.setName(name)

      override def writeTemplate(
                                  outputOptions: OutputOptions,
                                  errorsManagement: ErrorsManagement,
                                  errorOutputs: Seq[OutputStep[Dataset]],
                                  predecessors: Seq[String],
                                  save: (DataFrame, SaveModeEnum.Value, Map[String, String]) => Unit
                                ): Unit =
        writeRDDTemplate(ds.rdd, outputOptions, errorsManagement, errorOutputs, predecessors, save)

    }

    /**
      * Type class instance for [[org.apache.spark.rdd.RDD[Row]]]
      * This is an implicit class. Therefore, whenever a [[org.apache.spark.rdd.RDD]] is passed to a function
      * expecting a [[DistributedMonad]] being this class visible,
      * the compiler will wrapp that [[org.apache.spark.rdd.RDD]] using the constructor of this class.
      *
      * @param ds [[org.apache.spark.rdd.RDD[Row]] to be wrapped.
      */
    implicit class RDDDistributedMonad(val ds: RDD[Row]) extends DistributedMonad[RDD] {

      override def map(func: Row => Row): RDD[Row] = ds.map(func)

      override def flatMap(func: Row => TraversableOnce[Row]): RDD[Row] = ds.flatMap(func)

      override def toEmpty: DistributedMonad[RDD] = ds.sparkContext.emptyRDD[Row]

      override def setStepName(name: String, forced: Boolean): Unit =
        if (Option(ds.name).notBlank.isDefined && !forced)
          ds.setName(s"${ds.name}#$name")
        else ds.setName(name)

      override def writeTemplate(
                                  outputOptions: OutputOptions,
                                  errorsManagement: ErrorsManagement,
                                  errorOutputs: Seq[OutputStep[RDD]],
                                  predecessors: Seq[String],
                                  save: (DataFrame, SaveModeEnum.Value, Map[String, String]) => Unit
                                ): Unit =
        writeRDDTemplate(ds, outputOptions, errorsManagement, errorOutputs, predecessors, save)
    }

    implicit def asDistributedMonadMap[K, Underlying[Row]](m: Map[K, Underlying[Row]])(
      implicit underlying2distributedMonad: Underlying[Row] => DistributedMonad[Underlying]
    ): Map[K, DistributedMonad[Underlying]] = m.mapValues(v => v: DistributedMonad[Underlying])

  }

  //scalastyle:on

  object Implicits extends DistributedMonadImplicits with ContextBuilderImplicits with Serializable

  private def saveOptionsFromOutputOptions(outputOptions: OutputOptions): Map[String, String] = {
    Map(TableNameKey -> outputOptions.tableName) ++
      outputOptions.partitionBy.notBlank.fold(Map.empty[String, String]) { partition =>
        Map(PartitionByKey -> partition)
      } ++
      outputOptions.primaryKey.notBlank.fold(Map.empty[String, String]) { key =>
        Map(PrimaryKey -> key)
      }
  }

}


