@rest
Feature: [SPARTA-2811] Lineage Validation
  Scenario: [SPARTA-2811][01 -PRE] Get MarathonLB DNS
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    And I run 'dcos marathon task list | grep marathon.*lb.* | awk '{print $4}'' in the ssh connection and save the value in environment variable 'marathonIP'
    And I wait '2' seconds
    When  I run 'echo !{marathonIP}' in the ssh connection
    And I open a ssh connection to '!{marathonIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    And I run 'hostname | sed -e 's|\..*||'' in the ssh connection with exit status '0' and save the value in environment variable 'MarathonLbDns'

  @skipOnEnv(SKIP_POLICY_GOV=true)
  Scenario: [SPARTA-2811][01] Add postgres policy for authorization in Governance database
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/governance_policy.json' as 'json' with:
      | $.id       | UPDATE | ${ID_SPARTA_POSTGRES_GOV:-sparta-gov} | n/a |
      | $.name     | UPDATE | ${ID_SPARTA_POSTGRES_GOV:-sparta-gov} | n/a |
      | $.users[0] | UPDATE | ${DCOS_SERVICE_NAME}             | n/a |
      | $.services[0].instancesAcl[0].instances[0].name | UPDATE | ${POSTGRES_NAME}  | n/a |
    Then the service response status must be '201'

  Scenario:[SPARTA-2811][02] Obtain postgres docker
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When in less than '600' seconds, checking each '20' seconds, I send a 'GET' request to '/exhibitor/exhibitor/v1/explorer/node-data?key=%2Fdatastore%2Fcommunity%2F${POSTGRES_NAME:-postgrestls}%2Fplan-v2-json&_=' so that the response contains 'str'
    Then I send a 'GET' request to '/exhibitor/exhibitor/v1/explorer/node-data?key=%2Fdatastore%2Fcommunity%2F${POSTGRES_NAME}%2Fplan-v2-json&_='
    And I save element '$.str' in environment variable 'exhibitor_answer'
    And I save ''!{exhibitor_answer}'' in variable 'parsed_answer'
    And I run 'echo !{parsed_answer} | jq '.phases[0]' | jq '."0001".steps[0]'| jq '."0"'.agent_hostname | sed 's/^.\|.$//g'' in the ssh connection with exit status '0' and save the value in environment variable 'pgIP'
    Then I wait '4' seconds
    And I run 'echo !{parsed_answer} | jq '.phases[0]' | jq '."0001".steps[0]'| jq '."0"'.container_hostname | sed 's/^.\|.$//g'' in the ssh connection with exit status '0' and save the value in environment variable 'pgIPCalico'
    Then I wait '4' seconds
    And I run 'echo !{pgIPCalico}' locally
    Given I open a ssh connection to '!{pgIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    When I run 'sudo docker ps -q | sudo  xargs -n 1 docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}} {{ .Name }}' | sed 's/ \// /'| grep '!{pgIPCalico} ' | awk '{print $2}'' in the ssh connection and save the value in environment variable 'postgresDocker'
    And I wait '10' seconds


  #***********************************************************
  # INSTALL AND EXECUTE Generali- Batch Mode                 *
  #***********************************************************
  @web
  Scenario:[SPARTA-2811][03] Install lineage workflow - test (json) -Postgres/HDFS
    #Login into the platform
    Given My app is running in '!{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I securely browse to '/${DCOS_SERVICE_NAME}'
    And I wait '2' seconds
    Given My app is running in '!{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I securely browse to '/${DCOS_SERVICE_NAME}'
    And I wait '1' seconds
    And '1' elements exists with 'xpath://*[@id="username"]'
    And I type '${SPARTA-USER:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'xpath://*[@id="password"]'
    And I type '${SPARTA-PASSWORD:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'id:login-button'
    And I click on the element on index '0'
    And I wait '1' seconds
    Then I save selenium cookies in context
    #include workflow
    When I securely send requests to '!{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    Then I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflows' based on 'schemas/workflows/lineage-workflow.json' as 'json' with:
      |$.pipelineGraph.nodes[1].configuration.url|  UPDATE  | jdbc:postgresql://${POSTGRES_HOST:-pg-0001.postgrestls.mesos}:5432/governance?user=${DCOS_SERVICE_NAME}   | n/a |
      |$.pipelineGraph.nodes[2].writer.tableName|  UPDATE  | ${DATATABLE_NAME_JSON_PATH:-governance.automatic_idtable}   | n/a |
      |$.pipelineGraph.nodes[3].writer.tableName|  UPDATE  | ${DATATABLE_NAME_JSON:-governance.automatic_details}   | n/a |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '5' seconds


  @web
  Scenario:[SPARTA-2811][04] Execute lineage-workflow workflow
    #Login into the platform
    Given My app is running in '!{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I securely browse to '/${DCOS_SERVICE_NAME}'
    And I wait '2' seconds
    Given My app is running in '!{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I securely browse to '/${DCOS_SERVICE_NAME}'
    And I wait '1' seconds
    And '1' elements exists with 'xpath://*[@id="username"]'
    And I type '${SPARTA-USER:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'xpath://*[@id="password"]'
    And I type '${SPARTA-PASSWORD:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'id:login-button'
    And I click on the element on index '0'
    And I wait '1' seconds
    Then I save selenium cookies in context
    When I securely send requests to '!{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    Then I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflows/run/!{previousWorkflowID}'
    And the service response status must be '200'
    And I save element '$' in environment variable 'previousWorkflowID_execution'
    And I wait '5' seconds

#  #********************************
#  # VERIFY lineage-workflow*
#  #********************************

  @runOnEnv(TESTS_EXECUTION_WORKFLOW=true)
  Scenario:[SPARTA-2811][05] Test stop batch lineage workflow at the end of batch
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    #Get ip in marathon
    Then in less than '200' seconds, checking each '2' seconds, the command output 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/lineage-workflow/lineage-workflow-v1/!{previousWorkflowID_execution}  | awk '{print $5}' | grep lineage | wc -l' contains '1'
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/lineage-workflow/lineage-workflow-v1/!{previousWorkflowID_execution}  | awk '{print $5}' | grep lineage ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    Given in less than '600' seconds, checking each '20' seconds, the command output 'dcos task | grep -w lineage' contains '!{workflowTaskId}'
    And I wait '2' seconds
    #Wait for stop Batch mode process when finish task
    Given in less than '400' seconds, checking each '10' seconds, the command output 'dcos task | grep !{workflowTaskId} | wc -l' contains '0'

  @runOnEnv(TESTS_EXECUTION_WORKFLOW=true)
  Scenario:[SPARTA-2811][06] Test stop batch workflow at the end of batch
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    # Wait for stop Batch mode process when finish task
    Given in less than '900' seconds, checking each '10' seconds, the command output 'dcos task | grep !{workflowTaskId} | wc -l' contains '0'

#    **************************
#     TEST RESULT IN POSTGRES *
#    **************************
  Scenario:[SPARTA-2811][07] Generali batch Result in postgres
    Given I open a ssh connection to '!{pgIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    When in less than '600' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresDocker} psql -p 5432 -U postgres -d governance -c "select count(*) as total  from governance.automatic_details"' contains '${NUMBER_AUTOMATIC_DETAIL:-100}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresDocker} psql -p 5432 -U postgres -d governance -c "select count(*) as total  from governance.automatic_idtable"' contains '${NUMBER_AUTOMATIC_IDTABLE:-100}'

  Scenario:[SPARTA-2811][08] Obtain postgres governance docker
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When in less than '600' seconds, checking each '20' seconds, I send a 'GET' request to '/exhibitor/exhibitor/v1/explorer/node-data?key=%2Fdatastore%2Fcommunity%2F${POSTGRES_GOVERNANCE_NAME:-postgreseos}%2Fplan-v2-json&_=' so that the response contains 'str'
    Then I send a 'GET' request to '/exhibitor/exhibitor/v1/explorer/node-data?key=%2Fdatastore%2Fcommunity%2F${POSTGRES_GOVERNANCE_NAME:-postgreseos}%2Fplan-v2-json&_='
    And I save element '$.str' in environment variable 'exhibitor_answer'
    And I save ''!{exhibitor_answer}'' in variable 'parsed_answer'
    And I run 'echo !{parsed_answer} | jq '.phases[0]' | jq '."0001".steps[0]'| jq '."0"'.agent_hostname | sed 's/^.\|.$//g'' in the ssh connection with exit status '0' and save the value in environment variable 'pgIP_governance'
    Then I wait '4' seconds
    And I run 'echo !{parsed_answer} | jq '.phases[0]' | jq '."0001".steps[0]'| jq '."0"'.container_hostname | sed 's/^.\|.$//g'' in the ssh connection with exit status '0' and save the value in environment variable 'pgIPCalico_governance'
    Then I wait '4' seconds
    Given I open a ssh connection to '!{pgIP_governance}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    When I run 'sudo docker ps -q | sudo xargs -n 1 docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}} {{ .Name }}' | sed 's/ \// /'| grep '!{pgIPCalico_governance} ' | awk '{print $2}'' in the ssh connection and save the value in environment variable 'postgresGovernanceDocker'
    And I run 'echo !{postgresGovernanceDocker}' locally
    And I wait '10' seconds

  @skipOnEnv(SKIP_HDFS_AGENT=true)
  Scenario:[SPARTA-2811][09] Check governance results in tables in Postgres Agent
    Given I open a ssh connection to '!{pgIP_governance}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    #When in less than '100' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresGovernanceDocker} psql -p 5432 -U postgres -d ${POSTGRES_GOVERNANCE_NAME:-postgreseos} -c "select * from dg_metadata.actor where name='${NAME_WORKFLOW:-lineage-workflow}' and status_code='FINISHED' and job_type='BATCH' and version='${STRATIO_SPARTA_VERSION:-2.6.0-65e4295}' limit 1"' contains '${NAME_WORKFLOW:-lineage-workflow}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresGovernanceDocker} psql -p 5432 -U postgres -d ${POSTGRES_GOVERNANCE_NAME:-postgreseos} -c "select * from dg_metadata.actor where name='${NAME_WORKFLOW:-lineage-workflow}' and status_code='FINISHED' and job_type='BATCH' limit 1"' contains '${NAME_WORKFLOW:-lineage-workflow}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresGovernanceDocker} psql -p 5432 -U postgres -d ${POSTGRES_GOVERNANCE_NAME:-postgreseos} -c "select * from dg_metadata.actor_data_asset where metadata_path like '%automatic_details%' and data_store_type='SQL' limit 1"' contains 'SQL'
    When in less than '100' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresGovernanceDocker} psql -p 5432 -U postgres -d ${POSTGRES_GOVERNANCE_NAME:-postgreseos} -c "select * from dg_metadata.actor_data_asset where metadata_path like '%automatic_idtable%' and data_store_type='SQL'limit 1"' contains 'SQL'
    When in less than '100' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresGovernanceDocker} psql -p 5432 -U postgres -d ${POSTGRES_GOVERNANCE_NAME:-postgreseos} -c "select * from dg_metadata.actor_data_asset where metadata_path like '%automatic_idtable%' and data_store_type='SQL'limit 1"' contains 'SQL'
    #  When I run 'sudo docker exec -t !{postgresGovernanceDocker} psql -p 5432 -d ${POSTGRES_GOVERNANCE_NAME:-postgreseos} -U postgres -c "select id from dg_metadata.data_asset where metadata_path like '%automatic_details%' limit 1" | tail -3 | head -1' in the ssh connection and save the value in environment variable 'resultgovernance'
    #  And I run 'echo !{resultgovernance}' locally and save the value in environment variable 'idgovernance'

  Scenario:[SPARTA-1890][10] Check governance results in tables in HDFS Agent
    Given I open a ssh connection to '!{pgIP_governance}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    #When in less than '100' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresGovernanceDocker} psql -p 5432 -U postgres -d ${POSTGRES_GOVERNANCE_NAME:-postgreseos} -c "select * from dg_metadata.actor where name='${NAME_WORKFLOW:-lineage-workflow}' and status_code='FINISHED' and job_type='BATCH' and version='${STRATIO_SPARTA_VERSION:-2.6.0-65e4295}' limit 1"' contains '${NAME_WORKFLOW:-lineage-workflow}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresGovernanceDocker} psql -p 5432 -U postgres -d ${POSTGRES_GOVERNANCE_NAME:-postgreseos} -c "select * from dg_metadata.actor where name='${NAME_WORKFLOW:-lineage-workflow}' and status_code='FINISHED' and job_type='BATCH' limit 1"' contains '${NAME_WORKFLOW:-lineage-workflow}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresGovernanceDocker} psql -p 5432 -U postgres -d ${POSTGRES_GOVERNANCE_NAME:-postgreseos} -c "select * from dg_metadata.actor_data_asset where metadata_path like '%automatic_details%' and data_store_type='HDFS' limit 1"' contains 'HDFS'
    When in less than '100' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresGovernanceDocker} psql -p 5432 -U postgres -d ${POSTGRES_GOVERNANCE_NAME:-postgreseos} -c "select * from dg_metadata.actor_data_asset where metadata_path like '%automatic_idtable%' and data_store_type='HDFS' limit 1"' contains 'HDFS'

  @web
  Scenario:[SPARTA-2811][11] Lineage evidences Batch
  #Login into the platform
    Given My app is running in '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I securely browse to '/service/governance-ui/datastores'
    And I wait '2' seconds
    Given My app is running in '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I securely browse to '/service/governance-ui/datastores  '
    And I wait '1' seconds
    And '1' elements exists with 'xpath://*[@id="username"]'
    And I type '${GOV-USER:-admin}' on the element on index '0'
    And '1' elements exists with 'xpath://*[@id="password"]'
    And I type '${GOV-PASSWORD:-1234}' on the element on index '0'
    And '1' elements exists with 'id:login-button'
    And I click on the element on index '0'
    And I wait '6' seconds
    Then I save selenium cookies in context
    #Evidences
    #Then I securely browse to '/service/governance-ui/datastores/${POSTGRES_NAME:-postgrestls}/lineage?assetId=!{idgovernance}&assetMetadataPath=postgrestls:%2F%2Fgovernance>%2F:governance.automatic_details:'
    Then I securely browse to '/service/governance-ui/datastores/postgrestls/lineage?assetId=618&assetMetadataPath=postgrestls:%2F%2Fgovernance>%2F:governance.automatic_details:'
    And I wait '02' seconds
    Then I take a snapshot
    And this text exists 'lineage-workflow'


  @skipOnEnv(SKIP_PURGE_DATA=true)
  Scenario:[SPARTA-2811][12] delete user and table in postgres
      #Delete postgres table
    Given I open a ssh connection to '!{pgIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    When I run 'sudo docker exec -t !{postgresDocker} psql -p 5432 -d governance -U postgres -c "drop table governance.automatic_details"' in the ssh connection
    And I run 'sudo docker exec -t !{postgresDocker} psql -p 5432 -d governance -U postgres -c "drop table governance.automatic_idtable"' in the ssh connection

  @skipOnEnv(SKIP_POLICY_POSTGRES_GOV)
  Scenario: [SPARTA-2811][13] Delete user of Governance Policy
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${DCOS_USER:-admin}' and password '${DCOS_PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I send a 'PUT' request to '/service/gosecmanagement/api/policy/${ID_SPARTA_POSTGRES_GOV:-sparta-gov}' based on 'schemas/gosec/sparta_policy.json' as 'json' with:
      | $.id       | UPDATE | ${ID_SPARTA_POSTGRES_GOV:-sparta-gov} | n/a |
      | $.name     | UPDATE | ${ID_SPARTA_POSTGRES_GOV:-sparta-gov} | n/a |
      | $.users | UPDATE  | []                               | n/a    |
    Then the service response status must be '200'
    And I wait '10' seconds

  @skipOnEnv(SKIP_POLICY_POSTGRES_GOV)
  Scenario: [SPARTA-2811][14] Delete Governance Policy
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I send a 'DELETE' request to '/service/gosecmanagement/api/policy/${ID_SPARTA_POSTGRES_GOV:-sparta-gov}'
    Then the service response status must be '200'

  @skipOnEnv(SKIP_PURGE_DATA=true)
  @web
  Scenario: [SPARTA-2811][15] Remove lineage workflow
    #Get cookie from app
    Given My app is running in '!{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I securely browse to '/${DCOS_SERVICE_NAME}'
    And I wait '2' seconds
    Given My app is running in '!{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I securely browse to '/${DCOS_SERVICE_NAME}'
    And I wait '1' seconds
    And '1' elements exists with 'xpath://*[@id="username"]'
    And I type '${SPARTA-USER:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'xpath://*[@id="password"]'
    And I type '${SPARTA-PASSWORD:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'id:login-button'
    And I click on the element on index '0'
    And I wait '1' seconds
    Then I save selenium cookies in context
    Given I securely send requests to '!{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I send a 'DELETE' request to '/${DCOS_SERVICE_NAME}/workflows/!{previousWorkflowID}'
    Then the service response status must be '200'

  Scenario: [SPARTA-2811][15] Remove lineage datas in governance
    Given I open a ssh connection to '!{pgIP_governance}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    When I run 'sudo docker exec -t !{postgresGovernanceDocker} psql -p 5432 -d ${POSTGRES_GOVERNANCE_NAME:-postgreseos} -U postgres -c "TRUNCATE dg_metadata.actor CASCADE"' in the ssh connection
    And I run 'sudo docker exec -t !{postgresGovernanceDocker} psql -p 5432 -d ${POSTGRES_GOVERNANCE_NAME:-postgreseos} -U postgres -c "TRUNCATE dg_metadata.actor_data_asset CASCADE"' in the ssh connection
