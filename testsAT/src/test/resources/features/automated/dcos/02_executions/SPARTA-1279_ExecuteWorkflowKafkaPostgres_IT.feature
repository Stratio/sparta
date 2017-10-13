@rest @web
Feature: [SPARTA-1279][DCOS]Generate and Execute Workflow Kafka Postgres and see Streaming
  Background: conect to navigator
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user 'admin' and password '1234'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    When I run 'dcos task | grep pg_0001 | awk '{print $2}'' in the ssh connection and save the value in environment variable 'pgIP'
    Then I wait '5' seconds

  Scenario: [SPARTA-1279][03]Add sparta policy to write in kafka
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/kafka_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | kafka-sparta        | n/a |
      |   $.name                  |  UPDATE    | kafka-sparta        | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}      | n/a |
    Then the service response status must be '201'

  Scenario: Obtain postgres docker
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    Then I run 'docker ps | grep postgresql-community | awk '{print $1}'' in the ssh connection and save the value in environment variable 'postgresDocker'
    And I wait '5' seconds

  Scenario: Create sparta user in postgres
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    When I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "create user ${DCOS_SERVICE_NAME} with password ''"' in the ssh connection
    Then the command output contains 'CREATE ROLE'

  Scenario:[SPARTA_1196][01]Install and execute workflow
    #include workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/policy' based on 'schemas/workflows/kafka-postgres.json' as 'json' with:
      | id | DELETE | N/A  |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '1' seconds
    #Execute workflow
    Given I send a 'GET' request to '/service/${DCOS_SERVICE_NAME}/policy/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text '{"message":"Launched policy with name !{nameWorkflow}'
    #verify the generation of  workflow in dcos
  Scenario:[SPARTA_1196][02]Test workflow in Dcos
    Given in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep -w kafka-postgres | wc -l' contains '1'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/kafka-postgres  | awk '{print $5}' | grep kafka-postgres ' in the ssh connection and save the value in environment variable 'workflowTaskId'
    And I wait '1' seconds
    #Check workflow is runing in DCOS
    And  I run 'echo !{workflowTaskId}' in the ssh connection
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId} | grep TASK_RUNNING | wc -l' contains '1'

  Scenario:[SPARTA_1196][03] Generate report of Spark Streaming
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/kafka-postgres | awk '{print $2}'' contains 'True'
    #Now we give time to generate streaming process
    Then I wait '30' seconds
        #Get PORT and IP of workflow
    And I run 'dcos marathon app show /sparta/${DCOS_SERVICE_NAME}/workflows/kafka-postgres |jq '.tasks[0].ports' |sed -n 2p | sed 's/ //g'' in the ssh connection and save the value in environment variable 'workflowPORT'
    And I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/kafka-postgres | awk '{print $4}'| awk 'NR ==2'' in the ssh connection and save the value in environment variable 'workflowIP'
    #show streaming workflow
    Given My app is running in '!{workflowIP}:!{workflowPORT}'
    When I browse to '/streaming'
    #Check streaming process
    Then '1' element exists with 'id:completed'
    #Take evidence of streaming
    And I take a snapshot

  @ignore @manual
  Scenario: TestResults Postgres
    #Find Aplication ip
    Given I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/${DCOS_SERVICE_NAME} | awk '{print $4}'| awk 'NR ==2'' in the ssh connection and save the value in environment variable 'spartaIP'
    Then  I run 'echo !{spartaIP}' in the ssh connection
    #Run sparta'
    Given I securely send requests to '!{spartaIP}:10148'
    #Generate a table to connect to Postgres using crossdata ${TABLEPOSTGRES}-> triggertickets
    Given I send a 'POST' request to '/crossdata/queries/{"query": "CREATE TEMPORARY TABLE blacklist USING org.apache.spark.sql.jdbc OPTIONS (url 'jdbc:postgresql://${POSTGRESIP}:${POSTGRESPORT}/postgres?user=postgres&pass=postgres&connectTimeout=10', dbtable '${TABLEPOSTGRES}', driver 'org.postgresql.Driver')"}'
    Then the service response status must be '200'
    #Generate a table to check the total number of register
    Given I send a 'POST' request to '/crossdata/queries/{"query": "select count(*) as total from blacklist"}'
    Then the service response status must be '200' and its response must contain the text '{""total": ${TOTALDATA}'
    Given I send a 'POST' request to '/crossdata/queries' based on 'schemas/queries/querycrossdata.conf' as 'string' with:
          | $.tablepostgres | UPDATE | ${TABLEPOSTGRES}  |
          | $.postgresip  | UPDATE | ${POSTGRESIP}  |
          | $.postgresport  | UPDATE | ${POSTGRESPORT}  |
    #Generate a table to connect to Postgres using crossdata
    Then I send a 'POST' request to '/crossdata/queries' based on 'schemas/queries/searchdatapostgres.conf'
