/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core

import java.sql.Timestamp
import java.time.Instant
import java.util.Calendar

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.ContextBuilder.ContextBuilderImplicits
import com.stratio.sparta.core.DistributedMonad.{TableNameKey, saveOptionsFromOutputOptions}
import com.stratio.sparta.core.enumerators.{SaveModeEnum, WhenError}
import com.stratio.sparta.core.helpers.SdkSchemaHelper._
import com.stratio.sparta.core.helpers.{CastingHelper, QualityRuleActionEnum, SdkSchemaHelper}
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.models.qualityrule._
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Encoder, Row}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.util.LongAccumulator

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}
import QualityRuleActionEnum._

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

  @transient protected var xdSession: XDSession = _
  @transient protected var ssc: StreamingContext = _

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

  def registerAsTable(session: XDSession, schema: StructType, name: String): Unit

  def setStepName(name: String, forced: Boolean): Unit

  def discards(
                targetData: DistributedMonad[Underlying],
                targetTable: String,
                targetSchema: Option[StructType],
                sourceTable: String,
                sourceSchema: Option[StructType],
                conditions: Seq[DiscardCondition]
              ): DistributedMonad[Underlying]

  def setXdSession(xDSession: XDSession): Unit =
    xdSession = xDSession

  def setStreamingContext(streamingContext: StreamingContext): Unit =
    ssc = streamingContext

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
                   predecessors: Seq[String],
                   qualityRules: Seq[SpartaQualityRule] = Seq.empty[SpartaQualityRule]
                 )(save: (DataFrame, SaveModeEnum.Value, Map[String, String]) => Unit): Seq[SparkQualityRuleResults] = {
    xdSession = xDSession
    writeTemplate(outputOptions, errorsManagement, errorOutputs, predecessors, qualityRules, save)
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
                               qualityRules: Seq[SpartaQualityRule] = Seq.empty[SpartaQualityRule],
                               save: (DataFrame, SaveModeEnum.Value, Map[String, String]) => Unit
                             ): Seq[SparkQualityRuleResults]

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

        !isCurrentRdd && pendingToSend
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

    rdd.setName(s"${rdd.name}#$processedKey")

    SdkSchemaHelper.getSchemaFromSession(xdSession, saveOptions(TableNameKey))
      .orElse(if (!rdd.isEmpty()) Option(rdd.first().schema) else None).foreach { schema =>
      val dataFrame = xdSession.createDataFrame(rdd, schema)

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
  }

  //scalastyle:off

  protected[core] def writeRDDTemplate(
                                        rdd: RDD[Row],
                                        outputOptions: OutputOptions,
                                        errorsManagement: ErrorsManagement,
                                        errorOutputs: Seq[OutputStep[Underlying]],
                                        predecessors: Seq[String],
                                        qualityRules: Seq[SpartaQualityRule] = Seq.empty[SpartaQualityRule],
                                        save: (DataFrame, SaveModeEnum.Value, Map[String, String]) => Unit
                                      ): Seq[SparkQualityRuleResults] = {

    val res = new ListBuffer[SparkQualityRuleResults]()
    Try {
      SdkSchemaHelper.getSchemaFromSession(xdSession, outputOptions.stepName)
        .orElse(if (!rdd.isEmpty()) Option(rdd.first().schema) else None).foreach { schemaExtracted =>
        val dataFrame = xdSession.createDataFrame(rdd, schemaExtracted)
        val saveOptions: Map[String, String] = saveOptionsFromOutputOptions(outputOptions)

        if (qualityRules.nonEmpty) {

          log.info(s"Quality rules to be executed for step ${outputOptions.stepName} and table ${outputOptions.tableName} : ${qualityRules.mkString(",")}" )

          val sparkRules: Seq[SparkQualityRule[Row]] = qualityRules.map(rule => new SparkQualityRule[Row](rule, schemaExtracted))

          val rowCountAccumulator: LongAccumulator = xdSession.sparkContext.longAccumulator("Accumulator_row_count")

          val mapAccumulators: Map[Long, LongAccumulator] =
            sparkRules.map(rule => rule.id -> xdSession.sparkContext.longAccumulator(s"Accumulator_QR_${rule.id}")).toMap

          xdSession.sparkContext.broadcast(mapAccumulators)
          xdSession.sparkContext.broadcast(sparkRules)

          val cachedRDD: DataFrame = dataFrame.cache()

          val executionTime = CastingHelper.castingToSchemaType(TimestampType,
            Timestamp.from(Instant.now).getTime)

          val executionId = Try(qualityRules.head.executionId.get).toOption.getOrElse("")

          val qualityRulesStructFields: Array[StructField] = Array(
            StructField("passingQualityRules", ArrayType(StringType)),
            StructField("failingQualityRules", ArrayType(StringType)),
            StructField("executionId", StringType),
            StructField("executionTime", TimestampType)
          )

          val schemaWithQualityRulesNewFields = StructType(cachedRDD.schema.fields ++ qualityRulesStructFields)

          val qualityRulesPassingDataFrame = cachedRDD.filter { row =>
            sparkRules.forall(rule => rule.composedPredicates(row))
          }

          val qualityRulesFailingDataFrame = cachedRDD.map { row =>
            val failingQRs = sparkRules.filter(rule => !rule.composedPredicates(row)).map(_.id)
            val passingQRs = sparkRules.filterNot(x => failingQRs.contains(x.id)).map(_.id)
            if(failingQRs.nonEmpty)
              Row.fromSeq(row.toSeq ++ Seq(passingQRs) ++ Seq(failingQRs) ++ Seq(executionId) ++ Seq(executionTime))
            else Row.empty
          } (RowEncoder(schemaWithQualityRulesNewFields)).filter(_.toSeq.nonEmpty)

          cachedRDD.foreach { row =>
            sparkRules.foreach(rule => if (rule.composedPredicates(row)) mapAccumulators(rule.id).add(1))
            rowCountAccumulator.add(1)
          }

          val seqThresholds: Map[Long, (Boolean, String, SpartaQualityRuleThresholdActionType)] =
            qualityRules.map { rule =>
              val validationThreshold = new SparkQualityRuleThreshold(rule.threshold, mapAccumulators(rule.id).value, rowCountAccumulator.value)
              val validationResult =
                if (validationThreshold.valid && validationThreshold.isThresholdSatisfied) (true, s"${validationThreshold.toString}", rule.threshold.actionType)
                else (false, s"${validationThreshold.toString}", rule.threshold.actionType)
              rule.id -> validationResult
            }.toMap

          val strictestPolicy = findOutputMode(seqThresholds)

          res ++= qualityRules.map { qr =>
            val stringConditions = qr.predicates.mkString(s"\n${qr.logicalOperator.toUpperCase} ")

            SparkQualityRuleResults(
              dataQualityRuleId = qr.id.toString,
              numTotalEvents = rowCountAccumulator.value,
              numPassedEvents = mapAccumulators(qr.id).value,
              numDiscardedEvents = rowCountAccumulator.value - mapAccumulators(qr.id).value,
              metadataPath = qr.metadataPath,
              transformationStepName = qr.stepName,
              outputStepName = qr.outputName,
              satisfied = seqThresholds(qr.id)._1,
              condition = seqThresholds(qr.id)._2,
              successfulWriting = true,
              qualityRuleName = qr.name,
              conditionsString = stringConditions,
              globalAction = strictestPolicy.toString
            )}

          log.info(s"Quality Rule Results: ${res.mkString(",")}")

          strictestPolicy match {
            case ActionPassthrough if (dataFrame.schema.fields.nonEmpty) =>
              save(dataFrame, outputOptions.saveMode, saveOptions)

            case ActionMove if (dataFrame.schema.fields.nonEmpty) =>
              val tableNameRefusals = ("tableName" -> s"${saveOptions("tableName")}_refusals")
              save(qualityRulesPassingDataFrame, outputOptions.saveMode, saveOptions)
              save(qualityRulesFailingDataFrame, outputOptions.saveMode, saveOptions + tableNameRefusals)
          }
          dataFrame.unpersist()
        }
        else if (dataFrame.schema.fields.nonEmpty)
          save(dataFrame, outputOptions.saveMode, saveOptions)


      }
    } match {
      case Success(_) =>
        log.debug(s"Input data saved correctly into ${outputOptions.tableName}")
        res
      case Failure(e) =>
        Try {
          import errorsManagement.transactionsManagement._
          val sendToOutputsNames = sendToOutputs.map(_.outputStepName)
          val outputsToSend = errorOutputs.filter { output =>
            sendToOutputsNames.contains(output.name)
          }
          val currentDate = new Timestamp(Calendar.getInstance().getTimeInMillis)

          if (sendInputData && outputsToSend.nonEmpty)
            redirectDependencies(
              RedirectContext(rdd, outputOptions, outputsToSend, sendToOutputs, currentDate),
              Seq(InputStep.StepType)
            )
          if (sendPredecessorsData && outputsToSend.nonEmpty)
            redirectDependencies(
              RedirectContext(rdd, outputOptions, outputsToSend, sendToOutputs, currentDate),
              predecessors
            )
          if (sendStepData && outputsToSend.nonEmpty && Option(rdd.name).notBlank.isDefined && rdd.name != processedKey)
            redirectToOutput(
              RedirectContext(rdd, outputOptions, outputsToSend, sendToOutputs, currentDate),
              Map(TableNameKey -> outputOptions.errorTableName.getOrElse(outputOptions.tableName))
            )
        } match {
          case Success(_) =>
            log.debug(s"Error management executed correctly in ${outputOptions.tableName}")
            if (errorsManagement.genericErrorManagement.whenError == WhenError.Error)
              throw e
            else {
              log.warn(s"Error executing the workflow, the error will be discarded by the errors management." +
                s" The exception is: ${e.toString}")
              res ++= res.map(sparkQRResult => sparkQRResult.copy(successfulWriting = false))
            }
          case Failure(exception) =>
            log.debug(s"Error management executed with errors in ${outputOptions.tableName}." +
              s" ${exception.getLocalizedMessage}")
            if (errorsManagement.genericErrorManagement.whenError == WhenError.Error)
              throw new Exception(s"Main exception: ${e.getLocalizedMessage}." +
                s" Error management exception: ${exception.getLocalizedMessage}", e)
            else {
              log.warn(s"Error executing the workflow and executing the errors management," +
                s" the error will be discarded by the errors management." +
                s" Main exception: ${e.toString}. Error management exception: ${exception.toString}")
              res ++= res.map(sparkQRResult => sparkQRResult.copy(successfulWriting = false))
            }
        }
    }
  }

  //scalastyle:on
}

object DistributedMonad {

  val PrimaryKey = "primaryKey"
  val TableNameKey = "tableName"
  val PartitionByKey = "partitionBy"
  val StepName = "stepName"
  val UniqueConstraintName = "uniqueConstraintName"
  val UniqueConstraintFields = "uniqueConstraintFields"
  val UpdateFields = "updateFields"

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
        ds.filter(_ => false)

      override def registerAsTable(session: XDSession, schema: StructType, name: String): Unit = {
        ds.transform { rdd =>
          session.createDataFrame(rdd, schema).createOrReplaceTempView(name)
          rdd
        }
      }

      override def setStepName(name: String, forced: Boolean): Unit =
        ds.foreachRDD { rdd =>
          if (Option(rdd.name).notBlank.isDefined && !forced)
            rdd.setName(s"${rdd.name}#$name")
          else rdd.setName(name)
        }

      override def discards(
                             targetData: DistributedMonad[DStream],
                             targetTable: String,
                             targetSchema: Option[StructType],
                             sourceTable: String,
                             sourceSchema: Option[StructType],
                             conditions: Seq[DiscardCondition]
                           ): DistributedMonad[DStream] = {
        if (conditions.nonEmpty) {
          val transformFunc: (RDD[Row], RDD[Row]) => RDD[Row] = {
            case (rdd1, rdd2) =>
              val discardConditions = conditions.map(_.toSql(sourceTable, targetTable)).mkString(" AND ")
              sourceSchema.orElse(getSchemaFromRdd(rdd1))
                .foreach(schema => xdSession.createDataFrame(rdd1, schema).createOrReplaceTempView(sourceTable))
              targetSchema.orElse(getSchemaFromRdd(rdd2))
                .foreach(schema => xdSession.createDataFrame(rdd2, schema).createOrReplaceTempView(targetTable))

              val discards = xdSession.sql(s"SELECT * FROM $sourceTable LEFT ANTI JOIN $targetTable ON $discardConditions")
              discards.createOrReplaceTempView(SdkSchemaHelper.discardTableName(targetTable))
              discards.rdd
          }

          ds.transformWith(targetData.ds, transformFunc)
        } else ssc.queueStream(new mutable.Queue[RDD[Row]])
      }

      override def writeTemplate(
                                  outputOptions: OutputOptions,
                                  errorsManagement: ErrorsManagement,
                                  errorOutputs: Seq[OutputStep[DStream]],
                                  predecessors: Seq[String],
                                  qualityRules: Seq[SpartaQualityRule] = Seq.empty[SpartaQualityRule],
                                  save: (DataFrame, SaveModeEnum.Value, Map[String, String]) => Unit
                                ): Seq[SparkQualityRuleResults] = {
        val res = new ListBuffer[SparkQualityRuleResults]()
        ds.foreachRDD { rdd =>
          res ++= writeRDDTemplate(rdd, outputOptions, errorsManagement, errorOutputs, predecessors, qualityRules, save)
        }
        res
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

      override def toEmpty: DistributedMonad[Dataset] =
        ds.filter(_ => false)

      override def registerAsTable(session: XDSession, schema: StructType, name: String): Unit = {
        require(isCorrectTableName(name),
          s"The step ($name) has an incorrect name and it's not possible to register it as a temporal table")
        ds.createOrReplaceTempView(name)
      }


      override def setStepName(name: String, forced: Boolean): Unit =
        if (Option(ds.rdd.name).notBlank.isDefined && !forced)
          ds.rdd.setName(s"${ds.rdd.name}#$name")
        else ds.rdd.setName(name)

      override def discards(
                             targetData: DistributedMonad[Dataset],
                             targetTable: String,
                             targetSchema: Option[StructType],
                             sourceTable: String,
                             sourceSchema: Option[StructType],
                             conditions: Seq[DiscardCondition]
                           ): DistributedMonad[Dataset] = {
        if (conditions.nonEmpty) {
          val discardConditions = conditions.map(_.toSql(sourceTable, targetTable)).mkString(" AND ")
          ds.createOrReplaceTempView(sourceTable)
          targetData.ds.createOrReplaceTempView(targetTable)

          val discards = xdSession.sql(s"SELECT * FROM $sourceTable LEFT ANTI JOIN $targetTable ON $discardConditions")
          discards.createOrReplaceTempView(SdkSchemaHelper.discardTableName(targetTable))
          discards
        } else xdSession.emptyDataFrame
      }

      override def writeTemplate(
                                  outputOptions: OutputOptions,
                                  errorsManagement: ErrorsManagement,
                                  errorOutputs: Seq[OutputStep[Dataset]],
                                  predecessors: Seq[String],
                                  qualityRules: Seq[SpartaQualityRule] = Seq.empty[SpartaQualityRule],
                                  save: (DataFrame, SaveModeEnum.Value, Map[String, String]) => Unit
                                ): Seq[SparkQualityRuleResults] =
        writeRDDTemplate(ds.rdd, outputOptions, errorsManagement, errorOutputs, predecessors, qualityRules, save)

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

      override def map(func: Row => Row): RDD[Row] =
        ds.map(func)

      override def flatMap(func: Row => TraversableOnce[Row]): RDD[Row] =
        ds.flatMap(func)

      override def toEmpty: DistributedMonad[RDD] =
        ds.filter(_ => false)

      override def registerAsTable(session: XDSession, schema: StructType, name: String): Unit = {
        require(isCorrectTableName(name),
          s"The step ($name) has an incorrect name and it's not possible to register it as a temporal table")
        session.createDataFrame(ds, schema).createOrReplaceTempView(name)
      }

      override def setStepName(name: String, forced: Boolean): Unit =
        if (Option(ds.name).notBlank.isDefined && !forced)
          ds.setName(s"${ds.name}#$name")
        else ds.setName(name)

      override def discards(
                             targetData: DistributedMonad[RDD],
                             targetTable: String,
                             targetSchema: Option[StructType],
                             sourceTable: String,
                             sourceSchema: Option[StructType],
                             conditions: Seq[DiscardCondition]
                           ): DistributedMonad[RDD] = {
        if (conditions.nonEmpty) {
          val discardConditions = conditions.map(_.toSql(sourceTable, targetTable)).mkString(" AND ")
          sourceSchema.orElse(getSchemaFromRdd(ds))
            .foreach(schema => xdSession.createDataFrame(ds, schema).createOrReplaceTempView(sourceTable))
          targetSchema.orElse(getSchemaFromRdd(targetData.ds))
            .foreach(schema => xdSession.createDataFrame(targetData.ds, schema).createOrReplaceTempView(targetTable))

          val discards = xdSession.sql(s"SELECT * FROM $sourceTable LEFT ANTI JOIN $targetTable ON $discardConditions")
          discards.createOrReplaceTempView(SdkSchemaHelper.discardTableName(targetTable))
          discards.rdd
        } else xdSession.sparkContext.emptyRDD[Row]
      }

      override def writeTemplate(
                                  outputOptions: OutputOptions,
                                  errorsManagement: ErrorsManagement,
                                  errorOutputs: Seq[OutputStep[RDD]],
                                  predecessors: Seq[String],
                                  qualityRules: Seq[SpartaQualityRule] = Seq.empty[SpartaQualityRule],
                                  save: (DataFrame, SaveModeEnum.Value, Map[String, String]) => Unit
                                ): Seq[SparkQualityRuleResults] =
        writeRDDTemplate(ds, outputOptions, errorsManagement, errorOutputs, predecessors, qualityRules, save)
    }

    implicit def asDistributedMonadMap[K, Underlying[Row]](m: Map[K, Underlying[Row]])(
      implicit underlying2distributedMonad: Underlying[Row] => DistributedMonad[Underlying]
    ): Map[K, DistributedMonad[Underlying]] = m.mapValues(v => v: DistributedMonad[Underlying])

    implicit def fromOptionRDDToOptionMonad(optionalRdd: Option[RDD[Row]]): Option[RDDDistributedMonad] =
      optionalRdd.map { rdd => RDDDistributedMonad(rdd) }

    implicit def fromOptionDStreamToOptionMonad(optionalDStream: Option[DStream[Row]]): Option[DStreamAsDistributedMonad] =
      optionalDStream.map { stream => DStreamAsDistributedMonad(stream) }
  }

  //scalastyle:on

  object Implicits extends DistributedMonadImplicits with ContextBuilderImplicits with Serializable

  private def saveOptionsFromOutputOptions(outputOptions: OutputOptions): Map[String, String] = {
    Map(TableNameKey -> outputOptions.tableName, StepName -> outputOptions.stepName) ++
      outputOptions.partitionBy.notBlank.fold(Map.empty[String, String]) { partition =>
        Map(PartitionByKey -> partition)
      } ++
      outputOptions.primaryKey.notBlank.fold(Map.empty[String, String]) { key =>
        Map(PrimaryKey -> key)
      } ++
      outputOptions.uniqueConstraintName.notBlank.fold(Map.empty[String, String]) { key =>
        Map(UniqueConstraintName -> key)
      } ++
      outputOptions.uniqueConstraintFields.notBlank.fold(Map.empty[String, String]) { key =>
        Map(UniqueConstraintFields -> key)
      } ++
      outputOptions.updateFields.notBlank.fold(Map.empty[String, String]) { key =>
        Map(UpdateFields -> key)
      }
  }
}


