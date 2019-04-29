@rest
Feature: [SPARTA-1196] Generate and Execute Workflow and see Streaming

  @runOnEnv(CREATE_TOPIC=TRUE)
  Scenario: [SPARTA-1278][01] - Create topic for sparta in kafka
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    When I send a 'PUT' request to '/service/${KAFKA_NAME:-eos-kafka-framework}/v1/topics/${TOPIC:-idtopic}?partitions=${KAFKA_PARTITION:-1}&replication=${KAFKA_REPLICATION:-1}'
    Then the service response status must be '200'

  @skipOnEnv(EXECUTION=FALSE)
  Scenario: [SPARTA-1278][02] Take Marathon-lb IP
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Then I run 'dcos task ${MARATHON_LB_TASK:-marathon-lb} | awk '{print $2}'| tail -n 1' in the ssh connection and save the value in environment variable 'marathonIP'
    Then I wait '1' seconds
    And I open a ssh connection to '!{marathonIP}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    And I run 'hostname | sed -e 's|\..*||'' in the ssh connection with exit status '0' and save the value in environment variable 'MarathonLbDns'

  @web
  @skipOnEnv(EXECUTION=FALSE)
  @loop(EXECUTIONS,EXECUTION)
  Scenario: [SPARTA-1196][03]See workflow Details postgres-kafka-PNF Execution number: <EXECUTION>
    Given My app is running in '!{MarathonLbDns}.labs.stratio.com:443'
    When I securely browse to '${URL_WORKFLOW_STREAMING:-/workflows-sparta-server/home/kafka-postgres-pnf-2/kafka-postgres-pnf-2-v0/d0b5c2}/streaming/'
    Then I take a snapshot
    When I securely browse to '${URL_WORKFLOW_STREAMING:-/workflows-sparta-server/home/kafka-postgres-pnf-2/kafka-postgres-pnf-2-v0/d0b5c2}/executors/'
    Then I take a snapshot
    When I securely browse to '${URL_WORKFLOW_STREAMING:-/workflows-sparta-server/home/kafka-postgres-pnf-2/kafka-postgres-pnf-2-v0/d0b5c2}/executors/threadDump/?executorId=2'
    Then I take a snapshot
    And I wait '${WAIT_WORKFLOW:-300}' seconds

    #MVN Example
    #mvn verify -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.SPARTA_1196_Workflow_PNF_IT -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-fulle1 -DDCOS_SERVICE_NAME=sparta-server -DSELENIUM_GRID=sl.demo.stratio.com:4444 -DFORCE_BROWSER=chrome_64sparta -DMARATHON_LB_TASK=marathonlb -EXECUTIONS=1,2,3,4,5