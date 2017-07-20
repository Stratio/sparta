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
package org.apache.spark.streaming.datasource.models

case class InputSentences(
                           query: String,
                           offsetConditions: Option[OffsetConditions],
                           initialStatements: Seq[String]
                         ) {

  override def toString: String =
    s"[Query: $query" +
      s"\nOffsetConditions: $offsetConditions" +
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
   * @return The inputSentence object with all options
   */
  def apply(
             query: String,
             offsetConditions: OffsetConditions,
             initialStatements: Seq[String]
           ): InputSentences = new InputSentences(query, Option(offsetConditions), initialStatements)

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
           ): InputSentences = new InputSentences(query, Option(offsetConditions), Seq.empty[String])

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
           ): InputSentences = new InputSentences(query, None, initialStatements)

  /**
   * Constructor for create input objects with the query conditions for monitoring tables from dataSources
   *
   * @param query Initial query same as " select * from tableName "
   * @return The inputSentence object with all options
   */
  def apply(
             query: String
           ): InputSentences = new InputSentences(query, None, Seq.empty[String])
}