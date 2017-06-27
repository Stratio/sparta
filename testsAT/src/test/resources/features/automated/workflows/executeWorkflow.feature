@rest
Feature: Test workflow execution
  #Generate workflow, execute and delete workflow
  Background: Setup Sparta workflow
    Given I send requests to '${SPARTA_HOST}:${SPARTA_PORT}'

  Scenario: Generate workflow
    Given I send a 'POST' request to '/policy' based on 'schemas/workflows/workflowFromWebsockettoPrint.json' as 'json' with:
      | id | DELETE | N/A |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '2' seconds

  Scenario: Run workflow
    When I send a 'GET' request to '/policy/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text '{"message":"Launched policy with name !{nameWorkflow}'

  Scenario: delete workflow
    When I send a 'DELETE' request to '/policy/!{previousWorkflowID}'
    Then the service response status must be '200'

#Example Execution: mvn verify  -DSPARTA_HOST=localhost -DSPARTA_PORT=9090 -Dit.test=com.stratio.sparta.testsAT.automated.workflows.ISExecuteWorkflow -DlogLevel=DEBUG -Dmaven.failsafe.debu
