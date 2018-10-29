@Library('libpipelines@master') _

hose {
    EMAIL = 'sparta'
    MODULE = 'sparta-workflow'
    DEVTIMEOUT = 70
    RELEASETIMEOUT = 40
    FOSS = true
    REPOSITORY = 'sparta-workflow'
    PKGMODULES = ['dist']
    PKGMODULESNAMES = ['stratio-sparta']
    DEBARCH = 'all'
    MAVEN_THREADSPERCORE = 4
    EXPOSED_PORTS = [9090,10000,11000]
    KMS_UTILS = '0.4.0'
    BASEIMG = 'qa.stratio.com/stratio/spark-stratio-driver:2.2.0-2.1.0-f969ad8'
    DOCKERFILECOMMAND = 'WORKDIR / \n RUN apt-get update -y && apt-get install -y nginx realpath coreutils krb5-user libpam-krb5 libpam-ccreds auth-client-config curl wget php5-curl make jq vim && update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java && wget -qO- https://www.openssl.org/source/openssl-1.0.2l.tar.gz | tar xz && cd openssl-1.0.2l && sudo ./config && sudo make && sudo make install && sudo ln -sf /usr/local/ssl/bin/openssl /usr/bin/openssl && wget https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64 && chmod +x jq-linux64 && mv jq-linux64 /usr/bin/jq'
    BUILDTOOLVERSION = '3.5.0'
    NEW_VERSIONING = 'true'

    ITSERVICES = [
            ['HDFS': [
              'image': 'stratio/hdfs:2.6.0',
              'sleep': 30,
              'healthcheck': 9000
              ]
            ],
            ['POSTGRESQL':[
              'image': 'postgres:9.6',
              'sleep': 60,
              'healthcheck': 5432]
            ],
            ['ELASTIC': [
              'image': 'elasticsearch/elasticsearch:5.6.2',
              'sleep': 60,
              'healthcheck': 9200,
              'env': ['xpack.security.enabled=false'
                      ]
              ]
            ],
            ['ZOOKEEPER': [
              'image': 'jplock/zookeeper:3.5.2-alpha',
              'sleep': 30,
              'healthcheck': 2181
              ]
            ],
            ['KAFKA': [
              'image': 'confluent/kafka:0.10.0.0-cp1',
              'sleep': 30,
              'healthcheck': 9092,
              'env': ['KAFKA_ZOOKEEPER_CONNECT=%%ZOOKEEPER:2181',
                'KAFKA_ADVERTISED_HOST_NAME=%%OWNHOSTNAME']
              ]
            ],
            ['CASSANDRA': [
              'image': 'stratio/cassandra-lucene-index:3.0.7.3',
              'sleep': 20,
              'healthcheck': 9042
              ]
            ],
            ['MONGODB': [
              'image': 'mongo:3.2',
              'sleep': 20,
              'healthcheck': 27017
             ]
            ]
    ]

    ITPARAMETERS = """
      |    -Dkafka.hosts=%%KAFKA
      |    -Dsparta.zookeeper.connectionString=%%ZOOKEEPER:2181
      |    -Dsparta.hdfs.hdfsMaster=%%HDFS
      |    -Dsparta.hdfs.hdfsPort=9000
      |    -Dpostgresql.host=%%POSTGRESQL
      |    -Des.host=%%ELASTIC
      |    -Dcassandra.hosts.0=%%CASSANDRA#0
      |    -Dcassandra.port=9042
      |    -Dmongo.host=%%MONGODB
      | """

    DEV = { config ->

        doCompile(config)

        parallel(UT: {
            doUT(config)
        }, IT: {
            doIT(config)
        }, failFast: config.FAILFAST)


        doPackage(config)

        parallel(DEPLOY: {
            doDeploy(config)
        }, DOCKER : {
            doDocker(config)
        }, failFast: config.FAILFAST)

    }
    INSTALLSERVICES = [
        ['CHROME': [
            'image': 'selenium/node-chrome-debug:3.9.1',
            'volumes': ['/dev/shm:/dev/shm'],
            'env': ['HUB_HOST=selenium391.cd','HUB_PORT=4444','SE_OPTS="-browser browserName=chrome,version=64%%JUID "']
            ]
        ],
        ['DCOSCLI':   ['image': 'stratio/dcos-cli:0.4.15-SNAPSHOT',
                           'env':     ['DCOS_IP=10.200.0.205',
                                      'SSL=true',
                                      'SSH=true',
                                      'TOKEN_AUTHENTICATION=true',
                                      'DCOS_USER=admin',
                                      'DCOS_PASSWORD=1234',
                                      'CLI_BOOTSTRAP_USER=root',
                                'CLI_BOOTSTRAP_PASSWORD=stratio'],
                           'sleep':  120,
                           'healthcheck': 5000]]
    ]
    INSTALLPARAMETERS = """
            | -DSTRATIO_SPARTA_VERSION=2.2.0
            | -DDOCKER_URL=qa.stratio.com/stratio/sparta
            | -DDCOS_SERVICE_NAME=sparta-server
            | -DFORCEPULLIMAGE=false    
            | -DZK_URL=zk-0001.zkuserland.mesos:2181,zk-0002.zkuserland.mesos:2181,zk-0003.zkuserland.mesos:2181
            | -DMARATHON_SSO_CLIENT_ID=adminrouter_paas-master-1.node.paas.labs.stratio.com
            | -DHDFS_IP=10.200.0.74
            | -DHDFS_PORT=8020
            | -DHDFS_REALM=DEMO.STRATIO.COM
            | -DCROSSDATA_SERVER_CONFIG_SPARK_IMAGE=qa.stratio.com/stratio/spark-stratio-driver:2.2.0-2.1.0-f969ad8
            | -DROLE_SPARTA=open
            | -DID_POLICY_ZK=spartazk
            | -DDCOS_CLI_HOST=%%DCOSCLI#0
            | -DSPARTA_JSON=spartamustache-2.2.json
            | -DWORKFLOW=testinput-to-print
            | -DAUTH_ENABLED=true
            | -DCALICOENABLED=true
            | -DCLIENTSECRET=cr7gDH6hX2-C3SBZYWj8F
            | -DIDNODE=564        
            | -DSELENIUM_GRID=selenium391.cd:4444
            | -DFORCE_BROWSER=chrome_64%%JUID
            | -DWORKFLOW_LIST=testinput-kafka,kafka-postgres
            | -Dquietasdefault=false
            | -DNGINX_ACTIVE=true
            | -DPOSTGRES_NODE=pg-0001
            | -DPOSTGRES_NAME=postgrestls
            | -DURL_GOSEC=/opt/stratio/gosec-sso/conf
            | -DPOSTGRES_INSTANCE=pg-0001.postgrestls.mesos:5432/postgres
            | -DPURGE_DATA=true
            """
            
    INSTALL = { config ->
        if (config.INSTALLPARAMETERS.contains('GROUPS_SPARTA')) {
            config.INSTALLPARAMETERS = "${config.INSTALLPARAMETERS}".replaceAll('-DGROUPS_SPARTA', '-Dgroups')
          doAT(conf: config)
        } else {
            doAT(conf: config, groups: ['Installation_Executions_FullSecurity'])
        }
     }
}