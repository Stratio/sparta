@rest
Feature: [SPARTA-1487] Test secure workflow execution
  Background: Setup Sparta workflow
    Given I securely send requests to '${SPARTA_HOST}:${SPARTA_PORT}'
  Scenario:[SPARTA-1487][01] Generate workflow
    Given I send a 'POST' request to '/workflows' based on 'schemas/workflows/${WORKFLOW}.json' as 'json' with:
      | id | DELETE | N/A |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '2' seconds
  Scenario:[SPARTA-1487][02] Run workflow
    When I send a 'GET' request to '/workflows/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text '{"message":"Launched policy with name !{nameWorkflow}'
  Scenario:[SPARTA-1487][03] delete workflow
    When I send a 'DELETE' request to '/workflows/!{previousWorkflowID}'
    Then the service response status must be '200'

#Example Execution: mvn verify  -DSPARTA_HOST=10.200.0.148 -DSPARTA_PORT=24922 -Dit.test=com.stratio.sparta.testsAT.automated.workflows.ISExecuteWorkflowSecurely -DlogLevel=DEBUG -Dmaven.failsafe.debu
