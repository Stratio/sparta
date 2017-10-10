@rest
Feature: [SPARTA-1182]Instalation sparta with security + dynamic Authentication without Auth in DCOS
  Background: Setup DCOS-CLI
    #Start SSH with DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
  Scenario: [SPARTA-1182][01] Install Sparta aplication in DCOS
    Given I create file 'SpartaSecurityInstalation.json' based on 'schemas/dcosFiles/${SPARTA_JSON}' as 'json' with:
      |   $.container.docker.image                                |  UPDATE     | ${DOCKER_URL}:${STRATIO_SPARTA_VERSION}             | n/a    |
      |   $.id                                                    |  UPDATE     | /sparta/${DCOS_SERVICE_NAME}/${DCOS_SERVICE_NAME}   | n/a    |
      |   $.env.DCOS_SERVICE_NAME                                 |  UPDATE     | ${DCOS_SERVICE_NAME}                                | n/a    |
      |   $.env.DCOS_PACKAGE_FRAMEWORK_NAME                       |  UPDATE     | ${DCOS_SERVICE_NAME}                                | n/a    |
      |   $.container.docker.forcePullImage                       |  REPLACE    | ${FORCEPULLIMAGE}                                   |boolean |
      |   $.env.SPARTA_ZOOKEEPER_CONNECTION_STRING                |  UPDATE     | ${ZK_URL}                                           |n/a |
      |   $.env.MARATHON_TIKI_TAKKA_MARATHON_URI                  |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com/service/marathon/ |n/a |
      |   $.env.MARATHON_SSO_CLIENT_ID                            |  UPDATE     | ${MARATHON_SSO_CLIENT_ID}                           |n/a |
      |   $.env.SPARTA_DOCKER_IMAGE                               |  UPDATE     | ${DOCKER_URL}:${STRATIO_SPARTA_VERSION}             |n/a |
      |   $.env.VAULT_HOSTS                                       |  UPDATE     | ${VAULT_HOST}                                       | n/a|
      |   $.env.VAULT_PORT                                        |  UPDATE     | ${VAULT_PORT}                                       |n/a |
      |   $.env.MARATHON_SSO_REDIRECT_URI                         |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com/acs/api/v1/auth/login    |n/a |
      |   $.env.MARATHON_SSO_URI                                  |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com:9005/sso     |n/a |
      |   $.env.CROSSDATA_CORE_CATALOG_ZOOKEEPER_CONNECTIONSTRING |  UPDATE     | ${ZK_URL}                                           |n/a |
      |   $.env.HADOOP_FS_DEFAULT_NAME                            |  UPDATE     | hdfs://${HDFS_IP}:${HDFS_PORT}                      |n/a |
      |   $.env.HADOOP_NAMENODE_KRB_PRINCIPAL                     |  UPDATE     | hdfs/${HDFS_IP}@${HDFS_REALM}                       |n/a |
      |   $.env.HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN             |  UPDATE     | hdfs/*@${HDFS_REALM}                                |n/a |
      |   $.env.CROSSDATA_SERVER_CONFIG_SPARK_IMAGE               |  UPDATE     | ${CROSSDATA_SERVER_CONFIG_SPARK_IMAGE}              |n/a |
      |   $.secrets.role.source                                   |  UPDATE     | ${ROLE_SPARTA}                                      |n/a |
    #Copy DEPLOY JSON to DCOS-CLI
    Then I outbound copy 'target/test-classes/SpartaSecurityInstalation.json' through a ssh connection to '/dcos'
    #Start image from JSON
    Given I run 'dcos marathon app add /dcos/SpartaSecurityInstalation.json' in the ssh connection
    #Check Sparta is Running
    Then in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep sparta-server_sparta | grep R | wc -l' contains '1'
    #Find task-id if from DCOS-CLI
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos marathon task list /sparta/sparta-server/sparta-server  | grep sparta-server | awk '{print $2}'' contains 'True'
    And I run 'dcos marathon task list /sparta/sparta-server/sparta-server | awk '{print $5}' | grep sparta-server' in the ssh connection and save the value in environment variable 'spartaTaskId'
  @ignore  @manual
  Scenario: Remove workflow Sparta
    When  I run 'dcos marathon app remove /sparta/sparta-server/sparta-server' in the ssh connection
    Then in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep sparta-server | grep R | wc -l' contains '0'