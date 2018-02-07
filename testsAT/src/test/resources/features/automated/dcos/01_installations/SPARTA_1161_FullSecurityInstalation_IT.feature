@rest
Feature: [SPARTA-1161] Installation sparta with mustache
  Background: Setup DCOS-CLI
    #Start SSH with DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user 'admin' and password '1234'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'

  Scenario: [SPARTA-1161][01]Add zookeper-sparta policy to write in zookeper
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/zookeeper_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_POLICY_ZK}       | n/a |
      |   $.name                  |  UPDATE    | ${ID_POLICY_ZK}       | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}  | n/a |
    Then the service response status must be '201'
  Scenario: [SPARTA-1161][02] Sparta Instalation with Mustache in DCOS
    #Modify json to install specific configuration forSparta
    Given I create file 'spartamustache.json' based on 'schemas/dcosFiles/${SPARTA_JSON}' as 'json' with:
      |   $.Framework.name                                    |  UPDATE     | ${DCOS_SERVICE_NAME}                                                    |n/a |
      |   $.Marathon-LB.haproxy_path                          |  UPDATE     | /${DCOS_SERVICE_NAME}                                                   |n/a |
      |   $.Hdfs.user_name                                    |  UPDATE     | ${DCOS_SERVICE_NAME}                                                    |n/a |
      |   $.Zookeeper.address                                 |  UPDATE     | ${ZK_URL}                                                               |n/a |
      |   $.Marathon.uri                                      |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com/service/marathon                 |n/a |
      |   $.Marathon-LB.haproxy_host                          |  UPDATE     | sparta.${CLUSTER_ID}.labs.stratio.com                                   |n/a |
      |   $.Marathon-LB.haproxy_path                          |  UPDATE     | /${DCOS_SERVICE_NAME}                                                   |n/a |
      |   $.Marathon.sso_redirectUri                          |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com/acs/api/v1/auth/login            |n/a |
      |   $.Marathon.sparta_docker_image                      |  UPDATE     | ${DOCKER_URL}:${STRATIO_SPARTA_VERSION}                                 |n/a |
      |   $.Calico.enabled                                    |  REPLACE    | true                                                        |boolean |
      |   $.Hdfs.default_fs                                   |  UPDATE     | ${HDFS_IP}                                                              |n/a |
      |   $.Hdfs.conf_uri                                     |  UPDATE     | hdfs://${HDFS_IP}:8020                                                  |n/a |
      |   $.Hdfs.user_name                                    |  UPDATE     | ${DCOS_SERVICE_NAME}                                                    |n/a |
      |   $.Security.HDFS.hadoop_namenode_krb_principal       |  UPDATE     | hdfs/${HDFS_IP}@${HDFS_REALM}                                          |n/a |
      |   $.Security.HDFS.hadoop_namnode_krb_principal_pattern|  UPDATE     | hdfs/*@${HDFS_REALM}                                                   |n/a |
      |   $.Security.Marathon.sso_uri                         |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com:9005/sso                         |n/a |
      |   $.Security.Marathon.sso_redirectUri                 |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com/acs/api/v1/auth/login            |n/a |
      |   $.Security.use_dynamic_authentication               |  UPDATE     | true                                                                    |n/a |
      |   $.Security.Components.oauth2_enabled                |  REPLACE    | true                                                         |boolean |
      |   $.Security.Components.gosec_enabled                 |  REPLACE    | true                                                         |boolean |
      |   $.Security.Components.marathon_enabled              |  REPLACE    | true                                                                    |boolean |
      |   $.Security.Oauth.onLoginGoTo                        |  UPDATE     | /${DCOS_SERVICE_NAME}                                                    |n/a |
      |   $.Security.Oauth.authorize                          |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com:9005/sso/oauth2.0/authorize       |n/a |
      |   $.Security.Oauth.accessToken                        |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com:9005/sso/oauth2.0/accessToken     |n/a |
      |   $.Security.Oauth.profile                            |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com:9005/sso/oauth2.0/profile         |n/a |
      |   $.Security.Oauth.logout                             |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com:9005/sso/logout                   |n/a |
      |   $.Security.Oauth.callback                           |  UPDATE     | https://sparta.${CLUSTER_ID}.labs.stratio.com/${DCOS_SERVICE_NAME}/login |n/a |
      |   $.Security.Vault.host                               |  UPDATE     | vault.service.paas.labs.stratio.com                                      |n/a |
      |   $.Security.authorize                                |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com:9005/sso/oauth2.0/authorize       |n/a |
      |   $.Security.accessToken                              |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com:9005/sso/oauth2.0/accessToken     |n/a |
      |   $.Security.profile                                  |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com:9005/sso/oauth2.0/profile         |n/a |
      |   $.Security.logout                                   |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com:9005/sso/logout                   |n/a |
      |   $.Security.callback                                 |  UPDATE     | https://sparta.${CLUSTER_ID}.labs.stratio.com/${DCOS_SERVICE_NAME}/login |n/a |
      |   $.Crossdata.Spark.Image                             |  UPDATE     | ${CROSSDATA_SERVER_CONFIG_SPARK_IMAGE}                                   |n/a |
      |   $.Crossdata.Catalog.zookeeper_connection_string     |  UPDATE     | ${ZK_URL}                                                                |n/a |
    #Copy DEPLOY JSON to DCOS-CLI
    And I outbound copy 'target/test-classes/spartamustache.json' through a ssh connection to '/dcos'
    #Start image from mustache
    When I run 'dcos package describe --app --options=/dcos/spartamustache.json sparta >> /dcos/spartaBasicMarathon.json' in the ssh connection
    Then I run 'sed -i -e 's|"image":.*|"image": "${DOCKER_URL}:${STRATIO_SPARTA_VERSION}",|g' /dcos/spartaBasicMarathon.json' in the ssh connection
    And I run 'dcos marathon app add /dcos/spartaBasicMarathon.json' in the ssh connection

    And in less than '400' seconds, checking each '20' seconds, the command output 'dcos task | grep -w ${DCOS_SERVICE_NAME}' contains '${DCOS_SERVICE_NAME}'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/${DCOS_SERVICE_NAME}  | awk '{print $5}' | grep ${DCOS_SERVICE_NAME} ' in the ssh connection and save the value in environment variable 'spartaTaskId'
    #Check sparta is runing in DCOS
    When  I run 'echo !{spartaTaskId}' in the ssh connection
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{spartaTaskId} | grep TASK_RUNNING' contains 'TASK_RUNNING'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{spartaTaskId} | grep healthCheckResults' contains 'healthCheckResults'
    And in less than '600' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{spartaTaskId} | grep  '"alive": true'' contains '"alive": true'

  #Add Sparta Policy
  Scenario: [SPARTA-1161][03] Add sparta policy for authorization in sparta with full security
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/sp_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${DCOS_SERVICE_NAME}     | n/a |
      |   $.name                  |  UPDATE    | ${DCOS_SERVICE_NAME}     | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}     | n/a |
    Then the service response status must be '201'

  #Remove Policy
  Scenario: [SPARTA-1161][04]Delete zk-sparta Policy
    When I send a 'DELETE' request to '/service/gosecmanagement/api/policy/${ID_POLICY_ZK}'
    Then the service response status must be '200'

  Scenario: [SPARTA-1161][05] Remove Instalation with full security in DCOS
    When  I run 'dcos marathon app remove /sparta/${DCOS_SERVICE_NAME}/${DCOS_SERVICE_NAME}' in the ssh connection
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos task | grep ${DCOS_SERVICE_NAME} | wc -l' contains '0'

  @runOnEnv(DELETE_SPARTA_POLICY)
  Scenario: [SPARTA-1161][05]Delete Sparta Policy
    When I send a 'DELETE' request to '/service/gosecmanagement/api/policy/${DCOS_SERVICE_NAME}'
    Then the service response status must be '200'