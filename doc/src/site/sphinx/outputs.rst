
Outputs Plugins Configurations
******************

- :ref:`generic-label`

- :ref:`mongodb-label`

- :ref:`cassandra-label`

- :ref:`elasticsearch-label`

- :ref:`redis-label`

- :ref:`print-label`


.. image:: images/outputs.png
   :scale: 50 %
   :alt: Outputs in Sparkta



----------------------

.. _generic-label:

Generic Outputs Configuration
==========

In the SDK you can find the model that must follow an output to be implemented.It has several settings that can modify system operation.
These parameters can be completed in the policy file:

* multiplexer:
  If you want to multiplex all possible combinations that occur within a rollup,so that the outputs are saved
  multiple "tables".
  With this parameter the possibility of multi cubes and the possibility of generating it implements a data aggregation lake.
  Is possible omit this parameter in policy.

  * Sample:
::

   "multiplexer": ("true"/"false")  Default: "false"

* timeBucket:
  You can specify the time bucket containing the event, thanks to this parameter can be stored aggregate data and
  generate timeseries.
  This name will be as identified in the system of persistence.
  Is possible omit this parameter in policy.

  * Sample:
::

   "timeBucket": ("BUCKET_LABEL")  Default: ""

* granularity:
  If not created any bucketer time to identify with "timeBucket" you can leave the system assigned to each event time
   with the specified granularity.
  Is possible omit this parameter in policy.

  * Sample:
::

   "granularity": ("second"/"minute"/"hour"/"day"/"month"/"year"/)  Default: ""

* isAutoCalculateId:
   The system is capable of assigning an id added to each event, so that it may identify only the output.
   This field is calculated with all the values of the bucket rollup, including timeBucket date if the parameter is specified.
   Only for DataFrames persistence, disable in UpdateMetricOperation.
   Is possible omit this parameter in policy.

   * Sample:
::

   "isAutoCalculateId": ("true"/"false")  Default: "false"


.. _mongodb-label:

MongoDB Configuration
==========

The output of MongoDB does not use the generic implementation with DataFrames, it has multiple configuration
parameters to connect to the DB and self-creation of indexes.

* mongoClientUri:
   This parameter Connection routes specified the different nodes of a cluster of MongoDB, with replica set or with sharding.
   Is possible omit this parameter in policy.

   * Sample:
::

   "mongoClientUri": ("mongodb://localhost:27017")  Default: "mongodb://localhost:27017"

* dbName:
   The system is capable of assigning an id added to each event, so that it may identify only the output.
   This field is calculated with all the values of the bucket rollup, including timeBucket date if the parameter is specified.
   Only for DataFrames persistence, disable in UpdateMetricOperation.
   Is possible omit this parameter in policy.

   * Sample:
::

   "dbName": ("DATABASE_NAME")  Default: "sparkta"

* connectionsPerHost:
   Number of connections per host that the system open.
   Is possible omit this parameter in policy.

   * Sample:
::

   "connectionsPerHost": ("NUMBER")  Default: "5"

* threadsAllowedToBlock:
   This multiplier, multiplied with the connectionsPerHost setting, gives the maximum number of threads that may be waiting for a connection to become available from the pool.
   Is possible omit this parameter in policy.

   * Sample:
::

   "threadsAllowedToBlock": ("NUMBER")  Default: "10"

* fieldsSeparator:
   Is possible specify the character that separate the fields in the "textIndexFields" parameter.
   Is possible omit this parameter in policy.

   * Sample:
::

   "fieldsSeparator": ("any_character")  Default: ","

* textIndexFields:
   The system is capable of insert data in a full-text index. All of this fields compound the index.
   Is possible omit this parameter in policy.

   * Sample:
::

   "textIndexFields": ("field1,field2")  Default: ""

* language:
   Specify the language of the tokenizer in the full-text index in MongoDB, each document inserted must have this
   key-value.
   Is possible omit this parameter in policy.

   * Sample:
::

   "language": ("english"/"spanish"..)  Default: "none"


.. _cassandra-label:

Cassandra Configuration
==========

The output of MongoDB does not use the generic implementation with DataFrames, it has multiple configuration
parameters to connect to the DB and self-creation of indexes.

* mongoClientUri:
   This parameter Connection routes specified the different nodes of a cluster of MongoDB, with replica set or with sharding.
   Is possible omit this parameter in policy.

   * Sample:
::

   "mongoClientUri": ("mongodb://localhost:27017")  Default: "mongodb://localhost:27017"

