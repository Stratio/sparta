#!/usr/bin/env bash

INPUT=`curl -sX POST -H "Content-Type: application/json" --data @../policies/fragments/WebSocketFragment.json localhost:9090/fragment | jq '.id' | sed -e "s/\"//g"`
echo $INPUT
OUTPUT=`curl -sX POST -H "Content-Type: application/json" --data @../policies/fragments/MongoFragment.json localhost:9090/fragment | jq '.id' | sed -e "s/\"//g"`
echo $OUTPUT
cat ../policies/fragments/IWebSocket-OMongo.json|sed -e "s/_input_id_/$INPUT/g"|sed -e "s/_output_id_/$OUTPUT/g" >temp.json

curl -X POST -H "Content-Type: application/json" --data @./temp.json  localhost:9090/policy


echo $POL