@rest
Feature: [QATM_1863] Upload Sparta dockers in enviroment
    Background: [SetUp] Copy PEM file to DCOS-CLI
        Given I open a ssh connection to '${DCOS_CLI_HOST}' with user '${DCOS_CLI_USER:-root}' and password '${DCOS_CLI_PASSWORD:-stratio}'
        Then I run 'mkdir -p /src/test/resources/credentials' in the ssh connection
        And I outbound copy '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}' through a ssh connection to '/src/test/resources/credentials'
        And I run 'chmod 400 /src/test/resources/credentials/key.pem' in the ssh connection

    @skipOnEnv(SPECIFIC_COMMAND)
    Scenario: [01][SetUp] Downloading docker images (Sparta, Spark) in all agent nodes
        Then I execute the command 'sudo docker pull ${SPARK_DOCKER_IMAGE:-qa.stratio.com/stratio/spark-stratio-driver}:${STRATIO_SPARK_VERSION:-2.2.0-2.3.0-7180ac5}' in all the nodes of my cluster with user '${BOOTSTRAP_USER:-operador}' and pem '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
        And I execute the command 'sudo docker pull ${SPARTA_DOCKER_IMAGE:-qa.stratio.com/stratio/sparta}:${STRATIO_SPARTA_VERSION:-2.6.0-60ced2e}' in all the nodes of my cluster with user '${BOOTSTRAP_USER:-operador}' and pem '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'

    @runOnEnv(SPECIFIC_COMMAND)
    Scenario: [01][SetUp] Downloading docker images (Sparta, Spark) in all agent nodes
        Then I execute the command '${SPECIFIC_COMMAND}' in all the nodes of my cluster with user '${BOOTSTRAP_USER:-operador}' and pem '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'

