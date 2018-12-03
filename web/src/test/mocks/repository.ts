/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

export const REPOSITORY_STORE_MOCK = {
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
               name: 'batch1',
               description: 'Ensurance example for ingestion and data cleaning',
               settings: {
                  executionMode: 'marathon',
                  userPluginsJars: [],
                  initSqlSentences: [
                     {
                        sentence: 'CREATE TABLE IF NOT EXISTS sie_stat_corporativas USING com.databricks.spark.csv)'
                     },
                     {
                        sentence: 'CREATE TABLE IF NOT EXISTS dim_comercial USING com.databricks.spark.csv)'
                     },
                     {
                        sentence: 'CREATE TABLE IF NOT EXISTS sie_rem_preguntas USING com.databricks.spark.csv)'
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
                  'ensurance'
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
               tagsAux: 'etl, data cleaning, ensurance',
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
                  name: 'batch2',
                  type: 'Batch',
                  group: '/home',
                  lastUpdateAux: 1525080955000,
                  lastUpdate: '30 Apr 2018 - 11:35',
                  versions: [
                     {
                        id: '37c9264d-66cf-4e9e-aee1-8b98b3139c30',
                        name: 'batch3',
                        description: 'Ensurance example for ingestion and data cleaning',
                        settings: {
                           executionMode: 'marathon',
                           userPluginsJars: [],
                           initSqlSentences: [
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS sie_stat_corporativas USING com.databricks.spark.csv)'
                              },
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS dim_comercial USING com.databricks.spark.csv)'
                              },
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS sie_rem_preguntas USING com.databricks.spark.csv)'
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
                           'ensurance'
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
                        tagsAux: 'etl, data cleaning, ensurance',
                        formattedStatus: 'Failed'
                     },
                     {
                        id: '46fd724a-31f0-4095-acbd-e436a68f37c2',
                        name: 'batch3',
                        description: 'Ensurance example for ingestion and data cleaning',
                        settings: {
                           executionMode: 'marathon',
                           userPluginsJars: [],
                           initSqlSentences: [
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS sie_stat_corporativas USING com.databricks.spark.csv)'
                              },
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS dim_comercial USING com.databricks.spark.csv)'
                              },
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS sie_rem_preguntas USING com.databricks.spark.csv)'
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
                        name: 'batch4',
                        description: 'Ensurance example for ingestion and data cleaning',
                        settings: {
                           executionMode: 'marathon',
                           userPluginsJars: [],
                           initSqlSentences: [
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS sie_stat_corporativas USING com.databricks.spark.csv)'
                              },
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS dim_comercial USING com.databricks.spark.csv)'
                              },
                              {
                                 sentence: 'CREATE TABLE IF NOT EXISTS sie_rem_preguntas USING com.databricks.spark.csv)'
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
   }
};
