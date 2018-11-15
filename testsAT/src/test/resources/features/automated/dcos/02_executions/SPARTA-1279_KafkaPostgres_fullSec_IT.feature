@rest
Feature: [SPARTA-1279] E2E Execution of Workflow Kafka Postgres x Elements with all security enabled
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
  @skipOnEnv(SKIP_POLICY=true)
  Scenario:[SPARTA-1279][01] Add kafka policy to write in kafka
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    And I wait '3' seconds
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/kafka_policy_fr.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${DCOS_SERVICE_NAME}_kf     | n/a |
      |   $.name                  |  UPDATE    | ${DCOS_SERVICE_NAME}_kf     | n/a |
      |   $.users[0]              |  UPDATE    | ${SPARTA-USER:-sparta-server}  | n/a |
      |   $.services[0].version   |  UPDATE    | ${KF-GOSEC-VERSION:-2.2.0-59b63a7}     | n/a |
    Then the service response status must be '201'

  #************************************************
  # INSTALL AND EXECUTE kafka to postgres WORKFLOW*
  #************************************************
  @web
  Scenario:[SPARTA-1279][03] Install kafka-postgres workflow
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
    |$.pipelineGraph.nodes[2].configuration.url|  UPDATE  | jdbc:postgresql://${POSTGRES_INSTANCE:-pg-0001.postgrestls.mesos:5432/postgres}?user=${DCOS_SERVICE_NAME}   | n/a |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID1'
    And I save element '$.name' in environment variable 'nameWorkflow'
    Then the service response status must be '200'
    And I wait '10' seconds

  @web
  Scenario:[SPARTA-1279][04] Execute kafka-postgres workflow
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
    Then I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflows/run/!{previousWorkflowID1}'
    And the service response status must be '200'
    And I save element '$' in environment variable 'previousWorkflowID1'
    When  I run 'echo !{previousWorkflowID1}' in the ssh connection
  
  #*********************************
  # VERIFY Kafka-Postgres WORKFLOW*
  #*********************************
  Scenario:[SPARTA-1279][05] Test kafka-postgres workflow in Dcos
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Given in less than '600' seconds, checking each '20' seconds, the command output 'dcos task | grep -w kafka-postgres' contains 'kafka-postgres-v0'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/kafka-postgres/kafka-postgres-v0/!{previousWorkflowID1}  | awk '{print $5}' | grep kafka-postgres ' in the ssh connection and save the value in environment variable 'workflowTaskId1'
    And I wait '2' seconds
    #Check workflow is runing in DCOS
    When  I run 'echo !{workflowTaskId1}' in the ssh connection
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId1} | grep TASK_RUNNING' contains 'TASK_RUNNING'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId1} | grep healthCheckResults' contains 'healthCheckResults'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{workflowTaskId1} | grep  '"alive": true'' contains '"alive": true'


  #**************************************************
  # INSTALLL AND EXECUTE testInput to kafka WORKFLOW*
  #**************************************************
  @web
  Scenario:[SPARTA-1279][07] Install testInput-Kafka workflow
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
    #include workflow
    When I securely send requests to '!{MarathonLbDns}.labs.stratio.com:443'
    Then I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflows' based on 'schemas/workflows/testinput-kafka.json' as 'json' with:
      | id | DELETE | N/A|
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID2'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '10' seconds

  @web
  Scenario:[SPARTA-1279][08] Execute testInput-Kafka workflow
    #Get cookie from app
    Given My app is running in '!{MarathonLbDns}.labs.stratio.com:443'
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
    And I save element '$' in environment variable 'previousWorkflowID2'

  #*********************************
  # VERIFY testInput-Kafka WORKFLOW*
  #*********************************
  Scenario:[SPARTA-1279][09] Test kafka-postgres workflow in Dcos
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Given in less than '600' seconds, checking each '20' seconds, the command output 'dcos task | grep -w testinput-kafka' contains 'testinput-kafka-v0'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/workflows/home/testinput-kafka/testinput-kafka-v0/!{previousWorkflowID2}  | awk '{print $5}' | grep testinput-kafka' in the ssh connection and save the value in environment variable 'workflowTaskId2'
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

  Scenario:[SPARTA-1279][10] TestResult in postgres
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "select count(*) as total  from tabletest"' contains '${TABLETEST_NUMBER:-400}'
    When I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "select count(*) as total  from tabletest"' in the ssh connection and save the value in environment variable 'postgresresult'

  @web
  Scenario:[SPARTA-1279][10] Streaming evidences kafka-postgres
    #Get cookie from app
    Given My app is running in '!{MarathonLbDns}.labs.stratio.com:443'
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
    When I securely browse to '/workflows-${DCOS_SERVICE_NAME}/home/kafka-postgres/kafka-postgres-v0/streaming/'
    Then I take a snapshot
    And this text exists '${TABLETEST_NUMBER:-400}'
    And I wait '02' seconds
    When I securely browse to '/workflows-${DCOS_SERVICE_NAME}/home/kafka-postgres/kafka-postgres-v0/executors/'
    And I wait '03' seconds
    Then I take a snapshot
    When I securely browse to '/${DCOS_SERVICE_NAME}/#/wizard/edit/!{previousWorkflowID1}'
    And I wait '02' seconds
    Then I take a snapshot

  @runOnEnv(PURGE_DATA=true)
  @web
  Scenario: [SPARTA-1279][11] Stop workflows
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
    Then I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflows/stop/!{previousWorkflowID1}'
    And the service response status must be '200'
    Given I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflows/stop/!{previousWorkflowID2}'
    Then the service response status must be '200'

  Scenario:[SPARTA-1279][12] Test stop streaming workflows
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    # Wait for stop Batch mode process when finish task
    Given in less than '800' seconds, checking each '10' seconds, the command output 'dcos task | grep !{workflowTaskId1} | wc -l' contains '0'
    Given in less than '800' seconds, checking each '10' seconds, the command output 'dcos task | grep !{workflowTaskId2} | wc -l' contains '0'


  @runOnEnv(PURGE_DATA=true)
  @web
  Scenario: [SPARTA-1279][13] Remove workflows
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
    When I send a 'DELETE' request to '/${DCOS_SERVICE_NAME}/workflows/!{previousWorkflowID1}'
    Then the service response status must be '200'
    Given I send a 'DELETE' request to '/${DCOS_SERVICE_NAME}/workflows/!{previousWorkflowID2}'
    Then the service response status must be '200'

  @runOnEnv(PURGE_DATA=true)
  @skipOnEnv(SKIP_POLICY=true)
  Scenario: [SPARTA-1279][14]Delete Kafka Policy
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    When I send a 'DELETE' request to '/service/gosecmanagement/api/policy/${DCOS_SERVICE_NAME}_kf'
    Then the service response status must be '200'

  @runOnEnv(PURGE_DATA=true)
  @skipOnEnv(SKIP_POLICY=true)
  Scenario: [SPARTA-1279][15]Delete Postgres Policy
    #Get cookie from app
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    When I send a 'DELETE' request to '/service/gosecmanagement/api/policy/${ID_SPARTA_POSTGRES:-sparta-pg}'
    Then the service response status must be '200'
    And I wait '5' seconds

  @runOnEnv(PURGE_DATA=true)
  Scenario:[SPARTA-1279][16] delete user and table in postgres
    #Delete postgres table
    Given I open a ssh connection to '!{pgIP}' with user 'root' and password 'stratio'
    When I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "drop table tabletest"' in the ssh connection
    And I run 'docker exec -t !{postgresDocker} psql -p 5432 -U postgres -c "drop table cube"' in the ssh connection

#MVN Example
# mvn verify -DCLUSTER_ID=nightly -DDCOS_SERVICE_NAME=sparta-server -Dgroups=dcos_streaming -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nightly -DDOCKER_URL=qa.stratio.com/stratio/sparta -DCALICOENABLED=true -DHDFS_IP=10.200.0.74 -DROLE_SPARTA=open -DAUTH_ENABLED=false -DSTRATIO_SPARTA_VERSION=2.2.0 -DZK_URL=zk-0001.zkuserland.mesos:2181,zk-0002.zkuserland.mesos:2181,zk-0003.zkuserland.mesos:2181  -DCROSSDATA_SERVER_CONFIG_SPARK_IMAGE=qa.stratio.com/stratio/stratio-spark:2.2.0-1.0.0 -DHDFS_REALM=DEMO.STRATIO.COM -DNGINX_ACTIVE=false -DMASTERS_LIST=10.200.0.156 -DPRIVATE_AGENTS_LIST=10.200.0.161,10.200.0.162,10.200.0.181,10.200.0.182,10.200.0.183,10.200.0.184,10.200.0.185,10.200.0.194  -DCLIENTSECRET=cr7gDH6hX2-C3SBZYWj8F -DID_POLICY_ZK=spartazk  -DSELENIUM_GRID=sl.demo.stratio.com:4444 -DFORCE_BROWSER=chrome_64spart
