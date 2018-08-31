/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.cassandra

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.enumerators.SaveModeEnum.{Append, ErrorIfExists, Ignore, Overwrite}
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.plugin.common.cassandra.CassandraBase
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession

class CassandraOutputStep(
                           name: String,
                           xDSession: XDSession,
                           properties: Map[String, JSerializable]
                         ) extends OutputStep(name, xDSession, properties) with CassandraBase {


  override def supportedSaveModes: Seq[SaveModeEnum.Value] = Seq(Append, ErrorIfExists, Ignore, Overwrite)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {

    require(keyspace.isDefined, "It is mandatory to define the keyspace")
    require(cluster.isDefined, "It is mandatory to define the cluster")
    validateSaveMode(saveMode)

    val tableName = getTableNameFromOptions(options)
    val sparkConfig = getSparkConfig(tableName)

    dataFrame.write
      .format(CassandraClass)
      .mode(getSparkSaveMode(saveMode))
      .options(sparkConfig ++ getCustomProperties)
      .save()
  }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations =
    validateProperties(options)
}

object CassandraOutputStep {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    Seq(
      ("spark.cassandra.connection.host", CassandraBase.getConnectionHostsConf(configuration))
    )
  }
}


