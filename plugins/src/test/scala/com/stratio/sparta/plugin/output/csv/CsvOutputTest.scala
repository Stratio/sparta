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
package com.stratio.sparta.plugin.output.csv

import com.databricks.spark.csv._
import com.stratio.sparta.plugin.output.csv.CsvOutput
import com.stratio.sparta.sdk.Output
import org.apache.spark.sql._
import org.junit.runner.RunWith
import org.mockito.Matchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}


@RunWith(classOf[JUnitRunner])
class CsvOutputTest extends FlatSpec with Matchers with MockitoSugar {
  "CsvOutput" should "upsert a file" in {
    val out = spy(new CsvOutput("keyName", None, Map("path" -> "path"), Seq()))
    val dataframe = mock[DataFrame]
    implicit val savemock = mock[CsvSchemaRDD]
    doNothing().when(out).saveAction(any[String], meq(dataframe))

    out.upsert(dataframe, Map(Output.TableNameKey -> "tableName"))

    verify(out).saveAction(any[String], meq(dataframe))
  }
}
