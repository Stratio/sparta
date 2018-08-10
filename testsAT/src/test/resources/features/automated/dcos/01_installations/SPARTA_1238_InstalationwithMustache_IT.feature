@rest
Feature: [SPARTA_1238] Installation Sparta with mustache
  Background: Setup DCOS-CLI
    #Start SSH with DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'

  Scenario: [SPARTA-1161][01] Take Marathon-lb IP
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Then I run 'dcos task ${MARATHON_LB_TASK:-marathon-lb} | awk '{print $2}'| tail -n 1' in the ssh connection and save the value in environment variable 'marathonIP'
    Then I wait '1' seconds
    And I open a ssh connection to '!{marathonIP}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    And I run 'hostname | sed -e 's|\..*||'' in the ssh connection with exit status '0' and save the value in environment variable 'MarathonLbDns'

  @skipOnEnv(SKIP_POLICY=true)
  Scenario: [SPARTA-1161][01]Add zookeper-sparta policy to write in zookeper
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/zookeeper_policy.json' as 'json' with:
      |   $.name                  |  UPDATE    | ${ID_POLICY_ZK}       | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}  | n/a |
    Then the service response status must be '201'

  @runOnEnv(STRATIO_SPARTA_VERSION=2.0.0)
  Scenario: [SPARTA_1161][02] Cassiopeia Sparta Installation with Mustache in DCOS
    #Modify json to install specific configuration forSparta
    Given I create file 'spartamustache.json' based on 'schemas/dcosFiles/${SPARTA_JSON:-spartamustache-2.1.json}' as 'json' with:
      |   $.Framework.name                                    |  UPDATE     | ${DCOS_SERVICE_NAME}                                                    |n/a |
      |   $.Framework.environment_uri                         |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com                                  |n/a |
      |   $.Zookeeper.address                                 |  UPDATE     | ${ZK_URL}                                                               |n/a |
      |   $.Marathon-LB.nginx_proxy                           |  REPLACE    | ${NGINX_ACTIVE}                                                         |boolean |
      |   $.Marathon-LB.haproxy_host                          |  UPDATE     | !{MarathonLbDns}.labs.stratio.com                                       |n/a |
      |   $.Marathon.sparta_docker_image                      |  UPDATE     | ${DOCKER_URL}:${STRATIO_SPARTA_VERSION}                                 |n/a |
      |   $.Calico.enabled                                    |  REPLACE    | ${CALICOENABLED}                                                        |boolean |
      |   $.Hdfs.conf_uri                                     |  UPDATE     | ${CONF_HDFS_URI:-http://10.200.0.74:8085/}                              |n/a |
      |   $.Security.Components.oauth2_enabled                |  REPLACE    | ${AUTH_ENABLED}                                                         |boolean |
      |   $.Security.Components.gosec_enabled                 |  REPLACE    | ${AUTH_ENABLED}                                                         |boolean |
      |   $.Security.Components.crossdata_security_enabled    |  REPLACE    | ${AUTH_ENABLED}                                                         |boolean |
      |   $.Security.Components.marathon_enabled              |  REPLACE    | ${AUTH_ENABLED:-true}                                                   |boolean |

  #Last Version
  @skipOnEnv(STRATIO_SPARTA_VERSION=2.0.0)
  Scenario: [SPARTA_1161][02] Sparta Installation with Mustache in DCOS
    #Modify json to install specific configuration forSparta
    Given I create file 'spartamustache.json' based on 'schemas/dcosFiles/${SPARTA_JSON:-spartamustache-2.2.json}' as 'json' with:
      |   $.Framework.name                                    |  UPDATE     | ${DCOS_SERVICE_NAME}                                                    |n/a |
      |   $.Framework.environment_uri                         |  UPDATE     | https://${CLUSTER_ID}.labs.stratio.com                                  |n/a |
      |   $.Zookeeper.address                                 |  UPDATE     | ${ZK_URL}                                                               |n/a |
      |   $.Marathon-LB.nginx_proxy                           |  REPLACE    | ${NGINX_ACTIVE}                                                         |boolean |
      |   $.Marathon-LB.haproxy_host                          |  UPDATE     | !{MarathonLbDns}.labs.stratio.com                                       |n/a |
      |   $.Marathon.sparta_docker_image                      |  UPDATE     | ${DOCKER_URL}:${STRATIO_SPARTA_VERSION}                                 |n/a |
      |   $.Calico.enabled                                    |  REPLACE    | ${CALICOENABLED}                                                        |boolean |
      |   $.Hdfs.conf_uri                                     |  UPDATE     | ${CONF_HDFS_URI:-http://10.200.0.74:8085/}                              |n/a |
      |   $.Security.Components.oauth2_enabled                |  REPLACE    | ${AUTH_ENABLED}                                                         |boolean |
      |   $.Security.Components.gosec_enabled                 |  REPLACE    | ${AUTH_ENABLED}                                                         |boolean |
      |   $.Security.Components.crossdata_security_enabled    |  REPLACE    | ${AUTH_ENABLED}                                                         |boolean |
      |   $.Security.Components.marathon_enabled              |  REPLACE    | ${AUTH_ENABLED:-true}                                                   |boolean |
      |   $.Data-Governance.dg_enabled                        |  REPLACE    | ${GOV_ENABLED:-true}                                                    |boolean |
      |   $.Sparta-History.history_enabled                    |  REPLACE    | ${HISTORY_ENABLED:-true}                                                |boolean |
      |   $.Sparta-History.host                               |  UPDATE     | jdbc:postgresql://${POSTGRES_URL:-pg-0001.postgrestls.mesos}:${POSTGRES_PORT:-5432}   |n/a |
      |   $.Data-Governance.host                              |  UPDATE     | ${POSTGRES_URL:-pg-0001.postgrestls.mesos}                                              |n/a |
      |   $.Data-Governance.port                              |  UPDATE     | ${POSTGRES_PORT:-5432}                                                                   |n/a |
      |   $.Data-Governance.user                              |  UPDATE     |  ${DCOS_SERVICE_NAME}                                                                  |n/a |

    #Copy DEPLOY JSON to DCOS-CLI
    When I outbound copy 'target/test-classes/spartamustache.json' through a ssh connection to '/dcos'
    #Erase previous images for sparta
    Then I run 'rm -f /dcos/spartaBasicMarathon.json' in the ssh connection
    #Start image from mustache
    When I run 'dcos package describe --app --options=/dcos/spartamustache.json sparta >> /dcos/spartaBasicMarathon.json' in the ssh connection
    Then I run 'sed -i -e 's|"image":.*|"image": "${DOCKER_URL}:${STRATIO_SPARTA_VERSION}",|g' /dcos/spartaBasicMarathon.json' in the ssh connection
    And I run 'dcos marathon app add /dcos/spartaBasicMarathon.json' in the ssh connection


    And in less than '1200' seconds, checking each '20' seconds, the command output 'dcos task | grep -w ${DCOS_SERVICE_NAME}' contains '${DCOS_SERVICE_NAME}'
    #Get ip in marathon
    When I run 'dcos marathon task list /sparta/${DCOS_SERVICE_NAME}/${DCOS_SERVICE_NAME}  | awk '{print $5}' | grep ${DCOS_SERVICE_NAME} ' in the ssh connection and save the value in environment variable 'spartaTaskId'
    #Check sparta is runing in DCOS
    When  I run 'echo !{spartaTaskId}' in the ssh connection
    Then in less than '1200' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{spartaTaskId} | grep TASK_RUNNING' contains 'TASK_RUNNING'
    And in less than '1200' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{spartaTaskId} | grep healthCheckResults' contains 'healthCheckResults'
    And in less than '1200' seconds, checking each '10' seconds, the command output 'dcos marathon task show !{spartaTaskId} | grep  '"alive": true'' contains '"alive": true'

  @runOnEnv(AUTH_ENABLED=true)
  @skipOnEnv(SKIP_POLICY=true)
  Scenario: [SPARTA-1162][02]Add sparta policy for authorization in sparta
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/sp_policy_2.json' as 'json' with:
      |   $.id                    |  UPDATE    | spartap     | n/a |
      |   $.name                  |  UPDATE    | spartap     | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}     | n/a |
    Then the service response status must be '201'

    #mvn Example:
    #mvn verify -DCLUSTER_ID=nightly -DNGINX_ACTIVE=false -DDCOS_SERVICE_NAME=sparta-server -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.SPARTA_1238_InstalationwithMustache_IT -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nigthly.demo.stratio.com -DDOCKER_URL=qa.stratio.com/stratio/sparta -DCALICOENABLED=false -DHDFS_IP=10.200.0.74 -DROLE_SPARTA=open -DAUTH_ENABLED=true -DSTRATIO_SPARTA_VERSION=1.7.6 -DZK_URL=zk-0001-zkuserland.service.paas.labs.stratio.com:2181,zk-0002-zkuserland.service.paas.labs.stratio.com:2181,zk-0003-zkuserland.service.paas.labs.stratio.com:2181 -DDCOS_IP=10.200.0.21 -DSPARTA_NAME=sparta-server -DCROSSDATA_SERVER_CONFIG_SPARK_IMAGE=qa.stratio.com/stratio/stratio-spark:2.1.0.1 -DSPARTA_JSON=spartamustache_1.7.6.json -DHDFS_REALM=DEMO.STRATIO.COM