@rest
Feature: [SPARTA-1279] E2E Execution of Workflow Kafka Postgres x Elements
  Background: : conect to navigator
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user 'admin' and password '1234'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    And I wait '3' seconds
  #********************
  # ADD SPARTA POLICY *
  #********************
  Scenario:[SPARTA-1279][01] Add sparta policy to write in kafka
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/kafka_policy_fr.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${DCOS_SERVICE_NAME}_kf     | n/a |
      |   $.name                  |  UPDATE    | ${DCOS_SERVICE_NAME}_kf     | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}        | n/a |
    Then the service response status must be '201'

  #******************************
  # ADD SPARTA USER IN POSTGRES *
  #******************************
  Scenario:[SPARTA-1279][02] Obtain postgres docker
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

  Scenario:[SPARTA-1279][03] Create sparta user in postgres
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    When I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "create user \"${DCOS_SERVICE_NAME}\" with password ''"' in the ssh connection
    Then the command output contains 'CREATE ROLE'

  #************************************************
  # INSTALL AND EXECUTE kafka to postgres WORKFLOW*
  #************************************************
  Scenario:[SPARTA-1279][04] Install kafka-postgres workflow
    #include workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows' based on 'schemas/workflows/kafka-postgres.json' as 'json' with:
    |$.pipelineGraph.nodes[2].configuration.url|  UPDATE  | jdbc:postgresql://${POSTGRES_INSTANCE}?user=${DCOS_SERVICE_NAME}   | n/a |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '10' seconds

  Scenario:[SPARTA-1279][05] Execute kafka-postgres workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text 'OK'

  #********************************
  # VERIFY kafka-postgres WORKFLOW*
  #********************************

  Scenario:[SPARTA-1279][06] Test kafka-postgres workflow in Dcos
    Given in less than '600' seconds, checking each '20' seconds, the command output 'dcos task | grep -w kafka-postgres' contains 'kafka-postgres'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/kafka-postgres/kafka-postgres-v0  | awk '{print $5}' | grep kafka-postgres ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    And I wait '2' seconds
    #Check workflow is runing in DCOS
    When  I run 'echo !{workflowTaskId}' in the ssh connection
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep TASK_RUNNING' contains 'TASK_RUNNING'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep healthCheckResults' contains 'healthCheckResults'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep  '"alive": true'' contains '"alive": true'
    #Its necesary to wait to execute another workflow
    And I wait '30' seconds

  #**************************************************
  # INSTALLL AND EXECUTE testInput to kafka WORKFLOW*
  #**************************************************
  Scenario:[SPARTA-1279][07] Install testInput-Kafka workflow
    #include workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows' based on 'schemas/workflows/testinput-kafka.json' as 'json' with:
      | id | DELETE | N/A|
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '10' seconds

  Scenario:[SPARTA-1279][08] Execute kafka-postgres workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text 'OK'

  #*********************************
  # VERIFY testInput-Kafka WORKFLOW*
  #*********************************
  Scenario:[SPARTA-1279][09] Test testinput-kafka workflow in Dcos
    Given in less than '600' seconds, checking each '20' seconds, the command output 'dcos task | grep -w testinput-kafka' contains 'testinput-kafka'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/testinput-kafka/testinput-kafka-v0  | awk '{print $5}' | grep testinput-kafka ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    And I wait '2' seconds
    #Check workflow is runing in DCOS
    When  I run 'echo !{workflowTaskId}' in the ssh connection
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep TASK_RUNNING' contains 'TASK_RUNNING'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep healthCheckResults' contains 'healthCheckResults'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep  '"alive": true'' contains '"alive": true'
    #Its necesary to wait to test results in Postgres
    And I wait '60' seconds

  #**************************
  # TEST RESULT IN POSTGRES *
  #**************************

  @ignore @tillfixed(SPARTA-1866)
  Scenario:[SPARTA-1279][10] TestResult in postgres
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "select count(*) as total  from tabletest"' contains '${TABLETEST_NUMBER:-400}'
    When I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "select count(*) as total  from tabletest"' in the ssh connection and save the value in environment variable 'postgresresult'


  @runOnEnv(DELETE_POSTGRES_INFO)
  Scenario:[SPARTA-1279][11] delete user and table in postgres
    #Delete postgres table
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    When I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "drop table tabletest"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "drop table cube1"' in the ssh connection

#MVN Example
# mvn verify -DCLUSTER_ID=megadev  -DDCOS_SERVICE_NAME=sparta-server -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.SPARTA_1279_KafkaPostgres_IT -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-megadev.demo.stratio.com