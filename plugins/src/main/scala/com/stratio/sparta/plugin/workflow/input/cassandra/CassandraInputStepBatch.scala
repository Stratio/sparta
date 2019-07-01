/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.cassandra

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.InputStep
import com.stratio.sparta.plugin.common.cassandra.CassandraBase
import com.stratio.sparta.plugin.helper.SecurityHelper
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

import scala.util._

class CassandraInputStepBatch(
                               name: String,
                               outputOptions: OutputOptions,
                               ssc: Option[StreamingContext],
                               xDSession: XDSession,
                               properties: Map[String, JSerializable]
                             )
  extends InputStep[RDD](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging with CassandraBase {

  lazy val table = properties.getString("table", None).notBlank
  lazy val tlsKey = "tlsEnabled"
  lazy val sparkConf = xDSession.conf.getAll
  lazy val tlsEnabled = Try(properties.getString(tlsKey, "false").toBoolean).getOrElse(false)
  lazy val securityOpts = if (tlsEnabled) SecurityHelper.cassandraSecurityOptions(sparkConf) else Map.empty

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = validateProperties(options)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The step name $name is not valid.", name)
      )

    if (table.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"table cannot be empty", name)
      )

    if (debugOptions.isDefined && !validDebuggingOptions)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"$errorDebugValidation", name)
      )

    validation
  }

  //Dummy function on batch inputs that generates DataSets with schema
  def init(): DistributedMonad[RDD] = {
    throw new Exception("Not used on inputs that generates DataSets with schema")
  }

  override def initWithSchema(): (DistributedMonad[RDD], Option[StructType]) = {
    require(keyspace.isDefined, "It is mandatory to define the keyspace")
    require(cluster.isDefined, "It is mandatory to define the cluster")
    require(table.isDefined, "It is mandatory to define the table")

    val filterOptions = Seq(NodesKey, KeyspaceKey, ClusterKey, TableKey)
    val userOptions = getCustomProperties.flatMap { case (key, value) =>
      if (!filterOptions.contains(key))
        Option(key -> value.toString)
      else None
    }
    val df = xDSession.sqlContext.read
      .format(CassandraClass)
      .options(getSparkConfig(table.get) ++ securityOpts ++ userOptions)
      .load()

    (df.rdd, Option(df.schema))
  }
}

object CassandraInputStepBatch {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }

}


