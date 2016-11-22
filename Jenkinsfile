@Library('libpipelines@feature/multibranch') _

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
    EXPOSED_PORTS = [9091]

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
