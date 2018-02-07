@rest @web
Feature: [SPARTA-1196] Generate and Execute Workflow and see Streaming

  Background: conect to navigator
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user 'admin' and password '1234'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    And I wait '10' seconds
  @loop(WORKFLOW_LIST,WORKFLOW)
  Scenario:[SPARTA-1196][01]Generate report of Spark Streaming '<WORKFLOW>'
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    Given in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep -w '<WORKFLOW>' | wc -l' contains '1'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/<WORKFLOW>/<WORKFLOW>-v0 awk '{print $5}' | grep <WORKFLOW> ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    #Check workflow is runing in DCOS
    And I wait '1' seconds
    And  I run 'echo !{workflowTaskId}' in the ssh connection
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep TASK_RUNNING | wc -l' contains '1'
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/<WORKFLOW>/<WORKFLOW>-v0  | grep ${DCOS_SERVICE_NAME} | awk '{print $2}'' contains 'True'

    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/<WORKFLOW>/<WORKFLOW>-v0 | awk '{print $2}'' contains 'True'

   #Get PORT and IP of workflow
    And I run 'dcos marathon app show /sparta/${DCOS_SERVICE_NAME}/workflows/home/<WORKFLOW>/<WORKFLOW>-v0 |jq '.tasks[0].ports' |sed -n 2p | sed 's/ //g'' in the ssh connection and save the value in environment variable 'workflowPORT'
    And I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/<WORKFLOW>/<WORKFLOW>-v0 | awk '{print $4}'| awk 'NR ==2'' in the ssh connection and save the value in environment variable 'workflowIP'
  #show streaming workflow
    Given My app is running in '!{workflowIP}:!{workflowPORT}'
    When I browse to '/streaming'
  #Check streaming process
    Then '1' element exists with 'id:completed'
  #Take evidence of streaming
    And I take a snapshot

    When I browse to '/executors'
    Then '1' element exists with 'id:active-executors'
    And I wait '5' seconds
    And I take a snapshot

    #MVN Example
    #mvn verify -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.SPARTA_1196_ExecuteListWorkflows_IT -DCLUSTER_ID='newcore' -DWORKFLOW_LIST=testinput-kafka,kafka-elastic  -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-newcore.demo.stratio.com -DDCOS_SERVICE_NAME=sparta-server