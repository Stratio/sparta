.. _examples:

Sparkta examples
****************

Twitter to MongoDB
=================================

In this example we are going to show one of the most interesting inputs right now.
Let's explain what the example is going to do:


* Get the data that we want to work with, specifying it in the policy.
* Aggregate the data based on the policy parameters.
* Apply operations such as counters,max,min,averages and many others.
* Save the data in MongoDB, where we can see the results of the operations

Now let's get started on how to do it, without touching any line of code

Steps
----------

* **First**

The most important step it's to set the policy up with the right parameters. ::

     "input":
      {
      "name": "in-twitter",
      "type": "Twitter",
      "configuration": {
        "consumerKey": "****",
        "consumerSecret": "****",
        "accessToken": "****",
        "accessTokenSecret": "****",
        "termsOfSearch":"Your,#search,#could,be,whatever"
       }
      }
     ]

In order to get the twitter access keys you will have to register in |twitter_keys|


.. |twitter_keys| raw:: html

   <a href="https://apps.twitter.com/"
   target="_blank">Twitter developer web site</a>


Once you have they keys you have to edit the policy file::

 cd /opt/sds/sparkta/examples/policies/ITwitter-OMongo-Example.json

The new feature that we have included in the twitter input it's the parameter **termsOfSearch**, it allows you
to search tweets based on the words you specify on it. They could be single words or hashtags.
If the program find one of the words, the tweet will be sent to be processed.

Now it's the time to decide if we want to custom our twitter search with our own terms or
if we want the global trending topic at the moment.
As we explained, if in the input you add::

 "termsOfSearch":"Your,#search,#could,be,whatever"


It will be a custom search, if you want the other choice(global trending topics) just delete the whole line, and the
policy will look like this::

 "input":
      {
      "name": "in-twitter",
      "type": "Twitter",
      "configuration": {
        "consumerKey": "****",
        "consumerSecret": "****",
        "accessToken": "****",
        "accessTokenSecret": "****",
       }
      }
     ]

* **Second**

Then we have to choose which dimensions we are going to consider relevant::

    "cubes": [
    {
      "name": "testCube",
      "dimensions": [
        {
          "field": "status",
          "name": "hashtags",
          "type": "TwitterStatus",
          "precision": "hashtags"
        },
        {
          "field": "status",
          "name": "firsthashtag",
          "type": "TwitterStatus",
          "precision": "firsthashtag"
        },
        {
          "field": "status",
          "name": "retweets",
          "type": "TwitterStatus",
          "precision": "retweets"
        },
        {
          "name": "userLocation",
          "field": "userLocation"
        },
        {
          "field": "geolocation",
          "name": "precision3",
          "type": "GeoHash",
          "precision": "precision3"
        },
        {
          "field": "timestamp",
          "name": "minute",
          "type": "DateTime",
          "precision": "minute"
        }
      ]


The dimensions are:

- status(hashtags,firsthashtag,retweets and urls)
- userLocation
- wordsN
- timestamp
- geolocation

Some of the fields doesn't have to specify their types because it's set by default. In the others you have to specify it.

* **Third**

In this step we are going to define all the operators that we want to apply to our data::

  "operators": [
        {
          "name": "count-operator",
          "type": "Count",
          "configuration": {}
        },
        {
          "name": "sum-operator",
          "type": "Sum",
          "configuration": {
            "inputField": "wordsN"
          }
        },
        {
          "name": "max-operator",
          "type": "Max",
          "configuration": {
            "inputField": "wordsN"
          }
        },
        {
          "name": "min-operator",
          "type": "Min",
          "configuration": {
            "inputField": "wordsN"
          }
        },
        {
          "name": "avg-operator",
          "type": "Avg",
          "configuration": {
            "inputField": "wordsN"
          }
        },
        {
          "name": "fullText-operator",
          "type": "FullText",
          "configuration": {
            "inputField": "userLocation"
          }
        }
      ]
    }
  ]

In this example we are going to use sum,max,min,avg operators on WordsN.
Count operator will count the number of events that we process per minute.
FullText operator will write the location where the tweet was tweeted.

You may ask, What's WordsN?

WordsN it's defined in  |Twitterinput_scala| and it's the number of words of the tweet::

    "wordsN" -> data.getText.split(" ").size


.. |Twitterinput_scala| raw:: html

   <a href="https://github.com/Stratio/sparkta/blob/master/plugins/
   input-twitter/src/main/scala/com/stratio/sparkta/plugin/input/twitter/TwitterInput.scala"
   target="_blank">TwitterInput.scala</a>

* **Fourth**

The last step it's to declare our output database where we want to store our aggregated data.
In this example we use MongoDB as database::

  "outputs": [
     {
      "name": "out-mongo",
      "type": "MongoDb",
      "configuration": {
        "hosts": "localhost:27017",
        "dbName": "sparkta"
      }
    }
  ]

You can have more information about the policies configuration in the |doc_link|

.. |doc_link| raw:: html

   <a href="http://docs.stratio.com/modules/sparkta/development/"
   target="_blank">documentation</a>

After we had configured our policy, let's get started in the example!

Note that Zookeeper must be running::

  sudo service zookeeper start

Run Sparkta::

    cd /opt/sds/sparkta

    sudo sh bin/run &> /tmp/sparkta.out &

Now let's send the policy to sparkta::

      cd /opt/sds/sparkta

      curl -X POST -H "Content-Type: application/json" --data @examples/policies/ITwitter-OMongo-Example.json localhost:9090/policyContext

When sparkta is running it's ready to work, open your twitter account and write some tweets within a minute, since we are going to aggregate by minute(You can see the full policy |twitter_policy_link|)


.. |twitter_policy_link| raw:: html

   <a href="https://github.com/Stratio/sparkta/blob/master/examples/policies/ITwitter-OMongo-Example.json"
   target="_blank">here</a>

In this case we are using meaningless words to do the search, so we can assure that we are just processing our tweets::

  "termsOfSearch":"#hekj,prlk,#drm"

We tweeted four tweets in the same minute

.. image:: images/tweets.png
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

 precision3_firsthashtag_hashtags_retweets_minute_userLocation
 system.indexes


Enter in the collection and find the results of the operations::

 > db.precision3_firsthashtag_hashtags_retweets_minute_userLocation.find().pretty()

    {
  	 "_id" : ObjectId("5590eedca3475b6c9a0ff486"),
         "id" : "1_drm_0_madrid, comunidad de madrid_List(0.703125, 0.703125)",
 	 "minute" : ISODate("2015-06-29T07:08:00Z"),
	 "count" : NumberLong(4),
         "avg_wordsN" : 4.5,
	 "min_wordsN" : 2,
	 "fulltext_userLocation" : "madrid, comunidad de madrid madrid, comunidad de madrid madrid, comunidad de madrid madrid, comunidad de madrid",
	 "max_wordsN" : 9,
	 "sum_wordsN" : 18,
	 "median_wordsN" : 3.5
   }

Here you can see all the metrics operations that we did.

- Maximum number of words: 9
- Minimum number of words: 2
- Location of the user: Madrid
- Sum of all the words in this minute: 18
- Median of all the words: 3.5
- Average of words by tweet per minute: 4.5
- Number of tweets per minute matching our search terms("**#drm**" in this case): 4




RabbitMQ: from Twitter to MongoDB
=================================

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

    cd /opt/sds/sparkta

    sudo sh bin/run &> /tmp/sparkta.out &

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

      mvn exec:java -Dexec.mainClass="com.stratio.examples.twittertorabbit.TwitterTabbitMQWithRouting" -Dexec.args="routingKey3"

e-commerce to RabbitMQ and ElasticSearch
========================================

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
where to write and how to transform the input data.

  Note that Zookeeper must be running::

    sudo service zookeeper start

    cd /opt/sds/sparkta

    sudo sh bin/run &> /tmp/sparkta.out &

    curl -H "Content-Type: application/json" http://localhost:9090/policyContext --data @examples/data-generators/ecommerce/ecommerce-policy.json

* And last we need to run the data generators in two different shells. This generators will generate random data and
will write it into RabbitMQ. In a few seconds Sparkta will start to read the data and write it into elasticsearch::

    cd examples/data-generators/ecommerce

    mvn -PorderLines clean install benerator:generate

    mvn -PvisitLog clean install benerator:generate


