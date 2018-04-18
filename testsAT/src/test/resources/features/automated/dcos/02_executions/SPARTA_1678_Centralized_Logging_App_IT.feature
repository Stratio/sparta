@rest
Feature: [SPARTA-1678][Centralized Logging - Stdout & Stderr] Sparta installation with proper logging dispatch to stdout & stderr

  Background: Setup Paas rest client and set sso token
    # Start SSH with DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user 'admin' and password '1234'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'

  Scenario: [SPARTA-1161][01] Delete Zookeper Policy
    When I send a 'DELETE' request to '/service/gosecmanagement/api/policy/${ID_POLICY_ZK}'
    Then the service response status must be '200'
    And I wait '10' seconds

  Scenario:[SPARTA-1279][02] Execute kafka-postgres workflow without zookeper authorization
    Given I send a 'GET' request to '/service/${DCOS_SERVICE_NAME}/workflows'
    Then the service response status must be '500'

   #*********************************************
   # CENTRALIZED LOGGING IN SPARTA APLICATION *
   #*********************************************

#  #Check error in stderr only with errors
  Scenario:[SPARTA-1678][03] Check Sparta log (stdout & stderr)
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log --lines 80 sparta_${DCOS_SERVICE_NAME}_${DCOS_SERVICE_NAME} stderr |grep -e @message | grep -e ERROR | grep ""KeeperErrorCode = NoAuth for /stratio/sparta/${DCOS_SERVICE_NAME}/workflows" | wc -l' contains '1'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log --lines 80 sparta_${DCOS_SERVICE_NAME}_${DCOS_SERVICE_NAME} stderr |grep -e @message | grep -e DEBUG | grep -e @message| wc -l' contains '0'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log --lines 80 sparta_${DCOS_SERVICE_NAME}_${DCOS_SERVICE_NAME} stderr |grep -e @message | grep -e INFO  |grep -e @message| wc -l' contains '0'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log --lines 80 sparta_${DCOS_SERVICE_NAME}_${DCOS_SERVICE_NAME} stderr |grep -e @message | grep -e WARN  |grep -e @message| wc -l' contains '0'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log ${DCOS_SERVICE_NAME} stdout | grep -e ERROR | wc -l' contains '0'

#  #Global check
  Scenario: [SPARTA-1678][04] Check global Sparta aplication format
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log --lines 80 sparta_${DCOS_SERVICE_NAME}_${DCOS_SERVICE_NAME} stderr | grep -e @message| grep -e "^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}T[0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}.[0-9]\{3\}[+-][0-9]\{2\}:[0-9]\{2\} ERROR .* .* .* .* .*$" | grep "KeeperErrorCode = NoAuth for /stratio/sparta/${DCOS_SERVICE_NAME}/workflows" | wc -l' contains '1'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log --lines 80 sparta_${DCOS_SERVICE_NAME}_${DCOS_SERVICE_NAME} stderr | grep -e @message|  | grep -e "^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}T[0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}.[0-9]\{3\}[+-][0-9]\{2\}:[0-9]\{2\} \(INFO\|WARN\|DEBUG\|TRACE\) .* .* .* .* .*$" | wc -l' contains '0'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log sparta_${DCOS_SERVICE_NAME}_${DCOS_SERVICE_NAME} stdout | grep -e "^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}T[0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}.[0-9]\{3\}[+-][0-9]\{2\}:[0-9]\{2\} ERROR .* .* .* .* .*$" | wc -l' contains '0'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log sparta_${DCOS_SERVICE_NAME}_${DCOS_SERVICE_NAME}  stdout | grep -e "^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}T[0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}.[0-9]\{3\}[+-][0-9]\{2\}:[0-9]\{2\} \(INFO\|WARN\|DEBUG\|TRACE\) .* .* .* .* .*$" | wc -l | grep ^0$ | wc -l' contains '0'

#  #Specific Check
  Scenario: [SPARTA-1678][05] Check specific Sparta aplication format
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log --lines 80 sparta_${DCOS_SERVICE_NAME}_${DCOS_SERVICE_NAME} stderr | grep -e "^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}T[0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}.[0-9]\{3\}[+-][0-9]\{2\}:[0-9]\{2\} ERROR .* .* .* .* {\"@message\":\".*\",\"@data\":{.*}}$" | grep "KeeperErrorCode = NoAuth for /stratio/sparta/${DCOS_SERVICE_NAME}/workflows" | wc -l' contains '1'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log --lines 80 sparta_${DCOS_SERVICE_NAME}_${DCOS_SERVICE_NAME} stderr | grep -e "^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}T[0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}.[0-9]\{3\}[+-][0-9]\{2\}:[0-9]\{2\} \(INFO\|WARN\|DEBUG\|TRACE\) .* .* .* .* {\"@message\":\".*\",\"@data\":{.*}}$" | wc -l' contains '0'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log sparta_${DCOS_SERVICE_NAME}_${DCOS_SERVICE_NAME} stdout | grep -e "^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}T[0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}.[0-9]\{3\}[+-][0-9]\{2\}:[0-9]\{2\} ERROR .* .* .* .* {\"@message\":\".*\",\"@data\":{.*}}$" | wc -l' contains '0'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log sparta_${DCOS_SERVICE_NAME}_${DCOS_SERVICE_NAME} stdout | grep -e "^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}T[0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}.[0-9]\{3\}[+-][0-9]\{2\}:[0-9]\{2\} \(INFO\|WARN\|DEBUG\|TRACE\) .* .* .* .* {\"@message\":\".*\",\"@data\":{.*}}$" | wc -l | grep ^0$ | wc -l' contains '0'

  Scenario: [SPARTA-1162][06] Restore zookeper-sparta policy to write in zookeper
    Given I send a 'POST' request to '/service/gosecmanagement/api/policy' based on 'schemas/gosec/zookeeper_policy.json' as 'json' with:
      |   $.id                    |  UPDATE    | ${ID_POLICY_ZK}       | n/a |
      |   $.name                  |  UPDATE    | ${ID_POLICY_ZK}       | n/a |
      |   $.users[0]              |  UPDATE    | ${DCOS_SERVICE_NAME}  | n/a |
    Then the service response status must be '201'
    And I wait '5' seconds

#  #Example
#  # mvn verify -DCLUSTER_ID=intbootstrap -DDCOS_SERVICE_NAME=sparta-dg -Dit.test=costratio.sparta.testsAT.automated.dcos.executions.SPARTA_1678_Centralized_Loggin_IT -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-aceptacion.demo.stratio.com -DDOCKER_URL=qa.stratio.com/stratio/sparta -DCALICOENABLED=false -DHDFS_IP=10.200.0.74 -DROLE_SPARTA=open -DAUTH_ENABLED=false -DSTRATIO_SPARTA_VERSION=2.0.0-M3 -DZK_URL=zk-0001.zkuserland.mesos:2181,zk-0002.zkuserland.mesos:2181,zk-0003.zkuserland.mesos:2181  -DCROSSDATA_SERVER_CONFIG_SPARK_IMAGE=qa.stratio.com/stratio/stratio-spark:2.2.0.4 -DSPARTA_JSON=spartamustache.json -DHDFS_REALM=DEMO.STRATIO.COM -DNGINX_ACTIVE=false -DPRIVATE_AGENTS_LIST=10.200.1.88,10.200.1.86,10.200.1.87,10.200.1.63,10.200.0.221 -DSPARTA_INSTALATION=true
