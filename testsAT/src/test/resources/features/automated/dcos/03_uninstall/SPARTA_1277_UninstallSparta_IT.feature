@rest
Feature: [SPARTA-1277] Uninstall Sparta with mustache
  Background: Setup DCOS-CLI
    #Start SSH with DCOS-CLI
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'

  Scenario: [SPARTA-1277][01] Remove Installation with full security in DCOS
    When  I run 'dcos marathon app remove /sparta/${DCOS_SERVICE_NAME}/${DCOS_SERVICE_NAME}' in the ssh connection
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task | grep ${DCOS_SERVICE_NAME} | wc -l' contains '0'

  @runOnEnv(AUTH_ENABLED=true)
  @skipOnEnv(REGISTERSERVICEOLD=true)
  Scenario: [SPARTA-1277][02] Unregister service from cas
    Given I run 'echo "${MASTERS_LIST}" | cut -d',' -f1' locally with exit status '0' and save the value in environment variable 'MASTER_IP'
    Then I open a ssh connection to '!{MASTER_IP}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    #recover id cas of service
    Given  I run 'curl -s https://master-1.node.paas.labs.stratio.com:9006/registry/services --cert /opt/mesosphere/etc/pki/node.pem --key /opt/mesosphere/etc/pki/node.key  | jq '.[]  | select(.name == "${DCOS_SERVICE_NAME}").id' in the ssh connection and save the value in environment variable 'idCas'
    #delete service
    Then  I run 'curl -XDELETE https://master-1.node.paas.labs.stratio.com:9006/registry/services/!{idCas} -H "Content-Type: application/json" --cert /opt/mesosphere/etc/pki/node.pem --key /opt/mesosphere/etc/pki/node.key' in the ssh connection