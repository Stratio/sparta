#!/usr/bin/env bash


curl -X POST -H "Content-Type: application/json" --data @../policies/IWebSocket-OElasticsearch.json  localhost:9090/policy