@rest
Feature: [SPARTA-1895] E2E Execution of Carrefour Workflow -Batch mode
  Background: conect to navigator
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Then I run 'dcos task ${MARATHON_LB_TASK:-marathon-lb-sec} | awk '{print $2}'| tail -n 1' in the ssh connection and save the value in environment variable 'marathonIP'
    And I wait '1' seconds
    When  I run 'echo !{marathonIP}' in the ssh connection
    And I open a ssh connection to '!{marathonIP}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    And I run 'hostname | sed -e 's|\..*||'' in the ssh connection with exit status '0' and save the value in environment variable 'MarathonLbDns'

  Scenario:[SPARTA-1279][09] Obtain postgres docker
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
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


  #***********************************************************
  # INSTALL AND EXECUTE Carrefour- Batch Mode                 *
  #***********************************************************
  @web
  Scenario:[SPARTA-1895][03] Install Carrefour workflow -HDFS (csv)-Postgres
    #include workflow
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
    #include workflow
    When I securely send requests to '!{MarathonLbDns}.labs.stratio.com:443'
    Then I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflows' based on 'schemas/workflows/batch-carrefour-workflow.json' as 'json' with:
      |$.pipelineGraph.nodes[2].configuration.url|  UPDATE  | jdbc:postgresql://${POSTGRES_INSTANCE}?user=${DCOS_SERVICE_NAME}   | n/a |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '10' seconds

  @web
  Scenario:[SPARTA-1279][04] Execute batch-carrefour-workflow
    #Login into the platform
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
    Then I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflows/run/!{previousWorkflowID}'
    And the service response status must be '200'
    And I save element '$' in environment variable 'previousWorkflowID'

  #*********************************
  # VERIFY batch-carrefour-workflow*
  #*********************************

  Scenario:[SPARTA-1895][05] Test Runing batch-carrefour-workflow in Dcos
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Given in less than '600' seconds, checking each '20' seconds, the command output 'dcos task | grep -w batch-carrefour-workflow' contains 'batch-carrefour-workflow'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/batch-carrefour-workflow/batch-carrefour-workflow-v0/!{previousWorkflowID}  | awk '{print $5}' | grep batch-carrefour-workflow ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    And I wait '2' seconds
    #Check workflow is runing in DCOS
    When  I run 'echo !{workflowTaskId}' in the ssh connection
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep TASK_RUNNING' contains 'TASK_RUNNING'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep healthCheckResults' contains 'healthCheckResults'
    #And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep  '"alive": true'' contains '"alive": true'
    And I wait '2' seconds

  @web
  Scenario:[SPARTA-1279][10] Spark evidences Generali Batch
    #Login into the platform
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
    #Evidences
    When I securely browse to '/workflows-${DCOS_SERVICE_NAME}/home/batch-carrefour-workflow/batch-carrefour-workflow-v0/'
    And I wait '02' seconds
    Then I take a snapshot
    When I securely browse to '/workflows-${DCOS_SERVICE_NAME}/home/batch-carrefour-workflow/batch-carrefour-workflow-v0/executors/'
    And I wait '02' seconds
    Then I take a snapshot
    When I securely browse to '/#/executions/edit/!{previousWorkflowID}'
    And I wait '02' seconds
    Then I take a snapshot

  Scenario:[SPARTA-1895][06] Test stop batch carrefour workflow at the end of batch
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    # Wait for stop Batch mode process when finish task
    Given in less than '800' seconds, checking each '10' seconds, the command output 'dcos task | grep !{workflowTaskId} | wc -l' contains '0'

  #**************************
  # TEST RESULT IN POSTGRES *
  #**************************

  Scenario:[SPARTA-1895][07] Test Postgres results of Carrefour Batch - Number of Elements per table
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -d ${POSTGRES_DATABASE:-sparta} -c "select count(*) as total  from \"${DCOS_SERVICE_NAME}\".tablavolcado"' contains '${TABLAVOLCADO:-7}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -d ${POSTGRES_DATABASE:-sparta} -c "select count(*) as total  from \"${DCOS_SERVICE_NAME}\".fallo_refdate"' contains '${FALLO_REFDATE:-0}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -d ${POSTGRES_DATABASE:-sparta} -c "select count(*) as total  from \"${DCOS_SERVICE_NAME}\".fallo_rango"' contains '${FALLO_RANGO:-0}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -d ${POSTGRES_DATABASE:-sparta} -c "select count(*) as total  from \"${DCOS_SERVICE_NAME}\".fallo_id_emprs"' contains '${FALLO_ID_EMPRS:-0}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -d ${POSTGRES_DATABASE:-sparta} -c "select count(*) as total  from \"${DCOS_SERVICE_NAME}\".fallo_duplicados"' contains '${FALLO_DUPLICADOS:-0}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -d ${POSTGRES_DATABASE:-sparta} -c "select count(*) as total  from \"${DCOS_SERVICE_NAME}\".fallo_dt_trusted"' contains '${FALLO_DT_TRUSTED:-0}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -d ${POSTGRES_DATABASE:-sparta} -c "select count(*) as total  from \"${DCOS_SERVICE_NAME}\".fallo_casting"' contains '${FALLO_CASTING:-0}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -d ${POSTGRES_DATABASE:-sparta} -c "select count(*) as total  from \"${DCOS_SERVICE_NAME}\".error_tm_trusted"' contains '${ERROR_TM_TRUSTED:-0}'

  Scenario:[SPARTA-1895][08] Test Postgres results of Carrefour Batch - Number of Columns per table
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "select count(*) from information_schema.columns where table_name = '\"${DCOS_SERVICE_NAME}\".tablavolcado'"' contains '${numcolum:-246}'

  @runOnEnv(PURGE_DATA=TRUE)
  Scenario:[SPARTA-1279][09] delete user and table in postgres
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    When I run 'docker exec -t !{postgresDocker} psql -d ${POSTGRES_DATABASE:-sparta} -p 5432 -U postgres -c "drop table \"${DCOS_SERVICE_NAME}\".tablavolcado"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -d ${POSTGRES_DATABASE:-sparta} -p 5432 -U postgres -c "drop table \"${DCOS_SERVICE_NAME}\".fallo_refdate"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -d ${POSTGRES_DATABASE:-sparta} -p 5432 -U postgres -c "drop table \"${DCOS_SERVICE_NAME}\".fallo_rango"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -d ${POSTGRES_DATABASE:-sparta} -p 5432 -U postgres -c "drop table \"${DCOS_SERVICE_NAME}\".fallo_id_emprs"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -d ${POSTGRES_DATABASE:-sparta} -p 5432 -U postgres -c "drop table \"${DCOS_SERVICE_NAME}\".fallo_duplicados"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -d ${POSTGRES_DATABASE:-sparta} -p 5432 -U postgres -c "drop table \"${DCOS_SERVICE_NAME}\".fallo_dt_trusted"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -d ${POSTGRES_DATABASE:-sparta} -p 5432 -U postgres -c "drop table \"${DCOS_SERVICE_NAME}\".fallo_casting"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -d ${POSTGRES_DATABASE:-sparta} -p 5432 -U postgres -c "drop table \"${DCOS_SERVICE_NAME}\".error_tm_trusted"' in the ssh connection

  @runOnEnv(PURGE_DATA=true)
  @web
  Scenario: [SPARTA-1279][11] Stop workflow
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
    Given I securely send requests to '!{MarathonLbDns}.labs.stratio.com:443'
    Then I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflows/stop/!{previousWorkflowID}'
    And the service response status must be '200'

  @runOnEnv(PURGE_DATA=true)
  @web
  Scenario: [SPARTA-1279][12] Remove workflow
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
    Given I securely send requests to '!{MarathonLbDns}.labs.stratio.com:443'
    When I send a 'DELETE' request to '/${DCOS_SERVICE_NAME}/workflows/!{previousWorkflowID}'
    Then the service response status must be '200'

  @runOnEnv(PURGE_DATA=true)
  Scenario: [SPARTA-1279][14]Delete Postgres Policy
    #Get cookie from app
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    When I send a 'DELETE' request to '/service/gosecmanagement/api/policy/${ID_SPARTA_POSTGRES:-sparta-pg}'
    Then the service response status must be '200'

#MVN Example
# mvn verify -DCLUSTER_ID=nightly  -DDCOS_SERVICE_NAME=sparta-server -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.SPARTA_1895_CarrefourBatchworkflow_IT -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nightly.demo.stratio.com -DPOSTGRES_NAME=postgrestls -DPOSTGRES_INSTANCE=pg-0001.postgrestls.mesos:5432/postgres