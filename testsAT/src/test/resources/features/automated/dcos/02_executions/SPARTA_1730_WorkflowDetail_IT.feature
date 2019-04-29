@rest @web
Feature: [SPARTA-1196] Generate and Execute Workflow and see Streaming

  Background: [SetUp] Setup EOS GUI
    Given My app is running in '${CLUSTER_ID}.labs.stratio.com:443'
    When I securely browse to '/'
    Then I switch to iframe with 'id:oauth-iframe'

  Scenario: [SPARTA-1196][01]See workflow Details
    And I wait '2' seconds
    When '1' elements exists with 'id:username'
    And I type '${USER:-admin}' on the element on index '0'
    And '1' elements exists with 'id:password'
    And I type '${PASSWORD:-1234}' on the element on index '0'
    And '1' elements exists with 'id:login-button'
    Then I click on the element on index '0'
    And I wait '5' seconds
    Then I take a snapshot

    #MVN Example
    #mvn verify -Dgroups=dcos_workflowDetail -DlogLevel=DEBUG  -DCLUSTER_ID=nightly -DSELENIUM_GRID=sl.demo.stratio.com:4444 -DFORCE_BROWSER=chrome_64sparta