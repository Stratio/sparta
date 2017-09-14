@rest
Feature: [SPARTA][DCOS]Instalation sparta with full security with calico

  Background: Setup DCOS-CLI
    #Start SSH with DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'

  Scenario: Generate workflow Sparta
    Given I create file 'SpartaSecurityInstalation.json' based on 'schemas/dcosFiles/${JSONFILE}' as 'json' with:
      |   $.container.docker.image                           |  UPDATE     | ${SPARTA_DOCKER_IMAGE}           | n/a    |
      |   $.container.docker.forcePullImage                  |  REPLACE    | ${FORCEPULLIMAGE}                |boolean |
      |   $.env.SPARTA_ZOOKEEPER_CONNECTION_STRING           |  UPDATE     | ${ZK_URL}                        |n/a |
      |   $.env.VAULT_TOKEN                                  |  UPDATE     | !{vaultToken}                    |n/a |
      |   $.env.MARATHON_TIKI_TAKKA_MARATHON_URI             |  UPDATE     | ${MARATHON_TIKI_TAKKA}           |n/a |
      |   $.env.SPARTA_DOCKER_IMAGE                          |  UPDATE     | ${SPARTA_DOCKER_IMAGE}           |n/a |
      |   $.env.VAULT_HOSTS                                  |  UPDATE     | !{vaultIP}                       | n/a|
      |   $.env.VAULT_PORT                                   |  UPDATE     | !{vaultport}                     |n/a |
      |   $ env.OAUTH2_URL_ACCESS_TOKEN                          |  UPDATE     | !{OAUTH2_URL_ACCESS_TOKEN}       |n/a |
      |   $ env.MARATHON_SSO_REDIRECT_URI                        |  UPDATE     | !{MARATHON_SSO_REDIRECT_URI}     |n/a |
      |   $ env.MARATHON_SSO_URI                                 |  UPDATE     | !{MARATHON_SSO_URI}              |n/a |
      |   $ env.OAUTH2_URL_LOGOUT                                |  UPDATE     | !{OAUTH2_URL_LOGOUT}             |n/a |
      |   $ env.OAUTH2_URL_PROFILE                               |  UPDATE     | !{OAUTH2_URL_PROFILE}            |n/a |
      |   $ env.OAUTH2_SSL_AUTHORIZE                             |  UPDATE     | !{OAUTH2_SSL_AUTHORIZE}          |n/a |

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
  @ignore @manual
  Scenario: Remove workflow Sparta
    When  I run 'dcos marathon app remove /sparta/sparta-auto' in the ssh connection
    Then in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep sparta-auto | grep R | wc -l' contains '0'


# Example of execution with mvn :
  #  mvn verify -DHAPROXY_0_VHOST='agent-14.node.paas.labs.stratio.com' -DOAUTH2_URL_ACCESS_TOKEN='https://newcore.labs.stratio.com:9005/sso/oauth2.0/accessToken' -DMARATHON_SSO_REDIRECT_URI='https://newcore.labs.stratio.com/acs/api/v1/auth/login' -DMARATHON_SSO_URI=https://newcore.labs.stratio.com:9005/sso' -DOAUTH2_URL_LOGOUT='https://newcore.labs.stratio.com:9005/sso/logout' -DOAUTH2_URL_PROFILE=https://newcore.labs.stratio.com:9005/sso/oauth2.0/profile' -DOAUTH2_SSL_AUTHORIZE='https://newcore.labs.stratio.com:9005/sso/oauth2.0/authorize' -DJSONFILE='fullSecurityInstalation.json' -DVAULT_HOST='https://vault.service.paas.labs.stratio.com:8200' -DZK_URL='zk-0001-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0002-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0003-zookeeperstable.service.paas.labs.stratio.com:2181' -DDCOS_CLI_HOST=172.17.0.2 -DSPARTA_DOCKER_IMAGE=qa.stratio.com/stratio/sparta:1.7.1 -DFORCEPULLIMAGE=false -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.AppInstalationwithfullSecurity -DlogLevel=DEBUG -Dmaven.failsafe.debu
