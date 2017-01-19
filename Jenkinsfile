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
    EXPOSED_PORTS = [9090,9091]
    USERBASEIMG = 'qa.stratio.com/stratio/mesosphere-spark-scala211:1.6.2'


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
        }, QC: {
            doStaticAnalysis(config)
        }, DEPLOY: {
            doDeploy(config)
        }, DOCKER : {    
            doDocker(config)
        }, failFast: config.FAILFAST)

    }
}
