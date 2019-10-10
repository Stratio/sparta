/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.input.postgres

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions}
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.common.postgresql.PostgresSuiteBase
import com.stratio.sparta.plugin.workflow.input.postgres.PostgresInputStepBatch
import com.stratio.sparta.serving.core.constants.AppConstant.DefaultEnvironmentParametersMap
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers, ShouldMatchers}


@RunWith(classOf[JUnitRunner])
class PostgresInputStepBatchIT extends TemporalSparkContext with PostgresSuiteBase with ShouldMatchers with Matchers with BeforeAndAfterAll {



  val tableName = "test"
  val driver = DefaultEnvironmentParametersMap("POSTGRES_DRIVER").value.getOrElse("")
  val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))


  val properties: Map[String, JSerializable] = Map("url" -> postgresURL, "dbtable" -> tableName, "driver" -> driver)

  override def beforeAll(): Unit = {
    val createStatement: String = s"CREATE TABLE $tableName (ID int NOT NULL, NAME VARCHAR NOT NULL, PRIMARY KEY (ID))"
    withConnectionExecute(createStatement)
    val insertStatement: String = s"INSERT INTO $tableName (ID, NAME) VALUES ('1','Turing'), ('2','Apollo'), ('3','Pacman'), " +
      s"('4','Babagge'), ('5','Ada'), ('6','Infinity'), ('7','Space Invaders'), ('8','Space AI')"
    withConnectionExecute(insertStatement)
  }

  override def afterAll(): Unit = {
    val dropStatement: String = s"DROP TABLE $tableName"
    withConnectionExecute(dropStatement)
  }

  "Read a table from Postgres" should "returns a dataframe with the table" in {

    val postgresInputStep = new PostgresInputStepBatch(name = "postgresInput", outputOptions, Option(ssc), sparkSession, properties)
    val rdd = postgresInputStep.initWithSchema()._1
    val count = rdd.ds.count()

    count shouldBe 8
  }
}