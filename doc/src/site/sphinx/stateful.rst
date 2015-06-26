Checkpointing
*************

The system runs with time windows, these windows are configurable and allow us to use not associative operations
through Apache Spark Streaming |streaming_link|

.. |examples_link| raw:: html

   <a href="https://github.com/Stratio/sparkta/tree/master/examples/policies"
   target="_blank">examples</a>

This is an example of the checkpointing configuration in a policy::

    "checkpointing": {
        "path": "myCheckpointPath",
        "timeDimension": "minute",
        "granularity": "minute",
        "interval": 30000,
        "timeAvailability": 60000
      }

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



* timeBucket:
   You can specify the time dimension containing the event, thanks to this parameter can be stored aggregate data and
   generate timeseries.
   This name will be as identified in the system of persistence.
   Is possible omit this parameter in policy.

   * Example:
::

   "timeBucket": ("BUCKET_LABEL")  Default: "minute"

* checkpointGranularity:
   If not created any dimensioner time to identify with "timeBucket" you can leave the system assigned to each event time
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

