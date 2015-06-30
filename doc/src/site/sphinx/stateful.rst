Checkpointing
*************

The system works with time windows, they are configurable and allow us to use associative operations
through Apache Spark Streaming |streaming_link|

.. |streaming_link| raw:: html

   <a href="https://spark.apache.org/docs/latest/streaming-programming-guide.html#checkpointing"
   target="_blank">configuration parameters</a>

This is an example of the checkpointing configuration in a policy::

    "checkpointing": {
        "path": "myCheckpointPath",
        "timeDimension": "timeStamp",
        "granularity": "minute",
        "interval": 30000,
        "timeAvailability": 60000
      }

+-----------------+-------------------------------------------------------------------------+--------------------------+
| Property        | Description                                                             | Optional                 |
+=================+=========================================================================+==========================+
| path            | This is the path where the temporal data is going to be saved, this     | Yes (default: checkpoint)|
|                 | path should point to a distributed file system as HDFS, S3,...          |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+
| timeDimension   | This is the name of the time dimension, if the cube contains this       | Yes                      |
|                 | dimension, this is used for group events with the selected granularity. |                          |
|                 | If the cube not contain this dimension, the time is auto generated.     |                          |
|                 | The data persistence save this name as a field.                         |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+
| granularity     | Defines the granularity of time in the process of aggregation.          | No                       |
|                 | The events will be saved and grouped with the minimum granuladidad.     |                          |
|                 | Additionally, this parameter will mark the time of the event.           |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+
| interval        | This parameter defines the time in milliseconds that store the          | Yes (default: 20000)     |
|                 | temporary data in a distributed file system. This must be 5 or 10       |                          |
|                 | times the sparkStreamingWindow parameter.                               |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+
| timeAvailability| This is the addicional time in milliseconds that allows us to have      | Yes (default: 60000)     |
|                 | data stored in the temporary file system. Thus time we can receive      |                          |
|                 | events that include a pre-current time, with this parameter is possible |                          |
|                 | define a maximum time in which is expected to receive these events      |                          |
|                 | to add.                                                                 |                          |
+-----------------+-------------------------------------------------------------------------+--------------------------+
