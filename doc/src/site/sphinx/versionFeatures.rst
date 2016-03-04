Release Notes
****************

Version 0.6
=============

- Upgrade Spark version to 1.4.1

- User Interface to help to create your policies

- Automatic deployment in Mesos cluster

- Policy status lifecycle

- New WebSocket input

- Elasticsearch output updated to version 1.7

- New EntityCount and TotalEntityCount operators

- Filters in operators



Version 0.5
=============

- Added compatibility with Spark 1.4

- Parquet and CSV outputs.

- Improved Twitter input. Possibility to filter tweets by hashtag.

- Fixed important bug in Elasticsearch output. Fixed are mapped according to their type.

- Policy refactor. Improved semantic of the JSON.

- Support for fragment composition in policies.

- Bugfixing



Version 0.4
=============

Driver
------
- **Akka Actors** (Supervisor and StreamingContextActor)
- Json parser
- First approach streaming context generator

Aggregator
----------
- Dimensions
- Cubes

Plugins
-------
- Tag dimension
- Hierarchy dimension
- Max operator
- Min operator
- Sum operator
- Avg operator
- Median operator
- Variance operator
- Stddev operator
- Datetime parser
- Full-Text operator
- Accumulator operator
- Last Value operator
- Morphlines parser ( **Kite Sdk** )
- Output MongoDB:
    - Count aggregation update
    - Max aggregation update
    - Min aggregation update
- Output Print
- Input Flume
- Input Kafka
- Input Socket
- Input Twitter
- Default dimension
- DateTime dimension
- GeoHash dimension
- Count operator
- KeyValue parser
- Temporal TwitterParser (demo purposes)
- Multiplex cubes for **multi-cube** integration
- BulkOperation in MongoDB Output
- Auto creation Id and Full-Text indexes in MongoDB Output
- Update aggregations commands in MongoDB:
    - Avg aggregation update
    - Set update
    - AddToSet aggregation update
- Save Raw data in **Parquet** files
- Auto detection schema with policies
- Reflexions for native plugins
- **Swagger API**:
    - Interactive documentation
    - Client SDK generation and discoverability
- Compatibility with multiple outputs
- Output **Cassandra**:
    - Auto tables creation
    - Auto primary key and column families detection
    - Auto index creation
- Output **ElasticSearch**:
    - Options in mapping date types
    - Auto index type with time fields
- Output **Redis**
- Input **Kafka Direct Streaming** integrated in Spark 1.3.1
- Input **RabbitMq**

Parent
------
- Created new maven modules:
    - Doc: Documentation module.
    - Serving-core: Simple library to read aggregation data.
    - Serving-api: REST api implementing serving-core.
    - Plugins: Now, the platform is **pluggable**. This module contains all **official** plugins.
    - Sdk: This library can be used to develop new plugins to adapt Sparta to your necessities.
- Now a distribution is generated
- SandBox Stratio Sparta

Sdk
---
- Multiplex cubes for **multi-cube** integration
- Integration with **Spark 1.3.0**
- Auto create time series in outputs if not exist time dimension
- Full integration in Outputs with **Spark DataFrames**
- Auto create "Id" in DataFrames with dimensions
