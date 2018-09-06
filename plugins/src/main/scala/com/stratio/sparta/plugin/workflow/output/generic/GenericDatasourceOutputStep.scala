/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.generic

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.plugin.helper.ErrorValidationsHelper
import com.stratio.sparta.plugin.helper.ErrorValidationsHelper.HasError
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession

object GenericDatasourceOutputStep{
  val Placeholder = "_TABLE_NAME_"
  val Datasource = "datasource"
  val TableKeyOption = "tableKeyOption"
  val TableValuePattern = "tablePattern"
}

class GenericDatasourceOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  import GenericDatasourceOutputStep._

  override private[sparta] def supportedSaveModes = SaveModeEnum.sparkSaveModes

  // TODO datasource alternatives should be recovered from Classpath in order to avoid user errors
  lazy val datasource: Option[String] = properties.getString(Datasource, None)
  lazy val tableKeyOption: Option[String] = properties.getString(TableKeyOption, None)
  lazy val tableValuePattern: Option[String] = properties.getString(TableValuePattern, None)

  override def validate(options: Map[String, String] = Map.empty): ErrorValidations = {

    val validationSeq = Seq[(HasError,String)](
      datasource.isEmpty -> "The custom class field cannot be empty",
      tableKeyOption.isEmpty -> "The table key field cannot be empty"
    )

    ErrorValidationsHelper.validate(validationSeq, name)
  }

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val source: String =
      datasource.getOrElse(throw new RuntimeException("The input datasource format is mandatory"))

    validateSaveMode(saveMode)

    val tableName = getTableNameFromOptions(options)

    val tableKey: String = tableKeyOption.getOrElse(throw new RuntimeException("The table key is mandatory"))
    val tableValue: String =
      properties.getString("tablePattern", None)
        .map(_.replaceAll(Placeholder, tableName))
        .getOrElse(tableName)

    val userOptions = getCustomProperties + (tableKey -> tableValue)

    if (log.isDebugEnabled){
      log.debug(s"Generic datasource '$source' options: $userOptions")
    }

    applyPartitionBy(options, dataFrame.write, dataFrame.schema.fields)
      .format(source)
      .mode(getSparkSaveMode(saveMode))
      .options(userOptions)
      .save()
  }

}