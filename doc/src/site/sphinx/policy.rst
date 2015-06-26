Policies
########

A policy is the way we tell SpaRkTA how to aggregate data. It is in JSON format and you can check some
|examples_link| in the official repo.

.. |examples_link| raw:: html

   <a href="https://github.com/Stratio/sparkta/tree/master/examples/policies"
   target="_blank">examples</a>

It consists of the following parts:


General configuration
*********************

In this part you must define the global parameters of your policy::

  "name": "Twitter-Cassandra-policy",
  "sparkStreamingWindow": 6000,
  "checkpointing": {
    "path": "myCheckpointPath",
    "timeDimension": "minute",
    "granularity": "minute",
    "interval": 30000,
    "timeAvailability": 60000
  },
  "rawData": {
    "enabled": "true",
    "partitionFormat": "day",
    "path": "myParquetPath"
  }


+--------------------------+-----------------------------------------------+----------+
| Property                 | Description                                   | Optional |
+==========================+===============================================+==========+
| name                     | Policy name to identify it                    | No       |
+--------------------------+-----------------------------------------------+----------+
| sparkStreamingWindow     | Apache Spark Streaming window duration        | No       |
+--------------------------+-----------------------------------------------+----------+


The `checkpointing <stateful.html>`__ block is where you must define the Apache Spark Streaming |streaming_link|

The `rawData` block allow you to save the `raw data <rawdata.html>`__ into HDFS + Parquet.

.. |streaming_link| raw:: html

   <a href="https://spark.apache.org/docs/latest/streaming-programming-guide.html#checkpointing"
   target="_blank">configuration parameters</a>

.. _input:

Input
*****

Here you define the source of your data. Currently, you can have only one input. For more info
about supported inputs, you can visit :doc:`inputs`

Example::

    "inputs": [
      {
        "name": "in-twitter",
        "elementType": "TwitterInput",
        "configuration": {
          "consumerKey": "*****",
          "consumerSecret": "*****",
          "accessToken": "*****",
          "accessTokenSecret": "*****"
        }
      }
    ]


.. _dimension:


Transformation(s)
*****************

Once the data passes through the input to SpaRkTA you usually need to treat this raw data in order to model your fields.

You can learn more about transformations `here <transformations.html>`__

Example::

  "transformations": [
      {
        "name": "morphline-parser",
        "order": 0,
        "type": "Morphlines",
        "outputFields": [
          "userName",
          "tweet",
          "responseTime"
        ],
        "configuration": {
          "morphline": {
            "id": "morphline1",
            "importCommands": [
              "org.kitesdk.**"
            ],
            "commands": [
              {
                "readJson": {}
              },
              {
                "extractJsonPaths": {
                  "paths": {
                    "userName": "/user/name",
                    "tweet": "/user/tweet",
                    "responseTime": "/responseTime"
                  }
                }
              },
              {
                "removeFields": {
                  "blacklist": [
                    "literal:_attachment_body",
                    "literal:message"
                  ]
                }
              }
            ]
          }
        }
      },
      {
        "name": "responseTime-parser",
        "order": 1,
        "inputField": "responseTime",
        "outputFields": [
          "system-timestamp"
        ],
        "type": "DateTime",
        "configuration": {
          "responseTime": "unixMillis"
        }
      }
    ]

.. _cube:


Cube(s)
*******

The cubes are the way you want to aggregate your fields generated in the previous step.

Learn `here <cube.html>`__ more about cubes.

Example::

    "cubes": [
      {
        "name": "tweets-per-user-per-minute",
        "dimensions": [
          {
            "name": "userName",
            "field": "userName",
            "type": "Default"
          },
          {
            "name": "tweet",
            "field": "tweet",
            "type": "Default"
          },
          {
            "name": "responseTime",
            "field": "responseTime",
            "type": "DateTime",
            "precision": "minute"
          }
        ],
        "operators": [
          {
            "name": "count-operator",
            "type": "Count",
            "configuration": {}
          }
        ]
      }
    ]


.. _output:


Output(s)
*********

Here is where you decide where to persist your aggregated data. One output is equivalent to one datastore. You can
have one or more outputs in your policy.

Learn `here <outputs.html>`__ more about cubes.

Example::

    "outputs": [
      {
        "name": "out-mongo",
        "elementType": "MongoDb",
        "configuration": {
          "clientUri": "mongodb://localhost:27017",
          "dbName": "sparkta"
        }
      }
    ]

.. _fragment:

Fragment(s)
===========

For your convenience, it is possible to have an alias about input[s]/output[s] in your policy. These alias are
fragments that will be included in your policy when the policy has been run.

Fragments have an API Rest to perform CRUD operations over them. For more information you can read documentation about
it querying Swagger::

    http://<SPARKTA-HOST>:<SPARKTA-PORT>/swagger#!/fragment

You have more configuration info `here <fragments.html>`__