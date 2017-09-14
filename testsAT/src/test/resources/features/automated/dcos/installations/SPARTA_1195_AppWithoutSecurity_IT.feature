@rest
Feature: [SPARTA_1195]Running sparta without security

  Background: Setup DCOS-CLI
    #Start SSH with DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'

  Scenario: [SPARTA_1195][01]Running sparta without security
    Given I create file 'SpartaBasic.json' based on 'schemas/dcosFiles/spartaBasicInstalation.json' as 'json' with:
      |   $.container.docker.image                           |  UPDATE     | ${SPARTA_DOCKER_IMAGE}      | n/a    |
      |   $.container.docker.forcePullImage                  |  REPLACE    | ${FORCEPULLIMAGE}           |boolean |
      |   $.env.SPARTA_ZOOKEEPER_CONNECTION_STRING           |  UPDATE     | ${ZK_URL}                   |n/a |
    #Copy DEPLOY JSON to DCOS-CLI
    When I outbound copy 'target/test-classes/SpartaBasic.json' through a ssh connection to '/dcos'
    #Start image from JSON
    And I run 'dcos marathon app add /dcos/SpartaBasic.json' in the ssh connection
    #Check Sparta is Running
    Then in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep spartabasic | grep R | wc -l' contains '1'
    #Find task-id if from DCOS-CLI
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/spartabasic | grep spartabasic | awk '{print $2}'' contains 'True'
    And I run 'dcos marathon task list /sparta/spartabasic | awk '{print $5}' | grep spartabasic' in the ssh connection and save the value in environment variable 'spartaTaskId'
    #keep IP
    And I run 'dcos marathon task list /sparta/spartabasic | awk '{print $4}' | grep spartabasic' in the ssh connection and save the value in environment variable 'spartaIP'
    # dcos marathon app kill
    When  I run 'dcos marathon app remove /sparta/spartabasic' in the ssh connection
    Then in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep spartabasic | grep R | wc -l' contains '0'


# Example of execution with mvn :
#  mvn verify -DZK_URL='localhost:2181' -DDCOS_CLI_HOST=172.17.0.3 -DSPARTA_DOCKER_IMAGE=qa.stratio.com/stratio/sparta:latest -DFORCEPULLIMAGE=false -Dit.test=com.stratio.sparta.testsAT.automated.dcos.ISAppWithoutSecurity -DlogLevel=DEBUG -Dmaven.failsafe.debu
