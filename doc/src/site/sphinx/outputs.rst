  
Outputs Configurations
************************

- :ref:`generic-label`

- :ref:`mongodb-label`

- :ref:`cassandra-label`

- :ref:`elasticsearch-label`

- :ref:`redis-label`

- :ref:`print-label`

- :ref:`parquet-label`

- :ref:`csv-label`


.. _generic-label:

Generic Configuration
=======================

In the SDK you can find the model that an output has to follow to be implemented.It has several settings that can modify system operation.

These parameters can be set in the policy file:


+-----------------------+----------------------------------------------------------+----------+-----------------------+
| Property              | Description                                              | Optional | Default               |
+=======================+==========================================================+==========+=======================+
| replication_factor    | Required if class is SimpleStrategy; otherwise, not used | Yes      | 1                     |
|                       | The parameter specifies the number of replicas of data   |          |                       |
|                       | on multiple nodes.                                       |          |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| fixedDimensions       | You can specify dimensions that will be fixed to         | Yes      | ""                    |
|                       | calculate the multiplexer. This way you can obtain       |          |                       |
|                       | fixed dimensions and a small number of tables and        |          |                       |
|                       | possibilities.                                           |          |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| fieldsSeparator       | The character that separate the fields to the others     | Yes      | ,                     |
|                       | parameters.                                              |          |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| isAutoCalculateId     | If it's true it will generate the primary key of the     | Yes      | false                 |
|                       | register with the value of the dimensions and the        |          |                       |
|                       | dimensionTime                                            |          |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| fixedAggregation      | It's possible to specify one fixed aggregation with      | Yes      | None                  |
|                       | value for all dimensions.                                |          |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+


.. _mongodb-label:

MongoDB Configuration
==========================
* Sample:
::

  "outputs": [
    {
      "name": "out-mongo",
      "type": "MongoDb",
      "configuration": {
        "hosts": [{"host": "localhost" , "port": "27017" }],
        "dbName": "sparkta",
        "identitiesSaved": "true",
        "idAsField": "true"
      }
    }
The output of MongoDB uses the generic implementation with DataFrames, it has multiple configuration
parameters to connect to the DB and self-creation of indexes.


+-----------------------+----------------------------------------------------+----------+---------------------------+
| Property              | Description                                        | Optional | Default                   |
+=======================+====================================================+==========+===========================+
| hosts                 | This parameter connection specifies the            | Yes      | localhost:27017           |
|                       | nodes of a cluster of MongoDB, with different      |          |                           |
|                       | replica set or with sharding.                      |          |                           |
+-----------------------+----------------------------------------------------+----------+---------------------------+
| dbName                | The name of the database                           | Yes      | "sparkta"                 |
+-----------------------+----------------------------------------------------+----------+---------------------------+
| connectionsPerHost    | Number of connections per host                     | Yes      | 5                         |
+-----------------------+----------------------------------------------------+----------+---------------------------+
| threadsAllowedToBlock | This multiplier, multiplied with the               | Yes      | 10                        |
|                       | connectionsPerHost setting, gives the maximum      |          |                           |
|                       | number of threads that may be waiting for a        |          |                           |
|                       | connection to become available from the pool.      |          |                           |
+-----------------------+----------------------------------------------------+----------+---------------------------+
| idAsField             | It's possible to save all fields that compound the | Yes      | false                     |
|                       | unique key as a independent field.                 |          |                           |
+-----------------------+----------------------------------------------------+----------+---------------------------+
| textIndexFields       | The system is capable of insert data in a full-text| Yes      |                           |
|                       | index. All of this fields compound the index.      |          |                           |
+-----------------------+----------------------------------------------------+----------+---------------------------+
| language              | Specify the language of the tokenizer in the       | Yes      | None                      |
|                       | full-text index in MongoDB, each document          |          |                           |
|                       | inserted must have this key-value.                 |          |                           |
+-----------------------+----------------------------------------------------+----------+---------------------------+
| retrySleep            | The number of milliseconds to wait for reconnect   | Yes      | 1000                      |
|                       | with MongoDB nodes when the last client fails. It  |          |                           |
|                       | is recommendable to set less time to the slide     |          |                           |
|                       | interval of the streaming window.                  |          |                           |
+-----------------------+----------------------------------------------------+----------+---------------------------+



.. _cassandra-label:

Cassandra Configuration
==============================
* Sample:
::

  "outputs": [
    {
      "name": "out-cassandra",
      "type": "Cassandra",
      "configuration": {
        "connectionHost": "127.0.0.1",
        "connectionPort": "9142",
        "cluster": "Test Cluster",
        "keyspace": "sparkta"
      }
    }
  ]

The Cassandra output uses the generic implementation with DataFrames.


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
| clusteringDimensions  | Clustering columns for the primary key.                  | Yes      |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| indexFields           | The indexed fields, could be any aggregate field         | Yes      |                       |
|                       | or clustering column field.                              |          |                       |
+-----------------------+----------------------------------------------------------+----------+-----------------------+
| textIndexFields       | The text index fields, this feature is for the Stratio's | Yes      |                       |
|                       | Cassandra Lucene Index                                   |          |                       |
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

In Cassandra each cube define one table, but when you modify the policy that involve this cube, Sparkta create a new
table with the next version. The name of all tables are "dimensions_v1" when modify the policy the new table in
Cassandra is "dimensions_v2".


.. _elasticsearch-label:

ElasticSearch Configuration
==============================
* Sample:
::


   "outputs": [
    {
      "name": "out-elasticsearch",
      "type": "ElasticSearch",
      "configuration": {
        "nodes": [
          {
            "node": "localhost",
            "defaultPort": "9200"
          }
        ],
        "indexMapping": "day"
      }
    }
   ]
The Elasticsearch output uses the generic implementation with DataFrames.



+--------------------------+-----------------------------------------------+----------+-----------------------+
| Property                 | Description                                   | Optional | Default               |
+==========================+===============================================+==========+=======================+
| nodes                    | Nodes of a cluster of ElasticSearch.          | Yes      | localhost             |
+--------------------------+-----------------------------------------------+----------+-----------------------+
| defaultPort              | The port to connect with ElasticSearch.       | Yes      | 9200                  |
+--------------------------+-----------------------------------------------+----------+-----------------------+
| idField                  | Field used as unique id for the row.          | Yes      | "id"                  |
+--------------------------+-----------------------------------------------+----------+-----------------------+
| indexMapping             | Field used as mapping for the index.          | Yes      | "sparkta"             |
+--------------------------+-----------------------------------------------+----------+-----------------------+
| dateType                 | The type of the date fields.                  | Yes      | None                  |
+--------------------------+-----------------------------------------------+----------+-----------------------+

In ElasticSearch each cube define one index, but when you modify the policy that involve this cube, Sparkta create a new
mapping with the next version. The name of all tables are the dimensions separated by '_' and the default mapping is
"sparkta_v1" when modify the policy the new mapping in ElasticSearch is "sparkta_v2".


.. _redis-label:

Redis Configuration
====================
* Sample:
::

  "outputs": [
    {
      "name": "out-redis",
      "type": "Redis",
      "configuration": {
        "hostname": "localhost",
        "port": 63790
      }
    }
  ]
The output of Redis doesn't use the generic implementation with DataFrames.



+--------------------------+-----------------------------------------------+----------+-----------------------+
| Property                 | Description                                   | Optional | Default               |
+==========================+===============================================+==========+=======================+
| hostname                 | The Ip of a Redis host.                       | Yes      | localhost             |
+--------------------------+-----------------------------------------------+----------+-----------------------+
| port                     | The port to connect with ElasticSearch.       | Yes      | 9200                  |
+--------------------------+-----------------------------------------------+----------+-----------------------+


.. _print-label:

Print Configuration
====================
* Sample:
::

  "outputs": [
    {
      "name": "out-print",
      "type": "Print",
      "configuration": {
      }
    }
  ]
The print output uses the generic implementation with DataFrames, this implementation print each dataframe with his
 schema.

.. _parquet-label:

Parquet Configuration
====================
* Sample:
::

  "outputs": [
    {
      "name": "out-parquet",
      "type": "Parquet",
      "jarFile" : "output-parquet-plugin.jar",
      "configuration": {
        "path": "/tmp/sparkta/operators/parquet",
        "datePattern": "yyyy/MM/dd"
      }
    }
  ]

The parquet output uses generic implementation of DataFrames.

+--------------------------+-----------------------------------------------+----------+-----------------------+
| Property                 | Description                                   | Optional | Default               |
+==========================+===============================================+==========+=======================+
| path                     | Destination path to store info.               | No       |                       |
+--------------------------+-----------------------------------------------+----------+-----------------------+


.. _csv-label:

Csv Configuration
============
* Sample:
::

  "outputs": [
    {
      "name": "out-csv",
      "type": "Csv",
      "configuration": {
        "path": "/tmp/sparkta/operators/csv/",
        "header": "true",
        "delimiter": ","
      }
    }
  ]

+--------------------------+-----------------------------------------------+----------+-----------------------+
| Property                 | Description                                   | Optional | Default               |
+==========================+===============================================+==========+=======================+
| path                     | Destination path to store info.               | Yes      | None                  |
+--------------------------+-----------------------------------------------+----------+-----------------------+
| header                   | Indicates if the file has header or not.      | Yes      | false                 |
+--------------------------+-----------------------------------------------+----------+-----------------------+
| delimiter                | Fields are separated by the delimiter.        | Yes      | ","                   |
+--------------------------+-----------------------------------------------+----------+-----------------------+
| datePattern              | Indicates the date pattern of the file.       | Yes      | None                  |
+--------------------------+-----------------------------------------------+----------+-----------------------+
| dateGranularity          | Specify the granularity from second to year   | Yes      | Day                   |
+--------------------------+-----------------------------------------------+----------+-----------------------+
