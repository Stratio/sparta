About Stratio Sparkta
******************

Since Aryabhatta invented zero, Mathematicians such as John von Neuman have been in pursuit
of efficient counting and architects have constantly built systems that computes counts quicker. In
this age of social media, where 100s of 1000s events take place every second, we were inspired
by twitter's Rainbird project to develop a distributed aggregation engine with this high level
features:

- Pure Spark
- No need of coding, only declarative aggregation workflows
- Data continuously streamed in & processed in near real-time
- Ready to use, plug&play
- Flexible workflows (input, output, parsers, etc...)
- High performance
- Scalable and fault tolerant
- BAM ready
- Visualization



Introduction
============




Features
========

- Highly business-project oriented
- Community engagement from zero day
- Separate storage and query layers
- Multiple application
- Aggregation:
    - Several operators (count, inc, min, max, avg, median, variance, stddev, full-text, count distinct...)
    - Automatic rollups (secondly, minutely, hourly, daily, monthly)
    - GeoLocation
    - Hierarchical counting
    - Flexible definition of aggregation policies (json, web app)
- Querying
    - Automatic REST api
    - In memory data cube
    - Extend sparkSQL to allow MDX queries


Architecture
============


Sparkta overview
------------
.. figure:: images/sparkta1.png
   :alt: Spark Architecture Overview


Architectures used
------------

- `Spark Streaming & Spark <http://spark.apache.org>`__
- `SparkSQL <https://spark.apache.org/sql>`__
- `Akka <http://akka.io>`__
- `MongoDB <http://www.mongodb.org/>`__
- `Apache Cassandra <http://cassandra.apache.org>`__
- `ElasticSearch <https://www.elastic.co>`__
- `Redis <http://redis.io>`__
- `Apache Parquet <http://parquet.apache.org/>`__
- `HDFS <http://hadoop.apache.org/docs/r1.2.1/hdfs_design.html>`__
- `Apache Kafka <http://kafka.apache.org>`__
- `Apache Flume <https://flume.apache.org/>`__
- `RabbitMQ <https://www.rabbitmq.com/>`__
- `Spray <http://spray.io/>`__
- `KiteSDK (morphlines) <http://kitesdk.org/docs/current>`__


Inputs
------------

- `Twitter <inputs.html#twitter-label>`__
- `Kafka <inputs.html#kafka-label>`__
- `Flume <inputs.html#flume-label>`__
- `RabbitMQ <inputs.html#rabbitMQ-label>`__
- `Socket <inputs.html#socket-label>`__

.. image:: images/Inputs.png
   :height: 500 px
   :width: 500 px
   :scale: 50 %
   :alt: Spark Inputs Overview


Outputs
------------

- MongoDB
- Cassandra
- ElasticSearch
- Redis
- Spark's DataFrames Outputs
- PrintOut

.. image:: images/Outputs.png
   :height: 500 px
   :width: 500 px
   :scale: 50 %
   :alt: Spark Outputs Overview

