# CHANGELOG


## v0.1

###Driver
- **Akka Actors** (Supervisor and StreamingContextActor)
- Json parser
- First approach streaming context generator

###Aggregator
- Dimensions
- Cubes

###Plugins
- Pass through bucketer
- DateTime bucketer
- GeoHash bucketer
- Count operator
- First approach **MongoDB** output
- KeyValue parser
- Temporal TwitterParser (demo purposes)


## v0.2

###Parent
- Created new maven modules:
    - Doc: Documentation module.
    - Serving-core: Simple library to read aggregation data.
    - Serving-api: REST api implementing serving-core.
    - Plugins: Now, the platform is **pluggable**. This module contains all **official** plugins.
    - Sdk: This library can be used to develop new plugins to adapt Sparkta to your necessities.
- Now a distribution is generated

###Plugins
- Tag bucketer
- Hierarchy bucketer
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


## v0.3

###Sdk
- Multiplex cubes for **multi-cube** integration

###Plugins
- Avg operator
- Median operator
- Variance operator
- Stddev operator
- Input Twitter
- BulkOperantion in MongoDB Output
- Auto creation Id and Full-Text indexes in MongoDB Output
- Update aggregations commands in MongoDB:
    - Avg aggregation update
    - Set update
    - AddToSet aggregation update


## v0.4


###Parent
- SandBox Stratio Sparkta


###Sdk
- Integration with **Spark 1.3.1**
- Auto create time series in outputs if not exist time bucket
- Full integration in Outputs with **Spark DataFrames**
- Auto create "Id" in DataFrames with dimensions


###Driver
- Save Raw data in **Parquet** files
- Auto detection schema with policies
- Reflexions for native plugins
- **Swagger API**:
    - Interactive documentation
    - Client SDK generation and discoverability
- Compatibility with multiple oputputs

###Plugins
- Output **Cassandra**:
    - Auto tables creation
    - Auto primary key and column families detection
    - Auto index creation
- Output **ElasticSearch**:
    - Options in mapping date types
    - Auto index type with time fields
- Output **Redis**
- Input **Kafka Direct Streaming** integrated in Spark 1.3.0
- Input **RabbitMq**
- Full-Text operator
- Accumulator operator
- Last Value operator


