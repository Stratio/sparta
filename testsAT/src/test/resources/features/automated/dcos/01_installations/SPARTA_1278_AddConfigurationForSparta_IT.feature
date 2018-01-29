Feature: [SPARTA-1278] Add Initial Configuration for Sparta

  @loop(PRIVATE_AGENTS_LIST,AGENT_IP)
  Scenario:[SPARTA-1278][01]Create sparta-server user in all private agents
    Given I open a ssh connection to '<AGENT_IP>' with user 'root' and password 'stratio'
    When I run 'echo $(useradd ${DCOS_SERVICE_NAME})' in the ssh connection

  @loop(MASTERS_LIST,MASTER_IP)
  Scenario:[SPARTA-1278][02]Modify registerService.xml for sparta Auth in all master agents
    Given I open a ssh connection to '<MASTER_IP>' with user 'root' and password 'stratio'
    #add sparta-node in RegisterService.xml
    When I run 'sed -i '/<\/util:list>/i <bean class="org.jasig.cas.support.oauth.services.OAuthRegisteredService" p:id="${IDNODE}" p:name="${DCOS_SERVICE_NAME}" p:description="A service for sparta-server running in DCOS" p:serviceId="https://sparta.${CLUSTER_ID}.labs.stratio.com/${DCOS_SERVICE_NAME}/login" p:bypassApprovalPrompt="true" p:clientId="${DCOS_SERVICE_NAME}-oauth-id" p:clientSecret="${CLIENTSECRET}"/>' /opt/stratio/gosec-sso/conf/cas/spring-configuration/register-services.xml' in the ssh connection
    # Restart Gosec
    When I run 'echo $(systemctl restart  gosec-sso.service)' in the ssh connection
    Then I wait '15' seconds
    # Mvn example:
    # mvn verify -DCLUSTER_ID=nightly -DDCOS_SERVICE_NAME=sparta-server -DMASTERS_LIST=10.200.0.156 -DPRIVATE_AGENTS_LIST=10.200.0.161,10.200.0.162,10.200.0.181,10.200.0.182,10.200.0.183,10.200.0.160 -DDCOS_CLI_HOST=dcos-nigthly.demo.stratio.com -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.SPARTA_1273_AddConfigurationForSparta_IT -DIDNODE=525
    # mvn verify -DCLUSTER_ID=newcore -DDCOS_SERVICE_NAME=sparta-dg -DCLIENTSECRET=LBNuJVgfsb-WHEkP83zt -DMASTERS_LIST=10.200.1.244,10.200.1.30,10.200.1.236 -DPRIVATE_AGENTS_LIST=10.200.1.238,10.200.1.173,10.200.1.53,10.200.1.51,10.200.1.184 -DDCOS_CLI_HOST=dcos-nigthly.demo.stratio.com -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.SPARTA_1273_AddConfigurationForSparta_IT -DIDNODE=526
    # mvn verify -DCLUSTER_ID=megadev -DDCOS_SERVICE_NAME=sparta-dg -DCLIENTSECRET=LBNuJVgfsb-WHEkP83zt -DMASTERS_LIST=10.200.0.21 -DPRIVATE_AGENTS_LIST=10.200.0.54,10.200.0.55,10.200.0.56,10.200.0.57,10.200.0.58,10.200.0.59,10.200.0.60,10.200.0.61,10.200.0.69,10.200.0.70,10.200.0.71,10.200.0.72 -DDCOS_CLI_HOST=dcos-nigthly.demo.stratio.com -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.SPARTA_1273_AddConfigurationForSparta_IT -DIDNODE=527

