/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.custom

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.workflow.step.OutputStep._
import com.stratio.sparta.plugin.TemporalSparkContext
import org.apache.spark.sql.crossdata.XDSession
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner



@RunWith(classOf[JUnitRunner])
class CustomCommonOutputStepIT extends TemporalSparkContext with Matchers with BeforeAndAfterAll {

  import TestLiteOutputStep._

  val optionsMap = Map(
    PrimaryKey -> PrimaryKeyValue,
    TableNameKey -> TableValue,
    PartitionByKey -> PartitionByKeyValue
  )

  "CustomLiteOutput" should "be compatible with 2.6 plugins" in {

    val sparkSession = XDSession.builder().config(sc.getConf).create("dummyUser")
    import sparkSession.implicits._

    val classStr = new TestLiteOutputStep(sparkSession, Map.empty).getClass.getName
    val outputLite = new CustomLiteOutputStep("name", sparkSession, Map(CustomLiteOutputCommon.CustomLiteClassTypeProp -> classStr))

    outputLite.save(Seq(8).toDF(PartitionByKeyValue), SaveModeEnum.Append, optionsMap)

  }

}
