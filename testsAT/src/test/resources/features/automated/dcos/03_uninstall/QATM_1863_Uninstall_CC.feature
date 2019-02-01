@rest
Feature: [Uninstall Sparta Command Center] Sparta uninstall testing with command center

  Scenario: [QATM-1863] Uninstall Sparta
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    And I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    When I send a 'DELETE' request to '/service/deploy-api/deploy/uninstall?app=/sparta/${DCOS_SERVICE_NAME:-sparta-server}/${DCOS_SERVICE_NAME:-sparta-server}'
    # Check Uninstall in DCOS
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task | grep ${DCOS_SERVICE_NAME:-sparta-server} | wc -l' contains '0'
    # Check Uninstall in CCT-API
    And in less than '200' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/all' so that the response does not contains '${DCOS_SERVICE_NAME:-sparta-server}'


  @RunOnEnv(POLICY_POSTGRES_AGENT)
  Scenario: [QATM-1863] Delete user of Postgres Policy
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${DCOS_USER:-admin}' and password '${DCOS_PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I send a 'PUT' request to '/service/gosecmanagement/api/policy/${ID_SPARTA_POSTGRES:-sparta-pg}' based on 'schemas/gosec/sparta_policy.json' as 'json' with:
      | $.id | UPDATE | testPGCAgent1 | string |
      | $.users | REPLACE | [] | array |
      |   $.id                    |  UPDATE    | ${ID_SPARTA_POSTGRES:-sparta-pg}     | n/a |
      |   $.name                  |  UPDATE    | ${ID_SPARTA_POSTGRES:-sparta-pg}     | n/a |
      |   $.users              |  UPDATE       | []    | n/a |
    Then the service response status must be '200'
    And I wait '10' seconds


  @RunOnEnv(DROP_ROLE)
  Scenario:[QATM-1863] Obtain postgres docker
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When in less than '600' seconds, checking each '20' seconds, I send a 'GET' request to '/exhibitor/exhibitor/v1/explorer/node-data?key=%2Fdatastore%2Fcommunity%2F${POSTGRES_NAME:-postgrestls}%2Fplan-v2-json&_=' so that the response contains 'str'
    Then I send a 'GET' request to '/exhibitor/exhibitor/v1/explorer/node-data?key=%2Fdatastore%2Fcommunity%2F${POSTGRES_NAME:-postgrestls}%2Fplan-v2-json&_='
    And I save element '$.str' in environment variable 'exhibitor_answer'
    And I save ''!{exhibitor_answer}'' in variable 'parsed_answer'
    And I wait '2' seconds
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    And I run 'echo !{parsed_answer} | jq '.phases[0]."0001".steps[0]."0"'.agent_hostname | sed 's/^.\|.$//g'' in the ssh connection with exit status '0' and save the value in environment variable 'pgIP'
    And I run 'echo !{pgIP}' in the ssh connection
    Then I wait '10' seconds
    When in less than '600' seconds, checking each '20' seconds, I send a 'GET' request to '/service/${POSTGRES_NAME:-postgrestls}/v1/service/status' so that the response contains 'status'
    Then the service response status must be '200'
    And I save element in position '0' in '$.status[?(@.role == "master")].assignedHost' in environment variable 'pgIPCalico'
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    When I run 'docker ps -q | xargs -n 1 docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}} {{ .Name }}' | sed 's/ \// /'| grep !{pgIPCalico} | awk '{print $2}'' in the ssh connection and save the value in environment variable 'postgresDocker'
    And I run 'echo !{postgresDocker}' locally
    And I wait '10' seconds
    And I run 'echo !{postgresDocker}' in the ssh connection with exit status '0'

  @RunOnEnv(DROP_ROLE)
  Scenario:[QATM-1863] Drop Sparta Role and Database
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    And I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "DROP SCHEMA \"${DCOS_SERVICE_NAME}\" CASCADE"' in the ssh connection with exit status '0'
    And I wait '1' seconds
    Then in less than '300' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "DROP DATABASE  ${POSTGRES_DATABASE:-sparta}"' contains 'DROP DATABASE'
    #wait for drop user from Postgres
    Then in less than '300' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "DROP ROLE \"${DCOS_SERVICE_NAME}\""' contains 'role "${DCOS_SERVICE_NAME}" does not exist'


  @RunOnEnv(POLICY_POSTGRES_AGENT)
  Scenario: [QATM-1863] Delete Postgres Policy
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I send a 'DELETE' request to '/service/gosecmanagement/api/policy/${ID_SPARTA_POSTGRES:-sparta-pg}'
    Then the service response status must be '200'

  # Add Sparta dependencies in Postgres

#  @skipOnEnv(SKIP_POLICY)
#  Scenario: [QATM-1863] Delete Sparta Policy
#    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
#    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
#    When I send a 'DELETE' request to '/service/gosecmanagement/api/policy/${DCOS_SERVICE_NAME}'
#    Then the service response status must be '200'

  @skipOnEnv(SKIP_POLICY)
  Scenario: [QATM-1863] Delete Zookeeper Policy
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I send a 'DELETE' request to '/service/gosecmanagement/api/policy/${ID_POLICY_ZK:-spartazk}'
    Then the service response status must be '200'

  @skipOnEnv(SKIP_GENERATE_DESCRIPTOR)
   # Delete Descriptor
    When I send a 'DELETE' request to '/service/deploy-api/universe/sparta/${FLAVOUR}-auto/descriptor'
    Then the service response status must be '200'

