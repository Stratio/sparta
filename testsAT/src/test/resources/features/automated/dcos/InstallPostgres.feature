@rest
Feature: install postgres

 ##################################################
   ## INSTALL POSTGRES COMMUNITY
   ##################################################
  Scenario: Install Postgres Community

    Given I open a ssh connection to '10.200.0.21' with user 'root' and password 'stratio'
    Then I run ' cat /etc/sds/gosec-sso/cas/cas-vault.properties |grep token | cut -f 2 -d :' in the ssh connection and save the value in environment variable 'vaultToken'
    And I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER}' with user '${REMOTE_USER}' and password 'stratio'
    And I securely send requests to '${DCOS_IP}:443'

      # Install according to json with marathon
    When I send a 'POST' request to '/marathon/v2/apps' based on 'schemas/dcosFiles/postgresinstalation.json' as 'json' with:
      | $.id | UPDATE | /${INSTANCE} |
      | $.env.POSTGRES_MESOS_PRINCIPAL | UPDATE | 012345 |
      | $.env.DISABLE_DYNAMIC_RESERVATION | UPDATE | true |
      | $.env.DISABLE_ZOOKEEPER_SEC       | UPDATE | disabled |
      | $.env.POSTGRES_MESOS_ROLE | UPDATE | slave_public |
      | $.env.REALM | UPDATE | DEMO.STRATIO.COM |
      | $.env.POSTGRES_ZOOKEEPER | UPDATE | master.mesos:2181 |
      | $.env.POSTGRES_CPU | UPDATE | 0.5 |
      | $.env.KADMIN_HOST | UPDATE | idp.integration.labs.stratio.com |
      | $.env.VAULT_TOKEN | UPDATE | !{vaultToken} |
      | $.env.POSTGRES_MESOS_MASTER | UPDATE | master.mesos:2181 |
      | $.env.TENANT_NAME | UPDATE | ${INSTANCE} |
      | $.env.VAULT_PORT | UPDATE | 8200 |
      | $.env.ENABLE_MESOS_SEC | UPDATE | true |
      | $.env.VAULT_HOSTS | UPDATE | gosec2.node.default-cluster.labs.stratio.com |
      | $.env.KDC_HOST | UPDATE | idp.integration.labs.stratio.com |
      | $.env.POSTGRES_DOCKER_IMAGE | UPDATE | qa.stratio.com/stratio/postgresql-community:${COMMUNITY_VERSION} |
      | $.env.POSTGRES_SERVICE_NAME | UPDATE | ${INSTANCE} |
      | $.env.ENABLE_MARATHON_SEC | UPDATE | true |
      | $.container.docker.image | UPDATE | qa.stratio.com/stratio/postgres-bigdata-framework:${FRAMEWORK_VERSION} |
      | $.labels.DCOS_SERVICE_NAME | UPDATE | ${INSTANCE} |
      | $.labels.DCOS_PACKAGE_FRAMEWORK_NAME | UPDATE | ${INSTANCE} |
    Then the service response status must be '201'

      #  Check the result with dcos-cli
    Given I open a ssh connection to '${DCOS_CLI}' with user '${CLI_USER}' and password '${CLI_PASSWORD}'
    And in less than '300' seconds, checking each '20' seconds, the command output 'dcos task | grep ${INSTANCE} | grep R | wc -l' contains '1'
    And I wait '200' seconds
    When I send a 'GET' request to '/mesos/frameworks'
    Then the service response status must be '200'
    And I save element '$' in environment variable 'coordinator'
    And 'coordinator' matches the following cases:
      | $.frameworks[?(@.name == "${INSTANCE}")].tasks[?(@.name == "pg_0001")].statuses[*].state           | contains   | TASK_RUNNING          |
    And 'coordinator' matches the following cases:
      | $.frameworks[?(@.name == "${INSTANCE}")].tasks[?(@.name == "pg_0002")].statuses[*].state           | contains   | TASK_RUNNING          |


    # Example of execution with mvn :
#   mvn verify -DDCOS_CLI_HOST=172.17.0.2  -DINSTANCE='postgres-auto' -DCOMMUNITY_VERSION=0.9.0 -DFRAMEWORK_VERSION=0.14.0 -DREMOTE_USER=root -DDCOS_IP=10.200.0.21 -DDCOS_USER=root   -DDCOS_PASSWORD=stratio  -DZK_URL='zk-0001-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0002-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0003-zookeeperstable.service.paas.labs.stratio.com:2181' -DFORCEPULLIMAGE=false -Dit.test=com.stratio.sparta.testsAT.automated.dcos.ISInstallPostgres -DlogLevel=DEBUG -Dmaven.failsafe.debu
 # mvn verify -DINSTANCE='postgres-auto' -DDCOS_IP='10.200.0.21' -DDCOS_USER=admin@demo.stratio.com -DREMOTE_USER=root -DREMOTE_PASSWORD=stratio -DDCOS_CLI_HOST=dcos-cli.demo.labs.stratio.com -DCLI_USER=root -DCLI_PASSWORD=stratio