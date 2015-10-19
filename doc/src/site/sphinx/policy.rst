Policies
########

A policy is the way we tell Sparkta how to aggregate data. It is in JSON format and you can check some
|examples_link| in the official repository.

.. |examples_link| raw:: html

   <a href="https://github.com/Stratio/sparkta/tree/master/examples/policies"
   target="_blank">examples</a>

It consists of the following parts:


General configuration
*********************

In this part you have to define the global parameters of your policy::

 {
  "name": "policy-TwitterJson-Cassandra",
  "description": "policy-TwitterJson-Cassandra",
  "sparkStreamingWindow": 6000,
  "checkpointPath": "checkpoint",
  "rawData": {
    "enabled": "true",
    "partitionFormat": "day",
    "path": "myTestParquetPath"
  }
+--------------------------+-----------------------------------------------+----------+
| Property                 | Description                                   | Optional |
+==========================+===============================================+==========+
| name                     | Policy name to identify it                    | No       |
+--------------------------+-----------------------------------------------+----------+
| sparkStreamingWindow     | Apache Spark Streaming window duration        | No       |
+--------------------------+-----------------------------------------------+----------+
| checkpointPath           | The path to save the checkpoint               | No       |
+--------------------------+-----------------------------------------------+----------+
| rawData                  | To specify if you want to save the raw data   | No       |
+--------------------------+-----------------------------------------------+----------+

The `rawData` block allow you to save the `raw data <rawdata.html>`__ into HDFS + Parquet.

.. _input:

Inputs
******

Here you can define the source of your data. Currently, you can have just one input. For more information
about supported inputs, you can visit :doc:`inputs`

Example::

 "input": {
    "name": "in-twitter-json",
    "type": "TwitterJson",
    "configuration": {
      "consumerKey": "*********",
      "consumerSecret": "*********",
      "accessToken": "*********",
      "accessTokenSecret": "*********"
    }
  }

Transformations
***************

Once the data passes through the input to Sparkta you usually need to treat this raw data in order to model your fields.

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


Cubes
*****

The cubes are the way you want to aggregate your fields generated in the previous step.

Learn more about cubes `here <cube.html>`__ .

Example::

    "cubes": [
      {
        "name": "tweets-per-user-per-minute",
        "checkpointConfig": {
          "timeDimension": "minute",
          "granularity": "minute",
          "interval": 30000,
          "timeAvailability": 60000
        },
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


Outputs
*******

Here is where you decide where to persist your aggregated data. An output is equivalent to a datastore. You can
have one or more outputs in your policy.

Note: it is important to mark that the result of the cube is saved in a datastore table. The name of this table is
built concatenating the dimension names of the cube.

In the previous example the name of the table would be userName_tweet_responseTime. Be careful with not allowed
characters or size of the names. For example Cassandra do not allow tables with more of 48 characters or capital
letters in its name (then userName_tweet_responseTime is incorrect).

Learn more about outputs `here <outputs.html>`__ .

Example::

    "outputs": [
      {
        "name": "out-mongo",
        "elementType": "MongoDb",
        "configuration": {
          "hosts": [{"host": "localhost" , "port": "27017" }],
          "dbName": "sparkta"
        }
      }
    ]


