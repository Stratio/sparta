/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.streaming.datasource.receiver

import org.apache.spark.partial.{BoundedDouble, CountEvaluator, PartialResult}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.parser.{AbstractSqlParser, AstBuilder, ParserInterface}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.commons.lang.StringUtils
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.streaming.datasource.models.{InputSentences, OffsetConditions, OffsetField, OffsetOperator}
import org.apache.spark.{Partition, TaskContext}

private[datasource]
class DatasourceRDD(
                     @transient sparkSession: SparkSession,
                     inputSentences: InputSentences,
                     datasourceParams: Map[String, String]
                   ) extends RDD[Row](sparkSession.sparkContext, Nil) with DatasourceRDDHelper {

  private var totalCalculated: Option[Long] = None

  private val LimitedTableName = "limitedTable"
  private val complexQuery = checkIfComplexQuery(inputSentences.query)
  lazy private val initialWhereCondition = if (complexQuery) None
  else retrieveWhereCondition(inputSentences.query)

  val dataFrame: DataFrame =
    inputSentences.offsetConditions.fold(sparkSession.sql(inputSentences.query)) { offset =>
      val parsedQuery = parseInitialQuery(complexQuery,inputSentences.query)
      if (parsedQuery.equals(TempInitQuery)) initializeTempTable
      val conditionsSentence = offset.extractConditionSentence(initialWhereCondition)
      val orderSentence = offset.extractOrderSentence(parsedQuery, inverse = offset.limitRecords.isEmpty)
      val limitSentence = inputSentences.extractLimitSentence
      if(possibleConflictsWRTColumns(initialWhereCondition, offset))
        log.warn("One or more columns specified as Offset appear in the user-provided WHERE condition")
      sparkSession.sql(parsedQuery + conditionsSentence + orderSentence + limitSentence)
    }

  private def initializeTempTable =
    sparkSession.sql(inputSentences.query).createOrReplaceTempView(InitTableName)


  def progressInputSentences: InputSentences = {
    if (!dataFrame.rdd.isEmpty()) {
      inputSentences.offsetConditions.fold(inputSentences) { offset =>
        val offsetValues = if (offset.limitRecords.isEmpty){
          val firstRecord = dataFrame.rdd.first()
            offset.fromOffset.map( currentField =>
              (currentField.name,firstRecord.get(dataFrame.schema.fieldIndex(currentField.name)))
            ).toMap
           }
        else {
          dataFrame.createOrReplaceTempView(LimitedTableName)
          val limitedQuery = s"select * from $LimitedTableName " +
            offset.extractOrderSentence(inputSentences.query, true) +
            " limit 1"
          val currentRecord = sparkSession.sql(limitedQuery).rdd.first()
            offset.fromOffset.map( currentField =>
              (currentField.name,currentRecord.get(dataFrame.schema.fieldIndex(currentField.name)))).toMap
        }

        val updatedConditions =
          offset.fromOffset.map(
            currentField =>
              OffsetField(currentField.name,
                if(offset.fromOffset.size == 1)
                  OffsetOperator.toProgressOperator(currentField.operator)
                else
                  OffsetOperator.toMultiProgressOperator(currentField.operator),
                offsetValues(currentField.name)
          ))

        inputSentences.copy(offsetConditions =
          Option(offset.copy(fromOffset = updatedConditions)))
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
                            confidence: Double = 0.95): PartialResult[BoundedDouble] = {
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

  override def getPartitions: Array[Partition] = dataFrame.rdd.partitions

  override def compute(thePart: Partition, context: TaskContext): Iterator[Row] =
    dataFrame.rdd.compute(thePart, context)

  override def getPreferredLocations(thePart: Partition): Seq[String] = dataFrame.rdd.preferredLocations(thePart)
}