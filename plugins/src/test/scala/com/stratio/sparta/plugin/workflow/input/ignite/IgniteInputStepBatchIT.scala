/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.input.ignite

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions}
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.common.ignite.IgniteSuiteBase
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers, ShouldMatchers}


@RunWith(classOf[JUnitRunner])
class IgniteInputStepBatchIT extends TemporalSparkContext with IgniteSuiteBase with ShouldMatchers with Matchers with BeforeAndAfterAll {


  val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
  val tableName = "test"
  val properties: Map[String, JSerializable] = Map("host" -> igniteURL, "dbtable" -> tableName, "fetchSize" -> fetchSize )

  Class.forName("org.apache.ignite.IgniteJdbcThinDriver")

  override def beforeAll(): Unit = {
    val createStatement: String = s"CREATE TABLE $tableName (ID int NOT NULL, NAME VARCHAR NOT NULL, PRIMARY KEY (ID))"
    withConnectionExecute(createStatement)
    val insertStatement: String = s"INSERT INTO $tableName (ID, NAME) VALUES ('1','Pepe'), ('2','Juan'), ('3','Esteban'), " +
      s"('4','Roberto'), ('5','Teresa'), ('6','Manuel'), ('7','Jose Luis'), ('8','Julia')"
    withConnectionExecute(insertStatement)
  }

  override def afterAll(): Unit = {
    val dropStatement: String = s"DROP TABLE $tableName"
    withConnectionExecute(dropStatement)
  }

  "Read a table from Ignite" should "returns a dataframe with the table" in {

    val igniteInputStep = new IgniteInputStepBatch("igniteInput", outputOptions, Option(ssc), sparkSession, properties)
    val rdd = igniteInputStep .initWithSchema()._1
    val count = rdd.ds.count()

    count shouldBe 8
  }
}