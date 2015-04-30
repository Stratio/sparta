#!/usr/bin/env bash

mvn -PorderLines clean install benerator:generate &
mvn -PvisitLog clean install benerator:generate &