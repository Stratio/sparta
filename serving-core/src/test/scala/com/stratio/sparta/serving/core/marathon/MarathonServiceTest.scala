/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.marathon

import com.stratio.sparta.core.properties.{JsoneyString, JsoneyStringSerializer}
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.{KeyValuePair, MarathonDeploymentSettings, Workflow, WorkflowExecution}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, OptionValues, ShouldMatchers, WordSpec}
import org.json4s.jackson.JsonMethods._
import org.json4s._
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}




@RunWith(classOf[JUnitRunner])
class MarathonServiceTest extends WordSpec with ShouldMatchers with Matchers with MockitoSugar with OptionValues
  with SpartaSerializer {
  import MarathonServiceTest._


  def workflowObjFromString(workflowString: String): Workflow =
    read[Workflow](workflowString)

  val expectedMarathonDeploymentConfig = MarathonDeploymentSettings(
    gracePeriodSeconds = Option(JsoneyString("360")),
    intervalSeconds = Option(JsoneyString("20")),
    timeoutSeconds = Option(JsoneyString("12")),
    maxConsecutiveFailures = Option(JsoneyString("7")),
    forcePullImage = Option(true),
    userEnvVariables = Seq(KeyValuePair(key = JsoneyString("ENV1"), value = JsoneyString("VAL1"))),
    userLabels = Seq(KeyValuePair(key = JsoneyString("LAB2"), value = JsoneyString("VAL2"))),
    logLevel= Option(JsoneyString("ERROR"))
  )
  val expectedEmptyMarathonDeploymentConfig = MarathonDeploymentSettings()

  "MarathonService" when {
    "the front pass a json containing the workflow" should {
      "store the marathonDeploymentSettings config if correct" in {
        val validWf = workflowObjFromString(stringCorrectWf)
        validWf.settings.global.marathonDeploymentSettings.value should equal(expectedMarathonDeploymentConfig)
      }
      "return an empty marathonDeploymentSettings config" in {
        val emptyWf = workflowObjFromString(stringEmptyWf)
        emptyWf.settings.global.marathonDeploymentSettings should be (None)
      }
    }
  }

}

object MarathonServiceTest{
  val stringEmptyWf= """{
                       |  "id": "e6d59fd3-9503-45f5-89f5-92260fcd08c1",
                       |  "name": "workflow-aftercheck",
                       |  "description": "",
                       |  "settings": {
                       |    "global": {
                       |      "executionMode": "marathon",
                       |      "userPluginsJars": [],
                       |      "preExecutionSqlSentences": [],
                       |      "postExecutionSqlSentences": [],
                       |      "addAllUploadedPlugins": true,
                       |      "mesosConstraint": "",
                       |      "mesosConstraintOperator": "CLUSTER",
                       |      "parametersLists": ["Environment"],
                       |      "parametersUsed": ["Global.DEFAULT_OUTPUT_FIELD", "Global.SPARK_CORES_MAX", "Global.SPARK_DRIVER_CORES", "Global.SPARK_DRIVER_JAVA_OPTIONS", "Global.SPARK_DRIVER_MEMORY", "Global.SPARK_EXECUTOR_CORES", "Global.SPARK_EXECUTOR_EXTRA_JAVA_OPTIONS", "Global.SPARK_EXECUTOR_MEMORY", "Global.SPARK_LOCALITY_WAIT", "Global.SPARK_LOCAL_PATH", "Global.SPARK_MEMORY_FRACTION", "Global.SPARK_TASK_MAX_FAILURES"],
                       |      "udfsToRegister": [],
                       |      "udafsToRegister": [],
                       |      "mesosRole": ""
                       |    },
                       |    "streamingSettings": {
                       |      "window": "2s",
                       |      "blockInterval": "100ms",
                       |      "checkpointSettings": {
                       |        "checkpointPath": "sparta/checkpoint",
                       |        "enableCheckpointing": true,
                       |        "autoDeleteCheckpoint": true,
                       |        "addTimeToCheckpointPath": false
                       |      }
                       |    },
                       |    "sparkSettings": {
                       |      "master": "mesos://leader.mesos:5050",
                       |      "sparkKerberos": true,
                       |      "sparkDataStoreTls": true,
                       |      "sparkMesosSecurity": true,
                       |      "submitArguments": {
                       |        "userArguments": [],
                       |        "deployMode": "client",
                       |        "driverJavaOptions": "{{{Global.SPARK_DRIVER_JAVA_OPTIONS}}}"
                       |      },
                       |      "sparkConf": {
                       |        "sparkResourcesConf": {
                       |          "coresMax": "{{{Global.SPARK_CORES_MAX}}}",
                       |          "executorMemory": "{{{Global.SPARK_EXECUTOR_MEMORY}}}",
                       |          "executorCores": "{{{Global.SPARK_EXECUTOR_CORES}}}",
                       |          "driverCores": "{{{Global.SPARK_DRIVER_CORES}}}",
                       |          "driverMemory": "{{{Global.SPARK_DRIVER_MEMORY}}}",
                       |          "mesosExtraCores": "",
                       |          "localityWait": "{{{Global.SPARK_LOCALITY_WAIT}}}",
                       |          "taskMaxFailures": "{{{Global.SPARK_TASK_MAX_FAILURES}}}",
                       |          "sparkMemoryFraction": "{{{Global.SPARK_MEMORY_FRACTION}}}",
                       |          "sparkParallelism": ""
                       |        },
                       |        "sparkHistoryServerConf": {
                       |          "enableHistoryServerMonitoring": false
                       |        },
                       |        "userSparkConf": [],
                       |        "coarse": true,
                       |        "sparkUser": "",
                       |        "sparkLocalDir": "{{{Global.SPARK_LOCAL_PATH}}}",
                       |        "sparkKryoSerialization": false,
                       |        "sparkSqlCaseSensitive": true,
                       |        "logStagesProgress": false,
                       |        "hdfsTokenCache": true,
                       |        "executorExtraJavaOptions": "{{{Global.SPARK_EXECUTOR_EXTRA_JAVA_OPTIONS}}}"
                       |      }
                       |    },
                       |    "errorsManagement": {
                       |      "genericErrorManagement": {
                       |        "whenError": "Error"
                       |      },
                       |      "transformationStepsManagement": {
                       |        "whenError": "Error",
                       |        "whenRowError": "RowError",
                       |        "whenFieldError": "FieldError"
                       |      },
                       |      "transactionsManagement": {
                       |        "sendToOutputs": [],
                       |        "sendStepData": false,
                       |        "sendPredecessorsData": true,
                       |        "sendInputData": true
                       |      }
                       |    }
                       |  },
                       |  "pipelineGraph": {
                       |    "nodes": [{
                       |      "name": "Test",
                       |      "stepType": "Input",
                       |      "className": "TestInputStep",
                       |      "classPrettyName": "Test",
                       |      "arity": ["NullaryToNary"],
                       |      "writer": {
                       |        "saveMode": "Append",
                       |        "tableName": "",
                       |        "partitionBy": "",
                       |        "errorTableName": "",
                       |        "discardTableName": ""
                       |      },
                       |      "uiConfiguration": {
                       |        "position": {
                       |          "x": 415,
                       |          "y": 145
                       |        }
                       |      },
                       |      "configuration": {
                       |        "eventType": "STRING",
                       |        "event": "aaaa",
                       |        "numEvents": "10",
                       |        "outputField": "{{{Global.DEFAULT_OUTPUT_FIELD}}}"
                       |      },
                       |      "supportedEngines": ["Batch"],
                       |      "executionEngine": "Batch",
                       |      "supportedDataRelations": ["ValidData"],
                       |      "lineageProperties": []
                       |    }, {
                       |      "name": "Print",
                       |      "stepType": "Output",
                       |      "className": "PrintOutputStep",
                       |      "classPrettyName": "Print",
                       |      "arity": ["NullaryToNullary", "NaryToNullary"],
                       |      "writer": {
                       |        "saveMode": "Append"
                       |      },
                       |      "uiConfiguration": {
                       |        "position": {
                       |          "x": 656,
                       |          "y": 145
                       |        }
                       |      },
                       |      "configuration": {
                       |        "printData": "",
                       |        "printSchema": "",
                       |        "errorSink": "",
                       |        "printMetadata": true,
                       |        "logLevel": "warn"
                       |      },
                       |      "supportedEngines": ["Streaming", "Batch"],
                       |      "executionEngine": "Batch",
                       |      "lineageProperties": []
                       |    }],
                       |    "edges": [{
                       |      "origin": "Test",
                       |      "destination": "Print",
                       |      "dataType": "ValidData"
                       |    }]
                       |  },
                       |  "executionEngine": "Batch",
                       |  "uiSettings": {
                       |    "position": {
                       |      "x": 0,
                       |      "y": 0,
                       |      "k": 1
                       |    }
                       |  },
                       |  "lastUpdateDate": "2019-01-31T14:58:06Z",
                       |  "version": 0,
                       |  "group": {
                       |    "id": "940800b2-6d81-44a8-84d9-26913a2faea4",
                       |    "name": "/home"
                       |  },
                       |  "debugMode": false,
                       |  "groupId": "940800b2-6d81-44a8-84d9-26913a2faea4"
                       |}""".stripMargin

  val stringCorrectWf = """{
                          |  "id": "e6d59fd3-9503-45f5-89f5-92260fcd08c1",
                          |  "name": "workflow-aftercheck",
                          |  "description": "",
                          |  "settings": {
                          |    "global": {
                          |      "executionMode": "marathon",
                          |      "userPluginsJars": [],
                          |      "preExecutionSqlSentences": [],
                          |      "postExecutionSqlSentences": [],
                          |      "addAllUploadedPlugins": true,
                          |      "mesosConstraint": "",
                          |      "mesosConstraintOperator": "CLUSTER",
                          |      "parametersLists": ["Environment"],
                          |      "parametersUsed": ["Global.DEFAULT_OUTPUT_FIELD", "Global.SPARK_CORES_MAX", "Global.SPARK_DRIVER_CORES", "Global.SPARK_DRIVER_JAVA_OPTIONS", "Global.SPARK_DRIVER_MEMORY", "Global.SPARK_EXECUTOR_CORES", "Global.SPARK_EXECUTOR_EXTRA_JAVA_OPTIONS", "Global.SPARK_EXECUTOR_MEMORY", "Global.SPARK_LOCALITY_WAIT", "Global.SPARK_LOCAL_PATH", "Global.SPARK_MEMORY_FRACTION", "Global.SPARK_TASK_MAX_FAILURES"],
                          |      "udfsToRegister": [],
                          |      "udafsToRegister": [],
                          |      "mesosRole": "",
                          |      "marathonDeploymentSettings": {
                          |        "gracePeriodSeconds": "360",
                          |        "intervalSeconds": "20",
                          |        "timeoutSeconds": "12",
                          |        "maxConsecutiveFailures": "7",
                          |        "forcePullImage": true,
                          |        "privileged": false,
                          |        "userEnvVariables": [{
                          |          "key": "ENV1",
                          |          "value": "VAL1"
                          |        }],
                          |        "userLabels": [{
                          |          "key": "LAB2",
                          |          "value": "VAL2"
                          |        }],
                          |        "logLevel": "ERROR"
                          |      }
                          |    },
                          |    "streamingSettings": {
                          |      "window": "2s",
                          |      "blockInterval": "100ms",
                          |      "checkpointSettings": {
                          |        "checkpointPath": "sparta/checkpoint",
                          |        "enableCheckpointing": true,
                          |        "autoDeleteCheckpoint": true,
                          |        "addTimeToCheckpointPath": false
                          |      }
                          |    },
                          |    "sparkSettings": {
                          |      "master": "mesos://leader.mesos:5050",
                          |      "sparkKerberos": true,
                          |      "sparkDataStoreTls": true,
                          |      "sparkMesosSecurity": true,
                          |      "submitArguments": {
                          |        "userArguments": [],
                          |        "deployMode": "client",
                          |        "driverJavaOptions": "{{{Global.SPARK_DRIVER_JAVA_OPTIONS}}}"
                          |      },
                          |      "sparkConf": {
                          |        "sparkResourcesConf": {
                          |          "coresMax": "{{{Global.SPARK_CORES_MAX}}}",
                          |          "executorMemory": "{{{Global.SPARK_EXECUTOR_MEMORY}}}",
                          |          "executorCores": "{{{Global.SPARK_EXECUTOR_CORES}}}",
                          |          "driverCores": "{{{Global.SPARK_DRIVER_CORES}}}",
                          |          "driverMemory": "{{{Global.SPARK_DRIVER_MEMORY}}}",
                          |          "mesosExtraCores": "",
                          |          "localityWait": "{{{Global.SPARK_LOCALITY_WAIT}}}",
                          |          "taskMaxFailures": "{{{Global.SPARK_TASK_MAX_FAILURES}}}",
                          |          "sparkMemoryFraction": "{{{Global.SPARK_MEMORY_FRACTION}}}",
                          |          "sparkParallelism": ""
                          |        },
                          |        "sparkHistoryServerConf": {
                          |          "enableHistoryServerMonitoring": false
                          |        },
                          |        "userSparkConf": [],
                          |        "coarse": true,
                          |        "sparkUser": "",
                          |        "sparkLocalDir": "{{{Global.SPARK_LOCAL_PATH}}}",
                          |        "sparkKryoSerialization": false,
                          |        "sparkSqlCaseSensitive": true,
                          |        "logStagesProgress": false,
                          |        "hdfsTokenCache": true,
                          |        "executorExtraJavaOptions": "{{{Global.SPARK_EXECUTOR_EXTRA_JAVA_OPTIONS}}}"
                          |      }
                          |    },
                          |    "errorsManagement": {
                          |      "genericErrorManagement": {
                          |        "whenError": "Error"
                          |      },
                          |      "transformationStepsManagement": {
                          |        "whenError": "Error",
                          |        "whenRowError": "RowError",
                          |        "whenFieldError": "FieldError"
                          |      },
                          |      "transactionsManagement": {
                          |        "sendToOutputs": [],
                          |        "sendStepData": false,
                          |        "sendPredecessorsData": true,
                          |        "sendInputData": true
                          |      }
                          |    }
                          |  },
                          |  "pipelineGraph": {
                          |    "nodes": [{
                          |      "name": "Test",
                          |      "stepType": "Input",
                          |      "className": "TestInputStep",
                          |      "classPrettyName": "Test",
                          |      "arity": ["NullaryToNary"],
                          |      "writer": {
                          |        "saveMode": "Append",
                          |        "tableName": "",
                          |        "partitionBy": "",
                          |        "errorTableName": "",
                          |        "discardTableName": ""
                          |      },
                          |      "uiConfiguration": {
                          |        "position": {
                          |          "x": 415,
                          |          "y": 145
                          |        }
                          |      },
                          |      "configuration": {
                          |        "eventType": "STRING",
                          |        "event": "aaaa",
                          |        "numEvents": "10",
                          |        "outputField": "{{{Global.DEFAULT_OUTPUT_FIELD}}}"
                          |      },
                          |      "supportedEngines": ["Batch"],
                          |      "executionEngine": "Batch",
                          |      "supportedDataRelations": ["ValidData"],
                          |      "lineageProperties": []
                          |    }, {
                          |      "name": "Print",
                          |      "stepType": "Output",
                          |      "className": "PrintOutputStep",
                          |      "classPrettyName": "Print",
                          |      "arity": ["NullaryToNullary", "NaryToNullary"],
                          |      "writer": {
                          |        "saveMode": "Append"
                          |      },
                          |      "uiConfiguration": {
                          |        "position": {
                          |          "x": 656,
                          |          "y": 145
                          |        }
                          |      },
                          |      "configuration": {
                          |        "printData": "",
                          |        "printSchema": "",
                          |        "errorSink": "",
                          |        "printMetadata": true,
                          |        "logLevel": "warn"
                          |      },
                          |      "supportedEngines": ["Streaming", "Batch"],
                          |      "executionEngine": "Batch",
                          |      "lineageProperties": []
                          |    }],
                          |    "edges": [{
                          |      "origin": "Test",
                          |      "destination": "Print",
                          |      "dataType": "ValidData"
                          |    }]
                          |  },
                          |  "executionEngine": "Batch",
                          |  "uiSettings": {
                          |    "position": {
                          |      "x": 0,
                          |      "y": 0,
                          |      "k": 1
                          |    }
                          |  },
                          |  "lastUpdateDate": "2019-01-31T14:58:06Z",
                          |  "version": 0,
                          |  "group": {
                          |    "id": "940800b2-6d81-44a8-84d9-26913a2faea4",
                          |    "name": "/home"
                          |  },
                          |  "debugMode": false,
                          |  "groupId": "940800b2-6d81-44a8-84d9-26913a2faea4"
                          |}""".stripMargin

}