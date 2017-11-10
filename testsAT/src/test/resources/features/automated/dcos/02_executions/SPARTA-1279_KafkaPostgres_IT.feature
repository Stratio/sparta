@rest
Feature: [SPARTA-1279] E2E Execution of Workflow Kafka Postgres
  Background: conect to navigator
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user 'admin' and password '1234'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    When I run 'dcos task | grep pg_0001 | awk '{print $2}'' in the ssh connection and save the value in environment variable 'pgIP'
    Then I wait '10' seconds

  #********************
  # ADD SPARTA POLICY *
  #********************
  Scenario:[SPARTA-1279][01] Add sparta policy to write in kafka
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/kafka_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | spartak                   | n/a |
      |   $.name                  |  UPDATE    | spartak                   | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}       | n/a |
    Then the service response status must be '201'

  #******************************
  # ADD SPARTA USER IN POSTGRES *
  #******************************
  Scenario:[SPARTA-1279][02] Obtain postgres docker
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    Then I run 'docker ps | grep postgresql-community | awk '{print $1}'' in the ssh connection and save the value in environment variable 'postgresDocker'
    And I wait '10' seconds

  Scenario:[SPARTA-1279][03] Create sparta user in postgres
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    When I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "create user \"${DCOS_SERVICE_NAME}\" with password ''"' in the ssh connection
   # Then the command output contains 'CREATE ROLE'

  #************************************************
  # INSTALL AND EXECUTE kafka to postgres WORKFLOW*
  #************************************************
  Scenario:[SPARTA-1279][07] Install kafka-postgres workflow
    #include workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/policy' based on 'schemas/workflows/kafka-postgres.json' as 'json' with:
      | id | DELETE | N/A  |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '10' seconds

  Scenario:[SPARTA-1279][08] Execute kafka-postgres workflow
    Given I send a 'GET' request to '/service/${DCOS_SERVICE_NAME}/policy/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text '{"message":"Launched policy with name !{nameWorkflow}'

  #********************************
  # VERIFY kafka-postgres WORKFLOW*
  #********************************
  Scenario:[SPARTA-1279][09] Test kafka-postgres workflow in Dcos
    Given in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep -w kafka-postgres | wc -l' contains '1'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/kafka-postgres  | awk '{print $5}' | grep kafka-postgres ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    And I wait '2' seconds
    #Check workflow is runing in DCOS
    When  I run 'echo !{workflowTaskId}' in the ssh connection
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep TASK_RUNNING | wc -l' contains '1'
    And in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep healthCheckResults | wc -l' contains '1'
    And in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep  '"alive": true' | wc -l' contains '1'
    #Its necesary to wait to execute another workflow
    And I wait '30' seconds

  #**************************************************
  # INSTALLL AND EXECUTE testInput to kafka WORKFLOW*
  #**************************************************
  Scenario:[SPARTA-1279][04] Install kafka-postgres workflow
    #include workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/policy' based on 'schemas/workflows/testinput-kafka.json' as 'json' with:
      | id | DELETE | N/A|
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '10' seconds

  Scenario:[SPARTA-1279][05] Execute kafka-postgres workflow
    Given I send a 'GET' request to '/service/${DCOS_SERVICE_NAME}/policy/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text '{"message":"Launched policy with name !{nameWorkflow}'

  #*********************************
  # VERIFY testInput-Kafka WORKFLOW*
  #*********************************
  Scenario:[SPARTA-1279][06] Test testinput-kafka workflow in Dcos
    Given in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep -w testinput-kafka | wc -l' contains '1'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/testinput-kafka  | awk '{print $5}' | grep testinput-kafka ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    And I wait '2' seconds
    #Check workflow is runing in DCOS
    When  I run 'echo !{workflowTaskId}' in the ssh connection
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep TASK_RUNNING | wc -l' contains '1'
    And in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep healthCheckResults | wc -l' contains '1'
    And in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep  '"alive": true' | wc -l' contains '1'
    #Its necesary to wait to test results in Postgres
    And I wait '60' seconds

  #**************************
  # TEST RESULT IN POSTGRES *
  #**************************
  Scenario:[SPARTA-1279][10] TestResult in postgres
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    Then in less than '300' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "select count(*) as total  from tabletest"' contains '400'
    When I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "select count(*) as total  from tabletest"' in the ssh connection and save the value in environment variable 'postgresresult'


  @ignore @manual
  Scenario:[SPARTA-1279][11] delete user and table in postgres
    #Delete postgres table
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    When I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "drop table tabletest"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "drop table cube1"' in the ssh connection
    Then I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "drop role \"${DCOS_SERVICE_NAME}\" "' in the ssh connection

