@web @rest
Feature: [SPARTA][DCOS]Streaming screenshots for Sparta Workflow

  Background: Streaming screenshots for workflow
    #Start SSH with DCOS-CLI


  Scenario: [SPARTA][Scenario-1][01]Running sparta without security
    Given My app is running in '${WORKFLOW_URL}:${WORKFLOW_PORT}'
    When I browse to '/streaming'
    Then '1' element exists with 'id:completed'
    And I take a snapshot
    #Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
    #When I click on the element on index '0'



# Example of execution with mvn :
#  mvn verify -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.ISworkflowScreenShots -DWORKFLOW_URL=192.168.0.85 -DWORKFLOW_PORT=2158 -DFORCE_BROWSER=chrome_48iddiegotest -DSELENIUM_GRID=localhost:4444

