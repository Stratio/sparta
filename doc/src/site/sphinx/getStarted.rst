
Get Started
******************


Stratio Sparkta sandbox
===========
Sandbox is a test environment where you can easily try the examples. It's the fastest way to do it, because you don't need to install
all the applications needed such as zookeeper,mongoDB and many others by yourself.

Everything you need to run sparkta it's already in the sandbox, it allows you in a few minutes to have a fully Sparkta application installed with all their dependencies.

Also in our sandbox you can find some demonstrations of our technology, they are explained step by step `here <examples.html>`__ .

Step 1: Vagrant Setup
----------

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
----------

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
----------

 Located in /install-folder

 * Run the command::

    vagrant ssh



Step 4: Run Sparkta
----------


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
----------

 * Start the sandbox::

    vagrant up

 * Shut down the sandbox::

    vagrant halt

 * Exit the sandbox::

    exit


Now you are ready to `test <examples.html>`__ the sandbox

What you will find in the sandbox
----------

-  OS: CentOS 6.5
-  3GB RAM - 2 CPU
-  Two ethernet interfaces.

+------------------+---------+-------------------------------+
|    Name          | Version |         Command               |
+==================+=========+===============================+
| Spark            | 1.3.0   | service spark start           |
+------------------+---------+-------------------------------+
| Cassandra        | 2.1.2   | service cassandra start       |
+------------------+---------+-------------------------------+
| MongoDB          | 2.6.9   | service mongod start          |
+------------------+---------+-------------------------------+
| Elasticsearch    | 1.5.2   | service elasticearch start    |
+------------------+---------+-------------------------------+
| zookeeper        | 3.4.6   | service zookeeper start       |
+------------------+---------+-------------------------------+
| Kafka            | 0.8.1   |                               |
+------------------+---------+-------------------------------+
| scala            | 2.10.4  |                               |
+------------------+---------+-------------------------------+
| RabbitMQ         | 3.5.1   | service rabbitmq-server start |
+------------------+---------+-------------------------------+


F.A.Q about the sandbox
----------

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

Sparkta examples
===========

Twitter to MongoDB
----------

In this example we are going to show one of the most interesting inputs right now.
Let's explain what the example is going to do:


* Get the data that we want to work with, specifying it in the policy.
* Aggregate the data based on the policy parameters.
* Apply operators to the data such as count operator.
* Save the data in MongoDB, where we can see the results of the operations

Summarizing in this example we will take the text of the tweets that contains this two words, **Stratio** and **#Stratio**

Now let's get started on how to do it, without touching any line of code

Steps
^^^^

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





RabbitMQ: from Twitter to MongoDB
----------

Example to take data in streaming from Twitter and ingesting it in RabbitMQ in order to test the Sparkta input.
To access to the Twitter API it is necessary to config the file::

    /opt/sds/sparkta/examples/data-generators/twitter-to-rabbit/src/main/resources/twitter4j.properties

Steps

* Run the RabbitMQ server where we want to read from. We will use Mongodb to write our aggregated data in the sparta
database::

    sudo service rabbitmq-server start

    sudo service mongod start

* Next we run Sparkta and send the policy.
If you are using the sandbox, you may need to start a new ssh session ( **vagrant ssh** ).
This policy contains the configuration that tells Sparkta where to read,
where to write and how to transform the input data.

 Note that Zookeeper must be running::

    sudo service zookeeper start

    service sparkta start

    curl -H "Content-Type: application/json" http://localhost:9090/policyContext --data @examples/data-generators/twitter-to-rabbit/twitter-policy.json

* There are two ways of testing it. Producing data directly into a RabbitMQ queue or producing data into a RabbitMQ
queue through a direct exchange (https://www.rabbitmq.com/tutorials/tutorial-four-java.html)

    - For producing data directly into a RabbitMQ queue run the class TwitterToRabbitMQSimple::

      cd /opt/sds/sparkta/examples/data-generators/twitter-to-rabbit/

      mvn clean package

      mvn exec:java -Dexec.mainClass="com.stratio.examples.twittertorabbit.TwitterToRabbitMQSimple"

    - For Producing data into a RabbitMQ queue through a direct exchange run the class TwitterToRabbitMQWithRouting
    with the routingKey you want to write the data as argument::

      cd /opt/sds/sparkta/examples/data-generators/twitter-to-rabbit/

      mvn clean package

      mvn exec:java -Dexec.mainClass="com.stratio.examples.twittertorabbit.TwitterToRabbitMQWithRouting" -Dexec.args="routingKey3"

e-commerce to RabbitMQ and ElasticSearch
----------

This example simulates an environment of an e-commerce architecture.
In one hand we have the logs generated by an apache server and in the other the orders requested in the web site.
We'll publish all this events in `RabbitMQ <https://www.rabbitmq.com>`__ and aggregate them with Sparkta which will
save the aggregated data in elasticsearch.

Steps

* First we need to start the RabbitMQ server where we will tell Sparkta to read from. And elasticsearch where Sparkta
will save the aggregated data::

    sudo service rabbitmq-server start

    sudo service elasticsearch start

* Next we run Sparkta and send the policy. This policy contains the configuration that tells Sparkta where to read,
where to write and how to transform the input data. Note that Zookeeper must be running::

    sudo service zookeeper start

    service sparkta start

    curl -H "Content-Type: application/json" http://localhost:9090/policyContext --data @examples/data-generators/ecommerce/ecommerce-policy.json

* And last we need to run the data generators in two different shells. This generators will generate random data and
will write it into RabbitMQ. In a few seconds Sparkta will start to read the data and write it into elasticsearch::

    cd examples/data-generators/ecommerce

    mvn -PorderLines clean install benerator:generate

    mvn -PvisitLog clean install benerator:generate


Script Examples
----------

We have added also some scripts to help you getting started with Sparkta. When you execute one of the scripts,
it will create a policy with a WebSocket input and some database to storage the data. The policy will be displayed
in the website, so you can skip the process of create a policy by yourself.

The path to the scripts is::

 /opt/sds/sparkta/examples/scripts/



