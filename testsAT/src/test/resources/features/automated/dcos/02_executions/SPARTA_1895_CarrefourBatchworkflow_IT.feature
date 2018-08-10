@rest
Feature: [SPARTA-1895] E2E Execution of Carrefour Workflow -Batch mode
  Background: : conect to navigator
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    And I wait '3' seconds

  Scenario: [SPARTA-1895][01]Add postgres policy to write in postgres
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/postgres_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_SPARTA_POSTGRES}     | n/a |
      |   $.name                  |  UPDATE    | ${ID_SPARTA_POSTGRES}     | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}      | n/a |

  Scenario:[SPARTA-1895][02] Obtain postgres docker
    When in less than '600' seconds, checking each '20' seconds, I send a 'GET' request to '/exhibitor/exhibitor/v1/explorer/node-data?key=%2Fdatastore%2Fcommunity%2F${POSTGRES_NAME:-postgrestls}%2Fplan-v2-json&_=' so that the response contains 'str'
    Then I send a 'GET' request to '/exhibitor/exhibitor/v1/explorer/node-data?key=%2Fdatastore%2Fcommunity%2F${POSTGRES_NAME}%2Fplan-v2-json&_='
    And I save element '$.str' in environment variable 'exhibitor_answer'
    And I save ''!{exhibitor_answer}'' in variable 'parsed_answer'
    And I run 'echo !{parsed_answer} | jq '.phases[0]' | jq '."0001".steps[0]'| jq '."0"'.agent_hostname | sed 's/^.\|.$//g'' in the ssh connection with exit status '0' and save the value in environment variable 'pgIP'
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

  #******************************
  # ADD SPARTA USER IN POSTGRES *
  #******************************

  @runOnEnv(SINGLE_EXECUTION)
  Scenario:[SPARTA-1895][03] Create sparta user in postgres
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    When I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "create user \"${DCOS_SERVICE_NAME}\" with password ''"' in the ssh connection
    Then the command output contains 'CREATE ROLE'

  #***********************************************************
  # INSTALL AND EXECUTE Carrefour- Batch Mode                 *
  #***********************************************************
  Scenario:[SPARTA-1895][04] Install Carrefour workflow - Crossdata(csv)-Postgres
    #include workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows' based on 'schemas/workflows/batch-carrefour-workflow.json' as 'json' with:
      |$.pipelineGraph.nodes[2].configuration.url|  UPDATE  | jdbc:postgresql://${POSTGRES_INSTANCE}?user=${DCOS_SERVICE_NAME}   | n/a |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '10' seconds

  Scenario:[SPARTA-1279][05] Execute batch-carrefour-workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text 'OK'

  #********************************
  # VERIFY batch-carrefour-workflow WORKFLOW*
  #********************************

  Scenario:[SPARTA-1895][06] Test Runing batch-carrefour-workflow in Dcos
    Given in less than '600' seconds, checking each '20' seconds, the command output 'dcos task | grep -w batch-carrefour-workflow' contains 'batch-carrefour-workflow'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/batch-carrefour-workflow/batch-carrefour-workflow-v0  | awk '{print $5}' | grep batch-carrefour-workflow ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    And I wait '2' seconds
    #Check workflow is runing in DCOS
    When  I run 'echo !{workflowTaskId}' in the ssh connection
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep TASK_RUNNING' contains 'TASK_RUNNING'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep healthCheckResults' contains 'healthCheckResults'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep  '"alive": true'' contains '"alive": true'
    And I wait '30' seconds

  Scenario:[SPARTA-1895][07] Test stop batch carrefour workflow at the end of batch
    # Wait for stop Batch mode process when finish task
    Given in less than '800' seconds, checking each '10' seconds, the command output 'dcos task | grep !{workflowTaskId} | wc -l' contains '0'

  #**************************
  # TEST RESULT IN POSTGRES *
  #**************************

  Scenario:[SPARTA-1895][08] Test Postgres results of Carrefour Batch - Number of Elements per table
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "select count(*) as total  from tablavolcado"' contains '${TABLAVOLCADO:-14251}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "select count(*) as total  from fallo_refdate"' contains '${FALLO_REFDATE:-0}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "select count(*) as total  from fallo_rango"' contains '${FALLO_RANGO:-4212}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "select count(*) as total  from fallo_id_emprs"' contains '${FALLO_ID_EMPRS:-0}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "select count(*) as total  from fallo_duplicados"' contains '${FALLO_DUPLICADOS:-0}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "select count(*) as total  from fallo_dt_trusted"' contains '${FALLO_DT_TRUSTED:-0}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "select count(*) as total  from fallo_casting"' contains '${FALLO_CASTING:-0}'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "select count(*) as total  from error_tm_trusted"' contains '${ERROR_TM_TRUSTED:-0}'

  Scenario:[SPARTA-1895][09] Test Postgres results of Carrefour Batch - Number of Columns per table
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    When in less than '100' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "select count(*) from information_schema.columns where table_name = 'tablavolcado'"' contains '${numcolum:-246}'


  @runOnEnv(DELETE_POSTGRES_INFO)
  Scenario:[SPARTA-1279][08] delete user and table in postgres
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    When I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "drop table tablavolcado"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "drop table fallo_refdate"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "drop table fallo_rango"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "drop table fallo_id_emprs"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "drop table fallo_duplicados"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "drop table fallo_dt_trusted"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "drop table fallo_casting"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "drop table error_tm_trusted"' in the ssh connection
    Then I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "drop role \"${DCOS_SERVICE_NAME}\" "' in the ssh connection

  Scenario: [SPARTA-1277][10] Remove workflow
    Given I send a 'DELETE' request to '/service/${DCOS_SERVICE_NAME}/workflows/!{previousWorkflowID}'
    Then the service response status must be '200'

#MVN Example
# mvn verify -DCLUSTER_ID=nightly  -DDCOS_SERVICE_NAME=sparta-server -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.SPARTA_1895_CarrefourBatchworkflow_IT -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nightly.demo.stratio.com -DPOSTGRES_NAME=postgrestls -DPOSTGRES_INSTANCE=pg-0001.postgrestls.mesos:5432/postgres