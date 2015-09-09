#!/usr/bin/env bash


curl -X POST -H "Content-Type: application/json" --data @../policies/IWebSocket-OCassandra.json  localhost:9090/policy