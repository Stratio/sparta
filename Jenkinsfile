@Library('libpipelines@master') _

hose {
    EMAIL = 'sparta'
    MODULE = 'sparta'
    DEVTIMEOUT = 70
    RELEASETIMEOUT = 40
    FOSS = true
    REPOSITORY = 'sparta-workflow'
    PKGMODULES = ['dist']
    PKGMODULESNAMES = ['sparta']
    DEBARCH = 'all'
    MAVEN_THREADSPERCORE = 4
    EXPOSED_PORTS = [9090,10000,11000]
    NEW_VERSIONING = 'true'
    LABEL_CONTROL = 'true'

    ITSERVICES = [
            ['ARANGODB': [
              'image': 'arangodb/arangodb:3.4.1',
              'healthcheck': 8529,
              'sleep': 30,
              'env': ['ARANGO_ROOT_PASSWORD=openSesame']
              ]
            ],
            ['HDFS': [
              'image': 'stratio/hdfs:2.6.0',
              'sleep': 30,
              'healthcheck': 9000
              ]
            ],
            ['POSTGRESQL':[
              'image': 'postgres:9.6',
              'sleep': 50,
              'healthcheck': 5432]
            ],
            ['ELASTIC': [
              'image': 'elasticsearch/elasticsearch:5.6.2',
              'sleep': 30,
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
              'image': 'wurstmeister/kafka:2.11-0.10.2.2',
              'sleep': 50,
              'healthcheck': 9092,
              'env': ['KAFKA_ZOOKEEPER_CONNECT=%%ZOOKEEPER:2181',
                'KAFKA_ADVERTISED_HOST_NAME=%%OWNHOSTNAME']
              ]
            ],
            ['CASSANDRA': [
              'image': 'stratio/cassandra-lucene-index:3.0.7.3',
              'sleep': 30,
              'healthcheck': 9042
              ]
            ],
            ['MONGODB': [
              'image': 'mongo:3.2',
              'sleep': 30,
              'healthcheck': 27017
             ]
            ],
            ['SFTP': [
               'image': 'atmoz/sftp',
               'sleep': 30,
               'healthcheck': 22,
               'cmd': 'foo:pass:1001',
               'volumes': [
               '/tmp:/home/foo/tmp'],

             ]
            ],
            ['SCHEMAREGISTRY': [
                'image': 'confluentinc/cp-schema-registry:4.1.0',
                'healthcheck': 8081,
                'sleep': 80,
                'env': ['SCHEMA_REGISTRY_KAFKASTORE_CONNECTION_URL=%%ZOOKEEPER:2181',
                  'SCHEMA_REGISTRY_HOST_NAME=%%OWNHOSTNAME',
                  'SCHEMA_REGISTRY_LISTENERS=http://%%OWNHOSTNAME:8081']
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
      |    -Dsftp.host=%%SFTP
      |    -Dsftp.port=22
      |    -Dsftp.volume=/home/foo/tmp
      |    -Dschemaregistry.host=%%SCHEMAREGISTRY
      |    -Darangodb.host=%%ARANGODB
      |    -Darandodb.port=8529
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
            | -DDCOS_SERVICE_NAME=sparta-server
            | -DFORCEPULLIMAGE=false
            | -DZOOKEEPER_NAME=zkuserland
            | -DMARATHON_SSO_CLIENT_ID=adminrouter_paas-master-1.node.paas.labs.stratio.com
            | -DHDFS_IP=10.200.0.74
            | -DHDFS_PORT=8020
            | -DHDFS_REALM=DEMO.STRATIO.COM
            | -DCROSSDATA_SERVER_CONFIG_SPARK_IMAGE=qa.stratio.com/stratio/spark-stratio-driver:2.2.0-2.5.0-84c385f
            | -DROLE_SPARTA=open
            | -DID_POLICY_ZK=spartazk
            | -DDCOS_CLI_HOST=%%DCOSCLI#0
            | -DSELENIUM_GRID=selenium391.cd:4444
            | -DFORCE_BROWSER=chrome_64%%JUID
            | -Dquietasdefault=false
            | -DNGINX_ACTIVE=true
            | -DPOSTGRES_NODE=pg-0001
            | -DPOSTGRES_NAME=postgrestls
            | -DURL_GOSEC=/opt/stratio/gosec-sso/conf
            | -DPOSTGRES_INSTANCE=pg-0001.postgrestls.mesos:5432/postgres
            | -DPURGE_DATA=true
            | -DSKIP_USERS=true
            """

    INSTALL = { config ->
        if (config.INSTALLPARAMETERS.contains('GROUPS_SPARTA')) {
            config.INSTALLPARAMETERS = "${config.INSTALLPARAMETERS}".replaceAll('-DGROUPS_SPARTA', '-Dgroups')
          doAT(conf: config)
        } else {
            doAT(conf: config, groups: ['nightly'])
        }
     }
}