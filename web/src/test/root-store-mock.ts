/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
export const ROOT_STORE_MOCK = {
   user: {
      userName: '',
         editFromMonitoring: false,
         xDSparkUi: 'http://localhost:4041',
         timeout: 20000
   },
   alerts: {
      confirmSave: false,
         currentAlert: [    ],
         notification: null,
         showPersistentError: false
   },
   workflowsManaging: {
      workflowsManaging: {
         currentLevel: {
            id: '940800b2-6d81-44a8-84d9-26913a2faea4',
               name: '/home'
         },
         groups: [
            {
               id: 'e33f5b0d-009c-42ec-b5fc-5678d0f9c5c1',
               name: '/home/adsf'
            },
            {
               id: '940800b2-6d81-44a8-84d9-26913a2faea4',
               name: '/home'
            }
         ],
            loading: false,
            workflowList: [   {
               id: '37c9264d-66cf-4e9e-aee1-8b98b3139c30',
               name: 'generali-stat-corporativas-batch',
               description: 'Ensurance example for ingestion and data cleaning',
               settings: {
                  executionMode: 'marathon',
                  userPluginsJars: [],
                  initSqlSentences: [
                     {
                        sentence: 'CREATE TABLE IF NOT EXISTS sie_stat_corporativas USING com.databricks.spark.csv OPTIONS (path \'hdfs://10.200.0.74:8020/user/dg-agent/csv/generali/sie_stat_corporativas.csv\', sep \'\\t\', header \'true\',  inferSchema \'true\')'
                     },
                     {
                        sentence: 'CREATE TABLE IF NOT EXISTS dim_comercial USING com.databricks.spark.csv OPTIONS(path \'hdfs://10.200.0.74:8020/user/dg-agent/csv/generali/sie_estructura_comercial.csv\', header \'true\', inferSchema \'true\', delimiter \'\\t\')'
                     },
                     {
                        sentence: 'CREATE TABLE IF NOT EXISTS sie_rem_preguntas USING com.databricks.spark.csv OPTIONS(path \'hdfs://10.200.0.74:8020/user/dg-agent/csv/generali/sie_rem_preguntas.csv\', header \'true\', inferSchema \'true\', delimiter \'\\t\')'
                     }
                  ],
                  addAllUploadedPlugins: true,
                  mesosConstraint: '',
                  mesosConstraintOperator: 'CLUSTER'
               },
               nodes: [
                  {
                     name: 'Crossdata',
                     stepType: 'Input'
                  },
                  {
                     name: 'Casting',
                     stepType: 'Transformation'
                  },
                  {
                     name: 'variables_calc',
                     stepType: 'Transformation'
                  },
                  {
                     name: 'Select_Indi',
                     stepType: 'Transformation'
                  },
                  {
                     name: '2016_cluster1',
                     stepType: 'Transformation'
                  },
                  {
                     name: '2016_cluster3',
                     stepType: 'Transformation'
                  },
                  {
                     name: '2016_cluster2',
                     stepType: 'Transformation'
                  },
                  {
                     name: '2015_cluster1',
                     stepType: 'Transformation'
                  },
                  {
                     name: '2015_cluster2',
                     stepType: 'Transformation'
                  },
                  {
                     name: '2015_cluster3',
                     stepType: 'Transformation'
                  },
                  {
                     name: 'resto_cluster1',
                     stepType: 'Transformation'
                  },
                  {
                     name: 'resto_cluster3',
                     stepType: 'Transformation'
                  },
                  {
                     name: 'resto_cluster2',
                     stepType: 'Transformation'
                  },
                  {
                     name: 'Mix_dim_com',
                     stepType: 'Transformation'
                  },
                  {
                     name: 'Postgres(1)',
                     stepType: 'Output'
                  },
                  {
                     name: 'Postgres(1)(1)',
                     stepType: 'Output'
                  },
                  {
                     name: 'Repartition',
                     stepType: 'Transformation'
                  }
               ],
               executionEngine: 'Batch',
               lastUpdateDate: '2018-04-30T09:35:55Z',
               version: 0,
               group: '/home',
               tags: [
                  'etl',
                  'data cleaning',
                  'ensurance',
                  'generali'
               ],
               status: {
                  id: '37c9264d-66cf-4e9e-aee1-8b98b3139c30',
                  status: 'Failed',
                  statusId: 'b9cd1932-2f71-4346-8757-5f406fd51f57',
                  statusInfo: 'Error launching workflow with the selected execution mode',
                  lastUpdateDate: '2018-06-05T08:35:50Z'
               },
               lastUpdateAux: 1525080955000,
               lastUpdate: '30 Apr 2018 - 11:35',
               tagsAux: 'etl, data cleaning, ensurance, generali',
               formattedStatus: 'Failed'
            },
               {
                  id: '344d6786-1820-4274-ba08-55a2350241c1',
                  name: 'nuevoworkflow',
                  description: '',
                  settings: {
                     executionMode: 'marathon',
                     userPluginsJars: [],
                     initSqlSentences: [],
                     addAllUploadedPlugins: true,
                     mesosConstraint: '',
                     mesosConstraintOperator: 'CLUSTER'
                  },
                  nodes: [
                     {
                        name: 'Test',
                        stepType: 'Input'
                     },
                     {
                        name: 'Text',
                        stepType: 'Output'
                     }
                  ],
                  executionEngine: 'Streaming',
                  version: 1,
                  group: '/home',
                  status: {
                     id: '344d6786-1820-4274-ba08-55a2350241c1',
                     status: 'Failed',
                     statusId: 'dfbbbb18-94cb-4f30-bb21-4ab1696bd72c',
                     statusInfo: 'An error was encountered while launching the Workflow App in the Marathon API',
                     lastUpdateDate: '2018-06-22T07:03:10Z'
                  },
                  execution: {
                     id: '344d6786-1820-4274-ba08-55a2350241c1',
                     marathonExecution: {
                        marathonId: 'sparta/undefined/workflows/home/nuevoworkflow/nuevoworkflow-v1'
                     },
                     genericDataExecution: {
                        executionMode: 'marathon',
                        executionId: 'dff3b867-cbfa-4bd7-b686-27e8f8b2dbaf',
                        lastError: {
                           message: 'An error was encountered while launching the Workflow App in the Marathon API',
                           phase: 'Launch',
                           exceptionMsg: 'java.io.FileNotFoundException: /etc/sds/sparta/marathon-app-template.json (No such file or directory)',
                           localizedMsg: '/etc/sds/sparta/marathon-app-template.json (No such file or directory)',
                           date: '2018-06-22T07:03:10Z'
                        }
                     }
                  },
                  lastUpdateAux: null,
                  lastUpdate: '',
                  tagsAux: '',
                  formattedStatus: 'Failed',
                  lastErrorDate: '22 Jun 2018 - 9:03'
               }],
            workflowsVersionsList: [
               {
                  name: 'generali-stat-corporativas-batch',
                  type: 'Batch',
                  group: '/home',
                  lastUpdateAux: 1525080955000,
                  lastUpdate: '30 Apr 2018 - 11:35',
                  versions: [
                     {
                        id: '37c9264d-66cf-4e9e-aee1-8b98b3139c30',
                        name: 'generali-stat-corporativas-batch',
                        description: 'Ensurance example for ingestion and data cleaning',
                        settings: {
                           executionMode: 'marathon',
                           userPluginsJars: [],
                           initSqlSentences: [
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS sie_stat_corporativas USING com.databricks.spark.csv OPTIONS (path \'hdfs://10.200.0.74:8020/user/dg-agent/csv/generali/sie_stat_corporativas.csv\', sep \'\\t\', header \'true\',  inferSchema \'true\')'
                              },
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS dim_comercial USING com.databricks.spark.csv OPTIONS(path \'hdfs://10.200.0.74:8020/user/dg-agent/csv/generali/sie_estructura_comercial.csv\', header \'true\', inferSchema \'true\', delimiter \'\\t\')'
                              },
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS sie_rem_preguntas USING com.databricks.spark.csv OPTIONS(path \'hdfs://10.200.0.74:8020/user/dg-agent/csv/generali/sie_rem_preguntas.csv\', header \'true\', inferSchema \'true\', delimiter \'\\t\')'
                              }
                           ],
                           addAllUploadedPlugins: true,
                           mesosConstraint: '',
                           mesosConstraintOperator: 'CLUSTER'
                        },
                        nodes: [
                           {
                              name: 'Crossdata',
                              stepType: 'Input'
                           },
                           {
                              name: 'Casting',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'variables_calc',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'Select_Indi',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2016_cluster1',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2016_cluster3',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2016_cluster2',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2015_cluster1',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2015_cluster2',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2015_cluster3',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'resto_cluster1',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'resto_cluster3',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'resto_cluster2',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'Mix_dim_com',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'Postgres(1)',
                              stepType: 'Output'
                           },
                           {
                              name: 'Postgres(1)(1)',
                              stepType: 'Output'
                           },
                           {
                              name: 'Repartition',
                              stepType: 'Transformation'
                           }
                        ],
                        executionEngine: 'Batch',
                        lastUpdateDate: '2018-04-30T09:35:55Z',
                        version: 0,
                        group: '/home',
                        tags: [
                           'etl',
                           'data cleaning',
                           'ensurance',
                           'generali'
                        ],
                        status: {
                           id: '37c9264d-66cf-4e9e-aee1-8b98b3139c30',
                           status: 'Failed',
                           statusId: 'b9cd1932-2f71-4346-8757-5f406fd51f57',
                           statusInfo: 'Error launching workflow with the selected execution mode',
                           lastUpdateDate: '2018-06-05T08:35:50Z'
                        },
                        lastUpdateAux: 1525080955000,
                        lastUpdate: '30 Apr 2018 - 11:35',
                        tagsAux: 'etl, data cleaning, ensurance, generali',
                        formattedStatus: 'Failed'
                     },
                     {
                        id: '46fd724a-31f0-4095-acbd-e436a68f37c2',
                        name: 'generali-stat-corporativas-batch',
                        description: 'Ensurance example for ingestion and data cleaning',
                        settings: {
                           executionMode: 'marathon',
                           userPluginsJars: [],
                           initSqlSentences: [
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS sie_stat_corporativas USING com.databricks.spark.csv OPTIONS (path \'hdfs://10.200.0.74:8020/user/dg-agent/csv/generali/sie_stat_corporativas.csv\', sep \'\\t\', header \'true\',  inferSchema \'true\')'
                              },
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS dim_comercial USING com.databricks.spark.csv OPTIONS(path \'hdfs://10.200.0.74:8020/user/dg-agent/csv/generali/sie_estructura_comercial.csv\', header \'true\', inferSchema \'true\', delimiter \'\\t\')'
                              },
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS sie_rem_preguntas USING com.databricks.spark.csv OPTIONS(path \'hdfs://10.200.0.74:8020/user/dg-agent/csv/generali/sie_rem_preguntas.csv\', header \'true\', inferSchema \'true\', delimiter \'\\t\')'
                              }
                           ],
                           addAllUploadedPlugins: true,
                           mesosConstraint: '',
                           mesosConstraintOperator: 'CLUSTER'
                        },
                        nodes: [
                           {
                              name: 'Crossdata',
                              stepType: 'Input'
                           },
                           {
                              name: 'Casting',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'variables_calc',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'Select_Indi',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2016_cluster1',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2016_cluster3',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2016_cluster2',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2015_cluster1',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2015_cluster2',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2015_cluster3',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'resto_cluster1',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'resto_cluster3',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'resto_cluster2',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'Mix_dim_com',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'Postgres(1)',
                              stepType: 'Output'
                           },
                           {
                              name: 'Postgres(1)(1)',
                              stepType: 'Output'
                           },
                           {
                              name: 'Repartition',
                              stepType: 'Transformation'
                           }
                        ],
                        executionEngine: 'Batch',
                        lastUpdateDate: '2018-04-30T09:35:55Z',
                        version: 2,
                        group: '/home',
                        status: {
                           id: '46fd724a-31f0-4095-acbd-e436a68f37c2',
                           status: 'Created',
                           statusId: '2e17214c-e403-4b1e-b86d-67582e6a397f',
                           creationDate: '2018-06-21T11:22:29Z'
                        },
                        lastUpdateAux: 1525080955000,
                        lastUpdate: '30 Apr 2018 - 11:35',
                        tagsAux: '',
                        formattedStatus: 'Stopped'
                     },
                     {
                        id: '377eb2a9-6b0c-4d5c-bcbc-48406ad6a709',
                        name: 'generali-stat-corporativas-batch',
                        description: 'Ensurance example for ingestion and data cleaning',
                        settings: {
                           executionMode: 'marathon',
                           userPluginsJars: [],
                           initSqlSentences: [
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS sie_stat_corporativas USING com.databricks.spark.csv OPTIONS (path \'hdfs://10.200.0.74:8020/user/dg-agent/csv/generali/sie_stat_corporativas.csv\', sep \'\\t\', header \'true\',  inferSchema \'true\')'
                              },
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS dim_comercial USING com.databricks.spark.csv OPTIONS(path \'hdfs://10.200.0.74:8020/user/dg-agent/csv/generali/sie_estructura_comercial.csv\', header \'true\', inferSchema \'true\', delimiter \'\\t\')'
                              },
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS sie_rem_preguntas USING com.databricks.spark.csv OPTIONS(path \'hdfs://10.200.0.74:8020/user/dg-agent/csv/generali/sie_rem_preguntas.csv\', header \'true\', inferSchema \'true\', delimiter \'\\t\')'
                              }
                           ],
                           addAllUploadedPlugins: true,
                           mesosConstraint: '',
                           mesosConstraintOperator: 'CLUSTER'
                        },
                        nodes: [
                           {
                              name: 'Crossdata',
                              stepType: 'Input'
                           },
                           {
                              name: 'Casting',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'variables_calc',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'Select_Indi',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2016_cluster1',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2016_cluster3',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2016_cluster2',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2015_cluster1',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2015_cluster2',
                              stepType: 'Transformation'
                           },
                           {
                              name: '2015_cluster3',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'resto_cluster1',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'resto_cluster3',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'resto_cluster2',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'Mix_dim_com',
                              stepType: 'Transformation'
                           },
                           {
                              name: 'Postgres(1)',
                              stepType: 'Output'
                           },
                           {
                              name: 'Postgres(1)(1)',
                              stepType: 'Output'
                           },
                           {
                              name: 'Repartition',
                              stepType: 'Transformation'
                           }
                        ],
                        executionEngine: 'Batch',
                        lastUpdateDate: '2018-04-30T09:35:55Z',
                        version: 1,
                        group: '/home',
                        status: {
                           id: '377eb2a9-6b0c-4d5c-bcbc-48406ad6a709',
                           status: 'Created',
                           statusId: '73493ef0-d979-401a-ac3e-0f26b295b45c',
                           creationDate: '2018-06-21T11:22:28Z'
                        },
                        lastUpdateAux: 1525080955000,
                        lastUpdate: '30 Apr 2018 - 11:35',
                        tagsAux: '',
                        formattedStatus: 'Stopped'
                     }
                  ]
               },
               {
                  name: 'nuevoworkflow',
                  type: 'Streaming',
                  group: '/home',
                  lastUpdateAux: null,
                  lastUpdate: '',
                  versions: [
                     {
                        id: '344d6786-1820-4274-ba08-55a2350241c1',
                        name: 'nuevoworkflow',
                        description: '',
                        settings: {
                           executionMode: 'marathon',
                           userPluginsJars: [],
                           initSqlSentences: [],
                           addAllUploadedPlugins: true,
                           mesosConstraint: '',
                           mesosConstraintOperator: 'CLUSTER'
                        },
                        nodes: [
                           {
                              name: 'Test',
                              stepType: 'Input'
                           },
                           {
                              name: 'Text',
                              stepType: 'Output'
                           }
                        ],
                        executionEngine: 'Streaming',
                        version: 1,
                        group: '/home',
                        status: {
                           id: '344d6786-1820-4274-ba08-55a2350241c1',
                           status: 'Failed',
                           statusId: 'dfbbbb18-94cb-4f30-bb21-4ab1696bd72c',
                           statusInfo: 'An error was encountered while launching the Workflow App in the Marathon API',
                           lastUpdateDate: '2018-06-22T07:03:10Z'
                        },
                        execution: {
                           id: '344d6786-1820-4274-ba08-55a2350241c1',
                           marathonExecution: {
                              marathonId: 'sparta/undefined/workflows/home/nuevoworkflow/nuevoworkflow-v1'
                           },
                           genericDataExecution: {
                              executionMode: 'marathon',
                              executionId: 'dff3b867-cbfa-4bd7-b686-27e8f8b2dbaf',
                              lastError: {
                                 message: 'An error was encountered while launching the Workflow App in the Marathon API',
                                 phase: 'Launch',
                                 exceptionMsg: 'java.io.FileNotFoundException: /etc/sds/sparta/marathon-app-template.json (No such file or directory)',
                                 localizedMsg: '/etc/sds/sparta/marathon-app-template.json (No such file or directory)',
                                 date: '2018-06-22T07:03:10Z'
                              }
                           }
                        },
                        lastUpdateAux: null,
                        lastUpdate: '',
                        tagsAux: '',
                        formattedStatus: 'Failed',
                        lastErrorDate: '22 Jun 2018 - 9:03'
                     },
                     {
                        id: '1939a795-af27-40a2-a155-560e96fd3a79',
                        name: 'nuevoworkflow',
                        description: '',
                        settings: {
                           executionMode: 'marathon',
                           userPluginsJars: [],
                           initSqlSentences: [],
                           addAllUploadedPlugins: true,
                           mesosConstraint: '',
                           mesosConstraintOperator: 'CLUSTER'
                        },
                        nodes: [
                           {
                              name: 'Test',
                              stepType: 'Input'
                           },
                           {
                              name: 'Text',
                              stepType: 'Output'
                           }
                        ],
                        executionEngine: 'Streaming',
                        version: 2,
                        group: '/home',
                        status: {
                           id: '1939a795-af27-40a2-a155-560e96fd3a79',
                           status: 'Created',
                           statusId: 'df6ebd84-bd26-4e7e-a65d-1f75c0e2c079',
                           creationDate: '2018-06-21T14:09:38Z'
                        },
                        lastUpdateAux: null,
                        lastUpdate: '',
                        tagsAux: '',
                        formattedStatus: 'Stopped'
                     },
                     {
                        id: '517b0e3d-19b5-4af6-b7b8-d8637d416770',
                        name: 'nuevoworkflow',
                        description: '',
                        settings: {
                           executionMode: 'marathon',
                           userPluginsJars: [],
                           initSqlSentences: [],
                           addAllUploadedPlugins: true,
                           mesosConstraint: '',
                           mesosConstraintOperator: 'CLUSTER'
                        },
                        nodes: [
                           {
                              name: 'Test',
                              stepType: 'Input'
                           },
                           {
                              name: 'Text',
                              stepType: 'Output'
                           }
                        ],
                        executionEngine: 'Streaming',
                        version: 0,
                        group: '/home',
                        status: {
                           id: '517b0e3d-19b5-4af6-b7b8-d8637d416770',
                           status: 'Failed',
                           statusId: 'efe479e9-e778-4ec8-af6d-e9b09253066c',
                           statusInfo: 'An error was encountered while launching the Workflow App in the Marathon API',
                           lastUpdateDate: '2018-06-20T13:42:13Z'
                        },
                        execution: {
                           id: '517b0e3d-19b5-4af6-b7b8-d8637d416770',
                           marathonExecution: {
                              marathonId: 'sparta/undefined/workflows/home/nuevoworkflow/nuevoworkflow-v0'
                           },
                           genericDataExecution: {
                              executionMode: 'marathon',
                              executionId: '862c0c6e-3d6d-4d85-adaf-1363fe5de214',
                              endDate: '2018-06-20T13:42:13Z',
                              lastError: {
                                 message: 'An error was encountered while launching the Workflow App in the Marathon API',
                                 phase: 'Launch',
                                 exceptionMsg: 'java.io.FileNotFoundException: /etc/sds/sparta/marathon-app-template.json (No such file or directory)',
                                 localizedMsg: '/etc/sds/sparta/marathon-app-template.json (No such file or directory)',
                                 date: '2018-06-20T13:42:13Z'
                              }
                           }
                        },
                        lastUpdateAux: null,
                        lastUpdate: '',
                        tagsAux: '',
                        formattedStatus: 'Failed',
                        lastErrorDate: '20 Jun 2018 - 15:42'
                     }
                  ]
               }
            ],
            workflowsStatus: {},
         selectedWorkflows: [],
            openedWorkflow: null,
            selectedGroups: [],
            selectedVersions: [],
            selectedVersionsData: [],
            selectedEntities: [],
            executionInfo: null,
            workflowNameValidation: {
            validatedName: false
         },
         reload: true,
            showModal: true,
            modalError: ''
      },
      order: {
         sortOrder: {
            orderBy: 'name',
               type: 1
         },
         sortOrderVersions: {
            orderBy: 'version',
               type: 1
         }
      }
   },
   workflows: {
      workflows: {
         workflowList: [  {
            id: 'f3dcbf49-18d2-4b5d-a52c-d3ca60efc8a6',
            name: 'name111',
            description: '',
            settings: {
               executionMode: 'marathon',
               userPluginsJars: [],
               initSqlSentences: [],
               addAllUploadedPlugins: true,
               mesosConstraint: '',
               mesosConstraintOperator: 'CLUSTER'
            },
            nodes: [
               {
                  name: 'Test',
                  stepType: 'Input'
               },
               {
                  name: 'Print',
                  stepType: 'Output'
               },
               {
                  name: 'Casting',
                  stepType: 'Transformation'
               }
            ],
            executionEngine: 'Streaming',
            lastUpdateDate: '2018-06-20T14:47:56Z',
            version: 3,
            group: '/home/22',
            status: {
               id: 'f3dcbf49-18d2-4b5d-a52c-d3ca60efc8a6',
               status: 'Failed',
               statusId: 'bd726bbf-d6b1-4629-9d8d-95a1f84110ac',
               statusInfo: 'An error was encountered while launching the Workflow App in the Marathon API',
               lastUpdateDate: '2018-06-22T07:03:02Z'
            },
            execution: {
               id: 'f3dcbf49-18d2-4b5d-a52c-d3ca60efc8a6',
               marathonExecution: {
                  marathonId: 'sparta/undefined/workflows/home/22/name111/name111-v3'
               },
               genericDataExecution: {
                  executionMode: 'marathon',
                  executionId: '487062fd-1bd8-421f-93aa-e1d7faaca3b7',
                  lastError: {
                     message: 'An error was encountered while launching the Workflow App in the Marathon API',
                     phase: 'Launch',
                     exceptionMsg: 'java.io.FileNotFoundException: /etc/sds/sparta/marathon-app-template.json (No such file or directory)',
                     localizedMsg: '/etc/sds/sparta/marathon-app-template.json (No such file or directory)',
                     date: '2018-06-22T07:03:02Z'
                  }
               }
            },
            filterStatus: 'Failed',
            tagsAux: '',
            startDate: '',
            endDate: '',
            lastErrorDate: '22 Jun 2018 - 9:03',
            lastUpdate: '22 Jun 2018 - 9:03',
            lastUpdateOrder: 1529650982000,
            launchDate: ''
         },
            {
               id: '37c9264d-66cf-4e9e-aee1-8b98b3139c30',
               name: 'generali-stat-corporativas-batch',
               description: 'Ensurance example for ingestion and data cleaning',
               settings: {
                  executionMode: 'marathon',
                  userPluginsJars: [],
                  initSqlSentences: [
                     {
                        sentence: 'CREATE TABLE IF NOT EXISTS sie_stat_corporativas USING com.databricks.spark.csv OPTIONS (path \'hdfs://10.200.0.74:8020/user/dg-agent/csv/generali/sie_stat_corporativas.csv\', sep \'\\t\', header \'true\',  inferSchema \'true\')'
                     },
                     {
                        sentence: 'CREATE TABLE IF NOT EXISTS dim_comercial USING com.databricks.spark.csv OPTIONS(path \'hdfs://10.200.0.74:8020/user/dg-agent/csv/generali/sie_estructura_comercial.csv\', header \'true\', inferSchema \'true\', delimiter \'\\t\')'
                     },
                     {
                        sentence: 'CREATE TABLE IF NOT EXISTS sie_rem_preguntas USING com.databricks.spark.csv OPTIONS(path \'hdfs://10.200.0.74:8020/user/dg-agent/csv/generali/sie_rem_preguntas.csv\', header \'true\', inferSchema \'true\', delimiter \'\\t\')'
                     }
                  ],
                  addAllUploadedPlugins: true,
                  mesosConstraint: '',
                  mesosConstraintOperator: 'CLUSTER'
               },
               nodes: [
                  {
                     name: 'Crossdata',
                     stepType: 'Input'
                  },
                  {
                     name: 'Casting',
                     stepType: 'Transformation'
                  },
                  {
                     name: 'variables_calc',
                     stepType: 'Transformation'
                  },
                  {
                     name: 'Select_Indi',
                     stepType: 'Transformation'
                  },
                  {
                     name: '2016_cluster1',
                     stepType: 'Transformation'
                  },
                  {
                     name: '2016_cluster3',
                     stepType: 'Transformation'
                  },
                  {
                     name: '2016_cluster2',
                     stepType: 'Transformation'
                  },
                  {
                     name: '2015_cluster1',
                     stepType: 'Transformation'
                  },
                  {
                     name: '2015_cluster2',
                     stepType: 'Transformation'
                  },
                  {
                     name: '2015_cluster3',
                     stepType: 'Transformation'
                  },
                  {
                     name: 'resto_cluster1',
                     stepType: 'Transformation'
                  },
                  {
                     name: 'resto_cluster3',
                     stepType: 'Transformation'
                  },
                  {
                     name: 'resto_cluster2',
                     stepType: 'Transformation'
                  },
                  {
                     name: 'Mix_dim_com',
                     stepType: 'Transformation'
                  },
                  {
                     name: 'Postgres(1)',
                     stepType: 'Output'
                  },
                  {
                     name: 'Postgres(1)(1)',
                     stepType: 'Output'
                  },
                  {
                     name: 'Repartition',
                     stepType: 'Transformation'
                  }
               ],
               executionEngine: 'Batch',
               lastUpdateDate: '2018-04-30T09:35:55Z',
               version: 0,
               group: '/home',
               tags: [
                  'etl',
                  'data cleaning',
                  'ensurance',
                  'generali'
               ],
               status: {
                  id: '37c9264d-66cf-4e9e-aee1-8b98b3139c30',
                  status: 'Failed',
                  statusId: 'b9cd1932-2f71-4346-8757-5f406fd51f57',
                  statusInfo: 'Error launching workflow with the selected execution mode',
                  lastUpdateDate: '2018-06-05T08:35:50Z'
               },
               filterStatus: 'Failed',
               tagsAux: 'etl, data cleaning, ensurance, generali',
               lastUpdate: '5 Jun 2018 - 10:35',
               lastUpdateOrder: 1528187750000
            },],
            selectedWorkflows: [],
            selectedWorkflowsIds: [],
            executionInfo: null,
            loading: false
      },
      filters: {
         currentFilterStatus: '',
            searchQuery: '',
            paginationOptions: {
            currentPage: 1,
               perPage: 20
         },
         workflowOrder: {
            orderBy: '',
               type: 1
         }
      }
   },
   crossdata: {
      crossdata: {
         databases: [
            {
               name: 'default',
               description: 'default database',
               locationUri: 'file:/D:/projects/sparta-workflow/serving-api/spark-warehouse'
            }
         ],
            tableList: [],
            selectedTables: [],
            queryResult: null,
            selectedDatabase: 'default',
            showTemporaryTables: false,
            searchTables: '',
            queryError: '',
            tablesSortOrder: true,
            tablesOrderBy: 'name',
            loadingDatabases: false,
            loadingTables: false,
            loadingQuery: false,
            openedTables: []
      }
   },
   wizard: {
      wizard: {
         editionMode: false,
            workflowId: 'fcf18f80-5abc-4e7f-95b8-ecb85b5132b0',
            workflowGroup: {
            id: '940800b2-6d81-44a8-84d9-26913a2faea4',
               name: '/home'
         },
         workflowVersion: 3,
            loading: false,
            serverStepValidations: {
            Postgres: [
               'when TLS is enabled, the security options inside sparkConf must be filled'
            ],
               Postgres_1: [
               'when TLS is enabled, the security options inside sparkConf must be filled'
            ]
         },
         settings: {
            basic: {
               name: 'batch-carrefour-workflow',
                  description: ''
            },
            advancedSettings: {
               global: {
                  executionMode: 'marathon',
                     userPluginsJars: [],
                     initSqlSentences: [
                     {
                        sentence: 'CREATE TABLE IF NOT EXISTS tablaVolcado USING com.databricks.spark.csv OPTIONS (path \'/user/dg-agent/csv/batch-carrefour/resultsduplicates3.csv\', header \'true\', inferSchema \'true\', delimiter \'|\')'
                     }
                  ],
                     addAllUploadedPlugins: true,
                     mesosConstraint: '',
                     mesosConstraintOperator: 'CLUSTER'
               },
               streamingSettings: {
                  window: '2s',
                     blockInterval: '100ms',
                     checkpointSettings: {
                     checkpointPath: 'sparta/checkpoint',
                        enableCheckpointing: true,
                        autoDeleteCheckpoint: true,
                        addTimeToCheckpointPath: false
                  }
               },
               sparkSettings: {
                  master: 'mesos://leader.mesos:5050',
                     sparkKerberos: true,
                     sparkDataStoreTls: true,
                     sparkMesosSecurity: true,
                     submitArguments: {
                     userArguments: [],
                        deployMode: 'client',
                        driverJavaOptions: '{{{SPARK_DRIVER_JAVA_OPTIONS}}}'
                  },
                  sparkConf: {
                     sparkResourcesConf: {
                        coresMax: '{{{SPARK_CORES_MAX}}}',
                           executorMemory: '6G',
                           executorCores: '{{{SPARK_EXECUTOR_CORES}}}',
                           driverCores: '{{{SPARK_DRIVER_CORES}}}',
                           driverMemory: '4G',
                           mesosExtraCores: '',
                           localityWait: '{{{SPARK_LOCALITY_WAIT}}}',
                           taskMaxFailures: '{{{SPARK_TASK_MAX_FAILURES}}}',
                           sparkMemoryFraction: '{{{SPARK_MEMORY_FRACTION}}}',
                           sparkParallelism: ''
                     },
                     userSparkConf: [],
                        coarse: true,
                        sparkUser: '',
                        sparkLocalDir: '{{{SPARK_LOCAL_PATH}}}',
                        executorDockerImage: '{{{SPARK_EXECUTOR_BASE_IMAGE}}}',
                        sparkKryoSerialization: false,
                        sparkSqlCaseSensitive: true,
                        logStagesProgress: false,
                        hdfsTokenCache: true,
                        executorExtraJavaOptions: '{{{SPARK_EXECUTOR_EXTRA_JAVA_OPTIONS}}}'
                  }
               },
               errorsManagement: {
                  genericErrorManagement: {
                     whenError: 'Error'
                  },
                  transformationStepsManagement: {
                     whenError: 'Error',
                        whenRowError: 'RowError',
                        whenFieldError: 'FieldError'
                  },
                  transactionsManagement: {
                     sendToOutputs: [],
                        sendStepData: false,
                        sendPredecessorsData: true,
                        sendInputData: true
                  }
               }
            }
         },
         svgPosition: {
            x: 77.66281222065174,
               y: 192.39278098878714,
               k: 0.559527909353835
         },
         edges: [
            {
               origin: 'FileSystem',
               destination: 'Csv',
               dataType: 'ValidData'
            },
            {
               origin: 'Csv',
               destination: 'Datetime',
               dataType: 'ValidData'
            },
            {
               origin: 'Datetime',
               destination: 'Casting',
               dataType: 'ValidData'
            },
            {
               origin: 'Casting',
               destination: 'fallo_casting',
               dataType: 'ValidData'
            },
            {
               origin: 'Casting',
               destination: 'no_duplicados',
               dataType: 'ValidData'
            },
            {
               origin: 'Datetime',
               destination: 'fallo_refdate',
               dataType: 'ValidData'
            },
            {
               origin: 'Csv',
               destination: 'fallo_refdate',
               dataType: 'ValidData'
            },
            {
               origin: 'Datetime',
               destination: 'fallo_casting',
               dataType: 'ValidData'
            },
            {
               origin: 'Casting',
               destination: 'fallo_duplicados',
               dataType: 'ValidData'
            },
            {
               origin: 'no_duplicados',
               destination: 'rango_msgflags',
               dataType: 'ValidData'
            },
            {
               origin: 'rango_msgflags',
               destination: 'fallo_rango',
               dataType: 'ValidData'
            },
            {
               origin: 'no_duplicados',
               destination: 'fallo_rango',
               dataType: 'ValidData'
            },
            {
               origin: 'rango_msgflags',
               destination: 'new_field_id_emprs',
               dataType: 'ValidData'
            },
            {
               origin: 'new_field_id_emprs',
               destination: 'fallo_id_emprs',
               dataType: 'ValidData'
            },
            {
               origin: 'rango_msgflags',
               destination: 'fallo_id_emprs',
               dataType: 'ValidData'
            },
            {
               origin: 'new_field_id_emprs',
               destination: 'tm_trusted_field',
               dataType: 'ValidData'
            },
            {
               origin: 'tm_trusted_field',
               destination: 'error_tm_trusted',
               dataType: 'ValidData'
            },
            {
               origin: 'new_field_id_emprs',
               destination: 'error_tm_trusted',
               dataType: 'ValidData'
            },
            {
               origin: 'dt_trusted_field',
               destination: 'fallo_dt_trusted',
               dataType: 'ValidData'
            },
            {
               origin: 'tm_trusted_field',
               destination: 'fallo_dt_trusted',
               dataType: 'ValidData'
            },
            {
               origin: 'tm_trusted_field',
               destination: 'dt_trusted_field',
               dataType: 'ValidData'
            },
            {
               origin: 'dt_trusted_field',
               destination: 'Parquet',
               dataType: 'ValidData'
            },
            {
               origin: 'dt_trusted_field',
               destination: 'Postgres',
               dataType: 'ValidData'
            },
            {
               origin: 'fallo_refdate',
               destination: 'Postgres_1',
               dataType: 'ValidData'
            },
            {
               origin: 'fallo_casting',
               destination: 'Postgres_1',
               dataType: 'ValidData'
            },
            {
               origin: 'fallo_duplicados',
               destination: 'Postgres_1',
               dataType: 'ValidData'
            },
            {
               origin: 'fallo_rango',
               destination: 'Postgres_1',
               dataType: 'ValidData'
            },
            {
               origin: 'fallo_id_emprs',
               destination: 'Postgres_1',
               dataType: 'ValidData'
            },
            {
               origin: 'error_tm_trusted',
               destination: 'Postgres_1',
               dataType: 'ValidData'
            },
            {
               origin: 'fallo_dt_trusted',
               destination: 'Postgres_1',
               dataType: 'ValidData'
            }
         ],
            redoStates: [],
            undoStates: [],
            pristineWorkflow: true,
            nodes: [
            {
               name: 'FileSystem',
               stepType: 'Input',
               className: 'FileSystemInputStep',
               classPrettyName: 'FileSystem',
               arity: [
                  'NullaryToNary'
               ],
               writer: {
                  saveMode: 'Append',
                  tableName: '{{CASSANDRA_HOST}}',
                  partitionBy: '',
                  errorTableName: ''
               },
               uiConfiguration: {
                  position: {
                     x: 330.60650634765625,
                     y: 61.393455505371094
                  }
               },
               configuration: {
                  path: '{{ES_PORT}}',
                  outputField: 'rawData'
               },
               supportedEngines: [
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData'
               ],
               lineageProperties: []
            },
            {
               name: 'Csv',
               stepType: 'Transformation',
               className: 'CsvTransformStep',
               classPrettyName: 'Csv',
               arity: [
                  'UnaryToNary'
               ],
               writer: {
                  saveMode: 'Append',
                  tableName: '',
                  partitionBy: '',
                  errorTableName: ''
               },
               uiConfiguration: {
                  position: {
                     x: 549,
                     y: 59.21302795410156
                  }
               },
               configuration: {
                  'schema.inputMode': 'HEADER',
                  'schema.header': 'TYPE|SERNO|LTIMESTAMP|PROCESSTIMEMS|GROUPFROMSERIAL|GROUPTOSERIAL|EXTSERNO|CARDSERNO|TRUECARDNO|CARDCURRENCY|EODSERNO|ORIGSERNO|ISOLOGSERNO|SOURCE|FORMAT|REASONCODE|OTB_AMT_CENTER|OTB_AMT_CARD|AMT_CENTER|AMT_CARD|ORIG_AMT_CENTER|ORIG_AMT_CARD|FEE_AMT_CARD|FEE_AMT_CENTER|SAFFLAG|DEBITPORT|SAFTIMESTAMP|SAFRETRYCOUNT|BATCHNO|MSGFLAGS|ADDITIONAFLAGS|AFFECTEDOTBS|EXTERNALLOGSERNO|INSTITUIONID|TRXNSERNO|ISSUER_FEESERNO|AUTH_FEESERNO|MATCHTIME|DROPDATE|ACTION_RESCODE|WORKERID|MSGTRXNTYPE|I000_MSG_TYPE|I002_NUMBER|I003_PROC_CODE|I004_AMT_TRXN|I005_AMT_SETTLE|I006_AMT_BILL|I007_TRANS_DT|I009_SETL_CONV|I011_TRACE_NUM|I012_LCL_TIME|I013_LCL_DT|I014_EXP_DATE|I016_CONV_DT|I018_MERCH_TYPE|I019_ACQ_COUNTRY|I021_FWD_CNTRY|I022_POS_ENTRY|I023_CARD_SEQ_NUM|I024_INT_NETWK_ID|I025_POS_COND|I026_ACCPTR_BUSCD|I028_TRANSFER_FEE|I029_SETL_FEE|I030_TRSF_PRC_FEE|I031_SETL_PROC_FEE|I032_ACQUIRER_ID|I033_FORWARDER_ID|I035_TRK_2|I037_RET_REF_NUM|I038_AUTH_ID|I039_RSP_CD|I041_POS_ID|I042_MERCH_ID|I043A_MERCH_NAME|I043B_MERCH_CITY|I043C_MERCH_CNT|I044_ADDTNL_RSP|I045_TRK_1|I047_NAT_ADDTNL_DT|I048_TEXT_DATA|I049_CUR_TRXN|I050_CUR_SETTLE|I051_CUR_BILL|I053_SEC_CNTRL|I054_ADDTNL_AMT|I055_ICC_DATA|I056_ORG_DATA|I058_AUTH_AGENT|I059_POS_GEO_DATA|I060_POS_CAP|I061V1_OTH_AMT_TXN|I061V2_OTH_AMT_BILL|I061V3_REP_AMT_BILL|I061M_POS_DATA|I062V1_AUTH_CHAR|I062V2_TRANS_ID|I062V3_VALIDAT_CD|I062V4_MRKT_DATA|I062V5_DURANTION|I062V6_PRSTG_PROP|I063_BANKNET_DATA|I063V1_NETWORK_ID|I063V2_TIM3_LIMIT|I063V3_MIS_CAS_RD|I063V4_STIP_RS|I063V5_PMC_ID|I068_RCV_CNTRY|I072_MSG_NUM_LST|I090_ORIG_DATA|I093_RSP_IND|I094_SVC_IND|I095_ACT_TRAN_AMT|I100_RCV_INST|I101_FILE_NAME|I102_ACCT_ID1|I103_ACCT_ID2|I104_TRAN_DESC|I118_INTRA_COUNTRY|I120_ORIG_MSG_TYP|I123_ADR_VER|I125_SUPP_INFO|I126V6_CH_CSERIAL|I126V7_ME_CSERIAL|I126V8_TRXN_ID|I126V9_STAIN|I126V10_CVV2|I130_TERM_CAPA|I131_TVR|I132_UNPREDICT|I133_TERM_SERIAL|I134_VISA_DISCR|I135A_PREAUTHTRACE|I135B_PURCHTRACE|I135C_CRYPTTRACE|I135D_KEYVERSION|I136_CRYPTOGRAM|I137_ATC|I138_APL|I139_1_ARPC|I139_2_ARPC_RESCD|I142_SCRIPTSEND|I143_SCRIPTS_RES|I144_CRYP_TR|I145_TRM_CNTRY|I146_TRM_DATE|I147_CRYPT_AMT|I148_CRYPT_CURR|I149_CRYPT_CSBK|TRXNSERNO_B|MATCHTIME_B|GROUP0SERIAL|GROUP0CURRENCY|GROUP0OTBAMOUNT|GROUP0AMOUNT|GROUP0ORIGAMOUNT|GROUP0FEEAMOUNT|GROUP1SERIAL|GROUP1CURRENCY|GROUP1OTBAMOUNT|GROUP1AMOUNT|GROUP1ORIGAMOUNT|GROUP1FEEAMOUNT|GROUP2SERIAL|GROUP2CURRENCY|GROUP2OTBAMOUNT|GROUP2AMOUNT|GROUP2ORIGAMOUNT|GROUP2FEEAMOUNT|GROUP3SERIAL|GROUP3CURRENCY|GROUP3OTBAMOUNT|GROUP3AMOUNT|GROUP3ORIGAMOUNT|GROUP3FEEAMOUNT|GROUP4SERIAL|GROUP4CURRENCY|GROUP4OTBAMOUNT|GROUP4AMOUNT|GROUP4ORIGAMOUNT|GROUP4FEEAMOUNT|GROUP5SERIAL|GROUP5CURRENCY|GROUP5OTBAMOUNT|GROUP5AMOUNT|GROUP5ORIGAMOUNT|GROUP5FEEAMOUNT|GROUP6SERIAL|GROUP6CURRENCY|GROUP6OTBAMOUNT|GROUP6AMOUNT|GROUP6ORIGAMOUNT|GROUP6FEEAMOUNT|GROUP7SERIAL|GROUP7CURRENCY|GROUP7OTBAMOUNT|GROUP7AMOUNT|GROUP7ORIGAMOUNT|GROUP7FEEAMOUNT|GROUP8SERIAL|GROUP8CURRENCY|GROUP8OTBAMOUNT|GROUP8AMOUNT|GROUP8ORIGAMOUNT|GROUP8FEEAMOUNT|GROUP9SERIAL|GROUP9CURRENCY|GROUP9OTBAMOUNT|GROUP9AMOUNT|GROUP9ORIGAMOUNT|GROUP9FEEAMOUNT|INSTALLMENT|MONTHLYAMOUNT|FULLAMOUNT|INSTALLTYPE|FILLER|INTERESTRATE|OFFSET|OFFSET_TYPE|ADJUSTEDAMOUNT|APR|FEES|TAX|PLANSERNO|PRIMECARDSERNO|I062V21_RISK_SCORE_RS|I062V22_RISK_COND_CD|CARDPRODUCTSERNO|FGPROCESSTIMEMS|SMPROCESSTIMEMES|GPCREDITAFFECTOTB|I124_GOODS_CODE|I127_FILL_RCD|PANTYPE|TOKENSERNO|OTP|V1|V2|REFDATE|TEST_FIELD|DT_LANDING',
                  delimiterType: 'CHARACTER',
                  splitLimit: '-1',
                  whenFieldError: 'Null',
                  whenRowError: 'RowDiscard',
                  fieldsPreservationPolicy: 'APPEND',
                  inputField: 'rawData',
                  delimiter: '|'
               },
               supportedEngines: [
                  'Streaming',
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData',
                  'DiscardedData'
               ],
               lineageProperties: []
            },
            {
               name: 'Casting',
               stepType: 'Transformation',
               className: 'CastingTransformStep',
               classPrettyName: 'Casting',
               arity: [
                  'UnaryToNary'
               ],
               writer: {
                  saveMode: 'Append',
                  tableName: '',
                  partitionBy: '',
                  errorTableName: ''
               },
               uiConfiguration: {
                  position: {
                     x: 1087.1229248046875,
                     y: 56.819549560546875
                  }
               },
               configuration: {
                  outputFieldsFrom: 'STRING',
                  fieldsString: 'StructType((\nStructField(TYPE,StringType,true),\nStructField(SERNO,LongType,true),\nStructField(LTIMESTAMP,StringType,true),\nStructField(PROCESSTIMEMS,LongType,true),\nStructField(GROUPFROMSERIAL,LongType,true),\nStructField(GROUPTOSERIAL,LongType,true),\nStructField(EXTSERNO,LongType,true),\nStructField(CARDSERNO,LongType,true),\nStructField(TRUECARDNO,StringType,true),\nStructField(CARDCURRENCY,StringType,true),\nStructField(EODSERNO,LongType,true),\nStructField(ORIGSERNO,LongType,true),\nStructField(ISOLOGSERNO,LongType,true),\nStructField(SOURCE,StringType,true),\nStructField(FORMAT,StringType,true),\nStructField(REASONCODE,IntegerType,true),\nStructField(OTB_AMT_CENTER,DoubleType,true),\nStructField(OTB_AMT_CARD,DoubleType,true),\nStructField(AMT_CENTER,DoubleType,true),\nStructField(AMT_CARD,DoubleType,true),\nStructField(ORIG_AMT_CENTER,DoubleType,true),\nStructField(ORIG_AMT_CARD,DoubleType,true),\nStructField(FEE_AMT_CARD,DoubleType,true),\nStructField(FEE_AMT_CENTER,DoubleType,true),\nStructField(SAFFLAG,StringType,true),\nStructField(DEBITPORT,IntegerType,true),\nStructField(SAFTIMESTAMP,StringType,true),\nStructField(SAFRETRYCOUNT,LongType,true),\nStructField(BATCHNO,LongType,true),\nStructField(MSGFLAGS,LongType,true),\nStructField(ADDITIONAFLAGS,StringType,true),\nStructField(AFFECTEDOTBS,LongType,true),\nStructField(EXTERNALLOGSERNO,LongType,true),\nStructField(INSTITUIONID,IntegerType,true),\nStructField(TRXNSERNO,LongType,true),\nStructField(ISSUER_FEESERNO,LongType,true),\nStructField(AUTH_FEESERNO,LongType,true),\nStructField(MATCHTIME,StringType,true),\nStructField(DROPDATE,StringType,true),\nStructField(ACTION_RESCODE,StringType,true),\nStructField(WORKERID,IntegerType,true),\nStructField(MSGTRXNTYPE,IntegerType,true),\nStructField(I000_MSG_TYPE,StringType,true),\nStructField(I002_NUMBER,StringType,true),\nStructField(I003_PROC_CODE,StringType,true),\nStructField(I004_AMT_TRXN,DoubleType,true),\nStructField(I005_AMT_SETTLE,DoubleType,true),\nStructField(I006_AMT_BILL,DoubleType,true),\nStructField(I007_TRANS_DT,StringType,true),\nStructField(I009_SETL_CONV,StringType,true),\nStructField(I011_TRACE_NUM,StringType,true),\nStructField(I012_LCL_TIME,StringType,true),\nStructField(I013_LCL_DT,StringType,true),\nStructField(I014_EXP_DATE,StringType,true),\nStructField(I016_CONV_DT,StringType,true),\nStructField(I018_MERCH_TYPE,StringType,true),\nStructField(I019_ACQ_COUNTRY,StringType,true),\nStructField(I021_FWD_CNTRY,StringType,true),\nStructField(I022_POS_ENTRY,StringType,true),\nStructField(I023_CARD_SEQ_NUM,StringType,true),\nStructField(I024_INT_NETWK_ID,StringType,true),\nStructField(I025_POS_COND,StringType,true),\nStructField(I026_ACCPTR_BUSCD,StringType,true),\nStructField(I028_TRANSFER_FEE,StringType,true),\nStructField(I029_SETL_FEE,StringType,true),\nStructField(I030_TRSF_PRC_FEE,StringType,true),\nStructField(I031_SETL_PROC_FEE,StringType,true),\nStructField(I032_ACQUIRER_ID,StringType,true),\nStructField(I033_FORWARDER_ID,StringType,true),\nStructField(I035_TRK_2,StringType,true),\nStructField(I037_RET_REF_NUM,StringType,true),\nStructField(I038_AUTH_ID,StringType,true),\nStructField(I039_RSP_CD,StringType,true),\nStructField(I041_POS_ID,StringType,true),\nStructField(I042_MERCH_ID,StringType,true),\nStructField(I043A_MERCH_NAME,StringType,true),\nStructField(I043B_MERCH_CITY,StringType,true),\nStructField(I043C_MERCH_CNT,StringType,true),\nStructField(I044_ADDTNL_RSP,StringType,true),\nStructField(I045_TRK_1,StringType,true),\nStructField(I047_NAT_ADDTNL_DT,StringType,true),\nStructField(I048_TEXT_DATA,StringType,true),\nStructField(I049_CUR_TRXN,StringType,true),\nStructField(I050_CUR_SETTLE,StringType,true),\nStructField(I051_CUR_BILL,StringType,true),\nStructField(I053_SEC_CNTRL,StringType,true),\nStructField(I054_ADDTNL_AMT,StringType,true),\nStructField(I055_ICC_DATA,StringType,true),\nStructField(I056_ORG_DATA,StringType,true),\nStructField(I058_AUTH_AGENT,StringType,true),\nStructField(I059_POS_GEO_DATA,StringType,true),\nStructField(I060_POS_CAP,StringType,true),\nStructField(I061V1_OTH_AMT_TXN,DoubleType,true),\nStructField(I061V2_OTH_AMT_BILL,DoubleType,true),\nStructField(I061V3_REP_AMT_BILL,DoubleType,true),\nStructField(I061M_POS_DATA,StringType,true),\nStructField(I062V1_AUTH_CHAR,StringType,true),\nStructField(I062V2_TRANS_ID,StringType,true),\nStructField(I062V3_VALIDAT_CD,StringType,true),\nStructField(I062V4_MRKT_DATA,StringType,true),\nStructField(I062V5_DURANTION,StringType,true),\nStructField(I062V6_PRSTG_PROP,StringType,true),\nStructField(I063_BANKNET_DATA,StringType,true),\nStructField(I063V1_NETWORK_ID,StringType,true),\nStructField(I063V2_TIM3_LIMIT,StringType,true),\nStructField(I063V3_MIS_CAS_RD,StringType,true),\nStructField(I063V4_STIP_RS,StringType,true),\nStructField(I063V5_PMC_ID,StringType,true),\nStructField(I068_RCV_CNTRY,StringType,true),\nStructField(I072_MSG_NUM_LST,StringType,true),\nStructField(I090_ORIG_DATA,StringType,true),\nStructField(I093_RSP_IND,StringType,true),\nStructField(I094_SVC_IND,StringType,true),\nStructField(I095_ACT_TRAN_AMT,DoubleType,true),\nStructField(I100_RCV_INST,StringType,true),\nStructField(I101_FILE_NAME,StringType,true),\nStructField(I102_ACCT_ID1,StringType,true),\nStructField(I103_ACCT_ID2,StringType,true),\nStructField(I104_TRAN_DESC,StringType,true),\nStructField(I118_INTRA_COUNTRY,StringType,true),\nStructField(I120_ORIG_MSG_TYP,StringType,true),\nStructField(I123_ADR_VER,StringType,true),\nStructField(I125_SUPP_INFO,StringType,true),\nStructField(I126V6_CH_CSERIAL,StringType,true),\nStructField(I126V7_ME_CSERIAL,StringType,true),\nStructField(I126V8_TRXN_ID,StringType,true),\nStructField(I126V9_STAIN,StringType,true),\nStructField(I126V10_CVV2,StringType,true),\nStructField(I130_TERM_CAPA,StringType,true),\nStructField(I131_TVR,StringType,true),\nStructField(I132_UNPREDICT,StringType,true),\nStructField(I133_TERM_SERIAL,StringType,true),\nStructField(I134_VISA_DISCR,StringType,true),\nStructField(I135A_PREAUTHTRACE,IntegerType,true),\nStructField(I135B_PURCHTRACE,IntegerType,true),\nStructField(I135C_CRYPTTRACE,IntegerType,true),\nStructField(I135D_KEYVERSION,IntegerType,true),\nStructField(I136_CRYPTOGRAM,StringType,true),\nStructField(I137_ATC,StringType,true),\nStructField(I138_APL,StringType,true),\nStructField(I139_1_ARPC,StringType,true),\nStructField(I139_2_ARPC_RESCD,StringType,true),\nStructField(I142_SCRIPTSEND,LongType,true),\nStructField(I143_SCRIPTS_RES,StringType,true),\nStructField(I144_CRYP_TR,StringType,true),\nStructField(I145_TRM_CNTRY,StringType,true),\nStructField(I146_TRM_DATE,StringType,true),\nStructField(I147_CRYPT_AMT,StringType,true),\nStructField(I148_CRYPT_CURR,StringType,true),\nStructField(I149_CRYPT_CSBK,StringType,true),\nStructField(TRXNSERNO_B,LongType,true),\nStructField(MATCHTIME_B,StringType,true),\nStructField(GROUP0SERIAL,LongType,true),\nStructField(GROUP0CURRENCY,StringType,true),\nStructField(GROUP0OTBAMOUNT,DoubleType,true),\nStructField(GROUP0AMOUNT,DoubleType,true),\nStructField(GROUP0ORIGAMOUNT,DoubleType,true),\nStructField(GROUP0FEEAMOUNT,DoubleType,true),\nStructField(GROUP1SERIAL,LongType,true),\nStructField(GROUP1CURRENCY,StringType,true),\nStructField(GROUP1OTBAMOUNT,DoubleType,true),\nStructField(GROUP1AMOUNT,DoubleType,true),\nStructField(GROUP1ORIGAMOUNT,DoubleType,true),\nStructField(GROUP1FEEAMOUNT,DoubleType,true),\nStructField(GROUP2SERIAL,LongType,true),\nStructField(GROUP2CURRENCY,StringType,true),\nStructField(GROUP2OTBAMOUNT,DoubleType,true),\nStructField(GROUP2AMOUNT,DoubleType,true),\nStructField(GROUP2ORIGAMOUNT,DoubleType,true),\nStructField(GROUP2FEEAMOUNT,DoubleType,true),\nStructField(GROUP3SERIAL,LongType,true),\nStructField(GROUP3CURRENCY,StringType,true),\nStructField(GROUP3OTBAMOUNT,DoubleType,true),\nStructField(GROUP3AMOUNT,DoubleType,true),\nStructField(GROUP3ORIGAMOUNT,DoubleType,true),\nStructField(GROUP3FEEAMOUNT,DoubleType,true),\nStructField(GROUP4SERIAL,LongType,true),\nStructField(GROUP4CURRENCY,StringType,true),\nStructField(GROUP4OTBAMOUNT,DoubleType,true),\nStructField(GROUP4AMOUNT,DoubleType,true),\nStructField(GROUP4ORIGAMOUNT,DoubleType,true),\nStructField(GROUP4FEEAMOUNT,DoubleType,true),\nStructField(GROUP5SERIAL,LongType,true),\nStructField(GROUP5CURRENCY,StringType,true),\nStructField(GROUP5OTBAMOUNT,DoubleType,true),\nStructField(GROUP5AMOUNT,DoubleType,true),\nStructField(GROUP5ORIGAMOUNT,DoubleType,true),\nStructField(GROUP5FEEAMOUNT,DoubleType,true),\nStructField(GROUP6SERIAL,LongType,true),\nStructField(GROUP6CURRENCY,StringType,true),\nStructField(GROUP6OTBAMOUNT,DoubleType,true),\nStructField(GROUP6AMOUNT,DoubleType,true),\nStructField(GROUP6ORIGAMOUNT,DoubleType,true),\nStructField(GROUP6FEEAMOUNT,DoubleType,true),\nStructField(GROUP7SERIAL,LongType,true),\nStructField(GROUP7CURRENCY,StringType,true),\nStructField(GROUP7OTBAMOUNT,DoubleType,true),\nStructField(GROUP7AMOUNT,DoubleType,true),\nStructField(GROUP7ORIGAMOUNT,DoubleType,true),\nStructField(GROUP7FEEAMOUNT,DoubleType,true),\nStructField(GROUP8SERIAL,LongType,true),\nStructField(GROUP8CURRENCY,StringType,true),\nStructField(GROUP8OTBAMOUNT,DoubleType,true),\nStructField(GROUP8AMOUNT,DoubleType,true),\nStructField(GROUP8ORIGAMOUNT,DoubleType,true),\nStructField(GROUP8FEEAMOUNT,DoubleType,true),\nStructField(GROUP9SERIAL,LongType,true),\nStructField(GROUP9CURRENCY,StringType,true),\nStructField(GROUP9OTBAMOUNT,DoubleType,true),\nStructField(GROUP9AMOUNT,DoubleType,true),\nStructField(GROUP9ORIGAMOUNT,DoubleType,true),\nStructField(GROUP9FEEAMOUNT,DoubleType,true),\nStructField(INSTALLMENT,IntegerType,true),\nStructField(MONTHLYAMOUNT,DoubleType,true),\nStructField(FULLAMOUNT,DoubleType,true),\nStructField(INSTALLTYPE,StringType,true),\nStructField(FILLER,StringType,true),\nStructField(INTERESTRATE,DoubleType,true),\nStructField(OFFSET,IntegerType,true),\nStructField(OFFSET_TYPE,StringType,true),\nStructField(ADJUSTEDAMOUNT,DoubleType,true),\nStructField(APR,DoubleType,true),\nStructField(FEES,DoubleType,true),\nStructField(TAX,DoubleType,true),\nStructField(PLANSERNO,StringType,true),\nStructField(PRIMECARDSERNO,LongType,true),\nStructField(I062V21_RISK_SCORE_RS,StringType,true),\nStructField(I062V22_RISK_COND_CD,StringType,true),\nStructField(CARDPRODUCTSERNO,LongType,true),\nStructField(FGPROCESSTIMEMS,LongType,true),\nStructField(SMPROCESSTIMEMES,DoubleType,true),\nStructField(GPCREDITAFFECTOTB,StringType,true),\nStructField(I124_GOODS_CODE,StringType,true),\nStructField(I127_FILL_RCD,StringType,true),\nStructField(PANTYPE,IntegerType,true),\nStructField(TOKENSERNO,LongType,true),\nStructField(OTP,StringType,true),\nStructField(V1,StringType,true),\nStructField(V2,StringType,true),\nStructField(REFDATE,TimestampType,true),\nStructField(TEST_FIELD,TimestampType,true),\nStructField(DT_LANDING,IntegerType,true)))',
                  whenRowError: '',
                  whenFieldError: ''
               },
               supportedEngines: [
                  'Streaming',
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData',
                  'DiscardedData'
               ],
               lineageProperties: []
            },
            {
               name: 'no_duplicados',
               stepType: 'Transformation',
               className: 'TriggerTransformStep',
               classPrettyName: 'Trigger',
               arity: [
                  'BinaryToNary',
                  'UnaryToNary',
                  'NaryToNary'
               ],
               writer: {
                  saveMode: 'Append',
                  tableName: '',
                  partitionBy: '',
                  errorTableName: ''
               },
               uiConfiguration: {
                  position: {
                     x: 1353.8453369140625,
                     y: 57.27845001220703
                  }
               },
               configuration: {
                  sql: 'select c.* from Casting c left join tablaVolcado d on c.SERNO = d.SERNO  where d.SERNO IS NULL',
                  inputSchemas: '[]',
                  executeSqlWhenEmpty: false
               },
               supportedEngines: [
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData',
                  'DiscardedData'
               ],
               lineageProperties: []
            },
            {
               name: 'Datetime',
               stepType: 'Transformation',
               className: 'DateTimeTransformStep',
               classPrettyName: 'Datetime',
               arity: [
                  'UnaryToNary'
               ],
               writer: {
                  saveMode: 'Append',
                  tableName: '',
                  partitionBy: '',
                  errorTableName: ''
               },
               uiConfiguration: {
                  position: {
                     x: 766.761962890625,
                     y: 57.60649871826172
                  }
               },
               configuration: {
                  fieldsDatetime: [
                     {
                        formatFrom: 'STANDARD',
                        inputField: 'REFDATE',
                        standardFormat: 'basicDate',
                        localeTime: 'ENGLISH',
                        granularityNumber: '',
                        granularityTime: 'millisecond',
                        fieldsPreservationPolicy: 'REPLACE',
                        outputFieldName: 'REFDATE',
                        outputFieldType: 'timestamp',
                        nullable: true,
                        outputFormatFrom: 'DEFAULT'
                     }
                  ],
                  whenRowError: '',
                  whenFieldError: ''
               },
               supportedEngines: [
                  'Streaming',
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData',
                  'DiscardedData'
               ],
               lineageProperties: []
            },
            {
               name: 'Parquet',
               stepType: 'Output',
               className: 'ParquetOutputStep',
               classPrettyName: 'Parquet',
               arity: [
                  'NullaryToNullary',
                  'NaryToNullary'
               ],
               writer: {
                  saveMode: 'Append'
               },
               uiConfiguration: {
                  position: {
                     x: 2626.065185546875,
                     y: 58.40686798095703
                  }
               },
               configuration: {
                  path: '/tmp',
                  errorSink: false,
                  saveOptions: '[]'
               },
               supportedEngines: [
                  'Streaming',
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData'
               ],
               lineageProperties: []
            },
            {
               name: 'fallo_casting',
               stepType: 'Transformation',
               className: 'TriggerTransformStep',
               classPrettyName: 'Trigger',
               arity: [
                  'BinaryToNary',
                  'UnaryToNary',
                  'NaryToNary'
               ],
               writer: {
                  saveMode: 'Append',
                  tableName: '',
                  partitionBy: '',
                  errorTableName: ''
               },
               uiConfiguration: {
                  position: {
                     x: 967.205049259217,
                     y: -119.82544210633012
                  }
               },
               configuration: {
                  sql: 'select  "fallo_casting" as TableName, "TYPE_MISMATCH" as Error, current_timestamp as RawDate, c.rawData \n\tfrom Datetime c left join Casting d on c.SERNO = d.SERNO  where d.SERNO IS NULL',
                  inputSchemas: '[]',
                  executeSqlWhenEmpty: false
               },
               supportedEngines: [
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData',
                  'DiscardedData'
               ],
               lineageProperties: []
            },
            {
               name: 'fallo_rango',
               stepType: 'Transformation',
               className: 'TriggerTransformStep',
               classPrettyName: 'Trigger',
               arity: [
                  'BinaryToNary',
                  'UnaryToNary',
                  'NaryToNary'
               ],
               writer: {
                  saveMode: 'Append',
                  tableName: '',
                  partitionBy: '',
                  errorTableName: ''
               },
               uiConfiguration: {
                  position: {
                     x: 1454.9247628184876,
                     y: 221.91081140266658
                  }
               },
               configuration: {
                  sql: 'select "fallo_rango" as TableName, "FORMAT_MISMATCH" as Error, current_timestamp as RawDate , c.rawData\nfrom no_duplicados c left join rango_msgflags d on c.SERNO = d.SERNO  where d.SERNO IS NULL',
                  inputSchemas: '[]',
                  executeSqlWhenEmpty: false
               },
               supportedEngines: [
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData',
                  'DiscardedData'
               ],
               lineageProperties: []
            },
            {
               name: 'fallo_duplicados',
               stepType: 'Transformation',
               className: 'TriggerTransformStep',
               classPrettyName: 'Trigger',
               arity: [
                  'BinaryToNary',
                  'UnaryToNary',
                  'NaryToNary'
               ],
               writer: {
                  saveMode: 'Append',
                  tableName: '',
                  partitionBy: '',
                  errorTableName: ''
               },
               description: 'Hay que ver si mandamos la fila del parquet o la del Casting',
               uiConfiguration: {
                  position: {
                     x: 1256.0863519305024,
                     y: -128.69240579745252
                  }
               },
               configuration: {
                  sql: 'select  "fallo_duplicados" as TableName, "DUPLICATED_KEY" as Error, current_timestamp as RawDate, c.rawData\n\tfrom Casting c inner join tablaVolcado d on c.SERNO = d.SERNO',
                  inputSchemas: '[]',
                  executeSqlWhenEmpty: false
               },
               supportedEngines: [
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData',
                  'DiscardedData'
               ],
               lineageProperties: []
            },
            {
               name: 'new_field_id_emprs',
               stepType: 'Transformation',
               className: 'TriggerTransformStep',
               classPrettyName: 'Trigger',
               arity: [
                  'BinaryToNary',
                  'UnaryToNary',
                  'NaryToNary'
               ],
               writer: {
                  saveMode: 'Append',
                  tableName: '',
                  partitionBy: '',
                  errorTableName: ''
               },
               uiConfiguration: {
                  position: {
                     x: 1936.7081601765497,
                     y: 56.327263217108
                  }
               },
               configuration: {
                  sql: 'SELECT *,\n  (CASE \n     WHEN CARDPRODUCTSERNO IN (726,730,736,740) THEN 2  \n     ELSE 1\n   END) AS ID_EMPRS\nFROM rango_msgflags',
                  inputSchemas: '[]'
               },
               supportedEngines: [
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData',
                  'DiscardedData'
               ],
               lineageProperties: []
            },
            {
               name: 'fallo_refdate',
               stepType: 'Transformation',
               className: 'TriggerTransformStep',
               classPrettyName: 'Trigger',
               arity: [
                  'BinaryToNary',
                  'UnaryToNary',
                  'NaryToNary'
               ],
               writer: {
                  saveMode: 'Append',
                  tableName: '',
                  partitionBy: '',
                  errorTableName: ''
               },
               uiConfiguration: {
                  position: {
                     x: 662.0323668649262,
                     y: -106.95130246303472
                  }
               },
               configuration: {
                  sql: 'select  "fallo-refdate" as TableName, "DATE_ERROR" as Error, current_timestamp as RawDate, c.rawData \n\tfrom Csv c left join Datetime d on c.SERNO = d.SERNO where d.SERNO IS NULL',
                  inputSchemas: '[]',
                  executeSqlWhenEmpty: false
               },
               supportedEngines: [
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData',
                  'DiscardedData'
               ],
               lineageProperties: []
            },
            {
               name: 'rango_msgflags',
               stepType: 'Transformation',
               className: 'TriggerTransformStep',
               classPrettyName: 'Trigger',
               arity: [
                  'BinaryToNary',
                  'UnaryToNary',
                  'NaryToNary'
               ],
               writer: {
                  saveMode: 'Append',
                  tableName: '',
                  partitionBy: '',
                  errorTableName: ''
               },
               uiConfiguration: {
                  position: {
                     x: 1625.7205516032416,
                     y: 57.961881604072744
                  }
               },
               configuration: {
                  sql: 'select * from no_duplicados where MSGFLAGS in (0, 1, 2, 4, 8, 16, 32, 64, 128, 16384)\n',
                  inputSchemas: '[]',
                  executeSqlWhenEmpty: false
               },
               supportedEngines: [
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData',
                  'DiscardedData'
               ],
               lineageProperties: []
            },
            {
               name: 'fallo_id_emprs',
               stepType: 'Transformation',
               className: 'TriggerTransformStep',
               classPrettyName: 'Trigger',
               arity: [
                  'BinaryToNary',
                  'UnaryToNary',
                  'NaryToNary'
               ],
               writer: {
                  saveMode: 'Append',
                  tableName: '',
                  partitionBy: '',
                  errorTableName: ''
               },
               uiConfiguration: {
                  position: {
                     x: 1781.052387678573,
                     y: 226.6939041390542
                  }
               },
               configuration: {
                  sql: 'select  "fallo_id_emprs" as TableName, "FAILED_FIELD_CREATION" as Error, current_timestamp as RawDate, c.rawData \n\tfrom rango_msgflags c left join new_field_id_emprs d on c.SERNO = d.SERNO  where d.SERNO IS NULL',
                  inputSchemas: '[]',
                  executeSqlWhenEmpty: false
               },
               supportedEngines: [
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData',
                  'DiscardedData'
               ],
               lineageProperties: []
            },
            {
               name: 'tm_trusted_field',
               stepType: 'Transformation',
               className: 'DateTimeTransformStep',
               classPrettyName: 'Datetime',
               arity: [
                  'UnaryToNary'
               ],
               writer: {
                  saveMode: 'Append',
                  tableName: '',
                  partitionBy: '',
                  errorTableName: ''
               },
               uiConfiguration: {
                  position: {
                     x: 2171.8452302510445,
                     y: 56.688870471672374
                  }
               },
               configuration: {
                  fieldsDatetime: [
                     {
                        formatFrom: 'AUTOGENERATED',
                        localeTime: 'ENGLISH',
                        granularityNumber: '',
                        granularityTime: 'millisecond',
                        fieldsPreservationPolicy: 'APPEND',
                        outputFieldName: 'TM_TRUSTED',
                        outputFieldType: 'timestamp',
                        nullable: true,
                        outputFormatFrom: 'DEFAULT'
                     }
                  ],
                  whenRowError: '',
                  whenFieldError: ''
               },
               supportedEngines: [
                  'Streaming',
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData',
                  'DiscardedData'
               ],
               lineageProperties: []
            },
            {
               name: 'dt_trusted_field',
               stepType: 'Transformation',
               className: 'DateTimeTransformStep',
               classPrettyName: 'Datetime',
               arity: [
                  'UnaryToNary'
               ],
               writer: {
                  saveMode: 'Append',
                  tableName: 'tablaVolcado',
                  partitionBy: '',
                  errorTableName: ''
               },
               uiConfiguration: {
                  position: {
                     x: 2404.1394832018714,
                     y: 57.54109700359426
                  }
               },
               configuration: {
                  fieldsDatetime: [
                     {
                        formatFrom: 'AUTOGENERATED',
                        localeTime: 'ENGLISH',
                        granularityNumber: '',
                        granularityTime: 'millisecond',
                        fieldsPreservationPolicy: 'APPEND',
                        outputFieldName: 'DT_TRUSTED',
                        outputFieldType: 'string',
                        nullable: true,
                        outputFormatFrom: 'STANDARD',
                        outputStandardFormat: 'yyyyMMdd'
                     }
                  ],
                  whenRowError: '',
                  whenFieldError: ''
               },
               supportedEngines: [
                  'Streaming',
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData',
                  'DiscardedData'
               ],
               lineageProperties: []
            },
            {
               name: 'error_tm_trusted',
               stepType: 'Transformation',
               className: 'TriggerTransformStep',
               classPrettyName: 'Trigger',
               arity: [
                  'BinaryToNary',
                  'UnaryToNary',
                  'NaryToNary'
               ],
               writer: {
                  saveMode: 'Append',
                  tableName: '',
                  partitionBy: '',
                  errorTableName: ''
               },
               uiConfiguration: {
                  position: {
                     x: 2058.034936122605,
                     y: 219.83347644827228
                  }
               },
               configuration: {
                  sql: 'select  "error_tm_trusted" as TableName, "FAILED_FIELD_CREATION" as Error, current_timestamp as RawDate, c.rawData \n\tfrom new_field_id_emprs c left join tm_trusted_field d on c.SERNO = d.SERNO  where d.SERNO IS NULL',
                  inputSchemas: '[]',
                  executeSqlWhenEmpty: false
               },
               supportedEngines: [
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData',
                  'DiscardedData'
               ],
               lineageProperties: []
            },
            {
               name: 'fallo_dt_trusted',
               stepType: 'Transformation',
               className: 'TriggerTransformStep',
               classPrettyName: 'Trigger',
               arity: [
                  'BinaryToNary',
                  'UnaryToNary',
                  'NaryToNary'
               ],
               writer: {
                  saveMode: 'Append',
                  tableName: '',
                  partitionBy: '',
                  errorTableName: ''
               },
               uiConfiguration: {
                  position: {
                     x: 2291.6209612598677,
                     y: 216.80550201452354
                  }
               },
               configuration: {
                  sql: 'select  "fallo_dt_trusted" as TableName, "FAILED_FIELD_CREATION" as Error, current_timestamp as RawDate, c.rawData \n\tfrom tm_trusted_field c left join dt_trusted_field d on c.SERNO = d.SERNO  where d.SERNO IS NULL',
                  inputSchemas: '[]',
                  executeSqlWhenEmpty: false
               },
               supportedEngines: [
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData',
                  'DiscardedData'
               ],
               lineageProperties: []
            },
            {
               name: 'Postgres',
               stepType: 'Output',
               className: 'PostgresOutputStep',
               classPrettyName: 'Postgres',
               arity: [
                  'NullaryToNullary',
                  'NaryToNullary'
               ],
               writer: {
                  saveMode: 'Append'
               },
               uiConfiguration: {
                  position: {
                     x: 2622.914892791544,
                     y: 195.7184660582094
                  }
               },
               configuration: {
                  errorSink: false,
                  url: 'jdbc:postgresql://pg-0001.postgrestls.mesos:5432/postgres?user=sparta-server',
                  postgresSaveMode: 'STATEMENT',
                  batchsize: '1000',
                  isolationLevel: 'READ_UNCOMMITTED',
                  failFast: true,
                  tlsEnabled: true,
                  driver: '',
                  schemaFromDatabase: false,
                  saveOptions: '[]'
               },
               supportedEngines: [
                  'Streaming',
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData'
               ],
               lineageProperties: []
            },
            {
               name: 'Postgres_1',
               stepType: 'Output',
               className: 'PostgresOutputStep',
               classPrettyName: 'Postgres',
               arity: [
                  'NullaryToNullary',
                  'NaryToNullary'
               ],
               writer: {
                  saveMode: 'Append'
               },
               uiConfiguration: {
                  position: {
                     x: 1455.1070761487083,
                     y: 735.3683597690222
                  }
               },
               configuration: {
                  errorSink: false,
                  url: 'jdbc:postgresql://pg-0001.postgrestls.mesos:5432/postgres?user=sparta-server',
                  postgresSaveMode: 'STATEMENT',
                  batchsize: '1000',
                  isolationLevel: 'READ_UNCOMMITTED',
                  failFast: true,
                  tlsEnabled: true,
                  driver: '',
                  schemaFromDatabase: false,
                  saveOptions: '[]'
               },
               supportedEngines: [
                  'Streaming',
                  'Batch'
               ],
               executionEngine: 'Batch',
               supportedDataRelations: [
                  'ValidData'
               ],
               lineageProperties: []
            }
         ],
            validationErrors: {
            valid: false,
               messages: []
         },
         savedWorkflow: false,
            editionConfig: false,
            editionConfigType: null,
            editionConfigData: null,
            editionSaved: false,
            entityNameValidation: false,
            selectedEdge: null,
            selectedEntity: '',
            showEntityDetails: false,
            showSettings: false,
            isShowedCrossdataCatalog: true,
            edgeOptions: {
            clientX: 0,
               clientY: 0,
               active: false
         }
      },
      entities: {
         templates: {
            input: [],
               output: [
               {
                  id: 'dce0c172-6f29-4656-b435-b26f2de720b4',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(15)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c62f3d7c-1c2a-4939-ad02-f115057275a7',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(44)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '4a4ed281-710f-422f-8b0b-1117cdc7f4c5',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(3)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a3736d0c-5e1a-4d9d-afc7-3d6f9bc781b0',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(42)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f94a2d9c-ec49-47af-af3b-2dfedd641fd5',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f12e7b92-bbef-4dab-9ae6-4310a661da89',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(44)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '50b4fea3-165e-4444-8f9b-4b880da1e729',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(20)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'dc5fceb4-4c50-458d-8a46-75a9fe3e9f8b',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(13)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '732f1311-4785-4d3b-b082-89852ec9eb8c',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(36)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a9e222d5-0317-4e62-9cee-56481c27d372',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(17)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '83e9c578-4471-4978-a296-6964ed58a923',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(21)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '2d9bd08f-d6a0-4ecd-99dc-2a6f5f59c5f6',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(35)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'da5094cf-8035-434e-adc2-017d5934074f',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(35)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '43b1f3b0-d02d-4e02-8e6d-0bb18c4f9a43',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(43)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '8c70846e-fa38-44b8-8bb6-55719e8b8fcb',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(14)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a495728b-07f5-45be-8699-b9d4353fb529',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(27)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '425c7822-0bf9-40d4-981d-f3a89aa6812d',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(0)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '1c589287-10b9-4561-82f8-361a9b73ac17',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(43)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '7c051d12-5785-4a3d-ba28-78de21fd5eb4',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(24)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '6d4303c8-1f09-42da-87cb-17917b842dba',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(32)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'bebdf741-b0c2-4055-b10d-ee9b42c52476',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(32)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '630ca9e0-8c1d-4a53-93be-9556d1c01001',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(34)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '8f2f9bfc-bcf4-4910-b932-ca01674a663b',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(50)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c2013129-bbb2-4a0f-b745-0013b80f3342',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a2b3812c-e8c2-42d0-b463-f71f2cd7811c',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(25)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a8be74cb-03fe-43cd-bef9-710deebd7b47',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(31)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '3807847c-9723-4a94-8ec0-b77859ed6806',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(48)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '4562e3a9-a88b-4b19-b23e-0e85bc3a020c',
                  templateType: 'output',
                  name: 'Print tpl(2)(8)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e0a8b3d1-9708-48c7-b3b2-c62364c548f4',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(20)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f104a48b-cc5c-4df5-b716-6278f4c11d80',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(4)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a32d6e85-cc43-447d-8d55-10e58513b5d7',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(18)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e41ed595-2809-4eba-a17a-94cf11f1bca6',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(43)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '4d143306-5201-4ec0-8628-078a916e6d5c',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(45)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '49f60f49-628b-4c66-a071-de7745670d39',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(5)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'ac2326da-7c83-4475-9a49-aeb2dd950121',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(32)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '4b6faa10-0574-4545-90f0-a29ca2773488',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(26)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c5a2fcb3-65da-47e0-a36b-a0b89debe61c',
                  templateType: 'output',
                  name: 'Print tpl(2)(4)(3)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '905cfceb-3e7a-4adc-8024-23d52a264149',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(35)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '304462d8-ae40-43e6-b0e1-63b655a75ef4',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(1)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '278a5246-132b-45df-b4e3-a7736ad0203a',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(6)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '0fa0eca0-3b5c-4e8e-b6d0-17c2f8687436',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(50)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '77f77119-2363-44b8-a5fc-3e03099538ef',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(35)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a90747ef-4a7d-48c5-ae6b-43f2eafeed3b',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(49)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '31f21088-9519-4ba4-8e63-8043e2ec0c30',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(26)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '51ba81d3-d400-4783-952a-36fbd793503b',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(12)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '3c57dd54-731b-452e-b98a-fd5670186131',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(24)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '573bfc04-d5ea-462d-9875-02810e7a507c',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(18)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '242a1d4b-7a24-49cf-8380-c956114a678c',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(4)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'fedd144b-d553-41e6-8c39-0799e65f2950',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(12)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a1307eca-a321-409a-8762-941cfe496232',
                  templateType: 'output',
                  name: 'Print tpl(2)(5)(1)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '4d90ca70-9e32-4b3b-a41d-6cef666c1135',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(26)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '6f6e0341-1864-40c2-adfe-bb1818b4a9fc',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(9)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'cb01577c-8cd4-490e-954a-5a52984d068f',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(24)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '6d12a8dd-5221-4a7b-adff-bd318b08f714',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(20)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '45a2e105-19e1-4258-bf50-6dedd54ed29a',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(14)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '9987abe5-2653-42a3-ac05-b48709a631cf',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(8)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '9c9f75cc-fe0e-4369-8f30-c9c9713c4031',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(4)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'd340da68-0b3c-4fe3-b6f5-d6251397a52e',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(42)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '57473d30-c303-46cf-afb4-489d3494de3b',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(54)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e5423849-7305-43eb-a82e-b906b7e60ba9',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(6)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a4662d1b-1f1d-4c98-9ae1-24a6c5172d82',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(25)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c5e46e89-02bc-4b44-b850-e6b09b0fb559',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(19)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e55a10ae-acd3-4717-9b01-d5a4a35a271e',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(39)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'db80ed2e-cc89-44ad-b665-444ea40d1e05',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(37)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '21e9d16a-36eb-4c2c-940d-ffdb6b362edf',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(29)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '87cb066b-b8d5-4891-89eb-2de07863e6e8',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(2)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '41eb1558-5fed-4a25-beb7-9ca6cd24071f',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(39)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'dcaedead-08f0-4418-a1bb-b9eca0098925',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(50)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '1e1144e5-c4b1-4628-95ac-c2305c593f03',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(35)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c1f3c456-27aa-4ff3-a4b2-aed99bfc4218',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(30)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'db69f316-ef6d-4066-bfa2-9f806d84b3e3',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(10)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '41bb419a-656e-4c87-a9cd-8d9ebf7ce9d3',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(3)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c5495228-0a66-4fdf-bc90-e97b79d8253a',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(9)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'fbe78e67-4982-49de-9038-38450fbe97b5',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(55)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'de826b4e-8c3a-48d9-bea7-e2a48399accf',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(20)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f80f1604-4aee-43ae-8e3c-7ad7e854f9fa',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(28)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '10768001-c24c-47d7-9639-4d0c2fe608ec',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(20)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '8e9be0f3-82fc-43be-b197-ba3cf2ea1028',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(30)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e8d52a44-cd49-4df1-a2d2-c79e57607f24',
                  templateType: 'output',
                  name: 'Print tpl(2)(4)(4)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5e164f1b-2bbb-44a3-a7ed-efc6c3e5a0a8',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(7)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '60f78d99-6ba6-432b-8807-c1f1f83909d5',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(14)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '4aa231de-7f78-41ac-8276-d3dd2f77d0d9',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(19)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '2726fa07-a260-4e2e-ae97-aff0595f5894',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(39)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '8eb374f4-28bf-4b0d-b144-c4c91abdffc3',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(44)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '948f609f-bbf1-4cae-9454-f14a0a49d8f5',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(55)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'fcc5f785-e987-4447-b375-5afa9d95ec20',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(40)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e1ec6a2b-f944-4b45-856d-6e723099edee',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(11)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '3158a346-2bca-4ede-891b-f204b595a481',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(25)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'd674fd8b-995d-4170-b79b-3746fc701896',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(53)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a3bf9920-3869-4d23-aa67-a4d88025e935',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(10)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'bf8fe556-e425-439c-8049-f3e2de437530',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(0)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '4c1842f0-d88c-4818-89ab-7558e12a76cb',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(7)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '9b6da8a4-0858-4915-9a91-ac8e52fd57b7',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(13)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a5dfa7d0-4904-4606-9bf7-ebe024518eb2',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(4)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '665ac04c-666f-4802-95ed-6c7767508220',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(41)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '06f69131-3635-4065-bd22-7d4c2e08036f',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(47)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '150b30cb-07c6-47a3-815a-4fb4d5e9454c',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(18)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '3b679836-8de6-4893-8228-eb4d2216fd55',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f9ef4012-ba67-4fd1-b9d9-c13595cb2bc1',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(46)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '6657ac2c-18e5-4473-b053-22899d9c49ac',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(38)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '4cae80ce-15ea-4eba-b280-a13f731c165d',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(27)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '2656a11a-7b97-49be-8e93-f526eb7f59dc',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(31)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '3686b042-a279-4638-9112-7533b6f52b41',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(12)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '7466bf9f-7a70-4c03-818c-15a2f02a1e07',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(14)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'ca342bcc-3e3b-4263-a70e-eb3545cd1711',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(30)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '3a4bf520-5c91-4e28-b117-96667416f0a5',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(14)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '2992437b-c399-467f-b9ed-a0fad20823ed',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(52)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '2ac3b5b1-dadc-4a09-8146-b57d15a27a9e',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(33)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '6e2e1012-54c5-4b7f-a3fa-ea65f81d660e',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(18)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'ea34a525-0e65-4b0a-a3bd-5cdf632215fe',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(6)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '12c4b0fa-31db-4cd1-9867-28bb924beaea',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(11)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '06df872b-f4b4-47a6-99d3-f00553a2f492',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(40)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'b6322aeb-5029-4d78-a388-edb1c5a49900',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(34)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '3f29a554-1236-4ee8-a516-b6aa6c92ecbe',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(13)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '8f8e7114-c449-4f0d-96c8-4354b6360847',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(47)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'b695086b-cfad-4bf1-94c4-bc5b7c12b9de',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(35)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '0ca165d0-99ba-4181-87c8-5f20a4e98cc5',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(10)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5581b780-d2a6-4c9c-8fd8-a230b5c00d08',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(22)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5af6e8b8-8a68-4ce1-8884-33864b7a261e',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(41)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5c03cb26-22fd-43e8-b3bd-dec926a349da',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(49)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'd7e272c0-9849-4ae6-b730-b782b94fdca5',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(15)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c22ed1b6-c619-465f-a73b-0f69420c0733',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(49)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '97ea6da8-2fa9-4ff4-95b9-281a22e91ce8',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(38)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'ea60896c-2423-4045-ba3a-049f5c0bb4c2',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(53)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'bac6753f-e901-4442-9d2b-935c4758c1be',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(38)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '1f88b797-a1b2-4000-9a68-fbd668ee2f2e',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(2)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '42660454-336e-4ecf-9745-ff295ee278cb',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(36)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c30dc738-8a34-4bdf-a024-a366dc8c821c',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(28)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'd5ef00fa-fb0e-4167-98bf-223b5e5b9503',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(13)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f484f90b-cb84-4e68-883b-ccda6ed64d23',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(39)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '9d6b4b55-7c96-4eee-97c0-11ef93210ce0',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(6)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '89a20f10-6baa-4501-bc89-bca85687793d',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(52)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'dbcb09d9-559d-4d7d-9400-dc48ec79066c',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(21)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c0fcc657-8087-4513-803b-249bb6328dd7',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(39)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '366b68ba-9497-4340-9339-6cf967b5549f',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(8)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'b8c1af51-e459-40b4-8739-483225bdbf2a',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(42)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'de1ef28c-31d1-4d71-b3e7-3c94b90228db',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(5)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '885b80cf-22ec-4b2c-bd5c-815fb7a63a33',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(4)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '42e76139-2d1b-4590-9063-e72ac638619c',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(9)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '90f56948-0427-4268-bf61-73bc522268b1',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(36)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '1b5412dc-8dd9-4231-9d82-8335728bfd0d',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(38)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '05b29f96-e497-4067-9d19-ebc0e902e667',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(40)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '3bed8651-a281-43cb-9a8b-5da0f7002a9e',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(45)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '95399d0d-e91d-486c-ba4d-8308c699a18a',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(4)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'ec9c2ecb-bb6d-4750-ba2a-967675553ead',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(17)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e840481d-4c1e-42d4-936a-bd7780e3640b',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(9)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f42d5302-368b-4c01-bdef-1ec785b425e8',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(31)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '34a286f0-d137-45ae-a225-92d6e777d1f5',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(16)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '59b9d23b-b212-411c-9fdd-71fb4ae567c5',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(2)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '692980d8-804c-4a8a-a679-2e172c0d8b90',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(6)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '32505954-bd12-4096-9b4f-f44b7c8efdf3',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(30)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'cd8d2b55-1b31-4780-baac-e207855f93b0',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(26)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'cc56def4-c077-4446-9bdc-10c3ce97d917',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(5)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5125c908-98a3-4a6d-b588-eacb20a9b67e',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(36)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '36ded950-6c82-48e9-8240-823408cedfa2',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(7)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '893c997c-581e-4771-b187-995f44fb4f82',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(18)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '01da86aa-86c3-44cc-afd6-c67877f19174',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(36)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '21e9bf95-86e1-4c4b-865c-5f4ea5aa58d4',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(1)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'cb266ca3-60af-4b20-b48e-ff9ec6cf68fd',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(10)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'fdc0a363-daee-47a0-9afa-8d8a5f9d5350',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(10)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'ebd2eb4c-1c1e-4fff-ae49-5e42454eec9e',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(37)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'd346ec5a-360f-47f3-acb7-7496018ff7e7',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(7)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '11d61b8c-e050-4df9-96a1-a972eb62ca7c',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(8)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '49491a80-719a-4abd-870d-9d2b2a22d483',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(44)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '8efc59a9-fb5e-4601-963d-1b549598d17f',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(41)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a793b2f2-41fb-4c6e-87b2-caf24321bc43',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(49)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '058652cf-9ed2-4627-a752-ff1ae1c38b6f',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(5)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f6e7b73c-8d25-4f4e-a6c9-97230a930925',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(40)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'b17cba8f-6841-4839-adbf-f92c5e53b4b6',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(21)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '03e6bf65-2cf7-4549-bcbc-9acc87029837',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(29)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '7c761002-8ee4-4c54-9689-cff61040058b',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(12)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'd28d8572-6448-4432-8c7a-bebb3529f503',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(0)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'b6179c36-8238-4784-9577-7df25d907171',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(3)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a7c0df09-50b8-4b45-a932-56ba0c407100',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(22)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e88a3982-27a7-4953-8ce7-427c5a20eca0',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(13)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e886fe28-1d47-461b-8694-f269936c4aeb',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(12)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '34658d7f-9bbc-4fe4-be43-eda6eecc27f7',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(17)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '0c646e36-8b7f-4742-b07a-87b42d48c453',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(3)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '6b44f25c-b3f9-4538-8e36-4b7ccc17da52',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(16)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c10f3f34-7f10-4393-b1a4-f7aed8e63b8d',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(26)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '7b75f97b-166f-4325-9266-a31c5c8db4cf',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(30)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '4915e008-7778-4095-8229-4049cd00b829',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(33)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'd9607720-2ae6-4d69-be65-5098a46bfa1b',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(31)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '6c9377bd-bf60-4c91-b56b-cae8d7514fef',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(8)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '06822bfb-d950-482f-8cff-f6ed1aef31b8',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(54)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '1b19816a-57ea-4482-bb01-5738e766f36c',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(16)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e6b1255c-a54b-42ff-8154-fd35a59508b4',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(16)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '9b67f1a6-f386-4f7f-8570-8a6628179a5e',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '3fd10db8-4880-4636-a751-01dc98b64f29',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(31)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '8ae69cfa-330d-4827-89d5-011dd2f77534',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(28)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c77f2086-3166-4bcf-9c01-b72a736aeae1',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(1)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '0a93d757-e74b-462b-8645-f426e71a5c37',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(34)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5a14e610-3c6c-4c65-afc2-5ce7b8f8f3d1',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(46)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '9d94012b-e8da-4c49-8d93-4b4fc62af966',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(53)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '1766270e-a64c-45a8-aeb7-91e60fc36078',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(3)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'de7ed6f4-f777-45c0-b323-ea8063d70612',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(1)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a0205c67-9644-46c2-8205-bd038d731890',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(21)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '0b08fd6f-78ed-4210-b143-a2cdf0fa0b87',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(9)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a4955e1b-150b-4cf3-8161-2659b96dac1a',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(52)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'eac32abb-4d9a-4661-898e-013965285bd5',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(41)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '086b60da-9d4a-46a5-9106-b92073138eb0',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(21)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a599265c-504a-4708-9904-e8a9ad5f470b',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(46)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '73ac8ea5-3517-42cf-a951-f020cb00d36e',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(43)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'b21513b9-d31b-4dfe-b30f-ccca7bf32f87',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(37)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'ed17e442-e99d-4ee6-b76a-46bc135ab207',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(11)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'b0160b2b-f265-41d9-a352-26584a088caf',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(51)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '74b11c5d-9358-4c2a-b48b-b7315e9a4eef',
                  templateType: 'output',
                  name: 'Print tpl(2)(4)(0)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '436eac60-da36-443f-9bc7-cda96af6c5d7',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(27)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'ae0799cd-f252-4d59-a169-4e70c15fb6b0',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(42)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'dd87005e-fd16-4c28-8da7-7722f0f4cb40',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(29)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '50d70f96-4946-4e07-aa1c-5134003d623c',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '6c2a11d9-e97f-4535-9ce3-9253c56ffadf',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(11)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f3cba3e5-b4e0-4e12-915c-dbceb7ce4915',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(1)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '4710e220-9a01-4ed5-8fed-50cd72e34d1b',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(47)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '710cdd83-593d-4bd7-9c78-4657e27f5146',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(16)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '3238470b-6252-4da8-a93b-22622170a444',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(35)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a16d7ee4-4a1d-44d4-8b87-96e440760f36',
                  templateType: 'output',
                  name: 'Print tpl(2)(5)(2)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '402a4c70-53ce-434c-bf36-07c80fcdd06b',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(54)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '930f4dc6-6234-41c6-8b26-2af37b67a03b',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(37)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e7ddd1a9-b2d6-4ac2-aec9-bbd9d92eecd9',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(24)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'ccdcaa5a-5abd-468f-9989-fbbed3772ec4',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(29)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '89a956a8-347a-4f66-b8bb-00bc1c6a27de',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(3)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '332b5518-6dfc-45a5-849f-aad41e8e1550',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(1)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'cd7977f3-aee9-4029-bfff-0b7729d0aeac',
                  templateType: 'output',
                  name: 'Print tpl(2)(5)(0)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a064fe45-c1d6-4822-a059-d9c4f3e8f52b',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(26)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '637d5a86-c498-4dbe-a790-81efae6dfb86',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(16)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5e9c15b7-4b94-4d22-916b-e497488712c1',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(24)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '165b1dda-9a2d-415b-b1ca-2576b06a9ce9',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e8b7d489-f94d-443c-b2e5-73987377ea72',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(6)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f52df516-68c2-458b-a55f-557c309e6ebf',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(22)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '84631973-4e57-4ec5-b09e-20ae59e54dfe',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(8)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '43315e2c-8df8-4c73-b08e-3d75897b1b85',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(8)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '0485132f-6878-4ee6-ae63-99cdc0df2806',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(11)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '04a4b863-249a-4b13-8b4f-9dd95e810620',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(38)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a1e9e8a9-f85d-447b-ba17-58ceb086d1b5',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(49)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5d81e850-76b7-4a67-90e0-cff24d8efd78',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(22)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f65ad5aa-cbce-479c-8d4d-b1e332608757',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(16)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '1b2ed6b4-6633-43c8-bb96-ad88abb88398',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(15)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '11e52870-a0fd-4a68-9121-0efca032e0a8',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(45)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5b946a7c-4ac5-4d40-8ff4-ae84fb8e6f7a',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(5)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '72486116-f36b-446a-8fbe-1919363fca27',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(7)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'b4bc57f6-8454-4511-bde2-07bed196b98d',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(34)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c5f8e381-a802-440f-8035-92562f54e5c2',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(14)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5f51e848-08f6-414b-8d46-0dcbcfb78596',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(11)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e651b52e-b584-4c9d-b939-9509cf9cd08f',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(22)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c8ad5d30-2eb6-4dc6-82d8-f98e4b9d014a',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(48)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '730d4b7f-d2ae-4483-901e-17ca635548a0',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(12)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '2a30eb13-b3ac-469d-9204-459caa6cc0c7',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(27)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'b9af9aa8-8faa-46dd-be73-d05ebcf04965',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(37)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '276cc25c-cff9-4429-8d25-bede76f0ccc8',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(24)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f655501e-0d93-49a6-a4e0-bb542ee4bc3c',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(52)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '7c283ebb-44ec-425a-a5ff-225d3d2c8cd0',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(19)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5e4d0789-ee9d-49c2-8d86-ea7c4c9150d1',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(23)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '464f2164-b30a-48ae-bbc3-71a8fe15e7aa',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(42)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '0ff6a397-23d2-497d-bbf0-dff70c13f33a',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(10)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '388dc1d6-3352-46ff-942f-b9c35d5cc48a',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(29)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '6982ffe9-0448-418e-a1b4-4e1a7df90873',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(41)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5095520b-9f9b-44de-adbb-42fe56f9a642',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(7)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '18a3dabd-0ba5-471e-bc3b-41f219f5093f',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(14)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'bfd0ee55-b397-457d-8a72-1b475f81ad06',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(15)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '7e4917cb-25c5-49f3-ac66-fc2cf20444f5',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(33)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '17987d07-f495-474f-92d5-c0bfb912168f',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(32)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '14d5f998-0eaf-43c8-ae9a-91de9c8ed7ba',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(8)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '419e3a9d-df17-4033-a17a-6d0fe380f1a7',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(34)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '715236f5-79f8-47a2-a031-36cf6c80a486',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(4)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '18f26624-726a-4c0b-a303-d7252a4d98a9',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(15)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'db70cc9f-00bc-4e7e-9612-8013042da388',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(20)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e790c0d7-a330-453f-b14c-8ef43fe2d7c1',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(45)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'b1b7a314-8963-4c25-bc92-6b3c25fc58f8',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(36)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '569bcc96-cbbe-40a6-8f3d-58a71ef6587a',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(32)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '3a3dc4cc-b10f-41d8-8695-e805843b23fb',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(0)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '2c9de631-1e12-4bb8-9d44-de2b6f001951',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(32)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '8ffbb1ad-c5f8-4988-9c3d-ffd546a96f7c',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(13)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '54009265-cc07-4031-bf6b-c5b80a72b474',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(25)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '19642451-82fd-4020-a623-279b0608c50f',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(39)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'b5d9b8f1-04e0-4f59-ab17-d021b123d7f6',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(47)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '7c9a91ae-962c-4d54-bc57-91f0f6a7dac6',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(9)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '8b92e482-b1a6-44fa-b374-8b947fa31ba4',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(39)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '9db5945e-ab6a-4eee-9c9b-e01e09f95c16',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(28)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '469e7149-aacf-480e-a5b8-ee6099b939fb',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(21)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f84eabcc-7237-46d7-a76a-97a7ba5695df',
                  templateType: 'output',
                  name: 'Print tpl(2)(4)(1)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '91961a7b-363d-4284-9186-3abb0d8b5ac5',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(11)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '4acf4142-9640-41a3-b556-40e6ef584e3c',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(23)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a6a88160-5b8c-4dc8-a3f6-7bcf25ab3a16',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(50)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '47c0bdf8-aa5d-419f-a597-80e372607509',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(33)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '94f45f0b-580f-4f42-8bab-450cb3dfe673',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(23)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '00d04cee-222a-4f3a-b55c-d9d2c3e53843',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(9)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '3115a2f5-2841-420f-8ff3-11bc0df35e2d',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(44)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '4d9044fa-1018-4c5c-b01d-facc1dfe48ce',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(48)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '1e341fa4-f217-471f-b562-56290208d384',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(23)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '3f433f12-3fb8-412d-bf3e-d72298ece776',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(33)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '438c64b3-a623-4e52-8468-960b2741701e',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(15)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '34e098cc-9b2d-4ead-8772-fdb2eae956ee',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(54)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '2cb48a89-1928-437a-8277-8fa0495e07de',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(2)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '3ae0f05e-7335-460c-9842-aee4fb28d5f4',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(13)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'd4f9710f-8c2d-4e00-bca0-88fc5cc84393',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(2)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '96a663e6-7bf5-41f7-9388-662c0262df79',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(6)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5039a553-b48b-4b84-b0b0-bd29da50d70a',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(8)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '96c32853-1032-4ad5-b537-8c9435c8aafb',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(37)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e55ec599-0d35-441d-932d-591286d6a655',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(25)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c7b3d9cd-17f6-45db-97e4-70db9fc2c0d1',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(17)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '3644bc3d-1de2-4aec-aa34-0fc8fe3cb1cf',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(48)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'b8366fa3-f796-4544-a00a-73fda609712e',
                  templateType: 'output',
                  name: 'Print tpl(2)(4)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f57db233-6264-47ae-939d-73a225c3ddac',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(15)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '61db6dd9-d513-4bd0-99d8-342448348626',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(33)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '4eb17acd-ff11-40f1-b7f2-f1871d4415f4',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(0)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '9d004f6d-bf49-4203-99d4-d65e4bbfbcd5',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(27)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '520b7940-e516-40b7-bc89-69961a24f654',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(7)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '2b8b7982-5ac3-4c4a-a447-f00c4bcd256b',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(45)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '89a51990-72e9-438a-bd92-4aceaf8816bc',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(37)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '91cc4c22-6fbf-4421-a06a-0cd708730771',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(0)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c0c3ca3f-a3bb-4014-a195-a08cfe5f0b72',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(11)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '4681bcd0-1c42-434f-b6cd-6bbbc0309cf2',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(15)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '38fe8e57-1de3-41a5-b3ec-27fb74565df7',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(52)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'bd02e4d8-7623-415d-92d7-1317539e431b',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(14)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '7d8d2562-cc84-47aa-b5a2-13e801b0e3c7',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(48)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '2fe39918-c978-47cf-a77c-764113bdf0be',
                  templateType: 'output',
                  name: 'Print tpl(2)(5)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '9858c7c3-b434-484b-93a8-fa8f9f92670f',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(53)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f0409091-0b27-4791-8f62-d780b2012e31',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(12)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '45ca87b1-45cc-4aba-8ee5-2eeaeb16da94',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(13)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e284995b-dc3c-4583-9b89-0ec8b9045166',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(5)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '9e4d2386-81e4-4a76-9699-4c4743af2e8e',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(51)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5030d752-362a-4887-a997-4bb16671994e',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(51)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'fbdd6f0d-d648-4f6b-ba88-f669a24d86b2',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(2)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '6622b313-464f-4f26-87e4-e691c9f5aae4',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(9)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '7f631bdc-692c-4c20-af3e-fac1350dadba',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(10)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '9a827325-5317-4442-96ec-a5183fe11276',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(12)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'd0cead50-5967-49ea-933d-0d6c25db9e55',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(22)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'eb37f66b-c187-4969-a1aa-3dee748bf384',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(10)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'd127e9d5-0fef-4447-96a6-8ec7515ddf01',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'd7f0e321-2c31-47f9-aa8c-054d72e24f8f',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(1)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printData: true,
                     printSchema: true,
                     errorSink: false,
                     printMetadata: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2018-03-07T09:29:39Z',
                  supportedEngines: [
                     'Streaming',
                     'Batch'
                  ],
                  executionEngine: 'Streaming'
               },
               {
                  id: '9148d8e6-faf3-4f34-9799-331659bf3e7c',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(20)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '97456eff-24ac-4f53-be01-0e5ebc871321',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(3)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'fe08f058-dd08-4da5-96dd-40dc2582e20e',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(13)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'd5be770d-a618-4b4b-8360-5ea158cd4b77',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(33)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '9987996f-4785-4d18-aa07-58bc51ab24c6',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(23)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '723a3931-7419-4125-b153-ef8e17cbbde0',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(7)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '994a63e1-178b-45c0-b353-79ed0b7b97d4',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(40)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '64cb97b0-b7c4-4cab-bca4-f9db8bcc5436',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '743ba809-ec23-4027-a869-48ed91621154',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(15)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '06366b2c-cc45-4f70-a1d8-07a06eb7880c',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(29)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '574e679f-6f57-4f02-ae43-eb59fa19979b',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(18)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'def24b59-334d-470d-aff8-81805abcf06a',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(25)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'd9f25089-6a77-4ee0-9341-375ea86b2e9d',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(43)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'ef11fd83-eb84-45f1-945e-119507926d53',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(3)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '425cf08f-135f-45b4-8610-2425d289f462',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(0)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '4779ea1c-5da5-4f8a-9cbd-9cca81ee2644',
                  templateType: 'output',
                  name: 'Print tpl(2)(9)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'ca4f7230-9c1f-4299-bb6f-1ec7dc13877b',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(1)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '775f31a6-aefb-4dde-a008-3f0531ffbed2',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(30)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '7babb7a2-ebc3-4532-a84a-14b3d85fc1e9',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(4)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '0118fee6-07ce-49f2-98e3-eaa36fe8932c',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(36)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e6b9e6aa-f780-4146-b9d6-8837db7cd41d',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(6)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '4280a3d5-9422-4c58-b726-2e767e970e05',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(0)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '8c1a22e3-d315-4479-a2cd-e4a7eb386cb2',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(2)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '30db6020-be4c-4d38-9cd5-72aa6fb36f71',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(29)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'ee7c7477-7274-4b47-b3ae-ac6b23682ae8',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(19)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '682d75c5-0a66-4569-8c9d-ff1e034ed5fb',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(47)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '2bbe4e8f-7916-4c66-ac4f-6b9b66a27ecb',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(43)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '1a403176-04e0-41d4-bbee-cf8cacd22d89',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(41)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '935d3187-166a-4451-a0ba-1304951e4c72',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(23)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '35c120d7-0d1b-41e8-ae38-2866a9b56223',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(14)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '8a6b64f3-fbd9-4f8b-9df5-c102f000ac98',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(17)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c9771b51-fcb5-4e02-9ec6-1707d6ebef4b',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(46)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c94fb136-2cee-4121-a9d5-2bbb7df15d9b',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(27)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'ff70d895-fa29-456d-83f1-4ce1f0081c4e',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(17)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '59acecf9-ed99-4ffe-ad39-e6000eb4167b',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(46)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '0a76d1c4-c50f-4f7e-9fd9-033934c76a6b',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(12)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5197f1ef-e228-4d12-90a4-d92ee991e2ec',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(1)(0)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'ec5a4f2e-9e6b-4051-bdbb-4077917ad746',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(21)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '14dba3be-0dce-4720-962b-87861b6c1caf',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(51)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '77bc19d0-6880-42ea-9090-73769cb02a45',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(5)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a5f66871-714f-411f-ab85-d863c5c7fd88',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(17)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '9fb840ec-2e8f-400e-94a7-bd3a6235dfc5',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(31)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '8ea9711a-f9e5-4f6c-af3f-9d42bd4ec900',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(55)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'bb933eb6-1d6d-40b2-b6a8-76dce4766f2c',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(19)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f4ea3a3f-461f-4360-99fa-36347ba7be5a',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(25)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '9fb893ec-31ae-4121-bbf5-cb8ae87ec077',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(18)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '0e033975-7b89-4514-9e9b-d19e7eef0290',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(54)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '6b6c5a2f-e90c-4686-bf49-3003398b263f',
                  templateType: 'output',
                  name: 'Print tpl(2)(6)(16)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '07a8d141-85b3-47a9-9cac-0ac23ca56534',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(22)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'd02c4156-130a-43f7-b787-1383063f67d6',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(28)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5b8ee598-cc22-43ab-9586-685f67e8ea55',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(30)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '1f7828e9-8219-44e9-aa22-b39aa5b55689',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(24)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f049a368-fbe3-4dfb-9172-5ec65565d842',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(55)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '57d801c3-6ed4-479b-9f14-d29ecd278d17',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(38)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '63a6f96d-0445-43af-9367-de3be84a2d13',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(23)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '00e346b8-f057-4a55-b759-900c63e579a0',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(34)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '30e5efd0-525f-4dfa-a7b5-d502d49910d1',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(40)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '2c6082b2-6e95-4677-b5f1-75741f3e45b4',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(3)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'bea57599-16a3-4fbd-92db-2933e161e2ca',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(28)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '49d2c383-4e92-40e2-b188-9f777daf6902',
                  templateType: 'output',
                  name: 'Print tpl(2)(7)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '0bf44916-d299-4cc6-a740-573a5e4ee558',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(9)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'fa1e7e6a-6738-42d0-820f-3c7d3893a7a9',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(11)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '1926e936-0c29-46b1-a0be-f527afc79838',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(1)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '99d37a94-d3c7-425b-8531-8fffc2aa79ff',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(4)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a11a8b65-48e8-4793-9c3d-7a56fb84fd37',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(51)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'f91a0ece-8beb-4583-91d3-119523011d88',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(5)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'b27a115c-e058-48ca-a067-ce398214c888',
                  templateType: 'output',
                  name: 'Print tpl(2)(4)(2)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5afe705c-9a57-4fd5-bc88-4679f8544060',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(50)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'ba122d31-d3a8-48f3-8a24-82ef2f7b18e2',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(26)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '98551278-612a-4832-82d2-52a2cc6f445b',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(32)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '71688c1b-cc84-4efe-be66-c081fc14401d',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(10)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'a20c2511-6c9a-4f13-b08f-60dad8073387',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(6)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'faef2186-559b-4ff3-896a-1509f0f622fb',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(7)(19)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '2b13146a-0290-46be-998e-675e60ebde4e',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(17)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '27547caa-1d80-4a5a-8319-c89010021e3e',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(7)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'e20882e1-cc6e-43f6-acd3-2c72c4b80a3f',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(19)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '20ac46ed-8c96-4560-a76f-f996e9fdaf39',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(2)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '8a5f7a57-bc3f-4951-bea9-a9cfc1b53125',
                  templateType: 'output',
                  name: 'Print tpl(2)(0)(27)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '5771bada-be24-419b-a8cb-94476a40b98f',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(31)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'c4a6ec61-58da-4436-be91-a2981c9331d2',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(38)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '85aa2300-fbd7-4697-8016-f00fbc563352',
                  templateType: 'output',
                  name: 'Print tpl(2)(1)(5)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: 'cd71ee4b-8445-40da-9a4c-cb7a65273f0a',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(8)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '2e1eafe5-2df4-4ca6-afe9-faec4b5711c8',
                  templateType: 'output',
                  name: 'Print tpl(2)(10)(28)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '8747eb49-1189-4e9a-844b-590bebe1e6cc',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(34)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '1f7428e6-14dd-42f5-8fcf-39750fc164ef',
                  templateType: 'output',
                  name: 'Print tpl(2)(3)(42)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '05d7041d-dc12-4db2-bf14-97af7328e74f',
                  templateType: 'output',
                  name: 'Print tpl(1)(0)(6)(2)(16)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               },
               {
                  id: '93da591d-d045-429d-ae76-46123c2aff0f',
                  templateType: 'output',
                  name: 'Print tpl(2)(2)(53)',
                  description: 'Print the number of elements, schemas and data',
                  className: 'PrintOutputStep',
                  classPrettyName: 'Print',
                  configuration: {
                     printMetadata: true,
                     printData: true,
                     printSchema: true,
                     logLevel: 'error'
                  },
                  creationDate: '2017-09-28T10:19:45Z',
                  lastUpdateDate: '2017-12-20T15:37:01Z',
                  supportedEngines: [],
                  executionEngine: 'Streaming'
               }
            ],
               transformation: []
         },
         selectedCreationEntity: null,
            entityCreationMode: false,
            floatingMenuSearch: '',
            workflowType: 'Batch',
            menuOptions: [
            {
               name: 'Input',
               icon: 'icon-login',
               value: 'action',
               subMenus: [
                  {
                     name: 'Avro',
                     value: {
                        name: 'Avro',
                        icon: 'Avro',
                        className: 'AvroInputStep',
                        classPrettyName: 'Avro',
                        arity: [
                           'NullaryToNary'
                        ],
                        supportedEngines: [
                           'Batch'
                        ],
                        supportedDataRelations: [
                           'ValidData'
                        ],
                        description: 'Allows you to load data from avro files',
                        properties: [
                           {
                              propertyId: 'path',
                              propertyName: '_PATH_',
                              propertyType: 'text',
                              required: true,
                              tooltip: 'Path to an Avro file located in a compatible filesystem (HDFS, S3, Azure, etc). If the file is located in the HDFS used in the tenant configuration one can specify the path omitting the connection details.',
                              placeholder: '/user/avro/example.avro',
                              qa: 'fragment-details-avro-path'
                           },
                           {
                              propertyId: 'schema.provided',
                              propertyName: '_DESERIALIZER_SCHEMA_',
                              propertyType: 'textarea',
                              contentType: 'JSON',
                              width: 8,
                              tooltip: 'Avro Schema expressed in JSON format.',
                              required: false,
                              qa: 'workflow-input-avro-schema-provided'
                           },
                           {
                              propertyId: 'inputOptions',
                              propertyName: '_OPTION_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'custom-input-properties',
                              fields: [
                                 {
                                    propertyId: 'inputOptionsKey',
                                    propertyName: '_OPTION_KEY_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-input-properties-key'
                                 },
                                 {
                                    propertyId: 'inputOptionsValue',
                                    propertyName: '_OPTION_VALUE_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-input-properties-value'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Input'
                  },
                  {
                     name: 'Crossdata',
                     value: {
                        name: 'Crossdata',
                        icon: 'Crossdata',
                        className: 'CrossdataInputStep',
                        classPrettyName: 'Crossdata',
                        arity: [
                           'NullaryToNary'
                        ],
                        crossdataCatalog: true,
                        supportedEngines: [
                           'Batch'
                        ],
                        supportedDataRelations: [
                           'ValidData'
                        ],
                        description: 'Allows you to load data from external Data Sources using Crossdata',
                        properties: [
                           {
                              propertyId: 'query',
                              propertyName: '_QUERY_',
                              propertyType: 'textarea',
                              contentType: 'SQL',
                              width: 8,
                              required: true,
                              tooltip: 'Query to retrieve data from any temporal table available in the Crossdata catalog. The result will be stored in another temporal table named after the input step.',
                              placeholder: 'select * from tableName',
                              qa: 'fragment-details-crossdata-query'
                           },
                           {
                              propertyId: 'tlsEnabled',
                              propertyName: '_TLS_ENABLE_',
                              propertyType: 'boolean',
                              tooltip: 'Enable TLS protocol in order to connect to a secured Crossdata catalog using certificates',
                              'default': false,
                              qa: 'fragment-details-postgres-tls'
                           }
                        ]
                     },
                     stepType: 'Input'
                  },
                  {
                     name: 'Csv',
                     value: {
                        name: 'Csv',
                        icon: 'Csv',
                        className: 'CsvInputStep',
                        classPrettyName: 'Csv',
                        arity: [
                           'NullaryToNary'
                        ],
                        supportedEngines: [
                           'Batch'
                        ],
                        supportedDataRelations: [
                           'ValidData'
                        ],
                        description: 'Allows you to load data from CSV files',
                        properties: [
                           {
                              propertyId: 'path',
                              propertyName: '_PATH_',
                              propertyType: 'text',
                              required: true,
                              tooltip: 'Path to an CSV file located in a compatible filesystem (HDFS, S3, Azure, etc). If the file is located in the HDFS used in the tenant configuration one can specify the path omitting the connection details.',
                              qa: 'fragment-details-csv-path'
                           },
                           {
                              propertyId: 'header',
                              propertyName: '_HEADER_',
                              propertyType: 'boolean',
                              required: true,
                              'default': false,
                              tooltip: 'If checked, the first row must contain the header which will be used later to construct the data schema.',
                              qa: 'workflow-transformation-csv-header'
                           },
                           {
                              propertyId: 'delimiter',
                              propertyName: '_DELIMITER_',
                              propertyType: 'text',
                              tooltip: 'Delimiter character for input rows.',
                              'default': '{{{DEFAULT_DELIMITER}}}',
                              required: false,
                              qa: 'fragment-details-csv-delimiter'
                           },
                           {
                              propertyId: 'inputOptions',
                              propertyName: '_OPTION_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'custom-input-properties',
                              fields: [
                                 {
                                    propertyId: 'inputOptionsKey',
                                    propertyName: '_OPTION_KEY_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-input-properties-key'
                                 },
                                 {
                                    propertyId: 'inputOptionsValue',
                                    propertyName: '_OPTION_VALUE_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-input-properties-value'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Input'
                  },
                  {
                     name: 'Custom',
                     value: {
                        name: 'Custom',
                        icon: 'Custom',
                        className: 'CustomInputStep',
                        classPrettyName: 'Custom',
                        category: 'Custom',
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        supportedDataRelations: [
                           'ValidData'
                        ],
                        description: 'Custom input defined by user. User can extend the Sparta SDK with his own implementation of Input class',
                        properties: [
                           {
                              propertyId: 'customClassType',
                              propertyName: '_MODEL_TYPE_',
                              propertyType: 'text',
                              placeHolder: 'Custom',
                              required: true,
                              tooltip: 'Input class type that implements the input SDK step',
                              qa: 'custom-input-modelType'
                           },
                           {
                              propertyId: 'inputOptions',
                              propertyName: '_OPTION_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'custom-input-properties',
                              fields: [
                                 {
                                    propertyId: 'inputOptionsKey',
                                    propertyName: '_OPTION_KEY_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-input-properties-key'
                                 },
                                 {
                                    propertyId: 'inputOptionsValue',
                                    propertyName: '_OPTION_VALUE_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-input-properties-value'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Input'
                  },
                  {
                     name: 'CustomLite',
                     value: {
                        name: 'CustomLite',
                        icon: 'CustomLite',
                        className: 'CustomLiteInputStep',
                        classPrettyName: 'CustomLite',
                        category: 'Custom',
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        supportedDataRelations: [
                           'ValidData'
                        ],
                        description: 'Custom input defined by user. User can extend the Sparta SDK-Lite with his own implementation of Input class',
                        properties: [
                           {
                              propertyId: 'customLiteClassType',
                              propertyName: '_MODEL_TYPE_',
                              propertyType: 'text',
                              placeHolder: 'CustomLiteInput',
                              required: true,
                              tooltip: 'Input class type that implements the input SDK step',
                              qa: 'custom-input-modelType'
                           },
                           {
                              propertyId: 'inputOptions',
                              propertyName: '_OPTION_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'custom-input-properties',
                              fields: [
                                 {
                                    propertyId: 'inputOptionsKey',
                                    propertyName: '_OPTION_KEY_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-input-properties-key'
                                 },
                                 {
                                    propertyId: 'inputOptionsValue',
                                    propertyName: '_OPTION_VALUE_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-input-properties-value'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Input'
                  },
                  {
                     name: 'CustomLiteXD',
                     value: {
                        name: 'CustomLiteXD',
                        icon: 'CustomLiteXD',
                        className: 'CustomLiteXDInputStep',
                        classPrettyName: 'CustomLiteXD',
                        category: 'Custom',
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        supportedDataRelations: [
                           'ValidData'
                        ],
                        description: 'Custom input defined by user. User can extend the Sparta SDK-Lite with his own implementation of Input class',
                        properties: [
                           {
                              propertyId: 'customLiteClassType',
                              propertyName: '_MODEL_TYPE_',
                              propertyType: 'text',
                              placeHolder: 'CustomLiteXDInput',
                              required: true,
                              tooltip: 'Input class type that implements the input SDK step',
                              qa: 'custom-input-modelType'
                           },
                           {
                              propertyId: 'inputOptions',
                              propertyName: '_OPTION_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'custom-input-properties',
                              fields: [
                                 {
                                    propertyId: 'inputOptionsKey',
                                    propertyName: '_OPTION_KEY_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-input-properties-key'
                                 },
                                 {
                                    propertyId: 'inputOptionsValue',
                                    propertyName: '_OPTION_VALUE_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-input-properties-value'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Input'
                  },
                  {
                     name: 'Jdbc',
                     value: {
                        name: 'Jdbc',
                        icon: 'Jdbc',
                        className: 'JdbcInputStep',
                        classPrettyName: 'Jdbc',
                        arity: [
                           'NullaryToNary'
                        ],
                        supportedEngines: [
                           'Batch'
                        ],
                        supportedDataRelations: [
                           'ValidData'
                        ],
                        description: 'Allows you to load data from JDBC databases.',
                        properties: [
                           {
                              propertyId: 'url',
                              propertyName: '_JDBC_URL_',
                              propertyType: 'text',
                              'default': '{{{JDBC_URL}}}',
                              required: true,
                              tooltip: 'Url to connect to one relational database with JDBC.',
                              qa: 'fragment-details-jdbc-url'
                           },
                           {
                              propertyId: 'dbtable',
                              propertyName: '_TABLE_',
                              propertyType: 'text',
                              required: true,
                              tooltip: 'Table name to connect to.',
                              qa: 'fragment-details-jdbc-table'
                           },
                           {
                              propertyId: 'driver',
                              propertyName: '_JDBC_DRIVER_',
                              propertyType: 'text',
                              'default': '{{{JDBC_DRIVER}}}',
                              required: false,
                              tooltip: 'Driver class. This property has priority over the URL.',
                              qa: 'fragment-details-jdbc-driver'
                           },
                           {
                              propertyId: 'isolationLevel',
                              propertyName: '_ISOLATION_LEVEL_',
                              propertyType: 'select',
                              values: [
                                 {
                                    label: 'None',
                                    value: 'NONE'
                                 },
                                 {
                                    label: 'Read Uncommitted',
                                    value: 'READ_UNCOMMITTED'
                                 },
                                 {
                                    label: 'Read Committed',
                                    value: 'READ_COMMITTED'
                                 },
                                 {
                                    label: 'Repeatable Read',
                                    value: 'REPEATABLE_READ'
                                 },
                                 {
                                    label: 'Serializable',
                                    value: 'SERIALIZABLE'
                                 }
                              ],
                              'default': 'READ_UNCOMMITTED',
                              required: true,
                              tooltip: 'Sets the isolation level when persisting data in the database. This feature follows the standard used in JDBC transactions.',
                              qa: 'fragment-postgres-isolation-level'
                           },
                           {
                              propertyId: 'tlsEnabled',
                              propertyName: '_TLS_ENABLE_',
                              propertyType: 'boolean',
                              tooltip: 'Enable TLS protocol in order to connect to a secured JDBC database using certificates',
                              'default': false,
                              qa: 'fragment-details-jdbc-tls'
                           },
                           {
                              propertyId: 'inputOptions',
                              propertyName: '_OPTION_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'custom-input-properties',
                              fields: [
                                 {
                                    propertyId: 'inputOptionsKey',
                                    propertyName: '_OPTION_KEY_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-input-properties-key'
                                 },
                                 {
                                    propertyId: 'inputOptionsValue',
                                    propertyName: '_OPTION_VALUE_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-input-properties-value'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Input'
                  },
                  {
                     name: 'Json',
                     value: {
                        name: 'Json',
                        icon: 'Json',
                        className: 'JsonInputStep',
                        classPrettyName: 'Json',
                        arity: [
                           'NullaryToNary'
                        ],
                        supportedEngines: [
                           'Batch'
                        ],
                        supportedDataRelations: [
                           'ValidData'
                        ],
                        description: 'Allows you to load data from JSON files',
                        properties: [
                           {
                              propertyId: 'path',
                              propertyName: '_PATH_',
                              propertyType: 'text',
                              required: true,
                              tooltip: 'Path to an JSON file located in a compatible filesystem (HDFS, S3, Azure, etc). If the file is located in the HDFS used in the tenant configuration one can specify the path omitting the connection details.',
                              qa: 'fragment-details-json-path'
                           },
                           {
                              propertyId: 'inputOptions',
                              propertyName: '_OPTION_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'custom-input-properties',
                              fields: [
                                 {
                                    propertyId: 'inputOptionsKey',
                                    propertyName: '_OPTION_KEY_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-input-properties-key'
                                 },
                                 {
                                    propertyId: 'inputOptionsValue',
                                    propertyName: '_OPTION_VALUE_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-input-properties-value'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Input'
                  },
                  {
                     name: 'FileSystem',
                     value: {
                        name: 'FileSystem',
                        classPrettyName: 'FileSystem',
                        className: 'FileSystemInputStep',
                        icon: 'FileSystem',
                        arity: [
                           'NullaryToNary'
                        ],
                        supportedEngines: [
                           'Batch'
                        ],
                        supportedDataRelations: [
                           'ValidData'
                        ],
                        description: 'Input used to read data from files stored in a Hadoop-compatible monitored filesystem directory (that is: HDFS, S3, NFS, etc.)',
                        properties: [
                           {
                              propertyId: 'path',
                              propertyName: '_PATH_',
                              propertyType: 'text',
                              required: true,
                              tooltip: 'Path to an file located in a compatible filesystem (HDFS, S3, Azure, etc). If the file is located in the HDFS used in the tenant configuration one can specify the path omitting the connection details.',
                              qa: 'fragment-details-filesystem-path'
                           },
                           {
                              propertyId: 'outputField',
                              propertyName: '_OUTPUT_FIELD_',
                              propertyType: 'text',
                              'default': '{{{DEFAULT_OUTPUT_FIELD}}}',
                              required: true,
                              tooltip: 'Name assigned to the output field generated',
                              qa: 'fragment-details-websocket-outputField'
                           }
                        ]
                     },
                     stepType: 'Input'
                  },
                  {
                     name: 'Parquet',
                     value: {
                        name: 'Parquet',
                        icon: 'Parquet',
                        className: 'ParquetInputStep',
                        classPrettyName: 'Parquet',
                        arity: [
                           'NullaryToNary'
                        ],
                        supportedEngines: [
                           'Batch'
                        ],
                        supportedDataRelations: [
                           'ValidData'
                        ],
                        description: 'Allows you to load data from parquet files',
                        properties: [
                           {
                              propertyId: 'path',
                              propertyName: '_PATH_',
                              propertyType: 'text',
                              required: true,
                              tooltip: 'Path to an parquet file located in a compatible filesystem (HDFS, S3, Azure, etc). If the file is located in the HDFS used in the tenant configuration one can specify the path omitting the connection details.',
                              qa: 'fragment-details-parquet-path'
                           },
                           {
                              propertyId: 'inputOptions',
                              propertyName: '_OPTION_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'custom-input-properties',
                              fields: [
                                 {
                                    propertyId: 'inputOptionsKey',
                                    propertyName: '_OPTION_KEY_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-input-properties-key'
                                 },
                                 {
                                    propertyId: 'inputOptionsValue',
                                    propertyName: '_OPTION_VALUE_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-input-properties-value'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Input'
                  },
                  {
                     name: 'Test',
                     value: {
                        name: 'Test',
                        className: 'TestInputStep',
                        classPrettyName: 'Test',
                        arity: [
                           'NullaryToNary'
                        ],
                        supportedEngines: [
                           'Batch'
                        ],
                        supportedDataRelations: [
                           'ValidData'
                        ],
                        executionEngine: 'Batch',
                        description: 'Simulates a data generating events based on a fixed string or random numbers',
                        properties: [
                           {
                              propertyId: 'eventType',
                              propertyName: '_EVENT_TYPE_',
                              propertyType: 'select',
                              'default': 'STRING',
                              required: true,
                              values: [
                                 {
                                    label: 'String',
                                    value: 'STRING'
                                 },
                                 {
                                    label: 'Random number',
                                    value: 'RANDOM_NUMBER'
                                 }
                              ],
                              tooltip: 'Type of events generated: random number using the max integer specified or one string event.',
                              qa: 'fragment-details-test-eventType'
                           },
                           {
                              propertyId: 'event',
                              propertyName: '_EVENT_',
                              propertyType: 'textarea',
                              contentType: 'JSON',
                              visible: [
                                 [
                                    {
                                       propertyId: 'eventType',
                                       value: 'STRING'
                                    }
                                 ]
                              ],
                              required: true,
                              tooltip: 'Event that will be generated on each window. The user may specify a simple string or a JSON later to be transformed and parsed.',
                              qa: 'fragment-details-test-event'
                           },
                           {
                              propertyId: 'maxNumber',
                              propertyName: '_MAX_NUMBER_',
                              propertyType: 'text',
                              required: true,
                              visible: [
                                 [
                                    {
                                       propertyId: 'eventType',
                                       value: 'RANDOM_NUMBER'
                                    }
                                 ]
                              ],
                              tooltip: 'Max number for the random events generated.',
                              qa: 'fragment-details-test-maxNumber'
                           },
                           {
                              propertyId: 'numEvents',
                              propertyName: '_NUMBER_EVENTS_BATCH_',
                              propertyType: 'text',
                              validateValue: 'true',
                              required: true,
                              tooltip: 'Number of events generated',
                              qa: 'fragment-details-test-numEvents'
                           },
                           {
                              propertyId: 'outputField',
                              propertyName: '_OUTPUT_FIELD_',
                              propertyType: 'text',
                              'default': '{{{DEFAULT_OUTPUT_FIELD}}}',
                              required: true,
                              tooltip: 'Name assigned to the output field generated.',
                              qa: 'fragment-details-test-outputField'
                           }
                        ]
                     },
                     stepType: 'Input'
                  }
               ]
            },
            {
               name: 'Transformation',
               value: 'action',
               icon: 'icon-shuffle',
               subMenus: [
                  {
                     name: 'Column',
                     value: '',
                     subMenus: [
                        {
                           name: 'AddColumns',
                           value: {
                              name: 'AddColumns',
                              icon: 'AddColumns',
                              category: 'Column',
                              className: 'AddColumnsTransformStep',
                              classPrettyName: 'AddColumns',
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData'
                              ],
                              description: 'Add new columns',
                              properties: [
                                 {
                                    propertyId: 'columns',
                                    propertyName: '_COLUMNS_TO_ADD_',
                                    propertyType: 'list',
                                    required: true,
                                    tooltip: 'Schema definition for a new column or columns based on list of fields.',
                                    qa: 'fragment-details-addcolumn-fields',
                                    fields: [
                                       {
                                          propertyId: 'field',
                                          propertyName: '_NAME_',
                                          propertyType: 'text',
                                          required: true,
                                          width: 4,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'type',
                                          propertyName: '_TYPE_',
                                          propertyType: 'select',
                                          custom: true,
                                          required: true,
                                          values: [
                                             {
                                                label: 'String',
                                                value: 'string'
                                             },
                                             {
                                                label: 'Long',
                                                value: 'long'
                                             },
                                             {
                                                label: 'Float',
                                                value: 'float'
                                             },
                                             {
                                                label: 'Double',
                                                value: 'double'
                                             },
                                             {
                                                label: 'Integer',
                                                value: 'integer'
                                             },
                                             {
                                                label: 'Boolean',
                                                value: 'boolean'
                                             },
                                             {
                                                label: 'Binary',
                                                value: 'binary'
                                             },
                                             {
                                                label: 'Date',
                                                value: 'date'
                                             },
                                             {
                                                label: 'Timestamp',
                                                value: 'timestamp'
                                             }
                                          ],
                                          'default': 'string',
                                          width: 3,
                                          qa: 'fragment-details-field-type'
                                       },
                                       {
                                          propertyId: 'query',
                                          propertyName: '_VALUE_',
                                          propertyType: 'text',
                                          width: 4,
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    width: 12,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. If defined properly this would result in avoiding doing the schema calculation for all defined incoming inputs.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          'float': false,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 12,
                                          tooltip: 'Schema expressed in JSON/Spark format or from a valid sample from the input being defined.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'DropColumns',
                           value: {
                              name: 'DropColumns',
                              icon: 'DropColumns',
                              category: 'Column',
                              className: 'DropColumnsTransformStep',
                              classPrettyName: 'DropColumns',
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData'
                              ],
                              description: 'Drop input columns in order to avoid using them in next steps.',
                              properties: [
                                 {
                                    propertyId: 'schema.fields',
                                    propertyName: '_COLUMNS_TO_DROP_',
                                    propertyType: 'list',
                                    required: true,
                                    tooltip: 'List of columns to be dropped.',
                                    qa: 'fragment-details-dropcolumns-fields',
                                    fields: [
                                       {
                                          propertyId: 'name',
                                          propertyName: '_NAME_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          required: true,
                                          width: 6,
                                          qa: 'fragment-details-field-name'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'RenameColumns',
                           value: {
                              name: 'RenameColumns',
                              icon: 'RenameColumns',
                              category: 'Column',
                              className: 'RenameColumnTransformStep',
                              classPrettyName: 'RenameColumns',
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData'
                              ],
                              description: 'Rename input columns',
                              properties: [
                                 {
                                    propertyId: 'columns',
                                    propertyName: '_COLUMNS_TO_RENAME_',
                                    propertyType: 'list',
                                    required: true,
                                    tooltip: 'Schema definition from a list of fields.',
                                    qa: 'fragment-details-renamecolumn-fields',
                                    fields: [
                                       {
                                          propertyId: 'name',
                                          propertyName: '_NAME_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          required: true,
                                          width: 4,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'alias',
                                          propertyName: '_NEW_COLUMN_NAME_',
                                          propertyType: 'text',
                                          required: true,
                                          width: 4,
                                          qa: 'fragment-details-field-name'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        }
                     ]
                  },
                  {
                     name: 'Row',
                     value: '',
                     subMenus: [
                        {
                           name: 'Avro',
                           value: {
                              name: 'Avro',
                              icon: 'Avro',
                              category: 'Row',
                              className: 'AvroTransformStep',
                              classPrettyName: 'Avro',
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData',
                                 'DiscardedData'
                              ],
                              description: 'Allows you to convert the current dataset according to the AVRO binary format.',
                              properties: [
                                 {
                                    propertyId: 'inputField',
                                    propertyName: '_INPUT_FIELD_',
                                    propertyType: 'text',
                                    showSchemaFields: true,
                                    required: true,
                                    tooltip: 'Field that contains the AVRO string',
                                    qa: 'fragment-details-casting-fieldsString'
                                 },
                                 {
                                    propertyId: 'fieldsPreservationPolicy',
                                    propertyName: '_FIELDS_PRESERVATION_',
                                    required: true,
                                    propertyType: 'select',
                                    'default': 'REPLACE',
                                    values: [
                                       {
                                          label: 'Replacing input field',
                                          value: 'REPLACE'
                                       },
                                       {
                                          label: 'Appending extracted',
                                          value: 'APPEND'
                                       },
                                       {
                                          label: 'Keeping just extracted',
                                          value: 'JUST_EXTRACTED'
                                       }
                                    ],
                                    tooltip: 'The user might choose between 3 modes. If \'Replacing input field\' is chosen the resulting data will be saved in the position occupied by the input field. \'Appending extracted\' will add the data at the end of the row and lastly \'Keeping just extracted\' will only keep the transformed data discarding the received.',
                                    qa: 'workflow-transformation-avro-fieldsPreservationPolicy'
                                 },
                                 {
                                    propertyId: 'schema.provided',
                                    propertyName: '_DESERIALIZER_SCHEMA_',
                                    propertyType: 'textarea',
                                    contentType: 'JSON',
                                    width: 8,
                                    tooltip: 'Avro schema in JSON format.',
                                    required: false,
                                    qa: 'workflow-transformation-avro-schema-provided'
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    width: 12,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. If defined properly this would result in avoiding doing the schema calculation for all defined incoming inputs.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 6,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 12,
                                          tooltip: 'Schema expressed in JSON/Spark format or from a valid sample from the input being defined.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'whenRowError',
                                    propertyName: '_TRANSFORMATION_WHEN_ROW_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'RowError'
                                       },
                                       {
                                          label: 'Discard',
                                          value: 'RowDiscard'
                                       }
                                    ],
                                    qa: 'workflow-transformation-whenRowError'
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'Casting',
                           value: {
                              name: 'Casting',
                              icon: 'Casting',
                              category: 'Row',
                              className: 'CastingTransformStep',
                              classPrettyName: 'Casting',
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData',
                                 'DiscardedData'
                              ],
                              description: 'Casts input fields to other types. Advanced Spark types conversions are supported.',
                              properties: [
                                 {
                                    propertyId: 'outputFieldsFrom',
                                    propertyName: '_OUTPUT_FIELDS_FROM_',
                                    required: true,
                                    propertyType: 'select',
                                    'default': 'FIELDS',
                                    values: [
                                       {
                                          label: 'String',
                                          value: 'STRING'
                                       },
                                       {
                                          label: 'Fields',
                                          value: 'FIELDS'
                                       }
                                    ],
                                    tooltip: 'Output fields definition. The user may choose between providing a string in JSON or Spark format or using the field model definition.',
                                    qa: 'fragment-details-casting-outputFieldsFrom',
                                    classed: 'col-xs-8'
                                 },
                                 {
                                    propertyId: 'fieldsString',
                                    propertyName: '_FIELDS_DEFINITION_STRING_',
                                    propertyType: 'textarea',
                                    contentType: 'JSON',
                                    width: 12,
                                    visible: [
                                       [
                                          {
                                             propertyId: 'outputFieldsFrom',
                                             value: 'STRING'
                                          }
                                       ]
                                    ],
                                    required: true,
                                    tooltip: 'Schema definition in JSON or Spark format.',
                                    qa: 'fragment-details-casting-fieldsString',
                                    classed: 'col-xs-12'
                                 },
                                 {
                                    propertyId: 'fields',
                                    propertyName: '_FIELDS_DEFINITION_',
                                    propertyType: 'list',
                                    required: true,
                                    tooltip: 'Schema definition from list of fields.',
                                    qa: 'fragment-details-casting-fields',
                                    visible: [
                                       [
                                          {
                                             propertyId: 'outputFieldsFrom',
                                             value: 'FIELDS'
                                          }
                                       ]
                                    ],
                                    fields: [
                                       {
                                          propertyId: 'name',
                                          propertyName: '_NAME_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          required: true,
                                          width: 4,
                                          qa: 'fragment-details-field-name',
                                          classed: 'list-item col-sm-4'
                                       },
                                       {
                                          propertyId: 'type',
                                          propertyName: '_TYPE_',
                                          propertyType: 'select',
                                          custom: true,
                                          width: 4,
                                          required: true,
                                          values: [
                                             {
                                                label: 'String',
                                                value: 'string'
                                             },
                                             {
                                                label: 'Long',
                                                value: 'long'
                                             },
                                             {
                                                label: 'Float',
                                                value: 'float'
                                             },
                                             {
                                                label: 'Double',
                                                value: 'double'
                                             },
                                             {
                                                label: 'Integer',
                                                value: 'integer'
                                             },
                                             {
                                                label: 'Boolean',
                                                value: 'boolean'
                                             },
                                             {
                                                label: 'Binary',
                                                value: 'binary'
                                             },
                                             {
                                                label: 'Date',
                                                value: 'date'
                                             },
                                             {
                                                label: 'Timestamp',
                                                value: 'timestamp'
                                             },
                                             {
                                                label: 'Array(Double)',
                                                value: 'arraydouble'
                                             },
                                             {
                                                label: 'Array(String)',
                                                value: 'arraystring'
                                             },
                                             {
                                                label: 'Array(Long)',
                                                value: 'arraylong'
                                             },
                                             {
                                                label: 'Array(Integer)',
                                                value: 'arrayinteger'
                                             },
                                             {
                                                label: 'Array(Map(String, String))',
                                                value: 'arraymapstringstring'
                                             },
                                             {
                                                label: 'Map(String, Long)',
                                                value: 'mapstringlong'
                                             },
                                             {
                                                label: 'Map(String, Double)',
                                                value: 'mapstringdouble'
                                             },
                                             {
                                                label: 'Map(String, Integer)',
                                                value: 'mapstringint'
                                             },
                                             {
                                                label: 'Map(String, String)',
                                                value: 'mapstringstring'
                                             }
                                          ],
                                          'default': 'string',
                                          qa: 'fragment-details-field-type',
                                          classed: 'list-item col-sm-4'
                                       },
                                       {
                                          propertyId: 'nullable',
                                          propertyName: '_NULLABLE_',
                                          propertyType: 'boolean',
                                          'default': true,
                                          required: true,
                                          qa: 'fragment-details-field-nullable',
                                          classed: 'list-item check-column'
                                       }
                                    ],
                                    classed: 'col-xs-8'
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    showInputSteps: true,
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 6,
                                          'float': false,
                                          qa: 'fragment-details-field-name',
                                          classed: 'list-item col-sm-6'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 12,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query',
                                          classed: 'list-item col-sm-12'
                                       }
                                    ],
                                    classed: 'col-xs-8'
                                 },
                                 {
                                    propertyId: 'whenRowError',
                                    propertyName: '_TRANSFORMATION_WHEN_ROW_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'RowError'
                                       },
                                       {
                                          label: 'Discard',
                                          value: 'RowDiscard'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a row-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Discard\' is chosen, no error will be prompted and the row will be omitted.',
                                    qa: 'workflow-transformation-whenRowError',
                                    classed: 'col-xs-8'
                                 },
                                 {
                                    propertyId: 'whenFieldError',
                                    propertyName: '_TRANSFORMATION_WHEN_FIELD_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'FieldError'
                                       },
                                       {
                                          label: 'Null',
                                          value: 'Null'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a field-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Null\' is chosen, no error will be prompted and the field value will be set to null.',
                                    qa: 'workflow-transformation-whenFieldError',
                                    classed: 'col-xs-8'
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'Csv',
                           value: {
                              name: 'Csv',
                              icon: 'Csv',
                              category: 'Row',
                              className: 'CsvTransformStep',
                              classPrettyName: 'Csv',
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData',
                                 'DiscardedData'
                              ],
                              description: 'Parses an input field with CSV format and returns all the obtained new fields or selected ones.',
                              properties: [
                                 {
                                    propertyId: 'inputField',
                                    propertyName: '_INPUT_FIELD_',
                                    propertyType: 'text',
                                    showSchemaFields: true,
                                    required: true,
                                    tooltip: 'Field containing the CSV string.',
                                    qa: 'fragment-details-csv-fieldsString'
                                 },
                                 {
                                    propertyId: 'fieldsPreservationPolicy',
                                    propertyName: '_FIELDS_PRESERVATION_',
                                    required: true,
                                    propertyType: 'select',
                                    'default': 'REPLACE',
                                    values: [
                                       {
                                          label: 'Replacing input field',
                                          value: 'REPLACE'
                                       },
                                       {
                                          label: 'Appending extracted',
                                          value: 'APPEND'
                                       },
                                       {
                                          label: 'Keeping just extracted',
                                          value: 'JUST_EXTRACTED'
                                       }
                                    ],
                                    tooltip: 'If \'Replacing input field\' is chosen, the resulting data will be saved in the position occupied by the input field. \'Appending extracted\' will add the data at the end of the row while \'Keeping just extracted\' will only keep the transformed data discarding the received.',
                                    qa: 'workflow-transformation-csv-fieldsPreservationPolicy'
                                 },
                                 {
                                    propertyId: 'schema.inputMode',
                                    propertyName: '_INPUT_SCHEMA_FROM_',
                                    propertyType: 'select',
                                    required: true,
                                    values: [
                                       {
                                          label: 'Header',
                                          value: 'HEADER'
                                       },
                                       {
                                          label: 'Fields',
                                          value: 'FIELDS'
                                       },
                                       {
                                          label: 'Spark format',
                                          value: 'SPARKFORMAT'
                                       }
                                    ],
                                    'default': 'HEADER',
                                    tooltip: 'Specifies whether to generate the schema either by looking at the header or at the fields property or by reading a user-provided Spark format string.',
                                    qa: 'fragment-details-input-schema-type'
                                 },
                                 {
                                    propertyId: 'schema.header',
                                    propertyName: '_HEADER_',
                                    propertyType: 'textarea',
                                    contentType: 'JSON',
                                    width: 12,
                                    visible: [
                                       [
                                          {
                                             propertyId: 'schema.inputMode',
                                             value: 'HEADER'
                                          }
                                       ]
                                    ],
                                    required: true,
                                    tooltip: 'Header with the fields names',
                                    qa: 'fragment-details-csv-header'
                                 },
                                 {
                                    propertyId: 'headerRemoval',
                                    propertyName: '_HEADER_REMOVAL_',
                                    propertyType: 'boolean',
                                    required: true,
                                    'default': false,
                                    tooltip: 'If checked, the provided header will be removed from the input file',
                                    qa: 'workflow-transformation-json-header-removal'
                                 },
                                 {
                                    propertyId: 'schema.fields',
                                    propertyName: '_FIELDS_DEFINITION_',
                                    propertyType: 'list',
                                    visible: [
                                       [
                                          {
                                             propertyId: 'schema.inputMode',
                                             value: 'FIELDS'
                                          }
                                       ]
                                    ],
                                    required: true,
                                    tooltip: 'Schema definition from list of fields',
                                    qa: 'fragment-details-csv-fields',
                                    fields: [
                                       {
                                          propertyId: 'name',
                                          propertyName: '_NAME_',
                                          propertyType: 'text',
                                          required: true,
                                          width: 3,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'type',
                                          propertyName: '_TYPE_',
                                          propertyType: 'select',
                                          custom: true,
                                          required: true,
                                          width: 3,
                                          values: [
                                             {
                                                label: 'String',
                                                value: 'string'
                                             },
                                             {
                                                label: 'Long',
                                                value: 'long'
                                             },
                                             {
                                                label: 'Float',
                                                value: 'float'
                                             },
                                             {
                                                label: 'Double',
                                                value: 'double'
                                             },
                                             {
                                                label: 'Integer',
                                                value: 'integer'
                                             },
                                             {
                                                label: 'Boolean',
                                                value: 'boolean'
                                             },
                                             {
                                                label: 'Binary',
                                                value: 'binary'
                                             },
                                             {
                                                label: 'Date',
                                                value: 'date'
                                             },
                                             {
                                                label: 'Timestamp',
                                                value: 'timestamp'
                                             },
                                             {
                                                label: 'Array(Double)',
                                                value: 'arraydouble'
                                             },
                                             {
                                                label: 'Array(String)',
                                                value: 'arraystring'
                                             },
                                             {
                                                label: 'Array(Long)',
                                                value: 'arraylong'
                                             },
                                             {
                                                label: 'Array(Integer)',
                                                value: 'arrayinteger'
                                             },
                                             {
                                                label: 'Array(Map(String, String))',
                                                value: 'arraymapstringstring'
                                             },
                                             {
                                                label: 'Map(String, Long)',
                                                value: 'mapstringlong'
                                             },
                                             {
                                                label: 'Map(String, Double)',
                                                value: 'mapstringdouble'
                                             },
                                             {
                                                label: 'Map(String, Integer)',
                                                value: 'mapstringint'
                                             },
                                             {
                                                label: 'Map(String, String)',
                                                value: 'mapstringstring'
                                             }
                                          ],
                                          'default': 'string',
                                          qa: 'fragment-details-field-type'
                                       },
                                       {
                                          propertyId: 'nullable',
                                          propertyName: '_NULLABLE_',
                                          propertyType: 'boolean',
                                          'default': true,
                                          required: true,
                                          qa: 'fragment-details-field-nullable'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'schema.sparkSchema',
                                    propertyName: '_SPARK_SCHEMA_',
                                    propertyType: 'textarea',
                                    contentType: 'JSON',
                                    width: 12,
                                    visible: [
                                       [
                                          {
                                             propertyId: 'schema.inputMode',
                                             value: 'SPARKFORMAT'
                                          }
                                       ]
                                    ],
                                    required: true,
                                    tooltip: 'Spark schema defining all the fields in the CSV',
                                    qa: 'fragment-details-csv-spark-schema'
                                 },
                                 {
                                    propertyId: 'delimiter',
                                    propertyName: '_DELIMITER_',
                                    propertyType: 'text',
                                    required: true,
                                    'default': '{{{DEFAULT_DELIMITER}}}',
                                    tooltip: 'Delimiter character used to parse the CSV string',
                                    qa: 'fragment-details-csv-delimiter'
                                 },
                                 {
                                    propertyId: 'splitLimit',
                                    propertyName: '_SPLIT_LIMIT_',
                                    propertyType: 'text',
                                    required: true,
                                    'default': '-1',
                                    tooltip: 'Determines the number of results obtained from parsing. If set to -1 no limit will be specified.',
                                    qa: 'fragment-details-csv-splitLimit'
                                 },
                                 {
                                    propertyId: 'delimiterType',
                                    propertyName: '_DELIMITER_TYPE_',
                                    propertyType: 'select',
                                    'default': 'CHARACTER',
                                    required: true,
                                    values: [
                                       {
                                          label: 'Character',
                                          value: 'CHARACTER'
                                       },
                                       {
                                          label: 'Regex',
                                          value: 'REGEX'
                                       }
                                    ],
                                    qa: 'workflow-transformation-csv-delimiterType'
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'whenRowError',
                                    propertyName: '_TRANSFORMATION_WHEN_ROW_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'RowError'
                                       },
                                       {
                                          label: 'Discard',
                                          value: 'RowDiscard'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a row-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Discard\' is chosen, no error will be prompted and the row will be omitted.',
                                    qa: 'workflow-transformation-whenRowError'
                                 },
                                 {
                                    propertyId: 'whenFieldError',
                                    propertyName: '_TRANSFORMATION_WHEN_FIELD_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'FieldError'
                                       },
                                       {
                                          label: 'Null',
                                          value: 'Null'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a field-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Null\' is chosen, no error will be prompted and the field value will be set to null.',
                                    qa: 'workflow-transformation-whenFieldError'
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'Datetime',
                           value: {
                              name: 'Datetime',
                              icon: 'Datetime',
                              category: 'Row',
                              className: 'DateTimeTransformStep',
                              classPrettyName: 'Datetime',
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedDataRelations: [
                                 'ValidData',
                                 'DiscardedData'
                              ],
                              description: 'It can either parse a list of input fields or create fields according to a given time format. Depending on the fieldsPreservationPolicy chosen by the user, either all the obtained new fields or just the selected ones will be returned.',
                              properties: [
                                 {
                                    propertyId: 'fieldsDatetime',
                                    propertyName: '_FIELDS_DATETIME_DEFINITION_',
                                    propertyType: 'list',
                                    complexForm: true,
                                    required: true,
                                    tooltip: ' Specify a list of fields to format into Datetime',
                                    qa: 'fragment-details-datetime-fields',
                                    fields: [
                                       {
                                          propertyId: 'formatFrom',
                                          propertyName: '_FORMAT_FROM_',
                                          propertyType: 'select',
                                          width: 4,
                                          qa: 'fragment-details-datetime-field-formatFrom',
                                          values: [
                                             {
                                                label: 'Auto Generated',
                                                value: 'AUTOGENERATED'
                                             },
                                             {
                                                label: 'Standard',
                                                value: 'STANDARD'
                                             },
                                             {
                                                label: 'User',
                                                value: 'USER'
                                             }
                                          ],
                                          'default': 'AUTOGENERATED',
                                          required: true
                                       },
                                       {
                                          propertyId: 'inputField',
                                          propertyName: '_INPUT_FIELD_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          width: 4,
                                          required: true,
                                          tooltip: 'Field belonging to a previous incoming transformation from which the value will be extracted',
                                          qa: 'fragment-details-datetime-field-name',
                                          visibleOR: [
                                             [
                                                {
                                                   propertyId: 'formatFrom',
                                                   value: 'USER'
                                                },
                                                {
                                                   propertyId: 'formatFrom',
                                                   value: 'STANDARD'
                                                }
                                             ]
                                          ]
                                       },
                                       {
                                          propertyId: 'userFormat',
                                          propertyName: '_USER_FORMAT_',
                                          propertyType: 'text',
                                          width: 4,
                                          required: false,
                                          visible: [
                                             [
                                                {
                                                   propertyId: 'formatFrom',
                                                   value: 'USER'
                                                }
                                             ]
                                          ],
                                          tooltip: 'Custom datetime format that will be applied to the input field specified previously.',
                                          qa: 'fragment-details-datetime-field-userFormat'
                                       },
                                       {
                                          propertyId: 'standardFormat',
                                          propertyName: '_STANDARD_FORMAT_',
                                          propertyType: 'select',
                                          'float': false,
                                          width: 4,
                                          tooltip: 'Standard date time format used to parse the input field.',
                                          qa: 'fragment-details-datetime-field-standardFormat',
                                          values: [
                                             {
                                                label: 'Unix',
                                                value: 'unix'
                                             },
                                             {
                                                label: 'Unix millis',
                                                value: 'unixMillis'
                                             },
                                             {
                                                label: 'yyyy-MM-dd HH:mm:ss',
                                                value: 'hive'
                                             },
                                             {
                                                label: 'yyyyMMdd',
                                                value: 'basicDate'
                                             },
                                             {
                                                label: 'yyyyMMdd\'T\'HHmmss.SSSZ',
                                                value: 'basicDateTime'
                                             },
                                             {
                                                label: 'yyyyMMdd\'T\'HHmmssZ',
                                                value: 'basicDateTimeNoMillis'
                                             },
                                             {
                                                label: 'yyyyDDD',
                                                value: 'basicOrdinalDate'
                                             },
                                             {
                                                label: 'yyyyDDD\'T\'HHmmss.SSSZ',
                                                value: 'basicOrdinalDateTime'
                                             },
                                             {
                                                label: 'yyyyDDD\'T\'HHmmssZ',
                                                value: 'basicOrdinalDateTimeNoMillis'
                                             },
                                             {
                                                label: 'HHmmss.SSSZ',
                                                value: 'basicTime'
                                             },
                                             {
                                                label: 'HHmmssZ',
                                                value: 'basicTimeNoMillis'
                                             },
                                             {
                                                label: '\'T\'HHmmss.SSSZ',
                                                value: 'basicTTime'
                                             },
                                             {
                                                label: '\'T\'HHmmssZ',
                                                value: 'basicTTimeNoMillis'
                                             },
                                             {
                                                label: 'yyyy-MM-dd',
                                                value: 'date'
                                             },
                                             {
                                                label: 'yyyy-MM-dd\'T\'HH:mm:ss.SSS',
                                                value: 'dateHourMinuteSecondMillis'
                                             },
                                             {
                                                label: 'yyyy-MM-dd\'T\'HH:mm:ss.SSSZZ',
                                                value: 'dateTime'
                                             },
                                             {
                                                label: 'yyyy-MM-dd\'T\'HH:mm:ssZZ',
                                                value: 'dateTimeNoMillis'
                                             },
                                             {
                                                label: 'HH:mm:ss.SSS',
                                                value: 'hourMinuteSecondFraction'
                                             },
                                             {
                                                label: 'yyyy-DDD',
                                                value: 'ordinalDate'
                                             },
                                             {
                                                label: 'yyyy-DDD\'T\'HH:mm:ssZZ',
                                                value: 'ordinalDateTimeNoMillis'
                                             },
                                             {
                                                label: 'HH:mm:ss.SSSZZ',
                                                value: 'time'
                                             },
                                             {
                                                label: '\'T\'HH:mm:ss.SSSZZ',
                                                value: 'tTime'
                                             },
                                             {
                                                label: '\'T\'HH:mm:ssZZ',
                                                value: 'tTimeNoMillis'
                                             }
                                          ],
                                          visible: [
                                             [
                                                {
                                                   propertyId: 'formatFrom',
                                                   value: 'STANDARD'
                                                }
                                             ]
                                          ],
                                          required: false
                                       },
                                       {
                                          propertyId: 'localeTime',
                                          propertyName: '_LOCALE_TIME_',
                                          propertyType: 'select',
                                          width: 4,
                                          required: true,
                                          'float': false,
                                          'default': 'ENGLISH',
                                          tooltip: 'Apply a Locale (e.g. EN, ES, IT, etc) to the specified format.',
                                          qa: 'fragment-details-datetime-field-localeTime',
                                          values: [
                                             {
                                                label: 'ENGLISH',
                                                value: 'ENGLISH'
                                             },
                                             {
                                                label: 'SPANISH',
                                                value: 'SPANISH'
                                             },
                                             {
                                                label: 'ITALIAN',
                                                value: 'ITALIAN'
                                             },
                                             {
                                                label: 'PORTUGUESE',
                                                value: 'PORTUGUESE'
                                             },
                                             {
                                                label: 'FRENCH',
                                                value: 'FRENCH'
                                             },
                                             {
                                                label: 'GERMAN',
                                                value: 'GERMAN'
                                             },
                                             {
                                                label: 'JAPANESE',
                                                value: 'JAPANESE'
                                             },
                                             {
                                                label: 'KOREAN',
                                                value: 'KOREAN'
                                             },
                                             {
                                                label: 'CHINESE',
                                                value: 'CHINESE'
                                             },
                                             {
                                                label: 'GREEK',
                                                value: 'GREEK'
                                             },
                                             {
                                                label: 'RUSSIAN',
                                                value: 'RUSSIAN'
                                             }
                                          ]
                                       },
                                       {
                                          propertyId: 'granularityNumber',
                                          propertyName: '_GRANULARITY_NUMBER_',
                                          propertyType: 'text',
                                          width: 4,
                                          position: 'left',
                                          required: false,
                                          regexp: '^[0-9]*$',
                                          validateValue: 'true',
                                          tooltip: 'Specify a value for the granularity time unit (when the time unit ends with (s)).',
                                          qa: 'fragment-details-datetime-field-granularityNumber'
                                       },
                                       {
                                          propertyId: 'granularityTime',
                                          propertyName: '_GRANULARITY_TIME_',
                                          propertyType: 'select',
                                          width: 4,
                                          required: false,
                                          'float': false,
                                          qa: 'fragment-details-datetime-field-granularityTime',
                                          values: [
                                             {
                                                label: 'Millisecond(s)',
                                                value: 'ms'
                                             },
                                             {
                                                label: 'Second(s)',
                                                value: 's'
                                             },
                                             {
                                                label: 'Minute(s)',
                                                value: 'm'
                                             },
                                             {
                                                label: 'Hour(s)',
                                                value: 'h'
                                             },
                                             {
                                                label: 'Day(s)',
                                                value: 'd'
                                             },
                                             {
                                                label: 'Week(s)',
                                                value: 'w'
                                             },
                                             {
                                                label: 'Millisecond',
                                                value: 'millisecond'
                                             },
                                             {
                                                label: 'Second',
                                                value: 'second'
                                             },
                                             {
                                                label: 'Minute',
                                                value: 'minute'
                                             },
                                             {
                                                label: 'Hour',
                                                value: 'hour'
                                             },
                                             {
                                                label: 'Day',
                                                value: 'day'
                                             },
                                             {
                                                label: 'Week',
                                                value: 'week'
                                             },
                                             {
                                                label: 'Month',
                                                value: 'month'
                                             },
                                             {
                                                label: 'Year',
                                                value: 'year'
                                             }
                                          ],
                                          tooltip: 'Allows the user to specify the time unit granularity: if it ends with (s) it is required for the user to specify a value. E.g. for each minute select Minute and not Minute(s).',
                                          'default': 'millisecond'
                                       },
                                       {
                                          propertyId: 'fieldsPreservationPolicy',
                                          propertyName: '_FIELDS_PRESERVATION_',
                                          required: true,
                                          propertyType: 'select',
                                          'default': 'APPEND',
                                          width: 4,
                                          'float': false,
                                          values: [
                                             {
                                                label: 'Replacing input field',
                                                value: 'REPLACE'
                                             },
                                             {
                                                label: 'Appending extracted',
                                                value: 'APPEND'
                                             }
                                          ],
                                          tooltip: 'The user might choose between 2 modes. If \'Replacing input field\' is chosen the resulting data will be saved in the position occupied by the input field. \'Appending extracted\' will add the data at the end of the row.',
                                          qa: 'fragment-details-datetime-field-fieldsPreservationPolicy'
                                       },
                                       {
                                          propertyId: 'outputFieldName',
                                          propertyName: '_OUTPUT_NAME_',
                                          propertyType: 'text',
                                          required: true,
                                          width: 4,
                                          tooltip: 'Name given to the resulting datetime transformation.',
                                          qa: 'fragment-details-datetime-field-outputFieldName'
                                       },
                                       {
                                          propertyId: 'outputFieldType',
                                          propertyName: '_DATE_TYPE_',
                                          propertyType: 'select',
                                          custom: true,
                                          width: 4,
                                          required: true,
                                          values: [
                                             {
                                                label: 'String',
                                                value: 'string'
                                             },
                                             {
                                                label: 'Long',
                                                value: 'long'
                                             },
                                             {
                                                label: 'Date',
                                                value: 'date'
                                             },
                                             {
                                                label: 'Timestamp',
                                                value: 'timestamp'
                                             }
                                          ],
                                          'default': 'string',
                                          tooltip: 'Type given to the resulting datetime transformation.',
                                          qa: 'fragment-details-datetime-field-type'
                                       },
                                       {
                                          propertyId: 'nullable',
                                          propertyName: '_NULLABLE_',
                                          propertyType: 'boolean',
                                          'default': true,
                                          required: true,
                                          'float': false,
                                          tooltip: 'Indicates if values of this field can be `null` values.',
                                          qa: 'fragment-details-datetime-field-nullable'
                                       },
                                       {
                                          propertyId: 'outputFormatFrom',
                                          propertyName: '_OUTPUT_FORMAT_FROM_',
                                          propertyType: 'select',
                                          width: 4,
                                          qa: 'fragment-details-datetime-field-outputFormatFrom',
                                          values: [
                                             {
                                                label: 'Default Timestamp',
                                                value: 'DEFAULT'
                                             },
                                             {
                                                label: 'Standard',
                                                value: 'STANDARD'
                                             },
                                             {
                                                label: 'User',
                                                value: 'USER'
                                             }
                                          ],
                                          tooltip: 'Format given to the resulting datetime transformation.',
                                          'default': 'DEFAULT',
                                          required: true
                                       },
                                       {
                                          propertyId: 'outputUserFormat',
                                          propertyName: '_OUTPUT_USER_FORMAT_',
                                          propertyType: 'text',
                                          width: 4,
                                          required: false,
                                          visible: [
                                             [
                                                {
                                                   propertyId: 'outputFormatFrom',
                                                   value: 'USER'
                                                }
                                             ]
                                          ],
                                          tooltip: 'User specified format given to the resulting datetime transformation.',
                                          qa: 'fragment-details-datetime-field-outputUserFormat'
                                       },
                                       {
                                          propertyId: 'outputStandardFormat',
                                          propertyName: '_OUTPUT_STANDARD_FORMAT_',
                                          propertyType: 'select',
                                          width: 4,
                                          qa: 'fragment-details-datetime-field-outputStandardFormat',
                                          values: [
                                             {
                                                label: 'yyyy-MM-dd HH:mm:ss',
                                                value: 'yyyy-MM-dd HH:mm:ss'
                                             },
                                             {
                                                label: 'yyyyMMdd',
                                                value: 'yyyyMMdd'
                                             },
                                             {
                                                label: 'yyyyMMdd\'T\'HHmmss.SSSZ',
                                                value: 'yyyyMMdd\'T\'HHmmss.SSSZ'
                                             },
                                             {
                                                label: 'yyyyMMdd\'T\'HHmmssZ',
                                                value: 'yyyyMMdd\'T\'HHmmssZ'
                                             },
                                             {
                                                label: 'yyyyDDD',
                                                value: 'yyyyDDD'
                                             },
                                             {
                                                label: 'yyyyDDD\'T\'HHmmss.SSSZ',
                                                value: 'yyyyDDD\'T\'HHmmss.SSSZ'
                                             },
                                             {
                                                label: 'yyyyDDD\'T\'HHmmssZ',
                                                value: 'yyyyDDD\'T\'HHmmssZ'
                                             },
                                             {
                                                label: 'HHmmss.SSSZ',
                                                value: 'HHmmss.SSSZ'
                                             },
                                             {
                                                label: 'HHmmssZ',
                                                value: 'HHmmssZ'
                                             },
                                             {
                                                label: '\'T\'HHmmss.SSSZ',
                                                value: '\'T\'HHmmss.SSSZ'
                                             },
                                             {
                                                label: 'yyyy-MM-dd\'T\'HH:mm:ss.SSS',
                                                value: 'yyyy-MM-dd\'T\'HH:mm:ss.SSS'
                                             },
                                             {
                                                label: 'yyyy-MM-dd\'T\'HH:mm:ss.SSSZZ',
                                                value: 'yyyy-MM-dd\'T\'HH:mm:ss.SSSZZ'
                                             },
                                             {
                                                label: 'yyyy-MM-dd\'T\'HH:mm:ssZZ',
                                                value: 'yyyy-MM-dd\'T\'HH:mm:ssZZ'
                                             },
                                             {
                                                label: 'HH:mm:ss.SSS',
                                                value: 'HH:mm:ss.SSS'
                                             },
                                             {
                                                label: 'yyyy-DDD',
                                                value: 'yyyy-DDD'
                                             },
                                             {
                                                label: 'yyyy-DDD\'T\'HH:mm:ssZZ',
                                                value: 'yyyy-DDD\'T\'HH:mm:ssZZ'
                                             },
                                             {
                                                label: 'HH:mm:ss.SSSZZ',
                                                value: 'HH:mm:ss.SSSZZ'
                                             },
                                             {
                                                label: '\'T\'HH:mm:ss.SSSZZ',
                                                value: '\'T\'HH:mm:ss.SSSZZ'
                                             },
                                             {
                                                label: '\'T\'HH:mm:ssZZ',
                                                value: '\'T\'HH:mm:ssZZ'
                                             }
                                          ],
                                          visible: [
                                             [
                                                {
                                                   propertyId: 'outputFormatFrom',
                                                   value: 'STANDARD'
                                                }
                                             ]
                                          ],
                                          required: false
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'whenRowError',
                                    propertyName: '_TRANSFORMATION_WHEN_ROW_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'RowError'
                                       },
                                       {
                                          label: 'Discard',
                                          value: 'RowDiscard'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a row-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Discard\' is chosen, no error will be prompted and the row will be omitted.',
                                    qa: 'workflow-transformation-whenRowError'
                                 },
                                 {
                                    propertyId: 'whenFieldError',
                                    propertyName: '_TRANSFORMATION_WHEN_FIELD_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'FieldError'
                                       },
                                       {
                                          label: 'Null',
                                          value: 'Null'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a field-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Null\' is chosen, no error will be prompted and the field value will be set to null.',
                                    qa: 'workflow-transformation-whenFieldError'
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'DropNulls',
                           value: {
                              name: 'DropNulls',
                              icon: 'DropNulls',
                              category: 'Row',
                              className: 'DropNullsTransformStep',
                              classPrettyName: 'DropNulls',
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData',
                                 'DiscardedData'
                              ],
                              description: 'Clean/Drop rows that contains null or NaN values in their columns',
                              properties: [
                                 {
                                    propertyId: 'cleanMode',
                                    propertyName: '_CLEAN_MODE_',
                                    required: true,
                                    propertyType: 'select',
                                    'default': 'ANY',
                                    values: [
                                       {
                                          label: 'Any',
                                          value: 'ANY'
                                       },
                                       {
                                          label: 'All',
                                          value: 'ALL'
                                       }
                                    ],
                                    tooltip: 'If \'Any\' is selected, the transformation will drop rows containing null or NaN value in at least one column. If \'All\' is chosen, only the rows containing  null or NaN values for all the specified columns will be dropped.',
                                    qa: 'fragment-details-cleanNulls-cleanMode'
                                 },
                                 {
                                    propertyId: 'columns',
                                    propertyName: '_COLUMNS_',
                                    width: 12,
                                    propertyType: 'list',
                                    required: false,
                                    tooltip: 'Columns to be checked. If this section is left blank, the transformation will check for null values in all the fields composing the input data.',
                                    qa: 'fragment-details-cleanNulls-columns',
                                    fields: [
                                       {
                                          propertyId: 'name',
                                          propertyName: '_COLUMN_NAME_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          width: 8,
                                          required: true,
                                          tooltip: 'Name of the column',
                                          qa: 'fragment-details-cleanNulls-columns-name'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'whenRowError',
                                    propertyName: '_TRANSFORMATION_WHEN_ROW_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'RowError'
                                       },
                                       {
                                          label: 'Discard',
                                          value: 'RowDiscard'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a row-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Discard\' is chosen, no error will be prompted and the row will be omitted.',
                                    qa: 'workflow-transformation-whenRowError'
                                 },
                                 {
                                    propertyId: 'whenFieldError',
                                    propertyName: '_TRANSFORMATION_WHEN_FIELD_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'FieldError'
                                       },
                                       {
                                          label: 'Null',
                                          value: 'Null'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a field-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Null\' is chosen, no error will be prompted and the field value will be set to null.',
                                    qa: 'workflow-transformation-whenFieldError'
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'InitNulls',
                           value: {
                              name: 'InitNulls',
                              icon: 'InitNulls',
                              category: 'Row',
                              className: 'InitNullsTransformStep',
                              classPrettyName: 'InitNulls',
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData',
                                 'DiscardedData'
                              ],
                              description: 'Initialize nulls to default values.',
                              properties: [
                                 {
                                    propertyId: 'defaultValueToColumn',
                                    propertyName: '_DEFAULT_COLUMN_VALUES_',
                                    propertyType: 'list',
                                    required: false,
                                    tooltip: 'Default values assigned to the specified columns.',
                                    qa: 'fragment-details-initNulls-defaultValueToColumn',
                                    fields: [
                                       {
                                          propertyId: 'columnName',
                                          propertyName: '_COLUMN_NAME_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          width: 3,
                                          required: true,
                                          tooltip: 'Name of the column.',
                                          qa: 'fragment-details-cleanNulls-columns-name'
                                       },
                                       {
                                          propertyId: 'value',
                                          propertyName: '_COLUMN_VALUE_',
                                          propertyType: 'text',
                                          width: 3,
                                          required: true,
                                          tooltip: 'Value given to the column.',
                                          qa: 'fragment-details-cleanNulls-columns-name'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'defaultValueToType',
                                    propertyName: '_DEFAULT_TYPE_VALUES_',
                                    propertyType: 'list',
                                    required: false,
                                    tooltip: 'Default values assigned to any of the columns whose types matches the specified type',
                                    qa: 'fragment-details-initNulls-defaultValueToType',
                                    fields: [
                                       {
                                          propertyId: 'value',
                                          propertyName: '_COLUMN_VALUE_',
                                          propertyType: 'text',
                                          width: 3,
                                          required: true,
                                          tooltip: 'Value of the column',
                                          qa: 'fragment-details-cleanNulls-columns-name'
                                       },
                                       {
                                          propertyId: 'type',
                                          propertyName: '_TYPE_',
                                          propertyType: 'select',
                                          width: 3,
                                          required: true,
                                          'default': 'string',
                                          values: [
                                             {
                                                label: 'String',
                                                value: 'string'
                                             },
                                             {
                                                label: 'Long',
                                                value: 'long'
                                             },
                                             {
                                                label: 'Float',
                                                value: 'float'
                                             },
                                             {
                                                label: 'Double',
                                                value: 'double'
                                             },
                                             {
                                                label: 'Integer',
                                                value: 'integer'
                                             },
                                             {
                                                label: 'Boolean',
                                                value: 'boolean'
                                             },
                                             {
                                                label: 'Binary',
                                                value: 'binary'
                                             },
                                             {
                                                label: 'Date',
                                                value: 'date'
                                             },
                                             {
                                                label: 'Timestamp',
                                                value: 'timestamp'
                                             }
                                          ],
                                          qa: 'fragment-details-field-type'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'whenRowError',
                                    propertyName: '_TRANSFORMATION_WHEN_ROW_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'RowError'
                                       },
                                       {
                                          label: 'Discard',
                                          value: 'RowDiscard'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a row-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Discard\' is chosen, no error will be prompted and the row will be omitted.',
                                    qa: 'workflow-transformation-whenRowError'
                                 },
                                 {
                                    propertyId: 'whenFieldError',
                                    propertyName: '_TRANSFORMATION_WHEN_FIELD_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'FieldError'
                                       },
                                       {
                                          label: 'Null',
                                          value: 'Null'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a field-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Null\' is chosen, no error will be prompted and the field value will be set to null.',
                                    qa: 'workflow-transformation-whenFieldError'
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'JsonPath',
                           value: {
                              name: 'JsonPath',
                              icon: 'JsonPath',
                              category: 'Row',
                              className: 'JsonPathTransformStep',
                              classPrettyName: 'JsonPath',
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData',
                                 'DiscardedData'
                              ],
                              arity: [
                                 'UnaryToNary'
                              ],
                              description: 'Parses a string with JSON format using a list of defined of typed columns',
                              properties: [
                                 {
                                    propertyId: 'inputField',
                                    propertyName: '_INPUT_FIELD_',
                                    propertyType: 'text',
                                    showSchemaFields: true,
                                    required: true,
                                    tooltip: 'Field containing the JSON string',
                                    qa: 'fragment-details-casting-fieldsString'
                                 },
                                 {
                                    propertyId: 'fieldsPreservationPolicy',
                                    propertyName: '_FIELDS_PRESERVATION_',
                                    required: true,
                                    propertyType: 'select',
                                    'default': 'REPLACE',
                                    values: [
                                       {
                                          label: 'Replacing input field',
                                          value: 'REPLACE'
                                       },
                                       {
                                          label: 'Appending extracted',
                                          value: 'APPEND'
                                       },
                                       {
                                          label: 'Keeping just extracted',
                                          value: 'JUST_EXTRACTED'
                                       }
                                    ],
                                    tooltip: 'The user might choose between 3 modes. If \'Replacing input field\' is chosen the resulting data will be saved in the position occupied by the input field. \'Appending extracted\' will add the data at the end of the row and lastly \'Keeping just extracted\' will only keep the transformed data discarding the received.',
                                    qa: 'workflow-transformation-jsonpath-fieldsPreservationPolicy'
                                 },
                                 {
                                    propertyId: 'queries',
                                    propertyName: '_QUERIES_',
                                    propertyType: 'list',
                                    required: true,
                                    tooltip: 'Schema definition from list of typed fields.',
                                    qa: 'fragment-details-casting-fields',
                                    fields: [
                                       {
                                          propertyId: 'field',
                                          propertyName: '_FIELD_',
                                          propertyType: 'text',
                                          required: true,
                                          width: 2,
                                          tooltip: 'Field name given to the parsed field. It\'s not a requirement to match the name found in the input string.',
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'query',
                                          propertyName: '_QUERY_',
                                          propertyType: 'text',
                                          width: 3,
                                          required: true,
                                          tooltip: 'Query used to search in the input string. The syntax used is as follows: \'$.field / $.field.object[*].field\'. For further information, please refer to the Jayway JsonPath documentation.',
                                          qa: 'fragment-details-field-query'
                                       },
                                       {
                                          propertyId: 'type',
                                          propertyName: '_TYPE_',
                                          propertyType: 'select',
                                          custom: true,
                                          required: true,
                                          values: [
                                             {
                                                label: 'String',
                                                value: 'string'
                                             },
                                             {
                                                label: 'Long',
                                                value: 'long'
                                             },
                                             {
                                                label: 'Float',
                                                value: 'float'
                                             },
                                             {
                                                label: 'Double',
                                                value: 'double'
                                             },
                                             {
                                                label: 'Integer',
                                                value: 'integer'
                                             },
                                             {
                                                label: 'Boolean',
                                                value: 'boolean'
                                             },
                                             {
                                                label: 'Binary',
                                                value: 'binary'
                                             },
                                             {
                                                label: 'Date',
                                                value: 'date'
                                             },
                                             {
                                                label: 'Timestamp',
                                                value: 'timestamp'
                                             },
                                             {
                                                label: 'Array(Double)',
                                                value: 'arraydouble'
                                             },
                                             {
                                                label: 'Array(String)',
                                                value: 'arraystring'
                                             },
                                             {
                                                label: 'Array(Long)',
                                                value: 'arraylong'
                                             },
                                             {
                                                label: 'Array(Integer)',
                                                value: 'arrayinteger'
                                             },
                                             {
                                                label: 'Array(Map(String, String))',
                                                value: 'arraymapstringstring'
                                             },
                                             {
                                                label: 'Map(String, Long)',
                                                value: 'mapstringlong'
                                             },
                                             {
                                                label: 'Map(String, Double)',
                                                value: 'mapstringdouble'
                                             },
                                             {
                                                label: 'Map(String, Integer)',
                                                value: 'mapstringint'
                                             },
                                             {
                                                label: 'Map(String, String)',
                                                value: 'mapstringstring'
                                             }
                                          ],
                                          'default': 'string',
                                          width: 3,
                                          qa: 'fragment-details-field-type'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    width: 8,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. If defined properly this would result in avoiding doing the schema calculation for all defined incoming inputs.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Schema expressed in JSON/Spark format or from a valid sample from the input being defined.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'supportNullValues',
                                    propertyName: '_SUPPORT_NULL_VALUES_',
                                    propertyType: 'boolean',
                                    'default': true,
                                    tooltip: 'If checked, fields with null values will be supported. Otherwise, an error will be generated and the policy chosen for error handling will be applied.',
                                    qa: 'workflow-transformation-json-supportNullValues'
                                 },
                                 {
                                    propertyId: 'whenRowError',
                                    propertyName: '_TRANSFORMATION_WHEN_ROW_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'RowError'
                                       },
                                       {
                                          label: 'Discard',
                                          value: 'RowDiscard'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a row-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Discard\' is chosen, no error will be prompted and the row will be omitted.',
                                    qa: 'workflow-transformation-whenRowError'
                                 },
                                 {
                                    propertyId: 'whenFieldError',
                                    propertyName: '_TRANSFORMATION_WHEN_FIELD_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'FieldError'
                                       },
                                       {
                                          label: 'Null',
                                          value: 'Null'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a field-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Null\' is chosen, no error will be prompted and the field value will be set to null.',
                                    qa: 'workflow-transformation-whenFieldError'
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'Json',
                           value: {
                              name: 'Json',
                              icon: 'Json',
                              category: 'Row',
                              className: 'JsonTransformStep',
                              classPrettyName: 'Json',
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData',
                                 'DiscardedData'
                              ],
                              description: 'Parses an input field in text format to row',
                              properties: [
                                 {
                                    propertyId: 'inputField',
                                    propertyName: '_INPUT_FIELD_',
                                    propertyType: 'text',
                                    showSchemaFields: true,
                                    required: true,
                                    tooltip: 'Field containing the JSON string',
                                    qa: 'fragment-details-casting-fieldsString'
                                 },
                                 {
                                    propertyId: 'fieldsPreservationPolicy',
                                    propertyName: '_FIELDS_PRESERVATION_',
                                    required: true,
                                    propertyType: 'select',
                                    'default': 'REPLACE',
                                    values: [
                                       {
                                          label: 'Replacing input field',
                                          value: 'REPLACE'
                                       },
                                       {
                                          label: 'Appending extracted',
                                          value: 'APPEND'
                                       },
                                       {
                                          label: 'Keeping just extracted',
                                          value: 'JUST_EXTRACTED'
                                       }
                                    ],
                                    tooltip: 'The user might choose between 3 modes. If \'Replacing input field\' is chosen the resulting data will be saved in the position occupied by the input field. \'Appending extracted\' will add the data at the end of the row and lastly \'Keeping just extracted\' will only keep the transformed data discarding the received.',
                                    qa: 'workflow-transformation-json-fieldsPreservationPolicy'
                                 },
                                 {
                                    propertyId: 'schema.fromRow',
                                    propertyName: '_SCHEMA_FROM_ROW_',
                                    propertyType: 'boolean',
                                    required: true,
                                    'default': true,
                                    tooltip: 'If checked, the row schema will be automatically inferred from its content.',
                                    qa: 'workflow-transformation-json-schema-fromRow'
                                 },
                                 {
                                    propertyId: 'schema.inputMode',
                                    propertyName: '_INPUT_SCHEMA_FROM_',
                                    propertyType: 'select',
                                    visible: [
                                       [
                                          {
                                             propertyId: 'schema.fromRow',
                                             value: false
                                          }
                                       ]
                                    ],
                                    values: [
                                       {
                                          label: 'Spark format',
                                          value: 'SPARKFORMAT'
                                       },
                                       {
                                          label: 'Example',
                                          value: 'EXAMPLE'
                                       }
                                    ],
                                    'default': 'SPARKFORMAT',
                                    required: true,
                                    qa: 'workflow-transformation-json-schema-mode'
                                 },
                                 {
                                    propertyId: 'schema.provided',
                                    propertyName: '_DESERIALIZER_SCHEMA_',
                                    propertyType: 'textarea',
                                    contentType: 'JSON',
                                    width: 8,
                                    tooltip: 'Schema expressed in JSON/Spark format or from a valid sample from the input data being transformed.',
                                    'default': '',
                                    visible: [
                                       [
                                          {
                                             propertyId: 'schema.fromRow',
                                             value: false
                                          }
                                       ]
                                    ],
                                    required: false,
                                    qa: 'workflow-transformation-json-schema-provided'
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'whenRowError',
                                    propertyName: '_TRANSFORMATION_WHEN_ROW_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'RowError'
                                       },
                                       {
                                          label: 'Discard',
                                          value: 'RowDiscard'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a row-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Discard\' is chosen, no error will be prompted and the row will be omitted.',
                                    qa: 'workflow-transformation-whenRowError'
                                 },
                                 {
                                    propertyId: 'whenFieldError',
                                    propertyName: '_TRANSFORMATION_WHEN_FIELD_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'FieldError'
                                       },
                                       {
                                          label: 'Null',
                                          value: 'Null'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a field-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Null\' is chosen, no error will be prompted and the field value will be set to null.',
                                    qa: 'workflow-transformation-whenFieldError'
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'Split',
                           value: {
                              name: 'Split',
                              icon: 'Split',
                              category: 'Row',
                              className: 'SplitTransformStep',
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData',
                                 'DiscardedData'
                              ],
                              classPrettyName: 'Split',
                              arity: [
                                 'UnaryToNary'
                              ],
                              description: 'Split values according to the method chosen by the user.',
                              properties: [
                                 {
                                    propertyId: 'inputField',
                                    propertyName: '_INPUT_FIELD_',
                                    propertyType: 'text',
                                    showSchemaFields: true,
                                    required: true,
                                    tooltip: 'Field that contains the string to be split.',
                                    qa: 'fragment-details-split-fieldString'
                                 },
                                 {
                                    propertyId: 'fieldsPreservationPolicy',
                                    propertyName: '_FIELDS_PRESERVATION_',
                                    required: true,
                                    propertyType: 'select',
                                    'default': 'REPLACE',
                                    values: [
                                       {
                                          label: 'Replacing input field',
                                          value: 'REPLACE'
                                       },
                                       {
                                          label: 'Appending extracted',
                                          value: 'APPEND'
                                       },
                                       {
                                          label: 'Keeping just extracted',
                                          value: 'JUST_EXTRACTED'
                                       }
                                    ],
                                    tooltip: 'The user might choose between 3 modes. If \'Replacing input field\' is chosen the resulting data will be saved in the position occupied by the input field. \'Appending extracted\' will add the data at the end of the row and lastly \'Keeping just extracted\' will only keep the transformed data discarding the received.',
                                    qa: 'workflow-transformation-csv-fieldsPreservationPolicy'
                                 },
                                 {
                                    propertyId: 'splitMethod',
                                    propertyName: '_SPLIT_METHOD_',
                                    propertyType: 'select',
                                    width: 6,
                                    qa: 'workflow-transformation-split-splitMethod',
                                    values: [
                                       {
                                          label: 'By index',
                                          value: 'BYINDEX'
                                       },
                                       {
                                          label: 'By regex',
                                          value: 'BYREGEX'
                                       },
                                       {
                                          label: 'By char',
                                          value: 'BYCHAR'
                                       }
                                    ],
                                    'default': 'BYINDEX',
                                    required: true
                                 },
                                 {
                                    propertyId: 'byRegexPattern',
                                    propertyName: '_BYREGEX_PATTERN_',
                                    propertyType: 'text',
                                    width: 6,
                                    required: false,
                                    tooltip: 'Regular expression that should be used to split the field (e.g. [.]).',
                                    visible: [
                                       [
                                          {
                                             propertyId: 'splitMethod',
                                             value: 'BYREGEX'
                                          }
                                       ]
                                    ],
                                    qa: 'workflow-transformation-split-byRegexPattern'
                                 },
                                 {
                                    propertyId: 'byCharPattern',
                                    propertyName: '_BYCHAR_PATTERN_',
                                    propertyType: 'text',
                                    width: 6,
                                    required: false,
                                    tooltip: 'Character that should be used to split the field (e.g. \\t, |, $).',
                                    visible: [
                                       [
                                          {
                                             propertyId: 'splitMethod',
                                             value: 'BYCHAR'
                                          }
                                       ]
                                    ],
                                    qa: 'workflow-transformation-split-byCharPattern'
                                 },
                                 {
                                    propertyId: 'byIndexPattern',
                                    propertyName: '_BYINDEX_PATTERN_',
                                    propertyType: 'text',
                                    width: 6,
                                    required: false,
                                    tooltip: 'Comma-separated indices that should be used to split the field (e.g. 1,3,7)[NOTE: indexes start at 0]',
                                    visible: [
                                       [
                                          {
                                             propertyId: 'splitMethod',
                                             value: 'BYINDEX'
                                          }
                                       ]
                                    ],
                                    qa: 'workflow-transformation-split-byIndexPattern'
                                 },
                                 {
                                    propertyId: 'excludeIndexes',
                                    propertyName: '_EXCLUDE_INDEXES_',
                                    propertyType: 'boolean',
                                    width: 6,
                                    'default': false,
                                    tooltip: 'If checked, the chars found at the positions specified for the splitting will be discarded.',
                                    visible: [
                                       [
                                          {
                                             propertyId: 'splitMethod',
                                             value: 'BYINDEX'
                                          }
                                       ]
                                    ],
                                    qa: 'policy-transformation-split-excludeIndexes'
                                 },
                                 {
                                    propertyId: 'schema.inputMode',
                                    propertyName: '_INPUT_SCHEMA_FROM_',
                                    propertyType: 'select',
                                    width: 6,
                                    required: true,
                                    values: [
                                       {
                                          label: 'Fields',
                                          value: 'FIELDS'
                                       },
                                       {
                                          label: 'Spark format',
                                          value: 'SPARKFORMAT'
                                       }
                                    ],
                                    'default': 'FIELDS',
                                    qa: 'fragment-details-input-schema-type'
                                 },
                                 {
                                    propertyId: 'schema.fields',
                                    propertyName: '_FIELDS_DEFINITION_',
                                    propertyType: 'list',
                                    width: 12,
                                    visible: [
                                       [
                                          {
                                             propertyId: 'schema.inputMode',
                                             value: 'FIELDS'
                                          }
                                       ]
                                    ],
                                    required: true,
                                    tooltip: 'Schema definition from a list of fields defined as <field name, field type> tuples.',
                                    qa: 'fragment-details-split-fields',
                                    fields: [
                                       {
                                          propertyId: 'name',
                                          propertyName: '_NAME_',
                                          propertyType: 'text',
                                          required: true,
                                          width: 4,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'type',
                                          propertyName: '_TYPE_',
                                          propertyType: 'select',
                                          custom: true,
                                          required: true,
                                          width: 4,
                                          values: [
                                             {
                                                label: 'String',
                                                value: 'string'
                                             },
                                             {
                                                label: 'Long',
                                                value: 'long'
                                             },
                                             {
                                                label: 'Float',
                                                value: 'float'
                                             },
                                             {
                                                label: 'Double',
                                                value: 'double'
                                             },
                                             {
                                                label: 'Integer',
                                                value: 'integer'
                                             },
                                             {
                                                label: 'Boolean',
                                                value: 'boolean'
                                             },
                                             {
                                                label: 'Binary',
                                                value: 'binary'
                                             },
                                             {
                                                label: 'Date',
                                                value: 'date'
                                             },
                                             {
                                                label: 'Timestamp',
                                                value: 'timestamp'
                                             },
                                             {
                                                label: 'Array(Double)',
                                                value: 'arraydouble'
                                             },
                                             {
                                                label: 'Array(String)',
                                                value: 'arraystring'
                                             },
                                             {
                                                label: 'Array(Long)',
                                                value: 'arraylong'
                                             },
                                             {
                                                label: 'Array(Integer)',
                                                value: 'arrayinteger'
                                             },
                                             {
                                                label: 'Array(Map(String, String))',
                                                value: 'arraymapstringstring'
                                             },
                                             {
                                                label: 'Map(String, Long)',
                                                value: 'mapstringlong'
                                             },
                                             {
                                                label: 'Map(String, Double)',
                                                value: 'mapstringdouble'
                                             },
                                             {
                                                label: 'Map(String, Integer)',
                                                value: 'mapstringint'
                                             },
                                             {
                                                label: 'Map(String, String)',
                                                value: 'mapstringstring'
                                             }
                                          ],
                                          'default': 'string',
                                          qa: 'fragment-details-field-type'
                                       },
                                       {
                                          propertyId: 'nullable',
                                          propertyName: '_NULLABLE_',
                                          propertyType: 'boolean',
                                          'default': true,
                                          required: true,
                                          qa: 'fragment-details-field-nullable'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'schema.sparkSchema',
                                    propertyName: '_SPARK_SCHEMA_',
                                    propertyType: 'textarea',
                                    contentType: 'JSON',
                                    width: 12,
                                    visible: [
                                       [
                                          {
                                             propertyId: 'schema.inputMode',
                                             value: 'SPARKFORMAT'
                                          }
                                       ]
                                    ],
                                    required: true,
                                    tooltip: 'Schema expressed in JSON/Spark format or from a valid sample from the input being defined.',
                                    qa: 'fragment-details-split-spark-schema'
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'whenRowError',
                                    propertyName: '_TRANSFORMATION_WHEN_ROW_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'RowError'
                                       },
                                       {
                                          label: 'Discard',
                                          value: 'RowDiscard'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a row-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Discard\' is chosen, no error will be prompted and the row will be omitted.',
                                    qa: 'workflow-transformation-whenRowError'
                                 },
                                 {
                                    propertyId: 'whenFieldError',
                                    propertyName: '_TRANSFORMATION_WHEN_FIELD_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'FieldError'
                                       },
                                       {
                                          label: 'Null',
                                          value: 'Null'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a field-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Null\' is chosen, no error will be prompted and the field value will be set to null.',
                                    qa: 'workflow-transformation-whenFieldError'
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        }
                     ]
                  },
                  {
                     name: 'Data Quality',
                     value: '',
                     subMenus: [
                        {
                           name: 'BasicDataQuality',
                           value: {
                              name: 'BasicDataQuality',
                              icon: 'BasicDataQuality',
                              category: 'Data Quality',
                              className: 'BasicDataQualityTransformStep',
                              classPrettyName: 'BasicDataQuality',
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedEngines: [
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData'
                              ],
                              description: 'Executes basic data quality analysis.',
                              properties: [
                                 {
                                    propertyId: 'columns',
                                    propertyName: '_COLUMNS_TO_ANALYZE_',
                                    propertyType: 'list',
                                    required: false,
                                    qa: 'fragment-details-columnsToAnalyze',
                                    fields: [
                                       {
                                          propertyId: 'name',
                                          propertyName: '_COLUMN_NAME_PROFILING_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          required: true,
                                          tooltip: 'Name of the column.',
                                          qa: 'fragment-details-columns-name'
                                       },
                                       {
                                          propertyId: 'characters',
                                          propertyName: '_CHARACTERS_ANALYSIS_',
                                          propertyType: 'text',
                                          tooltip: 'List of characters to look for in the selected column. The characters must be separated by commas',
                                          qa: 'fragment-details-characters'
                                       },
                                       {
                                          propertyId: 'nullAnalysis',
                                          propertyName: '_NULL_ANALYSIS_',
                                          propertyType: 'boolean',
                                          'default': true,
                                          tooltip: 'Enables or disables the null analysis over the column',
                                          qa: 'fragment-details-nullAnalysis'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'addExecutionDate',
                                    propertyName: '_EXECUTION_DATE_',
                                    propertyType: 'boolean',
                                    'default': true,
                                    qa: 'fragment-details-addExecutionDate'
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'DataProfiling',
                           value: {
                              name: 'DataProfiling',
                              icon: 'DataProfiling',
                              category: 'Data Quality',
                              className: 'DataProfilingTransformStep',
                              classPrettyName: 'DataProfiling',
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedEngines: [
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData'
                              ],
                              description: 'Creates the statistical analysis of data values for the entire dataSet.',
                              properties: [
                                 {
                                    propertyId: 'analysisType',
                                    propertyName: '_ANALYSIS_TYPE_',
                                    propertyType: 'select',
                                    required: true,
                                    'default': 'DATASET',
                                    values: [
                                       {
                                          label: 'Dataset',
                                          value: 'DATASET'
                                       },
                                       {
                                          label: 'Columns',
                                          value: 'COLUMNS'
                                       },
                                       {
                                          label: 'Column values',
                                          value: 'COLUMN'
                                       }
                                    ],
                                    qa: 'fragment-details-field-analysisType'
                                 },
                                 {
                                    propertyId: 'columns',
                                    propertyName: '_COLUMNS_TO_ANALYZE_',
                                    propertyType: 'list',
                                    showSchemaFields: true,
                                    required: false,
                                    visible: [
                                       [
                                          {
                                             propertyId: 'analysisType',
                                             value: 'COLUMNS'
                                          }
                                       ]
                                    ],
                                    qa: 'fragment-details-columnsToAnalyze',
                                    fields: [
                                       {
                                          propertyId: 'name',
                                          propertyName: '_COLUMN_NAME_PROFILING_',
                                          propertyType: 'text',
                                          required: true,
                                          tooltip: 'Name of the column.',
                                          qa: 'fragment-details-columns-name'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'columnName',
                                    propertyName: '_COLUMN_NAME_PROFILING_',
                                    propertyType: 'text',
                                    required: false,
                                    tooltip: 'Name of the column',
                                    visible: [
                                       [
                                          {
                                             propertyId: 'analysisType',
                                             value: 'COLUMN'
                                          }
                                       ]
                                    ],
                                    qa: 'fragment-details-columns-name'
                                 },
                                 {
                                    propertyId: 'limitResults',
                                    propertyName: '_LIMIT_RESULTS_',
                                    propertyType: 'text',
                                    required: false,
                                    'default': '10',
                                    tooltip: 'Sets a limit for the top values to be shown.',
                                    visible: [
                                       [
                                          {
                                             propertyId: 'analysisType',
                                             value: 'COLUMN'
                                          }
                                       ]
                                    ],
                                    qa: 'fragment-details-limit'
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    showInputSteps: true,
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        }
                     ]
                  },
                  {
                     name: 'SQL',
                     value: '',
                     subMenus: [
                        {
                           name: 'Cube',
                           value: {
                              name: 'Cube',
                              icon: 'Cube',
                              category: 'SQL',
                              className: 'CubeTransformStep',
                              classPrettyName: 'Cube',
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedEngines: [
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData'
                              ],
                              description: 'Create cubes in order to make aggregations based in the OLAP concept.',
                              properties: [
                                 {
                                    propertyId: 'dimensions',
                                    propertyName: '_DIMENSIONS_',
                                    propertyType: 'list',
                                    required: true,
                                    fields: [
                                       {
                                          propertyId: 'name',
                                          propertyName: '_INPUT_FIELD_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          required: true,
                                          width: 6,
                                          tooltip: 'Dimension name, the field we want to group by. This name must be present in the input row fields. A dimension is a structure that categorizes facts and apply measures onto them generating an aggregation. Dimensions may be identified with the fields in a GROUP BY operation.',
                                          qa: 'fragment-details-cube-dimension-name'
                                       }
                                    ],
                                    tooltip: 'List of defined dimensions assigned to the cube.',
                                    qa: 'fragment-details-cube-dimensions'
                                 },
                                 {
                                    propertyId: 'operators',
                                    propertyName: '_OPERATORS_',
                                    propertyType: 'list',
                                    required: true,
                                    fields: [
                                       {
                                          propertyId: 'name',
                                          propertyName: '_NAME_',
                                          propertyType: 'text',
                                          required: true,
                                          width: 3,
                                          tooltip: 'Name assigned to the new field containing the aggregated value for this operator.',
                                          qa: 'fragment-details-measure-name'
                                       },
                                       {
                                          propertyId: 'classType',
                                          propertyName: '_CLASS_TYPE_',
                                          propertyType: 'select',
                                          required: true,
                                          values: [
                                             {
                                                label: 'Avg',
                                                value: 'Avg'
                                             },
                                             {
                                                label: 'Count',
                                                value: 'Count'
                                             },
                                             {
                                                label: 'Max',
                                                value: 'Max'
                                             },
                                             {
                                                label: 'Min',
                                                value: 'Min'
                                             },
                                             {
                                                label: 'Sum',
                                                value: 'Sum'
                                             }
                                          ],
                                          width: 3,
                                          tooltip: 'Type of aggregation operator used.',
                                          qa: 'fragment-details-cube-operator-classType'
                                       },
                                       {
                                          propertyId: 'inputField',
                                          propertyName: '_INPUT_FIELD_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          width: 3,
                                          tooltip: 'Field belonging to an incoming input connected to the Cube transformation. In order to make the aggregation this field must be defined in the Dimensions section too.',
                                          qa: 'fragment-details-cube-operator-inputField'
                                       }
                                    ],
                                    tooltip: 'List of operators assigned to the cube.',
                                    qa: 'fragment-details-cube-measures'
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    width: 8,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. If defined properly this would result in avoiding doing the schema calculation for all defined incoming inputs.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Schema expressed in JSON/Spark format or from a valid sample from the input being defined.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'Distinct',
                           value: {
                              name: 'Distinct',
                              icon: 'Distinct',
                              category: 'SQL',
                              className: 'DistinctTransformStep',
                              classPrettyName: 'Distinct',
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData'
                              ],
                              description: 'Selects distinct events in the input DStream.',
                              properties: [
                                 {
                                    propertyId: 'partitions',
                                    propertyName: '_PARTITIONS_',
                                    propertyType: 'text',
                                    required: false,
                                    tooltip: 'Changes the number of partitions associated to the Spark RDDs.',
                                    qa: 'fragment-details-distinct-partitions'
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'DropDuplicates',
                           value: {
                              name: 'DropDuplicates',
                              icon: 'DropDuplicates',
                              category: 'SQL',
                              className: 'DropDuplicatesTransformStep',
                              classPrettyName: 'DropDuplicates',
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData',
                                 'DiscardedData'
                              ],
                              description: 'Drop duplicate events in the input data.',
                              properties: [
                                 {
                                    propertyId: 'columns',
                                    propertyName: '_COLUMNS_',
                                    width: 8,
                                    propertyType: 'list',
                                    required: false,
                                    tooltip: 'Column(s) that indicates the primary key(s) to be used for detecting and dropping duplicates.',
                                    qa: 'fragment-details-dropDuplicates-columns',
                                    fields: [
                                       {
                                          propertyId: 'name',
                                          propertyName: '_COLUMN_NAME_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          width: 4,
                                          required: true,
                                          tooltip: 'Name of the column',
                                          hidden: false,
                                          qa: 'fragment-details-dropDuplicates-columns-name'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'discardConditions',
                                    propertyName: '_DISCARD_CONDITIONS_',
                                    propertyType: 'list',
                                    required: false,
                                    width: 8,
                                    tooltip: 'Conditions to compare the previous data with the generated data in order to extract the previous data that is not present in the result',
                                    qa: 'fragment-details-discard-conditions',
                                    fields: [
                                       {
                                          propertyId: 'previousField',
                                          propertyName: '_PREVIOUS_FIELD_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          required: true,
                                          width: 3,
                                          tooltip: 'Field to compare in the previous data',
                                          qa: 'fragment-details-previousField'
                                       },
                                       {
                                          propertyId: 'transformedField',
                                          propertyName: '_TRANSFORMED_FIELD_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          tooltip: 'Field to compare in the generated data',
                                          required: true,
                                          width: 3,
                                          qa: 'fragment-details-transformedField'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'Explode',
                           value: {
                              name: 'Explode',
                              icon: 'Explode',
                              category: 'SQL',
                              className: 'ExplodeTransformStep',
                              classPrettyName: 'Explode',
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData'
                              ],
                              description: 'Explodes an input field and returns one row for each element in the input field.',
                              properties: [
                                 {
                                    propertyId: 'inputField',
                                    propertyName: '_INPUT_FIELD_',
                                    propertyType: 'text',
                                    showSchemaFields: true,
                                    required: true,
                                    tooltip: 'Field that contains the values to be exploded',
                                    qa: 'fragment-details-fieldsString'
                                 },
                                 {
                                    propertyId: 'fieldsPreservationPolicy',
                                    propertyName: '_FIELDS_PRESERVATION_',
                                    required: true,
                                    propertyType: 'select',
                                    'default': 'REPLACE',
                                    values: [
                                       {
                                          label: 'Replacing input field',
                                          value: 'REPLACE'
                                       },
                                       {
                                          label: 'Appending extracted',
                                          value: 'APPEND'
                                       },
                                       {
                                          label: 'Keeping just extracted',
                                          value: 'JUST_EXTRACTED'
                                       }
                                    ],
                                    tooltip: 'The user might choose between 3 modes. If \'Replacing input field\' is chosen the resulting data will be saved in the position occupied by the input field. \'Appending extracted\' will add the data at the end of the row and lastly \'Keeping just extracted\' will only keep the transformed data discarding the received.',
                                    qa: 'workflow-transformation-explode-fieldsPreservationPolicy'
                                 },
                                 {
                                    propertyId: 'explodedField',
                                    propertyName: '_EXPLODED_FIELD_',
                                    propertyType: 'text',
                                    required: true,
                                    tooltip: 'Specifies the name of the column/field where the exploded values will be stored.',
                                    qa: 'fragment-details-exploded-fields'
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'whenRowError',
                                    propertyName: '_TRANSFORMATION_WHEN_ROW_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'RowError'
                                       },
                                       {
                                          label: 'Discard',
                                          value: 'RowDiscard'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a row-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Discard\' is chosen, no error will be prompted and the row will be omitted.',
                                    qa: 'workflow-transformation-whenRowError'
                                 },
                                 {
                                    propertyId: 'whenFieldError',
                                    propertyName: '_TRANSFORMATION_WHEN_FIELD_ERROR_',
                                    propertyType: 'select',
                                    values: [
                                       {
                                          label: 'Error',
                                          value: 'FieldError'
                                       },
                                       {
                                          label: 'Null',
                                          value: 'Null'
                                       }
                                    ],
                                    tooltip: 'Specifies which policy to follow when a field-level error arises. If \'Error\' is chosen, the application will stop and the error trace will be written to the log. If \'Null\' is chosen, no error will be prompted and the field value will be set to null.',
                                    qa: 'workflow-transformation-whenFieldError'
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'Filter',
                           value: {
                              name: 'Filter',
                              icon: 'Filter',
                              category: 'SQL',
                              className: 'FilterTransformStep',
                              classPrettyName: 'Filter',
                              arity: [
                                 'UnaryToNary'
                              ],
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData',
                                 'DiscardedData'
                              ],
                              description: 'Filter the events in the input DStream.',
                              properties: [
                                 {
                                    propertyId: 'filterExp',
                                    propertyName: '_FILTER_EXPRESSION_',
                                    propertyType: 'textarea',
                                    contentType: 'JSON',
                                    width: 12,
                                    required: false,
                                    tooltip: 'Filter the events through an expression. It supports UDFs, logical operators, etc ...',
                                    qa: 'fragment-details-filter-expression'
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'Intersection',
                           value: {
                              name: 'Intersection',
                              icon: 'Intersection',
                              category: 'SQL',
                              className: 'IntersectionTransformStep',
                              classPrettyName: 'Intersection',
                              arity: [
                                 'BinaryToNary'
                              ],
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData',
                                 'DiscardedData'
                              ],
                              description: 'Select the intersection of events from the two input DStream',
                              properties: [
                                 {
                                    propertyId: 'partitions',
                                    propertyName: '_PARTITIONS_',
                                    propertyType: 'text',
                                    required: false,
                                    tooltip: 'Change the number of partitions associated to the Spark RDDs',
                                    qa: 'fragment-details-distinct-partitions'
                                 },
                                 {
                                    propertyId: 'discardConditions',
                                    propertyName: '_DISCARD_CONDITIONS_',
                                    propertyType: 'list',
                                    required: false,
                                    width: 8,
                                    tooltip: 'Conditions to compare the previous data with the generated data in order to extract the previous data that is not present in the result',
                                    qa: 'fragment-details-discard-conditions',
                                    fields: [
                                       {
                                          propertyId: 'previousField',
                                          propertyName: '_PREVIOUS_FIELD_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          required: true,
                                          width: 3,
                                          tooltip: 'Field to compare in the previous data',
                                          qa: 'fragment-details-previousField'
                                       },
                                       {
                                          propertyId: 'transformedField',
                                          propertyName: '_TRANSFORMED_FIELD_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          tooltip: 'Field to compare in the generated data',
                                          required: true,
                                          width: 3,
                                          qa: 'fragment-details-transformedField'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'Join',
                           value: {
                              name: 'Join',
                              icon: 'Join',
                              category: 'SQL',
                              className: 'JoinTransformStep',
                              supportedEngines: [
                                 'Batch',
                                 'Streaming'
                              ],
                              supportedDataRelations: [
                                 'ValidData'
                              ],
                              classPrettyName: 'Join',
                              arity: [
                                 'BinaryToNary'
                              ],
                              description: 'Executes a join between two steps.',
                              properties: [
                                 {
                                    propertyId: 'leftTable',
                                    propertyName: '_JOIN_LEFT_TABLE_',
                                    propertyType: 'text',
                                    showInputSteps: true,
                                    required: true,
                                    tooltip: 'Select the left table in the join sentence.',
                                    qa: 'fragment-details-join-leftTable'
                                 },
                                 {
                                    propertyId: 'rightTable',
                                    propertyName: '_JOIN_RIGHT_TABLE_',
                                    propertyType: 'text',
                                    showInputSteps: true,
                                    required: true,
                                    tooltip: 'Select the right table in the join sentence.',
                                    qa: 'fragment-details-join-leftTable'
                                 },
                                 {
                                    propertyId: 'joinType',
                                    propertyName: '_JOIN_TYPE_',
                                    required: true,
                                    propertyType: 'select',
                                    tooltip: 'Join type to be executed. This list represents every kind of join supported by SparkSQL.',
                                    'default': 'INNER',
                                    values: [
                                       {
                                          label: 'Inner',
                                          value: 'INNER'
                                       },
                                       {
                                          label: 'Full',
                                          value: 'FULL'
                                       },
                                       {
                                          label: 'Cross',
                                          value: 'CROSS'
                                       },
                                       {
                                          label: 'Left',
                                          value: 'LEFT'
                                       },
                                       {
                                          label: 'Left only',
                                          value: 'LEFT_ONLY'
                                       },
                                       {
                                          label: 'Right',
                                          value: 'RIGHT'
                                       },
                                       {
                                          label: 'Right only',
                                          value: 'RIGHT_ONLY'
                                       },
                                       {
                                          label: 'Left and right only',
                                          value: 'LEFT_RIGHT_ONLY'
                                       }
                                    ],
                                    qa: 'workflow-transformation-join-type'
                                 },
                                 {
                                    propertyId: 'joinReturn',
                                    propertyName: '_JOIN_RETURN_',
                                    required: true,
                                    propertyType: 'select',
                                    'default': 'ALL',
                                    values: [
                                       {
                                          label: 'All',
                                          value: 'ALL'
                                       },
                                       {
                                          label: 'Left',
                                          value: 'LEFT'
                                       },
                                       {
                                          label: 'Right',
                                          value: 'RIGHT'
                                       },
                                       {
                                          label: 'Selected',
                                          value: 'COLUMNS'
                                       }
                                    ],
                                    tooltip: 'Determines which columns of the join will be returned: the user can either select all columns, all the columns from the left or the right table or just a customized set of columns.',
                                    qa: 'workflow-transformation-join-return'
                                 },
                                 {
                                    propertyId: 'joinReturnColumns',
                                    propertyName: '_JOIN_RETURN_COLUMNS_',
                                    propertyType: 'list',
                                    required: false,
                                    width: 8,
                                    tooltip: 'List of columns to be returned after the join.',
                                    qa: 'fragment-details-join-return-columns',
                                    fields: [
                                       {
                                          propertyId: 'tableSide',
                                          propertyName: '_JOIN_RETURN_SIDE_',
                                          required: true,
                                          width: 3,
                                          propertyType: 'select',
                                          'default': 'LEFT',
                                          values: [
                                             {
                                                label: 'Left',
                                                value: 'LEFT'
                                             },
                                             {
                                                label: 'Right',
                                                value: 'RIGHT'
                                             }
                                          ],
                                          qa: 'fragment-details-join-return-side'
                                       },
                                       {
                                          propertyId: 'column',
                                          propertyName: '_JOIN_RETURN_COLUMN_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          required: true,
                                          width: 3,
                                          tooltip: 'Column name on input table.',
                                          qa: 'fragment-details-join-return-column'
                                       },
                                       {
                                          propertyId: 'alias',
                                          propertyName: '_JOIN_RETURN_ALIAS_',
                                          propertyType: 'text',
                                          width: 3,
                                          tooltip: 'Alias/new name assigned to the declared column. This field is not mandatory.',
                                          required: false,
                                          qa: 'fragment-details-join-return-column'
                                       }
                                    ],
                                    visible: [
                                       [
                                          {
                                             propertyId: 'joinReturn',
                                             value: 'COLUMNS'
                                          }
                                       ]
                                    ]
                                 },
                                 {
                                    propertyId: 'joinConditions',
                                    propertyName: '_JOIN_CONDITIONS_',
                                    propertyType: 'list',
                                    required: true,
                                    width: 8,
                                    tooltip: 'Field(s) used as join condition(s). The selected field(s) must exist in both tables and have matching types.',
                                    qa: 'fragment-details-join-conditions',
                                    fields: [
                                       {
                                          propertyId: 'leftField',
                                          propertyName: '_JOIN_CONDITION_LEFT_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          required: true,
                                          width: 4,
                                          tooltip: 'Left table field (leftFieldID) used in the join condition \'leftFieldID = rightFieldID\'',
                                          qa: 'fragment-details-join-conditions-left'
                                       },
                                       {
                                          propertyId: 'rightField',
                                          propertyName: '_JOIN_CONDITION_RIGHT_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          width: 4,
                                          tooltip: 'Right table field (rightFieldID) used in the join condition \'leftFieldID = rightFieldID\'',
                                          required: true,
                                          qa: 'fragment-details-join-conditions-right'
                                       }
                                    ],
                                    visibleOR: [
                                       [
                                          {
                                             propertyId: 'joinType',
                                             value: 'INNER'
                                          },
                                          {
                                             propertyId: 'joinType',
                                             value: 'FULL'
                                          },
                                          {
                                             propertyId: 'joinType',
                                             value: 'LEFT'
                                          },
                                          {
                                             propertyId: 'joinType',
                                             value: 'LEFT_ONLY'
                                          },
                                          {
                                             propertyId: 'joinType',
                                             value: 'RIGHT'
                                          },
                                          {
                                             propertyId: 'joinType',
                                             value: 'RIGHT_ONLY'
                                          },
                                          {
                                             propertyId: 'joinType',
                                             value: 'LEFT_RIGHT_ONLY'
                                          }
                                       ]
                                    ]
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'OrderBy',
                           value: {
                              name: 'OrderBy',
                              icon: 'OrderBy',
                              category: 'SQL',
                              className: 'OrderByTransformStep',
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData'
                              ],
                              classPrettyName: 'OrderBy',
                              description: 'Order fields from the events in the input DStream.',
                              properties: [
                                 {
                                    propertyId: 'orderExp',
                                    propertyName: '_ORDER_EXPRESSION_',
                                    propertyType: 'text',
                                    required: false,
                                    tooltip: 'Specifies which field(s) to use in the ORDER BY expression. This transformation implements the native \'Order by\' Spark operation.',
                                    qa: 'fragment-details-order-expression'
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'Select',
                           value: {
                              name: 'Select',
                              icon: 'Select',
                              category: 'SQL',
                              className: 'SelectTransformStep',
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData'
                              ],
                              classPrettyName: 'Select',
                              arity: [
                                 'UnaryToNary'
                              ],
                              description: 'Select fields from the events in the input DStream.',
                              properties: [
                                 {
                                    propertyId: 'selectType',
                                    propertyName: '_SELECT_TYPE_',
                                    required: true,
                                    propertyType: 'select',
                                    width: 4,
                                    'default': 'EXPRESSION',
                                    values: [
                                       {
                                          label: 'By SQL expression',
                                          value: 'EXPRESSION'
                                       },
                                       {
                                          label: 'By columns',
                                          value: 'COLUMNS'
                                       }
                                    ],
                                    qa: 'workflow-transformation-select-type',
                                    classed: 'col-xs-4'
                                 },
                                 {
                                    propertyId: 'selectExp',
                                    propertyName: '_SELECT_EXPRESSION_',
                                    propertyType: 'textarea',
                                    contentType: 'SQL',
                                    width: 12,
                                    required: false,
                                    visible: [
                                       [
                                          {
                                             propertyId: 'selectType',
                                             value: 'EXPRESSION'
                                          }
                                       ]
                                    ],
                                    tooltip: 'Select the fields based in a expression. UDFs, fixed fields and aliases are supported.',
                                    qa: 'fragment-details-select-expression',
                                    classed: 'col-xs-12'
                                 },
                                 {
                                    propertyId: 'columns',
                                    propertyName: '_SELECT_COLUMNS_',
                                    propertyType: 'list',
                                    showSchemaFields: true,
                                    required: false,
                                    width: 12,
                                    tooltip: 'List of selected columns to be returned.',
                                    qa: 'fragment-details-select-columns',
                                    fields: [
                                       {
                                          propertyId: 'name',
                                          propertyName: '_JOIN_RETURN_COLUMN_',
                                          propertyType: 'text',
                                          required: true,
                                          width: 4,
                                          tooltip: 'Column name on input table',
                                          qa: 'fragment-details-select-return-column',
                                          classed: 'list-item col-sm-4'
                                       },
                                       {
                                          propertyId: 'alias',
                                          propertyName: '_JOIN_RETURN_ALIAS_',
                                          propertyType: 'text',
                                          width: 4,
                                          tooltip: 'Alias/new name assigned to the input column ',
                                          required: false,
                                          qa: 'fragment-details-select-return-column',
                                          classed: 'list-item col-sm-4'
                                       }
                                    ],
                                    visible: [
                                       [
                                          {
                                             propertyId: 'selectType',
                                             value: 'COLUMNS'
                                          }
                                       ]
                                    ],
                                    classed: 'col-xs-12'
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name',
                                          classed: 'list-item col-sm-3'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query',
                                          classed: 'list-item col-sm-8'
                                       }
                                    ],
                                    classed: 'col-xs-8'
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'Trigger',
                           value: {
                              name: 'Trigger',
                              icon: 'Trigger',
                              category: 'SQL',
                              className: 'TriggerTransformStep',
                              supportedEngines: [
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData',
                                 'DiscardedData'
                              ],
                              crossdataCatalog: true,
                              classPrettyName: 'Trigger',
                              arity: [
                                 'BinaryToNary',
                                 'UnaryToNary',
                                 'NaryToNary'
                              ],
                              description: 'Execute a trigger following the SparkSQL syntax.',
                              properties: [
                                 {
                                    propertyId: 'sql',
                                    propertyName: '_SQL_',
                                    propertyType: 'textarea',
                                    contentType: 'SQL',
                                    width: 12,
                                    required: true,
                                    tooltip: 'Query in SQL language. Refer to the SparkSQL documentation for more information on supported operations.',
                                    qa: 'fragment-details-trigger-sql'
                                 },
                                 {
                                    propertyId: 'discardConditions',
                                    propertyName: '_DISCARD_CONDITIONS_',
                                    propertyType: 'list',
                                    required: false,
                                    width: 12,
                                    tooltip: 'Conditions to compare the previous data with the generated data in order to extract the previous data that is not present in the result.',
                                    qa: 'fragment-details-discard-conditions',
                                    fields: [
                                       {
                                          propertyId: 'previousField',
                                          propertyName: '_PREVIOUS_FIELD_',
                                          propertyType: 'text',
                                          showSchemaFields: true,
                                          required: true,
                                          width: 4,
                                          tooltip: 'Field to compare with the previous step data.',
                                          qa: 'fragment-details-previousField'
                                       },
                                       {
                                          propertyId: 'transformedField',
                                          propertyName: '_TRANSFORMED_FIELD_',
                                          propertyType: 'text',
                                          tooltip: 'Field to compare with the generated data.',
                                          required: true,
                                          width: 4,
                                          qa: 'fragment-details-transformedField'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'executeSqlWhenEmpty',
                                    propertyName: '_EXECUTE_WHEN_EMPTY_',
                                    propertyType: 'boolean',
                                    'default': true,
                                    required: true,
                                    width: 12,
                                    tooltip: 'Execute the query when any, or both, of the steps are empty and no schema is provided. This is a useful feature when the query contains joins.',
                                    qa: 'fragment-trigger-executeSqlWhenEmpty'
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'Union',
                           value: {
                              name: 'Union',
                              icon: 'Union',
                              category: 'SQL',
                              className: 'UnionTransformStep',
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData'
                              ],
                              classPrettyName: 'Union',
                              arity: [
                                 'NaryToNary'
                              ],
                              description: 'Unifies two or more DStreams in one. It\'s mandatory condition that both DStreams must have the same fields',
                              properties: [
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 6,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 12,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        }
                     ]
                  },
                  {
                     name: 'Custom',
                     value: '',
                     subMenus: [
                        {
                           name: 'Custom',
                           value: {
                              name: 'Custom',
                              icon: 'Custom',
                              className: 'CustomTransformStep',
                              classPrettyName: 'Custom',
                              category: 'Custom',
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData',
                                 'DiscardedData'
                              ],
                              description: 'Custom transform defined by user. User can extend the Sparta SDK with his own implementation of Transformation class.',
                              properties: [
                                 {
                                    propertyId: 'customClassType',
                                    propertyName: '_MODEL_TYPE_',
                                    propertyType: 'text',
                                    'default': 'Custom',
                                    required: true,
                                    tooltip: 'Transformation class type that implements the transform SDK step',
                                    qa: 'custom-transform-modelType'
                                 },
                                 {
                                    propertyId: 'transformationOptions',
                                    propertyName: '_OPTION_PROPERTIES_',
                                    propertyType: 'list',
                                    required: false,
                                    qa: 'custom-transform-properties',
                                    fields: [
                                       {
                                          propertyId: 'transformationOptionsKey',
                                          propertyName: '_OPTION_KEY_',
                                          propertyType: 'text',
                                          required: false,
                                          width: 4,
                                          qa: 'custom-transform-properties-key'
                                       },
                                       {
                                          propertyId: 'transformationOptionsValue',
                                          propertyName: '_OPTION_VALUE_',
                                          propertyType: 'text',
                                          required: false,
                                          width: 4,
                                          qa: 'custom-transform-properties-value'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    width: 8,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. If defined properly this would result in avoiding doing the schema calculation for all defined incoming inputs.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Schema expressed in JSON/Spark format or from a valid sample from the input being defined.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'CustomLite',
                           value: {
                              name: 'CustomLite',
                              icon: 'Custom',
                              className: 'CustomLiteTransformStep',
                              classPrettyName: 'CustomLite',
                              category: 'Custom',
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData',
                                 'DiscardedData'
                              ],
                              description: 'Custom transformation defined by user. User can extend the Sparta SDK-Lite with his own implementation of the Transformation class',
                              properties: [
                                 {
                                    propertyId: 'customLiteClassType',
                                    propertyName: '_MODEL_TYPE_',
                                    propertyType: 'text',
                                    'default': 'Custom',
                                    required: true,
                                    tooltip: 'Transformation class type that implements the SDK transformation step',
                                    qa: 'custom-transform-modelType'
                                 },
                                 {
                                    propertyId: 'transformationOptions',
                                    propertyName: '_OPTION_PROPERTIES_',
                                    propertyType: 'list',
                                    required: false,
                                    qa: 'custom-transform-properties',
                                    fields: [
                                       {
                                          propertyId: 'transformationOptionsKey',
                                          propertyName: '_OPTION_KEY_',
                                          propertyType: 'text',
                                          required: false,
                                          width: 4,
                                          qa: 'custom-transform-properties-key'
                                       },
                                       {
                                          propertyId: 'transformationOptionsValue',
                                          propertyName: '_OPTION_VALUE_',
                                          propertyType: 'text',
                                          required: false,
                                          width: 4,
                                          qa: 'custom-transform-properties-value'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'CustomLiteXD',
                           value: {
                              name: 'CustomLiteXD',
                              icon: 'CustomLiteXD',
                              className: 'CustomLiteXDTransformStep',
                              classPrettyName: 'CustomLiteXD',
                              category: 'Custom',
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData',
                                 'DiscardedData'
                              ],
                              description: 'Custom transformation defined by user. User can extend the Sparta SDK-Lite with his own implementation of Transformation class',
                              properties: [
                                 {
                                    propertyId: 'customLiteClassType',
                                    propertyName: '_MODEL_TYPE_',
                                    propertyType: 'text',
                                    'default': 'Custom',
                                    required: true,
                                    tooltip: 'Transformation class type that implements the SDK transformation step',
                                    qa: 'custom-transform-modelType'
                                 },
                                 {
                                    propertyId: 'transformationOptions',
                                    propertyName: '_OPTION_PROPERTIES_',
                                    propertyType: 'list',
                                    required: false,
                                    qa: 'custom-transform-properties',
                                    fields: [
                                       {
                                          propertyId: 'transformationOptionsKey',
                                          propertyName: '_OPTION_KEY_',
                                          propertyType: 'text',
                                          required: false,
                                          width: 4,
                                          qa: 'custom-transform-properties-key'
                                       },
                                       {
                                          propertyId: 'transformationOptionsValue',
                                          propertyName: '_OPTION_VALUE_',
                                          propertyType: 'text',
                                          required: false,
                                          width: 4,
                                          qa: 'custom-transform-properties-value'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        }
                     ]
                  },
                  {
                     name: 'Spark Native',
                     value: '',
                     subMenus: [
                        {
                           name: 'Persist',
                           value: {
                              name: 'Persist',
                              icon: 'Persist',
                              category: 'Spark Native',
                              className: 'PersistTransformStep',
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData'
                              ],
                              classPrettyName: 'Persist',
                              arity: [
                                 'UnaryToNary'
                              ],
                              description: 'Persist the input DStream',
                              properties: [
                                 {
                                    propertyId: 'storageLevel',
                                    propertyName: '_STORAGELEVEL_',
                                    propertyType: 'select',
                                    required: false,
                                    tooltip: 'Storage Level assigned to Spark DStreams in each partition.',
                                    qa: 'persist-storage-level',
                                    values: [
                                       {
                                          label: 'DISK_ONLY',
                                          value: 'DISK_ONLY'
                                       },
                                       {
                                          label: 'DISK_ONLY_2',
                                          value: 'DISK_ONLY_2'
                                       },
                                       {
                                          label: 'MEMORY_ONLY',
                                          value: 'MEMORY_ONLY'
                                       },
                                       {
                                          label: 'MEMORY_ONLY_2',
                                          value: 'MEMORY_ONLY_2'
                                       },
                                       {
                                          label: 'MEMORY_ONLY_SER',
                                          value: 'MEMORY_ONLY_SER'
                                       },
                                       {
                                          label: 'MEMORY_ONLY_SER_2',
                                          value: 'MEMORY_ONLY_SER_2'
                                       },
                                       {
                                          label: 'MEMORY_AND_DISK',
                                          value: 'MEMORY_AND_DISK'
                                       },
                                       {
                                          label: 'MEMORY_AND_DISK_2',
                                          value: 'MEMORY_AND_DISK_2'
                                       },
                                       {
                                          label: 'MEMORY_AND_DISK_SER',
                                          value: 'MEMORY_AND_DISK_SER'
                                       },
                                       {
                                          label: 'MEMORY_AND_DISK_SER_2',
                                          value: 'MEMORY_AND_DISK_SER_2'
                                       }
                                    ]
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        },
                        {
                           name: 'Repartition',
                           value: {
                              name: 'Repartition',
                              icon: 'Repartition',
                              category: 'Spark Native',
                              className: 'RepartitionTransformStep',
                              supportedEngines: [
                                 'Streaming',
                                 'Batch'
                              ],
                              supportedDataRelations: [
                                 'ValidData'
                              ],
                              classPrettyName: 'Repartition',
                              description: 'Repartition the input DStream',
                              properties: [
                                 {
                                    propertyId: 'partitions',
                                    propertyName: '_PARTITIONS_',
                                    propertyType: 'text',
                                    required: false,
                                    tooltip: 'Change the number of partitions associated to the Spark RDDs',
                                    qa: 'fragment-details-repartition-partitions'
                                 },
                                 {
                                    propertyId: 'inputSchemas',
                                    propertyName: '_INPUTS_TRIGGER_SCHEMA_',
                                    propertyType: 'list',
                                    required: false,
                                    complexForm: true,
                                    tooltip: 'Section that allows the user to define incoming steps schemas. Once defined properly, the schema calculation for all incoming inputs will be avoided thus increasing the workflow performance and type-safety.',
                                    qa: 'fragment-details-schema-fields',
                                    fields: [
                                       {
                                          propertyId: 'stepName',
                                          propertyName: '_INPUT_STEP_NAME_',
                                          propertyType: 'text',
                                          showInputSteps: true,
                                          required: true,
                                          tooltip: 'Incoming step name.',
                                          width: 3,
                                          'float': false,
                                          qa: 'fragment-details-field-name'
                                       },
                                       {
                                          propertyId: 'schema',
                                          propertyName: '_DESERIALIZER_SCHEMA_',
                                          propertyType: 'textarea',
                                          contentType: 'JSON',
                                          width: 8,
                                          tooltip: 'Input schema either expressed in JSON/Spark format or by providing a valid sample.',
                                          required: true,
                                          qa: 'fragment-details-field-query'
                                       }
                                    ]
                                 }
                              ]
                           },
                           stepType: 'Transformation'
                        }
                     ]
                  }
               ]
            },
            {
               name: 'Output',
               value: 'action',
               icon: 'icon-logout',
               subMenus: [
                  {
                     name: 'Avro',
                     value: {
                        name: 'Avro',
                        icon: 'Avro',
                        className: 'AvroOutputStep',
                        classPrettyName: 'Avro',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        description: 'Serializes data in a compact binary format. It can provide both a serialization format for persistent data, and a wire format for communication between Hadoop nodes.',
                        properties: [
                           {
                              propertyId: 'path',
                              propertyName: '_PATH_',
                              propertyType: 'text',
                              required: true,
                              tooltip: 'Path used to persist the transformed data. Please note that, in order to write to the HDFS specified in the tenant configuration, also relative paths can be used (e.g. /user/sparta-server/) without the namenode details (e.g. hdfs:///addressNamenode:8020/user/sparta-server/)',
                              qa: 'fragment-details-avro-path'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'If checked, this output can be used to send error events. This feature can be configured through the errors management settings.',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           },
                           {
                              propertyId: 'saveOptions',
                              propertyName: '_AVRO_SAVE_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'fragment-details-avro-save-properties',
                              fields: [
                                 {
                                    propertyId: 'saveOptionsKey',
                                    propertyName: '_SAVE_OPTIONS_KEY_',
                                    propertyType: 'text',
                                    width: 4,
                                    required: false,
                                    qa: 'fragment-details-avro-saveOptionsKey'
                                 },
                                 {
                                    propertyId: 'saveOptionsValue',
                                    propertyName: '_SAVE_OPTIONS_VALUE_',
                                    propertyType: 'text',
                                    width: 4,
                                    required: false,
                                    qa: 'fragment-details-avro-saveOptionsValue'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Output'
                  },
                  {
                     name: 'Crossdata',
                     value: {
                        name: 'Crossdata',
                        icon: 'Crossdata',
                        className: 'CrossdataOutputStep',
                        classPrettyName: 'Crossdata',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        description: 'Crossdata output use Crossdata as a library with the available XDSession in order to save the data into persistent tables. The tables must be created previously in corresponding Crossdata catalog.',
                        properties: [
                           {
                              propertyId: 'tlsEnabled',
                              propertyName: '_TLS_ENABLE_',
                              tooltip: 'Enable TLS protocol in order to connect to a secured filesystem using certificates.',
                              'default': false,
                              qa: 'fragment-details-postgres-tls'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'If checked, this output can be used to send error events. This feature can be configured through the errors management settings.',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           }
                        ]
                     },
                     stepType: 'Output'
                  },
                  {
                     name: 'Csv',
                     value: {
                        name: 'Csv',
                        icon: 'Csv',
                        className: 'CsvOutputStep',
                        classPrettyName: 'Csv',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        description: 'Persist your data in HDFS with CSV format.',
                        properties: [
                           {
                              propertyId: 'path',
                              propertyName: '_PATH_',
                              propertyType: 'text',
                              required: true,
                              qa: 'fragment-details-csv-path',
                              tooltip: 'Path to store the data in a compatible filesystem (HDFS, S3, Azure, etc). Please note that, in order to write to the HDFS specified in the tenant configuration, also relative paths can be used (e.g. /user/sparta-server/) without the namenode details (e.g. hdfs:///addressNamenode:8020/user/sparta-server/)'
                           },
                           {
                              propertyId: 'delimiter',
                              propertyName: '_DELIMITER_',
                              propertyType: 'text',
                              tooltip: 'Any character is accepted except: \\ " #',
                              'default': '{{{DEFAULT_DELIMITER}}}',
                              trim: false,
                              required: true,
                              qa: 'fragment-details-csv-delimiter'
                           },
                           {
                              propertyId: 'header',
                              propertyName: '_HEADER_',
                              propertyType: 'boolean',
                              regexp: 'true|false',
                              'default': false,
                              required: true,
                              tooltip: 'Sets if a header is attached to the output file.',
                              qa: 'fragment-details-csv-header'
                           },
                           {
                              propertyId: 'inferSchema',
                              propertyName: '_INFER_SCHEMA_',
                              propertyType: 'boolean',
                              regexp: 'true|false',
                              'default': false,
                              required: true,
                              tooltip: 'If checked, the data schema will be attached to the output file.',
                              qa: 'fragment-details-csv-inferSchema'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'If checked, this output can be used to send error events. This feature can be configured through the errors management settings.',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           },
                           {
                              propertyId: 'saveOptions',
                              propertyName: '_CSV_SAVE_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'fragment-details-csv-save-properties',
                              fields: [
                                 {
                                    propertyId: 'saveOptionsKey',
                                    propertyName: '_SAVE_OPTIONS_KEY_',
                                    propertyType: 'text',
                                    width: 4,
                                    required: false,
                                    qa: 'fragment-details-csv-saveOptionsKey'
                                 },
                                 {
                                    propertyId: 'saveOptionsValue',
                                    propertyName: '_SAVE_OPTIONS_VALUE_',
                                    propertyType: 'text',
                                    width: 4,
                                    required: false,
                                    qa: 'fragment-details-csv-saveOptionsValue'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Output'
                  },
                  {
                     name: 'Custom',
                     value: {
                        name: 'Custom',
                        icon: 'Custom',
                        className: 'CustomOutputStep',
                        category: 'Custom',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        classPrettyName: 'Custom',
                        description: 'Users can extend the Sparta SDK with their own implementation of the Output class.',
                        properties: [
                           {
                              propertyId: 'customClassType',
                              propertyName: '_MODEL_TYPE_',
                              propertyType: 'text',
                              placeHolder: 'Custom',
                              required: true,
                              tooltip: 'Output class type that implements the output SDK step',
                              qa: 'custom-output-modelType'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'If checked, this output can be used to send error events. This feature can be configured through the errors management settings.',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           },
                           {
                              propertyId: 'saveOptions',
                              propertyName: '_OPTION_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'custom-output-properties',
                              fields: [
                                 {
                                    propertyId: 'saveOptionsKey',
                                    propertyName: '_OPTION_KEY_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-output-properties-key'
                                 },
                                 {
                                    propertyId: 'saveOptionsValue',
                                    propertyName: '_OPTION_VALUE_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-output-properties-value'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Output'
                  },
                  {
                     name: 'CustomLite',
                     value: {
                        name: 'CustomLite',
                        icon: 'CustomLite',
                        className: 'CustomLiteOutputStep',
                        category: 'Custom',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        classPrettyName: 'CustomLite',
                        description: 'Custom output defined by user. User can extend the Sparta SDK-Lite with his own implementation of Output class',
                        properties: [
                           {
                              propertyId: 'customLiteClassType',
                              propertyName: '_MODEL_TYPE_',
                              propertyType: 'text',
                              placeHolder: 'CustomLiteOutput',
                              required: true,
                              tooltip: 'Output class type that implements the input SDK step',
                              qa: 'custom-output-modelType'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'This output can be used for sending errors in the errors management settings',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           },
                           {
                              propertyId: 'saveOptions',
                              propertyName: '_OPTION_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'custom-output-properties',
                              fields: [
                                 {
                                    propertyId: 'saveOptionsKey',
                                    propertyName: '_OPTION_KEY_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-output-properties-key'
                                 },
                                 {
                                    propertyId: 'saveOptionsValue',
                                    propertyName: '_OPTION_VALUE_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-output-properties-value'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Output'
                  },
                  {
                     name: 'CustomLiteXD',
                     value: {
                        name: 'CustomLiteXD',
                        icon: 'CustomLiteXD',
                        className: 'CustomLiteXDOutputStep',
                        category: 'Custom',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        classPrettyName: 'CustomLiteXD',
                        description: 'Custom output defined by user. User can extend the Sparta SDK-Lite with his own implementation of Output class',
                        properties: [
                           {
                              propertyId: 'customLiteClassType',
                              propertyName: '_MODEL_TYPE_',
                              propertyType: 'text',
                              placeHolder: 'CustomLiteXDOutput',
                              required: true,
                              tooltip: 'Output class type that implements the input SDK step',
                              qa: 'custom-output-modelType'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'This output can be used for sending errors in the errors management settings',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           },
                           {
                              propertyId: 'saveOptions',
                              propertyName: '_OPTION_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'custom-output-properties',
                              fields: [
                                 {
                                    propertyId: 'saveOptionsKey',
                                    propertyName: '_OPTION_KEY_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-output-properties-key'
                                 },
                                 {
                                    propertyId: 'saveOptionsValue',
                                    propertyName: '_OPTION_VALUE_',
                                    propertyType: 'text',
                                    required: false,
                                    width: 4,
                                    qa: 'custom-output-properties-value'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Output'
                  },
                  {
                     name: 'Elasticsearch',
                     value: {
                        name: 'Elasticsearch',
                        classPrettyName: 'ElasticSearch',
                        className: 'ElasticSearchOutputStep',
                        icon: 'Elasticsearch',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        description: 'Elasticsearch is a search server based on Lucene. This output uses the generic implementation with DataFrames.',
                        properties: [
                           {
                              propertyId: 'nodes',
                              propertyName: '_NODES_',
                              propertyType: 'list',
                              'default': '',
                              required: true,
                              hidden: false,
                              limit: 0,
                              tooltip: 'Configures the \'es.nodes\' and \'es.port\' properties.',
                              qa: 'fragment-details-elasticSearch-nodes',
                              fields: [
                                 {
                                    propertyId: 'node',
                                    propertyName: '_HOST_',
                                    propertyType: 'text',
                                    'default': '{{{ES_HOST}}}',
                                    required: true,
                                    width: 6,
                                    qa: 'fragment-details-elasticSearch-node'
                                 },
                                 {
                                    propertyId: 'httpPort',
                                    propertyName: '_HTTP_PORT_',
                                    propertyType: 'text',
                                    'default': '{{{ES_PORT}}}',
                                    width: 3,
                                    required: true,
                                    qa: 'fragment-details-elasticSearch-httpPort'
                                 }
                              ]
                           },
                           {
                              propertyId: 'indexMapping',
                              propertyName: '_INDEX_MAPPING_',
                              propertyType: 'text',
                              'default': '{{{ES_INDEX_MAPPING}}}',
                              tooltip: 'Value will be used as mapping name for indexing.',
                              required: false,
                              qa: 'fragment-details-elasticSearch-indexMapping'
                           },
                           {
                              propertyId: 'timeStampMapperFormat',
                              propertyName: '_TIME_STAMP_MAPPING_',
                              propertyType: 'text',
                              tooltip: 'Specifies a format for the document timestamp.',
                              required: false,
                              qa: 'fragment-details-elasticSearch-ts-mapper-format'
                           },
                           {
                              propertyId: 'enableAutoCreateIndex',
                              propertyName: '_ENABLE_INDEX_AUTO_CREATE_',
                              propertyType: 'boolean',
                              tooltip: 'Enables whether ElasticSearch should create an index (if it is missing) when writing data or fail.',
                              'default': true,
                              qa: 'fragment-details-elasticSearch-index-auto-create'
                           },
                           {
                              propertyId: 'tlsEnabled',
                              propertyName: '_TLS_ENABLE_',
                              propertyType: 'boolean',
                              tooltip: 'Retrieves the necessary certificates from Vault and uses them to establish a TLS connection between Spark and ElasticSearch.',
                              'default': false,
                              qa: 'fragment-details-elasticSearch-tls'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'If checked, this output can be used to send error events. This feature can be configured through the errors management settings.',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           },
                           {
                              propertyId: 'saveOptions',
                              propertyName: '_ELASTICSEARCH_SAVE_PROPERTIES_',
                              propertyType: 'list',
                              'default': '',
                              required: false,
                              tooltip: '',
                              qa: 'fragment-details-elasticsearch-save-properties',
                              fields: [
                                 {
                                    propertyId: 'saveOptionsKey',
                                    propertyName: '_SAVE_OPTIONS_KEY_',
                                    propertyType: 'text',
                                    regexp: '',
                                    'default': '',
                                    hidden: false,
                                    width: 4,
                                    required: false,
                                    qa: 'fragment-details-elasticsearch-saveOptionsKey'
                                 },
                                 {
                                    propertyId: 'saveOptionsValue',
                                    propertyName: '_SAVE_OPTIONS_VALUE_',
                                    propertyType: 'text',
                                    regexp: '',
                                    'default': '',
                                    width: 4,
                                    hidden: false,
                                    required: false,
                                    qa: 'fragment-details-elasticsearch-saveOptionsValue'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Output'
                  },
                  {
                     name: 'Http',
                     value: {
                        name: 'Http',
                        icon: 'Http',
                        className: 'HttpOutputStep',
                        classPrettyName: 'Http',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        description: 'Enables you to do an HTTP POST request to a URL specifying if the data is to be passed through format with a parameter name or attached in the request body.',
                        properties: [
                           {
                              propertyId: 'url',
                              propertyName: '_URL_',
                              propertyType: 'text',
                              required: true,
                              tooltip: '',
                              qa: 'fragment-details-http-url'
                           },
                           {
                              propertyId: 'connTimeout',
                              propertyName: '_CONN_TIMEOUT_',
                              propertyType: 'text',
                              'default': '1000',
                              required: false,
                              tooltip: 'Value for the connection timeout. Default to 1000ms.',
                              qa: 'fragment-details-http-output-conn-timeout'
                           },
                           {
                              propertyId: 'readTimeout',
                              propertyName: '_READ_TIMEOUT_',
                              propertyType: 'text',
                              'default': '5000',
                              required: false,
                              tooltip: 'Value for the read timeout. Default to 5000ms.',
                              qa: 'fragment-details-http-output-read-timeout'
                           },
                           {
                              propertyId: 'outputFormat',
                              propertyName: '_OUTPUT_FORMAT_',
                              propertyType: 'select',
                              regexp: 'json|row',
                              values: [
                                 {
                                    label: 'Json',
                                    value: 'json'
                                 },
                                 {
                                    label: 'Row',
                                    value: 'row'
                                 }
                              ],
                              'default': 'json',
                              required: true,
                              tooltip: 'Sets whether the output should be written as a Json or as a row of String values.',
                              qa: 'fragment-details-http-output-format'
                           },
                           {
                              propertyId: 'delimiter',
                              propertyName: '_DELIMITER_',
                              propertyType: 'text',
                              tooltip: 'Please note that any character belonging to the following set is not allowed: [ \\ , " , # ]. (Note that the square brackets [ ] only delimit the set).',
                              'default': '{{{DEFAULT_DELIMITER}}}',
                              trim: false,
                              required: false,
                              qa: 'fragment-details-http-delimiter'
                           },
                           {
                              propertyId: 'postType',
                              propertyName: '_POST_TYPE_',
                              propertyType: 'select',
                              regexp: 'body|parameter',
                              values: [
                                 {
                                    label: 'Body',
                                    value: 'body'
                                 },
                                 {
                                    label: 'Parameter',
                                    value: 'parameter'
                                 }
                              ],
                              'default': 'body',
                              required: true,
                              tooltip: 'Sets whether the data should be sent in the body or as a named parameter in a form.',
                              qa: 'fragment-details-http-post-type'
                           },
                           {
                              propertyId: 'parameterName',
                              propertyName: '_PARAMETER_NAME_',
                              propertyType: 'text',
                              required: false,
                              tooltip: 'Valid only if Post Type is set to parameter. Value entered will be used a key name.',
                              qa: 'fragment-details-http-parameter-name'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'This output can be used for sending errors in the errors management settings.',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           }
                        ]
                     },
                     stepType: 'Output'
                  },
                  {
                     name: 'Jdbc',
                     value: {
                        name: 'Jdbc',
                        icon: 'Jdbc',
                        className: 'JdbcOutputStep',
                        classPrettyName: 'Jdbc',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        description: 'With one JDBC connection it\'s possible to write data into relational SQL Databases.',
                        properties: [
                           {
                              propertyId: 'url',
                              propertyName: '_JDBC_URL_',
                              propertyType: 'text',
                              'default': '{{{JDBC_URL}}}',
                              required: true,
                              tooltip: 'Url to connect to a JDBC database.',
                              qa: 'fragment-details-jdbc-url'
                           },
                           {
                              propertyId: 'batchsize',
                              propertyName: '_JDBC_BATCH_SIZE_',
                              propertyType: 'text',
                              'default': '1000',
                              required: false,
                              tooltip: 'Number of insert or upsert statements that are sent as a batch to the database from each partition.',
                              qa: 'fragment-details-jdbc-batch-size'
                           },
                           {
                              propertyId: 'driver',
                              propertyName: '_JDBC_DRIVER_',
                              propertyType: 'text',
                              required: false,
                              tooltip: 'Driver class which tells Spark the kind of JDBC database used in this output.',
                              qa: 'fragment-details-jdbc-driver'
                           },
                           {
                              propertyId: 'isolationLevel',
                              propertyName: '_ISOLATION_LEVEL_',
                              propertyType: 'select',
                              values: [
                                 {
                                    label: 'None',
                                    value: 'NONE'
                                 },
                                 {
                                    label: 'Read Uncommitted',
                                    value: 'READ_UNCOMMITTED'
                                 },
                                 {
                                    label: 'Read Committed',
                                    value: 'READ_COMMITTED'
                                 },
                                 {
                                    label: 'Repeatable Read',
                                    value: 'REPEATABLE_READ'
                                 },
                                 {
                                    label: 'Serializable',
                                    value: 'SERIALIZABLE'
                                 }
                              ],
                              'default': 'READ_UNCOMMITTED',
                              required: true,
                              tooltip: 'Sets the isolation level when persisting the data in the database.',
                              qa: 'fragment-postgres-isolation-level'
                           },
                           {
                              propertyId: 'jdbcSaveMode',
                              propertyName: '_JDBC_SAVE_MODE_',
                              propertyType: 'select',
                              values: [
                                 {
                                    label: 'Batch statement',
                                    value: 'STATEMENT'
                                 },
                                 {
                                    label: 'Single statement',
                                    value: 'SINGLE_STATEMENT'
                                 }
                              ],
                              'default': 'STATEMENT',
                              required: true,
                              tooltip: 'Sets whether the output should be written as a batch statement or by using a row-by-row statement',
                              qa: 'fragment-jdbc-save-mode'
                           },
                           {
                              propertyId: 'failFast',
                              propertyName: '_FAIL_FAST_',
                              propertyType: 'boolean',
                              visibleOR: [
                                 {
                                    propertyId: 'jdbcSaveMode',
                                    value: 'STATEMENT'
                                 },
                                 {
                                    propertyId: 'jdbcSaveMode',
                                    value: 'SINGLE_STATEMENT'
                                 }
                              ],
                              'default': false,
                              required: true,
                              tooltip: 'When enabled, if a transaction fails a exception is thrown and the application is stopped abruptly.',
                              qa: 'fragment-details-jdbc-failfast'
                           },
                           {
                              propertyId: 'tlsEnabled',
                              propertyName: '_TLS_ENABLE_',
                              propertyType: 'boolean',
                              tooltip: 'Retrieves the necessary certificates from Vault and uses them to establish a TLS connection between Spark and the JDBC database.',
                              'default': false,
                              qa: 'fragment-details-postgres-tls'
                           },
                           {
                              propertyId: 'schemaFromDatabase',
                              propertyName: '_SCHEMA_FROM_DATABASE_',
                              propertyType: 'boolean',
                              tooltip: 'Tells the workflow to extract the schema from the output database.',
                              'default': false,
                              qa: 'fragment-details-jdbc-schema-database'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'If checked, this output can be used to send error events. This feature can be configured through the errors management settings.',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           },
                           {
                              propertyId: 'saveOptions',
                              propertyName: '_JDBC_SAVE_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'fragment-details-jdbc-save-properties',
                              fields: [
                                 {
                                    propertyId: 'saveOptionsKey',
                                    propertyName: '_SAVE_OPTIONS_KEY_',
                                    propertyType: 'text',
                                    width: 4,
                                    required: false,
                                    qa: 'fragment-details-jdbc-saveOptionsKey'
                                 },
                                 {
                                    propertyId: 'saveOptionsValue',
                                    propertyName: '_SAVE_OPTIONS_VALUE_',
                                    propertyType: 'text',
                                    width: 4,
                                    required: false,
                                    qa: 'fragment-details-jdbc-saveOptionsValue'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Output'
                  },
                  {
                     name: 'Json',
                     value: {
                        name: 'Json',
                        icon: 'Json',
                        className: 'JsonOutputStep',
                        classPrettyName: 'Json',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        description: 'Saves the processed data into a JSON file in any storage service.',
                        properties: [
                           {
                              propertyId: 'path',
                              propertyName: '_PATH_',
                              propertyType: 'text',
                              required: true,
                              tooltip: 'Path to store the data in a compatible filesystem (HDFS, S3, Azure, etc). Please note that, in order to write to the HDFS specified in the tenant configuration, also relative paths can be used (e.g. /user/sparta-server/) without the namenode details (e.g. hdfs:///addressNamenode:8020/user/sparta-server/).',
                              qa: 'fragment-details-filesystem-path'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'If checked, this output can be used to send error events. This feature can be configured through the errors management settings.',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           },
                           {
                              propertyId: 'saveOptions',
                              propertyName: '_JSON_SAVE_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'fragment-details-parquet-save-properties',
                              fields: [
                                 {
                                    propertyId: 'saveOptionsKey',
                                    propertyName: '_SAVE_OPTIONS_KEY_',
                                    propertyType: 'text',
                                    width: 4,
                                    required: false,
                                    qa: 'fragment-details-parquet-saveOptionsKey'
                                 },
                                 {
                                    propertyId: 'saveOptionsValue',
                                    propertyName: '_SAVE_OPTIONS_VALUE_',
                                    propertyType: 'text',
                                    width: 4,
                                    required: false,
                                    qa: 'fragment-details-parquet-saveOptionsValue'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Output'
                  },
                  {
                     name: 'Kafka',
                     value: {
                        name: 'Kafka',
                        classPrettyName: 'Kafka',
                        className: 'KafkaOutputStep',
                        icon: 'Kafka',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        description: 'Apache Kafka is publish-subscribe messaging rethought as a distributed commit log.',
                        properties: [
                           {
                              propertyId: 'bootstrap.servers',
                              propertyName: '_BOOTSTRAP_SERVERS_',
                              propertyType: 'list',
                              required: true,
                              tooltip: 'Sets the \'bootstrap.servers\' property.',
                              qa: 'fragment-details-kafka-bootstrap-servers',
                              fields: [
                                 {
                                    propertyId: 'host',
                                    propertyName: '_HOST_',
                                    propertyType: 'text',
                                    'default': '{{{KAFKA_BROKER_HOST}}}',
                                    required: true,
                                    tooltip: 'Kafka\'s address.',
                                    width: 6,
                                    qa: 'fragment-details-kafka-host'
                                 },
                                 {
                                    propertyId: 'port',
                                    propertyName: '_PORT_',
                                    propertyType: 'text',
                                    'default': '{{{KAFKA_BROKER_PORT}}}',
                                    required: true,
                                    tooltip: 'Kafka\'s port.',
                                    width: 2,
                                    qa: 'fragment-details-kafka-port'
                                 }
                              ]
                           },
                           {
                              propertyId: 'value.serializer.outputFormat',
                              propertyName: '_KAFKA_OUTPUT_FORMAT_',
                              propertyType: 'select',
                              values: [
                                 {
                                    label: 'Row',
                                    value: 'ROW'
                                 },
                                 {
                                    label: 'Json',
                                    value: 'JSON'
                                 },
                                 {
                                    label: 'Avro',
                                    value: 'AVRO'
                                 }
                              ],
                              'default': 'ROW',
                              required: true,
                              qa: 'fragment-details-kafka-outputFormat'
                           },
                           {
                              propertyId: 'value.serializer.row.delimiter',
                              propertyName: '_KAFKA_ROW_SEPARATOR_',
                              propertyType: 'text',
                              'default': '{{{DEFAULT_DELIMITER}}}',
                              required: false,
                              visible: [
                                 [
                                    {
                                       propertyId: 'value.serializer.outputFormat',
                                       value: 'ROW'
                                    }
                                 ]
                              ],
                              tooltip: 'Valid only with \'Row\' as serializing format. Character used to parse the data being sent to Kafka.',
                              qa: 'fragment-details-kafka-row-separator'
                           },
                           {
                              propertyId: 'value.serializer.avro.schema.recordNamespace',
                              propertyName: '_KAFKA_AVRO_SCHEMA_RECORD_NAMESPACE_',
                              propertyType: 'text',
                              required: false,
                              visible: [
                                 [
                                    {
                                       propertyId: 'value.serializer.outputFormat',
                                       value: 'AVRO'
                                    }
                                 ]
                              ],
                              tooltip: 'Character located in-between records. Default value set to comma.',
                              qa: 'fragment-details-kafka-avro-recordNamespace'
                           },
                           {
                              propertyId: 'value.serializer.avro.schema.recordName',
                              propertyName: '_KAFKA_AVRO_SCHEMA_RECORD_NAME',
                              propertyType: 'text',
                              required: false,
                              visible: [
                                 [
                                    {
                                       propertyId: 'value.serializer.outputFormat',
                                       value: 'AVRO'
                                    }
                                 ]
                              ],
                              tooltip: 'Name given to the record.',
                              qa: 'fragment-details-kafka-avro-topLevelRecord'
                           },
                           {
                              propertyId: 'value.serializer.avro.schema.fromRow',
                              propertyName: '_KAFKA_AVRO_SCHEMA_FROM_ROW_',
                              propertyType: 'boolean',
                              'default': false,
                              required: false,
                              tooltip: 'The schema will be automatically retrieved by the application from the output data.',
                              visible: [
                                 [
                                    {
                                       propertyId: 'value.serializer.outputFormat',
                                       value: 'AVRO'
                                    }
                                 ]
                              ],
                              qa: 'fragment-details-kafka-avro-fromRow'
                           },
                           {
                              propertyId: 'value.serializer.avro.schema.provided',
                              propertyName: '_DESERIALIZER_AVRO_SCHEMA_',
                              propertyType: 'textarea',
                              contentType: 'JSON',
                              width: 8,
                              tooltip: 'Avro schema expressed in JSON format.',
                              visible: [
                                 [
                                    {
                                       propertyId: 'value.serializer.outputFormat',
                                       value: 'AVRO'
                                    },
                                    {
                                       propertyId: 'value.serializer.avro.schema.fromRow',
                                       value: false
                                    }
                                 ]
                              ],
                              required: false,
                              qa: 'workflow-transformation-json-schema-provided'
                           },
                           {
                              propertyId: 'keySeparator',
                              propertyName: '_KEY_SEPARATOR_',
                              propertyType: 'text',
                              'default': ',',
                              required: false,
                              tooltip: 'Specifies the separator when a key or keys are used as partitioning values.',
                              qa: 'fragment-details-kafka-keySeparator'
                           },
                           {
                              propertyId: 'acks',
                              propertyName: '_ACKS_',
                              propertyType: 'text',
                              'default': '0',
                              required: false,
                              tooltip: 'Specifies how many acknowledgment signals the producer will expect from the broker.',
                              qa: 'fragment-details-kafka-request-required-acks'
                           },
                           {
                              propertyId: 'batch.size',
                              propertyName: '_BATCH_NUM_MESSAGES_',
                              propertyType: 'text',
                              'default': '200',
                              required: false,
                              tooltip: 'Number of messages to send in one batch.',
                              qa: 'fragment-details-kafka-batch-num-messages'
                           },
                           {
                              propertyId: 'security.protocol',
                              propertyName: '_SECURITY_PROTOCOL_',
                              propertyType: 'select',
                              regexp: 'PLAINTEXT|SSL',
                              values: [
                                 {
                                    label: 'PLAINTEXT',
                                    value: 'PLAINTEXT'
                                 },
                                 {
                                    label: 'SSL',
                                    value: 'SSL'
                                 }
                              ],
                              'default': 'PLAINTEXT',
                              required: true,
                              qa: 'fragment-details-kafka-security-protocol'
                           },
                           {
                              propertyId: 'ssl.client.auth',
                              propertyName: '_SECURITY_AUTH_',
                              propertyType: 'select',
                              values: [
                                 {
                                    label: 'none',
                                    value: 'none'
                                 },
                                 {
                                    label: 'required',
                                    value: 'required'
                                 },
                                 {
                                    label: 'requested',
                                    value: 'requested'
                                 }
                              ],
                              'default': 'none',
                              required: true,
                              tooltip: 'Client authentication. The use of requested is discouraged, as it provides a false sense of security and misconfigured clients can still connect.',
                              qa: 'fragment-details-kafka-security-auth'
                           },
                           {
                              propertyId: 'tlsEnabled',
                              propertyName: '_TLS_ENABLE_',
                              propertyType: 'boolean',
                              tooltip: 'Retrieves the necessary certificates from Vault and uses them to establish a TLS connection between Spark and Kafka.',
                              'default': false,
                              qa: 'fragment-details-kafka-tls'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'If checked, this output can be used to send error events. This feature can be configured through the errors management settings.',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           },
                           {
                              propertyId: 'saveOptions',
                              propertyName: '_KAFKA_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'fragment-details-kafka-properties',
                              fields: [
                                 {
                                    propertyId: 'saveOptionsKey',
                                    propertyName: '_KAFKA_PROPERTY_KEY_',
                                    propertyType: 'text',
                                    width: 4,
                                    required: false,
                                    qa: 'fragment-details-kafka-kafkaPropertyKey'
                                 },
                                 {
                                    propertyId: 'saveOptionsValue',
                                    propertyName: '_KAFKA_PROPERTY_VALUE_',
                                    propertyType: 'text',
                                    width: 4,
                                    required: false,
                                    qa: 'fragment-details-kafka-kafkaPropertyValue'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Output'
                  },
                  {
                     name: 'MongoDb',
                     value: {
                        name: 'MongoDb',
                        icon: 'MongoDb',
                        className: 'MongoDbOutputStep',
                        classPrettyName: 'MongoDb',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        description: 'MongoDB is an open-source document database that provides high performance, high availability, and automatic scaling.',
                        properties: [
                           {
                              propertyId: 'hosts',
                              propertyName: '_HOSTS_',
                              propertyType: 'list',
                              required: true,
                              tooltip: 'This section let us specify the parameters needed to establish a MongoDB connection, with different replica set or with sharding.',
                              qa: 'fragment-details-mongoDb-hosts',
                              fields: [
                                 {
                                    propertyId: 'host',
                                    propertyName: '_HOST_',
                                    propertyType: 'text',
                                    'default': '{{{MONGODB_HOST}}}',
                                    required: true,
                                    width: 6,
                                    qa: 'fragment-details-mongoDb-hostName'
                                 },
                                 {
                                    propertyId: 'port',
                                    propertyName: '_PORT_',
                                    propertyType: 'text',
                                    'default': '{{{MONGODB_PORT}}}',
                                    required: true,
                                    width: 2,
                                    qa: 'fragment-details-mongoDb-port'
                                 }
                              ]
                           },
                           {
                              propertyId: 'dbName',
                              propertyName: '_DATEBASE_NAME_',
                              propertyType: 'text',
                              regexp: '',
                              'default': '{{{MONGODB_DB}}}',
                              required: true,
                              tooltip: 'Default value set to sparta.',
                              qa: 'fragment-details-mongoDb-dbName'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'If checked, this output can be used to send error events. This feature can be configured through the errors management settings.',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           }
                        ]
                     },
                     stepType: 'Output'
                  },
                  {
                     name: 'Parquet',
                     value: {
                        name: 'Parquet',
                        icon: 'Parquet',
                        className: 'ParquetOutputStep',
                        classPrettyName: 'Parquet',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        description: 'Apache Parquet is a columnar storage format available to any project in the Hadoop ecosystem, regardless of the choice of data processing framework, data model or programming language.',
                        properties: [
                           {
                              propertyId: 'path',
                              propertyName: '_PATH_',
                              propertyType: 'text',
                              required: true,
                              tooltip: 'Path to store the data in a compatible filesystem (HDFS, S3, Azure, etc). Please note that, in order to write to the HDFS specified in the tenant configuration, also relative paths can be used (e.g. /user/sparta-server/) without the namenode details (e.g. hdfs:///addressNamenode:8020/user/sparta-server/).',
                              qa: 'fragment-details-parquet-path'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'If checked, this output can be used to send error events. This feature can be configured through the errors management settings.',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           },
                           {
                              propertyId: 'saveOptions',
                              propertyName: '_PARQUET_SAVE_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              tooltip: '',
                              qa: 'fragment-details-parquet-save-properties',
                              fields: [
                                 {
                                    propertyId: 'saveOptionsKey',
                                    propertyName: '_SAVE_OPTIONS_KEY_',
                                    propertyType: 'text',
                                    width: 4,
                                    required: false,
                                    qa: 'fragment-details-parquet-saveOptionsKey'
                                 },
                                 {
                                    propertyId: 'saveOptionsValue',
                                    propertyName: '_SAVE_OPTIONS_VALUE_',
                                    propertyType: 'text',
                                    width: 4,
                                    required: false,
                                    qa: 'fragment-details-parquet-saveOptionsValue'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Output'
                  },
                  {
                     name: 'Postgres',
                     value: {
                        name: 'Postgres',
                        icon: 'Postgres',
                        className: 'PostgresOutputStep',
                        classPrettyName: 'Postgres',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        description: 'Given a URL to a Postgres Database, it is possible to write data into multiple tables whose names will be retrieved from the connected previous steps configuration. In order to improve the JDBC writing performance it would be better to use the Postgres native function COPY IN.',
                        properties: [
                           {
                              propertyId: 'url',
                              propertyName: '_JDBC_URL_',
                              propertyType: 'text',
                              'default': '{{{POSTGRES_URL}}}',
                              required: true,
                              tooltip: 'Url to connect to a Postgres Database.',
                              qa: 'fragment-details-postgres-url'
                           },
                           {
                              propertyId: 'driver',
                              propertyName: '_JDBC_DRIVER_',
                              propertyType: 'text',
                              required: false,
                              tooltip: 'Driver class sent to the Spark configuration. Default value set to \'org.postgresql.Driver\'.',
                              qa: 'fragment-details-jdbc-driver'
                           },
                           {
                              propertyId: 'isolationLevel',
                              propertyName: '_ISOLATION_LEVEL_',
                              propertyType: 'select',
                              values: [
                                 {
                                    label: 'None',
                                    value: 'NONE'
                                 },
                                 {
                                    label: 'Read Uncommitted',
                                    value: 'READ_UNCOMMITTED'
                                 },
                                 {
                                    label: 'Read Committed',
                                    value: 'READ_COMMITTED'
                                 },
                                 {
                                    label: 'Repeatable Read',
                                    value: 'REPEATABLE_READ'
                                 },
                                 {
                                    label: 'Serializable',
                                    value: 'SERIALIZABLE'
                                 }
                              ],
                              'default': 'READ_UNCOMMITTED',
                              required: true,
                              tooltip: 'Sets the isolation level when saving the data in the database.',
                              qa: 'fragment-postgres-isolation-level'
                           },
                           {
                              propertyId: 'postgresSaveMode',
                              propertyName: '_POSTGRES_SAVE_MODE_',
                              propertyType: 'select',
                              values: [
                                 {
                                    label: 'Batch statement',
                                    value: 'STATEMENT'
                                 },
                                 {
                                    label: 'Single statement',
                                    value: 'SINGLE_STATEMENT'
                                 },
                                 {
                                    label: 'One transaction',
                                    value: 'ONE_TRANSACTION'
                                 },
                                 {
                                    label: 'CopyIn',
                                    value: 'COPYIN'
                                 }
                              ],
                              'default': 'STATEMENT',
                              required: true,
                              tooltip: 'Sets the save mode used by Postgres to persist the resulting transformed data. \'Batch statement\' groups and saves the data in n batches defined by the user. \'Single statement\': one transaction will be used per every row of data to be saved. \'One transaction\': All the data will be saved with only one transaction. \'Copy in\': the data will be grouped in bulks and sent into the PostgreSQL via the native COPY IN function.',
                              qa: 'fragment-postgres-save-mode'
                           },
                           {
                              propertyId: 'batchsize',
                              propertyName: '_JDBC_BATCH_SIZE_',
                              propertyType: 'text',
                              visible: [
                                 [
                                    {
                                       propertyId: 'postgresSaveMode',
                                       value: 'STATEMENT'
                                    }
                                 ]
                              ],
                              'default': '1000',
                              required: false,
                              tooltip: 'Number of upsert statements that are sent to the database on each partition.',
                              qa: 'fragment-details-jdbc-batch-size'
                           },
                           {
                              propertyId: 'delimiter',
                              propertyName: '_POSTGRES_DELIMITER_',
                              propertyType: 'text',
                              visible: [
                                 [
                                    {
                                       propertyId: 'postgresSaveMode',
                                       value: 'COPYIN'
                                    }
                                 ]
                              ],
                              'default': '\t',
                              required: false,
                              tooltip: 'Delimiter character used when transforming to CSV. It is mandatory that this character is not contained in the events being saved.',
                              qa: 'fragment-details-postgres-delimiter'
                           },
                           {
                              propertyId: 'newLineSubstitution',
                              propertyName: '_POSTGRES_NEW_LINE_SUBSTITUTION_',
                              propertyType: 'text',
                              'default': ' ',
                              visible: [
                                 [
                                    {
                                       propertyId: 'postgresSaveMode',
                                       value: 'COPYIN'
                                    }
                                 ]
                              ],
                              required: false,
                              tooltip: 'Character used to substitute the Linefeed (new line) character in the events. Default value set to a whitespace.',
                              qa: 'fragment-details-postgres-new-line-substitution'
                           },
                           {
                              propertyId: 'newQuotesSubstitution',
                              propertyName: '_POSTGRES_NEW_QUOTE_SUBSTITUTION_',
                              propertyType: 'text',
                              'default': '',
                              visible: [
                                 [
                                    {
                                       propertyId: 'postgresSaveMode',
                                       value: 'COPYIN'
                                    }
                                 ]
                              ],
                              required: false,
                              tooltip: 'Character used to substitute the quoting character in the events. Default value set to ASCII Backspace.',
                              qa: 'fragment-details-postgres-new-quote-substitution'
                           },
                           {
                              propertyId: 'encoding',
                              propertyName: '_POSTGRES_ENCODING_',
                              propertyType: 'text',
                              'default': 'UTF8',
                              visible: [
                                 [
                                    {
                                       propertyId: 'postgresSaveMode',
                                       value: 'COPYIN'
                                    }
                                 ]
                              ],
                              required: false,
                              tooltip: 'Charset encoding to use during saving.',
                              qa: 'fragment-details-postgres-encoding'
                           },
                           {
                              propertyId: 'failFast',
                              propertyName: '_FAIL_FAST_',
                              propertyType: 'boolean',
                              visibleOR: [
                                 [
                                    {
                                       propertyId: 'postgresSaveMode',
                                       value: 'STATEMENT'
                                    },
                                    {
                                       propertyId: 'postgresSaveMode',
                                       value: 'SINGLE_STATEMENT'
                                    }
                                 ]
                              ],
                              'default': true,
                              required: true,
                              tooltip: 'If enabled, the workflow will fail once it encounters its first error. If it is not enabled, errors are ignored.',
                              qa: 'fragment-details-postgres-failfast'
                           },
                           {
                              propertyId: 'dropTemporalTableSuccess',
                              propertyName: '_DROP_TEMP_TABLE_SUCCESS_',
                              propertyType: 'boolean',
                              visible: [
                                 [
                                    {
                                       propertyId: 'postgresSaveMode',
                                       value: 'ONE_TRANSACTION'
                                    }
                                 ]
                              ],
                              'default': true,
                              'float': true,
                              required: true,
                              tooltip: 'If checked, the temporal table used when choosing the \'One transaction\' save mode to persist intermediate records is deleted. These table are always named with the suffix _tmp_{Timestamp}.',
                              qa: 'fragment-details-postgres-droptemptable-success'
                           },
                           {
                              propertyId: 'dropTemporalTableFailure',
                              propertyName: '_DROP_TEMP_TABLE_FAILURE_',
                              propertyType: 'boolean',
                              visible: [
                                 [
                                    {
                                       propertyId: 'postgresSaveMode',
                                       value: 'ONE_TRANSACTION'
                                    }
                                 ]
                              ],
                              'default': false,
                              required: true,
                              tooltip: 'If checked, the temporal table used when choosing the \'One transaction\' save mode to persist intermediate records is deleted. These table are always named with the suffix _tmp_{Timestamp}.',
                              qa: 'fragment-details-postgres-droptemptable-failure'
                           },
                           {
                              propertyId: 'tlsEnabled',
                              propertyName: '_TLS_ENABLE_',
                              propertyType: 'boolean',
                              tooltip: 'Retrieves the necessary certificates from Vault and uses them to establish a TLS connection between Spark and PostgresSQL.',
                              'default': false,
                              qa: 'fragment-details-postgres-tls'
                           },
                           {
                              propertyId: 'schemaFromDatabase',
                              propertyName: '_SCHEMA_FROM_DATABASE_',
                              propertyType: 'boolean',
                              tooltip: 'Tells the workflow to extract the schema from the output database.',
                              'default': false,
                              qa: 'fragment-details-postgres-schema-database'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'If checked, this output can be used to send error events. This feature can be configured through the errors management settings.',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           },
                           {
                              propertyId: 'saveOptions',
                              propertyName: '_POSTGRES_SAVE_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'fragment-details-postgres-save-properties',
                              fields: [
                                 {
                                    propertyId: 'saveOptionsKey',
                                    propertyName: '_SAVE_OPTIONS_KEY_',
                                    propertyType: 'text',
                                    width: 4,
                                    hidden: false,
                                    required: false,
                                    qa: 'fragment-details-postgres-saveOptionsKey'
                                 },
                                 {
                                    propertyId: 'saveOptionsValue',
                                    propertyName: '_SAVE_OPTIONS_VALUE_',
                                    propertyType: 'text',
                                    width: 4,
                                    hidden: false,
                                    required: false,
                                    qa: 'fragment-details-postgres-saveOptionsValue'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Output'
                  },
                  {
                     name: 'Redis',
                     value: {
                        name: 'Redis',
                        icon: 'Redis',
                        className: 'RedisOutputStep',
                        classPrettyName: 'Redis',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        description: 'Redis is an open source (BSD licensed), in-memory data structure store, used as database, cache and message broker. The output doesn\'t use the generic implementation with DataFrames',
                        properties: [
                           {
                              propertyId: 'hostname',
                              propertyName: '_HOST_',
                              propertyType: 'text',
                              'default': '{{{REDIS_HOST}}}',
                              required: true,
                              tooltip: '',
                              qa: 'fragment-details-redis-hostname'
                           },
                           {
                              propertyId: 'port',
                              propertyName: '_PORT_',
                              propertyType: 'text',
                              'default': '{{{REDIS_PORT}}}',
                              required: true,
                              tooltip: '',
                              qa: 'fragment-details-redis-port'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'This output can be used for sending errors in the errors management settings',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           }
                        ]
                     },
                     stepType: 'Output'
                  },
                  {
                     name: 'Print',
                     value: {
                        name: 'Print',
                        icon: 'Print',
                        className: 'PrintOutputStep',
                        classPrettyName: 'Print',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        description: 'Prints the number of test elements, schemas and data.',
                        properties: [
                           {
                              propertyId: 'printMetadata',
                              propertyName: '_PRINT_META_DATA_',
                              propertyType: 'boolean',
                              'default': true,
                              required: false,
                              tooltip: 'Specifies if the metadata is printed by the logger depending on the level set',
                              qa: 'fragment-details-print-printMetadata'
                           },
                           {
                              propertyId: 'printData',
                              propertyName: '_PRINT_DATA_',
                              propertyType: 'boolean',
                              'default': false,
                              required: false,
                              tooltip: 'Specifies if the data is printed by the logger depending on the level set',
                              qa: 'fragment-details-print-printData'
                           },
                           {
                              propertyId: 'printSchema',
                              propertyName: '_PRINT_SCHEMA_',
                              propertyType: 'boolean',
                              'default': false,
                              required: false,
                              tooltip: 'Specifies if the schema is printed by the logger depending on the level set',
                              qa: 'fragment-details-print-printSchema'
                           },
                           {
                              propertyId: 'logLevel',
                              propertyName: '_LOG_LEVEL_',
                              propertyType: 'select',
                              regexp: 'info|error|warn',
                              values: [
                                 {
                                    label: 'Warning',
                                    value: 'warn'
                                 },
                                 {
                                    label: 'Error',
                                    value: 'error'
                                 },
                                 {
                                    label: 'Info',
                                    value: 'info'
                                 }
                              ],
                              'default': 'warn',
                              required: true,
                              tooltip: 'Sets the log level when printing the data.',
                              qa: 'fragment-details-output-print-logLevel'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'If checked, this output can be used to send error events. This feature can be configured through the errors management settings.',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           }
                        ]
                     },
                     stepType: 'Output'
                  },
                  {
                     name: 'Text',
                     value: {
                        name: 'Text',
                        icon: 'Text',
                        className: 'TextOutputStep',
                        classPrettyName: 'Text',
                        arity: [
                           'NullaryToNullary',
                           'NaryToNullary'
                        ],
                        supportedEngines: [
                           'Streaming',
                           'Batch'
                        ],
                        description: 'Saves the processed data to text file into any storage service',
                        properties: [
                           {
                              propertyId: 'path',
                              propertyName: '_PATH_',
                              propertyType: 'text',
                              regexp: '',
                              'default': '',
                              required: true,
                              tooltip: '',
                              qa: 'fragment-details-filesystem-path'
                           },
                           {
                              propertyId: 'delimiter',
                              propertyName: '_DELIMITER_',
                              propertyType: 'text',
                              tooltip: 'Only eligible if output format is set to Row. Any character is accepted except: \\ " #',
                              'default': '{{{DEFAULT_DELIMITER}}}',
                              trim: false,
                              required: false,
                              qa: 'fragment-details-filesystem-delimiter'
                           },
                           {
                              propertyId: 'errorSink',
                              propertyName: '_ERROR_SINK_',
                              propertyType: 'boolean',
                              tooltip: 'This output can be used for sending errors in the errors management settings',
                              'default': false,
                              qa: 'fragment-details-errorSink'
                           },
                           {
                              propertyId: 'saveOptions',
                              propertyName: '_TEXT_SAVE_PROPERTIES_',
                              propertyType: 'list',
                              required: false,
                              qa: 'fragment-details-parquet-save-properties',
                              fields: [
                                 {
                                    propertyId: 'saveOptionsKey',
                                    propertyName: '_SAVE_OPTIONS_KEY_',
                                    propertyType: 'text',
                                    width: 4,
                                    required: false,
                                    qa: 'fragment-details-parquet-saveOptionsKey'
                                 },
                                 {
                                    propertyId: 'saveOptionsValue',
                                    propertyName: '_SAVE_OPTIONS_VALUE_',
                                    propertyType: 'text',
                                    width: 4,
                                    required: false,
                                    qa: 'fragment-details-parquet-saveOptionsValue'
                                 }
                              ]
                           }
                        ]
                     },
                     stepType: 'Output'
                  }
               ]
            }
         ],
            notification: {
            type: '',
               message: '',
               templateType: ''
         }
      },
      debug: {
         isDebugging: false,
            lastDebugResult: null,
            showDebugConsole: false,
            debugConsoleSelectedTab: 'Exceptions',
            showedDebugDataEntity: ''
      },
      externalData: {
         environmentVariables: [
            {
               name: 'CASSANDRA_CLUSTER',
               value: 'sparta'
            },
            {
               name: 'CASSANDRA_HOST',
               value: 'localhost'
            }
         ]
      }
   }
};
