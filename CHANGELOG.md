# CHANGELOG

## v0.6 (upcoming)


## v0.5 (July 2015)

**Sdk**
- Added compatibility with Apache Spark 1.4.x
- Released a specific distribution for Apache Spark 1.3.x

**Serving-Api**
- Support for fragment composition in policies.
- Policy refactor. Improved semantic of the JSON.

**Plugins**
- Parquet and CSV outputs.
- Improved Twitter input. Possibility to filter tweets by hashtag.
- Fixed important bug in Elasticsearch output. Fields are mapped according to their type.

## v0.4 (May 2015)

**Parent**
- SandBox Stratio Sparkta

**Sdk**
- Integration with **Spark 1.3.1**
- Auto create time series in outputs if not exist time dimension
- Full integration in Outputs with **Spark DataFrames**
- Auto create "Id" in DataFrames with dimensions

**Driver**
- Save Raw data in **Parquet** files
- Auto detection schema with policies
- Reflexions for native plugins
- **Swagger API**:
   - Interactive documentation
   - Client SDK generation and discoverability
   - Compatibility with multiple oputputs

**Plugins**
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







