Feature: [SPARTA-1278] Add Initial Configuration for Sparta

  @loop(PRIVATE_AGENTS_LIST,AGENT_IP)
  Scenario:[SPARTA-1278][01]Create sparta user in all private agents
    Given I open a ssh connection to '<AGENT_IP>' with user 'root' and password 'stratio'
    When I run 'echo $(useradd ${DCOS_SERVICE_NAME})' in the ssh connection

  Scenario: [SPARTA-1278][02] Take Marathon-lb IP
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Then I run 'dcos task ${MARATHON_LB_TASK:-marathon-lb} | awk '{print $2}'| tail -n 1' in the ssh connection and save the value in environment variable 'marathonIP'
    Then I wait '1' seconds
    And I open a ssh connection to '!{marathonIP}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    And I run 'hostname | sed -e 's|\..*||'' in the ssh connection with exit status '0' and save the value in environment variable 'MarathonLbDns'

  @loop(MASTERS_LIST,MASTER_IP)
  @runOnEnv(REGISTERSERVICEOLD=true)
  Scenario:[SPARTA-1278][02] Register sparta tenant in cas (old version)
    Given I open a ssh connection to '<MASTER_IP>' with user 'root' and password 'stratio'
    When I run 'sed -i '/<\/util:list>/i <bean class="org.jasig.cas.support.oauth.services.OAuthRegisteredService" p:id="${IDNODE}" p:name="${DCOS_SERVICE_NAME}" p:description="A service for sparta-server running in DCOS" p:serviceId="https://!{MarathonLbDns}.labs.stratio.com/${DCOS_SERVICE_NAME}/login" p:bypassApprovalPrompt="true" p:clientId="${DCOS_SERVICE_NAME}-oauth-id" p:clientSecret="${CLIENTSECRET}"/>' ${URL_GOSEC:-/opt/stratio/gosec-sso/conf}/cas/spring-configuration/register-services.xml' in the ssh connection
    # Restart Gosec
    When I run 'systemctl restart  gosec-sso.service' in the ssh connection
    And I wait '15' seconds
    Then in less than '180' seconds, checking each '15' seconds, the command output 'consul watch -type=checks -http-addr=<MASTER_IP>\:8500 | jq  '.[] | .Name + " " + .Status'' contains 'Gosec SSO health check passing'

  @skipOnEnv(REGISTERSERVICEOLD=true)
  Scenario:[SPARTA-1278][02] Register sparta tenant in cas
    Given I run 'echo "${MASTERS_LIST}" | cut -d',' -f1' locally with exit status '0' and save the value in environment variable 'MASTER_IP'
    Given I open a ssh connection to '!{MASTER_IP}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Then  I run 'curl -XPOST -H "Content-Type: application/json" -d '{"clientId":"${DCOS_SERVICE_NAME}-oauth-id","clientSecret":"${CLIENTSECRET}","name":"${DCOS_SERVICE_NAME}","serviceId":"https://!{MarathonLbDns}.labs.stratio.com/${DCOS_SERVICE_NAME}/login","tenant":"NONE"}' --cert /opt/mesosphere/etc/pki/node.pem --key /opt/mesosphere/etc/pki/node.key https://master-1.node.paas.labs.stratio.com:9006/registry/services' in the ssh connection
    Then in less than '180' seconds, checking each '5' seconds, the command output 'curl https://master-1.node.paas.labs.stratio.com:9006/registry/services --cert /opt/mesosphere/etc/pki/node.pem --key /opt/mesosphere/etc/pki/node.key  | jq' contains '${DCOS_SERVICE_NAME}'

    # Mvn example:
    # mvn verify -DCLUSTER_ID=nightly -DURL_GOSEC=/opt/stratio/gosec-sso/conf -DDCOS_SERVICE_NAME=sparta-server -DMASTERS_LIST=10.200.0.156 -DPRIVATE_AGENTS_LIST=10.200.0.161,10.200.0.162,10.200.0.181,10.200.0.182,10.200.0.183,10.200.0.160 -DDCOS_CLI_HOST=dcos-nigthly.demo.stratio.com -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.SPARTA_1273_AddConfigurationForSparta_IT -DIDNODE=525
