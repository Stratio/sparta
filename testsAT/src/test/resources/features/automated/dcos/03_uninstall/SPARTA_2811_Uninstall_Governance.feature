@rest
Feature: [Uninstall Governance with Command Center]

  Scenario:[Setup] Select folder and instance PG Comm Agent
    Given I run 'echo "${FOLDER_GOVERNANCE:-governance}/${DATAGOV_PG_COMM_AGENT_SERVICE_NAME:-dg-postgres-agent}" | sed 's/^[\/]*//'' locally and save the value in environment variable 'IdPGCommAgent'
    And I run 'echo "${FOLDER_GOVERNANCE:-governance}/${DATAGOV_PG_COMM_AGENT_SERVICE_NAME:-dg-postgres-agent}" | sed 's/^[\/]*//' | sed "s/\//\n/g" | tac | sed ':a;N;$!ba;s/\n/\./g'' locally and save the value in environment variable 'IdDCOSPGCommAgent'
    And I run 'echo "${FOLDER_GOVERNANCE:-governance}_${DATAGOV_PG_COMM_AGENT_SERVICE_NAME:-dg-postgres-agent}" | sed 's/^[\/]*//' | sed 's/^[_]*//'' locally and save the value in environment variable 'IdUninstall'

  Scenario:[01][QATM-1862] Uninstall PG Comm Agent
    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    And I open a ssh connection to '${DCOS_CLI_HOST:-dcos-cli.demo.labs.stratio.com}' with user '${CLI_BOOTSTRAP_USER:-root}' and password '${CLI_BOOTSTRAP_PASSWORD:-stratio}'
    When I send a 'DELETE' request to '/service/deploy-api/deploy/uninstall?app=/!{IdPGCommAgent}'
    # Check Uninstall in DCOS
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task | awk {'print $5'} | grep !{IdUninstall} | wc -l' contains '0'
    # Check Uninstall in CCT-API
    And in less than '200' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/all' so that the response does not contains '!{IdPGCommAgent}'

  Scenario:[Setup] Select folder and instance HDFS Agent
    Given I run 'echo "${FOLDER_GOVERNANCE:-governance}/${DATAGOV_HDFS_AGENT_SERVICE_NAME:-dg-hdfs-agent}" | sed 's/^[\/]*//'' locally and save the value in environment variable 'IdHDFSAgent'
    And I run 'echo "${FOLDER_GOVERNANCE:-governance}/${DATAGOV_HDFS_AGENT_SERVICE_NAME:-dg-hdfs-agent}" | sed 's/^[\/]*//' | sed "s/\//\n/g" | tac | sed ':a;N;$!ba;s/\n/\./g'' locally and save the value in environment variable 'IdDCOSHDFSAgent'
    And I run 'echo "${FOLDER_GOVERNANCE:-governance}_${DATAGOV_HDFS_AGENT_SERVICE_NAME:-dg-hdfs-agent}" | sed 's/^[\/]*//' | sed 's/^[_]*//'' locally and save the value in environment variable 'IdUninstall'

  Scenario:[01][QATM-1862] Uninstall HDFS Agent
    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    And I open a ssh connection to '${DCOS_CLI_HOST:-dcos-cli.demo.labs.stratio.com}' with user '${CLI_BOOTSTRAP_USER:-root}' and password '${CLI_BOOTSTRAP_PASSWORD:-stratio}'
    When I send a 'DELETE' request to '/service/deploy-api/deploy/uninstall?app=/!{IdHDFSAgent}'
    # Check Uninstall in DCOS
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task | awk {'print $5'} | grep !{IdUninstall} | wc -l' contains '0'
    # Check Uninstall in CCT-API
    And in less than '200' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/all' so that the response does not contains '!{IdHDFSAgent}'

  Scenario:[Setup] Select folder and instance Business Glossary API
    Given I run 'echo "${FOLDER_GOVERNANCE:-governance}/${BG_API_SERVICE_NAME:-dg-businessglossary-api}" | sed 's/^[\/]*//'' locally and save the value in environment variable 'IdBGAPI'
    And I run 'echo "${FOLDER_GOVERNANCE:-governance}/${BG_API_SERVICE_NAME:-dg-businessglossary-api}" | sed 's/^[\/]*//' | sed "s/\//\n/g" | tac | sed ':a;N;$!ba;s/\n/\./g'' locally and save the value in environment variable 'IdDCOSBGAPI'
    And I run 'echo "${FOLDER_GOVERNANCE:-governance}_${BG_API_SERVICE_NAME:-dg-businessglossary-api}" | sed 's/^[\/]*//' | sed 's/^[_]*//'' locally and save the value in environment variable 'IdUninstall'

  Scenario:[01][QATM-1862] Uninstall Business Glossary API
    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    And I open a ssh connection to '${DCOS_CLI_HOST:-dcos-cli.demo.labs.stratio.com}' with user '${CLI_BOOTSTRAP_USER:-root}' and password '${CLI_BOOTSTRAP_PASSWORD:-stratio}'
    When I send a 'DELETE' request to '/service/deploy-api/deploy/uninstall?app=/!{IdBGAPI}'
    # Check Uninstall in DCOS
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task | awk {'print $5'} | grep !{IdUninstall} | wc -l' contains '0'
    # Check Uninstall in CCT-API
    And in less than '200' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/all' so that the response does not contains '!{IdBGAPI}'

  Scenario:[Setup] Select folder and instance Governance UI
    Given I run 'echo "${FOLDER_GOVERNANCE:-governance}/${DATAGOV_UI_SERVICE_NAME:-governance-ui}" | sed 's/^[\/]*//'' locally and save the value in environment variable 'IdGovernanceUI'
    And I run 'echo "${FOLDER_GOVERNANCE:-governance}/${DATAGOV_UI_SERVICE_NAME:-governance-ui}" | sed 's/^[\/]*//' | sed "s/\//\n/g" | tac | sed ':a;N;$!ba;s/\n/\./g'' locally and save the value in environment variable 'IdDCOSGovernanceUI'
    And I run 'echo "${FOLDER_GOVERNANCE:-governance}_${DATAGOV_UI_SERVICE_NAME:-governance-ui}" | sed 's/^[\/]*//' | sed 's/^[_]*//'' locally and save the value in environment variable 'IdUninstall'

  Scenario:[01][QATM-1862] Uninstall Governance UI
    Given I authenticate to DCOS cluster '${DCOS_IP}' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/credentials/${PEM_FILE:-key.pem}'
    And I securely send requests to '${CLUSTER_ID}.${CLUSTER_DOMAIN:-labs.stratio.com}:443'
    And I open a ssh connection to '${DCOS_CLI_HOST:-dcos-cli.demo.labs.stratio.com}' with user '${CLI_BOOTSTRAP_USER:-root}' and password '${CLI_BOOTSTRAP_PASSWORD:-stratio}'
    When I send a 'DELETE' request to '/service/deploy-api/deploy/uninstall?app=/!{IdGovernanceUI}'
    # Check Uninstall in DCOS
    Then in less than '600' seconds, checking each '10' seconds, the command output 'dcos task | awk {'print $5'} | grep !{IdUninstall} | wc -l' contains '0'
    # Check Uninstall in CCT-API
    And in less than '200' seconds, checking each '20' seconds, I send a 'GET' request to '/service/deploy-api/deploy/status/all' so that the response does not contains '!{IdGovernanceUI}'