@rest @web
Feature: [SPARTA-1183] Install and Execute Workflow
  Background: conect to navigator
    Given I set sso token using host '${ENVIROMENT_URL}' with user '${USERNAME}' and password '${PASSWORD}'
    And I securely send requests to '${ENVIROMENT_URL}'
  Scenario: [SPARTA-1183][01] Install and execute workflow
    #include workflow
    Given I send a 'POST' request to '/service/sparta-server/workflows' based on 'schemas/workflows/${WORKFLOW}.json' as 'json' with:
      | id | DELETE | N/A  |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '2' seconds
    #Execute workflow
    Given I send a 'GET' request to '/service/sparta-server/workflows/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text '{"message":"Launched policy with name !{nameWorkflow}'
    #verify the generation of  workflow in dcos
  Scenario: [SPARTA-1183][02] Worklflow test in Dcos
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    Then in less than '300' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/sparta-server/workflows/${WORKFLOW} | awk '{print $2}'' contains 'True'
