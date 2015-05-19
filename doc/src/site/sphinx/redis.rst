Redis Output Specification
******************

- :ref:`introduction-label`

- :ref:`driver-label`

- :ref:`worker-label`


.. _introduction-label:

Introduction
============

This output uses Redis DebasisHG's client that you can found here: https://github.com/debasishg/scala-redis

It is important to sign that dataframes are not supported for Redis.

This plugin creates one client connection per Worker in a Spark Cluster.

Is necessary need override two functions from the Output SDK:
::
  override def doPersist(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit
  override def upsert(metricOperations: Iterator[(DimensionValuesTime, Map[String, Option[Any]])]): Unit



.. _driver-label:

Driver Operations
============

Unlike other outputs, Redis' output do not need to create any kind of index to work.

.. _worker-label:

Worker Operations
============

As this Output does not use functionality of DataFrames, override the method Upsert, that save all values
of a **Tuple -> (DimensionValuesTime, Aggregations)**.
Below you can see each of the features implemented:

  * For each rollup it saves the following information:

    - As key: the rollup identifiers that is built with the name of the bucket separeted with "_"
    - Furthermore for each bucket we will have the name of the bucket and its value (separated with ":" too).
    - With this key structure we could perform searches over the keys using pattern to search keys.
    - Example:
      ::

        hastags_urls_minute_wordsN:hastags:0:urls:0:minute:Tue Apr 28 15:45:00 CEST 2015:wordsN:13
        precision3_minute_wordsN:precision3:0.703125/0.703125:minute:Tue Apr 28 15:45:00 CEST 2015:wordsN:5
        precision3_hastags_urls_minute:precision3:0.703125/0.703125:hastags:1:urls:0:minute:Tue Apr 28 15:40:00 CEST 2015
        precision3_hastags_retweets_minute_wordsN:precision3:0.703125/0.703125:hastags:1:retweets:0:minute:Tue Apr 28 15:45:00 CEST 2015:wordsN:19
        precision3_hastags_retweets_minute:precision3:0.703125/0.703125:hastags:3:retweets:0:minute:Tue Apr 28 15:40:00 CEST 2015
        precision3_hastags_retweets_urls_minute_wordsN:precision3:0.703125/0.703125:hastags:0:retweets:0:urls:1:minute:Tue Apr 28 15:40:00 CEST 2015:wordsN:18
        precision3_retweets_urls_wordsN:precision3:0.703125/0.703125:retweets:0:urls:1:wordsN:12
        precision3_hastags_retweets_wordsN:precision3:0.703125/0.703125:hastags:0:retweets:0:wordsN:22
        precision3_hastags_retweets_urls_minute_wordsN:precision3:0.703125/0.703125:hastags:0:retweets:0:urls:1:minute:Tue Apr 28 15:45:00 CEST 2015:wordsN:4
        precision3_hastags_retweets_urls_minute:precision3:0.703125/0.703125:hastags:0:retweets:0:urls:0:minute:Tue Apr 28 15:40:00 CEST 2015
        hastags_retweets_urls_minute:hastags:1:retweets:0:urls:0:minute:Tue Apr 28 15:40:00 CEST 2015"
        precision3_hastags_retweets_minute_wordsN:precision3:0.703125/0.703125:hastags:0:retweets:0:minute:Tue Apr 28 15:45:00 CEST 2015:wordsN:13


  * The output contains information about aggregations:

      - Example:
      ::

        1) "count"
        2) "2"
        3) "max_wordsN"
        4) "22.0"
        5) "min_wordsN"
        6) "22.0"