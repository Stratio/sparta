@rest @web
Feature: [SPARTA-1196] Generate and Execute Workflow and see Streaming

  Scenario: [SPARTA-1278][02] Take Marathon-lb IP
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Then I run 'dcos task ${MARATHON_LB_TASK:-marathon-lb} | awk '{print $2}'| tail -n 1' in the ssh connection and save the value in environment variable 'marathonIP'
    Then I wait '1' seconds
    And I open a ssh connection to '!{marathonIP}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    And I run 'hostname | sed -e 's|\..*||'' in the ssh connection with exit status '0' and save the value in environment variable 'MarathonLbDns'

  Scenario: [SPARTA-1196][01]See workflow Details postgres-kafka-12exec-v2
    Given My app is running in '!{MarathonLbDns}.labs.stratio.com:443'
    When I securely browse to '/workflows-sparta-server/home/rendimiento/kafka-postgres-12ex/kafka-postgres-12ex-v2/streaming/'
    Then I take a snapshot
    When I securely browse to '/workflows-sparta-server/home/rendimiento/kafka-postgres-12ex/kafka-postgres-12ex-v2/executors/'
    And I wait '60' seconds
    Then I take a snapshot
    When I securely browse to '/workflows-sparta-server/home/rendimiento/kafka-postgres-12ex/kafka-postgres-12ex-v2/executors/threadDump/?executorId=2'
    Then I take a snapshot

    #MVN Example
    #mvn verify -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.SPARTA_1196_Workflow_PNF_IT -DCLUSTER_ID='nightly' -DWORKFLOW_LIST=testinput-kafka,kafka-elastic  -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-fulle1 -DDCOS_SERVICE_NAME=sparta-server -DSELENIUM_GRID=sl.demo.stratio.com:4444 -DFORCE_BROWSER=chrome_64sparta