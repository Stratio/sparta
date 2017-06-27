@web @rest
Feature: [SPARTA][DCOS]Running sparta with security and Login

  Background: Setup DCOS-CLI
    #Start SSH with DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'

  Scenario: [SPARTA][Scenario-1][01]Running sparta without security
    #**************************
    #     ADD SPARTA APP      *
    #**************************

    #Import Configuration
    Given I create file 'SpartaSecurityInstalation.json' based on 'schemas/dcosFiles/spartaSecurityInstalation.json' as 'json' with:
      |   $.container.docker.image                           |  UPDATE     | ${SPARTA_DOCKER_IMAGE}           | n/a    |
      |   $.container.docker.forcePullImage                  |  REPLACE    | ${FORCEPULLIMAGE}                |boolean |
      |   $.env.SPARTA_ZOOKEEPER_CONNECTION_STRING           |  UPDATE     | ${ZK_URL}                        |n/a |
      |   $.env.VAULT_HOST                                   |  UPDATE     | ${VAULT_HOST}                    |n/a |

    #Copy DEPLOY JSON to DCOS-CLI
    When I outbound copy 'target/test-classes/SpartaSecurityInstalation.json' through a ssh connection to '/dcos'
    #Start image from JSON
    And I run 'dcos marathon app add /dcos/SpartaSecurityInstalation.json' in the ssh connection
    #Check Sparta is Running
    Then in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep sparta-auto | grep R | wc -l' contains '1'
    #Find task-id if from DCOS-CLI
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/sparta-auto | grep sparta-auto | awk '{print $2}'' contains 'True'
    And I run 'dcos marathon task list /sparta/sparta-auto | awk '{print $5}' | grep sparta-auto' in the ssh connection and save the value in environment variable 'spartaTaskId'
    And I run 'dcos marathon task list /sparta/sparta-auto | awk '{print $4}' | grep sparta-auto' in the ssh connection and save the value in environment variable 'spartaIP'
    #DCOS dcos marathon task show check healtcheck status
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{spartaTaskId} | grep TASK_RUNNING | wc -l' contains '1'
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{spartaTaskId} | grep healthCheckResults | wc -l' contains '1'
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{spartaTaskId} | grep  '"alive": true' | wc -l' contains '1'

    #**************************
    #     SPARTA LOGIN        *
    #**************************
    Given My app is running in '${MARATHONLB_URL}:443'
    And I securely browse to '/sparta'
    And I wait '2' seconds
    Then '1' element exists with 'id:username'
    And I type 'sparta' on the element on index '0'
    And '1' element exists with 'id:password'
    And I type 'stratio' on the element on index '0'
    #Verify the login button in sparta
    And '1' element exists with 'css:input[data-qa="login-button-submit"]'
    When I click on the element on index '0'
    Then I wait '2' second

    #**************************
    #     REMOVE SPARTA       *
    #**************************
    When  I run 'dcos marathon app remove /sparta/sparta-auto' in the ssh connection
    Then in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep sparta-auto | grep R | wc -l' contains '0'

# Example of execution with mvn :
#  mvn verify -DDCOS_CLI_HOST=172.17.0.3 -DFORCE_BROWSER=chrome_48iddiegotest  -DVAULT_HOST=https://vault.service.paas.labs.stratio.com:8200 -DZK_URL='zk-0001-zookeeperstable.service.paas.labs.stratio.com:1025,zk-0002-zookeeperstable.service.paas.labs.stratio.com:1025,zk-0003-zookeeperstable.service.paas.labs.stratio.com:1025' -DMARATHONLB_URL=agent-13.node.default-cluster.paas.labs.stratio.com -DSPARTA_DOCKER_IMAGE=qa.stratio.com/stratio/sparta:latest -DFORCEPULLIMAGE=false -Dit.test=com.stratio.sparta.testsAT.automated.dcos.ISAppwithSecurityLogin -DSELENIUM_GRID=localhost:4444
#mvn verify -DDCOS_IP=10.200.0.21 -DDCOS_CLI_HOST=dcos-cli.demo.stratio.com -DDCOS_USER=root -DDCOS_PASSWORD=stratio  -Dit.test=com.stratio.sparta.testsAT.automated.dcos.AppWithoutSecurity -DPEM_FILE=none -DREMOTE_USER=root -DREMOTE_PASSWORD=stratio -DVAULT_HOST=10.200.0.29 -DVAULT_PORT=8200  -DFORCE_BROWSER=chrome_48iddiegotest -DSELENIUM_GRID=localhost:4444 -DSPARTA_HOST=localhost -DSPARTA_PORT=9090 -DSPARTA_DOCKER_IMAGE=qa.stratio.com/stratio/sparta:latest -DSPARTA_DOCKER_IMAGE=qa.stratio.com/stratio/sparta:latest -DFORCEPULLIMAGE=false
