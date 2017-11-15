@rest
Feature: [SPARTA-1162] Add sparta policy in gosec

  Background: Setup token to gosec
    #Generate token to conect to gosec
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user 'admin' and password '1234'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'

  Scenario: [SPARTA-1162][01]Add zookeper-sparta policy to write in zookeper
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/zookeeper_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_POLICY_ZK}       | n/a |
      |   $.name                  |  UPDATE    | ${ID_POLICY_ZK}       | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}  | n/a |
    Then the service response status must be '201'
  @runOnEnv(ID_SPARTA_POLICY)
  Scenario: [SPARTA-1162][02]Add sparta policy for authorization in sparta
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/sp_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${DCOS_SERVICE_NAME}          | n/a |
      |   $.name                  |  UPDATE    | ${DCOS_SERVICE_NAME}         | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}     | n/a |
    Then the service response status must be '201'
  @runOnEnv(ID_KAFKA_POLICY)
  Scenario: [SPARTA-1162][03]Add sparta policy to write in kafka
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/kafka_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_KAFKA_POLICY}        | n/a |
      |   $.name                  |  UPDATE    | ${ID_KAFKA_POLICY}        | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}      | n/a |
    Then the service response status must be '201'
  @runOnEnv(ID_ELASTIC_POLICY)
  Scenario: [SPARTA-1162][03]Add Elastic policy to write in Elastic
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/elastic_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_ELASTIC_POLICY}        | n/a |
      |   $.name                  |  UPDATE    | ${ID_ELASTIC_POLICY}        | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}      | n/a |
    Then the service response status must be '201'

  @runOnEnv(RESTART_SPARTA)
  Scenario: [SPARTA-1162] [04] Restart Sparta Application after gosec
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    When I run 'echo $(dcos marathon app restart  /sparta/${DCOS_SERVICE_NAME}/${DCOS_SERVICE_NAME})' in the ssh connection
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep -w ${DCOS_SERVICE_NAME} | wc -l' contains '1'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/${DCOS_SERVICE_NAME}  | awk '{print $5}' | grep ${DCOS_SERVICE_NAME} ' in the ssh connection and save the value in environment variable 'spartaTaskId'
    #Check sparta is runing in DCOS
    And  I run 'echo !{spartaTaskId}' in the ssh connection
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{spartaTaskId} | grep TASK_RUNNING | wc -l' contains '1'

# Example of execution with mvn :
# mvn verify -DCLUSTER_ID=nightly -DID_POLICY_KAFKA=sparta-kafka -DID_POLICY_SP=sparta_1 -DID_POLICY_ZK=sparta-zk1 -DDCOS_SERVICE_NAME=sparta-server -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.SPARTA_1162_Gosec_AddzookeperPolicy_IT -DlogLevel=DEBUG