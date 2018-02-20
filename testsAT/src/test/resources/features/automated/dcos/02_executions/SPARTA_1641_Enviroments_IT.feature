@rest
Feature: [SPARTA-1641] Environment: Operations over environment

  Background: : conect to navigator
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user 'admin' and password '1234'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'

  Scenario:[SPARTA-1641][01] Find enviroment
    Given I send a 'GET' request to '/service/${DCOS_SERVICE_NAME}/environment'
    Then the service response status must be '200'

  Scenario:[SPARTA-1641][02] Create enviroment
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/environment' based on 'schemas/enviroments/enviroments_variableTest.json' as 'json' with:
      |$.variables[0].name |  UPDATE  |   ${ENVIROMENT_NAME:-DEFAULT_ENVIROMENT_NAME}  | n/a |
      |$.variables[0].value |  UPDATE  |   ${ENVIROMENT_VALUE:-1234}  | n/a |
    Then the service response status must be '200' and its response must contain the text '${ENVIROMENT_VALUE:-1234}'

  Scenario:[SPARTA-1641][03] Create enviroment variable
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/environment/variable' based on 'schemas/enviroments/enviroment_1variable.json' as 'json' with:
      |$.name |  UPDATE  |   ${ENVIROMENT_VARIABLE_NAME:-DEFAULT_ENVIROMENT_VARIABLE_NAME}  | n/a |
      |$.value |  UPDATE  |   ${ENVIROMENT_VARIABLE_VALUE:-6666}  | n/a |
    Then the service response status must be '200' and its response must contain the text '${ENVIROMENT_VARIABLE_VALUE:-6666}'

  Scenario:[SPARTA-1641][04] Update an enviroment variable
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/environment' based on 'schemas/enviroments/enviroments_variableTest.json' as 'json' with:
      |$.variables[0].name |  UPDATE  |   ${ENVIROMENT_NAME:-DEFAULT_ENVIROMENT_NAME}  | n/a |
      |$.variables[0].value |  UPDATE  |   ${ENVIROMENT_VALUE_UPDATE:-77777}  | n/a |
    Then the service response status must be '200' and its response must contain the text '${ENVIROMENT_VALUE_UPDATE:-77777}'

  Scenario:[SPARTA-1641][05] Find an variable by its name
    Given I send a 'GET' request to '/service/${DCOS_SERVICE_NAME}/environment/variable/${ENVIROMENT_NAME:-DEFAULT_ENVIROMENT_NAME}'
    Then the service response status must be '200'

  Scenario:[SPARTA-1641][06] Delete enviroment variable
    Given I send a 'DELETE' request to '/service/${DCOS_SERVICE_NAME}/environment/variable/${ENVIROMENT_NAME:-DEFAULT_ENVIROMENT_NAME}'
    Then the service response status must be '200'
    #check delete variable
    Given I send a 'GET' request to '/service/${DCOS_SERVICE_NAME}/environment/variable/${ENVIROMENT_NAME:-DEFAULT_ENVIROMENT_NAME}'
    Then the service response status must be '500'

  Scenario:[SPARTA-1641][07] Delete enviroment
    Given I send a 'DELETE' request to '/service/${DCOS_SERVICE_NAME}/environment'
    Then the service response status must be '200'

  Scenario:[SPARTA-1641][08] Create enviroment by default parameteres
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/environment' based on 'schemas/enviroments/enviroments_variables.json' as 'json' with:
      |$.variables[0].name |  UPDATE  |   DEFAULT_OUTPUT_FIELD  | n/a |
    Then the service response status must be '200' and its response must contain the text 'DEFAULT_OUTPUT_FIELD'

  Scenario:[SPARTA-1641][09] Import enviroment data
    Given I send a 'PUT' request to '/service/${DCOS_SERVICE_NAME}/environment/import' based on 'schemas/enviroments/enviroment_data.json' as 'json' with:
      | id | DELETE | N/A|
    Then the service response status must be '200'
    Given I send a 'GET' request to '/service/${DCOS_SERVICE_NAME}/environment'
    Then the service response status must be '200' and its response must contain the text 'DEFAULT_OUTPUT_FIELD'
    #Validate workflows exist
    Given I send a 'GET' request to '/service/${DCOS_SERVICE_NAME}/workflows'
    Then the service response status must be '200' and its response must contain the text 'kafka-postgres'

  Scenario:[SPARTA-1641][10] Export enviroment data to other enviroment
    Given I send a 'GET' request to '/service/${DCOS_SERVICE_NAME}/environment/export' based on 'schemas/enviroments/enviroment_data.json'
    Then the service response status must be '200' and its response must contain the text 'Automatic Workflow'


  #MVN Example
  #mvn verify -DCLUSTER_ID=pool1  -DDCOS_SERVICE_NAME=sparta-server1 -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.SPARTA_1641_Enviroments_IT -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-pool1.demo.stratio.com
