/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.core.models

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.OutputWriterOptions.OutputStepNameNA
import com.stratio.sparta.core.properties.ValidatingPropertyMap._


case class OutputOptions(outputWriterOptions: Seq[OutputWriterOptions]) {

  def outputStepWriterOptions(stepName: String, outputStepName: String): OutputWriterOptions = {
    outputWriterOptions.find(_.outputStepName == outputStepName)
      .orElse(outputWriterOptions.find(_.outputStepName == OutputStepNameNA))
      .getOrElse(OutputWriterOptions.defaultOutputWriterOptions(stepName, Option(outputStepName)))
  }

}

case class OutputWriterOptions(
                                saveMode: SaveModeEnum.Value = SaveModeEnum.Append,
                                stepName: String,
                                outputStepName: String,
                                tableName: String,
                                errorTableName: String,
                                discardTableName: Option[String] = None,
                                extraOptions: Map[String, String] = Map.empty[String, String]
                              )

case object OutputWriterOptions {

  val OutputStepNameNA = "NONE"

  def defaultOutputWriterOptions(
                                  nodeName: String,
                                  outputStepName: Option[String] = None,
                                  tableName: Option[String] = None
                                ): OutputWriterOptions = {
    OutputWriterOptions(
      saveMode = SaveModeEnum.Append,
      stepName = nodeName,
      tableName = tableName.notBlank.getOrElse(nodeName),
      errorTableName = nodeName,
      outputStepName = outputStepName.notBlank.getOrElse(OutputStepNameNA)
    )
  }

  def defaultOutputOptions(
                            nodeName: String,
                            outputStepName: Option[String] = None,
                            tableName: Option[String] = None
                          ): OutputOptions = {
    OutputOptions(Seq(defaultOutputWriterOptions(nodeName, outputStepName.notBlank, tableName.notBlank)))
  }
}
