Advanced Documentation
**********************

Zookeeper
=========

One of the necessities of Sparkta is to keep track of all policies running and their status. Since Zookeeper is a
Sparkta dependency and a common piece in any Big Data architecture we save all this metadata in Zookeeper nodes.

All this info is saved under the znode `/stratio/sparkta`. Under this root we save input and output fragments,
policies and policy statuses.

Fragments are the JSON blocks of and input or an output. We use them for reusing them when managing policies with the
user interface. An example of an input fragment::

  {
    "fragmentType": "input",
    "name": "twitter",
    "description": "twitter input",
    "shortDescription": "twitter input",
    "icon": "icon.png",
    "element": {
      "name": "in-twitter",
      "type": "Twitter",
      "configuration": {
        "consumerKey": "*",
        "consumerSecret": "*",
        "accessToken": "*",
        "accessTokenSecret": "*"
      }
    }
  }

This fragments are saved in Zookeeper in paths `/stratio/sparkta/fragments/input` and
`/stratio/sparkta/fragments/output`

Policies are saved in path `/stratio/sparkta/policies`.

Another useful information we saved is the policy statuses. We save the current status of a policy. This status is
persisted in path `/stratio/sparkta/contexts`
