/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.input.xls
import java.io.{Serializable => JSerializable}

import com.crealytics.spark.excel._
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.InputStep
import com.stratio.sparta.plugin.common.csv.CsvBase
import com.stratio.sparta.serving.core.workflow.lineage.HdfsLineage
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

import scala.util.Try

class XlsInputStepBatch(
                         name: String,
                         outputOptions: OutputOptions,
                         ssc: Option[StreamingContext],
                         xDSession: XDSession,
                         properties: Map[String, JSerializable]
                       )
  extends InputStep[RDD](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging with HdfsLineage {

  lazy val treatEmptyValuesAsNulls: Option[String] = properties.getString("treatEmptyValuesAsNulls",None).notBlank
  lazy val location: Option[String] = properties.getString("location",None).notBlank
  lazy val sheetName: Option[String] = properties.getString("sheetName",None).notBlank
  lazy val useHeader = Try(properties.getString("useHeader", "false").toBoolean).getOrElse(false)
  lazy val dataRange : Option[String] = properties.getString("dataRange", None).notBlank
  val sheetKey="sheetName"
  val dataRangeKey="dataRange"
  val locationKey="location"
  override lazy val lineagePath: String = location.getOrElse("")

  override lazy val lineageResourceSuffix: Option[String] = Option(".xls")

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The step name $name is not valid.", name)
      )

    if (sheetName.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"Sheet name cannot be empty", name)
      )

    if (location.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"location cannot be empty", name)
      )
    if (dataRange.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"Data range cannot be empty (start_cell:end_cell)", name)
      )

    if(debugOptions.isDefined && !validDebuggingOptions)
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

  override def lineageProperties(): Map[String, String] = getHdfsLineageProperties(InputStep.StepType)

  override def initWithSchema(): (DistributedMonad[RDD], Option[StructType]) = {
    require(location.nonEmpty, "The input path cannot be empty")

    val userOptions = propertiesWithCustom.flatMap { case (key, value) =>
      if(key==locationKey || key==sheetKey || key==dataRangeKey)
        None
      else
        Option(key -> value.toString)
    }

    val sheetData= "'"+sheetName.getOrElse(false).toString +"'!"+ dataRange.getOrElse(false).toString

    val userOptions2 :Map[String,String]= userOptions + ("dataAddress" -> sheetData)

    val df = xDSession.read.format("com.crealytics.spark.excel").options(userOptions2).load(location.get) //options(userOptions).load(path.get) //https://stackoverflow.com/questions/44196741/how-to-construct-dataframe-from-a-excel-xls-xlsx-file-in-scala-spark

    (df.rdd, Option(df.schema))
  }

}