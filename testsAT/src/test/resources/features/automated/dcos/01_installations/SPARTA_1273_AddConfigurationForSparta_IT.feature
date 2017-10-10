Feature: [SPARTA-1273]Add Initial Configuration for Sparta

  @loop(PRIVATE_AGENTS_LIST,AGENT_IP)
  Scenario:[SPARTA-1273][Scenario-1][01][Setup]Create sparta-server user in all private agents
    Given I open a ssh connection to '<AGENT_IP>' with user 'root' and password 'stratio'
    When I run 'echo $(useradd ${DCOS_SERVICE_NAME})' in the ssh connection

  @loop(MASTERS_LIST,MASTER_IP)
  Scenario:[SPARTA-1273][Scenario-1][01][Setup]Modify registerService.xml for sparta Auth in all master agents
    Given I open a ssh connection to '<MASTER_IP>' with user 'root' and password 'stratio'
    #add sparta-node in RegisterService.xml
    When I run 'sed -i '/<\/util:list>/i <bean class="org.jasig.cas.support.oauth.services.OAuthRegisteredService" p:id="523" p:name="${DCOS_SERVICE_NAME}" p:description="A service for sparta-server running in DCOS" p:serviceId="https://sparta.${CLUSTER_ID}.labs.stratio.com/sparta-server/login" p:bypassApprovalPrompt="true" p:clientId="${DCOS_SERVICE_NAME}-oauth-id" p:clientSecret="cr7gDH6hX2-C3SBZYWj8F"/>' /etc/sds/gosec-sso/cas/spring-configuration/register-services.xml' in the ssh connection
    #Restart Gosec
    And I run 'echo $(service gosec-sso restart)' in the ssh connection

    # Mvn example:
    # mvn verify -DCLUSTER_ID=nightly -DAGENT_IP= -DDCOS_SERVICE_NAME=sparta-server -DMASTERS_LIST=10.200.0.156 -DPRIVATE_AGENTS_LIST=10.200.0.161 -DDCOS_CLI_HOST=dcos-nigthly.demo.stratio.com -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.SPARTA_1273_AddConfigurationForSparta_IT
    # mvn verify -DCLUSTER_ID=newcore -DAGENT_IP= -DDCOS_SERVICE_NAME=sparta-server -DMASTERS_LIST=10.200.1.244,10.200.1.30,10.200.1.236 -DPRIVATE_AGENTS_LIST=10.200.1.238,10.200.1.173,10.200.1.53,10.200.1.51,10.200.1.184 -DDCOS_CLI_HOST=dcos-nigthly.demo.stratio.com -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.SPARTA_1273_AddConfigurationForSparta_IT

