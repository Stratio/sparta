@rest
Feature: [SPARTA][DCOS] Add sparta policy in gosec

  Background: Setup DCOS-CLI
    #Start SSH with DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
  ########################################
  #Is necesary token to execute this case#
  ########################################
  Scenario: [SPARTA][Gosec]Add zookeper-sparta policy to write in zookeper
    Given I securely send requests to '${GOSEC_URL}:${GOSEC_PORT}'
    When I send a 'POST' request to '/api/policy' based on 'schemas/policy.conf' as 'json' with:
    Then the service response status must be '200'