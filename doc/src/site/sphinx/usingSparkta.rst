Using SpaRkTA
*************

Installing SpaRkTA
==================

You can install SpaRkTA by unpackaging a `release <https://github.com/Stratio/sparkta/releases>`__ or by
generating the deb or rpm packages from the `source code <https://github.com/Stratio/sparkta>`__.

You can generate rpm and deb packages by running::

    mvn clean package -Ppackage

**Note:** you need to have installed the following programs in order to build these packages:

 * In a debian distribution:

  - fakeroot
  - dpkg-dev
  - rpm

 * In a centOS distribution:

  - fakeroot
  - dpkg-dev
  - rpmdevtools

Running SpaRkTA
===============

Once SpaRkTA has been installed, you can run ``sh $SPARKTA_HOME/bin/run``.
Default installation directory is ``/opt/sds/sparkta``

 * Starting the Stratio SpaRkTA Shell::

    cd /opt/sds/sparkta

    sh bin/run

Aggregation Policy
==================

An aggregation policy it's a JSON document. It's composed of:

* :ref:`Input <input>`: Where is the data coming from?
* :ref:`Output(s) <output>`: aggregate data should be stored?
* :ref:`Dimension(s) <dimension>`: Which fields will you need for your real-time needs?
* :ref:`RollUp(s) <rollup>`: How do you want to aggregate the dimensions?
* :ref:`Transformation(s) <transformation>`: Which functions should be applied before aggregation?
* :ref:`Save raw data <save-raw>`: Do you want to save raw events?

The policy have a few required fields like *name* and *duration* and others optional, like *saveRawData* and *rawDataParquetPath*


.. _input:

Input
-----

Here you define the source of your data. Currently, you can have only one input. For more info
about supported inputs, you can visit :doc:`inputs`

Example:
::
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

.. _output:


Output(s)
---------

You can have one or more outputs in your policy. One output is equivalent to one datastore.
For more configuration info you can visit :doc:`outputs`.

Example:
::
    "outputs": [
    {
      "name": "out-mongo",
      "elementType": "MongoDbOutput",
      "configuration": {
        "clientUri": "mongodb://localhost:27017",
        "dbName": "sparkta",
        "multiplexer": "true",
        "textIndexFields": "userLocation",
        "fieldsSeparator": ",",
        "language": "english"
      }
    }
  ]


You can read more specifications for the native outputs plugins here:
  - :doc:`mongodb`
  - :doc:`redis`

.. _dimension:


Dimension(s)
------------

Dimensions are the fields that you want to observe. You can, for example,
work with geospatial data.

Example:
::
    "dimensions": [
        {
          "dimensionType": "TwitterStatusBucketer",
          "name": "status"
        },
        {
          "dimensionType": "GeoHashBucketer",
          "name": "geolocation"
        }
      ]

.. _rollup:


RollUp(s)
---------

The rollups are the ways you want to aggregate the info.

Example:
::
    "rollups": [
      {
        "dimensionAndBucketTypes": [
          {
            "dimensionName": "status",
            "bucketType": "hastags"
          }
        ],
        "operators": ["count-operator","avg-operator"]
      }
    ]

.. _transformation:


Transformation(s)
-----------------

Here you can specify the functions which you want to apply before aggregation.

Example:
::
    "parsers": [
    {
      "name": "morphline-parser",
      "elementType": "MorphlinesParser",
      "configuration": {
        "morphline": {
          "id": "morphline1",
          "importCommands": [
            "org.kitesdk.**",
            "com.stratio.ingestion.morphline.**"
          ],
          "commands": [
            {
              "readJson": {}
            },
            {
              "extractJsonPaths": {
                "paths": {
                  "appName": "/appName",
                  "method": "/method",
                  "datetime": "/date",
                  "appCountry": "/appCountry",
                  "appPlatform": "/appPlatform",
                  "appVersion": "/appVersion",
                  "uid": "/uid",
                  "device": "/device",
                  "latitude": "/latitude",
                  "longitude": "/longitude",
                  "osVersion": "/osVersion",
                  "lang": "/lang",
                  "appLang": "/appLang",
                  "user_id": "/user_id",
                  "connection": "/connection",
                  "timestamp": "/timestamp",
                  "session": "/session",
                  "extra1": "/extra1",
                  "extra2": "/extra2",
                  "extra3": "/extra3",
                  "source": "/source",
                  "environment": "/environment",
                  "platform": "/platform",
                  "responseTime": "/responseTime"
                }
              }
            },
            {
              "addValues": {
                "geo": "@{latitude}__@{longitude}"
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
    }
  ]

.. _save-raw:


Save raw data
-------------

You can save the raw data to HDFS+Parquet with only two parameters:
::

    "saveRawData": "false",
    "rawDataParquetPath": "myTestParquetPath"


Stateful Operations
-------------------

The system runs with time windows, these windows are configurable and allow us to not associative operations:
::

  "checkpointDir": "checkpoint",
  "timeBucket": "minute",
  "checkpointGranularity": "minute",
  "checkpointInterval": 30000,
  "checkpointTimeAvailability": 60000,


* checkpointDir:
  This is the directory to save temporal data, this must be a distributed file system as HDFS, S3 ...
  Is possible omit this parameter in policy.

  * Example:
::

   "checkpointDir": ("directory")  Default: "checkpoint"

* timeBucket:
   You can specify the time bucket containing the event, thanks to this parameter can be stored aggregate data and
   generate timeseries.
   This name will be as identified in the system of persistence.
   Is possible omit this parameter in policy.

   * Example:
::

   "timeBucket": ("BUCKET_LABEL")  Default: "minute"

* checkpointGranularity:
   If not created any bucketer time to identify with "timeBucket" you can leave the system assigned to each event time
   with the specified granularity.
   Is possible omit this parameter in policy.

   * Example:
::

   "checkpointGranularity": ("second"/"minute"/"hour"/"day"/"month"/"year")  Default: "minute"

* checkpointInterval:
  Note that checkpointing of RDDs incurs the cost of saving to reliable storage. This may cause an increase in the
  processing time of those batches where RDDs get checkpointed. Hence, the interval of checkpointing needs to be set
  carefully. At small batch sizes (say 1 second), checkpointing every batch may significantly reduce operation throughput.
  Typically, a checkpoint interval of 5 - 10 times of sliding interval.
  Is possible omit this parameter in policy.

  * Example:
::

   "checkpointInterval": (TIME_IN_MILLISECONDS)  Default: 20000

* checkpointTimeAvailability:
  It is a window of time that allows us to have data stored in the temporary system for a period of additional
  granularity, thus time we can receive events that include a pre-current time. With this parameter you can define a
  maximum time in which we expect to receive these events to add.

  * Example:
::

   "checkpointTimeAvailability": (TIME_IN_MILLISECONDS)  Default: 60000


Submitting Policy
=================

The policy must be submitted via POST with the following syntax:
::

    curl -X POST -H "Content-Type: application/json" --data @PATH-TO-FILE/sample-policy.json SPARKTA-URL:PORT/policies
