Fragment(s)
-----------

For convenience, it is possible to have an alias about input[s]/output[s] in your policy. These alias are fragments that
will be included in your policy when the policy has been run.

Fragments have an API Rest to perform CRUD operations over them. For more information you can read documentation about
it querying Swagger:
::
    http://<host>:<port>/swagger#!/fragment

Example:

Let's imagine that you want to use a Twitter's input in some policies but you do not want to write over and over this
"fragment" in each policy that you made.
::
    {
      "type": "input",
      "name": "twitter",
      "element": {
        "name": "in-twitter",
        "type": "TwitterInput",
        "configuration": {
          "consumerKey": "*****",
          "consumerSecret": "*****",
          "accessToken": "*****",
          "accessTokenSecret": "*****"
        }
      }
    }

Then you can save this fragment in Sparkta:
::
    curl -X POST -H "Content-Type: application/json" --data @examples/policiesfragments/twitterExample.json localhost:9090/fragment

Now you can include this fragment in every policy that has Twitter as input in a simple and comprehensible way:
::
    "fragments": [
    {
      "name": "twitter",
      "fragmentType": "input",
    }
  ]

You can include as many fragments as you need. Easy, Right?