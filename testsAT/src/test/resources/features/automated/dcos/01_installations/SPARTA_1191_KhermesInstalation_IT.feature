@rest
Feature: [SPARTA-1191] Install Khermes

  Scenario: [SPARTA-1191][01] Install Khermes Seed
     #Connect to dcos to get token and vault
    Given I open a ssh connection to '${DCOS_IP}' with user 'root' and password 'stratio'
    #get token
    Then I run ' cat /etc/sds/gosec-sso/cas/cas-vault.properties |grep token | cut -f 2 -d :' in the ssh connection and save the value in environment variable 'vaultToken'
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    When I send a 'POST' request to '/marathon/v2/apps' based on 'schemas/dcosFiles/khermesInstalation.json' as 'json' with:
      | $.id | UPDATE | khermes |
      | $.env.SEED | UPDATE | true |
      | $.env.PORT0 | UPDATE | 2551 |
      | $.env.VAULT_HOSTS | UPDATE | ${VAULT_HOST} |
      | $.env.SEED_IP | UPDATE | ${DKHERMESIP} |
      | $.constraints| UPDATE |  [["hostname","CLUSTER",${DKHERMESIP}]]
      | $.env.VAULT_PORT  | UPDATE | ${VAULT_PORT}  |
      | $.env.VAULT_TOKEN | UPDATE | !{vaultToken} |
      | $.env.KAFKA_BROKER_INSTANCE_NAME | UPDATE | ${KAFKA_BROKER_INSTANCE_NAME} |
      | $.env.KAFKA_BROKER_PRINCIPAL     | UPDATE | ${KAFKA_BROKER_PRINCIPAL}     |
    Then the service response status must be '201'
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep -w khermes' contains 'khermes'
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user '${CLI_USER}' and password '${CLI_PASSWORD}'
    When I run 'dcos marathon task list khermes | awk '{print $5}' | grep khermes' in the ssh connection and save the value in environment variable 'marathonTaskId'
    # DCOS dcos marathon task show check healtcheck status
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{marathonTaskId} | grep TASK_RUNNING' contains 'TASK_RUNNING'


    # mvn verify  -DKHERMESIP=10.200.1.238 -DCLUSTER_ID=newcore -DVAULT_HOST=vault.service.paas.labs.stratio.com -DVAULT_PORT=8200 -DVAULT_PORT= -DKHERMESIP=10.200.0.156 -DDCOS_CLI_HOST=dcos-newcore.demo.stratio.com -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.KhermesInstalation_IT -DKAFKA_BROKER_INSTANCE_NAME=kafka-sec -DKAFKA_BROKER_PRINCIPAL=producer.kafka-sec.mesos