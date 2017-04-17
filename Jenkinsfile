@Library('libpipelines@master') _

hose {
    EMAIL = 'sparta'
    MODULE = 'sparta'
    DEVTIMEOUT = 70
    RELEASETIMEOUT = 40
    FOSS = true
    REPOSITORY = 'sparta'    
    PKGMODULES = ['dist']
    PKGMODULESNAMES = ['stratio-sparta']
    DEBARCH = 'all'
    RPMARCH = 'noarch'
    EXPOSED_PORTS = [9090]
    BASEIMG = 'stratio/spark-krb-dispatcher-support:2.1.0'
    DOCKERFILECOMMAND = 'WORKDIR / \n RUN update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java'


    ITSERVICES = [
            ['RABBITMQ': [
               'image': 'rabbitmq:3-management'
            ]],
          ]

    ITPARAMETERS = "-Drabbitmq.hosts=%%RABBITMQ"


    DEV = { config ->
    
        doCompile(config)
        
        parallel(UT: {
            doUT(config)
        }, IT: {
            doIT(config)
        }, failFast: config.FAILFAST)
                        
        doPackage(config)

        parallel(DOC: {
            doDoc(config)
        }, DEPLOY: {
            doDeploy(config)
        }, DOCKER : {    
            doDocker(config)
        }, failFast: config.FAILFAST)

    }
}
