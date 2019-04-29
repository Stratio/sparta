@rest
Feature: [QATM-1863] Sparta uninstall testing with command center

  Scenario: [QATM-1863][9000] Uninstall Sparta
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I send a 'DELETE' request to '/service/deploy-api/deploy/uninstall?force=true&app=/sparta/${DCOS_SERVICE_NAME:-sparta-server}/${DCOS_SERVICE_NAME:-sparta-server}'
    # Check Uninstall in DCOS
    And I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task | grep ${DCOS_SERVICE_NAME:-sparta-server} | wc -l' contains '0'
    # Check Uninstall in CCT-API
    And in less than '200' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/all' so that the response does not contains '${DCOS_SERVICE_NAME:-sparta-server}'

  Scenario:[QATM-1863][9002] Obtain postgres docker
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
    Then I wait '8' seconds
    When in less than '600' seconds, checking each '20' seconds, I send a 'GET' request to '/service/${POSTGRES_NAME:-postgrestls}/v1/service/status' so that the response contains 'status'
    Then the service response status must be '200'
    And I save element in position '0' in '$.status[?(@.role == "master")].assignedHost' in environment variable 'pgIPCalico'
    Given I open a ssh connection to '!{pgIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    When I run 'sudo docker ps -q |sudo xargs -n 1 docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}} {{ .Name }}' | sed 's/ \// /'| grep '!{pgIPCalico} ' | awk '{print $2}'' in the ssh connection and save the value in environment variable 'postgresDocker'
    And I run 'echo !{postgresDocker}' locally
    And I wait '8' seconds
    And I run 'echo !{postgresDocker}' in the ssh connection with exit status '0'

  @skipOnEnv(SKIP_DROP_SCHEMA)
  Scenario:[QATM-1863][9003] Drop Schema
    Given I open a ssh connection to '!{pgIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    And I run 'sudo docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "\connect ${POSTGRES_DATABASE:-sparta};" -c "DROP SCHEMA \"${DCOS_SERVICE_NAME}\" CASCADE;"' in the ssh connection with exit status '0'

  @skipOnEnv(SKIP_ADD_DROP_DATABASE)
  Scenario:[QATM-1863][9003] Drop Database
    Given I open a ssh connection to '!{pgIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    Then in less than '300' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "DROP DATABASE  ${POSTGRES_DATABASE:-sparta}"' contains 'DROP DATABASE'

  @skipOnEnv(SKIP_POLICY_POSTGRES_AGENT)
  Scenario: [QATM-1863][9001] Delete user of Postgres Policy
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${DCOS_USER:-admin}' and password '${DCOS_PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I send a 'PUT' request to '/service/gosecmanagement/api/policy/${DCOS_SERVICE_NAME:-sparta-server}-pg' based on 'schemas/gosec/sparta_policy.json' as 'json' with:
      | $.id    | UPDATE  | testPGCAgent1                    | string |
      | $.users | REPLACE | []                               | array  |
      | $.id    | UPDATE  | ${DCOS_SERVICE_NAME:-sparta-server}-pg | n/a    |
      | $.name  | UPDATE  | ${DCOS_SERVICE_NAME:-sparta-server}-pg | n/a    |
      | $.users | UPDATE  | []                               | n/a    |
    Then the service response status must be '200'
    Given I open a ssh connection to '!{pgIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    Then in less than '300' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "\du" -P pager=off | grep ${DCOS_SERVICE_NAME} -c' contains '0' with exit status '1'


  @skipOnEnv(SKIP_POLICY_POSTGRES_AGENT)
  Scenario: [QATM-1863][9004] Delete Postgres Policy
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I send a 'DELETE' request to '/service/gosecmanagement/api/policy/${DCOS_SERVICE_NAME:-sparta-server}-pg'
    Then the service response status must be '200'

  @runOnEnv(DROP_ROLE)
  Scenario:[QATM-1863][9005] Drop Role
    Given I open a ssh connection to '!{pgIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    #wait for drop user from Postgres
    Then in less than '300' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "DROP ROLE \"${DCOS_SERVICE_NAME}\""' contains 'role "${DCOS_SERVICE_NAME}" does not exist'

  # Add Sparta dependencies in Postgres
  @skipOnEnv(SKIP_POLICY_ZK)
  Scenario: [QATM-1863][9006] Delete Sparta Policy
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I send a 'DELETE' request to '/service/gosecmanagement/api/policy/${DCOS_SERVICE_NAME}'
    Then the service response status must be '200'

  @skipOnEnv(SKIP_POLICY_SP)
  Scenario: [QATM-1863][9007] Delete Zookeeper Policy
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I send a 'DELETE' request to '/service/gosecmanagement/api/policy/${DCOS_SERVICE_NAME:-sparta-server}-zk'
    Then the service response status must be '200'

  @skipOnEnv(SKIP_GENERATE_DESCRIPTOR)
  Scenario: [QATM-1863][9008] Delete Command Center Descriptor
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    # Delete Descriptor
    When I send a 'DELETE' request to '/service/deploy-api/universe/sparta/${SPARTA_FLAVOUR}-auto/descriptor?force=true'
    Then the service response status must be '200'

