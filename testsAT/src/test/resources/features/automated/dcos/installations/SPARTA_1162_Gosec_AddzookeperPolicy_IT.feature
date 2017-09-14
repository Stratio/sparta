@rest
Feature: [SPARTA-1162] Add sparta policy in gosec

  Background: Setup token to gosec
    #Generate token to conect to gosec
    Given I set sso token using host '${GOSECMANAGEMENT_HOST}' with user '${USERNAME}' and password '${PASSWORD}'
    And I securely send requests to '${GOSECMANAGEMENT_HOST}:${GOSECMANAGEMENT_PORT}'

  Scenario: [SPARTA-1162][01]Add zookeper-sparta policy to write in zookeper
    Given I send a 'POST' request to '${BASE_END_POINT}/api/policy' based on 'schemas/gosec/zookeeper_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_POLICY_ZK}    | n/a |
      |   $.name                  |  UPDATE    | ${NAME_POLICY_ZK}  | n/a |
      |   $.users[0]              |  UPDATE    | ${USER}            | n/a |
    Then the service response status must be '201'
  @manual @ignore
  Scenario: [SPARTA-1162][02]Add sparta policy for authorization in sparta
    Given I send a 'POST' request to '${BASE_END_POINT}/api/policy' based on 'schemas/gosec/sp_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_POLICY_SP}          | n/a |
      |   $.name                  |  UPDATE    | ${NAME_POLICY_SP}        | n/a |
      |   $.users[0]              |  UPDATE    | ${USER}                  | n/a |
    Then the service response status must be '201'
  @manual @ignore
  Scenario: [SPARTA-1162][03]Add sparta policy to write in kafka
    Given I send a 'POST' request to '${BASE_END_POINT}/api/policy' based on 'schemas/gosec/kafka_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_POLICY_KAFKA         | n/a |
      |   $.name                  |  UPDATE    | ${NAME_POLICY_KAFKA}      | n/a |
      |   $.users[0]              |  UPDATE    | ${USER}                   | n/a |
    Then the service response status must be '201'

# Example of execution with mvn :
#mvn verify -DBASE_END_POINT=/service/gosecmanagement -DGOSECMANAGEMENT_HOST='megadev.labs.stratio.com' -DGOSECMANAGEMENT_PORT=443 -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.ISGosec_AddzookeperPolitic -DlogLevel=DEBUG -DUSER_NAME=admin -DPASS_WORD=1234 -DSECURED=true -Dmaven.failsafe.debu -DID_POLICY_ZK='sparta_zk' -DNAME_POLICY_ZK='zk_sparta' -DID_POLICY_SP='sparta' -DNAME_POLICY_SP='sparta' -DID_POLICY_KAFKA='kafka-sec' -DNAME_POLICY_KAFKA='kafka-sec'-DUSER='sparta-dg'
