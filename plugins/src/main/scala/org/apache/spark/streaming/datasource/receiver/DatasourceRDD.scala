/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.streaming.datasource.receiver

import org.apache.spark.partial.{BoundedDouble, CountEvaluator, PartialResult}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.streaming.datasource.models.{InputSentences, OffsetField, OffsetOperator}
import org.apache.spark.{Partition, TaskContext}

import scala.util.Try

private[datasource]
class DatasourceRDD(
                     @transient sparkSession: SparkSession,
                     inputSentences: InputSentences,
                     datasourceParams: Map[String, String]
                   ) extends RDD[Row](sparkSession.sparkContext, Nil) with DatasourceRDDHelper {

  private var totalCalculated: Option[Long] = None

  lazy private val complexQuery: Boolean = checkIfComplexQuery(inputSentences.query)
  lazy private val initialWhereCondition: Option[String] = {
    if (complexQuery)
      None
    else
      retrieveWhereCondition(inputSentences.query)
  }

  //Execute continuous queries
  inputSentences.continuousStatements.foreach { statement =>
    sparkSession.sql(statement)
  }

  lazy val dataFrame: DataFrame =
    inputSentences.offsetConditions.fold(sparkSession.sql(inputSentences.query)) { offset =>
      val parsedQuery = parseInitialQuery(complexQuery, inputSentences.query)

      if (parsedQuery.equals(TempInitQuery))
        initializeTempTable()

      val conditionsSentence = offset.extractConditionSentence(initialWhereCondition)
      val orderSentence = offset.extractOrderSentence(offset.limitRecords.isEmpty)
      val limitSentence = inputSentences.extractLimitSentence

      if (possibleConflictsWRTColumns(initialWhereCondition, offset))
        log.warn("One or more columns specified as Offset appear in the user-provided WHERE condition")

      sparkSession.sql(parsedQuery + conditionsSentence + orderSentence + limitSentence)
    }

  private def initializeTempTable(): Unit =
    sparkSession.sql(inputSentences.query).createOrReplaceTempView(InitTableName)

  def progressInputSentences: InputSentences = {
    if (!dataFrame.rdd.isEmpty()) {
      inputSentences.offsetConditions.fold(inputSentences) { offset =>
        val offsetValues: Map[String, Option[Any]] = {
          val firstRecord = if (offset.limitRecords.isEmpty) {
            dataFrame.first()
          } else {
            dataFrame
              .orderBy(offset.extractOrderColumns(): _*)
              .limit(1)
              .first()
          }

          offset.fromOffset.map{ currentField =>
            val currentValue = Try{
              Option(firstRecord.get(dataFrame.schema.fieldIndex(currentField.name)))
            }.getOrElse(currentField.value)

            currentField.name -> currentValue
          }.toMap
        }

        val updatedConditions =
          offset.fromOffset.map(currentField =>
            OffsetField(
              name = currentField.name,
              operator = {
                if (offset.fromOffset.lengthCompare(1) == 0)
                  OffsetOperator.toProgressOperator(currentField.operator)
                else
                  OffsetOperator.toMultiProgressOperator(currentField.operator)
              },
              value = offsetValues(currentField.name)
            ))

        inputSentences.copy(
          offsetConditions = Option(offset.copy(fromOffset = updatedConditions))
        )
      }
    } else inputSentences
  }

  /**
    * Return the number of elements in the RDD. Optimized when is called the second place
    */
  override def count(): Long = {
    totalCalculated.getOrElse {
      totalCalculated = Option(dataFrame.count())
      totalCalculated.get
    }
  }

  /**
    * Return the number of elements in the RDD approximately. Optimized when count are called before
    */
  override def countApprox(
                            timeout: Long,
                            confidence: Double = 0.95
                          ): PartialResult[BoundedDouble] = {
    if (totalCalculated.isDefined) {
      val c = count()
      new PartialResult(new BoundedDouble(c, 1.0, c, c), true)
    } else {
      withScope {
        val countElements: (TaskContext, Iterator[Row]) => Long = { (_, iterator) =>
          var result = 0L
          while (iterator.hasNext) {
            result += 1L
            iterator.next()
          }
          result
        }
        val evaluator = new CountEvaluator(partitions.length, confidence)
        sparkSession.sparkContext.runApproximateJob(this, countElements, evaluator, timeout)
      }
    }
  }

  /**
    * Return if the RDD is empty. Optimized when count are called before
    */
  override def isEmpty(): Boolean = {
    totalCalculated.fold {
      withScope {
        partitions.length == 0 || take(1).length == 0
      }
    } { total => total == 0L }
  }

  override def getPartitions: Array[Partition] =
    dataFrame.rdd.partitions

  override def compute(thePart: Partition, context: TaskContext): Iterator[Row] =
    dataFrame.rdd.compute(thePart, context)

  override def getPreferredLocations(thePart: Partition): Seq[String] =
    dataFrame.rdd.preferredLocations(thePart)
}