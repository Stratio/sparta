@rest
Feature: [SPARTA-1162] Add sparta policy in gosec

  Background: Setup token to gosec
    #Generate token to conect to gosec
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
  @runOnEnv(ID_POLICY_ZK)
  Scenario: [SPARTA-1162][01]Add zookeper-sparta policy to write in zookeper
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/zookeeper_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_POLICY_ZK}       | n/a |
      |   $.name                  |  UPDATE    | ${ID_POLICY_ZK}       | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}  | n/a |
    Then the service response status must be '201'
  @runOnEnv(ID_SPARTA_POLICY_OLD)
  Scenario: [SPARTA-1162][02]Add sparta policy for authorization in sparta
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/sp_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_SPARTA_POLICY_OLD}     | n/a |
      |   $.name                  |  UPDATE    | ${ID_SPARTA_POLICY_OLD}     | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}     | n/a |
    Then the service response status must be '201'
  @runOnEnv(ID_SPARTA_POLICY)
  Scenario: [SPARTA-1162][02]Add sparta policy for authorization in sparta
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/sparta_policy.json' as 'json' with:
      |   $.id                       |  UPDATE    | ${ID_POLICY_SP:-spartaserver}   | n/a |
      |   $.name                     |  UPDATE    | ${ID_POLICY_SP:-spartaserver}    | n/a |
      |   $.users[0]                 |  UPDATE    | ${DCOS_SERVICE_NAME}            | n/a |

    Then the service response status must be '201'
  @runOnEnv(ID_KAFKA_POLICY)
  Scenario: [SPARTA-1162][03]Add sparta policy to write in kafka
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/kafka_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_KAFKA_POLICY}        | n/a |
      |   $.name                  |  UPDATE    | ${ID_KAFKA_POLICY}        | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}      | n/a |
      |   $.services[0].version   |  UPDATE    | ${KF-GOSEC-VERSION:-2.2.0}      | n/a |
    Then the service response status must be '201'
  @runOnEnv(ID_KAFKA_FR_POLICY)
  Scenario: [SPARTA-1162][04]Add sparta policy to write in kafka
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/kafka_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_KAFKA_FR_POLICY}        | n/a |
      |   $.name                  |  UPDATE    | ${ID_KAFKA_FR_POLICY}        | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}         | n/a |
      |   $.services[0].version   |  UPDATE    | ${KF-GOSEC-VERSION:-2.2.0}   | n/a |
    Then the service response status must be '201'
  @runOnEnv(ID_ELASTIC_POLICY)
  Scenario: [SPARTA-1162][05]Add Elastic policy to write in Elastic
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/elastic_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_ELASTIC_POLICY}        | n/a |
      |   $.name                  |  UPDATE    | ${ID_ELASTIC_POLICY}        | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}        | n/a |
      |   $.services[0].version      |  UPDATE    | ${EL-GOSEC-VERSION:-1.0.3}      | n/a |
    Then the service response status must be '201'
  @runOnEnv(ID_XD_POLICY)
  Scenario: [SPARTA-1162][06]Add XD policy to write in Elastic
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/xd_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_XD_POLICY}        | n/a |
      |   $.name                  |  UPDATE    | ${ID_XD_POLICY}        | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}   | n/a |
      |   $.services[0].version   |  UPDATE    | ${XD-GOSEC-VERSION:-2.4.0}      | n/a |
    Then the service response status must be '201'

  @runOnEnv(ID_SPARTA_POSTGRES)
  Scenario: [SPARTA-1162][08]Add postgres policy for authorization in sparta
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/postgres_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_SPARTA_POSTGRES}     | n/a |
      |   $.name                  |  UPDATE    | ${ID_SPARTA_POSTGRES}     | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}     | n/a |
      |   $.services[0].version   |  UPDATE    | ${POSTGRES-GOSEC-VERSION:-1.0.3}      | n/a |
    Then the service response status must be '201'

  @runOnEnv(ID_SPARTA_GOVERNANCE)
  Scenario: [SPARTA-1162][08]Add postgres policy for authorization in sparta
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/governance_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_SPARTA_GOVERNANCE}     | n/a |
      |   $.name                  |  UPDATE    | ${ID_SPARTA_GOVERNANCE}     | n/a |
      |   $.users[0]              |  UPDATE    | ${USER_GOVERNANCE:-dg-bootstrap}     | n/a |
      |   $.services[0].version   |  UPDATE    | ${POSTGRES-GOSEC-VERSION:-1.0.3}      | n/a |
    Then the service response status must be '201'


  @runOnEnv(ID_SPARTA_POLICY_CCT)
  Scenario: [SPARTA-1162][08]Add postgres policy for authorization in sparta
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/sparta_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_SPARTA_POLICY_CCT}     | n/a |
      |   $.name                  |  UPDATE    | ${ID_SPARTA_POLICY_CCT}     | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}     | n/a |
    Then the service response status must be '201'

  @runOnEnv(RESTART_SPARTA)
  Scenario: [SPARTA-1162] [08] Restart Sparta Application after gosec
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    When I run 'echo $(dcos marathon app restart  /sparta/${DCOS_SERVICE_NAME}/${DCOS_SERVICE_NAME})' in the ssh connection
    And in less than '600' seconds, checking each '20' seconds, the command output 'dcos task | grep -w ${DCOS_SERVICE_NAME}' contains '${DCOS_SERVICE_NAME}'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/${DCOS_SERVICE_NAME}  | awk '{print $5}' | grep ${DCOS_SERVICE_NAME} ' in the ssh connection and save the value in environment variable 'spartaTaskId'
    #Check sparta is runing in DCOS
    And  I run 'echo !{spartaTaskId}' in the ssh connection
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{spartaTaskId} | grep TASK_RUNNING' contains 'TASK_RUNNING'

# Example of execution with mvn :
# mvn verify -DCLUSTER_ID=nightly -DID_POLICY_KAFKA=sparta-kafka -DID_POLICY_SP=sparta_1 -DID_POLICY_ZK=sparta-zk1 -DDCOS_SERVICE_NAME=sparta-server -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.SPARTA_1162_Gosec_AddzookeperPolicy_IT -DlogLevel=DEBUG
