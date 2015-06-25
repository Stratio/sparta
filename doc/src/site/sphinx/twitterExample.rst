
Twitter examples
****************

Twitter
=================================

In this example we are going to show one of the most interesting inputs right now.
Let's explain what the example is going to do:


* Get data that we want to work with, specifying it in the policy.
* Aggregate the data based on the policy parameters.
* Apply operations such as counters,max,min,averages and many others.
* Save the data in MongoDB, where we can see the results of the operations

Now let's get started on how to do it, without touching any line of code

Steps

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

 In order to get the keys you will have to register in `Twitter developer web site <https://apps.twitter.com/>`__

 The new feature that we included in the twitter input it's the parameter **termsOfSearch**, it allows you
 to search tweets based on the words you put in. They could be single words or hashtags.
 If the program find one of the words the tweet will be sent to be processed.

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

- status(hashtags,firsthashtag,retweets and urls) //TODO Explain all of them
- userLocation
- wordsN
- timestamp
- geolocation

Some of the fields doesn't have to specify the type of the fields because it's set by default
in the others you have to specify it.

* **Operations**

In this step we are going to apply all the operators that we defined in the policy::

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

In this example we are going to use
* You can have more information about the policies configuration in the documentation(LINK)