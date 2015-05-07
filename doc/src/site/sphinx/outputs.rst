
Outputs Configurations
******************

- :ref:`generic-label` . :doc:`dataframes`

- :ref:`mongodb-label` . :doc:`mongodb`

- :ref:`cassandra-label` . :doc:`cassandra`

- :ref:`elasticsearch-label` . :doc:`elasticsearch`

- :ref:`redis-label` .:doc:`redis`

- :ref:`print-label`


.. image:: images/outputs.png
   :height: 400 px
   :width: 420 px
   :alt: Outputs in Sparkta



----------------------

.. _generic-label:

Generic Configuration
==========

In the SDK you can find the model that must follow an output to be implemented.It has several settings that can modify system operation.

For more information for this output you can visit the :doc:`dataframes`

These parameters can be completed in the policy file:

* multiplexer:
  If you want to multiplex all possible combinations that occur within a rollup,so that the outputs are saved
  multiple "tables".
  With this parameter the possibility of multi cubes and the possibility of generating it implements a data aggregation lake.
  Is possible omit this parameter in policy.

  * Sample:
::

   "multiplexer": ("true"/"false")  Default: "false"

* timeBucket:
  You can specify the time bucket containing the event, thanks to this parameter can be stored aggregate data and
  generate timeseries.
  This name will be as identified in the system of persistence.
  Is possible omit this parameter in policy.

  * Sample:
::

   "timeBucket": ("BUCKET_LABEL")  Default: ""

* granularity:
  If not created any bucketer time to identify with "timeBucket" you can leave the system assigned to each event time
   with the specified granularity.
  Is possible omit this parameter in policy.

  * Sample:
::

   "granularity": ("second"/"minute"/"hour"/"day"/"month"/"year")  Default: ""

* isAutoCalculateId:
   The system is capable of assigning an id added to each event, so that it may identify only the output.
   This field is calculated with all the values of the bucket rollup, including timeBucket date if the parameter is specified.
   Only for DataFrames persistence, disable in UpdateMetricOperation.
   Is possible omit this parameter in policy.

   * Sample:
::

   "isAutoCalculateId": ("true"/"false")  Default: "false"


.. _mongodb-label:

MongoDB Configuration
==========

The output of MongoDB does not use the generic implementation with DataFrames, it has multiple configuration
parameters to connect to the DB and self-creation of indexes.

For more information for this output you can visit the :doc:`mongodb`

* mongoClientUri:
   This parameter Connection routes specified the different nodes of a cluster of MongoDB, with replica set or with sharding.
   Is possible omit this parameter in policy.

   * Sample:
::

   "mongoClientUri": ("mongodb://localhost:27017")  Default: "mongodb://localhost:27017"

* dbName:
   The system is capable of assigning an id added to each event, so that it may identify only the output.
   This field is calculated with all the values of the bucket rollup, including timeBucket date if the parameter is specified.
   Only for DataFrames persistence, disable in UpdateMetricOperation.
   Is possible omit this parameter in policy.

   * Sample:
::

   "dbName": ("DATABASE_NAME")  Default: "sparkta"

* connectionsPerHost:
   Number of connections per host that the system open.
   Is possible omit this parameter in policy.

   * Sample:
::

   "connectionsPerHost": ("NUMBER")  Default: "5"

* threadsAllowedToBlock:
   This multiplier, multiplied with the connectionsPerHost setting, gives the maximum number of threads that may be waiting for a connection to become available from the pool.
   Is possible omit this parameter in policy.

   * Sample:
::

   "threadsAllowedToBlock": ("NUMBER")  Default: "10"

* fieldsSeparator:
   Is possible specify the character that separate the fields in the "textIndexFields" parameter.
   Is possible omit this parameter in policy.

   * Sample:
::

   "fieldsSeparator": ("any_character")  Default: ","

* textIndexFields:
   The system is capable of insert data in a full-text index. All of this fields compound the index.
   Is possible omit this parameter in policy.

   * Sample:
::

   "textIndexFields": ("field1,field2")  Default: ""

* language:
   Specify the language of the tokenizer in the full-text index in MongoDB, each document inserted must have this
   key-value.
   Is possible omit this parameter in policy.

   * Sample:
::

   "language": ("english"/"spanish"...)  Default: "none"


.. _cassandra-label:

Cassandra Configuration
==========

The output of Cassandra use the generic implementation with DataFrames, this implementation transform each
UpdateMetricOperation to Row type of Spark and identify each row with his schema.

For more information for this output you can visit the :doc:`cassandra`

* connectionHost:
   This parameter specified the different seeds of a cluster of Cassandra.
   Is possible omit this parameter in policy.

   * Sample:
::

   "connectionHost": ("ip_seed1,ip_seed2,ip_seed3,...")  Default: "127.0.0.1"

* cluster:
   This parameter specified the cluster name.
   Is possible omit this parameter in policy.

   * Sample:
::

   "cluster": ("CLUSTER_NAME")  Default: "Test Cluster"

* keyspace:
   This parameter specified the keyspace name.
   Is possible omit this parameter in policy.

   * Sample:
::

   "keyspace": ("KEYSPACE_NAME")  Default: "sparkta"

* keyspaceClass:
   - SimpleStrategy:
   Use only for a single data center. SimpleStrategy places the first replica on a node determined by the partitioner. Additional replicas are placed on the next nodes clockwise in the ring without considering topology (rack or data center location).
   - NetworkTopologyStrategy:
   Use NetworkTopologyStrategy when you have (or plan to have) your cluster deployed across multiple data centers. This strategy specifies how many replicas you want in each data center.
   Is possible omit this parameter in policy.

   * Sample:
::

   "keyspaceClass": ("SimpleStrategy"/"NetworkTopologyStrategy")  Default: "SimpleStrategy"

* replication_factor:
   Required if class is SimpleStrategy; otherwise, not used. The number of replicas of data on multiple nodes.
   Is possible omit this parameter in policy.

   * Sample:
::

   "replication_factor": ("NUMBER")  Default: "1"

* compactStorage:
   The compact storage directive is used for backward compatibility of CQL 2 applications and data in the legacy (Thrift) storage engine format. To take advantage of CQL 3 capabilities, do not use this directive in new applications. When you create a table using compound primary keys, for every piece of data stored, the column name needs to be stored along with it. Instead of each non-primary key column being stored such that each column corresponds to one column on disk, an entire row is stored in a single column on disk, hence the name compact storage.
   Is possible omit this parameter in policy.

   * Sample:
::

   "compactStorage": ("ANY")  Default: None

* fieldsSeparator:
   Is possible specify the character that separate the fields in the "textIndexFields" parameter.
   Is possible omit this parameter in policy.

   * Sample:
::

   "fieldsSeparator": ("any_character")  Default: ","

* clusteringBuckets:
   Is possible specify the clustering columns for the primary key.
   Is possible omit this parameter in policy.

   * Sample:
::

   "clusteringBuckets": ("bucket1,bucket2,bucket3...")  Default: ""

* indexFields:
   Is possible specify the indexed fields, could be any aggregate field or clustering column field.
   Is possible omit this parameter in policy.

   * Sample:
::

   "indexFields": ("bucket1,bucket2,bucket3, ...")  Default: ""

* textIndexFields:
   Is possible specify the text index fields, this feature is for the Stratio Cassandra.
   Is possible omit this parameter in policy.

   * Sample:
::

   "textIndexFields": ("bucket1,bucket2,bucket3,aggregate1, aggregate2, ...")  Default: ""

* analyzer:
   Is possible specify the analyzer for text index fields, this feature is for the Stratio Cassandra.
   Is possible omit this parameter in policy.

   * Sample:
::

   "analyzer": ("english"/"spanish"...)  Default: None

* textIndexName:
   Is possible specify the name of the text index, this feature is for the Stratio Cassandra.
   Is possible omit this parameter in policy.

   * Sample:
::

   "textIndexName": ("NAME")  Default: "lucene"


.. _elasticsearch-label:

ElasticSearch Configuration
==========

The output of ElasticSearch use the generic implementation with DataFrames, this implementation transform each
UpdateMetricOperation to Row type of Spark and identify each row with his schema.

For more information for this output you can visit the :doc:`elasticsearch`

* nodes:
   This parameter specified the different nodes of a cluster of ElasticSearch.
   Is possible omit this parameter in policy.

   * Sample:
::

   "connectionHost": ("ip_seed1,ip_seed2,ip_seed3,...")  Default: "localhost"

* defaultPort:
   This parameter specified the port to connect.
   Is possible omit this parameter in policy.

   * Sample:
::

   "defaultPort": ("PORT_NUMBER")  Default: "9200"

* defaultAnalyzerType:
   Is possible specify the analyzer for text index fields.
   Is possible omit this parameter in policy.

   * Sample:
::

   "defaultAnalyzerType": ("english"/"spanish"/"custom"...)  Default: None

* idField:
   Is possible specify the id field that contains the unique id for the row.
   Is possible omit this parameter in policy.

   * Sample:
::

   "idField": ("ID_NAME")  Default: "id"

* indexMapping:
   This parameter assign the mapping for the index, is possible auto generate mappings for the indexes with the date.
   Is possible omit this parameter in policy.

   * Sample:
::

   "indexMapping": ("second"/"minute"/"hour"/"day"/"month"/"year")  Default: "sparkta"

* dateType:
   Is possible specify the type of the date fields.
   Is possible omit this parameter in policy.

   * Sample:
::

   "dateType": ("timestamp"/"ANY_NAME")  Default: None


.. _redis-label:

Redis Configuration
==========

The output of Redis not use the generic implementation with DataFrames, this implementation save each
UpdateMetricOperation in redis hash sets.

For more information for this output you can visit the :doc:`redis`

* hostname:
   This parameter specified the Ip of a Redis host.
   Is possible omit this parameter in policy.

   * Sample:
::

   "hostname": ("ip_host")  Default: "localhost"

* port:
   This parameter specified the port to connect.
   Is possible omit this parameter in policy.

   * Sample:
::

   "port": ("PORT_NUMBER")  Default: "6379"


.. _print-label:

Print Configuration
==========

The output of Print use the generic implementation with DataFrames, this implementation print each dataframe with his
 schema.
