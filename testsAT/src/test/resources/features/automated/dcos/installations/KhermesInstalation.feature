@rest
Feature: Install Khermes

  Scenario: Install Khermes Seed
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    When I send a 'POST' request to '/marathon/v2/apps' based on 'schemas/dcosFiles/khermesInstalation.json' as 'json' with:
      | $.id | UPDATE | khermes |
      | $.env.SEED | UPDATE | true |
      | $.env.PORT0 | UPDATE | 2551 |
      | $.env.VAULT_HOSTS | UPDATE | ${VAULT_HOST} |
      | $.env.VAULT_PORT  | UPDATE | ${VAULT_PORT}  |
      | $.env.VAULT_TOKEN | UPDATE | !{vaultToken} |
      | $.env.KAFKA_BROKER_INSTANCE_NAME | UPDATE | ${KAFKA_BROKER_INSTANCE_NAME} |
      | $.env.KAFKA_BROKER_PRINCIPAL     | UPDATE | ${KAFKA_BROKER_PRINCIPAL}     |
    Then the service response status must be '201'
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep -w khermes | wc -l' contains '1'
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user '${CLI_USER}' and password '${CLI_PASSWORD}'
    When I run 'dcos marathon task list khermes | awk '{print $5}' | grep khermes' in the ssh connection and save the value in environment variable 'marathonTaskId'
    # DCOS dcos marathon task show check healtcheck status
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{marathonTaskId} | grep TASK_RUNNING | wc -l' contains '1'
