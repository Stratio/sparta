#!/usr/bin/env bash

FRAGMENT=$(curl -sX GET -H "Content-Type: application/json" localhost:9090/fragment/input/name/websocket | jq '.id')

if [ "$FRAGMENT" == "null" ]; then
  INPUT=$(curl -sX POST -H "Content-Type: application/json" --data @../policies/fragments/WebSocketFragment.json localhost:9090/fragment | jq '.id' | sed -e "s/\"//g")
else
  INPUT=$(echo "$FRAGMENT" | tr -d '"')
fi
echo $INPUT

OUTPUT=$(curl -sX POST -H "Content-Type: application/json" --data @../policies/fragments/ElasticSearchFragment.json localhost:9090/fragment | jq '.id' | sed -e "s/\"//g")
echo $OUTPUT

cat ../policies/fragments/IWebSocket-OElasticsearch.json|sed -e "s/_input_id_/$INPUT/g"|sed -e "s/_output_id_/$OUTPUT/g" >temp.json

POL=$(curl -X POST -H "Content-Type: application/json" --data @./temp.json  localhost:9090/policy |jq '.id'| sed -e "s/\"//g")
echo $POL