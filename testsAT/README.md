# README

## ACCEPTANCE TESTS

Cucumber automated and manual acceptance tests.
This module depends on a QA library (stratio-test-bdd), where common logic and steps are implemented.

## EXECUTION

These tests will be executed as part of the continuous integration flow as follows:

mvn verify [-D\<ENV_VAR>=\<VALUE>] [-Dit.test=\<TEST_TO_EXECUTE>|-Dgroups=\<GROUP_TO_EXECUTE>]

Example:

mvn verify -DSPARKTA_HOST=localhost -DSPARKTA_PORT=9090 -DSPARKTA_API_PORT=9091 -Dit.test=com.stratio.sparkta.testsAT.automated.gui.inputs.AddNewSocket

By default, in jenkins we will execute the group basic, which should contain a subset of tests, that are key to the functioning of the module and the ones generated for the new feature.

All tests, that are not fully implemented, should belong to the group manual and be tagged with '@ignore @manual'

## MIGRATED TESTS EXECUTION

From testsAT directory execute:

Cassandra:
mvn verify -DSPARKTA_HOST=sp.demo.stratio.com -DSPARKTA_PORT=9090 -DSPARKTA_API_PORT=9090 -DCASSANDRA_HOST=cs.demo.stratio.com -DCASSANDRA_PORT=9042 -DIFACE=eth0 -Dit.test=com.stratio.sparkta.testsAT.automated.endtoend.ISocketOCassandra

SPARKTA_HOST: ip/name of the sparkta instance
SPARKTA_PORT: sparkta web port
SPARKTA_API_PORT: sparkta api port
CASSANDRA_HOST: ip/name of the cassandra instance
CASSANDRA_PORT: cassandra port number
IFACE: name of the interface in your system to be used to create the socket

Mongo:
mvn verify -DSPARKTA_HOST=sp.demo.stratio.com -DSPARKTA_PORT=9090 -DSPARKTA_API_PORT=9090 -DMONGO_HOST=mn.demo.stratio.com -DMONGO_PORT=27017 -DIFACE=eth0 -Dit.test=com.stratio.sparkta.testsAT.automated.endtoend.ISocketOMongoDB

SPARKTA_HOST: ip/name of the sparkta instance
SPARKTA_PORT: sparkta web port
SPARKTA_API_PORT: sparkta api port
MONGO_HOST: ip/name of the mongodb instance
MONGO_PORT: mongodb port number
IFACE: name of the interface in your system to be used to create the socket

Elasticsearch:
mvn verify -DSPARKTA_HOST=sp.demo.stratio.com -DSPARKTA_PORT=9090 -DSPARKTA_API_PORT=9090 -DELASTICSEARCH_HOST=es.demo.stratio.com -DIFACE=eth0 -Dit.test=com.stratio.sparkta.testsAT.automated.endtoend.ISocketOElasticsearch

SPARKTA_HOST: ip/name of the sparkta instance
SPARKTA_PORT: sparkta web port
SPARKTA_API_PORT: sparkta api port
ELASTICSEARCH_HOST: ip/name of the elasticsearch instance
IFACE: name of the interface in your system to be used to create the socket