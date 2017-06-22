@Library('libpipelines@branch-0.3') _

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
        }, failFast: config.FAILFAST)

    }
}
