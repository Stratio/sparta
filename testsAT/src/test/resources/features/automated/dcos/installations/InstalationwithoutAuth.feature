@rest
Feature: [SPARTA][DCOS]Instalation sparta with security + dynamic Authentication without Auth
  Background: Setup DCOS-CLI
    #Start SSH with DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
  Scenario: Generate workflow Sparta
    Given I create file 'SpartaSecurityInstalation.json' based on 'schemas/dcosFiles/${SPARTA_JSON}' as 'json' with:
      |   $.container.docker.image                                |  UPDATE     | ${SPARTA_DOCKER_IMAGE}         | n/a    |
      |   $.id                                                    |  UPDATE     | ${id}                          | n/a    |
      |   $.env.DCOS_SERVICE_NAME                                 |  UPDATE     | ${DCOS_SERVICE_NAME}           | n/a    |
      |   $.env.DCOS_PACKAGE_FRAMEWORK_NAME                       |  UPDATE     | ${DCOS_PACKAGE_FRAMEWORK_NAME} | n/a    |
      |   $.container.docker.forcePullImage                       |  REPLACE    | ${FORCEPULLIMAGE}             |boolean |
      |   $.env.SPARTA_ZOOKEEPER_CONNECTION_STRING                |  UPDATE     | ${ZK_URL}                        |n/a |
      |   $.env.MARATHON_TIKI_TAKKA_MARATHON_URI                  |  UPDATE     | ${MARATHON_TIKI_TAKKA}           |n/a |
      |   $.env.MARATHON_SSO_CLIENT_ID                            |  UPDATE     | ${MARATHON_SSO_CLIENT_ID}        |n/a |
      |   $.env.SPARTA_DOCKER_IMAGE                               |  UPDATE     | ${SPARTA_DOCKER_IMAGE}           |n/a |
      |   $.env.VAULT_HOSTS                                       |  UPDATE     | ${VAULT_HOSTS}                   | n/a|
      |   $.env.VAULT_PORT                                        |  UPDATE     | ${VAULT_PORT}                    |n/a |
      |   $.env.MARATHON_SSO_REDIRECT_URI                         |  UPDATE     | ${MARATHON_SSO_REDIRECT_URI}     |n/a |
      |   $.env.MARATHON_SSO_URI                                  |  UPDATE     | ${MARATHON_SSO_URI}              |n/a |
      |   $.env.CROSSDATA_CORE_CATALOG_ZOOKEEPER_CONNECTIONSTRING |  UPDATE     | ${ZK_URL}                        |n/a |
      |   $.env.HADOOP_FS_DEFAULT_NAME                            |  UPDATE     | ${HADOOP_FS_DEFAULT_NAME}        |n/a |
      |   $.env.HADOOP_NAMENODE_KRB_PRINCIPAL                     |  UPDATE     | ${HADOOP_NAMENODE_KRB_PRINCIPAL} |n/a |
      |   $.env.HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN             |  UPDATE     | ${HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN} |n/a |
      |   $.env.CROSSDATA_SERVER_CONFIG_SPARK_IMAGE               |  UPDATE     | ${CROSSDATA_SERVER_CONFIG_SPARK_IMAGE}   |n/a |
      |   $.env.HADOOP_FS_DEFAULT_NAME                            |  UPDATE     | ${HADOOP_FS_DEFAULT_NAME}        |n/a |
      |   $.secrets.role.source                                   |  UPDATE     | ${ROLE_SPARTA}                   |n/a |
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
