@rest
Feature: [SPARTA-1890] E2E Execution of Generali Workflow -Batch mode
  Scenario:[QATM-1863][28 -PRE] Get Dns from marathonLB Generali
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    And I run 'dcos marathon task list | grep marathon.*lb.* | awk '{print $4}'' in the ssh connection and save the value in environment variable 'marathonIP'
    And I wait '1' seconds
    When  I run 'echo !{marathonIP}' in the ssh connection
    And I open a ssh connection to '!{marathonIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    Then I run 'hostname | sed -e 's|\..*||'' in the ssh connection with exit status '0' and save the value in environment variable 'MarathonLbDns'

  Scenario:[QATM-1863][28] Obtain postgres docker
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
    When I run 'sudo docker ps -q | sudo xargs -n 1 docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}} {{ .Name }}' | sed 's/ \// /'| grep '!{pgIPCalico} ' | awk '{print $2}'' in the ssh connection and save the value in environment variable 'postgresDocker'
    And I wait '10' seconds


  #***********************************************************
  # INSTALL AND EXECUTE Generali- Batch Mode                 *
  #***********************************************************
  @web
  Scenario:[QATM-1863][29] Install Generali workflow - HDFS (csv) -Postgres
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
    Then I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflows' based on 'schemas/workflows/batch-generali-workflow.json' as 'json' with:
      |$.pipelineGraph.nodes[13].configuration.url|  UPDATE  | jdbc:postgresql://${POSTGRES_HOST:-pg-0001.postgrestls.mesos}:5432/${POSTGRES_DATABASE:-sparta}?user=${DCOS_SERVICE_NAME}   | n/a |
      |$.pipelineGraph.nodes[19].configuration.url|  UPDATE  | jdbc:postgresql://${POSTGRES_HOST:-pg-0001.postgrestls.mesos}:5432/${POSTGRES_DATABASE:-sparta}?user=${DCOS_SERVICE_NAME}   | n/a |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '10' seconds


  @web
  Scenario:[QATM-1863][30] Execute batch-generali-workflow workflow
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

  #********************************
  # VERIFY batch-generali-workflow*
  #********************************

  Scenario:[QATM-1863][31] Test Runing batch-generali-workflow in Dcos
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Given in less than '600' seconds, checking each '20' seconds, the command output 'dcos task | grep -w batch-generali-workflow' contains 'batch-generali-workflow-v1'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/batch-generali-workflow/batch-generali-workflow-v1/!{previousWorkflowID_execution}  | awk '{print $5}' | grep batch-generali-workflow ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    And I wait '2' seconds
    #Check workflow is runing in DCOS
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep TASK_RUNNING' contains 'TASK_RUNNING'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep healthCheckResults' contains 'healthCheckResults'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep  '"alive": true'' contains '"alive": true'

  @web
  Scenario:[QATM-1863][32] Streaming evidences Generali Batch
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
    And I wait '6' seconds
    Then I save selenium cookies in context
    #Evidences
    Then I take a snapshot
    When I securely browse to '/workflows-${DCOS_SERVICE_NAME}/home/batch-generali-workflow/batch-generali-workflow-v1/!{previousWorkflowID_execution}/executors/'
    And I wait '05' seconds
    Then I take a snapshot
    When I securely browse to '/workflows-${DCOS_SERVICE_NAME}/home/batch-generali-workflow/batch-generali-workflow-v1/!{previousWorkflowID_execution}/jobs/'
    And I wait '05' seconds
    Then I take a snapshot

  Scenario:[SPARTA-1890][07] Test stop batch workflow at the end of batch
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    # Wait for stop Batch mode process when finish task
    Given in less than '900' seconds, checking each '10' seconds, the command output 'dcos task | grep !{workflowTaskId} | wc -l' contains '0'

  #**************************
  # TEST RESULT IN POSTGRES *
  #**************************
  Scenario:[SPARTA-1890][08] Generali batch Result in postgres
    Given I open a ssh connection to '!{pgIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresDocker} psql -p 5432 -U postgres -d ${POSTGRES_DATABASE:-sparta} -c "select count(*) as total  from \"${DCOS_SERVICE_NAME}\".cluster1"' contains '${CLUSTER1_NUMBER:-8824}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresDocker} psql -p 5432 -U postgres -d ${POSTGRES_DATABASE:-sparta} -c "select count(*) as total  from \"${DCOS_SERVICE_NAME}\".cluster2"' contains '${CLUSTER2_NUMBER:-15888}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresDocker} psql -p 5432 -U postgres -d ${POSTGRES_DATABASE:-sparta} -c "select count(*) as total  from \"${DCOS_SERVICE_NAME}\".cluster3"' contains '${CLUSTER3_NUMBER:-17661}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'sudo docker exec -t !{postgresDocker} psql -p 5432 -U postgres -d ${POSTGRES_DATABASE:-sparta} -c "select count(*) as total  from \"${DCOS_SERVICE_NAME}\".variables_calc"' contains '${VARIABLES_CAL:-43351}'


  @skipOnEnv(SKIP_PURGE_DATA=true)
  Scenario:[QATM-1863][33] delete user and table in postgres
    #Delete postgres table
    Given I open a ssh connection to '!{pgIP}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    When I run 'sudo docker exec -t !{postgresDocker} psql -p 5432 -d ${POSTGRES_DATABASE:-sparta} -U postgres -c "drop table \"${DCOS_SERVICE_NAME}\".cluster1"' in the ssh connection
    And I run 'sudo docker exec -t !{postgresDocker} psql -p 5432 -d ${POSTGRES_DATABASE:-sparta} -U postgres -c "drop table \"${DCOS_SERVICE_NAME}\".cluster2"' in the ssh connection
    And I run 'sudo docker exec -t !{postgresDocker} psql -p 5432 -d ${POSTGRES_DATABASE:-sparta} -U postgres -c "drop table \"${DCOS_SERVICE_NAME}\".cluster3"' in the ssh connection
    And I run 'sudo docker exec -t !{postgresDocker} psql -p 5432 -d ${POSTGRES_DATABASE:-sparta} -U postgres -c "drop table \"${DCOS_SERVICE_NAME}\".variables_calc"' in the ssh connection

  @skipOnEnv(SKIP_PURGE_DATA=true)
  @web
  Scenario: [QATM-1863][34] Remove workflow
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