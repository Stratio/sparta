Using Sparkta
******************

Running Sparkta
============

Once Sparkta has been installed, you can run ``sh $SPARKTA_HOME/bin/run``.
Default installation directory is ``/opt/sds/sparkta``

Aggregation Policy
============

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
        "dateBucket": "minute",
        "granularity": "minute",
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
work with geospatial data. For more info you can visit :doc:`dimensions`.

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

The rollups are the ways you want to aggregate the info. For more info you can visit :doc:`rollups`.

Example:
::
    "rollups": [
    {
      "dimensionAndBucketTypes": [
        {
          "dimensionName": "status",
          "bucketType": "hastags"
        }
      }
    ]

.. _transformation:


Transformation(s)
-----------------

Here you can specify the functions which you want to apply before aggregation.  For more info you can visit
:doc:`transformations`.

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



Submiting Policy
============

The policy must be submitted via POST with the following syntax:
::

    curl -X POST -H "Content-Type: application/json" --data @PATH-TO-FILE/sample-policy.json SPARKTA-URL:PORT/policies
