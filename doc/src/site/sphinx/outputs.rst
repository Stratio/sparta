
Outputs Configurations
************************

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
=======================

In the SDK you can find the model that must follow an output to be implemented.It has several settings that can modify system operation.

For more information for this output you can visit the :doc:`dataframes`

These parameters can be completed in the policy file:

* multiplexer:
   If you want to multiplex all possible combinations that occur within a cube,so that the outputs are saved
   multiple "tables".
   With this parameter the possibility of multi cubes and the possibility of generating it implements a data
   aggregation lake.
   You can omit this parameter in the policy.


   * Example:
::

   "multiplexer": ("true"/"false")  Default: "false"

* fixedDimensions:
* fixedDimensions:
   You can specify fields that will be fixed for the calculation of the multiplex, in this way can obtain fixed
   dimensions and a smaller number of tables and possibilities
   You can omit this parameter in the policy.

   * Example:
::

   "fixedDimensions": ("dimension1{fieldsSeparator}dimension2{fieldsSeparator}...")  Default: ""

* fieldsSeparator:
   Is possible specify the character that separate the fields for the others parameters.
   You can omit this parameter in the policy.

   * Example:
::

   "fieldsSeparator": ("any_character")  Default: ","

* isAutoCalculateId:
   The system is capable of assigning an id added to each event, so that it may identify only the output.
   This field is calculated with all the values of the fields, including timeDimension date if the parameter is specified.
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
==========================

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
   This field is calculated with all the values of the fields, including timeDimension date if the parameter is specified.
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
==============================

The output of Cassandra use the generic implementation with DataFrames, this implementation transform each
UpdateMetricOperation to Row type of Spark and identify each row with his schema.

For more information for this output you can visit the :doc:`cassandra`


+-----------------------+----------------------------------------------------------+----------+-----------------------+
| Property              | Description                                              | Optional | Default               |
+=======================+==========================================================+==========+=======================+
| connectionHost        | Different seeds of a cluster of Cassandra.               | Yes      | Yes                   |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| cluster               | The name of the cluster.                                 | Yes      | Yes                   |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| keyspace              | The name of the KeySpace                                 | Yes      | Yes                   |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| keyspaceClass         | The class of KeySpace.                                   | Yes      | SimpleStrategy        |
|                       |  * SimpleStrategy: Use it only for a single data center. |          |                       |
|                       |    SimpleStrategy places the first replica on a node     |          |                       |
|                       |    determined by the partitioner.Additional replicas     |          |                       |
|                       |    are placed on the next nodes clockwise in the         |          |                       |
|                       |    cassandra's ring without considering any topology     |          |                       |
|                       |    (rack or datacenter location).                        |          |                       |
|                       |  * NetworkTopologyStrategy:Use NetworkTopologyStrategy   |          |                       |
|                       |    when you have (or plan to have) your cluster deployed |          |                       |
|                       |    across multiple data centers. This strategy specifies |          |                       |
|                       |    how many replicas you want in each data center.       |          |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| replication_factor    | Required if class is SimpleStrategy; otherwise, not used | Yes      | 1                     |
|                       | The parameter specifies the number of replicas of data   |          |                       |
|                       | on multiple nodes.                                       |          |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| compactStorage        | The compact storage directive is used for backward       | Yes      | None                  |
|                       | compatibility of CQL 2 applications and data in the      |          |                       |
|                       | legacy (Thrift) storage engine format. To take advantage |          |                       |
|                       | of CQL 3 capabilities, do not use this directive in new  |          |                       |
|                       | applications. When you create a table using compound     |          |                       |
|                       | primary keys, for every piece of data stored,            |          |                       |
|                       | he column name needs to be stored along with it.         |          |                       |
|                       | Instead of each non-primary key column being stored      |          |                       |
|                       | such that each column corresponds to one column on disk, |          |                       |
|                       | an entire row is stored in a single column on disk,      |          |                       |
|                       | hence the name compact storage.                          |          |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| fieldsSeparator       | The character that separate the fields in the            | Yes      | ,                     |
|                       | "textIndexFields" parameter.                             |          |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| clusteringDimensions  | Clustering columns for the primary key.                  | Yes      |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| indexFields           | The indexed fields, could be any aggregate field         | Yes      |                       |
|                       | or clustering column field.                              |          |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| textIndexFields       | The text index fields, this feature is for the Stratio's | Yes      |                       |
|                       |  Cassandra Lucene Index                                  |          |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| analyzer              | The analyzer for text index fields, this feature is for  | Yes      | None                  |
|                       | the Stratio's Cassandra Lucene Index                     |          |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| refreshSeconds        | The number of seconds between refresh lucene index       | Yes      | 1                     |
|                       | operations, this feature is for the Stratio's Cassandra  |          |                       |
|                       | Lucene Index                                             |          |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| dateFormat            | The date format for the date fields indexed, this        | Yes      | yyyy/mm/dd            |
|                       | feature is for the Stratio's Cassandra Lucene Index      |          |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+

.. _elasticsearch-label:

ElasticSearch Configuration
==============================

The output of ElasticSearch use the generic implementation with DataFrames, this implementation transform each
UpdateMetricOperation to Row type of Spark and identify each row with his schema.

For more information for this output you can visit the :doc:`elasticsearch`



+--------------------------+-----------------------------------------------+----------+
| Property                 | Description                                   | Optional |
+==========================+===============================================+==========+
| nodes                    | Nodes of a cluster of ElasticSearch.          | Yes      |
+--------------------------+-----------------------------------------------+----------+
| defaultPort              | The port to connect with ElasticSearch.       | Yes      |
+--------------------------+-----------------------------------------------+----------+
| idField                  | Field used as unique id for the row.          | Yes      |
+--------------------------+-----------------------------------------------+----------+
| indexMapping             | Field used as mapping for the index.          | Yes      |
+--------------------------+-----------------------------------------------+----------+
| dateType                 | The type of the date fields.                  | Yes      |
+--------------------------+-----------------------------------------------+----------+


.. _redis-label:

Redis Configuration
====================

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
====================

The print output uses the generic implementation with DataFrames, this implementation print each dataframe with his
 schema.

.. _parquet-label:

Parquet Configuration
====================

The parquet output uses generic implementation of DataFrames.

+--------------------------+-----------------------------------------------+----------+
| Property                 | Description                                   | Optional |
+==========================+===============================================+==========+
| path                     | Destination path to store info.               | No       |
+--------------------------+-----------------------------------------------+----------+
