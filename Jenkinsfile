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
    RPMARCH = 'noarch'
    EXPOSED_PORTS = [9090]
    KMS_UTILS = '0.4.0'
    BASEIMG = 'qa.stratio.com/stratio/stratio-spark:2.2.0.5-RC2'
    DOCKERFILECOMMAND = 'WORKDIR / \n RUN apt-get update -y && apt-get install -y nginx realpath coreutils krb5-user libpam-krb5 libpam-ccreds auth-client-config curl wget php5-curl make jq vim && update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java && curl https://www.openssl.org/source/openssl-1.0.2l.tar.gz | tar xz && cd openssl-1.0.2l && sudo ./config && sudo make && sudo make install && sudo ln -sf /usr/local/ssl/bin/openssl /usr/bin/openssl && wget https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64 && chmod +x jq-linux64 && mv jq-linux64 /usr/bin/jq'

    ITSERVICES = [
            ['HDFS': [
              'image': 'stratio/hdfs:2.6.0',
              'sleep': 30,
              'healthcheck': 9000
              ]
            ],
            ['POSTGRESQL':[
              'image': 'postgresql:9.3',
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
            ]
    ]

    ITPARAMETERS = """
      |    -Dkafka.hosts=%%KAFKA
      |    -Dsparta.zookeeper.connectionString=%%ZOOKEEPER:2181
      |    -Dsparta.hdfs.hdfsMaster=%%HDFS
      |    -Dsparta.hdfs.hdfsPort=9000
      |    -Dpostgresql.host=%%POSTGRESQL
      |    -Des.host=%%ELASTIC
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
            'image': 'stratio/selenium-chrome:48',
            'volumes': ['/dev/shm:/dev/shm'],
            'env': ['SELENIUM_GRID=selenium.cd','ID=%%JUID']
            ]
        ],
        ['DCOSCLI': [
                'image': 'stratio/dcos-cli:0.4.15-SNAPSHOT',
                'volumes': ['stratio/paasintegrationpem:0.1.0'],
                'env': [
                    'DCOS_IP=10.200.0.205',
                    'SSL=true',
                    'SSH=true',
                    'TOKEN_AUTHENTICATION=true',
                    'DCOS_USER=admin@demo.stratio.com',
                    'DCOS_PASSWORD=stratiotest',
                    'BOOTSTRAP_USER=operador',
                    'PEM_FILE_PATH=/paascerts/PaasIntegration.pem'
                    ],
                'sleep':  10
                ]
        ]  
    ]
    INSTALLPARAMETERS = """
            | -DSTRATIO_SPARTA_VERSION=1.7.8
            | -DDOCKER_URL=qa.stratio.com/stratio/sparta
            | -DDCOS_SERVICE_NAME=sparta-server
            | -DFORCEPULLIMAGE=false    
            | -DZK_URL=zk-0001.zkuserland.mesos:2181,zk-0002.zkuserland.mesos:2181,zk-0003.zkuserland.mesos:2181
            | -DMARATHON_SSO_CLIENT_ID=adminrouter_paas-master-1.node.paas.labs.stratio.com
            | -DHDFS_IP=10.200.0.74
            | -DHDFS_PORT=8020
            | -DHDFS_REALM=DEMO.STRATIO.COM
            | -DCROSSDATA_SERVER_CONFIG_SPARK_IMAGE=qa.stratio.com/stratio/stratio-spark:2.2.0.4
            | -DROLE_SPARTA=open
            | -DID_POLICY_ZK=spartazk
            | -DDCOS_CLI_HOST=%%DCOSCLI#0
            | -DSPARTA_JSON=spartamustache.json
            | -DWORKFLOW=testinput-to-print
            | -DAUTH_ENABLED=false
            | -DCALICOENABLED=false
            | -DCLIENTSECRET=cr7gDH6hX2-C3SBZYWj8F
            | -DIDNODE=564        
            | -DSELENIUM_GRID=selenium.cd:4444
            | -DFORCE_BROWSER=chrome_48%%JUID
            | -DWORKFLOW_LIST=testinput-kafka,kafka-postgres
            | -Dquietasdefault=false
            | -DNGINX_ACTIVE=false
            | -DPOSTGRES_NODE=pg-0001
            | -DPOSTGRES_NAME=postgrestls
            """
            
    INSTALL = { config ->
        if (config.INSTALLPARAMETERS.contains('GROUPS_SPARTA')) {
            config.INSTALLPARAMETERS = "${config.INSTALLPARAMETERS}".replaceAll('-DGROUPS_SPARTA', '-Dgroups')
          doAT(conf: config)
        } else {
            doAT(conf: config, groups: ['dcos_installations_executions','dcos_streaming','dcos_centralLoggin','dcos_enviroments'])
        }
     }
}
