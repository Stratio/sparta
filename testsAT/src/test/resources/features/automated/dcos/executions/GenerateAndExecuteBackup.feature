@rest
Feature: [SPARTA][DCOS] Generate and Execute Backup
  Background: conect to navigator
    Given I set sso token using host '${ENVIROMENT_URL}' with user '${USERNAME}' and password '${PASSWORD}'
    And I securely send requests to '${ENVIROMENT_URL}'

  Scenario: [SPARTA][Backup][01] GenerateBackup
    #include workflow
    Given I send a 'GET' request to '/service/sparta-server/metadata/backup/build'
    Then the service response status must be '200'
  Scenario: [SPARTA][Backup][02] FindBackup
    #include workflow
    Given I send a 'GET' request to '/service/sparta-server/metadata/backup'
    Then the service response status must be '200'
    And I wait '2' seconds