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
- Scalable
- Business Activity Monitoring
- Visualization



Introduction
============
Social media and networking sites are  part of the fabric of everyday life, changing the way the world shares and
accesses information.
The overwhelming amount of information gathered not only from messages, updates and images but also readings
from sensors, GPS signals and many other sources was the origin of a (big) technological revolution.

This vast amount of data allows us to learn from the users and explore our own world.

We can follow in real-time the evolution of a topic, an event or even an incident just by exploring aggregated data.



.. figure:: images/map.png



 But beyond cool visualizations, there are some core services delivered in real-time, using aggregated data to
 answer common questions in the fastest way.

 These services are the heart of the business behind their nice logos.

 Site traffic, user engagement monitoring, service health, APIs, internal monitoring platforms, real-time dashboards…

 Aggregated data feeds directly to end users, publishers, and advertisers, among others.

 In Sparkta we want to start delivering real-time services. Real-time monitoring could be really nice, but your
 company needs to work in the same way as digital companies:

 Rethinking existing processes to deliver them faster, better.
 Creating new opportunities for competitive advantages.

Features
========

- Highly business-project oriented
- Multiple application
- Cubes
    - Default
    - Time-based
    - Secondly, minutely, hourly,  daily, monthly, yearly...
    - Hierarchical
    - GeoRange: Areas with different sizes (rectangles)
    - Flexible definition of aggregation policies (json, web app)
- Operators:
    - Max, min, count, sum
    - Average, median
    - Stdev, variance, count distinct
    - Last value
    - Full-text search



Architecture
============


Sparkta overview
------------
.. figure:: images/sparkta1.png
   :alt: Spark Architecture Overview


Key technologies
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


.. figure:: images/Inoutputs.png


Inputs
------------

- `Twitter <inputs.html#twitter-label>`__
- `Kafka <inputs.html#kafka-label>`__
- `Flume <inputs.html#flume-label>`__
- `RabbitMQ <inputs.html#rabbitMQ-label>`__
- `Socket <inputs.html#socket-label>`__


Outputs
------------

- :doc:`mongodb`
- :doc:`cassandra`
- :doc:`elasticsearch`
- :doc:`redis`
- :doc:`print`



