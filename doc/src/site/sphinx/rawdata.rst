Saving the raw data
*******************

Sometimes it's interesting to save the incoming data for post-analizing purposes or just for backup.
Sparkta gives the possibility to persist this data in raw format in storage services like S3 or HDFS.
Sparkta save the data as a Parquet file.You can find some examples |parquet_link|

.. |parquet_link| raw:: html

   <a href="https://parquet.apache.org/documentation/latest/"
   target="_blank">here</a>


The following code is an example of the policy block that is needed to setup the raw data backup::

  "rawData": {
    "enabled": "true",
    "path": "myParquetPath"
  }

+-------------------+-------------------------------------------------------------------------+------------------------+
| Property          | Description                                                             | Optional               |
+===================+=========================================================================+========================+
| enabled           | This parameter set if the raw data is going to be saved or not          | Yes (default: false)   |
+-------------------+-------------------------------------------------------------------------+------------------------+
| path              | This is the path where the temporal data is going to be saved, this path| Yes (default:default)  |
|                   | should point to a distributed file system as HDFS, S3,...               |                        |
+-------------------+-------------------------------------------------------------------------+------------------------+

