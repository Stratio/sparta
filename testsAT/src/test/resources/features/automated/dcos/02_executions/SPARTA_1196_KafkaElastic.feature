@rest
Feature: [SPARTA-1196] E2E Execution of Workflow Kafka Elastic 400 Elements
  Background: conect to navigator
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user 'admin' and password '1234'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    And I wait '10' seconds

  #************************************************
  # INSTALL AND EXECUTE TEST-INPUT KAFKA WORKFLOW *
  #************************************************
  Scenario:[SPARTA-1196][01]Install workflow testinput-kafka
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows' based on 'schemas/workflows/testinput-kafka.json' as 'json' with:
      | id | DELETE | N/A  |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '10' seconds

  Scenario:[SPARTA-1196][02] Execute workflow testinput-kafka
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text 'OK'

  #********************************************
  # TEST WORKFLOW IN DCOS TEST-INPUT TO KAFKA**
  #********************************************
  Scenario:[SPARTA_1196][03]Test workflow in Dcos testinput-kafka
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    Given in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep -w testinput-kafka' contains 'testinput-kafka'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/testinput-kafka  | awk '{print $5}' | grep testinput-kafka ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    #Check workflow is runing in DCOS
    And I wait '1' seconds
    And  I run 'echo !{workflowTaskId}' in the ssh connection
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep TASK_RUNNING' contains 'TASK_RUNNING'
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/testinput-kafka  | grep ${DCOS_SERVICE_NAME} | awk '{print $2}'' contains 'True'
    And in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep healthCheckResults' contains 'healthCheckResults'
    And in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep  '"alive": true'' contains '"alive": true'

  #************************************************
  # INSTALL AND EXECUTE TEST-INPUT KAFKA WORKFLOW *
  #************************************************
  Scenario:[SPARTA-1196][04]Install And Execute workflow kafka-elastic
    #include workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows' based on 'schemas/workflows/kafka-elastic.json' as 'json' with:
      | id | DELETE | N/A  |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '10' seconds

  Scenario:[SPARTA_1196][05] Execute workflow kafka-elastic
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text 'OK'
    Given I wait '30' seconds

  Scenario:[SPARTA-1196][06] Test workflow in Dcos kafka-elastic
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    Given in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep -w kafka-elastic' contains 'kafka-elastic'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/kafka-elastic  | awk '{print $5}' | grep kafka-elastic ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    #Check workflow is runing in DCOS
    And I wait '1' seconds
    And  I run 'echo !{workflowTaskId}' in the ssh connection
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep TASK_RUNNING' contains 'TASK_RUNNING'
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/kafka-elastic  | grep ${DCOS_SERVICE_NAME} | awk '{print $2}'' contains 'True'
    And in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep healthCheckResults' contains 'healthCheckResults'
    And in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep  '"alive": true'' contains '"alive": true'
  @ignore @manual
  Scenario:[SPARTA-1196][07] Delete table in Elastic
    Given I send requests to '/coordinator.elasticsearchstratio.l4lb.thisdcos.directory:9200/myindex \ --cacert ca.pem --cert keystore.pem'
    And   I send a 'DELETE' request to '/triggertickets?pretty'
    Then the service response status must be '200'



    #MVN Example
    #mvn verify -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.SPARTA_1196_ExecuteListWorkflows_IT -DCLUSTER_ID='newcore' -DWORKFLOW_LIST=testinput-kafka,kafka-elastic  -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-newcore.demo.stratio.com -DDCOS_SERVICE_NAME=sparta-server