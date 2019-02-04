@rest
Feature: [QATM-1863] E2E Execution of Workflow Kafka Postgres x Elements with all security enabled
  Background: Connect to Dcos-cli
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Then I run 'dcos task ${MARATHON_LB_TASK:-marathon-lb-sec} | awk '{print $2}'| tail -n 1' in the ssh connection and save the value in environment variable 'marathonIP'
    And I wait '1' seconds
    When  I run 'echo !{marathonIP}' in the ssh connection
    And I open a ssh connection to '!{marathonIP}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    And I run 'hostname | sed -e 's|\..*||'' in the ssh connection with exit status '0' and save the value in environment variable 'MarathonLbDns'

  #********************
  # ADD KAFKA POLICY  *
  #********************
  @skipOnEnv(SKIP_KAFKA_POLICY)
  Scenario:[QATM-1863][13] Add kafka policy to write in kafka
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    And I wait '3' seconds
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/kafka_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | kafka_${DCOS_SERVICE_NAME}     | n/a |
      |   $.name                  |  UPDATE    | ${DCOS_SERVICE_NAME}_kf     | n/a |
      |   $.users[0]              |  UPDATE    | ${SPARTA-USER:-sparta-server}  | n/a |
    Then the service response status must be '201'


  Scenario: [QATM-1863][14] - Create topic for sparta in kafka
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I send a 'PUT' request to '/service/${KAFKA_NAME:-eos-kafka-framework}/v1/topics/${TOPIC:-idtopic}?partitions=${KAFKA_PARTITION:-1}&replication=${KAFKA_REPLICATION:-1}'
    Then the service response status must be '200'


  #************************************************
  # INSTALL AND EXECUTE kafka to postgres WORKFLOW*
  #************************************************
  @web
  Scenario:[QATM-1863][15] Install kafka-postgres workflow
    #Get cookie from app
    Given My app is running in '!{MarathonLbDns}.labs.stratio.com:443'
    When I securely browse to '/${DCOS_SERVICE_NAME}'
    And I wait '3' seconds
    And '1' elements exists with 'xpath://*[@id="username"]'
    And I type '${SPARTA-USER:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'xpath://*[@id="password"]'
    And I type '${SPARTA-PASSWORD:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'id:login-button'
    And I click on the element on index '0'
    And I wait '1' seconds
    Then I save selenium cookies in context
    When I securely send requests to '!{MarathonLbDns}.labs.stratio.com:443'
    Then I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflows' based on 'schemas/workflows/kafka-postgres.json' as 'json' with:
    |$.pipelineGraph.nodes[2].configuration.url|  UPDATE  | jdbc:postgresql://${POSTGRES_HOST:-pg-0001.postgrestls.mesos}:5432/${POSTGRES_DATABASE:-sparta}?user=${DCOS_SERVICE_NAME}   | n/a |
    #|$.pipelineGraph.nodes.configuration.bootstrap.servers.port |  UPDATE  | 9093 | n/a |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID1'
    And I save element '$.name' in environment variable 'nameWorkflow'
    Then the service response status must be '200'
    And I wait '10' seconds

  @web
  Scenario:[QATM-1863][16] Execute kafka-postgres workflow
    #Get cookie from app
    Given My app is running in '!{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I securely browse to '/${DCOS_SERVICE_NAME}'
    And I wait '3' seconds
    And '1' elements exists with 'xpath://*[@id="username"]'
    And I type '${SPARTA-USER:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'xpath://*[@id="password"]'
    And I type '${SPARTA-PASSWORD:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'id:login-button'
    And I click on the element on index '0'
    And I wait '1' seconds
    Then I save selenium cookies in context

    When I securely send requests to '!{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    Then I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflows/run/!{previousWorkflowID1}'
    And the service response status must be '200'
    And I save element '$' in environment variable 'previousWorkflow_execution_ID1'
    When  I run 'echo !{previousWorkflow_execution_ID1}' in the ssh connection

  #*********************************
  # VERIFY Kafka-Postgres WORKFLOW*
  #*********************************
  Scenario:[QATM-1863][17] Test kafka-postgres workflow in Dcos
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Given in less than '600' seconds, checking each '20' seconds, the command output 'dcos task | grep -w kafka-postgres' contains 'kafka-postgres-v0'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/kafka-postgres/kafka-postgres-v0/!{previousWorkflow_execution_ID1}  | awk '{print $5}' | grep kafka-postgres ' in the ssh connection and save the value in environment variable 'workflowTaskId1'
        #Check workflow is runing in DCOS
    When  I run 'echo !{workflowTaskId1}' in the ssh connection
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId1} | grep TASK_RUNNING' contains 'TASK_RUNNING'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId1} | grep healthCheckResults' contains 'healthCheckResults'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId1} | grep  '"alive": true'' contains '"alive": true'
    And I wait '10' seconds

  #**************************************************
  # INSTALLL AND EXECUTE testInput to kafka WORKFLOW*
  #**************************************************
  @web
  Scenario:[QATM-1863][18] Install testInput-Kafka workflow
    #Get cookie from app
    Given My app is running in '!{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I securely browse to '/${DCOS_SERVICE_NAME}'
    And I wait '3' seconds
    And '1' elements exists with 'xpath://*[@id="username"]'
    And I type '${SPARTA-USER:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'xpath://*[@id="password"]'
    And I type '${SPARTA-PASSWORD:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'id:login-button'
    And I click on the element on index '0'
    And I wait '1' seconds
    Then I save selenium cookies in context
    #include workflow
    When I securely send requests to '!{MarathonLbDns}.labs.stratio.com:443'
    Then I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflows' based on 'schemas/workflows/testinput-kafka.json' as 'json' with:
      | id | DELETE | N/A|
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID2'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '10' seconds

  @web
  Scenario:[QATM-1863][19] Execute testInput-Kafka workflow
    #Get cookie from app
    Given My app is running in '!{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I securely browse to '/${DCOS_SERVICE_NAME}'
    And I wait '3' seconds
    And '1' elements exists with 'xpath://*[@id="username"]'
    And I type '${SPARTA-USER:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'xpath://*[@id="password"]'
    And I type '${PASSWORD:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'id:login-button'
    And I click on the element on index '0'
    And I wait '1' seconds
    Then I save selenium cookies in context
    When I securely send requests to '!{MarathonLbDns}.labs.stratio.com:443'
    Given I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflows/run/!{previousWorkflowID2}'
    Then the service response status must be '200'
    And I save element '$' in environment variable 'previousWorkflow_execution_ID2'

  #*********************************
  # VERIFY testInput-Kafka WORKFLOW*
  #*********************************
  Scenario:[QATM-1863][20] Test kafka-postgres workflow in Dcos
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Given in less than '600' seconds, checking each '20' seconds, the command output 'dcos task | grep -w testinput-kafka' contains 'testinput-kafka-v0'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/testinput-kafka/testinput-kafka-v0/!{previousWorkflow_execution_ID2}  | awk '{print $5}' | grep testinput-kafka' in the ssh connection and save the value in environment variable 'workflowTaskId2'
    And I wait '2' seconds
    #Check workflow is runing in DCOS
    When  I run 'echo !{workflowTaskId2}' in the ssh connection
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId2} | grep TASK_RUNNING' contains 'TASK_RUNNING'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId2} | grep healthCheckResults' contains 'healthCheckResults'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId2} | grep  '"alive": true'' contains '"alive": true'
    #Its necesary to wait to test results in Postgres
    And I wait '15' seconds

  #****************************
  #   TEST RESULT IN POSTGRES *
  #****************************
  Scenario:[QATM-1863][21] Obtain postgres docker
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
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    When I run 'docker ps -q | xargs -n 1 docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}} {{ .Name }}' | sed 's/ \// /'| grep !{pgIPCalico} | awk '{print $2}'' in the ssh connection and save the value in environment variable 'postgresDocker'
    And I run 'echo !{postgresDocker}' locally
    And I wait '10' seconds
    And I run 'echo !{postgresDocker}' in the ssh connection with exit status '0'

  Scenario:[QATM-1863][22] TestResult in postgres
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -d ${POSTGRES_DATABASE:-sparta} -p 5432 -U postgres -c "select count(*) as total  from \"${DCOS_SERVICE_NAME}\".tabletest"' contains '${TABLETEST_NUMBER:-400}'
    When I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -d ${POSTGRES_DATABASE:-sparta} -c "select count(*) as total  from \"${DCOS_SERVICE_NAME}\".tabletest"' in the ssh connection and save the value in environment variable 'postgresresult'

  @web
  Scenario:[QATM-1863][23] Streaming evidences kafka-postgres
    #Get cookie from app
    Given My app is running in '!{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I securely browse to '/${DCOS_SERVICE_NAME}'
    And I wait '3' seconds
    And '1' elements exists with 'xpath://*[@id="username"]'
    And I type '${USER:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'xpath://*[@id="password"]'
    And I type '${PASSWORD:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'id:login-button'
    And I click on the element on index '0'
    And I wait '1' seconds
    Then I save selenium cookies in context
    When I securely browse to '/workflows-${DCOS_SERVICE_NAME}/home/kafka-postgres/kafka-postgres-v0/!{previousWorkflow_execution_ID1}/streaming/'
    Then I take a snapshot
    And this text exists '${TABLETEST_NUMBER:-400}'
    And I wait '02' seconds
    When I securely browse to '/workflows-${DCOS_SERVICE_NAME}/home/kafka-postgres/kafka-postgres-v0/!{previousWorkflow_execution_ID1}/executors/'
    And I wait '03' seconds

  @web
  Scenario: [QATM-1863][24] Stop Streaming workflows
    #Get cookie from app
    Given My app is running in '!{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I securely browse to '/${DCOS_SERVICE_NAME}'
    And I wait '3' seconds
    And '1' elements exists with 'xpath://*[@id="username"]'
    And I type '${SPARTA-USER:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'xpath://*[@id="password"]'
    And I type '${SPARTA-PASSWORD:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'id:login-button'
    And I click on the element on index '0'
    And I wait '1' seconds
    Then I save selenium cookies in context
    Given I securely send requests to '!{MarathonLbDns}.labs.stratio.com:443'
    Then I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflowExecutions/stop/!{previousWorkflow_execution_ID1}'
    And the service response status must be '200'
    Given I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflowExecutions/stop/!{previousWorkflow_execution_ID2}'
    Then the service response status must be '200'

  Scenario:[QATM-1863][25] Test stop streaming workflows
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    # Wait for stop Batch mode process when finish task
    Given in less than '800' seconds, checking each '10' seconds, the command output 'dcos task | grep !{workflowTaskId1} | wc -l' contains '0'
    Given in less than '800' seconds, checking each '10' seconds, the command output 'dcos task | grep !{workflowTaskId2} | wc -l' contains '0'

  @web
  Scenario: [QATM-1863][26] Remove Streaming workflows
    #Get cookie from app
    Given My app is running in '!{MarathonLbDns}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I securely browse to '/${DCOS_SERVICE_NAME}'
    And I wait '3' seconds
    And '1' elements exists with 'xpath://*[@id="username"]'
    And I type '${SPARTA-USER:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'xpath://*[@id="password"]'
    And I type '${SPARTA-PASSWORD:-sparta-server}' on the element on index '0'
    And '1' elements exists with 'id:login-button'
    And I click on the element on index '0'
    And I wait '1' seconds
    Then I save selenium cookies in context
    Given I securely send requests to '!{MarathonLbDns}.labs.stratio.com:443'
    When I send a 'DELETE' request to '/${DCOS_SERVICE_NAME}/workflows/!{previousWorkflowID1}'
    Then the service response status must be '200'
    Given I send a 'DELETE' request to '/${DCOS_SERVICE_NAME}/workflows/!{previousWorkflowID2}'
    Then the service response status must be '200'

  @skipOnEnv(SKIP_POLICY=true)
  Scenario: [QATM-1863][27] Delete Kafka Policy
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I send a 'DELETE' request to '/service/gosecmanagement/api/policy/kafka_${DCOS_SERVICE_NAME}'
    Then the service response status must be '200'

  Scenario:[QATM-1863][28] Delete tables in postgres
    #Delete postgres table
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    When I run 'docker exec -t !{postgresDocker} psql -p 5432 -d ${POSTGRES_DATABASE:-sparta} -U postgres -c "drop table \"${DCOS_SERVICE_NAME}\".tabletest"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -p 5432 -d ${POSTGRES_DATABASE:-sparta} -U postgres -c "drop table \"${DCOS_SERVICE_NAME}\".cube"' in the ssh connection
