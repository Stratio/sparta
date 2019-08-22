/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.migration

import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant.{regexMatchingMoustacheVariable, version}
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.models.workflow.migration.{TemplateElementOrion, WorkflowAndromeda, WorkflowOrion}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex.Match

object MigrationUtils {

  val migrationStart = new MigrationUtils(false, 20)
  val migrationEndpoint = new MigrationUtils(true, 5)
}

class MigrationUtils private(private val refreshDB: Boolean, timeout: Int) {

  import com.stratio.sparta.serving.core.models.workflow.migration.MigrationModelImplicits._
  import scala.concurrent.ExecutionContext.Implicits.global

  private val paramListPostgresService = PostgresDaoFactory.parameterListPostgresDao
  private val globalParameterPostgresService = PostgresDaoFactory.globalParametersService

  private def movedToEnvironment: Set[String] = {
    if (refreshDB)
      findEnvironmentPostgres
    else
      movedToEnvironmentStart
  }

  private def findEnvironmentPostgres: Set[String] = {
    val list: Future[Set[String]] = for {
      envList <- paramListPostgresService.findById(AppConstant.EnvironmentParameterListId.get)
    } yield envList.parameters.map(_.name).toSet

    Try(Await.result(list, timeout seconds)) match {
      case Success(newSet) => newSet ++ defaultEnvironment
      case Failure(_) => defaultEnvironment
    }
  }

  private lazy val movedToEnvironmentStart: Set[String] = findEnvironmentPostgres

  private def movedToGlobal: Set[String] = {
    if (refreshDB) findGlobalPostgres
    else
      movedToGlobalStart
  }

  private lazy val movedToGlobalStart: Set[String] = findGlobalPostgres

  private def findGlobalPostgres: Set[String] = {
    val list: Future[Set[String]] = for {
      globalList <- globalParameterPostgresService.find()
    } yield globalList.variables.map(_.name).toSet

    Try(Await.result(list, timeout seconds)) match {
      case Success(newSet) => newSet ++ defaultGlobal
      case Failure(_) => defaultGlobal
    }
  }

  private val defaultEnvironment = Set(
    "CROSSDATA_ZOOKEEPER_CONNECTION", "CROSSDATA_ZOOKEEPER_PATH", "KAFKA_BROKER_HOST", "KAFKA_BROKER_PORT", "CASSANDRA_HOST", "CASSANDRA_PORT", "ES_HOST", "ES_PORT", "JDBC_URL", "JDBC_DRIVER", "POSTGRES_URL", "MONGODB_DB", "MONGODB_HOST", "MONGODB_PORT", "CASSANDRA_KEYSPACE", "CASSANDRA_CLUSTER", "KAFKA_GROUP_ID", "KAFKA_MAX_POLL_TIMEOUT", "KAFKA_MAX_RATE_PER_PARTITION", "ES_INDEX_MAPPING", "WEBSOCKET_URL", "REDIS_HOST", "REDIS_PORT")

  private val defaultGlobal = Set("DEFAULT_OUTPUT_FIELD", "DEFAULT_DELIMITER", "SPARK_EXECUTOR_BASE_IMAGE", "SPARK_DRIVER_JAVA_OPTIONS", "SPARK_EXECUTOR_EXTRA_JAVA_OPTIONS", "SPARK_STREAMING_CHECKPOINT_PATH",
    "SPARK_STREAMING_WINDOW", "SPARK_STREAMING_BLOCK_INTERVAL", "SPARK_LOCAL_PATH", "SPARK_CORES_MAX", "SPARK_EXECUTOR_MEMORY", "SPARK_EXECUTOR_CORES", "SPARK_DRIVER_CORES", "SPARK_DRIVER_MEMORY", "SPARK_LOCALITY_WAIT", "SPARK_TASK_MAX_FAILURES", "SPARK_MEMORY_FRACTION")

  private def matchEnvironmentVariables: Match => String = { matchFromRegex =>
    def matchAnonymous(matchFromRegex: Match): String = {
      val oldValue = matchFromRegex.group(0)
      if (movedToEnvironment.contains(oldValue)) s"Environment.$oldValue"
      else if (movedToGlobal.contains(oldValue)) s"Global.$oldValue"
      else oldValue
    }

    matchAnonymous(matchFromRegex)
  }

  private def andromedaMigrationNewJsoney(value: JsoneyString): JsoneyString =
    JsoneyString(regexMatchingMoustacheVariable.replaceAllIn(value.toString, matchEnvironmentVariables))

  private def andromedaMigrationString(value: String): String =
    regexMatchingMoustacheVariable.replaceAllIn(value.toString, matchEnvironmentVariables)

  def fromAndromedaToOrionTemplate(template: TemplateElementOrion): TemplateElementOrion = {
    val newConfig: Map[String, JsoneyString] = for {
      (key, value: JsoneyString) <- template.configuration
    } yield {
      key -> andromedaMigrationNewJsoney(value)
    }
    template.copy(versionSparta = Some(AppConstant.OrionVersion), configuration = newConfig)
  }

  def fromAndromedaToOrionWorkflow(workflow: WorkflowAndromeda): WorkflowOrion = {
    val workflowWithJsoneySubstitution: WorkflowAndromeda = workflow.copy(
      pipelineGraph =
        workflow.pipelineGraph.copy(nodes =
          workflow.pipelineGraph.nodes.map { node =>
            val newConfig: Map[String, JsoneyString] = for {
              (key, value: JsoneyString) <- node.configuration
            } yield {
              key -> andromedaMigrationNewJsoney(value)
            }
            val oldWriter = node.writer.getOrElse(WriterGraph())
            val newWriter = oldWriter.copy(
              tableName = oldWriter.tableName
                .map(value => andromedaMigrationString(value)),
              partitionBy = oldWriter.partitionBy
                .map(value => andromedaMigrationString(value)),
              constraintType = oldWriter.constraintType
                .map(value => andromedaMigrationString(value)),
              primaryKey = oldWriter.primaryKey
                .map(value => andromedaMigrationString(value)),
              uniqueConstraintName = oldWriter.uniqueConstraintName
                .map(value => andromedaMigrationString(value)),
              uniqueConstraintFields = oldWriter.uniqueConstraintFields
                .map(value => andromedaMigrationString(value)),
              updateFields = None,
              errorTableName = oldWriter.errorTableName
                .map(value => andromedaMigrationString(value))
            )
            node.copy(configuration = newConfig, writer = Option(newWriter))
          }
        ),
      settings = {
        val newGlobalSettings = workflow.settings.global.copy(
          userPluginsJars = workflow.settings.global.userPluginsJars
            .map { case UserJar(jarPathJsoney) => UserJar(andromedaMigrationNewJsoney(jarPathJsoney)) },
          initSqlSentences = workflow.settings.global.initSqlSentences
            .map { case SqlSentence(sentence) => SqlSentence(andromedaMigrationNewJsoney(sentence)) },
          mesosConstraint =
            workflow.settings.global.mesosConstraint.fold(Option.empty[JsoneyString]) {
              value =>
                Option(andromedaMigrationNewJsoney(value))
            },
          mesosConstraintOperator =
            workflow.settings.global.mesosConstraintOperator.fold(Option.empty[JsoneyString]) {
              value =>
                Option(andromedaMigrationNewJsoney(value))
            }
        )

        val newStreamingSettings = workflow.settings.streamingSettings.copy(
          window = andromedaMigrationNewJsoney(workflow.settings.streamingSettings.window),
          remember = workflow.settings.streamingSettings.remember.fold(Option.empty[JsoneyString]) {
            value => Option(andromedaMigrationNewJsoney(value))
          },
          backpressureInitialRate = workflow.settings.streamingSettings.backpressureInitialRate
            .map(value => andromedaMigrationNewJsoney(value)),
          backpressureMaxRate = workflow.settings.streamingSettings.backpressureMaxRate
            .map(value => andromedaMigrationNewJsoney(value)),
          blockInterval = workflow.settings.streamingSettings.blockInterval
            .map(value => andromedaMigrationNewJsoney(value)),
          checkpointSettings = workflow.settings.streamingSettings.checkpointSettings
            .copy(checkpointPath = andromedaMigrationNewJsoney(
              workflow.settings.streamingSettings.checkpointSettings.checkpointPath))
        )

        val newSparkSettings = workflow.settings.sparkSettings.copy(
          master = andromedaMigrationNewJsoney(workflow.settings.sparkSettings.master),
          submitArguments = {
            workflow.settings.sparkSettings.submitArguments.copy(
              driverJavaOptions = workflow.settings.sparkSettings.submitArguments.driverJavaOptions
                .map(value => andromedaMigrationNewJsoney(value)),
              userArguments = workflow.settings.sparkSettings.submitArguments.userArguments.map {
                case UserSubmitArgument(keyJsoney, valueJsoney) =>
                  UserSubmitArgument(andromedaMigrationNewJsoney(keyJsoney), andromedaMigrationNewJsoney(valueJsoney))
              }
            )
          },
          sparkConf = {
            workflow.settings.sparkSettings.sparkConf.copy(
              sparkResourcesConf = {
                val sparkResConf = workflow.settings.sparkSettings.sparkConf.sparkResourcesConf
                sparkResConf.copy(
                  coresMax = sparkResConf.coresMax
                    .map(value => andromedaMigrationNewJsoney(value)),
                  executorMemory = sparkResConf.executorMemory
                    .map(value => andromedaMigrationNewJsoney(value)),
                  executorCores = sparkResConf.executorCores
                    .map(value => andromedaMigrationNewJsoney(value)),
                  driverCores = sparkResConf.driverCores
                    .map(value => andromedaMigrationNewJsoney(value)),
                  driverMemory = sparkResConf.driverMemory
                    .map(value => andromedaMigrationNewJsoney(value)),
                  mesosExtraCores = sparkResConf.mesosExtraCores
                    .map(value => andromedaMigrationNewJsoney(value)),
                  localityWait = sparkResConf.localityWait
                    .map(value => andromedaMigrationNewJsoney(value)),
                  taskMaxFailures = sparkResConf.taskMaxFailures
                    .map(value => andromedaMigrationNewJsoney(value)),
                  sparkMemoryFraction = sparkResConf.sparkMemoryFraction
                    .map(value => andromedaMigrationNewJsoney(value)),
                  sparkParallelism = sparkResConf.sparkParallelism
                    .map(value => andromedaMigrationNewJsoney(value))
                )
              },
              userSparkConf = workflow.settings.sparkSettings.sparkConf.userSparkConf.map {
                case SparkProperty(keyJsoney, valueJsoney) =>
                  SparkProperty(andromedaMigrationNewJsoney(keyJsoney), andromedaMigrationNewJsoney(valueJsoney))
              },
              sparkUser = workflow.settings.sparkSettings.sparkConf.sparkUser
                .map(value => andromedaMigrationNewJsoney(value)),
              sparkLocalDir = workflow.settings.sparkSettings.sparkConf.sparkLocalDir
                .map(value => andromedaMigrationNewJsoney(value)),
              executorExtraJavaOptions = workflow.settings.sparkSettings.sparkConf.executorExtraJavaOptions
                .map(value => andromedaMigrationNewJsoney(value))
            )
          },
          killUrl = workflow.settings.sparkSettings.killUrl
            .map(value => andromedaMigrationNewJsoney(value))
        )

        workflow.settings.copy(
          global = newGlobalSettings,
          streamingSettings = newStreamingSettings,
          sparkSettings = newSparkSettings
        )
      }
    )
    val orionWorkflow: WorkflowOrion = workflowWithJsoneySubstitution
    val globalSetting = orionWorkflow.settings.global.copy(preExecutionSqlSentences = workflowWithJsoneySubstitution.settings.global.initSqlSentences)
    orionWorkflow.copy(settings = orionWorkflow.settings.copy(global = globalSetting))
  }
}