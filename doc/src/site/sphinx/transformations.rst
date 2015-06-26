Transformation(s)
****************
sdfsdfsdfsdfsdf

DateTime
========

+-----------------+-------------------------------------------------------------------------+--------------------------+
| Property        | Description                                                             | Optional                 |
+=================+=========================================================================+==========================+
| path            | This is the path where the temporal data is going to be saved, this path| Yes (default: checkpoint)|
|                 | should point to a distributed file system as HDFS, S3,...               |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+
| timeDimension   | This is the directory to save temporal data, this must be a distributed | Yes (default: xxx)       |
|                 | file system as HDFS, S3,...                                             |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+
| granularity     | This is the directory to save temporal data, this must be a distributed | Yes (default: minute)    |
|                 | file system as HDFS, S3 ...                                             |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+
| interval        | This is the directory to save temporal data, this must be a distributed | Yes (default: 20000)     |
|                 | file system as HDFS, S3 ...                                             |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+
| timeAvailability| This is the directory to save temporal data, this must be a distributed | Yes (default: 60000)     |
|                 | file system as HDFS, S3 ...                                             |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+

Type
====

    {
      "name": "type-parser",
      "type": "Type",
      "configuration": {
        "sourceField": "price",
        "type": "Long",
        "newField": "price_float"
      }
    }

+-----------------+-------------------------------------------------------------------------+--------------------------+
| Property        | Description                                                             | Optional                 |
+=================+=========================================================================+==========================+
| name     | This is the path where the temporal data is going to be saved, this path| No|
|                 | should point to a distributed file system as HDFS, S3,...               |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+
| type            | This is the directory to save temporal data, this must be a distributed | No      |
|                 | file system as HDFS, S3,...                                             |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+
| sourceField     | This is the path where the temporal data is going to be saved, this path| No|
|                 | should point to a distributed file system as HDFS, S3,...               |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+
| type            | This is the directory to save temporal data, this must be a distributed | No      |
|                 | file system as HDFS, S3,...                                             |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+
| newField        | This is the directory to save temporal data, this must be a distributed | No    |
|                 | file system as HDFS, S3 ...                                             |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+



Morphline
=========

http://kitesdk.org/docs/0.11.0/kite-morphlines/morphlinesReferenceGuide.html

Here you can specify the transformations you want to apply before aggregation.

Example:
::
    "parsers": [
    {
      "name": "morphline-parser",
      "type": "Morphlines",
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
                  "appName": "/appName",
                  "method": "/method",
                  "datetime": "/date"
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


Split
=====

+-----------------+-------------------------------------------------------------------------+--------------------------+
| Property        | Description                                                             | Optional                 |
+=================+=========================================================================+==========================+
| name     | This is the path where the temporal data is going to be saved, this path| No|
|                 | should point to a distributed file system as HDFS, S3,...               |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+
| type            | This is the directory to save temporal data, this must be a distributed | No      |
|                 | file system as HDFS, S3,...                                             |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+
