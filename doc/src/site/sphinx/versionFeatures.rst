Version Features
****************


Version 0.1.0
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
- Default dimension
- DateTime dimension
- GeoHash dimension
- Count operator
- First approach **MongoDB** output
- KeyValue parser
- Temporal TwitterParser (demo purposes)


Version 0.2.0
=============

Parent
------
- Created new maven modules:
    - Doc: Documentation module.
    - Serving-core: Simple library to read aggregation data.
    - Serving-api: REST api implementing serving-core.
    - Plugins: Now, the platform is **pluggable**. This module contains all **official** plugins.
    - Sdk: This library can be used to develop new plugins to adapt Sparkta to your necessities.
- Now a distribution is generated

Plugins
-------
- Tag dimension
- Hierarchy dimension
- Max operator
- Min operator
- Sum operator
- Datetime parser
- Morphlines parser ( **Kite Sdk** )
- Output MongoDB:
    - Count aggregation update
    - Max aggregation update
    - Min aggregation update
- Output Print
- Input Flume
- Input Kafka
- Input Socket


Version 0.3.0
=============

Sdk
---
- Multiplex rollups for **multi-cube** integration

Plugins
-------
- Avg operator
- Median operator
- Variance operator
- Stddev operator
- Input Twitter
- BulkOperation in MongoDB Output
- Auto creation Id and Full-Text indexes in MongoDB Output
- Update aggregations commands in MongoDB:
    - Avg aggregation update
    - Set update
    - AddToSet aggregation update


Version 0.4.0
=============

Parent
------
- SandBox Stratio Sparkta

Sdk
---
- Integration with **Spark 1.3.0**
- Auto create time series in outputs if not exist time bucket
- Full integration in Outputs with **Spark DataFrames**
- Auto create "Id" in DataFrames with dimensions

Driver
------
- Save Raw data in **Parquet** files
- Auto detection schema with policies
- Reflexions for native plugins
- **Swagger API**:
    - Interactive documentation
    - Client SDK generation and discoverability
- Compatibility with multiple outputs

Plugins
-------
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
- Full-Text operator
- Accumulator operator
- Last Value operator
