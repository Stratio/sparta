@rest
Feature: [SPARTA-1182]Instalation sparta with mustache
  Background: Setup DCOS-CLI
    #Start SSH with DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    #And  I securely send requests to '${DCOS_IP}:443'
  Scenario: [SPARTA-1238][Scenario-1] Sparta Instalation with Mustache in DCOS
    #Modify json to install specific configuration forSparta
    Given I create file 'spartamustache.json' based on 'schemas/dcosFiles/${SPARTA_JSON}' as 'json' with:
      |   $.Framework.name                                    |  UPDATE     | ${DCOS_SERVICE_NAME}                                                    |n/a |
      |   $.Marathon-LB.haproxy_path                          |  UPDATE     | /${DCOS_SERVICE_NAME}                                                   |n/a |
      |   $.Hdfs.user_name                                    |  UPDATE     | ${DCOS_SERVICE_NAME}                                                    |n/a |
      |   $.Zookeeper.address                                 |  UPDATE     | ${ZK_URL}                                                               |n/a |
      |   $.Marathon.uri                                      |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com/service/marathon                 |n/a |
      #Commented to fix problem with universe [SPARTA-1288]
      # | $.Marathon-LB.haproxy_host                          |  UPDATE     | sparta.${CLUSTER_ID}.labs.stratio.com                                   |n/a |
      |   $.Marathon-LB.haproxy_host                          |  UPDATE     | ${CLUSTER_ID}.labs.stratio.com/service/${DCOS_SERVICE_NAME}             |n/a |
      |   $.Marathon-LB.haproxy_path                          |  UPDATE     | /${DCOS_SERVICE_NAME}                                                   |n/a |
      |   $.Marathon.sso_redirectUri                          |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com/acs/api/v1/auth/login            |n/a |
      |   $.Marathon.sparta_docker_image                      |  UPDATE     | ${DOCKER_URL}:${STRATIO_SPARTA_VERSION}                                 |n/a |
      |   $.Calico.enabled                                    |  REPLACE    | ${CALICOENABLED}                                                        |boolean |
      |   $.Hdfs.default_fs                                   |  UPDATE     | ${HDFS_IP}                                                              |n/a |
      |   $.Hdfs.conf_uri                                     |  UPDATE     | hdfs://${HDFS_IP}:8020                                                  |n/a |
      |   $.Hdfs.user_name                                    |  UPDATE     | ${DCOS_SERVICE_NAME}                                                    |n/a |
      |   $.Security.HDFS.hadoop_namenode_krb_principal       |  UPDATE     | hdfs/${HDFS_IP}@${HDFS_REALM}                                          |n/a |
      |   $.Security.HDFS.hadoop_namnode_krb_principal_pattern|  UPDATE     | hdfs/*@${HDFS_REALM}                                                   |n/a |
      |   $.Security.Marathon.sso_uri                         |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com:9005/sso                         |n/a |
      |   $.Security.Marathon.sso_redirectUri                 |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com/acs/api/v1/auth/login            |n/a |
      |   $.Security.use_dynamic_authentication               |  UPDATE     | true                                                                    |n/a |
      |   $.Security.Components.oauth2_enabled                |  REPLACE    | ${AUTH_ENABLED}                                                         |boolean |
      |   $.Security.Components.gosec_enabled                 |  REPLACE    | ${AUTH_ENABLED}                                                         |boolean |
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
    When I run 'dcos package install --yes --options=/dcos/spartamustache.json sparta' in the ssh connection
    Then the command output contains 'Installing Marathon app for package [sparta]'

    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep -w ${DCOS_SERVICE_NAME} | wc -l' contains '1'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/${DCOS_SERVICE_NAME}  | awk '{print $5}' | grep ${DCOS_SERVICE_NAME} ' in the ssh connection and save the value in environment variable 'spartaTaskId'
    #Check sparta is runing in DCOS
    And  I run 'echo !{spartaTaskId}' in the ssh connection
    Then in less than '300' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{spartaTaskId} | grep TASK_RUNNING | wc -l' contains '1'

    #mvn Example:
    #mvn verify -DCLUSTER_ID=nightly -DDCOS_SERVICE_NAME=sparta-server -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.SPARTA_1238_InstalationwithMustache_IT -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nigthly.demo.stratio.com -DDOCKER_URL=qa.stratio.com/stratio/sparta -DCALICOENABLED=false -DHDFS_IP=10.200.0.74 -DROLE_SPARTA=open -DAUTH_ENABLED=true -DSTRATIO_SPARTA_VERSION=1.7.6 -DZK_URL=zk-0001-zkuserland.service.paas.labs.stratio.com:2181,zk-0002-zkuserland.service.paas.labs.stratio.com:2181,zk-0003-zkuserland.service.paas.labs.stratio.com:2181 -DDCOS_IP=10.200.0.21 -DSPARTA_NAME=sparta-server -DCROSSDATA_SERVER_CONFIG_SPARK_IMAGE=qa.stratio.com/stratio/stratio-spark:2.1.0.1 -DSPARTA_JSON=spartamustache_1.7.6.json -DHDFS_REALM=DEMO.STRATIO.COM