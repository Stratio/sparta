
Input plugins Configuration
******************

- :ref:`flume-label`

- :ref:`kafka-label`

- :ref:`rabbitMQ-label`

- :ref:`socket-label`

- :ref:`twitter-label`


----------------------

.. _flume-label:

Input-flume
==========

Read events from apache-flume

* Sample:
::

    "inputs": [
          {
            "name": "flumeInputName",
            "elementType": "FlumeInput",
            "configuration": {
                "type" : "pull", //pull or push
                "addresses": "[IP]:[PORT]",
                "storageLevel" : "MEMORY_AND_DISK_SER_2" //Spark storage conf
            }
        }
    ]

.. _kafka-label:

Input-kafka
=========
Reads events from apache-kafka

* Sample:
::

    "inputs": [
        {
            "name": "in",
            "elementType": "KafkaInput",
            "configuration": {
                "topics": "topic:1",
                "kafkaParams.zookeeper.connect" : "[IP]:[PORT]",
                "kafkaParams.group.id" : "kafka-test",
                "storageLevel" : "MEMORY_AND_DISK_SER_2"  //Spark storage conf
            }
        }
    ]

.. _rabbitMQ-label:

Input-rabbitMQ
=========
Reads events from rabbitMQ

* Sample:
::

     "inputs": [
        {
          "name": "in",
          "elementType": "RabbitMQInput",
          "configuration": {
            "queue": "logsQueue",
            "host": "localhost",
            "port": 5672,
            "storageLevel": "MEMORY_ONLY",
            "exchangeName": "logsExchange",
            "routingKeys" : ["webLogsRoute","purchasesRoute"]
          }
        }
      ]

.. _socket-label:

Input-socket
=========
Reads events from a socket

* Sample:
::

    "inputs": [
        {
            "name": "in-socket",
            "elementType": "socket",
            "configuration": {
                "hostname": "localhost",
                "port": "9998",
                "storageLevel": "MEMORY_ONLY"
            }
        }
    ]

.. _twitter-label:

Input-twitter
=========
Reads events from Twitter API

* Sample:
::

  "inputs": [
      {
      "name": "input-twitter",
      "elementType": "TwitterInput",
      "configuration": {
        "consumerKey": "CONSUMER_KEY",
        "consumerSecret": "CONSUMER_SECRET",
        "accessToken": "ACCESS_TOKEN",
        "accessTokenSecret": "ACCESS_TOKEN_SECRET"
      }
    }
  ]

