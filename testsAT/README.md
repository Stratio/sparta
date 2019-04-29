# README

## REQUISITES
 
### - dcos-cli

To launch a `dcos-cli` this command has to be executed:

```
docker run -d --name dcos-cli -e DCOS_IP=<MASTER_IP> -e SSL=true -e SSH=true -e TOKEN_AUTHENTICATION=true -e DCOS_USER=admin -e DCOS_PASSWORD=1234 -e CLI_BOOTSTRAP_USER=root -e CLI_BOOTSTRAP_PASSWORD=stratio qa.stratio.com/stratio/dcos-cli:0.4.15-SNAPSHOT
```
Example:
`docker run -dit --name dcos-newmegadev -e DCOS_IP=10.130.8.11 -e SSL=true -e SSH=true -e TOKEN_AUTHENTICATION=true -e DCOS_USER=admin -e DCOS_PASSWORD=1234 -e CLI_BOOTSTRAP_USER=root -e CLI_BOOTSTRAP_PASSWORD=stratio qa.stratio.com/stratio/dcos-cli:0.4.15`

## ACCEPTANCE TESTS

Cucumber automated and manual acceptance tests.
This module depends on a QA library (stratio-test-bdd), where common logic and steps are implemented.

## EXECUTION

These tests will be executed as part of the continuous integration flow as follows:

mvn verify [-D\<ENV_VAR>=\<VALUE>] [-Dit.test=\<TEST_TO_EXECUTE>|-Dgroups=\<GROUP_TO_EXECUTE>]

Examples:

- Sparta Instalations with Command Center:
 `Nightly: mvn verify -Dgroups=Installation_CC -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nightly -DCONF_HDFS_URI=http://10.200.0.74:8085/ -DSPARTA_FLAVOUR=hydra -DPOSTGRES_HOST=pg-0001.postgrestls.mesos -DSKIP_USERS=TRUE -DCLUSTER_ID=nightly -DCLUSTER_DOMAIN=labs.stratio.com -DMARATHON_LB_TASK=marathon-lb-sec -DPOSTGRES_NAME=postgrestls -DADDUSER_PRIVATE_NODES=true -DPRIVATE_AGENTS_LIST=10.200.0.161,10.200.0.162,10.200.0.181,10.200.0.182,10.200.0.183,10.200.0.184,10.200.0.185,10.200.0.194 -DDCOS_SERVICE_NAME=sparta-server -DBOOTSTRAP_IP=10.200.0.155 -DDCOS_IP=10.200.0.156 -DSTRATIO_SPARTA_VERSION=2.6.0-96de718 -DINTERNAL_HDFS_FRAMEWORK:true`
 `Fullerenos: mvn verify -Dgroups=Installation_CC -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-fulle1 -DCONF_HDFS_URI=http://api.hdfs-example.marathon.l4lb.thisdcos.directory/v1/config -DSPARTA_FLAVOUR=hydra -DPOSTGRES_HOST=pg-0001.postgresqa.mesos -DCLUSTER_ID=fulle1 -DCLUSTER_DOMAIN=labs.stratio.com -DMARATHON_LB_TASK=marathonlb -DPOSTGRES_NAME=postgresqa -DADDUSER_PRIVATE_NODES=true -DPRIVATE_AGENTS_LIST=10.200.0.211,10.200.0.227,10.200.0.228,10.200.0.230,10.200.1.131,10.200.1.175,10.200.1.182,10.200.1.225,10.200.1.234,10.200.1.44,10.200.1.46,10.200.1.58,10.200.1.80,10.200.1.82,10.200.1.85 -DDCOS_SERVICE_NAME=sparta-test -DBOOTSTRAP_IP=10.200.1.99 -DDCOS_IP=10.200.1.102 -DSTRATIO_SPARTA_VERSION=2.6.0-96de718 -DDCOS_SERVICE_NAME=sparta-26 -DZOOKEEPER_NAME=zookeeper -DID_SPARTA_POSTGRES=postgres-26 -DID_POLICY_ZK=zk-26 -DSKIP_USERS=true `
 `Rocket: mvn verify -Dgroups=Installation_CC -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-rocket -DSPARTA_FLAVOUR=hydra -DPOSTGRES_HOST=pg-0001.postgrestls.mesos -DSKIP_USERS=TRUE -DCLUSTER_ID=bootstrap -DCLUSTER_DOMAIN=rocket.hetzner.stratio.com  -DPOSTGRES_NAME=postgrestls -DDCOS_SERVICE_NAME=sparta-server -DBOOTSTRAP_IP=10.130.15.2 -DDCOS_IP=10.130.15.11 -DSTRATIO_SPARTA_VERSION=2.6.0 -DBOOTSTRAP_USER=automaticuser -DSKIP_ADD_DROP_DATABASE -DPGBOUNCER_ENABLE=true -DINTERNAL_HDFS_FRAMEWORK=true`
 `mvn verify -Dgroups=Installation_CC -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nightly -DCONF_HDFS_URI=http://10.200.0.74:8085/ -DSPARTA_FLAVOUR=hydra -DPOSTGRES_HOST=pg-0001.postgrestls.mesos -DSKIP_USERS=TRUE -DCLUSTER_ID=nightly -DCLUSTER_DOMAIN=labs.stratio.com -DMARATHON_LB_TASK=marathon-lb-sec -DPOSTGRES_NAME=postgrestls -DADDUSER_PRIVATE_NODES=true -DPRIVATE_AGENTS_LIST=10.200.0.161,10.200.0.162,10.200.0.181,10.200.0.182,10.200.0.183,10.200.0.184,10.200.0.185,10.200.0.194 -DDCOS_SERVICE_NAME=sparta-server -DBOOTSTRAP_IP=10.200.0.155 -DDCOS_IP=10.200.0.156`
 `mvn verify -Dgroups=Installation_CC -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-newcore -DCONF_HDFS_URI=http://10.130.8.7:8000/ -DSPARTA_FLAVOUR=hydra -DPOSTGRES_HOST=pg-0001.postgreseos.mesos -DID_POLICY_ZK=zk1test -DSKIP_USERS=TRUE -DCLUSTER_ID=bootstrap -DCLUSTER_DOMAIN=newmegadev.hetzner.stratio.com -DMARATHON_LB_TASK=marathonlb -DPOSTGRES_NAME=postgreseos -DADDUSER_PRIVATE_NODES=true -DPRIVATE_AGENTS_LIST=10.130.8.101,10.130.8.102,10.130.8.103,10.130.8.104,10.130.8.105,10.130.8.106,10.130.8.107,10.130.8.108,10.130.8.109,10.130.8.110,10.130.8.111,10.130.8.112,10.130.8.21,10.130.8.22`
 `mvn verify -Dgroups=Installation_CC -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-aceptacion -DCONF_HDFS_URI=http://10.200.0.74:8085/ -DSPARTA_FLAVOUR=orion -DPOSTGRES_HOST=pg-0001.postgrestls.mesos -DID_POLICY_ZK=zk1test -DSKIP_USERS=TRUE -DCLUSTER_ID=bootstrap -DCLUSTER_DOMAIN=labs.stratio.com -DMARATHON_LB_TASK=marathonlb -DPOSTGRES_NAME=postgrestls -DADDUSER_PRIVATE_NODES=true -DPRIVATE_AGENTS_LIST=10.200.0.161,10.200.0.162,10.200.0.181,10.200.0.182,10.200.0.183,10.200.0.184,10.200.0.185,10.200.0.194 -DDCOS_SERVICE_NAME=sparta-server`
 
- Sparta Uninstall with Command Center:
 `Nightly:mvn verify -Dgroups=uninstall_CC -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nightly -DSPARTA_FLAVOUR=hydra -DPOSTGRES_HOST=pg-0001.postgrestls.mesos -DSKIP_USERS=TRUE -DCLUSTER_ID=nightly -DCLUSTER_DOMAIN=labs.stratio.com -DMARATHON_LB_TASK=marathon-lb-sec -DPOSTGRES_NAME=postgrestls -DDCOS_SERVICE_NAME=sparta-server`
 `Rocket:mvn verify -Dgroups=uninstall_CC -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-rocket -DSPARTA_FLAVOUR=hydra -DPOSTGRES_HOST=pg-0001.postgrestls.mesos -DSKIP_USERS=TRUE -DCLUSTER_ID=bootstrap -DCLUSTER_DOMAIN=rocket.hetzner.stratio.com -DPOSTGRES_NAME=postgrestls -DDCOS_SERVICE_NAME=sparta-server -DBOOTSTRAP_USER=automaticuser -DSKIP_DROP_DATABASE=true`

- Nighly execution:
 `mvn verify -Dgroups=nightly -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nightly -DCONF_HDFS_URI=http://10.200.0.74:8085/ -DSPARTA_FLAVOUR=hydra -DPOSTGRES_HOST=pg-0001.postgrestls.mesos -DSKIP_USERS=TRUE -DCLUSTER_ID=nightly -DCLUSTER_DOMAIN=labs.stratio.com -DMARATHON_LB_TASK=marathon-lb-sec -DPOSTGRES_NAME=postgrestls -DADDUSER_PRIVATE_NODES=true -DPRIVATE_AGENTS_LIST=10.200.0.161,10.200.0.162,10.200.0.181,10.200.0.182,10.200.0.183,10.200.0.184,10.200.0.185,10.200.0.194 -DDCOS_SERVICE_NAME=sparta-server -DBOOTSTRAP_IP=10.200.0.155 -DDCOS_IP=10.200.0.156 -DSELENIUM_GRID=sl.demo.stratio.com:4444 -DFORCE_BROWSER=chrome_64sparta -DSKIP_ADDROLE=true`
  `mvn verify -Dgroups=nightly -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nightly -DCONF_HDFS_URI=http://10.200.0.74:8085/ -DSPARTA_FLAVOUR=hydra -DPOSTGRES_HOST=pg-0001.postgrestls.mesos -DSKIP_USERS=TRUE -DCLUSTER_ID=nightly -DCLUSTER_DOMAIN=labs.stratio.com -DMARATHON_LB_TASK=marathon-lb-sec -DPOSTGRES_NAME=postgrestls -DADDUSER_PRIVATE_NODES=true -DPRIVATE_AGENTS_LIST=10.200.0.161,10.200.0.162,10.200.0.181,10.200.0.182,10.200.0.183,10.200.0.184,10.200.0.185,10.200.0.194 -DDCOS_SERVICE_NAME=sparta-server -DBOOTSTRAP_IP=10.200.0.155 -DDCOS_IP=10.200.0.156 -DSELENIUM_GRID=sl.demo.stratio.com:4444 -DFORCE_BROWSER=chrome_64sparta -DSKIP_ADDROLE=true -DSTRATIO_SPARTA_VERSION=2.6.0-96de718`

- Rocket execution:
`Rocket:  mvn verify -Dgroups=rocket -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-rocket -DSPARTA_FLAVOUR=hydra -DPOSTGRES_HOST=pg-0001.postgrestls.mesos -DSKIP_USERS=TRUE -DCLUSTER_ID=bootstrap -DCLUSTER_DOMAIN=rocket.hetzner.stratio.com -DPOSTGRES_NAME=postgrestls -DDCOS_SERVICE_NAME=sparta-auto -DBOOTSTRAP_IP=10.130.15.2 -DDCOS_IP=10.130.15.11 -DSTRATIO_SPARTA_VERSION=2.6.0 -DBOOTSTRAP_USER=automaticuser -DSKIP_ADD_DROP_DATABASE -DPGBOUNCER_ENABLE=true -DSELENIUM_GRID=sl.demo.stratio.com:4444 -DFORCE_BROWSER=chrome_64sparta -DSPARTA-USER=admin -DSPARTA-PASSWORD=1234 -DINTERNAL_HDFS_FRAMEWORK=true
`

- Generate new CommandCenter Descriptor:
 `mvn verify -Dgroups=GenerateDescriptor_CC -DlogLevel=DEBUGDDCOS_CLI_HOST=dcos-nightly -DCONF_HDFS_URI=http://10.200.0.74:8085/ -DSPARTA_FLAVOUR=hydra -DPOSTGRES_HOST=pg-0001.postgrestls.mesos -DCLUSTER_ID=nightly -DCLUSTER_DOMAIN=labs.stratio.com -DDCOS_SERVICE_NAME=sparta-server -DDOCKER_URL=qa.stratio.com/stratio/sparta -DSTRATIO_SPARTA_VERSION=2.6.0-SNAPSHOT -DBOOTSTRAP_IP=10.200.0.155 -DDCOS_IP=10.200.0.156`

- Execute Workflow E2E -Workflows Streaming
 `mvn verify -Dgroups=postgres_kafka_fullsec -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nightly -DSPARTA_FLAVOUR=orion -DPOSTGRES_HOST=pg-0001.postgrestls.mesos -DSKIP_USERS=TRUE -DCLUSTER_ID=nightly -DCLUSTER_DOMAIN=labs.stratio.com -DMARATHON_LB_TASK=marathon-lb-sec -DPOSTGRES_NAME=postgrestls -DADDUSER_PRIVATE_NODES=true -DDCOS_SERVICE_NAME=sparta-server -DSELENIUM_GRID=sl.demo.stratio.com:4444 -DFORCE_BROWSER=chrome_64sparta`

- Execute Batch E2E - Workflows Batch
`mvn verify -Dgroups=batch -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nightly -DCONF_HDFS_URI=http://10.200.0.74:8085/ -DSPARTA_FLAVOUR=hydra -DPOSTGRES_HOST=pg-0001.postgrestls.mesos -DSKIP_USERS=TRUE -DCLUSTER_ID=nightly -DCLUSTER_DOMAIN=labs.stratio.com -DMARATHON_LB_TASK=marathon-lb-sec -DPOSTGRES_NAME=postgrestls -DADDUSER_PRIVATE_NODES=true -DPRIVATE_AGENTS_LIST=10.200.0.161,10.200.0.162,10.200.0.181,10.200.0.182,10.200.0.183,10.200.0.184,10.200.0.185,10.200.0.194 -DDCOS_SERVICE_NAME=sparta-server -DBOOTSTRAP_IP=10.200.0.155 -DDCOS_IP=10.200.0.156 -DSELENIUM_GRID=sl.demo.stratio.com:4444 -DFORCE_BROWSER=chrome_64sparta -DSKIP_ADDROLE=true`

- Create custom topic in Kafka
`mvn clean verify -Dgroups=dcos_pnf -DCLUSTER_ID=fulle1 -DDCOS_IP=10.200.0.156 -DDG_FLAVOUR=hydra -DlogLevel=DEBUG -DBOOTSTRAP_IP=10.200.0.155 -DKAFKA_NAME=eos-kafka-framework-1 -DTOPIC=khermes5 -DKAFKA_PARTITION=5 -DCREATE_TOPIC=TRUE -DEXECUTION=FALSE -DDCOS_CLI_HOST=dcos-fulle1`
`mvn clean verify -Dgroups=dcos_pnf -DCLUSTER_ID=bootstrap -DCLUSTER_DOMAIN=newmegadev.hetzner.stratio.com -DDCOS_IP=10.130.8.11 -DDG_FLAVOUR=hydra -DlogLevel=DEBUG -DBOOTSTRAP_IP=10.200.0.155 -DKAFKA_NAME=eos-kafka-framework -DTOPIC=topic3 -DKAFKA_PARTITION=3 -DCREATE_TOPIC=TRUE -DEXECUTION=FALSE -DDCOS_CLI_HOST=dcos-newmegadev`

- Upload Docker images
`mvn clean verify -Dgroups=docker_upload -DDCOS_CLI_HOST=dcos-newmegadev -DSTRATIO_SPARKERSION=2.2.0-2.3.0-7180ac5 -DSTRATIO_SPARTA_VERSION=2.6.0-60ced2e -DlogLevel=DEBUG -DCOMMAND='adduser sparta-tt'`

-Take evidences of workflow
`mvn clean verify -Dgroups=dcos_pnf -DCLUSTER_ID=fulle1 -DDCOS_IP=10.200.0.156 -DDG_FLAVOUR=hydra -DlogLevel=DEBUG -DBOOTSTRAP_IP=10.200.0.155 -DKAFKA_NAME=eos-kafka-framework-1 -DTOPIC=khermes5 -DKAFKA_PARTITION=5 -DCREATE_TOPIC=TRUE -DSKIP_EXECUTION=TRUE -DDCOS_CLI_HOST=dcos-fulle1`

- Install Governance
`mvn clean verify -Dgroups=Governance_Instalation -DCLUSTER_ID=nightly -DDCOS_IP=10.200.0.156 -DDG_FLAVOUR=hydra -DlogLevel=DEBUG -DBOOTSTRAP_IP=10.200.0.155 -DPOSTGRES_AGENT=true -DDATA_GOV_POLICY=true -DDATA_GOV_USER_LDAP=true -DPOSTGRES_FRAMEWORK_ID_GOV=postgrestls -DDATAGOV_POLICY=testDatagovTLS -DSTRATIO_COMMAND_CENTER -DDCOS_CLI_HOST=dcos-nightly`

- Uninstall Governace
`Fulle1: mvn verify -Dgroups=Governance_Uninstall -DDCOS_IP=10.200.1.102 -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-fulle1 -DPOSTGRES_HOST=pg-0001.postgresqa.mesos -DCLUSTER_ID=fulle1 -DCLUSTER_DOMAIN=labs.stratio.com -DMARATHON_LB_TASK=marathonlb -DPOSTGRES_NAME=postgresqa -DADDUSER_PRIVATE_NODES=true -DDCOS_SERVICE_NAME=sparta-server -DSELENIUM_GRID=sl.demo.stratio.com:4444 -DFORCE_BROWSER=chrome_64sparta -DSTRATIO_SPARTA_VERSION=2.6.0-96de718 -DDCOS_SERVICE_NAME=sparta-26 -DSPARTA-USER=admin -DSPARTA-PASSWORD=1234`

- Validate Linage 
`mvn verify -Dgroups=linage -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nightly -DPOSTGRES_HOST=pg-0001.postgrestls.mesos -DCLUSTER_ID=nightly -DCLUSTER_DOMAIN=labs.stratio.com -DMARATHON_LB_TASK=marathon-lb-sec -DPOSTGRES_NAME=postgrestls -DADDUSER_PRIVATE_NODES=true -DDCOS_SERVICE_NAME=sparta-server -DSELENIUM_GRID=sl.demo.stratio.com:4444 -DFORCE_BROWSER=chrome_64sparta -DSTRATIO_SPARTA_VERSION=2.6.0-96de718`


- Add permission to nodes:
 `mvn clean verify -Dgroups=docker_upload -DDCOS_CLI_HOST=dcos-rocket -DlogLevel=DEBUG -DSPECIFIC_COMMAND='addur sparta-tt' -Dmaven.failsafe.debug -DBOOTSTRAP_USER=automaticuser`

- Sparta Instalation with Old DCOS:
 `mvn verify -DCLUSTER_ID=intbootstrap -DDCOS_SERVICE_NAME=sparta-server -Dgroups=dcos_instalation -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-aceptacion.demo.stratio.com -DDOCKER_URL=qa.stratio.com/stratio/sparta -DCALICOENABLED=true -DHDFS_IP=10.200.0.74 -DROLE_SPARTA=open -DAUTH_ENABLED=true -DSTRATIO_SPARTA_VERSION=2.2.0 -DZK_URL=zk-0001.zkuserland.mesos:2181,zk-0002.zkuserland.mesos:2181,zk-0003.zkuserland.mesos:2181  -DCROSSDATA_SERVER_CONFIG_SPARK_IMAGE=qa.stratio.com/stratio/stratio-spark:2.2.0-1.0.0 -DSPARTA_JSON=spartamustache-2.2.json -DHDFS_REALM=DEMO.STRATIO.COM -DNGINX_ACTIVE=true -DMASTERS_LIST=10.200.0.242 -DPRIVATE_AGENTS_LIST=10.200.1.88,10.200.1.86,10.200.1.87,10.200.1.63,10.200.0.221 -DCLIENTSECRET=cr7gDH6hX2-C3SBZYWj8F -DID_SPARTA_POLICY=sparta_server -DURL_GOSEC=/etc/sds/gosec-sso -DREGISTERSERVICEOLD=true -DID_POLICY_ZK=spartazk -DIDNODE=594`

- Add Sparta-Policy:
 `mvn verify -DCLUSTER_ID=fulle1 -DID_SPARTA_POLICY=spartapc -DSP-GOSEC-VERSION=2.4.0 -DDCOS_SERVICE_NAME=sparta-server -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.SPARTA_1162_Gosec_AddzookeperPolicy_IT -DlogLevel=DEBUG`
 `mvn verify -DCLUSTER_ID=fulle1 -DID_POLICY_ZK=spartazk -DZK-GOSEC-VERSION=1.0.0 -DDCOS_SERVICE_NAME=sparta-server -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.SPARTA_1162_Gosec_AddzookeperPolicy_IT -DlogLevel=DEBUG`
 `mvn verify -DCLUSTER_ID=bootstrap -DID_POLICY_ZK=spartazk -DID_SPARTA_POLICY_CCT=bootstrap -DCLUSTER_DOMAIN=newmegadev.hetzner.stratio.com  -DDCOS_SERVICE_NAME=sparta-server -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.SPARTA_1162_Gosec_AddzookeperPolicy_IT -DlogLevel=DEBUG`
 `mvn verify -DCLUSTER_ID=nightly -DID_SPARTA_POSTGRES=sparta-pg  -DDCOS_SERVICE_NAME=sparta-server -Dit.test=com.stratio.sparta.testsAT.automated.dcos.installations.SPARTA_1162_Gosec_AddzookeperPolicy_IT -DlogLevel=DEBUG`


- Khermes Instalation:
`mvn verify -DMASTERS_LIST=10.200.0.242 -DPRIVATE_AGENTS_LIST=10.200.1.86  -DVAULT_PORT=8200 -DDCOS_CLI_HOST=dcos-aceptacion -Dgroups=khermes_instalation -DKHERMES_INSTANCE=khermes2`

- PNF:
`mvn verify -Dit.test=com.stratio.sparta.testsAT.automated.dcos.executions.SPARTA_1196_Workflow_PNF_IT -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-fulle1 -DDCOS_SERVICE_NAME=sparta-server -DSELENIUM_GRID=sl.demo.stratio.com:4444 -DFORCE_BROWSER=chrome_64sparta -DMARATHON_LB_TASK=marathonlb -EXECUTIONS=1,2,3,4,5 -DURL_WORKFLOW_STREAMING=XXX`

- Install and execute workflows:
`mvn verify -DCLUSTER_ID=fulle1 -DDCOS_SERVICE_NAME=sparta-server -Dgroups=executionswithsecurity -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-fulle1 -DDOCKER_URL=qa.stratio.com/stratio/sparta -DCALICOENABLED=true -DHDFS_IP=10.200.0.74 -DROLE_SPARTA=open -DAUTH_ENABLED=true -DSTRATIO_SPARTA_VERSION=2.2.0 -DZK_URL=zk-0001.zkuserland.mesos:2181,zk-0002.zkuserland.mesos:2181,zk-0003.zkuserland.mesos:2181  -DCROSSDATA_SERVER_CONFIG_SPARK_IMAGE=qa.stratio.com/stratio/stratio-spark:2.2.0-1.0.0 -DHDFS_REALM=DEMO.STRATIO.COM -DNGINX_ACTIVE=false -DMASTERS_LIST=10.200.1.102 -DPRIVATE_AGENTS_LIST=10.200.1.80,10.200.1.182,10.200.0.227,10.200.1.46,10.200.0.211,10.200.1.234,10.200.1.131,10.200.1.85,10.200.1.82,10.200.1.225,10.200.0.228,10.200.1.175,10.200.0.230 -DCLIENTSECRET=cr7gDH6hX2-C3SBZYWj8F -DID_POLICY_ZK=spartazk  -DSELENIUM_GRID=sl.demo.stratio.com:4444 -DFORCE_BROWSER=chrome_64sparta -DPURGE_DATA=true -DSKIP_POLICY=false -DPOSTGRES_NAME=postgrestls -DPOSTGRES_INSTANCE=pg-0001.postgrestls.mesos:5432/postgres`
`mvn verify -DDCOS_SERVICE_NAME=sparta-server -DCLUSTER_ID=intbootstrap -DDCOS_SERCE_NAME=sparta-server -Dgroups=Installation_Executions_FullSecurity -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-aceptacion.demo.stratio.com -DDOCKER_URL=qa.stratio.com/stratio/sparta -DCALICOENABLED=true -DHDFS_IP=10.200.0.74 -DROLE_SPARTA=open -DAUTH_ENABLED=true -DSTRATIO_SPARTA_VERSION=2.2.0 -DZK_URL=zk-0001.zkuserland.mesos:2181,zk-0002.zkuserland.mesos:2181,zk-0003.zkuserland.mesos:2181  -DCROSSDATA_SERVER_CONFIG_SPARK_IMAGE=qa.stratio.com/stratio/stratio-spark:2.2.0-1.0.0 -DSPARTA_JSON=spartamustache-2.2.json -DHDFS_REALM=DEMO.STRATIO.COM -DNGINX_ACTIVE=true -DMASTERS_LIST=10.200.0.242 -DPRIVATE_AGENTS_LIST=10.200.1.88,10.200.1.86,10.200.1.87,10.200.1.63,10.200.0.221 -DCLIENTSECRET=cr7gDH6hX2-C3SBZYWj8F -DID_SPARTA_POLICY=sparta_server -DURL_GOSEC=/etc/sds/gosec-sso -DREGISTERSERVICEOLD=true -DID_POLICY_ZK=spartazk -DIDNODE=594 -DSKIP_POLICY=false -DSELENIUM_GRID=sl.demo.stratio.com:4444 -DFORCE_BROWSER=chrome_64sparta -DPURGE_DATA=true -DPOSTGRES_INSTANCE=pg-0001.postgrestls.mesos:5432/postgres -DPOSTGRES_NAME=postgrestls`
`mvn verify -DCLUSTER_ID=nightly -DDCOS_SERVICE_NAME=sparta-server -Dgroups=dcos_instalation -DlogLevel=DEBUG -DDCOS_CLI_HOST=dcos-nightly -DDOCKER_URL=qa.stratio.com/stratio/sparta -DCALICOENABLED=true -DHDFS_IP=10.200.0.74 -DAUTH_ENABLED=true -DSTRATIO_SPARTA_VERSION=2.4.0 -DZK_URL=zk-0001.zkuserland.mesos:2181,zk-0002.zkuserland.mesos:2181,zk-0003.zkuserland.mesos:2181-DSPARTA_JSON=spartamustache-2.4.json -DNGINX_ACTIVE=true -DMASTERS_LIST=10.200.0.161,10.200.0.162,10.200.0.181,10.200.0.182,10.200.0.183,10.200.0.184,10.200.0.185,10.200.0.194 -DCLIENTSECRET=LBNuJVgfsb-WHEkP83zt -DID_SPARTA_POLICY=sparta_server -DPOSTGRES_URL=pg-0001.postgrestls.mesos -DSKIP_POLICY=false -DSTRATIO_RELEASE=ORION -DNEW_HEALTHCHECKS=false -DGOV_ENABLED=true -DSELENIUM_GRID=sl.demo.stratio.com:4444 -DFORCE_BROWSER=chrome_64sparta`