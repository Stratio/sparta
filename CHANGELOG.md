# Changelog

## 2.9.0 (upcoming)

* Improvements and new features:
    - [SPARTA-3128] Multitenant login
    - Integration with DCOS 1.12
    - Upgrade to new Gosec Facade version
    - Upgrade dyplon-crossdata version to 0.19.1-e67b2ab  
    
* Bug fixing:
    - [SPARTA-3093] Add user to jdbc connection string if not specified
    - [INTELL-2148] Fix Model repository method to download Spark pipeline models from repository

## 2.8.1 (June 04, 2019)

* Bug fixing
    - [SPARTA-3085] Rest steps: support special characters in placeholder replacement
    - Fix quality rules security
    - Marathon error management: add retry policy
    - Fix metrics query executions performance 
    - [SPARTA-3059] Export execution parameters together with metrics
    - [SPARTA-3075] Support case sensitive table names in Postgres and Jdbc outputs
    - [SPARTA-3070] Improve performance when loading latest version of plugins at runtime

## 2.8.0 (May 17, 2019)

* Improvements and new features:
    - Quality Rules
    - Scheduler
    - Integration with Schema Registry for Kafka Input/Output step
    - CrossdataInput Streaming: improve polling when using JDBC tables
    - Upgrade dg-commons to 1.0.0-0c72260
    - Upgrade gosec-dyplon to 0.16.0-988a912
    - Upgrade crossdata-dyplon to 0.15.5-a6e5b87
    - Upgrade crossdata to 2.16.0-fc09c7d
    - Upgrade mlmodelrep to 1.4.0-8e4d1b0

* Bug fixing
    - [SPARTA-3067] Allow whitespaces in partitionBy

## 2.7.0 (April 12, 2019)

* Improvements and new features:
    - Support AWS STS authentication
    - Workflow executions scheduler
    - Run workflow output
    - Workflows identity
    - FileUtils output
    - Row generator input
    - Binary input
    - SFTP security support
    - Workflow DCOS path configurable
    - Billing marathon labels
    - Nginx redirection support without vPath
    - Support query executions on each window in XD streaming input
    - (2.6.1) Upgrade Stratio Spark to 2.4.0 final version
    - (2.6.1) PgBouncer lineage integration
    - (2.6.1) Support postgres functions in pk updates

* Bug fixing
    - Fix continuous queries executions
    - [SPARTA-3067] Allow whitespaces in partitionBy
    - (2.6.1) Fixed duplicates <nameWorkflow,groupId,Version>, templates, upsert environment
    - (2.6.1) Fix hour format of the executions in the detail
    - (2.6.1) Added spark.mesos.driver.failoverTimeout
    - (2.6.1) Fix for SSO URI not being strip properly
    - (2.6.1) Fix bugs and add validations to sftp output/input
    - (2.6.1) Fix GBTClassifier cacheNodeProperty
    - (2.6.1) Fix postgres upsert

## 2.6.0-65e4295 (Built: March 21, 2019 | Released: March 22, 2019)

* Improvements and new features:
    - [SPARTA-2686] Support virtual host and virtual path for Nginx redirections 
    - [SPARTA-2683] Support authentication via headers
    - [SPARTA-2479][SPARTA-2684] Health checks, env and labels as properties for marathon deployment
    - [SPARTA-2555] Added kill deployments to marathon kill action
    - [SPARTA-2714] Improved sparta login: no need for environment variables MARATHON_SSO_CLIENT_ID and MARATHON_SSO_REDIRECT_URI
    - [SPARTA-2687] Avoid expiration of a user cookie who is using the editor
    - [SPARTA-2674] Support string values in cube operators
    - Upgrade Stratio Spark to 2.3.0 version
    - Upgrade Stratio Crossdata to 2.15.3-da9bb1c
    - [SPARTA-1358] Execution Detail View

* Bug fixing
    - [SPARTA-2680] [SPARTA-2681] Better handling of Gosec Zookeeper exception
    - [SPARTA-2661] Solve inconsistent seed ephemeral nodes during deployment
    - Fix bugs related to the Postgres save mode "Copy-in" 
    - Fix the format of user-defined parameters shown in Execution Info 
    - Fix Last update date in creation of a new workflow or a new version 
    - [SPARTA-2700] [SPARTA-2701] Added assign consumer strategy and more configuration properties in kafka input
    - Added Kafka offsets commit after all outputs writes correctly (one transaction)

## 2.5.0-062b5c9 (Built: January 24, 2019 | Released: January 25, 2019)

* New steps:
    - [SPARTA-2527] SFTP input
    - [SPARTA-2463] SFTP output 
 
* Improvements and new features:
    - [SPARTA-2472] [SPARTA-2563] [SPARTA-2580] **Multi-selection** in frontend, **keyboard shortcut** and wizard help
    - [SPARTA-2613][SPARTA-2614] New endpoint for multi-selection in backend
    - [SPARTA-2607] [SPARTA-2626] Orion to Hydra metadata migration
    - [SPARTA-2464] Integration with new Data Governance API
    - [SPARTA-2568] [SPARTA-2587] [SPARTA-2652] Upgrade spark version to 2.2.0-2.2.0-0e24995 and crossdata version to 2.15.1-8d96ee8
    - [SPARTA-2429] Configurable mesos role for each workflow and user-defined discarded table name[SPARTA-2560]
    - [SPARTA-2458] Re-launch a stopped execution
    - [SPARTA-2654] Added integration with Spark History Server
    - Allowing more types inside Cubes
    - [SPARTA-2559][SPARTA-2553] Improved overall Sparta failure-handling
    - Added new validations and fixed validation messages inside a Mlpipeline
 
* Bug fixing:
    - Fixed some bugs wrt Execution list pagination
    - [SPARTA-2291] Handling failures in starting/ending application via Marathon API
       
* AI Pipeline new features:
    - Algorithms:
       Classification, Clustering, Frequent Pattern Mining, Recommendation and Regression
    - Pre-processing steps (for a detailed list please consult the documentation)

## 2.4.1 (December 24, 2018)

+ [SPARTA-2619] Upgrade spark dyplon and xd versions
+ Support all andromeda sparta versions in migration process
+ Fix global variables initialization
+ Order by date in executions view
* [SPARTA-2512] Fix problems generating new version of workflow
* [SPARTA-2542] Fix marathon API requests
* [SPARTA-2559] Fix actors lifecycle
* [SPARTA-2559] Fix dashboard requests delay
* [SPARTA-2536] Added pagination in executions
* [SPARTA-2533] Added slick, java and akka tuning properties


## 2.4.0-926650d (Built: November 01, 2018 | Released: November 02, 2018)

* [SPARTA-2300] Lineage with N-executions model
* Sparta metadata datastore migration from Zookeeper to Postgres
* [SPARTA-2153] Fix Crossdata logs, update formats and integrate Async appender
* [SPARTA-2119] Generic Datasource for Input&Output
* Post workflow execution SQL sentences
* [SPARTA-2204] Integrated SpartaUDFs in the SDK  
* [SPARTA-2219] Refactor Marathon deployment
* [SPARTA-2064] [SPARTA-2068] [SPARTA-2066] [SPARTA-2067] [SPARTA-2070] Refactor environment to apply lists parameters 
* [SPARTA-2252] Data Governance catalog in Crossdata
* [SPARTA-2253] Ignite final integration to cache Sparta metadata
* [SPARTA-2247] Upgrade Crossdata to 2.14.0
* [SPARTA-2287] Upgrade Spark to 2.2.0-2.1.0
* Upgrade Mongo datasource to 0.13.1
* [SPARTA-2157] Sparta cluster: multiple Sparta instances can be deployed
* [SPARTA-2297] Orion migration process


* Dyplon Facade integration
    - [SPARTA-2253] Crossdata plugin compatibility
     
* New transformations:
    - [SPARTA-2171] Coalesce
    - [SPARTA-2122] Rest
 
* New batch inputs:
    - [SPARTA-2215] Cassandra
    - [SPARTA-2123] ElasticSearch
    - [SPARTA-2119] Generic Datasource 
    - [SPARTA-2122] Rest
    - XML

* New outputs:
    - [SPARTA-2209] Cassandra
    - [SPARTA-1789] MongoDB
    - [SPARTA-2119] Generic Datasource 
    - [INTELL-1821] Mlpipeline for building and training SparkMl pipelines
    - [SPARTA-2288] Rest
     
* Bug fixing:
    - [SPARTA-2228] Load latest version of plugins at runtime
    - [SPARTA-2319] Update workflows in cascade with templates updates and deletes 
    - [SPARTA-2246] Fix for groups not being renamed inside workflows
    - [SPARTA-2190] Fix show input fields in select transform
    - [SPARTA-2140] Fix error sinks with wrong schema
    - Fix for env variables and JsoneyString parsing
    - [SPARTA-2347] Fix MLModel transformation debug mode
    - [SPARTA-2245] Set encoding to UTF-8 by default

## 2.3.0-fe00b41 (Built: August 02, 2018 | Pre-release)

* N executions by workflow
* Parametrized executions

## 2.2.0-8b6b349 (Built: July 26, 2018 | Released: July 26, 2018)

* Visual query builder transformation
* Automatic workflows migration process from previous versions
* Bug fixing


## 2.1.0-68094b6 (Built: July 10, 2018 | Released: July 12, 2018)

* Debug mode with schema and data discovery

* Save the history of executions and statuses into Postgres database

* Rights by group and workflow

* Postgres "one transaction" save mode

* New transformations:
    - Join
    - Drop nulls and NaN
    - Drop duplicates
    - Drop columns
    - Rename columns
    - Init nulls with default values
    - Add column with default values
    - Data cleaning (batch)
    - Data profiling (batch)
    - Cube (batch)

* New batch inputs:
    - Avro
    - Csv
    - JDBC
    - Json
    - Parquet
     
* Plugins improvements:
    - Csv transformation:
        - Added flag for discarding header
        - Null control with empty data
    - Select transformation:
        - Select columns with alias configuration mode 
    - Explode transformation:
        - Change the implementation to Spark functions
    - Debugging options added for simulated data in each input

* Bug fixing:
    - Prevent the max size in marathon applications
    
* Other features:
    - Clean empty DC/OS groups triggered by the stopping of a workflow
    - Optimized DAG with less actions in Spark
    - Environment variables usage modified in editors
    - Usability refactor added in repository and monitoring view
    - Added more monitoring fields with execution dates

## 2.0.0 (April 03, 2018)

* New UI with a powerful data pipeline and workflow designer
* Monitoring view
* Repository view
* Workflow life cycle:
    - Environment variables management
    - Moustache substitution on workflow properties with environment variables
* Workflow groups management
* Workflow versioning
* Batch execution mode (project ready)
* New plugins implementation: 
    - Avro
    - Casting
    - Checkpoint
    - Distinct
    - Intersection
    - Json
    - OrderBy
    - Persist
    - Select
    - Union
    - Window
* Mayor refactor in existent plugins:
    - Crossdata input
    - Cube
    - Datetime
    - Filter
    - JDBC/Postgres
    - Trigger
* Centralized logging integration
* Errors management at workflow, row or field level
* Reprocessing process with error outputs
* In-memory API
* Plugins validations
* Security settings refactor
* Custom plugins management integrated with HDFS
* Update Spark Image (2.2.0.5)
* Avro/Json serializers and deserializers for kafka input and output
* Cube optimization with mapWithState
* Spark Streaming jobs optimized with less stages
* Nginx integration for monitoring workflows with the Spark UI
* Error catalog for API calls
* Dto models for frontend requeriments
* Fault torelance for server with Akka persistence
* Lineage integration with Data-Governance
* Bugfixing over all reported errors on the 1.x Sparta version


## 1.9.0 (December 29, 2017)
* **Alpha release for version 2.0**
* **Complete refactor of the inner mechanics of the application** and consequential migration of the components
* Workflow Validation
* [WIP]: Batch support, Environment variables
* Update Spark Image (2.1.0.6)
* Update sparta-dyplon (0.13.0)
* Bugfixes: Datetime issues, "null" as string value handling, Zookeeper offsets, case sensitive issues 

## 1.7.5 (September 20, 2017)

* Sending events with a key to a partitioned topic via Kafka Producer (Output)
* Csv parser delimiter options added
* Kerberos configurations added to spark submit 
* Fix kafka and avoid evaluation when writer options is empty
* Added onFailure and onSuccess actions to be executed after an Output stage
* Update Stratio Licence

## 1.7.4 (September 06, 2017)

* Added TLS option to spark settings in workflow
* Change TCP to HTTP heartbeat in Marathon applications
* Bugfix: Kafka output running with SSL security

## 1.7.3 (August 18, 2017)

* Marathon SSO secrets
* Bugfix: deployment with vault token
* Bugfix: Spark UI enabled by default with the port 4040 into Crossdata Service

## 1.7.2 (August 07, 2017)

* Crossdata service options running over Mesos
* Crossdata service integrated with Postgres securized with TLS
* Crossdata table creation integrated with HDFS securized
* Kafka offsets management in Kafka intput
* Checkpointing configurable
* Bugfixing: Application resources
* Bugfixing: Mesos security in Spark jobs
* Bugfixing: Spark user obtained from principal in Spark jobs

## 1.7.1 (July 25, 2017)

* Crossdata service integrated with HDFS securized
* Crossdata service securized in Dyplon plugin
* Updated log messages
* Bugfix: Swagger UI

## 1.7.0 (July 21, 2017)

* Update Spark Image
* Dyplon upgrade 0.10.3

## 1.6.3 (July 13, 2017)

* Fix missing kms-utils file

## 1.6.2 (July 12, 2017)

* Crossdata intput
* Crossdata output
* Network segmentation integration
* Dynamic Authentication integration
* Mesos securized integration

## 1.6.1 (June 28, 2017)

* Crossdata integration in streaming jobs
* Crossdata API and UI
* Multi-tenant support 
* Refactor configurations, available in workflow level

## 1.6.0 (June 13, 2017)

* **Authorization system with Stratio GoSec Dyplon**
* Kafka output securized
* Logout option
* _New Transformation_: **Explode**. Explode Array and Map fields into multiple rows.
* _New Transformation_: **Split**. Split a field into multiple fields either _by index_ or _by char_ or _by regex_
* Added support for MapType and ArrayType
* Added Datetime conversion via user-defined format
* Contact information in UI
* About information in UI


## 1.5.0 (May 31, 2017)

* Backup metadata
* Restore metadata
* Clean metadata
* Timeout configurable
* Info service
* Security options configurable
* Https with marathon-lb
* Frontend performance improvements
* Bugfix: Postgres library added

## 1.4.0 (May 17, 2017)

* Update to Spark 2.1
* Integration with HDFS securized
* Integration with Kafka securized
* Integration with Zookeeper securized
* Save Kafka offsets in Kafka with Spark 2.1 API
* Save transformations with writer
* Save Raw Data step
* Execution information with Spark properties
* Updated SSO with GoSec
* Added Postgres options

## 1.3.0 (April 07, 2017)

* Sparta dispatcher added with Marathon API
* Sparta driver added in Marathon execution mode 
* Diver and Plugins jars endpoints
* Executions properties and unified spark submit arguments and configurations
* Policy view with important policy data
* Settings step in wizard
* Support user, groups and permissions with Oauth2 library
* Delete checkpoint option
* Sparta config refactor
* Docker files refactor
* Bugfix: fragments duplication

## 1.2.0 (March 24, 2017)

* Optimized save modes (upsert, append and override) in Postgres and JDBC output
* Upsert in Postgres and JDBC output
* Postgres Output
* Allow null values in Json parser
* Partition by with multiple fields
* Output table name defined by user
* Added more save options in cubes and triggers
* Sparta executors docker image
* Run with docker containers in cluster and client mode
* Removed Spark 1.5 profile
* Unified spark options sent in spark submit arguments
* Bugfix: Hdfs token renewal
* Bugfix: Policy options correctly saved
* Bugfix: Cube and trigger options correctly saved

## 1.1.0 (February 10, 2017)

* Download secrets from Vault
* Weekly added to DateTime aggregation and expiring data
* Docker entry-point with fields validation
* Custom transformation example
* Swagger in the same port than API
* Optional KeyTab renovation
* Bugfix: Operator and Dimension typeOp inside configuration in GUI

## 1.0.0 (February 06, 2017)

* Policy statuses with HA support
* Upload and download plugins and driver jars with the Sparta API
* Split support by the transformations SDK
* Spark submit options can be added at policy definition level
* Spark context options can be added at policy definition level 
* JSON, CSV and XML transformations added
* Filter transformation
* FileSystem Input added
* Avro, FileSystem and HTTP Outputs added (HDFS, s3 ...)
* HDFS-Kerberos integration
* Generic properties added in outputs and inputs
* Custom transformations, inputs and outputs to see and configure user plugins in the UI
* Status information and errors is showed in the policies UI
* Discard or send null values in transformations
* Spark UDF integration for support auto-calculated fields
* All the Spark save modes added: Append, ErrorIfExists, Overwrite and Ignore
* All the Sparta API endpoints work asynchronously
* Trigger and Cubes at the same level
* Output types support in dimensions and operators
* Filters support generic types
* Granularities supported in datetime transformation
* Scala 2.11 update
* SDK refactor: more simple and more extensible
* Kafka 0.10.0.0 update with Stratio-Kafka repository
* RabbitMq 0.4.0 update with RabbitMq Distributed
* Bugfix: Stop correctly in Mesos cluster gracefully
* Bugfix: Stop correctly policies in local mode
* Bugfix: Geo and DateTime transformation errors corrected
* Bugfix: Removed metadata creation in outputs
* Bugfix: Checkpoint path with dateTime
* Bugfix: Tooltips corrected in UI
* Bugfix: Spark 1.5 compatibility
* Bugfix: Policy Statuses are assigned correctly
* Bugfix: Remove inputField in transformations
* Bugfix: Operators support generic types

## 0.11.0 (September 2016)

* Docker updated with Spark and DC/OS Mesos integration
* Updated Receiver RabbitMQ for Distributed mode
* Batch + Streaming Queries
* Input Sql Sentences
* Slidding Windows in Triggers
* User plugins jars added in policy definition
* Spark configurations added in policy definition
* Bugfix: Kafka Direct
* Bugfix: Ingestion parser
* Bugfix: Date options in dimension time
* Bugfix: Relaunch policies
* Bugfix: Auto fragments creation
* Bugfix: Delete checkpoint when edit one policy
* Bugfix: Api end points return the correct policy with fragments
* Bugfix: Swagger data types

## 0.10.0 (July 2016)

* Added coveralls badge
* Updated to Spark 1.6.2
* Updated Cassandra, ElasticSearch and MongoDb to the latest library version
* Updated Receiver RabbitMQ
* Zookeeper offset path updated in kafka receivers
* Ingestion Parser with Avro Serialization
* Bugfix: Akka dependency
* Bugfix: Actors creation process with not unique identifier
* Bugfix: Policy statuses corrected
* Bugfix: Updated all policies when one fragment is changed
* Bugfix: Policy download in Firefox

## 0.9.5 (April 2016)

* Added remember field in order to run queries that last more than the streaming batch
* Add Timestamp type to selectable output type in DateTime parser
* Bugfix: Removed fragments when downloading the policy
* Bugfix: Geo icon not showing
* Bugfix: Set default checkpoint path as relative
* Bugfix: Fixed measures field wrong behaviour

## 0.9.4 (April 2016)

* Bugfix: Solved problem with hdfs.

## 0.9.3 (April 2016)

* Bugfix: Solved problem with permissions and owners.

## 0.9.2 (April 2016)

* Bugfix: Fixed examples

## 0.9.1 (April 2016)

* Bugfix: Fixed documentation links

## 0.9.0 (April 2016)

* New look & feel and more user friendly user interface
* New policy component: Triggers
* New parser for geolocation
* Renamed Sparkta to Sparta
* Improved performance
* No-time aggregations
* User benchmark
* Kafka output
* JDBC output
* Policy refactor
* Fault tolerance
* Refactor Cluster deployment
* Addition of a new operator: Associative Mean
* Fixes for Count and Sum operators 
* Added flexibility to create policies with no cubes 
* Integration with a SSO 
* Sparta API supports SSL

## 0.8.0 (December 2015)

* Solved issue related to PID folder creation.
* New parser Stratio Ingestion
* Upgrade to Spark 1.5.2
* Possibility to package two Spark versions: 1.4.1 and 1.5.2
* MongoDb output updated to version 0.10.1
* New Solr output
* Migration of doc to confluence: https://stratio.atlassian.net/wiki/display/SPARTA0x8/
* Solved issue when a policy is stopped in the cluster
* Improve performance in associative operators
* Automatic deploy of policies in clusters: Spark Standalone, Mesos and YARN
* Improve Cassandra Output. Possibility to introduce Spark properties
* Solved issue related to stopping Sparta service
* bug corrected in aggregations, Array Index out of bounds

## 0.7.0 (November 2015)

* Bugfix: Front Minors
* Bugfix: Back Minors
* Bugfixes Cassandra
* Bugfixes Elasticsearch
* Package installation permissions fixes
* Bugfix: DateTime field and parser

## 0.6.2 (September 2015)

* Bugfix: DateTime now parses string formats
* Bugfix: Permission issues running Sparta service
* Bugfix: Minor errors in user interface

## 0.6.1 (September 2015)

* Hotfix: User interface was not showing up

## 0.6.0 (September 2015)

* Upgrade Spark version to 1.4.1
* User Interface to help create your cubes
* Automatic deployment in Mesos cluster
* Policy status lifecycle
* New WebSocket input
* Elasticsearch output updated to version 1.7
* New EntityCount and TotalEntityCount operators
* Filters in operators

## 0.5.0 (July 2015)

* Added compatibility with Apache Spark 1.4.x
* Released a specific distribution for Apache Spark 1.3.x
* Support for fragment composition in policies.
* Policy refactor. Improved semantic of the JSON.
* Parquet and CSV outputs.
* Improved Twitter input. Possibility to filter tweets by hashtag.
* Fixed important bug in Elasticsearch output. Fields are mapped according to their type.

## 0.4.0 (May 2015)

* SandBox Stratio Sparta
* Integration with **Spark 1.3.1**
* Auto create time series in outputs if not exist time dimension
* Full integration in Outputs with **Spark DataFrames**
* Auto create "Id" in DataFrames with dimensions
* Save Raw data in **Parquet** files
* Auto detection schema with policies
* Reflexions for native plugins
    * Interactive documentation
    * Client SDK generation and discoverability
    * Compatibility with multiple oputputs
* Output **Cassandra**:
    * Auto tables creation
    * Auto primary key and column families detection
    * Auto index creation
* Output **ElasticSearch**:
    * Options in mapping date types
    * Auto index type with time fields
* Output **Redis**
* Input **Kafka Direct Streaming** integrated in Spark 1.3.0
* Input **RabbitMq**
* Full-Text operator
* Accumulator operator
* Last Value operator
