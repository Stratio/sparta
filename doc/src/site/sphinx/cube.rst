Cubes
*****

If you know about OLAP you already know what a cube is. Cubes are an abstract representation of a dimension
combination and some operations over them. If you come from the relational world you can view cubes as a groupBy.

To create a cube you have to select at least one field. This field will be a dimension in your cube. You need also to
 select at least one operator.

Dimensions an operators will be explained later.

This is an example of how create a cube::

    {
      "name": "cube-session-per-minute",
      "checkpointConfig": {
        "timeDimension": "minute",
        "granularity": "minute",
        "interval": 30000,
        "timeAvailability": 60000
      },
      "dimensions": [
        {
          "name": "session",
          "field": "session"
          "configuration": {
            "typeOp": "string"
          }
        },
        {
          "name": "minute",
          "field": "timestamp",
          "type": "DateTime",
          "precision": "minute"
        }
      ],
      "operators": [
        {
          "name": "max-operator",
          "type": "Max",
          "configuration": {
            "inputField": "responseTime"
          }
        },
        {
          "name": "min-operator",
          "type": "Min",
          "configuration": {
            "inputField": "responseTime",
            "typeOp": "int"
          }
        }
      ]
    }


+-----------------+------------------------------------------------------------------+------------+
| Property        | Description                                                      | Optional   |
+=================+==================================================================+============+
| name            | Name of the cube                                                 | No         |
+-----------------+------------------------------------------------------------------+------------+
| dimensions      | Array of dimensions that compound the cube                       | No         |
+-----------------+------------------------------------------------------------------+------------+
| operators       | Array of operators                                               | No         |
+-----------------+------------------------------------------------------------------+------------+

The `checkpointing <stateful.html>`__ block is where you have to define the Apache Spark Streaming |streaming_link|

.. |streaming_link| raw:: html

   <a href="https://spark.apache.org/docs/latest/streaming-programming-guide.html#checkpointing"
   target="_blank">configuration parameters</a>

Dimensions
==========
Dimensions are the fields we want to group by. At the same time you can group by some sub-aggregation depending on
the field nature. This sub-aggregation is done depending on the dimension type.

Example of two dimensions in the cube::

      "dimensions": [
        {
          "name": "session",
          "field": "session"
        },
        {
          "name": "country",
          "field": "country"
        }
      ]

These are the common properties of a dimension

+-----------------+---------------------------------------------------------------------------------------+------------+
| Property        | Description                                                                           | Optional   |
+=================+=======================================================================================+============+
| name            | Name of the cube                                                                      | No         |
+-----------------+---------------------------------------------------------------------------------------+------------+
| field           | Field name that comes either in the input or in the transformations                   | No         |
+-----------------+---------------------------------------------------------------------------------------+------------+
| type            | Dimension type. It will sub-aggregate or not depending on the type                    | No         |
+-----------------+---------------------------------------------------------------------------------------+------------+
| precision       | In case the selected type make a sub-aggregation you will have to set this property   | Depending  |
|                 | in order to mark the precision for which you want to group by                         | on the type|
+-----------------+---------------------------------------------------------------------------------------+------------+
| typeOp          | Is possible select the returned type of the dimension. (int, long, double, string...) | Yes        |
+-----------------+---------------------------------------------------------------------------------------+------------+

Below are shown the possible dimension types available:

Default
-------

As the name says this is the default dimension. With this kind of dimension there is no sub-aggregation and therefore
 no precision.


DateTime
--------

A DateTime dimension will sub-aggregate by truncating the selected field into a time unit marked by the precision.
The selected field have to be a Date type. This is why this type of dimension is related with the `DateTime
transformation <transformations.html#datetime-transformation-label>`__

Possible precision values are: second, minute, hour, day, month and year

GeoHash
-------

The GeoHash dimension allow you to group by squares. It has to be feed by a field that is a location.

+-------------+-----------------------+
| precision   | zone to group by      |
+=============+=======================+
| precision1  | 5,009.4km x 4,992.6km |
+-------------+-----------------------+
| precision2  | 51,252.3km x 624.1km  |
+-------------+-----------------------+
| precision3  | 156.5km x 156km       |
+-------------+-----------------------+
| precision4  | 39.1km x 19.5km       |
+-------------+-----------------------+
| precision5  | 4.9km x 4.9km         |
+-------------+-----------------------+
| precision6  | 1.2km x 609.4m        |
+-------------+-----------------------+
| precision7  | 152.9m x 152.4m       |
+-------------+-----------------------+
| precision8  | 38.2m x 19m           |
+-------------+-----------------------+
| precision9  | 4.8m x 4.8m           |
+-------------+-----------------------+
| precision10 | 1.2m x 59.5cm         |
+-------------+-----------------------+
| precision11 | 14.9cm x 14.9cm       |
+-------------+-----------------------+
| precision12 | 3.7cm x 1.9cm         |
+-------------+-----------------------+


Tag
---

Given a field that is a sentence it is possible to group by some token in it.
Precision possible values are: firstTag, lastTag and allTags


Operators
=========

Operators allow us to make an operation over the aggregation that are made in the cube through the dimensions.

It is possible to apply filters on operators. If you make one or more filters, aggregation applies only on the values
that satisfy each and every one of them.
These filters should be entered in the operator configuration block.

Two examples of these filters are::

  "configuration": {
    "filters": [
      {"field":"field1", "type": "<", "dimensionValue":"field2"},
      {"field":"field1", "type": "!=", "value":2}
    ]
  }


The logical operators currently implemented are::

  <, >, <=, >=, =, !=



Accumulator
-----------

Returns the accumulation of the different values for a given field

Average
-------

Returns the arithmetic average for a given field

Count
-----

Returns the count of the different aggregations

First
-----

Returns the first value for a given field

Last
----

Returns the last value for a given field

FullText
--------

Returns an array of the different values for a given field


Max
---

Returns the max value for a given field

Min
---

Returns the min value for a given field

Median
------

Returns the arithmetic median for a given field

Range
-----

Returns the max value less the min value for a given field

Standard deviation
------------------

Returns the arithmetic standard deviation for a given field


Sum
---

Returns the sum for a given field

Variance
--------

Returns the arithmetic variance for a given field