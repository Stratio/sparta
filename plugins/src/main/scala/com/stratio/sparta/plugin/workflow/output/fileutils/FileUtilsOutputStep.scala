/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.fileutils

import java.io.{File, Serializable => JSerializable}
import java.net.URI

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.plugin.enumerations.FilesystemActionType.FilesystemActionType
import com.stratio.sparta.plugin.enumerations.FilesystemErrorPolicyType.FilesystemErrorPolicyType
import com.stratio.sparta.plugin.enumerations.{FilesystemActionType, FilesystemErrorPolicyType}
import com.stratio.sparta.serving.core.services.HdfsService
import org.apache.commons.io.FilenameUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, FileUtil, Path}
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row}
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}

class FileUtilsOutputStep (name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  import FileUtilsOutputStep._

  private val format = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm")

  implicit lazy val fileActionsOnComplete: Seq[FileActions] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val expression = s"${properties.getString("fileActionsOnComplete", None).notBlank.fold("[]") { values => values.toString }}"
    read[Seq[FileActions]](expression)
  }

  implicit lazy val inputDataFrame : InputFromDataframe = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    properties.getString("inputFromDataframe", None).map{ expression =>
      read[InputFromDataframe](expression)
    }.get
  }

  implicit lazy val inputBinary : InputBinary = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    properties.getString("inputBinary", None).map{ expression =>
      read[InputBinary](expression)
    }.get
  }


  lazy val filesystemConfiguration: Configuration = xDSession.sparkContext.hadoopConfiguration

  lazy val fs: FileSystem = FileSystem.get(filesystemConfiguration)
  lazy private val hdfsService = HdfsService()

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    if (inputDataFrame.inputDataframeField.notBlank.isDefined || inputBinary.inputBinaryField.notBlank.isDefined) {

      implicit val dataFrameCollected = dataFrame.collect()
      implicit val schema = dataFrame.schema

      checkDataframeActionAndExecute {
        dataframe =>
          dataframe.foreach { row =>
            val fileOrigin = row.getString(row.fieldIndex(inputDataFrame.inputDataframeField.get))
            val fileDestination = row.getString(row.fieldIndex(inputDataFrame.inputDataframeDestination.get))
            filesystemAction(fileOrigin,
              fileDestination,
              getAction(inputDataFrame.inputDataframeAction.get),
              inputDataFrame.addTimestampToFilenameFromDataframe,
              getErrorPolicy(inputDataFrame.errorPolicy.get)
            )
          }
      }

      checkBinaryAndExecute {
        dataframe =>
          dataframe.foreach { row =>
            hdfsService.writeBinary(row.getAs[Array[Byte]](row.fieldIndex(inputBinary.inputBinaryField.get)),
              if (inputBinary.inputBinaryDestinationField.isDefined)
                row.getString(row.fieldIndex(inputBinary.inputBinaryDestinationField.get))
              else
                inputBinary.inputBinaryDestinationPath.get,
              overwrite = true,
              failOnException = inputBinary.errorPolicy.fold(true){ _.equalsIgnoreCase("ERROR")}
            )
          }
      }
    }
  }

  def checkDataframeActionAndExecute(f: Function1[Array[Row], Unit])
                                    (implicit inputDataFrame: InputFromDataframe,
                                     dataFrame: Array[Row],
                                     schema: StructType) = {

    import inputDataFrame._
    if (inputDataframeField.notBlank.isDefined && inputDataframeDestination.notBlank.isDefined) {
      require(schema.exists(el => el.name == inputDataframeField.get), s"The input field ${inputDataframeField.get} is not present inside the dataframe")
      require(schema.exists(el => el.name == inputDataframeDestination.get), s"The destination field ${inputDataframeDestination.get} is not present inside the dataframe")
      f(dataFrame)
    }
  }

  def checkBinaryAndExecute(f: Function1[Array[Row], Unit])
                           (implicit inputBinary: InputBinary,
                            dataFrame: Array[Row],
                            schema: StructType) = {

    import inputBinary._
    if (inputBinaryField.notBlank.isDefined && (inputBinaryDestinationPath.notBlank.isDefined || inputBinaryDestinationField.isDefined)) {
      require(schema.exists(el => el.name == inputBinaryField.get), s"The input field ${inputBinaryField.get} is not present inside the dataframe")
      if (inputBinaryDestinationPath.isDefined)
        assume(dataFrame.length == 1L, s"There are more than one row, each row will overwrite the destination file ${inputBinaryDestinationPath.get}")
      if (inputBinaryDestinationField.isDefined)
        require(schema.exists(el => el.name == inputBinaryDestinationField.get), s"The destination field ${inputBinaryDestinationField.get} is not present inside the dataframe")

    }
  }

  override def cleanUp(options: Map[String, String] = Map.empty[String, String]): Unit = {
    fileActionsOnComplete.foreach {
      fileAction =>
        filesystemAction(fileAction.fileOrigin, fileAction.fileDestination,
          getAction(fileAction.fileAction),
          fileAction.addTimestampToFilename,
          getErrorPolicy(fileAction.errorPolicy)
        )
    }
  }

  protected def getAction(action: String) : FilesystemActionType.FilesystemActionType = Try {
    FilesystemActionType.withName(action.toUpperCase())
  }.getOrElse(FilesystemActionType.COPY)

  protected def getErrorPolicy(errorPolicy: String) : FilesystemErrorPolicyType.FilesystemErrorPolicyType = Try {
    FilesystemErrorPolicyType.withName(errorPolicy.toUpperCase())
  }.getOrElse(FilesystemErrorPolicyType.ERROR)


  protected def filesystemAction(fileOrigin: String,
                                 fileDestination: String,
                                 actionType: FilesystemActionType,
                                 addTimestampToFilename: Boolean = false,
                                 errorPolicy: FilesystemErrorPolicyType ) : Unit =
    Try {
      val extensionFileOutput = FilenameUtils.getExtension(fileDestination)
      val finalFileDestination = if (addTimestampToFilename && !new File(fileOrigin).isDirectory)
        s"${FilenameUtils.removeExtension(fileDestination)}-${format.format(new java.util.Date())}.$extensionFileOutput"
      else fileDestination

      val originFS = FileSystem.get(URI.create(fileOrigin), filesystemConfiguration)
      val destinationFS = FileSystem.get(URI.create(finalFileDestination), filesystemConfiguration)

      actionType match {
        case FilesystemActionType.COPY =>
          FileUtil.copy(
            originFS,
            new Path(fileOrigin),
            destinationFS,
            new Path(finalFileDestination),
            false,
            true,
            filesystemConfiguration )
        case FilesystemActionType.MOVE =>
         FileUtil.copy(
            originFS,
            new Path(fileOrigin),
            destinationFS,
            new Path(finalFileDestination),
            true,
            true,
            filesystemConfiguration)
        case FilesystemActionType.DELETE =>
          originFS.delete(new Path(fileOrigin), true)
      }
    } match {
      case Success(_) => log.info(s"Successfully executed action $actionType on $fileOrigin to $fileDestination")
      case Failure(e) =>
        errorPolicy match {
          case FilesystemErrorPolicyType.ERROR =>
            throw new RuntimeException(s"Cannot execute action $actionType on $fileOrigin. Error: ${e.getLocalizedMessage}")
          case FilesystemErrorPolicyType.DISCARD =>
            log.error(s"Cannot execute action $actionType on $fileOrigin. Error: ${e.getLocalizedMessage}")
        }
    }
}

object FileUtilsOutputStep {

  case class FileActions(fileOrigin: String,
                         fileDestination: String,
                         fileAction: String,
                         addTimestampToFilename: Boolean = false,
                         errorPolicy: String)

  case class InputBinary(inputBinaryField: Option[String],
                         inputBinaryDestinationPath: Option[String],
                         inputBinaryDestinationField: Option[String],
                         errorPolicy: Option[String])

  case class InputFromDataframe(inputDataframeField: Option[String],
                                inputDataframeDestination: Option[String],
                                inputDataframeAction:  Option[String],
                                addTimestampToFilenameFromDataframe: Boolean,
                                errorPolicy: Option[String]
                               )
}