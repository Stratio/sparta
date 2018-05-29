@rest
Feature: [SPARTA-1896] E2E Execution of AuditJob Workflow -Streaming mode
  Background: : conect to navigator
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user 'admin' and password '1234'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    And I wait '3' seconds


  #***********************************************************
  # INSTALL AND EXECUTE AuditJob- streaming Mode                 *
  #***********************************************************
  Scenario:[SPARTA-1896][03] Install AuditJob workflow
    #include workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows' based on 'schemas/workflows/audit-job-workflow.json' as 'json' with:
      | id | DELETE | N/A  |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '10' seconds

  Scenario:[SPARTA-1279][04] Execute audit-job-workflow workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text 'OK'

  #************************************
  # VERIFY audit-job-workflow WORKFLOW*
  #************************************

  Scenario:[SPARTA-1896][05] Test Runing audit-job-workflow in Dcos
    Given in less than '600' seconds, checking each '20' seconds, the command output 'dcos task | grep -w audit-job-workflow' contains 'audit-job-workflow'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/audit-job-workflow/audit-job-workflow-v0  | awk '{print $5}' | grep audit-job-workflow ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    And I wait '2' seconds
    #Check workflow is runing in DCOS
    When  I run 'echo !{workflowTaskId}' in the ssh connection
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep TASK_RUNNING' contains 'TASK_RUNNING'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep healthCheckResults' contains 'healthCheckResults'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep  '"alive": true'' contains '"alive": true'
    #Its necesary to wait to execute another workflow
    And I wait '30' seconds

  @web
  Scenario:[SPARTA-1656][01]Generate report of Spark Streaming Audit-Job
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/audit-job-workflow/audit-job-workflow-v0 | awk '{print $5}' | grep audit-job-workflow ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    #Check workflow is runing in DCOS
    And I wait '1' seconds
    And  I run 'echo !{workflowTaskId}' in the ssh connection
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep TASK_RUNNING | wc -l' contains '1'
    And in less than '600' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/audit-job-workflow/audit-job-workflow-v0  | grep ${DCOS_SERVICE_NAME} | awk '{print $2}'' contains 'True'

    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    And in less than '600' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/audit-job-workflow/audit-job-workflow-v0 | awk '{print $2}'' contains 'True'
    #Get PORT and IP of workflow
    And I run 'dcos marathon app show /sparta/${DCOS_SERVICE_NAME}/workflows/home/audit-job-workflow/audit-job-workflow-v0 |jq '.tasks[0].ports' |sed -n 2p | sed 's/ //g'' in the ssh connection and save the value in environment variable 'workflowPORT'
    And I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/audit-job-workflow/audit-job-workflow-v0 | awk '{print $4}'| awk 'NR ==2'' in the ssh connection and save the value in environment variable 'workflowIP'
    #show streaming workflow
    Given My app is running in '!{workflowIP}:!{workflowPORT}'
    When I browse to '/streaming'
    #Check streaming process
    Then '1' element exists with 'id:completed'
    #Take evidence of streaming
    And I take a snapshot
    
  Scenario:[SPARTA-1896][06] Test stop AuditJob streaming workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows/stop/!{previousWorkflowID}'
    # Wait for stop streaming mode process when finish task
    Given in less than '800' seconds, checking each '10' seconds, the command output 'dcos task | grep !{workflowTaskId} | wc -l' contains '0'

  Scenario: [SPARTA-1277][09] Remove workflow
    Given I send a 'DELETE' request to '/service/${DCOS_SERVICE_NAME}/workflows/!{previousWorkflowID}'
    Then the service response status must be '200'

#MVN Example
# mvn verify -DCLUSTER_ID=nightly  -DDCOS_SERVICE_NAME=sparta-server -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.SPARTA_1896_AuditJob_IT -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nightly.demo.stratio.com -DFORCE_BROWSER=chrome_64sparta -DSELENIUM_GRID=sl.demo.stratio.com:4444 -DSPARTA_HOST=localhost -DSPARTA_PORT=9090