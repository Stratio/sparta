
Twitter examples
****************

Twitter
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

     "inputs": [
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

The new feature that we have included in the twitter input it's the parameter **termsOfSearch**, it allows you
to search tweets based on the words you specify on it. They could be single words or hashtags.
If the program find one of the words, the tweet will be sent to be processed.

* **Second**

Then we have to choose which fields we are going to consider relevant::

  "fields": [
    {
      "type": "TwitterStatus",
      "name": "status",
      "configuration": {
        "typeOp": "string",
        "hashtags": "int",
        "firsthashtag": "string",
        "retweets": "int",
        "urls": "int"
      }
    },
    {
      "name": "userLocation"
    },
    {
      "name": "wordsN"
    },
    {
      "type": "DateTime",
      "name": "timestamp"
    },
    {
      "type": "GeoHash",
      "name": "geolocation",
      "configuration": {
        "typeOp": "arraydouble"
      }
    }
  ]


The fields are:

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
          "measureName": "count-operator",
          "type": "Count",
          "configuration": {}
        },
        {
          "measureName": "sum-operator",
          "type": "Sum",
          "configuration": {
            "inputField": "wordsN"
          }
        },
        {
          "measureName": "max-operator",
          "type": "Max",
          "configuration": {
            "inputField": "wordsN"
          }
        },
        {
          "measureName": "min-operator",
          "type": "Min",
          "configuration": {
            "inputField": "wordsN"
          }
        },
        {
          "measureName": "avg-operator",
          "type": "Avg",
          "configuration": {
            "inputField": "wordsN"
          }
        },
        {
          "measureName": "fullText-operator",
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

WordsN it's defined in the |Twitterinput_scala| and it's the number of words of the tweet::

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
        "clientUri": "mongodb://localhost:27017",
        "dbName": "sparkta"
      }
    }
  ]

You can have more information about the policies configuration in the |doc_link|

.. |doc_link| raw:: html

   <a href="http://docs.stratio.com/modules/sparkta/development/"
   target="_blank">documentation</a>

After we had configured our policy, let's get started in the example!


Run Sparkta::

    cd /opt/sds/sparkta

    sudo sh bin/run


Now it's the time to decide if we want to custom our twitter search with our own terms or
if we want the global trending topic at the moment.
As we explained, if in the input you add::

 "termsOfSearch":"Your,#search,#could,be,whatever"


It will be a custom search, if you want the other choice(global trending topics) just delete the whole line, and the
policy will look like this::

 "inputs": [
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

Now let's send the policy to sparkta::

      curl -H "Content-Type: application/json" http://localhost:9090 --data @examples/data-generators/twitter/ITwitter-OMongo.json

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


