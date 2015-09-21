# Changelog

Only listing significant user-visible, not internal code cleanups and minor bug fixes.

## 0.6.0 (September 2015)

- Upgrade Spark version to 1.4.1
- User Interface to help create your cubes
- Automatic deployment in Mesos cluster
- Policy status lifecycle
- New WebSocket input
- Elasticsearch output updated to version 1.7
- New EntityCount and TotalEntityCount operators
- Filters in operators

## 0.5.0 (July 2015)

- Added compatibility with Apache Spark 1.4.x
- Released a specific distribution for Apache Spark 1.3.x
- Support for fragment composition in policies.
- Policy refactor. Improved semantic of the JSON.
- Parquet and CSV outputs.
- Improved Twitter input. Possibility to filter tweets by hashtag.
- Fixed important bug in Elasticsearch output. Fields are mapped according to their type.

## 0.4.0 (May 2015)

- SandBox Stratio Sparkta
- Integration with **Spark 1.3.1**
- Auto create time series in outputs if not exist time dimension
- Full integration in Outputs with **Spark DataFrames**
- Auto create "Id" in DataFrames with dimensions
- Save Raw data in **Parquet** files
- Auto detection schema with policies
- Reflexions for native plugins
    - Interactive documentation
    - Client SDK generation and discoverability
    - Compatibility with multiple oputputs
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







