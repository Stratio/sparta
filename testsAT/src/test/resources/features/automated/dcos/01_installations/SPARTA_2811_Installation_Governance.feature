@rest
Feature: [Installation Business Glossary API Command Center]

   #######################################
   ## Business Glossary API Instalation ##
   #######################################
#  Scenario:[Setup] Select folder and instance
#    Given I run 'echo "${FOLDER_GOVERNANCE:-governance}/${BG_API_SERVICE_NAME:-dg-businessglossary-api}" | sed 's/^[\/]*//'' locally and save the value in environment variable 'IdBGAPI'
#    And I run 'echo "${FOLDER_GOVERNANCE:-governance}/${BG_API_SERVICE_NAME:-dg-businessglossary-api}" | sed 's/^[\/]*//' | sed "s/\//\n/g" | tac | sed ':a;N;$!ba;s/\n/\./g'' locally and save the value in environment variable 'IdDCOSBGAPI'
#
#  Scenario:[01][QATM-1867] Create basic installation file - Business Glossary API
#    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
#    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
#    #Obtain schema
#    When I send a 'GET' request to '/service/deploy-api/deploy/governance/governance-api-${DG_FLAVOUR}/schema?level=1'
#    Then I save element '$' in environment variable 'governance-api-json-schema'
#    #Convert to jsonSchema
#    And I convert jsonSchema '!{governance-api-json-schema}' to json and save it in variable 'governance-api-basic.json'
#    And I run 'echo '!{governance-api-basic.json}' > target/test-classes/schemas/governance-api-basic.json' locally
#    When I create file 'governance-api-config.json' based on 'schemas/governance-api-basic.json' as 'json' with:
#      | $.general.calico.networkName                 | REPLACE | ${DATAGOV_CALICO_NETWORK:-stratio}                   | string  |
#      | $.general.indentity.approlename              | REPLACE | ${DATAGOV_VAULT_ROLE:-open}                          | string  |
#
#  @runOnEnv(ADVANCED_INSTALL)
#  Scenario:[01b][QATM-1867] Create advanced installation file
#    When I create file 'governance-api-config.json' based on 'governance-api-config.json' as 'json' with:
#      | $.general.resources.CPUs                     | REPLACE | ${DATAGOV_BG_API_CPU:-2}                             | number  |
#      | $.general.resources.MEM                      | REPLACE | ${DATAGOV_BG_API_MEM:-1024}                          | number  |
#      | $.general.resources.INSTANCES                | REPLACE | ${DATAGOV_BG_API_INSTANCES:-1}                       | number  |
#      | $.settings.logs.debug                        | REPLACE | ${DATAGOV_BG_API_LOG_LEVEL_DEBUG:-false}             | boolean |
#      | $.security.tlsAuthClient                     | REPLACE | ${DATAGOV_BG_API_CN_WHITELIST:-*}                    | string  |
#
#  Scenario:[02][QATM-1867] Installation Command Center Business Glossary API
#    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
#    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
#    When I send a 'POST' request to '/service/deploy-api/deploy/governance/governance-api-${DG_FLAVOUR}/schema' based on 'governance-api-config.json' as 'json'
#    Then the service response status must be '202'
#
#  Scenario:[03][QATM-1867] Check status installation
#    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
#    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
#    # Check Application in API
#    Then in less than '200' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/all' so that the response contains '!{IdBGAPI}'
#    # Check status in API
#    And in less than '500' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/service?service=/!{IdBGAPI}' so that the response contains '"healthy":2'
#    And in less than '500' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/service?service=/!{IdBGAPI}' so that the response contains '"status":2'
#    # Check status in DCOS
#    When I open a ssh connection to '${DCOS_CLI_HOST:-dcos-cli.demo.labs.stratio.com}' with user 'root' and password 'stratio'
#    Then in less than '500' seconds, checking each '20' seconds, the command output 'dcos task | grep -w !{IdDCOSBGAPI} | awk '{print $4}' | grep R | wc -l' contains '1'
#    And I run 'dcos marathon task list | grep !{IdBGAPI} | awk '{print $5}'' in the ssh connection and save the value in environment variable 'BGAPITaskId'
#    Then in less than '1200' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{BGAPITaskId} | grep TASK_RUNNING | wc -l' contains '1'

#  Scenario:[04][QATM-1867] Check schema dg_metadata
#    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
#    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
#    When in less than '300' seconds, checking each '20' seconds, I send a 'GET' request to '/exhibitor/exhibitor/v1/explorer/node-data?key=%2Fdatastore%2Fcommunity%2F${POSTGRES_FRAMEWORK_ID_EOS:-postgreseos}%2Fplan-v2-json&_=' so that the response contains 'str'
#    And the service response status must be '200'
#    And I save element '$.str' in environment variable 'exhibitor_answer'
#    And I save ''!{exhibitor_answer}'' in variable 'parsed_answer'
#    And I run 'echo !{parsed_answer} | jq '.phases[0]' | jq '."0001".steps[0]'| jq '."0"'.agent_hostname | sed 's/^.\|.$//g'' in the ssh connection with exit status '0' and save the value in environment variable 'pgIP'
#    And I run 'echo !{pgIP}' in the ssh connection
#    Then I wait '10' seconds
#    When in less than '300' seconds, checking each '20' seconds, I send a 'GET' request to '/service/${POSTGRES_FRAMEWORK_ID_EOS:-postgreseos}/v1/service/status' so that the response contains 'status'
#    Then the service response status must be '200'
#    And I save element in position '0' in '$.status[?(@.role == "master")].assignedHost' in environment variable 'pgIPCalico'
#    And I save element in position '0' in '$.status[?(@.role == "master")].ports[0]' in environment variable 'pgPortCalico'
#    Given I open a ssh connection to '!{pgIP}' with user '${CLI_USER:-root}' and password '${CLI_PASSWORD:-stratio}'
#    When I run 'docker ps -q | xargs -n 1 docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}} {{ .Name }}' | sed 's/ \// /'| grep '!{pgIPCalico} '| awk '{print $2}'' in the ssh connection and save the value in environment variable 'postgresDocker'
#    When I run 'docker exec -t !{postgresDocker} psql -p !{pgPortCalico} -U "${DG_BACKEND_USER:-dg-bootstrap}" -d ${DG_BACKEND_DATABASE:-postgreseos} -c "SELECT schema_name FROM information_schema.schemata WHERE schema_name = '${DATAGOV_BACKEND_SCHEMA:-dg_metadata}'"' in the ssh connection
#    Then the command output contains 'dg_metadata'

    #########################
    ## GOV UI INSTALLATION ##
    #########################

  Scenario:[Setup] Select folder and instance
    Given I run 'echo "${FOLDER_GOVERNANCE:-governance}/${BG_API_SERVICE_NAME:-dg-businessglossary-api}" | sed 's/^[\/]*//'' locally and save the value in environment variable 'IdBGAPI'
    And I run 'echo "${FOLDER_GOVERNANCE:-governance}/${BG_API_SERVICE_NAME:-dg-businessglossary-api}" | sed 's/^[\/]*//' | sed "s/\//\n/g" | tac | sed ':a;N;$!ba;s/\n/\./g'' locally and save the value in environment variable 'IdDCOSBGAPI'

  Scenario:[01][QATM-1867] Create basic installation file
    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    # Obtain schema
    When I send a 'GET' request to '/service/deploy-api/deploy/governance/governance-api-${DG_FLAVOUR}/schema?level=1'
    Then I save element '$' in environment variable 'governance-api-json-schema'
    # Convert to jsonSchema
    And I convert jsonSchema '!{governance-api-json-schema}' to json and save it in variable 'governance-api-basic.json'
    And I run 'echo '!{governance-api-basic.json}' > target/test-classes/schemas/governance-api-basic.json' locally
    When I create file 'governance-api-config.json' based on 'schemas/governance-api-basic.json' as 'json' with:
      | $.general.calico.networkName                 | REPLACE | ${DATAGOV_CALICO_NETWORK:-stratio}                   | string  |
      | $.general.indentity.approlename              | REPLACE | ${DATAGOV_VAULT_ROLE:-open}                          | string  |

  @runOnEnv(ADVANCED_INSTALL)
  Scenario:[01b][QATM-1867] Create advanced installation file
    When I create file 'governance-api-config.json' based on 'governance-api-config.json' as 'json' with:
      | $.general.resources.CPUs                     | REPLACE | ${DATAGOV_BG_API_CPU:-2}                             | number  |
      | $.general.resources.MEM                      | REPLACE | ${DATAGOV_BG_API_MEM:-1024}                          | number  |
      | $.general.resources.INSTANCES                | REPLACE | ${DATAGOV_BG_API_INSTANCES:-1}                       | number  |
      | $.settings.logs.debug                        | REPLACE | ${DATAGOV_BG_API_LOG_LEVEL_DEBUG:-false}             | boolean |
      | $.security.tlsAuthClient                     | REPLACE | ${DATAGOV_BG_API_CN_WHITELIST:-*}                    | string  |

  Scenario:[02][QATM-1867] Installation Command Center Business Glossary API
    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I send a 'POST' request to '/service/deploy-api/deploy/governance/governance-api-${DG_FLAVOUR}/schema' based on 'governance-api-config.json' as 'json'
    Then the service response status must be '202'

  Scenario:[03][QATM-1867] Check status installation
    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    # Check Application in API
    Then in less than '200' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/all' so that the response contains '!{IdBGAPI}'
    # Check status in API
    And in less than '500' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/service?service=/!{IdBGAPI}' so that the response contains '"healthy":2'
    And in less than '500' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/service?service=/!{IdBGAPI}' so that the response contains '"status":2'
    # Check status in DCOS
    When I open a ssh connection to '${DCOS_CLI_HOST:-dcos-cli.demo.labs.stratio.com}' with user 'root' and password 'stratio'
    Then in less than '500' seconds, checking each '20' seconds, the command output 'dcos task | grep -w !{IdDCOSBGAPI} | awk '{print $4}' | grep R | wc -l' contains '1'
    And I run 'dcos marathon task list | grep !{IdBGAPI} | awk '{print $5}'' in the ssh connection and save the value in environment variable 'BGAPITaskId'
    Then in less than '1200' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{BGAPITaskId} | grep TASK_RUNNING | wc -l' contains '1'

  Scenario:[04][QATM-1867] Check schema dg_metadata
    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When in less than '300' seconds, checking each '20' seconds, I send a 'GET' request to '/exhibitor/exhibitor/v1/explorer/node-data?key=%2Fdatastore%2Fcommunity%2F${POSTGRES_FRAMEWORK_ID_EOS:-postgreseos}%2Fplan-v2-json&_=' so that the response contains 'str'
    And the service response status must be '200'
    And I save element '$.str' in environment variable 'exhibitor_answer'
    And I save ''!{exhibitor_answer}'' in variable 'parsed_answer'
    And I run 'echo !{parsed_answer} | jq '.phases[0]' | jq '."0001".steps[0]'| jq '."0"'.agent_hostname | sed 's/^.\|.$//g'' in the ssh connection with exit status '0' and save the value in environment variable 'pgIP'
    And I run 'echo !{pgIP}' in the ssh connection
    Then I wait '10' seconds
    When in less than '300' seconds, checking each '20' seconds, I send a 'GET' request to '/service/${POSTGRES_FRAMEWORK_ID_EOS:-postgreseos}/v1/service/status' so that the response contains 'status'
    Then the service response status must be '200'
    And I save element in position '0' in '$.status[?(@.role == "master")].assignedHost' in environment variable 'pgIPCalico'
    And I save element in position '0' in '$.status[?(@.role == "master")].ports[0]' in environment variable 'pgPortCalico'
    Given I open a ssh connection to '!{pgIP}' with user '${CLI_USER:-root}' and password '${CLI_PASSWORD:-stratio}'
    When I run 'docker ps -q | xargs -n 1 docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}} {{ .Name }}' | sed 's/ \// /'| grep '!{pgIPCalico} ' | awk '{print $2}'' in the ssh connection and save the value in environment variable 'postgresDocker'
    When I run 'docker exec -t !{postgresDocker} psql -p !{pgPortCalico} -U "${DG_BACKEND_USER:-dg-bootstrap}" -d ${DG_BACKEND_DATABASE:-postgreseos} -c "SELECT schema_name FROM information_schema.schemata WHERE schema_name = '${DATAGOV_BACKEND_SCHEMA:-dg_metadata}'"' in the ssh connection
    Then the command output contains 'dg_metadata'

    ############################
    # HDFS AGENT INSTALLATION ##
    ############################


  Scenario:[Setup] Select folder and instance for hdfs agent
    Given I run 'echo "${FOLDER_GOVERNANCE:-governance}/${DATAGOV_HDFS_AGENT_SERVICE_NAME:-dg-hdfs-agent}" | sed 's/^[\/]*//'' locally and save the value in environment variable 'IdHDFSAgent'
    And I run 'echo "${FOLDER_GOVERNANCE:-governance}/${DATAGOV_HDFS_AGENT_SERVICE_NAME:-dg-hdfs-agent}" | sed 's/^[\/]*//' | sed "s/\//\n/g" | tac | sed ':a;N;$!ba;s/\n/\./g'' locally and save the value in environment variable 'IdDCOSHDFSAgent'

  Scenario:[01][QATM-1867] Create basic installation file for hdfs agent
    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    # Obtain schema
    When I send a 'GET' request to '/service/deploy-api/deploy/governance/dg-hdfs-agent-${DG_FLAVOUR}/schema?level=1'
    Then I save element '$' in environment variable 'dg-hdfs-agent-schema'
    # Convert to jsonSchema
    And I convert jsonSchema '!{dg-hdfs-agent-schema}' to json and save it in variable 'dg-hdfs-agent-basic.json'
    And I run 'echo '!{dg-hdfs-agent-basic.json}' > target/test-classes/schemas/dg-hdfs-agent-basic.json' locally
    When I create file 'dg-hdfs-agent-config.json' based on 'schemas/dg-hdfs-agent-basic.json' as 'json' with:
      | $.general.serviceId                                             | REPLACE | !{IdHDFSAgent}                                                      | string  |
      | $.general.hdfs.hdfsdiscover.hdfsServiceVersion                  | REPLACE | ${DATAGOV_HDFS_SERVICE_VERSION:-2.9.2}                              | string  |
      | $.general.hdfs.hdfsdiscover.hdfsServiceName                     | REPLACE | ${DATAGOV_HDFS_SERVICE_NAME:-10.200.0.74}                           | string  |
      | $.general.hdfs.hdfsdiscover.hdfsInitPath                        | REPLACE | ${DATAGOV_HDFS_INIT_PATH:-/user/dg-agent}                           | string  |
      | $.general.hdfs.hdfsdiscover.kerberosConfUri                     | REPLACE | ${DATAGOV_HDFS_KRB_CONF:-http://10.200.0.74:8085/krb5.conf}         | string  |
      | $.general.hdfs.hdfsdiscover.hdfsCoresiteUri                     | REPLACE | ${DATAGOV_HDFS_CORE_SITE:-http://10.200.0.74:8085/core-site.xml}    | string  |
      | $.general.hdfs.hdfsdiscover.hdfsSiteUri                         | REPLACE | ${DATAGOV_HDFS_HDFS_SITE:-http://10.200.0.74:8085/hdfs-site.xml}    | string  |
      | $.general.calico.networkName                                    | REPLACE | ${DATAGOV_CALICO_NETWORK:-stratio}                                  | string  |
      | $.general.identity.approlename                                  | REPLACE | ${DATAGOV_VAULT_ROLE:-open}                                         | string  |
      | $.general.resources.CPUs                                        | REPLACE | ${DATAGOV_HDFS_AGENT_CPU:-2}                                        | number  |

  @runOnEnv(ADVANCED_INSTALL)
  Scenario:[01b][QATM-1867] Create advanced installation file
    When I create file 'dg-hdfs-agent-config.json' based on 'dg-hdfs-agent-config.json' as 'json' with:
      | $.general.identity.hdfsSecurityPrincipal                        | REPLACE | ${DATAGOV_HDFS_PRINCIPAL:-dg-agent}                                 | string  |
      | $.general.resources.MEM                                         | REPLACE | ${DATAGOV_HDFS_AGENT_MEM:-2048}                                     | number  |
      | $.general.resources.INSTANCES                                   | REPLACE | ${DATAGOV_HDFS_AGENT_INSTANCES:-1}                                  | number  |
      | $.settings.poolingInterval                                      | REPLACE | ${DATAGOV_HDFS_AGENT_POOLING:-30000}                                | number  |
      | $.settings.logs.commonsLogLevel                                 | REPLACE | ${DATAGOV_HDFS_AGENT_COMMONS_LOG_LEVEL:-INFO}                       | string  |
      | $.settings.logs.serviceLogLevel                                 | REPLACE | ${DATAGOV_HDFS_AGENT_SERVICE_LOG_LEVEL:-INFO}                       | string  |
      | $.settings.logs.hdfsAgentLogLevel                               | REPLACE | ${DATAGOV_HDFS_AGENT_LOG_LEVEL:-INFO}                               | string  |
      | $.security.hdfsServiceSecurity                                  | REPLACE | ${DATAGOV_HDFS_SERVICE_SECURITY:-NA}                                | string  |
      | $.security.hdfsServiceUrl                                       | REPLACE | ${DATAGOV_HDFS_SERVICE_URL:-HA}                                     | string  |

  Scenario:[02][QATM-1867] Installation Command Center HDFS Agent
    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I send a 'POST' request to '/service/deploy-api/deploy/governance/dg-hdfs-agent-${DG_FLAVOUR}/schema' based on 'dg-hdfs-agent-config.json' as 'json'
    Then the service response status must be '202'

  Scenario:[03][QATM-1867] Check status installation
    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    # Check Application in API
    Then in less than '200' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/all' so that the response contains '!{IdHDFSAgent}'
    # Check status in API
    And in less than '500' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/service?service=/!{IdHDFSAgent}' so that the response contains '"healthy":1'
    And in less than '500' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/service?service=/!{IdHDFSAgent}' so that the response contains '"status":2'
    # Check status in DCOS
    When I open a ssh connection to '${DCOS_CLI_HOST:-dcos-cli.demo.labs.stratio.com}' with user 'root' and password 'stratio'
    Then in less than '500' seconds, checking each '20' seconds, the command output 'dcos task | grep -w !{IdDCOSHDFSAgent} | awk '{print $4}' | grep R | wc -l' contains '1'
    And I run 'dcos marathon task list | grep !{IdHDFSAgent} | awk '{print $5}'' in the ssh connection and save the value in environment variable 'HDFSAgentTaskId'
    Then in less than '1200' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{HDFSAgentTaskId} | grep TASK_RUNNING | wc -l' contains '1'
    Then in less than '1200' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{HDFSAgentTaskId} | grep healthCheckResults | wc -l' contains '1'
    Then in less than '1200' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{HDFSAgentTaskId} | grep '"alive": true' | wc -l' contains '2'


    ##################################
    ## POSTGRES AGENT INSTALLATION  ##
    ##################################

  Scenario: Initial setup Postgres Agent
    Given I open a ssh connection to '${BOOTSTRAP_IP}' with user '${REMOTE_USER:-operador}' using pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
    And I run 'grep -Po '"root_token":\s*"(\d*?,|.*?[^\\]")' /stratio_volume/vault_response | awk -F":" '{print $2}' | sed -e 's/^\s*"//' -e 's/"$//'' in the ssh connection and save the value in environment variable 'vaultToken'
    And I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
    And I open a ssh connection to '${DCOS_CLI_HOST:-dcos-cli.demo.stratio.com}' with user '${CLI_USER:-root}' and password '${CLI_PASSWORD:-stratio}'
    And I run 'dcos marathon app show ${POSTGRES_FRAMEWORK_ID_GOV:-postgresgov} | grep -w POSTGRES_GOSEC_DOCKER_IMAGE | sed 's/.*postgres-agent://g' | sed 's/\",//g'' in the ssh connection and save the value in environment variable 'postgresAgentVersion'
    And I securely send requests to '${DCOS_IP}:443'

  @runOnEnv(POSTGRES_AGENT=true)
  Scenario:[01] Obtain postgreSQL host and port for Postgresgov
    Given I send a 'GET' request to '/service/${POSTGRES_FRAMEWORK_ID_GOV:-postgresgov}/v1/service/status'
    Then the service response status must be '200'
    And I save element in position '0' in '$.status[?(@.role == "master")].dnsHostname' in environment variable 'postgres_Host_discover'
    And I save element in position '0' in '$.status[?(@.role == "master")].ports[0]' in environment variable 'postgres_Port_discover'
    And I wait '5' seconds

  @runOnEnv(DATA_GOV_POLICY=true)
  @runOnEnv(DATA_GOV_USER_LDAP=false)
  Scenario:[03] Creation user dg-bootstrap
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user '${DCOS_USER:-admin}' and password '${DCOS_PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    When I send a 'POST' request to '${BASE_END_POINT:-/service/gosecmanagement}/api/user' based on 'schemas/gosec/gosec_user.json' as 'json' with:
      | $.id                                            | UPDATE  | ${DATAGOV_POLICY_USER_ID:-dg-agent}               | string |
      | $.name                                          | UPDATE  | ${DATAGOV_POLICY_USER_NAME:-dg-agent}             | string |
      | $.email                                         | UPDATE  | ${DATAGOV_POLICY_USER_NAME:-dg-agent@stratio.com} | string |
    Then the service response status must be '201'
    And the service response must contain the text '"id":"dg-agent"'
    And I wait '10' seconds

  @runOnEnv(DATA_GOV_POLICY=false)
  @runOnEnv(POSTGRES_AGENT=true)
  Scenario:[07] Creation database and user in Postgres Community
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    When in less than '300' seconds, checking each '20' seconds, I send a 'GET' request to '/exhibitor/exhibitor/v1/explorer/node-data?key=%2Fdatastore%2Fcommunity%2F${POSTGRES_FRAMEWORK_ID_GOV:-postgresgov}%2Fplan-v2-json&_=' so that the response contains 'str'
    And the service response status must be '200'
    And I save element '$.str' in environment variable 'exhibitor_answer'
    And I save ''!{exhibitor_answer}'' in variable 'parsed_answer'
    And I run 'echo !{parsed_answer} | jq '.phases[0]' | jq '."0001".steps[0]'| jq '."0"'.agent_hostname | sed 's/^.\|.$//g'' in the ssh connection with exit status '0' and save the value in environment variable 'pgIP'
    And I run 'echo !{pgIP}' in the ssh connection
    Then I wait '10' seconds
    When in less than '300' seconds, checking each '20' seconds, I send a 'GET' request to '/service/${POSTGRES_FRAMEWORK_ID_GOV:-postgresgov}/v1/service/status' so that the response contains 'status'
    Then the service response status must be '200'
    And I save element in position '0' in '$.status[?(@.role == "master")].assignedHost' in environment variable 'pgIPCalico'
    And I save element in position '0' in '$.status[?(@.role == "master")].ports[0]' in environment variable 'pgPortCalico'
    Given I open a ssh connection to '!{pgIP}' with user '${CLI_USER:-root}' and password '${CLI_PASSWORD:-stratio}'
    When I run 'docker ps -q | xargs -n 1 docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}} {{ .Name }}' | sed 's/ \// /'| grep '!{pgIPCalico} ' | awk '{print $2}'' in the ssh connection and save the value in environment variable 'postgresDocker'
    When I run 'docker exec -t !{postgresDocker} psql -p !{pgPortCalico} -U postgres -c "CREATE DATABASE ${DG_POSTGRESQL_AGENT_DATABASE:-governance}"' in the ssh connection
    Then the command output contains 'CREATE DATABASE'
    When I run 'docker exec -t !{postgresDocker} psql -p !{pgPortCalico} -U postgres -c "CREATE USER \"${DG_POSTGRESQL_AGENT_USER:-dg-agent}\" with password '${DG_POSTGRESQL_AGENT_PASSWORD:-stratio}' SUPERUSER CREATEDB CREATEROLE REPLICATION BYPASSRLS LOGIN"' in the ssh connection
    Then the command output contains 'CREATE ROLE'

  @runOnEnv(DATA_GOV_POLICY=true)
  @runOnEnv(POSTGRES_AGENT=true)
  Scenario:[08] Creation database in Postgres Community
    Given I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    When in less than '300' seconds, checking each '20' seconds, I send a 'GET' request to '/exhibitor/exhibitor/v1/explorer/node-data?key=%2Fdatastore%2Fcommunity%2F${POSTGRES_FRAMEWORK_ID_GOV:-postgresgov}%2Fplan-v2-json&_=' so that the response contains 'str'
    And the service response status must be '200'
    And I save element '$.str' in environment variable 'exhibitor_answer'
    And I save ''!{exhibitor_answer}'' in variable 'parsed_answer'
    And I run 'echo !{parsed_answer} | jq '.phases[0]' | jq '."0001".steps[0]'| jq '."0"'.agent_hostname | sed 's/^.\|.$//g'' in the ssh connection with exit status '0' and save the value in environment variable 'pgIP'
    And I run 'echo !{pgIP}' in the ssh connection
    Then I wait '10' seconds
    When in less than '300' seconds, checking each '20' seconds, I send a 'GET' request to '/service/${POSTGRES_FRAMEWORK_ID_GOV:-postgresgov}/v1/service/status' so that the response contains 'status'
    Then the service response status must be '200'
    And I save element in position '0' in '$.status[?(@.role == "master")].assignedHost' in environment variable 'pgIPCalico'
    And I save element in position '0' in '$.status[?(@.role == "master")].ports[0]' in environment variable 'pgPortCalico'
    Given I open a ssh connection to '!{pgIP}' with user '${CLI_USER:-root}' and password '${CLI_PASSWORD:-stratio}'
    When I run 'docker ps -q | xargs -n 1 docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}} {{ .Name }}' | sed 's/ \// /'| grep "!{pgIPCalico} " | awk '{print $2}'' in the ssh connection and save the value in environment variable 'postgresDocker'
    When I run 'docker exec -t !{postgresDocker} psql -p !{pgPortCalico} -U postgres -c "CREATE DATABASE ${DG_POSTGRESQL_AGENT_DATABASE:-governance}"' in the ssh connection
    Then the command output contains 'CREATE DATABASE'

  @runOnEnv(POSTGRES_AGENT=true)
  @runOnEnv(DATA_GOV_POLICY=true)
  Scenario:[09] Creation policy for database and user for Postgres Community
    Given I set sso token using host '${CLUSTER_ID:-nightly}.labs.stratio.com' with user '${DCOS_USER:-admin}' and password '${DCOS_PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID:-nightly}.labs.stratio.com:443'
    When I send a 'POST' request to '${BASE_END_POINT:-/service/gosecmanagement}/api/policy' based on 'schemas/gosec/objectsPolicy.json' as 'json' with:
      | $.id                                            | UPDATE  | ${DATAGOV_POLICY:-testDatagovGOV}               | string |
      | $.name                                          | UPDATE  | ${DATAGOV_POLICY:-testDatagovGOV}               | string |
      | $.users                                         | REPLACE | [${DG_POSTGRESQL_AGENT_USER:-dg-agent}]         | array  |
      | $.services[0].instancesAcl[0].instances[0].name | UPDATE  | ${POSTGRES_FRAMEWORK_ID_GOV:-postgresgov}       | string |
      | $.services[0].version                           | UPDATE  | !{postgresAgentVersion}                         | string |
    Then the service response status must be '201'
    And the service response must contain the text '"id":"${DATAGOV_POLICY:-testDatagovGOV}"'
    And I wait '120' seconds

  @runOnEnv(POSTGRES_AGENT=true)
  Scenario:[10] Creation schema governance in Postgres Community
    Given I open a ssh connection to '!{pgIP}' with user '${CLI_USER:-root}' and password '${CLI_PASSWORD:-stratio}'
    When I run 'docker exec -t !{postgresDocker} psql -p !{pgPortCalico} -U "${DG_POSTGRESQL_AGENT_USER:-dg-agent}" -d ${DG_POSTGRESQL_AGENT_DATABASE:-governance} -c "CREATE SCHEMA governance" | grep "CREATE SCHEMA" | wc -l' in the ssh connection
    Then the command output contains '1'

  @runOnEnv(POSTGRES_AGENT=true)
  Scenario:[11] Creation 100 tables in Postgres Community
    Given I open a ssh connection to '!{pgIP}' with user '${CLI_USER:-root}' and password '${CLI_PASSWORD:-stratio}'
    And I outbound copy 'src/test/resources/postgres/createTable.sql' through a ssh connection to '/tmp'
    When I run 'docker cp /tmp/createTable.sql !{postgresDocker}:/tmp/ ; docker exec -t !{postgresDocker} psql -p !{pgPortCalico} -U "${DG_POSTGRESQL_AGENT_USER:-dg-agent}" -d ${DG_POSTGRESQL_AGENT_DATABASE:-governance} -f /tmp/createTable.sql | grep "CREATE TABLE" | wc -l' in the ssh connection
    Then the command output contains '200'

  @runOnEnv(POSTGRES_AGENT=true)
  Scenario:[12] Check 100 tables created in Postgres Community
    Given I open a ssh connection to '!{pgIP}' with user '${CLI_USER:-root}' and password '${CLI_PASSWORD:-stratio}'
    And I outbound copy 'src/test/resources/postgres/createTable.sql' through a ssh connection to '/tmp'
    When I run 'docker cp /tmp/createTable.sql !{postgresDocker}:/tmp/ ; docker exec -t !{postgresDocker} psql -p !{pgPortCalico} -U "${DG_POSTGRESQL_AGENT_USER:-dg-agent}" -d ${DG_POSTGRESQL_AGENT_DATABASE:-governance} -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public'"' in the ssh connection
    Then the command output contains '100'
    When I run 'docker exec -t !{postgresDocker} psql -p !{pgPortCalico} -U "${DG_POSTGRESQL_AGENT_USER:-dg-agent}" -d ${DG_POSTGRESQL_AGENT_DATABASE:-governance} -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='governance'"' in the ssh connection
    Then the command output contains '100'

  @runOnEnv(POSTGRES_AGENT_XL=true)
  @runOnEnv(DATA_GOV_POLICY=false)
  Scenario:[13] Check Postgres XL and create role and database
    Given I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    When in less than '300' seconds, checking each '20' seconds, I send a 'GET' request to '/exhibitor/exhibitor/v1/explorer/node-data?key=%2Fdatastore%2Fpbd%2F${POSTGRESXL_FRAMEWORK_ID:-postgresbd}%2Fplan-v2-json&_=' so that the response contains 'str'
    And the service response status must be '200'
    And I save element '$.str' in environment variable 'exhibitor_answer'
    And I run 'echo '!{exhibitor_answer}' | jq -c -r -M '.phases | .. | .steps?[0] | .. | select(.name? == "co_0001") | select(. != null) | .agent_hostname'' in the ssh connection with exit status '0' and save the value in environment variable 'pgIP'
    Then I wait '10' seconds
    Given I open a ssh connection to '!{pgIP}' with user '${CLI_USER:-root}' and password '${CLI_PASSWORD:-stratio}'
    When I run 'docker ps -q | xargs -n 1 docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}} {{ .Name }}' | sed 's/ \// /'| grep "!{postgresxl_IP_datastore} " | awk '{print $2}'' in the ssh connection and save the value in environment variable 'postgresDocker'
    And I run 'echo '!{postgresDocker}'' locally
    When I run 'docker exec -t !{postgresDocker} psql -p !{postgresxl_Port_datastore} -U postgres -c "CREATE DATABASE ${DG_POSTGRESQL_AGENT_DATABASE:-governance}"' in the ssh connection
    Then the command output contains 'CREATE DATABASE'
    When I run 'docker exec -t !{postgresDocker} psql -p !{postgresxl_Port_datastore} -U postgres -c "CREATE USER \"${DG_POSTGRESQL_AGENT_USER:-dg-agent}\" with password '${DG_POSTGRESQL_AGENT_PASSWORD:-stratio}' SUPERUSER CREATEDB CREATEROLE REPLICATION BYPASSRLS LOGIN"' in the ssh connection
    Then the command output contains 'CREATE ROLE'

  @runOnEnv(POSTGRES_AGENT_XL=true)
  @runOnEnv(DATA_GOV_POLICY=true)
  Scenario:[14] Check Postgres XL and create database
    Given I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    When in less than '300' seconds, checking each '20' seconds, I send a 'GET' request to '/exhibitor/exhibitor/v1/explorer/node-data?key=%2Fdatastore%2Fpbd%2F${POSTGRESXL_FRAMEWORK_ID:-postgresbd}%2Fplan-v2-json&_=' so that the response contains 'str'
    And the service response status must be '200'
    And I save element '$.str' in environment variable 'exhibitor_answer'
    And I run 'echo '!{exhibitor_answer}' | jq -c -r -M '.phases | .. | .steps?[0] | .. | select(.name? == "co_0001") | select(. != null) | .agent_hostname'' in the ssh connection with exit status '0' and save the value in environment variable 'pgIP'
    Then I wait '10' seconds
    Given I open a ssh connection to '!{pgIP}' with user '${CLI_USER:-root}' and password '${CLI_PASSWORD:-stratio}'
    When I run 'docker ps -q | xargs -n 1 docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}} {{ .Name }}' | sed 's/ \// /'| grep "!{postgresxl_IP_datastore} " | awk '{print $2}'' in the ssh connection and save the value in environment variable 'postgresDocker'
    And I run 'echo '!{postgresDocker}'' locally
    When I run 'docker exec -t !{postgresDocker} psql -p !{postgresxl_Port_datastore} -U postgres -c "CREATE DATABASE ${DG_POSTGRESQL_AGENT_DATABASE:-governance}"' in the ssh connection
    Then the command output contains 'CREATE DATABASE'

  @runOnEnv(POSTGRES_AGENT_XL=true)
  @runOnEnv(DATA_GOV_POLICY=true)
  Scenario:[15] Creation policy for database and user for Postgres XL
    Given I set sso token using host '${CLUSTER_ID:-nightly}.labs.stratio.com' with user '${DCOS_USER:-admin}' and password '${DCOS_PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID:-nightly}.labs.stratio.com:443'
    When I send a 'POST' request to '${BASE_END_POINT:-/service/gosecmanagement}/api/policy' based on 'schemas/gosec/objectsPolicy.json' as 'json' with:
      | $.id                                            | UPDATE  | testDatagovGOVXL                                | string |
      | $.name                                          | UPDATE  | testDatagovGOVXL                                | string |
      | $.users                                         | REPLACE | [dg-agent]                                      | array  |
      | $.services[0].type                              | UPDATE  | ${POSTGRESXL_TYPE:-distributedpostgres}         | string |
      | $.services[0].instancesAcl[0].instances[0].name | UPDATE  | ${POSTGRESXL_FRAMEWORK_ID:-postgresbd}          | string |
      | $.services[0].version                           | UPDATE  | !{postgresAgentVersion}                         | string |
    Then the service response status must be '201'
    And the service response must contain the text '"id":"testDatagovGOVXL"'
    And I wait '120' seconds

  @runOnEnv(POSTGRES_AGENT_XL=true)
  Scenario:[16] Creation schema governance in Postgres XL
    Given I open a ssh connection to '!{pgIP}' with user '${CLI_USER:-root}' and password '${CLI_PASSWORD:-stratio}'
    And I outbound copy 'src/test/resources/postgres/createTable.sql' through a ssh connection to '/tmp'
    When I run 'docker exec -t !{postgresDocker} psql -p !{postgresxl_Port_datastore} -U "${DG_POSTGRESQL_AGENT_USER:-dg-agent}" -d ${DG_POSTGRESQL_AGENT_DATABASE:-governance} -c "CREATE SCHEMA governance" | grep "CREATE SCHEMA" | wc -l' in the ssh connection
    Then the command output contains '1'

  @runOnEnv(POSTGRES_AGENT_XL=true)
  Scenario:[17] Creation 100 tables in Postgres XL
    Given I open a ssh connection to '!{pgIP}' with user '${CLI_USER:-root}' and password '${CLI_PASSWORD:-stratio}'
    And I outbound copy 'src/test/resources/postgres/createTable.sql' through a ssh connection to '/tmp'
    When I run 'docker cp /tmp/createTable.sql !{postgresDocker}:/tmp/ ; docker exec -t !{postgresDocker} psql -p !{postgresxl_Port_datastore} -U "${DG_POSTGRESQL_AGENT_USER:-dg-agent}" -d ${DG_POSTGRESQL_AGENT_DATABASE:-governance} -f /tmp/createTable.sql | grep "CREATE TABLE" | wc -l' in the ssh connection
    Then the command output contains '200'

  @runOnEnv(POSTGRES_AGENT_XL=true)
  Scenario:[18] Check 100 tables created in Postgres XL
    Given I open a ssh connection to '!{pgIP}' with user '${CLI_USER:-root}' and password '${CLI_PASSWORD:-stratio}'
    And I outbound copy 'src/test/resources/postgres/createTable.sql' through a ssh connection to '/tmp'
    When I run 'docker cp /tmp/createTable.sql !{postgresDocker}:/tmp/ ; docker exec -t !{postgresDocker} psql -p !{postgresxl_Port_datastore} -U "${DG_POSTGRESQL_AGENT_USER:-dg-agent}" -d ${DG_POSTGRESQL_AGENT_DATABASE:-governance} -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public'"' in the ssh connection
    Then the command output contains '100'
    When I run 'docker exec -t !{postgresDocker} psql -p !{postgresxl_Port_datastore} -U "${DG_POSTGRESQL_AGENT_USER:-dg-agent}" -d ${DG_POSTGRESQL_AGENT_DATABASE:-governance} -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='governance'"' in the ssh connection
    Then the command output contains '100'