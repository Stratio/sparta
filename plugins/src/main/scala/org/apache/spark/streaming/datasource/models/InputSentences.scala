/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.streaming.datasource.models

case class InputSentences(
                           query: String,
                           offsetConditions: Option[OffsetConditions],
                           initialStatements: Seq[String],
                           continuousStatements: Seq[String]
                         ) {

  override def toString: String =
    s"[Query: $query" +
      s"\nOffsetConditions: $offsetConditions" +
      s"\nContinuousStatements: ${continuousStatements.mkString(" , ")}" +
      s"\nInitialStatements: ${initialStatements.mkString(" , ")}]"

  def extractLimitSentence: String =
    if (offsetConditions.isDefined && offsetConditions.get.limitRecords.isDefined)
      s" LIMIT ${offsetConditions.get.limitRecords.get}"
    else ""
}

object InputSentences {

  /**
   * Constructor for create input objects with the query conditions for monitoring tables from dataSources
   *
   * @param query             Initial query same as " select * from tableName "
   * @param offsetConditions  Conditions object with offset field and results limit
   * @param initialStatements Initial query statements to execute with the SqlContext. Useful when the user need to
   *                          create temporal tables related to one table in the dataSource
   * @param continuousStatements Continuous query statements to execute with the SqlContext on each window. Useful when
   *                          the user needs to refresh one or various tables related to a certain dataSource.
   * @return The inputSentence object with all options
   */
  def apply(
             query: String,
             offsetConditions: OffsetConditions,
             initialStatements: Seq[String],
             continuousStatements: Seq[String]
           ): InputSentences = new InputSentences(query, Option(offsetConditions), initialStatements, continuousStatements)

  /**
   * Constructor for create input objects with the query conditions for monitoring tables from dataSources
   *
   * @param query            Initial query same as " select * from tableName "
   * @param offsetConditions Conditions object with offset field and results limit
   * @return The inputSentence object with all options
   */
  def apply(
             query: String,
             offsetConditions: OffsetConditions
           ): InputSentences = new InputSentences(query, Option(offsetConditions), Seq.empty[String], Seq.empty[String])

  /**
   * Constructor for create input objects with the query conditions for monitoring tables from dataSources
   *
   * @param query             Initial query same as " select * from tableName "
   * @param initialStatements Initial query statements to execute with the SqlContext. Useful when the user need to
   *                          create temporal tables related to one table in the dataSource
   * @return The inputSentence object with all options
   */
  def apply(
             query: String,
             initialStatements: Seq[String]
           ): InputSentences = new InputSentences(query, None, initialStatements, Seq.empty[String])

  /**
   * Constructor for create input objects with the query conditions for monitoring tables from dataSources
   *
   * @param query Initial query same as " select * from tableName "
   * @return The inputSentence object with all options
   */
  def apply(
             query: String
           ): InputSentences = new InputSentences(query, None, Seq.empty[String], Seq.empty[String])
}