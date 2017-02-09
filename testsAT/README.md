# README

## ACCEPTANCE TESTS

Cucumber automated and manual acceptance tests.
This module depends on a QA library (stratio-test-bdd), where common logic and steps are implemented.

## EXECUTION

These tests will be executed as part of the continuous integration flow as follows:

mvn verify [-D\<ENV_VAR>=\<VALUE>] [-Dit.test=\<TEST_TO_EXECUTE>|-Dgroups=\<GROUP_TO_EXECUTE>]

Example:

mvn verify -DSPARTA_HOST=localhost -DSPARTA_PORT=9090 -DSPARTA_API_PORT=9090 -Dit.test=com.stratio.sparta.testsAT.automated.gui.inputs.AddNewSocket

By default, in jenkins we will execute the group basic, which should contain a subset of tests, that are key to the functioning of the module and the ones generated for the new feature.

All tests, that are not fully implemented, should belong to the group manual and be tagged with '@ignore @manual'

## MIGRATED TESTS EXECUTION

From testsAT directory execute:

Cassandra:
mvn verify -DSPARTA_HOST=sp.demo.stratio.com -DSPARTA_PORT=9090 -DSPARTA_API_PORT=9090 -DCASSANDRA_HOST=cs.demo.stratio.com -DCASSANDRA_PORT=9042 -DIFACE=eth0 -Dit.test=com.stratio.sparta.testsAT.automated.endtoend.ISocketOCassandra

SPARTA_HOST: ip/name of the sparta instance
SPARTA_PORT: sparta web port
SPARTA_API_PORT: sparta api port
CASSANDRA_HOST: ip/name of the cassandra instance
CASSANDRA_PORT: cassandra port number
IFACE: name of the interface in your system to be used to create the socket

Mongo:
mvn verify -DSPARTA_HOST=sp.demo.stratio.com -DSPARTA_PORT=9090 -DSPARTA_API_PORT=9090 -DMONGO_HOST=mn.demo.stratio.com -DMONGO_PORT=27017 -DIFACE=eth0 -Dit.test=com.stratio.sparta.testsAT.automated.endtoend.ISocketOMongoDB

SPARTA_HOST: ip/name of the sparta instance
SPARTA_PORT: sparta web port
SPARTA_API_PORT: sparta api port
MONGO_HOST: ip/name of the mongodb instance
MONGO_PORT: mongodb port number
IFACE: name of the interface in your system to be used to create the socket

Elasticsearch:
mvn verify -DSPARTA_HOST=sp.demo.stratio.com -DSPARTA_PORT=9090 -DSPARTA_API_PORT=9090 -DELASTICSEARCH_HOST=es.demo.stratio.com -DIFACE=eth0 -Dit.test=com.stratio.sparta.testsAT.automated.endtoend.ISocketOElasticsearch

SPARTA_HOST: ip/name of the sparta instance
SPARTA_PORT: sparta web port
SPARTA_API_PORT: sparta api port
ELASTICSEARCH_HOST: ip/name of the elasticsearch instance
IFACE: name of the interface in your system to be used to create the socket

CSV:
mvn verify -DSPARTA_HOST=sp.demo.stratio.com -DSPARTA_PORT=9090 -DSPARTA_API_PORT=9090 -DCSV_PATH=/tmp/sparta/csv -DIFACE=eth0 -Dit.test=com.stratio.sparta.testsAT.automated.endtoend.ISocketOCSV

SPARTA_HOST: ip/name of the sparta instance
SPARTA_PORT: sparta web port
SPARTA_API_PORT: sparta api port
CSV_PATH: Path in sparta system to csv file location
IFACE: name of the interface in your system to be used to create the socket