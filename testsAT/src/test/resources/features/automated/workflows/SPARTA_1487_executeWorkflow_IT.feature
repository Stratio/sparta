@rest
Feature: [SPARTA-1487] Test workflow execution
  #Generate workflow, execute and delete workflow
  Background: Setup Sparta workflow
    Given I send requests to '${SPARTA_HOST}:${SPARTA_PORT}'

  Scenario: [SPARTA-1487][01] Generate workflow
    Given I send a 'POST' request to '/workflows' based on 'schemas/workflows/${WORKFLOW}' as 'json' with:
      | id | DELETE | N/A |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '2' seconds

  Scenario: [SPARTA-1487][02] Run workflow
    When I send a 'POST' request to '/workflows/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text 'OK'

  Scenario: [SPARTA-1487][03] Delete workflow
    When I send a 'DELETE' request to '/workflows/!{previousWorkflowID}'
    Then the service response status must be '200'

#Example Execution: mvn verify  -DSPARTA_HOST=localhost -DSPARTA_PORT=9090 -Dit.test=com.stratio.sparta.testsAT.automated.workflows.ISExecuteWorkflow -DlogLevel=DEBUG -Dmaven.failsafe.debu
