@rest @web
Feature: [SPARTA-1196] Generate and Execute Workflow and see Streaming
  Background: conect to navigator
    Given My app is running in '${CLUSTER_ID}.labs.stratio.com/service/${DCOS_SERVICE_NAME}/#'
  Scenario: [SPARTA-1196][01]See workflow Details
    When I securely browse to '/workflows'
    And I wait '2' seconds
    When '1' elements exists with 'id:username'
    And I type 'admin' on the element on index '0'
    And '1' elements exists with 'id:password'
    And I type '1234' on the element on index '0'
    And '1' elements exists with 'id:login-button'
    Then I click on the element on index '0'
    And I wait '5' seconds
    Then '2' element exists with 'xpath://table[@id='table-qa-tag']/tbody/tr/td[2]/label'
    When I click on the element on index '0'
    And I wait '5' seconds
    Then '1' element exists with 'id:composition'
    Then I take a snapshot

    #MVN Example
    #mvn verify -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.SPARTA_1196_WorkflowDetail_IT -DCLUSTER_ID='nightly' -DWORKFLOW_LIST=testinput-kafka,kafka-elastic  -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-newcore.demo.stratio.com -DDCOS_SERVICE_NAME=sparta-server -DSELENIUM_GRID=localhost:4444 -DFORCE_BROWSER=chrome_48iddiegotest