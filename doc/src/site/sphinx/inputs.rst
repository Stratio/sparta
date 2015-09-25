
Input Configuration
******************

- :ref:`flume-label`

- :ref:`kafka-label`

- :ref:`kafkaDirect-label`

- :ref:`rabbitMQ-label`

- :ref:`socket-label`

- :ref:`twitter-label`

- :ref:`websocket-label`


----------------------

.. _flume-label:

Input-flume
==========

Read events from apache-flume

* Sample:
::

  "input":
    {
      "name": "in-flume",
      "type": "Flume",
      "configuration": {
        "type": "pull",
        "addresses": "localhost:10999",
        "maxBatchSize": 500
      }
    }

+-----------------+------------------------------------------------------------------+------------+
| Property        | Description                                                      | Optional   |
+=================+==================================================================+============+
| name            | Name of the input                                                | No         |
+-----------------+------------------------------------------------------------------+------------+
| type            | The Type of the input it's used to instantiate specific classes  | No         |
+-----------------+------------------------------------------------------------------+------------+
| Configuration   | The kind of operation that we are going to do                    | No         |
| Type            |                                                                  |            |
+-----------------+------------------------------------------------------------------+------------+
| Configuration   | host/port to connect                                             | No         |
| addresses       |                                                                  |            |
+-----------------+------------------------------------------------------------------+------------+
| Configuration:  | The max number of lines to read and send to the channel at a time| No         |
| maxBatchSize    |                                                                  |            |
+-----------------+------------------------------------------------------------------+------------+
.. _kafka-label:

Input-kafka
=========
Read events from apache-kafka

* Sample::

   "input":
    {
     "name": "in-kafka",
      "type": "Kafka",
      "configuration": {
        "topics": "zion2:1",
        "kafkaParams.zookeeper.connect": "localhost:2181",
        "kafkaParams.group.id": "kafka-pruebas"
      }
    }

+--------------------------------+----------------------------------------------------------+------------+
| Property                       | Description                                              | Optional   |
+================================+==========================================================+============+
| name                           | Name of the input                                        | No         |
+--------------------------------+----------------------------------------------------------+------------+
| type                           | The Type of the input it's used to instantiate specific  | No         |
|                                | classes                                                  |            |
+--------------------------------+----------------------------------------------------------+------------+
| Configuration:                 | Kafka topic parameter is needed to connect to it and get | No         |
| topics                         | the data that generates                                  |            |
+--------------------------------+----------------------------------------------------------+------------+
| Configuration:                 | Zookeeper host/port to connect                           | No         |
| kafkaParams.zookeeper.connect  |                                                          |            |
+--------------------------------+----------------------------------------------------------+------------+
| Configuration:                 | It's a string that uniquely identifies a set of consumers| No         |
| kafkaParams.group.id           | within the same consumer group                           |            |
+--------------------------------+----------------------------------------------------------+------------+


.. _kafkaDirect-label:

Input-KafkaDirect
=========
Read events from KafkaDirect

* Sample::

  "input":
    {
      "name": "in",
      "type": "KafkaDirect",
      "configuration": {
        "topics": "test",
        "kafkaParams.metadata.broker.list": "localhost:9092",
        "kafkaParams.group.id": "my-spt-grp"
      }
    }
+----------------------------------+----------------------------------------------------------+------------+
| Property                         | Description                                              | Optional   |
+==================================+==========================================================+============+
| name                             | Name of the input                                        | No         |
+----------------------------------+----------------------------------------------------------+------------+
| type                             | The Type of the input it's used to instantiate specific  | No         |
|                                  | classes                                                  |            |
+----------------------------------+----------------------------------------------------------+------------+
| topics                           | Kafka topic parameter is needed to connect to it and get | No         |
|                                  | the data that generates                                  |            |
+----------------------------------+----------------------------------------------------------+------------+
| kafkaParams.metadata.broker.list | Defines where the Producer can find a one or more        | No         |
|                                  | Brokers to determine the Leader for each topic           |            |
+----------------------------------+----------------------------------------------------------+------------+
| kafkaParams.group.id             | It's a string that uniquely identifies a set of consumers| No         |
|                                  | within the same consumer group                           |            |
+----------------------------------+----------------------------------------------------------+------------+
.. _rabbitMQ-label:

Input-rabbitMQ
=========
Reads events from rabbitMQ

* Sample:
::

       "input":
        {
            "name": "in",
            "type": "RabbitMQ",
            "configuration": {
                "queue": "test",
                "host": "localhost",
                "port": 5672,
                "exchangeName": "twitterExchange",
                "routingKeys": [
                    "routingKey3"
                ]
            }
        }

+------------------+-----------------------------------------------------------------+-----------------------------------+
| Property         | Description                                                     | Optional                          |
+==================+=================================================================+===================================+
| name             | Name of the input                                               | No                                |
+------------------+-----------------------------------------------------------------+-----------------------------------+
| type             | The Type of the input it's used to instantiate specific         | No                                |
|                  | classes                                                         |                                   |
+------------------+-----------------------------------------------------------------+-----------------------------------+
| Configuration:   | Name of the queue                                               | Yes. If you use it, you won't need|
| queue            |                                                                 | exchangeName and Routing Keys     |
+------------------+-----------------------------------------------------------------+-----------------------------------+
| Configuration:   | Name or IP of the host                                          | No                                |
| host             |                                                                 |                                   |
+------------------+-----------------------------------------------------------------+-----------------------------------+
| Configuration:   | Port to connect and listen                                      | No                                |
| port             |                                                                 |                                   |
+------------------+-----------------------------------------------------------------+-----------------------------------+
| Configuration:   | Name of the exchange where the data will be consumed            | Yes. If queue name it's empty you |
| exchangeName     |                                                                 | have to specify the exchange      |
+------------------+-----------------------------------------------------------------+-----------------------------------+
| Routing keys:    | The exchange will delivery the messages to all the routing keys | Yes. If exchange name is set up   |
|                  |                                                                 | you need to specify the keys      |
+------------------+-----------------------------------------------------------------+-----------------------------------+



.. _socket-label:

Input-socket
=========
Reads events from a socket

* Sample:
::

      "input":
       {
         "name": "in-socket",
         "type": "Socket",
         "configuration": {
           "hostname": "localhost",
           "port": "10666"
          }
       }

+------------------+---------------------------------------------------------+-----------+
| Property         | Description                                             | Optional  |
+==================+=========================================================+===========+
| name             | Name of the input                                       | No        |
+------------------+---------------------------------------------------------+-----------+
| type             | The Type of the input it's used to instantiate specific | No        |
|                  | classes                                                 |           |
+------------------+---------------------------------------------------------+-----------+
| Configuration:   | Name or IP of the host                                  | No        |
| hostname         |                                                         |           |
+------------------+---------------------------------------------------------+-----------+
| Configuration:   | Port to connect and listen                              | No        |
| port             |                                                         |           |
+------------------+---------------------------------------------------------+-----------+
.. _twitter-label:

Input-twitter
=========
Reads events from Twitter API

* Sample:
::

  "input":
      {
      "name": "in-twitter",
      "type": "Twitter",
         "configuration": {
           "consumerKey": "****",
           "consumerSecret": "****",
           "accessToken": "****",
           "accessTokenSecret": "****",
           "termsOfSearch": "#Your,search,#terms,could be,#whatever"
      }
    }

+-------------------+-----------------------------------------------------------+------------+
| Property          | Description                                               | Optional   |
+===================+===========================================================+============+
| name              | Name of the input                                         | No         |
+-------------------+-----------------------------------------------------------+------------+
| type              | The Type of the input it's used to instantiate specific   | No         |
|                   | classes                                                   |            |
+-------------------+-----------------------------------------------------------+------------+
| Configuration:    | Twitter key                                               | No         |
| consumerKey       |                                                           |            |
+-------------------+-----------------------------------------------------------+------------+
| Configuration:    | Twitter key                                               | No         |
| consumerSecret    |                                                           |            |
+-------------------+-----------------------------------------------------------+------------+
| Configuration:    | Twitter key                                               | No         |
| accessToken       |                                                           |            |
+-------------------+-----------------------------------------------------------+------------+
| Configuration:    | Twitter key                                               | No         |
| accessTokenSecret |                                                           |            |
+-------------------+-----------------------------------------------------------+------------+
| Configuration:    | It allows you to search tweets based on the words of this | Yes        |
| termsOfSearch     | field. If you don't use this field, she search will be    |            |
|                   | base on the global trending topics                        |            |
+-------------------+-----------------------------------------------------------+------------+

.. _websocket-label:

Input-websocket
=========
Reads events from a websocket stream

* Sample:
::

  "input":
      {
      "name": "in-websocket",
      "type": "WebSocket",
         "configuration": {
           "url": "ws://stream.meetup.com/2/rsvps"
      }
    }

+-------------------+-----------------------------------------------------------+------------+
| Property          | Description                                               | Optional   |
+===================+===========================================================+============+
| name              | Name of the input                                         | No         |
+-------------------+-----------------------------------------------------------+------------+
| type              | The Type of the input it's used to instantiate specific   | No         |
|                   | classes                                                   |            |
+-------------------+-----------------------------------------------------------+------------+
| Configuration:    | Url for the stream                                        | No         |
| url               |                                                           |            |
+-------------------+-----------------------------------------------------------+------------+

