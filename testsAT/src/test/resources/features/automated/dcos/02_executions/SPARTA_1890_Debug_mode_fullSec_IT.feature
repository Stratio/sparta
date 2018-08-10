@rest
Feature: [SPARTA-1890] E2E Execution DebugMode Workflow -Batch mode
  Background: Conect to navigator for Cookie
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Then I run 'dcos task ${MARATHON_LB_TASK:-marathon-lb-sec} | awk '{print $2}'| tail -n 1' in the ssh connection and save the value in environment variable 'marathonIP'
    And I wait '1' seconds
    When  I run 'echo !{marathonIP}' in the ssh connection
    And I open a ssh connection to '!{marathonIP}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    And I run 'hostname | sed -e 's|\..*||'' in the ssh connection with exit status '0' and save the value in environment variable 'MarathonLbDns'

  #***********************************************************
  # INSTALL AND EXECUTE Generali- Batch Mode                 *
  #***********************************************************
  @web
  Scenario:[SPARTA-1890][01] Install DebugMode workflow - HDFS (csv) -Postgres
    #Login into the platform
    Given My app is running in '!{MarathonLbDns}.labs.stratio.com:443'
    When I securely browse to '/${DCOS_SERVICE_NAME}'
    And I wait '3' seconds
    And '1' elements exists with 'xpath://*[@id="username"]'
    And I type '${USER:-admin}' on the element on index '0'
    And '1' elements exists with 'xpath://*[@id="password"]'
    And I type '${PASSWORD:-1234}' on the element on index '0'
    And '1' elements exists with 'id:login-button'
    And I click on the element on index '0'
    And I wait '1' seconds
    Then I save selenium cookies in context
    #include workflow
    When I securely send requests to '!{MarathonLbDns}.labs.stratio.com:443'
    Then I send a 'POST' request to '/${DCOS_SERVICE_NAME}/workflows' based on 'schemas/workflows/crossdata-write-batch.json' as 'json' with:
      |$.pipelineGraph.nodes[2].configuration.url|  UPDATE  | jdbc:postgresql://${POSTGRES_INSTANCE}?user=${DCOS_SERVICE_NAME}   | n/a |
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '10' seconds


  @web
  Scenario:[SPARTA-1279][02] Execute DebugMode workflow
    #Login into the platform
    Given My app is running in '!{MarathonLbDns}.labs.stratio.com:443'
    When I securely browse to '/${DCOS_SERVICE_NAME}'
    And I wait '3' seconds
    And '1' elements exists with 'xpath://*[@id="username"]'
    And I type '${USER:-admin}' on the element on index '0'
    And '1' elements exists with 'xpath://*[@id="password"]'
    And I type '${PASSWORD:-1234}' on the element on index '0'
    And '1' elements exists with 'id:login-button'
    And I click on the element on index '0'
    Then I take a snapshot
    And I wait '03' seconds
    Then I take a snapshot
    When I securely browse to '/${DCOS_SERVICE_NAME}/#/wizard/edit/!{previousWorkflowID}'
    And I wait '1' seconds
    Given '1' element exists with 'xpath://*[@id="debug-mode"]'
    When I click on the element on index '0'
    And this text exists 'Execution in debug mode has been successful'
    Then I take a snapshot


#MVN Example
# mvn verify -DCLUSTER_ID=nightly  -DDCOS_SERVICE_NAME=sparta-server -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.SPARTA_1890_GeneraliBatchworkflow_IT -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nightly.demo.stratio.com -DPOSTGRES_NAME=postgrestls -DPOSTGRES_INSTANCE=pg-0001.postgrestls.mesos:5432/postgres