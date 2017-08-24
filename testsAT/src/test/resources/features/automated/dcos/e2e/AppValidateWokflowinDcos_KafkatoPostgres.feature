@rest
Feature: [SPARTA][DCOS]Validate workflow in DCOS Kafka to Postgres

  Background: Setup DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'

  Scenario: Install Sparta and generate workflow
    #Connect to dcos to get token and vault
    Given I open a ssh connection to '${DCOS_IP}' with user 'root' and password 'stratio'
    #get token
    Then I run ' cat /etc/sds/gosec-sso/cas/cas-vault.properties |grep token | cut -f 2 -d :' in the ssh connection and save the value in environment variable 'vaultToken'
    #get vault ip url
    And I run 'cat /etc/sds/gosec-sso/cas/cas-vault.properties |grep address | cut -f 3 -d / | cut -f 1 -d :' in the ssh connection and save the value in environment variable 'vaultIP'
    #get vault port url
    And I run 'cat /etc/sds/gosec-sso/cas/cas-vault.properties |grep address | cut -f 3 -d / | cut -f 2 -d :' in the ssh connection and save the value in environment variable 'vaultport'
    #Given I run 'jq .root_token /opt/stratio/vault/vault_response | sed -e 's/^"//' -e 's/"$//'' in the ssh connection and save the value in environment variable 'vaultToken'
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    Given I create file 'SpartaSecurityInstalation.json' based on 'schemas/dcosFiles/spartaSecurelywithoutMarathon.json' as 'json' with:
      |   $.container.docker.image                           |  UPDATE     | ${SPARTA_DOCKER_IMAGE}           | n/a    |
      |   $.container.docker.forcePullImage                  |  REPLACE    | ${FORCEPULLIMAGE}                |boolean |
      |   $.env.SPARTA_ZOOKEEPER_CONNECTION_STRING           |  UPDATE     | ${ZK_URL}                        |n/a |
      |   $.env.VAULT_TOKEN                                  |  UPDATE     | !{vaultToken}                    |n/a |
      |   $.env.MARATHON_TIKI_TAKKA_MARATHON_URI             |  UPDATE     | ${MARATHON_TIKI_TAKKA}           |n/a |
      |   $.env.SPARTA_DOCKER_IMAGE                          |  UPDATE     | ${SPARTA_DOCKER_IMAGE}           |n/a |
      |   $.env.VAULT_HOSTS                                  |  UPDATE     | !{vaultIP}                       | n/a |
      |   $.env.HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN        |  UPDATE     | ${HADOOP_PATTERN}                |n/a |
      |   $.env.HADOOP_NAMENODE_KRB_PRINCIPAL                |  UPDATE     | ${HADOOP_PRINCIPAL}              |n/a |
      |   $.env.HADOOP_FS_DEFAULT_NAME                       |  UPDATE     | ${HADOOP_DEFAULT_NAME}          |n/a |
    #Copy DEPLOY JSON to DCOS-CLI
    When I outbound copy 'target/test-classes/SpartaSecurityInstalation.json' through a ssh connection to '/dcos'
    #Start image from JSON
    #Then I run 'dcos marathon app add /dcos/SpartaSecurityInstalation.json' in the ssh connection
    #Check Sparta is Running
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep sparta-workflow-server.sparta | grep R | wc -l' contains '1'
    #Find task-id if from DCOS-CLI
    And in less than '400' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/sparta/sparta-workflow-server | grep sparta-workflow-server | awk '{print $2}'' contains 'True'
    And I run 'dcos marathon task list /sparta/sparta/sparta-workflow-server | awk '{print $5}' | grep sparta-workflow-server' in the ssh connection and save the value in environment variable 'spartaTaskId'

   #********************************
   # GENERATE AND EXECUTE WORKFLOW**
   #********************************
    #Find Aplication ip
    When I run 'dcos marathon task list /sparta/sparta/sparta-workflow-server | awk '{print $4}'| awk 'NR ==2'' in the ssh connection and save the value in environment variable 'spartaIP'
    Then  I run 'echo !{spartaIP}' in the ssh connection
    Given I send requests to '!{spartaIP}:10148'
    #include workflow
    Given I send a 'POST' request to '/policy' based on 'schemas/workflows/kafka-postgres-tickets.json' as 'json' with:
      | id | DELETE | N/A  |

    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '2' seconds
    #Execute workflow
    When I send a 'GET' request to '/policy/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text '{"message":"Launched policy with name !{nameWorkflow}'
    #verify the generation of  workflow in dcos
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos marathon task list !{nameWorkflow}.workflows.sparta.sparta | wc -l' contains '2'



  @ignore @manual
  Scenario: TestResults Postgres

    #Find Aplication ip
    Given I run 'dcos marathon task list /sparta/sparta/sparta-workflow-server | awk '{print $4}'| awk 'NR ==2'' in the ssh connection and save the value in environment variable 'spartaIP'
    Then  I run 'echo !{spartaIP}' in the ssh connection
    #Run sparta'
    Given I securely send requests to '!{spartaIP}:10148'
    #Generate a table to connect to Postgres using crossdata
    Given I send a 'POST' request to '/crossdata/queries/{"query": "CREATE TEMPORARY TABLE blacklist USING org.apache.spark.sql.jdbc OPTIONS (url 'jdbc:postgresql://${POSTGRESIP}:${POSTGRESPORT}/postgres?user=postgres&pass=postgres&connectTimeout=10', dbtable '${TABLEPOSTGRES}', driver 'org.postgresql.Driver')"}'
    Then the service response status must be '200'
    #Generate a table to check the total number of register
    Given I send a 'POST' request to '/crossdata/queries/{"query": "select count(*) as total from blacklist"}'
    Then the service response status must be '200' and its response must contain the text '{""total": ${TOTALDATA}'
    # This is an alternative method pending to test
    #    Given I send a 'POST' request to '/crossdata/queries' based on 'schemas/queries/querycrossdata.conf' as 'string' with:
    #      | $.tablepostgres | UPDATE | ${TABLEPOSTGRES}  |
    #      | $.postgresip  | UPDATE | ${POSTGRESIP}  |
    #      | $.postgresport  | UPDATE | ${POSTGRESPORT}  |
    #    #Generate a table to connect to Postgres using crossdata
    #    Then I send a 'POST' request to '/crossdata/queries' based on 'schemas/queries/searchdatapostgres.conf'


    #*******************************
    #    *DELETE APP AND WORKFLOW***
    #*******************************
  Scenario: delete app and workflow
    Given I securely send requests to '!{spartaIP}:10148'
    #delete workflow
    When I send a 'DELETE' request to '/policy/!{previousWorkflowID}'
    Then the service response status must be '200'
    #Remove Sparta
    When  I run 'dcos marathon app remove /sparta/sparta' in the ssh connection
    Then in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep sparta | grep R | wc -l' contains '0'


# Example of execution with mvn :
#   mvn verify -DHADOOP_DEFAULT_NAME='hdfs://10.200.1.5:8020' -DHADOOP_PRINCIPAL='hdfs/10.200.1.5@PERFORMANCE.STRATIO.COM' -DHADOOP_PATTERN='hdfs/*@PERFORMANCE.STRATIO.COM' -DTABLEPOSTGRES='cubetickets' -DPOSTGRESIP='10.200.1.228' -DPOSTGRESPORT='1028' -DTOTALDATA='0' -DSPARTANAME='sparta' -DDCOS_IP='10.200.0.238' -DMARATHON_TIKI_TAKKA='http://10.200.0.21:8080' -DZK_URL='zk-0001-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0002-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0003-zookeeperstable.service.paas.labs.stratio.com:2181' -DDCOS_CLI_HOST=172.17.0.2 -DSPARTA_DOCKER_IMAGE=qa.stratio.com:8443/stratio/sparta:1.6.2-RC1-SNAPSHOTPR36 -DFORCEPULLIMAGE=false -Dit.test=com.stratio.sparta.testsAT.automated.dcos.ISAppValidateWorkflowinDcos_KafkatoPostgres -DlogLevel=DEBUG -Dmaven.failsafe.debu


