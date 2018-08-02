@rest
Feature: [SPARTA-1678][Centralized Logging - Stdout & Stderr] Sparta installation with proper logging dispatch to stdout & stderr

  Background: Setup Paas rest client
    # Start SSH with DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    Given I set sso token using host '${CLUSTER_ID}.labs.stratio.com' with user 'admin' and password '1234' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.labs.stratio.com:443'
  @ignore @tillfixed(SPARTA-1705)
  Scenario:[SPARTA-1279][01] Force sparta error installing workflow without policy
    #include workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows' based on 'schemas/workflows/kafkaerror.json' as 'json' with:
      | id | DELETE | N/A|
    Then the service response status must be '200'
    And I save element '$.id' in environment variable 'previousWorkflowID'
    And I save element '$.name' in environment variable 'nameWorkflow'
    And I wait '10' seconds
  @ignore @tillfixed(SPARTA-1705)
  Scenario:[SPARTA-1279][02] Execute kafka-postgres workflow
    Given I send a 'POST' request to '/service/${DCOS_SERVICE_NAME}/workflows/run/!{previousWorkflowID}'
    Then the service response status must be '200' and its response must contain the text 'OK'


   #*****************************************
   # CENTRALIZED LOGGING IN SPARTA WORKFLOW *
   #*****************************************
  #Global check
  @ignore @tillfixed(SPARTA-1705)
  Scenario: [SPARTA-1678][3] Check global workflow aplication format
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log --lines 40 ${DCOS_SERVICE_NAME}_workflows_home_kafkaerror stderr | grep -e @message| grep -e "^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}T[0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}.[0-9]\{3\}[+-][0-9]\{2\}:[0-9]\{2\} ERROR .* .* .* .* .*$" | grep "Error reading child process output" | wc -l' contains '1'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log --lines 40 ${DCOS_SERVICE_NAME}_workflows_home_kafkaerror stderr | grep -e @message|  | grep -e "^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}T[0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}.[0-9]\{3\}[+-][0-9]\{2\}:[0-9]\{2\} \(INFO\|WARN\|DEBUG\|TRACE\) .* .* .* .* .*$" | wc -l' contains '0'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log ${DCOS_SERVICE_NAME}_workflows_home_kafkaerror stdout | grep -e "^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}T[0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}.[0-9]\{3\}[+-][0-9]\{2\}:[0-9]\{2\} ERROR .* .* .* .* .*$" | wc -l' contains '0'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log ${DCOS_SERVICE_NAME}_workflows_home_kafkaerror  stdout | grep -e "^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}T[0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}.[0-9]\{3\}[+-][0-9]\{2\}:[0-9]\{2\} \(INFO\|WARN\|DEBUG\|TRACE\) .* .* .* .* .*$" | wc -l | grep ^0$ | wc -l' contains '0'

  #Specific Check
  @ignore @tillfixed(SPARTA-1705)
  Scenario: [SPARTA-1678][4] Check specific workflow aplication format
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log --lines 40 ${DCOS_SERVICE_NAME}_workflows_home_kafkaerror stderr | grep -e "^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}T[0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}.[0-9]\{3\}[+-][0-9]\{2\}:[0-9]\{2\} ERROR .* .* .* .* {\"@message\":\".*\",\"@data\":{.*}}$" | grep "Error reading child process output" | wc -l' contains '1'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log --lines 40 ${DCOS_SERVICE_NAME}_workflows_home_kafkaerror stderr | grep -e "^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}T[0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}.[0-9]\{3\}[+-][0-9]\{2\}:[0-9]\{2\} \(INFO\|WARN\|DEBUG\|TRACE\) .* .* .* .* {\"@message\":\".*\",\"@data\":{.*}}$" | wc -l' contains '0'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log ${DCOS_SERVICE_NAME}_workflows_home_kafkaerror stdout | grep -e "^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}T[0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}.[0-9]\{3\}[+-][0-9]\{2\}:[0-9]\{2\} ERROR .* .* .* .* {\"@message\":\".*\",\"@data\":{.*}}$" | wc -l' contains '0'
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task log ${DCOS_SERVICE_NAME}_workflows_home_kafkaerror stdout | grep -e "^[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}T[0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}.[0-9]\{3\}[+-][0-9]\{2\}:[0-9]\{2\} \(INFO\|WARN\|DEBUG\|TRACE\) .* .* .* .* {\"@message\":\".*\",\"@data\":{.*}}$" | wc -l | grep ^0$ | wc -l' contains '0'

  @ignore @tillfixed(SPARTA-1705)
  Scenario:[SPARTA-1279][02] Execute kafka-postgres workflow
    Given I send a 'DELETE' request to '/service/${DCOS_SERVICE_NAME}/workflows/!{previousWorkflowID}'
    Then the service response status must be '200'
  
  #Example
  #mvn verify -DCLUSTER_ID=intbootstrap -DDCOS_SERVICE_NAME=sparta-server -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.SPARTA_1678_Centralized_Logging_IT.feature -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-aceptacion.demo.stratio.com -DDOCKER_URL=qa.stratio.com/stratio/sparta -DCALICOENABLED=false -DHDFS_IP=10.200.0.74 -DROLE_SPARTA=open -DAUTH_ENABLED=false -DSTRATIO_SPARTA_VERSION=2.0.0-M3 -DZK_URL=zk-0001.zkuserland.mesos:2181,zk-0002.zkuserland.mesos:2181,zk-0003.zkuserland.mesos:2181  -DCROSSDATA_SERVER_CONFIG_SPARK_IMAGE=qa.stratio.com/stratio/stratio-spark:2.2.0.4 -DSPARTA_JSON=spartamustache.json -DHDFS_REALM=DEMO.STRATIO.COM -DNGINX_ACTIVE=false -DPRIVATE_AGENTS_LIST=10.200.1.88,10.200.1.86,10.200.1.87,10.200.1.63,10.200.0.221