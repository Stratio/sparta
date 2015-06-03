
Outputs Configurations
******************

- :ref:`generic-label`

- :ref:`mongodb-label`

- :ref:`cassandra-label`

- :ref:`elasticsearch-label`

- :ref:`redis-label`

- :ref:`print-label`

- :ref:`parquet-label`


.. image:: images/outputs.png
   :height: 400 px
   :width: 420 px
   :align: center
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
   With this parameter the possibility of multi cubes and the possibility of generating it implements a data
   aggregation lake.
   You can omit this parameter in the policy.


   * Example:
::

   "multiplexer": ("true"/"false")  Default: "false"

* fixedBuckets:
   You can specify fields that will be fixed for the calculation of the multiplex, in this way can obtain fixed
   dimensions and a smaller number of tables and possibilities
   You can omit this parameter in the policy.

   * Example:
::

   "fixedBuckets": ("bucket1{fieldsSeparator}bucket2{fieldsSeparator}...")  Default: ""

* fieldsSeparator:
   Is possible specify the character that separate the fields for the others parameters.
   You can omit this parameter in the policy.

   * Example:
::

   "fieldsSeparator": ("any_character")  Default: ","

* isAutoCalculateId:
   The system is capable of assigning an id added to each event, so that it may identify only the output.
   This field is calculated with all the values of the bucket rollup, including timeBucket date if the parameter is specified.
   Only for DataFrames persistence, disable in Tuple -> (DimensionValuesTime, Aggregations).
   You can omit this parameter in the policy.

   * Example:
::

   "isAutoCalculateId": ("true"/"false")  Default: "false"

* fixedAggregation:
   It's possible to specify one fixed aggregation with value for all dimensions.
   You can omit this parameter in the policy.

   * Example:
::

   "fixedAggregation": ("NAME:VALUE")  Default: None

.. _mongodb-label:

MongoDB Configuration
==========

The output of MongoDB does not use the generic implementation with DataFrames, it has multiple configuration
parameters to connect to the DB and self-creation of indexes.

For more information for this output you can visit the :doc:`mongodb`

* mongoClientUri:
   This parameter Connection routes specified the different nodes of a cluster of MongoDB, with replica set or with sharding.
   You can omit this parameter in the policy.

   * Example:
::

   "mongoClientUri": ("mongodb://localhost:27017")  Default: "mongodb://localhost:27017"

* dbName:
   The system is capable of assigning an id added to each event, so that it may identify only the output.
   This field is calculated with all the values of the bucket rollup, including timeBucket date if the parameter is specified.
   Only for DataFrames persistence, disable in UpdateMetricOperation.
   You can omit this parameter in the policy.

   * Example:
::

   "dbName": ("DATABASE_NAME")  Default: "sparkta"

* connectionsPerHost:
   Number of connections per host that the system open.
   You can omit this parameter in the policy.

   * Example:
::

   "connectionsPerHost": ("NUMBER")  Default: "5"

* threadsAllowedToBlock:
   This multiplier, multiplied with the connectionsPerHost setting, gives the maximum number of threads that may be waiting for a connection to become available from the pool.
   You can omit this parameter in the policy.

   * Example:
::

   "threadsAllowedToBlock": ("NUMBER")  Default: "10"

* textIndexFields:
   The system is capable of insert data in a full-text index. All of this fields compound the index.
   You can omit this parameter in the policy.

   * Example:
::

   "textIndexFields": ("field1,field2")  Default: ""

* language:
   Specify the language of the tokenizer in the full-text index in MongoDB, each document inserted must have this
   key-value.
   You can omit this parameter in the policy.

   * Example:
::

   "language": ("english"/"spanish"...)  Default: "none"

* retrySleep:
   It is possible to assign the number of milliseconds to wait for reconnect with MongoDb nodes when the last client
   fails.
   It is recommendable to set less time to the slide interval of the streaming window.
   You can omit this parameter in the policy.

   * Example:
::

   "retrySleep": (MILLISECONDS)  Default: 1000


.. _cassandra-label:

Cassandra Configuration
==========

The output of Cassandra use the generic implementation with DataFrames, this implementation transform each
UpdateMetricOperation to Row type of Spark and identify each row with his schema.

For more information for this output you can visit the :doc:`cassandra`

* connectionHost:
   This parameter specifies the different seeds of a cluster of Cassandra.
   You can omit this parameter in the policy.

   * Example:
::

   "connectionHost": ("ip_seed1,ip_seed2,ip_seed3,...")  Default: "127.0.0.1"

* cluster:
   This parameter specifies the name of the cluster.
   You can omit this parameter in the policy.

   * Example:
::

   "cluster": ("CLUSTER_NAME")  Default: "Test Cluster"

* keyspace:
   This parameter specifies the name of the keyspace.
   You can omit this parameter in the policy.

   * Example:
::

   "keyspace": ("KEYSPACE_NAME")  Default: "sparkta"

* keyspaceClass:
   - SimpleStrategy:
   Use it only for a single data center. SimpleStrategy places the first replica on a node determined by the partitioner. Additional replicas are placed on the next nodes clockwise in the cassandra's ring without considering any topology (rack or data center location).
   - NetworkTopologyStrategy:
   Use NetworkTopologyStrategy when you have (or plan to have) your cluster deployed across multiple data centers. This strategy specifies how many replicas you want in each data center.
   You can omit this parameter in the policy.

   * Example:
::

   "keyspaceClass": ("SimpleStrategy"/"NetworkTopologyStrategy")  Default: "SimpleStrategy"

* replication_factor:
   Required if class is SimpleStrategy; otherwise, not used. The parameter specifies the number of replicas of data on multiple nodes.
   You can omit this parameter in the policy.

   * Example:
::

   "replication_factor": ("NUMBER")  Default: "1"

* compactStorage:
   The compact storage directive is used for backward compatibility of CQL 2 applications and data in the legacy (Thrift) storage engine format. To take advantage of CQL 3 capabilities, do not use this directive in new applications. When you create a table using compound primary keys, for every piece of data stored, the column name needs to be stored along with it. Instead of each non-primary key column being stored such that each column corresponds to one column on disk, an entire row is stored in a single column on disk, hence the name compact storage.
   You can omit this parameter in the policy.

   * Example:
::

   "compactStorage": ("ANY")  Default: None

* fieldsSeparator:
   It's possible to specify the character that separate the fields in the "textIndexFields" parameter.
   You can omit this parameter in the policy.

   * Example:
::

   "fieldsSeparator": ("any_character")  Default: ","

* clusteringBuckets:
   It's possible to specify the clustering columns for the primary key.
   You can omit this parameter in the policy.

   * Example:
::

   "clusteringBuckets": ("bucket1,bucket2,bucket3...")  Default: ""

* indexFields:
   It's possible to specify the indexed fields, could be any aggregate field or clustering column field.
   You can omit this parameter in the policy.

   * Example:
::

   "indexFields": ("bucket1,bucket2,bucket3, ...")  Default: ""

* textIndexFields:
   It's possible to specify the text index fields, this feature is for the Stratio Cassandra.
   You can omit this parameter in the policy.

   * Example:
::

   "textIndexFields": ("bucket1:type,bucket2:type,bucket3:type,aggregate1:type, aggregate2:type, ...")  Default: ""

      type: "string/text/date/integer/long/double/...."

* analyzer:
   It's possible to specify the analyzer for text index fields, this feature is for the Stratio Cassandra.
   You can omit this parameter in the policy.

   * Example:
::

   "analyzer": ("english"/"spanish"...)  Default: None

* textIndexFieldsName:
   It's possible to specify the name of the text index, this feature is for the Stratio Cassandra.
   You can omit this parameter in the policy.

   * Example:
::

   "textIndexFieldsName": ("NAME")  Default: "lucene"

* refreshSeconds:
   It's possible to specify the number of seconds between refresh lucene index operations, this feature is for the
   Stratio Cassandra.
   You can omit this parameter in the policy.

   * Example:
::

   "refreshSeconds": ("NUMBER")  Default: "1"

* dateFormat:
   It's possible to specify the date format for the date fields indexed, this feature is for the
   Stratio Cassandra.
   You can omit this parameter in the policy.

   * Example:
::

   "dateFormat": ("SimpleDateFormat")  Default: "yyyy/mm/dd"


.. _elasticsearch-label:

ElasticSearch Configuration
==========

The output of ElasticSearch use the generic implementation with DataFrames, this implementation transform each
UpdateMetricOperation to Row type of Spark and identify each row with his schema.

For more information for this output you can visit the :doc:`elasticsearch`

* nodes:
   This parameter specified the different nodes of a cluster of ElasticSearch.
   You can omit this parameter in the policy.

   * Example:
::

   "connectionHost": ("ip_seed1,ip_seed2,ip_seed3,...")  Default: "localhost"

* defaultPort:
   This parameter specified the port to connect.
   You can omit this parameter in the policy.

   * Example:
::

   "defaultPort": ("PORT_NUMBER")  Default: "9200"

* defaultAnalyzerType:
   It's possible to specify the analyzer for text index fields.
   You can omit this parameter in the policy.

   * Example:
::

   "defaultAnalyzerType": ("english"/"spanish"/"custom"...)  Default: None

* idField:
   It's possible to specify the id field that contains the unique id for the row.
   You can omit this parameter in the policy.

   * Example:
::

   "idField": ("ID_NAME")  Default: "id"

* indexMapping:
   This parameter assign the mapping for the index, it's possible to auto generate mappings for the indexes with the date.
   You can omit this parameter in the policy.

   * Example:
::

   "indexMapping": ("second"/"minute"/"hour"/"day"/"month"/"year")  Default: "sparkta"

* dateType:
   It's possible to specify the type of the date fields.
   You can omit this parameter in the policy.

   * Example:
::

   "dateType": ("timestamp"/"ANY_NAME")  Default: None


.. _redis-label:

Redis Configuration
==========

The output of Redis not use the generic implementation with DataFrames, this implementation save each
UpdateMetricOperation in redis hash sets.

For more information for this output you can visit the :doc:`redis`

* hostname:
   This parameter specifies the Ip of a Redis host.
   You can omit this parameter in the policy.

   * Example:
::

   "hostname": ("ip_host")  Default: "localhost"

* port:
   This parameter specifies the port to connect.
   You can omit this parameter in the policy.

   * Example:
::

   "port": ("PORT_NUMBER")  Default: "6379"


.. _print-label:

Print Configuration
==========

The print output uses the generic implementation with DataFrames, this implementation print each dataframe with his
 schema.

.. _parquet-label:

Parquet Configuration
==========

The parquet output uses generic implementation of DataFrames. This output has the following parameters:

* path:
   Destination path to store info. Required.

   * Example:
::

   "path": "file:///path-to-parquet-ds"

* datePattern:
   You can specify a formatting pattern for dates. This is for split subfolders

   * Example:
::

  "dateFormat": "yyyy/MM/dd" ==> "/path-to-parquet-ds/agg-name/2011/07/19/..."
