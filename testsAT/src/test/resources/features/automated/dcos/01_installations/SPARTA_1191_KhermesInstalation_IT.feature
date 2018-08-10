@rest
Feature: [SPARTA-1191] Install Khermes

  Scenario: [SPARTA-1191][01] Take Khermes IP
    Given I run 'echo "${PRIVATE_AGENTS_LIST}" | cut -d',' -f1' locally with exit status '0' and save the value in environment variable 'PRIVATE_AGENT'

  Scenario: [SPARTA-1191][02] Install Khermes Seed
     #Connect to dcos to get token and vault
    Given I run 'echo "${MASTERS_LIST}" | cut -d',' -f1' locally with exit status '0' and save the value in environment variable 'MASTER_IP'
    Then I open a ssh connection to '!{MASTER_IP}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    #get token
    Then I run ' cat /opt/stratio/gosec-sso/conf/cas/cas-vault.properties |grep token | cut -f 2 -d :' in the ssh connection and save the value in environment variable 'vaultToken'
    #Install Khermes in DCOS
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    Given I create file 'khermesInstalation.json' based on 'schemas/dcosFiles/khermesInstalation.json' as 'json' with:
      | $.id | UPDATE |  ${KHERMES_INSTANCE:-khermes} |
      | $.env.SEED | UPDATE | true |
      | $.env.PORT0 | UPDATE | 2551 |
      | $.env.VAULT_HOSTS | UPDATE | ${VAULT_HOST:-vault.service.paas.labs.stratio.com} |
      | $.env.SEED_IP | UPDATE | !{PRIVATE_AGENT} |
      | $.env.VAULT_PORT  | UPDATE | ${VAULT_PORT:-8200}  |
      | $.env.VAULT_TOKEN | UPDATE | !{vaultToken} |
      | $.env.KAFKA_BROKER_INSTANCE_NAME | UPDATE | ${KAFKA_BROKER_INSTANCE_NAME:-eos-kafka-framework} |
      | $.env.KAFKA_BROKER_PRINCIPAL     | UPDATE | ${KAFKA_BROKER_PRINCIPAL:-producer.eos-kafka-framework.mesos}|
      | $.constraints[0][2] | UPDATE |  !{PRIVATE_AGENT} |
    Then I run 'rm -f /dcos/khermesInstalation.json' in the ssh connection
    And I outbound copy 'target/test-classes/khermesInstalation.json' through a ssh connection to '/dcos'
    And I run 'dcos marathon app add /dcos/khermesInstalation.json' in the ssh connection
#    And in less than '600' seconds, checking each '20' seconds, the command output 'dcos task | grep -w ${KHERMES_INSTANCE:-khermes}' contains '${KHERMES_INSTANCE:-khermes}'
#    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user '${CLI_USER:-root}' and password '${CLI_PASSWORD:-stratio}'
#    When I run 'dcos marathon task list ${KHERMES_INSTANCE:-khermes} | awk '{print $5}' | grep ${KHERMES_INSTANCE:-khermes}' in the ssh connection and save the value in environment variable 'marathonTaskId'
#    # DCOS dcos marathon task show check healtcheck status
#    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{marathonTaskId} | grep TASK_RUNNING' contains 'TASK_RUNNING'