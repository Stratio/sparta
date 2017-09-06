@rest @web
Feature: [SPARTA][DCOS]Install Execute Workflow and see Streaming
  Background: conect to navigator
    Given I set sso token using host '${ENVIROMENT_URL}' with user '${USERNAME}' and password '${PASSWORD}'
    And I securely send requests to '${ENVIROMENT_URL}'
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
  Scenario:Install and execute workflow
    #include workflow
    Given I send a 'POST' request to '/service/sparta-server/policy' based on 'schemas/workflows/${WORKFLOW}.json' as 'json' with:
      | id | DELETE | N/A  |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    #Execute workflow
    Given I send a 'GET' request to '/service/sparta-server/policy/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text '{"message":"Launched policy with name !{nameWorkflow}'
    #verify the generation of  workflow in dcos
  Scenario: Test workflow in Dcos
    Then in less than '300' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/sparta-server/workflows/${WORKFLOW} | awk '{print $2}'' contains 'True'
    #And I run 'dcos marathon task list /sparta/sparta-server/sparta-server | awk '{print $5}' | grep sparta-server' in the ssh connection and save the value in environment variable 'spartaTaskId'
  Scenario: Streaming Workflow
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/sparta-server/workflows/${WORKFLOW} | awk '{print $2}'' contains 'True'
    #Now we give time to generate streaming process
    Then I wait '30' seconds
    #Get PORT and IP of workflow
    And I run 'dcos marathon app show /sparta/sparta-server/workflows/${WORKFLOW} |jq '.tasks[0].ports' |sed -n 2p | sed 's/ //g'' in the ssh connection and save the value in environment variable 'workflowPORT'
    And I run 'dcos marathon task list /sparta/sparta-server/workflows/${WORKFLOW} | awk '{print $4}'| awk 'NR ==2'' in the ssh connection and save the value in environment variable 'workflowIP'

    #show streaming workflow
    Given My app is running in '!{workflowIP}:!{workflowPORT}'
    When I browse to '/streaming'
    #Check streaming process
    Then '1' element exists with 'id:completed'
    #Take evidence of streaming
    And I take a snapshot
