
@rest
Feature: [SPARTA][DCOS]Generate workflow in DCOS Kafka to Elastic

  Background: Setup DCOS-CLI
    #Start SSH with DCOS-CLI

  Scenario: Add Sparta app and Execute Workflow
    #************************
    # ADD SPARTA APLICATION**
    #************************
    Given I open a ssh connection to '${DCOS_IP}' with user 'root' and password 'stratio'
    #get token
    Then I run ' cat /etc/sds/gosec-sso/cas/cas-vault.properties |grep token | cut -f 2 -d :' in the ssh connection and save the value in environment variable 'vaultToken'
    #get vault ip url
    And I run 'cat /etc/sds/gosec-sso/cas/cas-vault.properties |grep address | cut -f 3 -d / | cut -f 1 -d :' in the ssh connection and save the value in environment variable 'vaultIP'
    #get vault port url
    And I run 'cat /etc/sds/gosec-sso/cas/cas-vault.properties |grep address | cut -f 3 -d / | cut -f 2 -d :' in the ssh connection and save the value in environment variable 'vaultport'

    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    Given I create file 'SpartaSecurityInstalation.json' based on 'schemas/dcosFiles/spartaSecurelywithoutMarathon.json' as 'json' with:
      |   $.container.docker.image                           |  UPDATE     | ${SPARTA_DOCKER_IMAGE}           | n/a    |
      |   $.container.docker.forcePullImage                  |  REPLACE    | ${FORCEPULLIMAGE}                |boolean |
      |   $.env.SPARTA_ZOOKEEPER_CONNECTION_STRING           |  UPDATE     | ${ZK_URL}                        |n/a |
      |   $.env.VAULT_TOKEN                                  |  UPDATE     | !{vaultToken}                   |n/a |
      |   $.env.MARATHON_TIKI_TAKKA_MARATHON_URI             |  UPDATE     | ${MARATHON_TIKI_TAKKA}           |n/a |
      |   $.env.SPARTA_DOCKER_IMAGE                          |  UPDATE     | ${SPARTA_DOCKER_IMAGE}           |n/a |
      |   $.env.VAULT_HOSTS                                  |  UPDATE     | !{vaultIP}                     | n/a |
      |   $.env.VAULT_PORT                                   |  UPDATE     | !{vaultport}                     |n/a |


    #Copy DEPLOY JSON to DCOS-CLI
    When I outbound copy 'target/test-classes/SpartaSecurityInstalation.json' through a ssh connection to '/dcos'
    #Start image from JSON
    #And I run 'dcos marathon app add /dcos/SpartaSecurityInstalation.json' in the ssh connection
    #Check Sparta is Running
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep sparta-workflow-server.sparta | grep R | wc -l' contains '1'
    #Find task-id if from DCOS-CLI
    And in less than '400' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/sparta/sparta-workflow-server | grep sparta-workflow-server | awk '{print $2}'' contains 'True'
    And I run 'dcos marathon task list /sparta/sparta/sparta-workflow-server | awk '{print $5}' | grep sparta-workflow-server' in the ssh connection and save the value in environment variable 'spartaTaskId'
    #Find Aplication ip
    #And I run 'dcos marathon task list /sparta/sparta/sparta-workflow-server | grep -v APP | awk '{print $4}' in the ssh connection and save the value in environment variable 'spartaIP'
    And I run 'dcos marathon task list /sparta/sparta/sparta-workflow-server | awk '{print $4}'| awk 'NR ==2'' in the ssh connection and save the value in environment variable 'spartaIP'
    Then  I run 'echo !{spartaIP}' in the ssh connection

    #********************************
    # GENERATE AND EXECUTE WORKFLOW**
    #********************************
    Given I securely send requests to '!{spartaIP}:10148'
   #include workflow
    Given I send a 'POST' request to '/policy' based on 'schemas/workflows/kafka-elastic-tickets-carrefour.json' as   'json' with:
      | id | DELETE | N/A  |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '2' seconds
    #Execute workflow
    When I send a 'GET' request to '/policy/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text '{"message":"Launched policy with name !{nameWorkflow}'
    #verify the generation of  workflow in dcos
    And in less than '400' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/sparta/workflows/!{nameWorkflow} | wc -l' contains '2'


    #*******************************
    #    *DELETE APP AND WORKFLOW***
    #*******************************
    #delete workflow
    When I send a 'DELETE' request to '/policy/!{previousWorkflowID}'
    Then the service response status must be '200'
    #Remove Sparta
    When  I run 'dcos marathon app remove /sparta/sparta' in the ssh connection
    Then in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep sparta | grep R | wc -l' contains '0'


# Example of execution with mvn :
#   mvn verify -DSPARTANAME='sparta' -DDCOS_IP='10.200.0.21' -DMARATHON_TIKI_TAKKA='http://10.200.0.21:8080' -DZK_URL='zk-0001-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0002-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0003-zookeeperstable.service.paas.labs.stratio.com:2181' -DDCOS_CLI_HOST=172.17.0.2 -DSPARTA_DOCKER_IMAGE=stratio/sparta:1.6.1 -DFORCEPULLIMAGE=false -Dit.test=com.stratio.sparta.testsAT.automated.dcos.ISAppValidateWorkflowinDcos_KafkatoPostgres -DlogLevel=DEBUG -Dmaven.failsafe.debu
#  mvn verify -DPORTELASTIC='31504'  -DTOTALDATA='20' -DSPARTANAME='sparta' -DDCOS_IP='10.200.0.21' -DMARATHON_TIKI_TAKKA='http://10.200.0.21:8080' -DZK_URL='zk-0001-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0002-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0003-zookeeperstable.service.paas.labs.stratio.com:2181' -DDCOS_CLI_HOST=172.17.0.2 -DSPARTA_DOCKER_IMAGE=qa.stratio.com/stratio/sparta:1.6.1 -DFORCEPULLIMAGE=false -Dit.test=com.stratio.sparta.testsAT.automated.dcos.ISAppValidateWorkflowinDcos_KafkatoElastic -DlogLevel=DEBUG -Dmaven.failsafe.debu


