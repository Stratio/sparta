@rest
Feature: [SPARTA_1196][DCOS]Generate and Execute Workflow and see Streaming
  Background: conect to navigator
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user 'admin' and password '1234'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    And I wait '10' seconds

  Scenario:[SPARTA_1196][01] Execute workflow
    #include workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/policy' based on 'schemas/workflows/${WORKFLOW}.json' as 'json' with:
      | id | DELETE | N/A  |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '10' seconds
    #Execute workflow
    Given I send a 'GET' request to '/service/${DCOS_SERVICE_NAME}/policy/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text '{"message":"Launched policy with name !{nameWorkflow}'
    #verify the generation of  workflow in dcos

  Scenario:[SPARTA_1196][02]Test workflow in Dcos
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    Given in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep -w ${WORKFLOW} | wc -l' contains '1'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/${WORKFLOW}  | awk '{print $5}' | grep ${WORKFLOW} ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    #Check workflow is runing in DCOS
    And I wait '1' seconds
    And  I run 'echo !{workflowTaskId}' in the ssh connection
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep TASK_RUNNING | wc -l' contains '1'
  @ignore @manual
  Scenario:[SPARTA_1196][03] Generate report of Spark Streaming
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/${WORKFLOW} | awk '{print $2}'' contains 'True'
    #Now we give time to generate streaming process
    Then I wait '60' seconds
    #TODO:Change inconditional wait
    #And in less than '\d+' seconds, checking each '\d+' seconds, '\d+' elements exists with '[^:]*:[^:]*'
    #Get PORT and IP of workflow
    And I run 'dcos marathon app show /sparta/${DCOS_SERVICE_NAME}/workflows/${WORKFLOW} |jq '.tasks[0].ports' |sed -n 2p | sed 's/ //g'' in the ssh connection and save the value in environment variable 'workflowPORT'
    And I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/${WORKFLOW} | awk '{print $4}'| awk 'NR ==2'' in the ssh connection and save the value in environment variable 'workflowIP'
    #show streaming workflow
    Given My app is running in '!{workflowIP}:!{workflowPORT}'
    When I browse to '/streaming'
    #Check streaming process
    Then '1' element exists with 'id:completed'
    #Take evidence of streaming
    And I take a snapshot

    When I browse to '/executors'
    Then '1' element exists with 'id:active-executors'
    And I take a snapshot
  @ignore @manual
  Scenario: [SPARTA_1196][04] Delete a existing policy
    Then  I run 'echo !{previousWorkflowID}' in the ssh connection
    Then I run 'echo !{previousWorkflowID}| cut -d'.' -f 2 ' in the ssh connection and save the value in environment variable 'idworkflowDelete'

    When I send a 'DELETE' request to '/policy/!{idworkflowDelete}'
    Then the service response status must be '200'
    When I send a 'GET' request to '/policy/all'
    Then the service response status must be '200' and its response must contain the text '[]'
  @ignore @manual
  Scenario: [SPARTA_1196][05] Remove workflow
    When  I run 'dcos marathon app remove /sparta/${DCOS_SERVICE_NAME}/workflows/${WORKFLOW}' in the ssh connection
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos task | grep ${DCOS_SERVICE_NAME} | wc -l' contains '0'


    #MVN Example
    #mvn verify -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.InstallAndExecuteWorkflowWithStreaming -DCLUSTER_ID='nightly' -DWORKFLOW=workflowfromwebsockettoprint -DFORCE_BROWSER=chrome_48iddiegotest -DSELENIUM_GRID=localhost:4444 -DGOSECMANAGEMENT_PORT=443  -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nightly.demo.stratio.com