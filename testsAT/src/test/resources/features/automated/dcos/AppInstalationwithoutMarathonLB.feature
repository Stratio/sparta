@rest
Feature: [SPARTA][DCOS]Instalation sparta without security without MarathonLB

  Background: Setup DCOS-CLI
    #Start SSH with DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'

  Scenario: Generate workflow Sparta

    Given I create file 'SpartaSecurityInstalation.json' based on 'schemas/dcosFiles/spartaSecurelywithoutMarathon.json' as 'json' with:
      |   $.container.docker.image                           |  UPDATE     | ${SPARTA_DOCKER_IMAGE}           | n/a    |
      |   $.container.docker.forcePullImage                  |  REPLACE    | ${FORCEPULLIMAGE}                |boolean |
      |   $.env.SPARTA_ZOOKEEPER_CONNECTION_STRING           |  UPDATE     | ${ZK_URL}                        |n/a |
      |   $.env.VAULT_HOST                                   |  UPDATE     | ${VAULT_HOST}                    |n/a |
      |   $.env.VAULT_TOKEN                                  |  UPDATE     | ${VAULT_TOKEN}                    |n/a |

    #Copy DEPLOY JSON to DCOS-CLI
    When I outbound copy 'target/test-classes/SpartaSecurityInstalation.json' through a ssh connection to '/dcos'
    #Start image from JSON
    And I run 'dcos marathon app add /dcos/SpartaSecurityInstalation.json' in the ssh connection
    #Check Sparta is Running
    Then in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep sparta-auto | grep R | wc -l' contains '1'
    #Find task-id if from DCOS-CLI
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/sparta-auto | grep sparta-auto | awk '{print $2}'' contains 'True'
    And I run 'dcos marathon task list /sparta/sparta-auto | awk '{print $5}' | grep sparta-auto' in the ssh connection and save the value in environment variable 'spartaTaskId'
    #keep IP
    And I run 'dcos marathon task list /sparta/sparta-auto | awk '{print $4}' | grep sparta-auto' in the ssh connection and save the value in environment variable 'spartaIP'

    When I send a 'DELETE' request to '/policy/!{previousWorkflowID}'
    Then the service response status must be '200'

  Scenario: Remove workflow Sparta
    When  I run 'dcos marathon app remove /sparta/sparta-auto' in the ssh connection
    Then in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep sparta-auto | grep R | wc -l' contains '0'


# Example of execution with mvn :
#  mvn verify -DVAULT_TOKEN='22423ddf-d12e-4a3a-fd49-871f06a0c35e' -DVAULT_HOST='https://vault.service.paas.labs.stratio.com:8200' -DZK_URL='zk-0001-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0002-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0003-zookeeperstable.service.paas.labs.stratio.com:2181' -DDCOS_CLI_HOST=172.17.0.3 -DSPARTA_DOCKER_IMAGE=qa.stratio.com/stratio/sparta:1.6.0 -DFORCEPULLIMAGE=false -Dit.test=com.stratio.sparta.testsAT.automated.dcos.ISAppInstalationwithoutMarathonLB -DlogLevel=DEBUG -Dmaven.failsafe.debu
