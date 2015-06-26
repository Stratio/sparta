Saving the raw data
*******************

Sometimes one is interested in persisting theirs incoming data for post-analizing purposes or just for backup.
SpaRkTA gives the possibility to persist this data in raw format in storage services like Parquet or HDFS. The
following code is an example of the policy block that is needed to setup the raw data backup::

  "rawData": {
    "enabled": "true",
    "partitionFormat": "day",
    "path": "myParquetPath"
  }

+-------------------+-------------------------------------------------------------------------+--------------------------+
| Property          | Description                                                             | Optional              |
+===================+=========================================================================+==========================+
| saveRawData       | This is the path where the temporal data is going to be saved, this path| Yes (default: false)  |
|                   | should point to a distributed file system as HDFS, S3,...           |                          |
+-------------------+-------------------------------------------------------------------------+--------------------------+
| rawDataParquetPath| This is the directory to save temporal data, this must be a distributed | Yes (default: xxx)       |
|                   | file system as HDFS, S3,...                                      |                          |
+-------------------+-------------------------------------------------------------------------+--------------------------+
| rawDataGranularity| This is the directory to save temporal data, this must be a distributed | Yes (default: minute)    |
|                   | file system as HDFS, S3 ...                                           |                        |
+-------------------+-------------------------------------------------------------------------+--------------------------+
