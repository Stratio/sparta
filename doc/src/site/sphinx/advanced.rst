Advanced Documentation
**********************
 
Zookeeper
=========

One of the necessities of Sparkta is to keep track of all policies running and their status. Since Zookeeper is a
Sparkta dependency and a common piece in any Big Data architecture we save all this metadata in Zookeeper nodes.

All this info is saved under the znode `/stratio/sparkta`. Under this root we save input and output fragments,
policies and policy statuses.

Fragments are JSON blocks of inputs/outputs that will be included in a policy. If one fragment is changed, all policies that had included it, will be automatically changed too. In fact, it is a nice way to reuse inputs/outputs between policies. An example of an input fragment::

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

Another useful information we save is the policy status. We save the current status of a policy. This status is
persisted in path `/stratio/sparkta/contexts`
