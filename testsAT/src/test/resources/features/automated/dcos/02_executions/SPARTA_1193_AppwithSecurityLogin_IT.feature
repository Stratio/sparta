@web @rest
Feature: [SPARTA-1193] Running sparta with security and Login

  Background: Setup DCOS-CLI
    #Start SSH with DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'

  Scenario: [SPARTA-1193][01]Execute Login
    #**************************
    #     SPARTA LOGIN        *
    #**************************
    Given My app is running in 'sparta.${CLUSTER_ID}.labs.stratio.com:443'
    And I securely browse to '/${DCOS_SERVICE_NAME}'
    And I wait '2' seconds
    Then '1' element exists with 'id:username'
    And I type 'sparta' on the element on index '0'
    And '1' element exists with 'id:password'
    And I type 'stratio' on the element on index '0'
    #Verify the login button in sparta
    And '1' element exists with 'css:input[data-qa="login-button-submit"]'
    When I click on the element on index '0'
    Then I wait '2' second
