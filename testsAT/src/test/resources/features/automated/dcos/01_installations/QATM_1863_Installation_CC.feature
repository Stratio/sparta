@rest
Feature: [QATM_1863] Sparta installation with Command Center

  Scenario: [QATM-1863][01] Take Marathon-lb IP
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    And I run 'dcos marathon task list | grep marathon.*lb.* | awk '{print $4}'' in the ssh connection and save the value in environment variable 'marathonIP'
    Then I wait '1' seconds
    And I open a ssh connection to '!{marathonIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    And I run 'hostname | sed -e 's|\..*||'' in the ssh connection with exit status '0' and save the value in environment variable 'MarathonLbDns'

  @skipOnEnv(SPARTA_FLAVOUR=pegaso)
  @skipOnEnv(SKIP_GENERATE_DESCRIPTOR)
  Scenario: [QATM-1863][02] Generate New Descriptor for CommandCenter
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    # Obtain Descriptor
    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${BOOTSTRAP_USER:-operador}' and pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    Then I run 'curl -k -s -X GET -H 'Cookie:dcos-acs-auth-cookie=!{dcosAuthCookie}' https://${CLUSTER_ID:-nightly}.${CLUSTER_DOMAIN:-labs.stratio.com}:443/service/${DEPLOY_API:-deploy-api}/universe/sparta/${SPARTA_FLAVOUR}/descriptor | jq .> target/test-classes/schemas/sparta-descriptor.json' locally
    # Create Descriptor
    When I send a 'POST' request to '/service/${DEPLOY_API:-deploy-api}/universe/sparta/${SPARTA_FLAVOUR}-auto/descriptor' based on 'schemas/sparta-descriptor.json' as 'json' with:
      | $.data.model                                                            | REPLACE | ${SPARTA_FLAVOUR}-auto                                                        | string |
      | $.data.container.runners[0].image                                       | REPLACE | ${DOCKER_URL:-qa.stratio.com/stratio/sparta}:${STRATIO_SPARTA_VERSION:-2.5.0} | string |
    Then the service response status must be '201'

  @runOnEnv(SPARTA_FLAVOUR=pegaso)
  @skipOnEnv(SKIP_GENERATE_DESCRIPTOR)
  Scenario: [QATM-1863][02] Generate New Descriptor for CommandCenter
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    # Obtain Descriptor
    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${BOOTSTRAP_USER:-operador}' and pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    Then I run 'curl -k -s -X GET -H 'Cookie:dcos-acs-auth-cookie=!{dcosAuthCookie}' https://${CLUSTER_ID:-nightly}.${CLUSTER_DOMAIN:-labs.stratio.com}:443/service/${DEPLOY_API:-deploy-api}/universe/sparta/${SPARTA_FLAVOUR}/descriptor | jq .> target/test-classes/schemas/sparta-descriptor.json' locally

    # Create Descriptor
    When I send a 'POST' request to '/service/${DEPLOY_API:-deploy-api}/universe/sparta/${SPARTA_FLAVOUR}-auto/descriptor' based on 'schemas/sparta-descriptor.json' as 'json' with:
      | $.data.model                                                          | REPLACE | ${SPARTA_FLAVOUR}-auto                                                        | string |
      | $.data.container.runners[0].image                                     | REPLACE | ${DOCKER_URL:-qa.stratio.com/stratio/sparta}:${STRATIO_SPARTA_VERSION:-2.6.0} | string |
      | $.parameters.properties.settings.properties.spartaDockerImage.default | REPLACE | ${DOCKER_URL:-qa.stratio.com/stratio/sparta}:${STRATIO_SPARTA_VERSION:-2.6.0} | string |
    Then the service response status must be '201'

  # Add Sparta user in Gosec
  @skipOnEnv(SKIP_USERS)
  Scenario: [QATM-1863][03] Generate Sparta user
    # Generate token to connect to gosec
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    # Send request - Sparta user
    When I send a 'POST' request to '/service/gosecmanagement/api/user' based on 'schemas/gosec/gosec_user.json' as 'json' with:
      | $.id    | UPDATE | ${DCOS_SERVICE_NAME:-sparta-server}            | string |
      | $.name  | UPDATE | ${DCOS_SERVICE_NAME:-sparta-server}            | string |
      | $.email | UPDATE | ${DCOS_SERVICE_NAME:-sparta-server}@sparta.com | string |
    Then the service response status must be '201'
    And the service response must contain the text '"id":"${DCOS_SERVICE_NAME:-sparta-server}"'

  Scenario:[QATM-1863][04] Obtain postgres docker
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When in less than '600' seconds, checking each '20' seconds, I send a 'GET' request to '/exhibitor/exhibitor/v1/explorer/node-data?key=%2Fdatastore%2Fcommunity%2F${POSTGRES_NAME}%2Fplan-v2-json&_=' so that the response contains 'str'
    Then I send a 'GET' request to '/exhibitor/exhibitor/v1/explorer/node-data?key=%2Fdatastore%2Fcommunity%2F${POSTGRES_NAME:-postgrestls}%2Fplan-v2-json&_='
    And I save element '$.str' in environment variable 'exhibitor_answer'
    And I save ''!{exhibitor_answer}'' in variable 'parsed_answer'
    And I wait '2' seconds
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    And I run 'echo !{parsed_answer} | jq '.phases[0]."0001".steps[0]."0"'.agent_hostname | sed 's/^.\|.$//g'' in the ssh connection with exit status '0' and save the value in environment variable 'pgIP'
    And I run 'echo !{pgIP}' in the ssh connection
    Then I wait '10' seconds
    When in less than '600' seconds, checking each '20' seconds, I send a 'GET' request to '/service/${POSTGRES_NAME}/v1/service/status' so that the response contains 'status'
    Then the service response status must be '200'
    And I save element in position '0' in '$.status[?(@.role == "master")].assignedHost' in environment variable 'pgIPCalico'
    Given I open a ssh connection to '!{pgIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    When I run 'sudo docker ps -q |sudo xargs -n 1 docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}} {{ .Name }}' | sed 's/ \// /'| grep '!{pgIPCalico} ' | awk '{print $2}'' in the ssh connection and save the value in environment variable 'postgresDocker'
    And I wait '10' seconds
    And I run 'echo !{postgresDocker}' in the ssh connection with exit status '0'

  @runOnEnv(ADDROLE)
  Scenario:[QATM-1863][05] Add Sparta User in Postgres
    Given I open a ssh connection to '!{pgIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    When I run 'sudo docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "create user \"${DCOS_SERVICE_NAME}\" with password 'xxxxx'"' in the ssh connection
   #Add Sparta dependencies in Postgres

  @skipOnEnv(SKIP_ADD_SCHEMA)
  Scenario:[QATM-1863][06] Add Database and Privileges in Postgres -Schema
    Given I open a ssh connection to '!{pgIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    When I run 'sudo docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "CREATE SCHEMA IF NOT EXISTS \"${DCOS_SERVICE_NAME}\";"' in the ssh connection

  @skipOnEnv(SKIP_ADD_DROP_DATABASE)
  Scenario:[QATM-1863][06] Add Database and Privileges in Postgres -Database
    Given I open a ssh connection to '!{pgIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    When I run 'sudo docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "CREATE DATABASE ${POSTGRES_DATABASE:-sparta};"' in the ssh connection with exit status '0'

   #Add Zookeeper Policy
  @skipOnEnv(SKIP_POLICY_ZK)
  Scenario: [QATM-1863][07] Add zookeper-sparta policy to write in zookeper
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/zookeeper_policy.json' as 'json' with:
      | $.name     | UPDATE | ${DCOS_SERVICE_NAME:-sparta-server}-zk           | n/a |
      | $.id       | UPDATE |  ${DCOS_SERVICE_NAME:-sparta-server}-zk           | n/a |
      | $.users[0] | UPDATE | ${DCOS_SERVICE_NAME:-sparta-server} | n/a |
    Then the service response status must be '201'

  @skipOnEnv(SKIP_POLICY_POSTGRES_AGENT)
  Scenario: [QATM-1863][08] Add postgres policy for authorization in sparta
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/postgres_policy.json' as 'json' with:
      | $.id       | UPDATE | ${DCOS_SERVICE_NAME:-sparta-server}-pg | n/a |
      | $.name     | UPDATE | ${DCOS_SERVICE_NAME:-sparta-server}-pg | n/a |
      | $.users[0] | UPDATE | ${DCOS_SERVICE_NAME:-sparta-server}   | n/a |
      | $.services[0].instancesAcl[0].instances[0].name | UPDATE | ${POSTGRES_NAME}  | n/a |
    Then the service response status must be '201'
    Given I open a ssh connection to '!{pgIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    Then in less than '300' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "\du" -P pager=off' contains '${DCOS_SERVICE_NAME:-sparta-server}'
    When I run 'sudo docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "ALTER USER \"${DCOS_SERVICE_NAME}\" WITH PASSWORD '${POSTGRES_USER_PASS:-1234}';"' in the ssh connection with exit status '0'

  Scenario:[QATM-1863][09] Create sparta user in all private agents
      #Copy PEM file to DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user '${DCOS_CLI_USER:-root}' and password '${DCOS_CLI_PASSWORD:-stratio}'
    Then I run 'mkdir -p /src/test/resources/credentials' in the ssh connection
    And I outbound copy '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}' through a ssh connection to '/src/test/resources/credentials'
    And I run 'chmod 400 /src/test/resources/credentials/key.pem' in the ssh connection
    #Add Users
    When I execute the command 'sudo useradd ${DCOS_SERVICE_NAME}' in all the nodes of my cluster with user '${BOOTSTRAP_USER:-operador}' and pem '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'

  @runOnEnv(DOWNLOAD_IMAGES_IN_ALL_NODES)
  Scenario:[QATM-1863][09 -2] Download all images in enviroment
    #Copy PEM file to DCOS-CLI
    Given I execute the command 'sudo docker pull ${DOCKER_URL:-qa.stratio.com/stratio/sparta}:${STRATIO_SPARTA_VERSION:-2.6.0}' in all the nodes of my cluster with user '${BOOTSTRAP_USER:-operador}' and pem '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    Then  I run 'echo 'All the images are unloaded'' locally

  @skipOnEnv(ADVANCED_INSTALL)
  Scenario: [QATM-1863][10] Basic installation
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    # Obtain schema
    When I send a 'GET' request to '/service/${DEPLOY_API:-deploy-api}/deploy/sparta/${SPARTA_FLAVOUR}-auto/schema?level=1'
    Then the service response status must be '200'
    And I save element '$' in environment variable 'sparta-json-schema'
    # Convert to jsonSchema
    And I convert jsonSchema '!{sparta-json-schema}' to json and save it in variable 'sparta-basic.json'
    And I run 'echo '!{sparta-basic.json}' > target/test-classes/schemas/sparta-basic.json' locally
    # Launch basic install
    When I send a 'POST' request to '/service/${DEPLOY_API:-deploy-api}/deploy/sparta/${SPARTA_FLAVOUR}-auto/schema' based on 'schemas/sparta-basic.json' as 'json' with:
      | $.general.serviceId                                                                             | REPLACE | /sparta/${DCOS_SERVICE_NAME:-sparta-server}/${DCOS_SERVICE_NAME:-sparta-server} | string |
      | $.general.haproxyHost                                                                           | REPLACE | !{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}                            | string |
      | $.general.external.catalog.zookeeperCatalogSwitch.crossdataCoreCatalogZookeeperConnectionstring | REPLACE | ${ZOOKEEPER_NAME:-zkuserland}                                                   | string |
      | $.general.zookeeper.spartaZookeeperConnectionString                                             | REPLACE | ${ZOOKEEPER_NAME:-zkuserland}                                                   | string |
      | $.general.postgresql.availablePgBouncer                                                         | REPLACE | ${PGBOUNCER_ENABLE:-false}                                                      | boolean |
      | $.general.postgresql.postgresHost                                                               | REPLACE | ${POSTGRES_NAME:-postgrestls}                                                   | string |
      | $.general.postgresql.postgresPgHost                                                             | REPLACE | ${PG_BOUNCER_NAME:-pgbouncer}                                                   | string |
      | $.general.postgresql.postgresDatabase                                                           | REPLACE | ${POSTGRES_DATABASE:-sparta}                                                    | string |
      | $.general.calico.networkName                                                                    | REPLACE | ${CALICO-NETWORK:-stratio}                                                      | string |
      | $.general.external.hdfs.hadoopConfUri                                                           | REPLACE | ${CONF_HDFS_URI:-http://10.200.0.74:8085/}                                      | string |
      | $.general.external.hdfs.internalHdfsFramework                                                   | REPLACE | ${INTERNAL_HDFS_FRAMEWORK:-false}                                                | boolean |
      | $.general.external.hdfs.internalHdfsFrameworkName                                               | REPLACE | ${HDFS_INTERNAL_NAME:-hdfs-example}                                             | string |
      | $.settings.intelligencemodelrepo.url                                                            | REPLACE | ${INTELLIGENCEMODELREPO:-https://modelrep.intelligence.marathon.mesos:8000}     | string |
      | $.settings.lineageIntegration.lineageEnable                                                     | REPLACE | ${LINAGE_ENABLE:-true}                                                          | boolean |

    Then the service response status must be '202'
    And I run 'rm -f target/test-classes/schemas/sparta-basic.json' locally


  @runOnEnv(ADVANCED_INSTALL)
  Scenario: [QATM-1863][11] Advanced installation
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
   # Obtain schema
    When I send a 'GET' request to '/service/${DEPLOY_API:-deploy-api}/deploy/sparta/${SPARTA_FLAVOUR}-auto/schema?level=1'
    Then the service response status must be '200'
    And I save element '$' in environment variable 'sparta-json-schema'
   # Convert to jsonSchema
    And I convert jsonSchema '!{sparta-json-schema}' to json and save it in variable 'sparta-advanced.json'
    And I wait '2' seconds
    And I run 'echo '!{sparta-advanced.json}' > target/test-classes/schemas/sparta-advanced.json' locally
    # Launch advanced install
    When I send a 'POST' request to '/service/${DEPLOY_API:-deploy-api}/deploy/sparta/${SPARTA_FLAVOUR}-auto/schema' based on 'schemas/sparta-advanced.json' as 'json' with:
      | $.general.serviceId                                                                             | REPLACE | /sparta/${DCOS_SERVICE_NAME:-sparta-server}/${DCOS_SERVICE_NAME:-sparta-server} | string  |
      | $.general.haproxyHost                                                                           | REPLACE | !{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}                            | string  |
      | $.general.external.catalog.zookeeperCatalogSwitch.crossdataCoreCatalogZookeeperConnectionstring | REPLACE | ${ZOOKEEPER_NAME:-zkuserland}                                                   | string  |
      | $.general.zookeeper.spartaZookeeperConnectionString                                             | REPLACE | ${ZOOKEEPER_NAME:-zkuserland}                                                   | string  |
      | $.general.postgresql.postgresHost                                                               | REPLACE | ${POSTGRES_NAME:-postgrestls}                                                   | string  |
      | $.general.postgresql.postgresDatabase                                                           | REPLACE | ${POSTGRES_DATABASE:-sparta}                                                    | string  |
      | $.general.calico.networkName                                                                    | REPLACE | ${CALICO-NETWORK:-stratio}                                                      | string  |
      | $.general.external.hdfs.hadoopConfUri                                                           | REPLACE | ${CONF_HDFS_URI:-http://10.200.0.74:8085/}                                      | string  |
      | $.settings.intelligencemodelrepo.intelligencemodelrepoEnabled                                   | REPLACE | ${INTELLIGENCE_MODEL_REPO_ENABLE:-false}                                        | boolean |
      | $.settings.intelligencemodelrepo.url                                                            | REPLACE | ${INTELLIGENCEMODELREPO:-https://modelrep.intelligence.marathon.mesos:8000}     | string  |
      | $.security.gosec.enableGosecAuth                                                                | REPLACE | ${GOSEC_ENABLE:-true}                                                           | boolean |
      | $.general.resources.CPUs                                                                        | REPLACE | ${SPARTA_CPU:-3}                                                                | number  |
      | $.general.resources.MEM                                                                         | REPLACE | ${SPARTA_MEM:-4096}                                                             | number  |
      | $.general.resources.INSTANCES                                                                   | REPLACE | ${SPARTA_INSTANCES:-1}                                                          | number  |
      | $.settings.lineageIntegration.lineageEnable                                                     | REPLACE | ${LINAGE_ENABLE:-true}                                                          | boolean |
    Then the service response status must be '202'
    And I run 'rm -f target/test-classes/schemas/sparta-advanced.json' locally

  Scenario: [QATM-1863][11] Check Sparta status
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    # Check Application in API
    Then in less than '500' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/all' so that the response contains '${DCOS_SERVICE_NAME:-sparta-server}'
    # Check status in API
    And in less than '800' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/service?service=/sparta/${DCOS_SERVICE_NAME:-sparta-server}/${DCOS_SERVICE_NAME:-sparta-server}' so that the response contains '"healthy":1'
    # Check status in DCOS
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    Then in less than '500' seconds, checking each '20' seconds, the command output 'dcos task | grep ${DCOS_SERVICE_NAME:-sparta-server} | grep R | wc -l' contains '1'
    When I run 'dcos task |  awk '{print $5}' | grep ${DCOS_SERVICE_NAME:-sparta-server}' in the ssh connection and save the value in environment variable 'spartaTaskId'
    Then in less than '1200' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{spartaTaskId} | grep TASK_RUNNING' contains 'TASK_RUNNING'
    And in less than '1200' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{spartaTaskId} | grep healthCheckResults' contains 'healthCheckResults'
    And in less than '1200' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{spartaTaskId} | grep  '"alive": true'' contains '"alive": true'

  #Add Sparta Policy
  @skipOnEnv(SKIP_POLICY_SP)
  Scenario: [SPARTA-1161][12] Add sparta policy for authorization in sparta with full security
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/sparta_policy.json' as 'json' with:
      | $.id       | UPDATE | ${DCOS_SERVICE_NAME} | n/a |
      | $.name     | UPDATE | ${DCOS_SERVICE_NAME} | n/a |
      | $.users[0] | UPDATE | ${SPARTA-USER:-sparta-server} | n/a |
    Then the service response status must be '201'
    And I wait '3' seconds