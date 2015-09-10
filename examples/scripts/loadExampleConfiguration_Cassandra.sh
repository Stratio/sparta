#!/usr/bin/env bash

INPUT=`curl -sX POST -H "Content-Type: application/json" --data @../policies/fragments/WebSocketFragment.json localhost:9090/fragment | jq '.id' | sed -e "s/\"//g"`
echo $INPUT
OUTPUT=`curl -sX POST -H "Content-Type: application/json" --data @../policies/fragments/CassandraFragment.json localhost:9090/fragment | jq '.id' | sed -e "s/\"//g"`
echo $OUTPUT
cat ../policies/fragments/IWebSocket-OCassandra.json|sed -e "s/_input_id_/$INPUT/g"|sed -e "s/_output_id_/$OUTPUT/g" >temp.json

POL=`curl -sX POST -H "Content-Type: application/json" --data @./temp.json  localhost:9090/policy |jq '.id'| sed -e "s/\"//g"`

echo $POL