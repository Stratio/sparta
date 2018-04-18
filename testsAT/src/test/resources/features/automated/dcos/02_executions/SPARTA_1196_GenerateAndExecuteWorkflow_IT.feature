@rest
Feature: [SPARTA-1196] Generate and Execute Workflow
  Background: conect to navigator
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user 'admin' and password '1234'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    And I wait '10' seconds

  Scenario:[SPARTA-1196][01] Execute workflow
    #include workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows' based on 'schemas/workflows/${WORKFLOW}.json' as 'json' with:
      | id | DELETE | N/A  |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '10' seconds
    #Execute workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text 'OK'
    #verify the generation of  workflow in dcos

  Scenario:[SPARTA-1196][02]Test workflow in Dcos
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    Given in less than '600' seconds, checking each '20' seconds, the command output 'dcos task | grep -w ${WORKFLOW}' contains '${WORKFLOW}'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/${WORKFLOW}  | awk '{print $5}' | grep ${WORKFLOW} ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    #Check workflow is runing in DCOS
    And I wait '1' seconds
    And  I run 'echo !{workflowTaskId}' in the ssh connection
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep TASK_RUNNING' contains 'TASK_RUNNING'
    And in less than '600' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/${WORKFLOW}  | grep ${DCOS_SERVICE_NAME} | awk '{print $2}'' contains 'True'
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/${WORKFLOW}  | awk '{print $5}' | grep ${WORKFLOW} ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep healthCheckResults' contains 'healthCheckResults'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep  '"alive": true'' contains '"alive": true'


    #MVN Example
    #mvn verify -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.InstallAndExecuteWorkflowWithStreaming -DCLUSTER_ID='nightly' -DWORKFLOW=workflowfromwebsockettoprint -DFORCE_BROWSER=chrome_48iddiegotest -DSELENIUM_GRID=localhost:4444 -DGOSECMANAGEMENT_PORT=443  -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nightly.demo.stratio.com