#!/usr/bin/env bash


curl -X POST -H "Content-Type: application/json" --data @../policies/IWebSocket-OMongo.json  localhost:9090/policy