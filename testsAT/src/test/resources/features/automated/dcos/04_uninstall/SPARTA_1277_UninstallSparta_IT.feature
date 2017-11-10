@rest
Feature: [SPARTA-1182]Unistall Sparta with mustache
  Background: Setup DCOS-CLI
    #Start SSH with DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'

  Scenario: [SPARTA-1182][Scenario-01] Remove Instalation with full security in DCOS
    When  I run 'dcos marathon app remove /sparta/${DCOS_SERVICE_NAME}/${DCOS_SERVICE_NAME}' in the ssh connection
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos task | grep ${DCOS_SERVICE_NAME} | wc -l' contains '0'
