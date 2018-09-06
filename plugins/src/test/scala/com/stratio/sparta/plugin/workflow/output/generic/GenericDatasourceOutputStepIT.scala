/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.generic

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.TemporalSparkContext
import org.apache.spark.sql.crossdata.XDSession
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.reflect.io.File

@RunWith(classOf[JUnitRunner])
class GenericDatasourceOutputStepIT extends TemporalSparkContext with Matchers with BeforeAndAfterAll {

  private val tempPath: String = File.makeTemp().name

  trait CommonValues {
    val xdSession: XDSession = XDSession.builder().config(sc.getConf).create("dummyUser")
  }

  trait WithEventData extends CommonValues {
    import xdSession.implicits._
    val data = sc.parallelize(Seq(Person("Marcos", 18), Person("Juan", 21), Person("Jose", 26))).toDS().toDF
  }

  val properties =
    Map(
      GenericDatasourceOutputStep.Datasource -> "parquet",
      GenericDatasourceOutputStep.TableKeyOption -> "path",
      GenericDatasourceOutputStep.TableValuePattern -> s"$tempPath/_TABLE_NAME_"
    )


  Seq(GenericDatasourceOutputStep.Datasource, GenericDatasourceOutputStep.TableKeyOption).foreach{ mandatoryProperty =>
    "GenericDatasourceOutput" should s"not validate an output without '$mandatoryProperty' option" in new CommonValues {
      val filteredProperties = properties.filterNot{ case (k, _) => k == mandatoryProperty}
      val genericDSOutput = new GenericDatasourceOutputStep("genericdatasource-test", sparkSession, filteredProperties)
      genericDSOutput.validate().valid shouldBe false
    }
  }


  "GenericDatasourceOutput" should s"validate an output without 'tablePattern' option" in new CommonValues {
    val filteredProperties = properties.filterNot{ case (k, _) => k == "tablePattern"}
    val genericDSOutput = new GenericDatasourceOutputStep("genericdatasource-test", sparkSession, filteredProperties)
    genericDSOutput.validate().valid shouldBe true
  }



  it should "save a DataFrame using 'Append' mode" in new WithEventData {
    val genericDSOutput = new GenericDatasourceOutputStep("genericdatasource-test", sparkSession, properties)
    genericDSOutput.validate().valid shouldBe true
    genericDSOutput.save(data, SaveModeEnum.Append, Map(genericDSOutput.TableNameKey -> "person"))
    val read = xdSession.read.parquet(s"$tempPath/person")
    read.count should be(3)
    File(tempPath).deleteRecursively()
  }

}

case class Person(name: String, age: Int) extends Serializable