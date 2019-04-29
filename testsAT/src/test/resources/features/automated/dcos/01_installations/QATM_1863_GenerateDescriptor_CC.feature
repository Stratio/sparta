@rest
Feature: [QATM_1863] Generate New CommandCenter Descriptor with Special Configuration

  Scenario: [QATM-1863] Generate New Descriptor for CommandCenter
    Given I set sso token using host '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}' with user '${USER:-admin}' and password '${PASSWORD:-1234}' and tenant 'NONE'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    # Obtain Descriptor
    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${BOOTSTRAP_USER:-operador}' and pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    Then I run 'curl -k -s -X GET -H 'Cookie:dcos-acs-auth-cookie=!{dcosAuthCookie}' https://${CLUSTER_ID:-nightly}.${CLUSTER_DOMAIN:-labs.stratio.com}:443/service/deploy-api/universe/sparta/${SPARTA_FLAVOUR}/descriptor | jq .> target/test-classes/schemas/sparta-descriptor.json' locally

    Given I open a ssh connection to '${BOOTSTRAP_IP}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    And I run 'cat /stratio_volume/descriptor.json | jq .nodes[0].id | sed 's/\"//g'' in the ssh connection and save the value in environment variable 'master'

    When I send a 'POST' request to '/service/deploy-api/universe/sparta/${SPARTA_FLAVOUR}-auto8/descriptor' based on 'schemas/sparta-descriptor.json' as 'json' with:
      | $.data.model                                 | REPLACE | ${SPARTA_FLAVOUR}-auto8                                                  | string |
      | $.parameters.properties.security.properties.marathonSsoClientId.default       | REPLACE | adminrouter_paas-!{master}.node.${CLUSTER_DOMAIN:-labs.stratio.com}                                                  | string |
      | $.data.container.runners[0].image       | REPLACE | ${DOCKER_URL:-qa.stratio.com/stratio/sparta}:${STRATIO_SPARTA_VERSION:-2.6.0-SNAPSHOT}                                                   | string |
      | $.parameters.properties.settings.properties.spartaDockerImage.default       | REPLACE | ${DOCKER_URL:-qa.stratio.com/stratio/sparta}:${STRATIO_SPARTA_VERSION:-2.6.0-SNAPSHOT}              | string |
    Then the service response status must be '201'



