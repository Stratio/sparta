@rest
Feature: [SPARTA-1189] Generate and Execute Backup
  Background: Conect to navigator
    Given I set sso token using host '${ENVIROMENT_URL}' with user '${USERNAME}' and password '${PASSWORD}'
    And I securely send requests to '${ENVIROMENT_URL}'

  Scenario: [SPARTA-1189][01] Generate Backup
    #include workflow
    Given I send a 'GET' request to '/service/sparta-server/metadata/backup/build'
    Then the service response status must be '200'
  Scenario: [SPARTA-1189][02] Find Backup
    #include workflow
    Given I send a 'GET' request to '/service/sparta-server/metadata/backup'
    Then the service response status must be '200'
    And I wait '2' seconds