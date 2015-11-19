Getting Started
******************

Prerequisites
===========

Apache Zookeeper needs to be installed and run in the system beforehand. You can run it in the sandbox with the
following commands::

    cd /opt/sds/zookeeper
    sudo ./bin/zkServer.sh start



Download
===========
An easy way to test Sparkta is using it with the :ref:`sandbox-label`.

To download Sparkta you can do itby unpackaging a `release <https://github
.com/Stratio/sparkta/releases>`__ or by generating the deb or rpm packages from the `source code <https://github
.com/Stratio/sparkta>`__.

Build
===========

You can generate rpm and deb packages by running::

    mvn clean package -Ppackage

**Note:** you need to have installed the following programs in order to build these packages:

 * In a debian distribution:

  - fakeroot
  - dpkg-dev
  - rpm

 * In a centOS distribution:

  - fakeroot
  - dpkg-dev
  - rpmdevtools

Install
===========

Once you have the rpm or deb packages you need to install them::

 dpkg -i package.deb

 rpm -i package.rpm

In case you need more help about how to install packages you can have more information about |deb_link| and |rpm_link|.


.. |deb_link| raw:: html

   <a href="http://www.cyberciti.biz/faq/ubuntu-linux-how-do-i-install-deb-packages/"
   target="_blank">deb</a>

.. |rpm_link| raw:: html

   <a href="http://www.rpm.org/max-rpm/ch-rpm-install.html"
   target="_blank">rpm</a>


Configure
===========

XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
Run
===========

Once Sparkta has been installed, to get it running you can use the following instructions:

 * You can run ``sh $SPARKTA_HOME/bin/run``. Default installation directory is in ``/opt/sds/sparkta``::

      cd /opt/sds/sparkta

      sh bin/run

 * As a service::

      sudo service sparkta [start|stop|restart|status]


Next Steps
===========

Creating a policy
------------

A policy it's a JSON document that define your aggregation rules. It's composed of:

* `Inputs <inputs.html>`__: where is the data coming from?
* `Transformations <transformations.html>`__ : do you want to enrich your data?
* `Cubes <cube.html>`__ : how do you want to aggregate your data?
* `Outputs <outputs.html>`__ : where aggregated data should be stored?

A policy is the way we tell Sparkta how to aggregate data. It is in JSON format and you can check some
|examples_link| in the official repository.

.. |examples_link| raw:: html

   <a href="https://github.com/Stratio/sparkta/tree/master/examples/policies"
   target="_blank">examples</a>

It consists of the following parts:


General configuration
^^^^^^^

In this part you have to define the global parameters of your policy::

 {
  "name": "policy-TwitterJson-Cassandra",
  "description": "policy-TwitterJson-Cassandra",
  "sparkStreamingWindow": 6000,
  "checkpointPath": "checkpoint",
  "rawData": {
    "enabled": "true",
    "partitionFormat": "day",
    "path": "myTestParquetPath"
  }
+--------------------------+-----------------------------------------------+----------+
| Property                 | Description                                   | Optional |
+==========================+===============================================+==========+
| name                     | Policy name to identify it                    | No       |
+--------------------------+-----------------------------------------------+----------+
| sparkStreamingWindow     | Apache Spark Streaming window duration        | No       |
+--------------------------+-----------------------------------------------+----------+
| checkpointPath           | The path to save the checkpoint               | No       |
+--------------------------+-----------------------------------------------+----------+
| rawData                  | To specify if you want to save the raw data   | No       |
+--------------------------+-----------------------------------------------+----------+

The `rawData` block allow you to save the `raw data <rawdata.html>`__ into HDFS + Parquet.

.. _input:

Inputs
^^^^^^^

Here you can define the source of your data. Currently, you can have just one input. For more information
about supported inputs, you can visit :doc:`inputs`

Example::

 "input": {
    "name": "in-twitter-json",
    "type": "TwitterJson",
    "configuration": {
      "consumerKey": "*********",
      "consumerSecret": "*********",
      "accessToken": "*********",
      "accessTokenSecret": "*********"
    }
  }

Transformations
^^^^^^^

Once the data passes through the input to Sparkta you usually need to treat this raw data in order to model your fields.

You can learn more about transformations `here <transformations.html>`__

Example::

  "transformations": [
      {
        "name": "morphline-parser",
        "order": 0,
        "type": "Morphlines",
        "outputFields": [
          "userName",
          "tweet",
          "responseTime"
        ],
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
                    "userName": "/user/name",
                    "tweet": "/user/tweet",
                    "responseTime": "/responseTime"
                  }
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
      },
      {
        "name": "responseTime-parser",
        "order": 1,
        "inputField": "responseTime",
        "outputFields": [
          "system-timestamp"
        ],
        "type": "DateTime",
        "configuration": {
          "responseTime": "unixMillis"
        }
      }
    ]

.. _cube:


Cubes
^^^^^^^

The cubes are the way you want to aggregate your fields generated in the previous step.

Learn more about cubes `here <cube.html>`__ .

Example::

    "cubes": [
      {
        "name": "tweets-per-user-per-minute",
        "checkpointConfig": {
          "timeDimension": "minute",
          "granularity": "minute",
          "interval": 30000,
          "timeAvailability": 60000
        },
        "dimensions": [
          {
            "name": "userName",
            "field": "userName",
            "type": "Default"
          },
          {
            "name": "tweet",
            "field": "tweet",
            "type": "Default"
          },
          {
            "name": "responseTime",
            "field": "responseTime",
            "type": "DateTime",
            "precision": "minute"
          }
        ],
        "operators": [
          {
            "name": "count-operator",
            "type": "Count",
            "configuration": {}
          }
        ]
      }
    ]


.. _output:


Outputs
^^^^^^^

Here is where you decide where to persist your aggregated data. An output is equivalent to a datastore. You can
have one or more outputs in your policy.

Note: it is important to mark that the result of the cube is saved in a datastore table. The name of this table is
built concatenating the dimension names of the cube.

In the previous example the name of the table would be userName_tweet_responseTime. Be careful with not allowed
characters or size of the names. For example Cassandra do not allow tables with more of 48 characters or capital
letters in its name (then userName_tweet_responseTime is incorrect).

Learn more about outputs `here <outputs.html>`__ .

Example::

    "outputs": [
      {
        "name": "out-mongo",
        "elementType": "MongoDb",
        "configuration": {
          "hosts": [{"host": "localhost" , "port": "27017" }],
          "dbName": "sparkta"
        }
      }
    ]
Submitting a Policy
--------

The best way to submit a policy is using the web, but you can still doing it with the terminal. The commands belows run Sparkta with the given policy.

The policy must be submitted with the following syntax::

    curl -H "Content-Type: application/json" --data @PATH-TO-POLICY http://<SPARKTA-HOST>:<SPARKTA-PORT>/policyContext

Example::

    curl -H "Content-Type: application/json" --data @examples/policies/ITwitter-OMongo.json http://localhost:9090/policyContext

Examples
========
Twitter to MongoDB
----------

In this example we are going to show one of the most interesting inputs right now.
Let's explain what the example is going to do:


* Get the data that we want to work with, specifying it in the policy.
* Aggregate the data based on the policy parameters.
* Apply operators to the data such as count operator.
* Save the data in MongoDB, where we can see the results of the operations

Summarizing in this example we will take the text of the tweets that contains this two words, **Stratio** and **#Stratio**

Now let's get started on how to do it, without touching any line of code:

* **First**

The most important step it's to set the policy up with the right parameters. ::

     "input":
      {
     "name": "in-twitter-json",
     "type": "TwitterJson",
      "configuration": {
        "consumerKey": "****",
        "consumerSecret": "****",
        "accessToken": "****",
        "accessTokenSecret": "****",
        "termsOfSearch":"Stratio,#Stratio"
       }
      }
     ]

In order to get the twitter access keys you will have to register in |twitter_keys|


.. |twitter_keys| raw:: html

   <a href="https://apps.twitter.com/"
   target="_blank">Twitter developer web site</a>


Once you have they keys you have to edit the policy file::

 cd /opt/sds/sparkta/examples/policies/Twitter-Stratio-Example.json

The new feature that we have included in the twitter input it's the parameter **termsOfSearch**, it allows you
to search tweets based on the words you specify on it. They could be single words or hashtags.
If the program find one of the words, the tweet will be sent to be processed.

Now it's the time to decide if we want to custom our twitter search with our own terms or
if we want the global trending topic at the moment.
As we explained, if in the input you add::

 "termsOfSearch":"Stratio,#Stratio"


It will be a custom search, if you want the other choice(global trending topics) just delete the whole line, and the
policy will look like this::

 "input":
      {
     "name": "in-twitter-json",
     "type": "TwitterJson",
      "configuration": {
        "consumerKey": "****",
        "consumerSecret": "****",
        "accessToken": "****",
        "accessTokenSecret": "****",
       }
      }
    ]

The event this input is going to read has |event| structure.

  .. |event| raw:: html

   <a href="https://github.com/Stratio/Sparkta/blob/master/doc/src/site/sphinx/Twitter-JSON-Format.json"
   target="_blank">this</a>

* **Second**

Then we have to define how our cube is going to be::

   "cubes": [
    {
      "name": "testCube",
      "checkpointConfig": {
        "timeDimension": "minute",
        "granularity": "minute",
        "interval": 30000,
        "timeAvailability": 60000
      },
      "dimensions": [
        {
          "field": "text",
          "name": "text"
        }
      ],
      "operators": [
        {
          "name": "total",
          "type": "Count",
          "configuration": {}
        }
      ]
    }
   ]
The main dimension is **text**.

In this example we are going to use the count operator.
Count operator will count the number of events that are exactly the same, even so it's not really important for this example since we just want the text of the tweets that contains **Stratio** and **#Stratio**.

* **Fourth**

The last step it's to declare our output database where we want to store our aggregated data.
In this example we use MongoDB as database::

  "outputs": [
    {
      "name": "out-mongo",
      "type": "MongoDb",
      "configuration": {
        "hosts": [{"host": "localhost" , "port": "27017" }],
        "dbName": "sparkta"
      }
    }
  ]

You can have more information about the policies configuration in the |doc_link|

.. |doc_link| raw:: html

   <a href="http://docs.stratio.com/modules/sparkta/development/inputs.html#twitter-label"
   target="_blank">documentation</a>

After we had configured our policy, let's get started in the example!

Note that Zookeeper must be running::

      sudo service zookeeper start

Run Sparkta::

      service sparkta start

Now let's send the policy to sparkta::

      cd /opt/sds/sparkta

      curl -X POST -H "Content-Type: application/json" --data @examples/policies/Twitter-Stratio-Example.json localhost:9090/policyContext

When sparkta is running it's ready to work, open your twitter account and write some tweets within a minute, since we are going to aggregate by minute(You can see the full policy |twitter_policy_link|)


.. |twitter_policy_link| raw:: html

   <a href="https://github.com/Stratio/sparkta/blob/master/examples/policies/Twitter-Stratio-Example.json"
   target="_blank">here</a>

As you can see in the next figure we tweeted two tweets, one using the word **Stratio** and one using the hashtag **#Stratio**, so we can say that now we have a twitter filter for this two words. That would be very useful if for example you want to know if twitter users are talking about your company.

    .. image:: images/twitterExample.png
       :height: 350 px
       :width:  500 px
       :scale:  100 %

    Now let's open a shell with MongoDB to see the aggregations::

 > sudo service mongod start


Find our database::

 > show dbs

 local    0.078GB
 sparkta  0.078GB

Enter in the database::

 > use sparkta

 switched to db sparkta

See the collections::

 > show collections

 id_text_minute
 system.indexes

Enter in the collection and find the results of the operations::

 > db.id_text_minute.find().pretty()


 {
	"_id" : ObjectId("5624b78f90132206bc766c63"),
	"id" : "#Stratio Stratio is the first spark-based big data platform released best time-to-value product in the market._2015-10-19 11:27:00.0",
	"text" : "#Stratio Stratio is the first spark-based big data platform released best time-to-value product in the market.",
	"minute" : ISODate("2015-10-19T09:27:00Z"),
	"total" : NumberLong(1)
 }

 {
	"_id" : ObjectId("5624b7d690132206bc766c64"),
	"id" : "In Stratio we work with BIG DATA_2015-10-19 11:28:00.0",
	"text" : "In Stratio we work with BIG DATA",
	"minute" : ISODate("2015-10-19T09:28:00Z"),
	"total" : NumberLong(1)
 }






.. _sandbox-label:
Sandbox
-----------
The sandbox is a test environment where you can easily try the examples. It's the fastest way to do it, because you don't need to install
all the applications needed such as zookeeper,mongoDB and many others by yourself.

Everything you need to run sparkta it's already in the sandbox, it allows you in a few minutes to have a fully Sparkta application installed with all their dependencies.

Also in our sandbox you can find some demonstrations of our technology, they are explained step by step in the :ref:`examples-label`.

Step 1: Vagrant Setup
^^^^^^^

To get an operating virtual machine with Stratio Sparkta distribution up
and running, we use `Vagrant <https://www.vagrantup.com/>`__.

-  Download and install the lastest version (1.7.2)
   `Vagrant <https://www.vagrantup.com/downloads.html>`__.
-  Download and install
   `VirtualBox <https://www.virtualbox.org/wiki/Downloads>`__.
-  If you are in a windows machine, we will install
   `Cygwin <https://cygwin.com/install.html>`__.

   **Note** that if you are using windows10 you might find interesting this `post <https://github.com/mitchellh/vagrant/issues/6059/>`__ in vagrant repository.
   You can download the VirtualBox fixed `here <https://www.virtualbox.org/attachment/ticket/14040/VBox-Win10-fix-14040.exe>`__

Step 2: Running the sandbox
^^^^^^

 * Initialize the current directory from the command line::

     vagrant init stratio/sparkta


 * Start the sandbox from command line::

     vagrant up

 * In case you need to provision the sandbox run::

     vagrant provision


 * If the sandbox ask you for the credentials::


     user -> vagrant

     pass -> vagrant


Step 3: Accessing the sandbox
^^^^^^

 Located in /install-folder

 * Run the command::

    vagrant ssh



Step 4: Run Sparkta
^^^^^^^


 To start Sparkta

 * Start zookeeper::

    sudo service zookeeper start

 * Start Sparkta::

    sudo service sparkta start

 * You can check the logs in::

    /var/log/sds/sparkta/sparkta.out

 * You can configure Sparkta in::

    /etc/sds/sparkta/


Useful commands
^^^^^^

 * Start the sandbox::

    vagrant up

 * Shut down the sandbox::

    vagrant halt

 * Exit the sandbox::

    exit


Now you are ready to test the :ref:`examples-label` with the sandbox

What you will find in the sandbox
^^^^^^

-  OS: CentOS 6.7
-  3GB RAM - 2 CPU
-  Two ethernet interfaces.

+------------------+---------+-------------------------------+
|    Name          | Version |         Command               |
+==================+=========+===============================+
| Spark            | 1.4.1   |                               |
+------------------+---------+-------------------------------+
| Cassandra        | 2.1.4.0 | service cassandra start       |
+------------------+---------+-------------------------------+
| MongoDB          | 2.6.11  | service mongod start          |
+------------------+---------+-------------------------------+
| Elasticsearch    | 1.5.2   | service elasticsearch start   |
+------------------+---------+-------------------------------+
| zookeeper        | 3.4.6   | service zookeeper start       |
+------------------+---------+-------------------------------+
| Kafka            | 0.8.1.1 |                               |
+------------------+---------+-------------------------------+
| scala            | 2.10    |                               |
+------------------+---------+-------------------------------+


F.A.Q about the sandbox
^^^^^^

I am in the same directory that I copy the Vagrant file but I have this error:

.. code:: bash

        A Vagrant environment or target machine is required to run this
        command. Run vagrant init to create a new Vagrant environment. Or,
        get an ID of a target machine from vagrant global-status to run
        this command on. A final option is to change to a directory with a
        Vagrantfile and to try again.

Make sure your file name is Vagrantfile instead of Vagrantfile.txt or
VagrantFile.

--------------

When I execute vagrant ssh I have this error:

.. code:: bash

        ssh executable not found in any directories in the %PATH% variable. Is an
        SSH client installed? Try installing Cygwin, MinGW or Git, all of which
        contain an SSH client. Or use your favorite SSH client with the following
        authentication information shown below:

We need to install `Cygwin <https://cygwin.com/install.html>`__ or `Git
for Windows <http://git-scm.com/download/win>`__.

