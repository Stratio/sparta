@rest @web
Feature: [SPARTA-1196] Generate and Execute Workflow and see Streaming

  Background: [SetUp] Setup EOS GUI
    Given My app is running in '${CLUSTER_ID}.labs.stratio.com:443'
    When I securely browse to '/service/${DCOS_SERVICE_NAME}/#/'
    Then I switch to iframe with 'id:oauth-iframe'

  Scenario: [SPARTA-1196][01]See workflow Details
    And I wait '2' seconds
    When '1' elements exists with 'id:username'
    And I type 'admin' on the element on index '0'
    And '1' elements exists with 'id:password'
    And I type '1234' on the element on index '0'
    And '1' elements exists with 'id:login-button'
    Then I click on the element on index '0'
    And I wait '5' seconds
    Then I take a snapshot

    #MVN Example
    #mvn verify -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.SPARTA_1196_WorkflowDetail_IT -DCLUSTER_ID='nightly' -DWORKFLOW_LIST=testinput-kafka,kafka-elastic  -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-newcore.demo.stratio.com -DDCOS_SERVICE_NAME=sparta-server -DSELENIUM_GRID=localhost:4444 -DFORCE_BROWSER=chrome_48iddiegotest