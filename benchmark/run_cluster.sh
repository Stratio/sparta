#!/usr/bin/env bash
#Case 1

#clean mongoDb
mongo sparkta benchmark/src/main/js/mongoClean.js
#start spakta
nohup bin/run & > sparkta.log
sparktaPid= $!
echo  $sparktaPid
sleep  5
#send the policy
curl -X POST -H "Content-Type: application/json" --data @benchmark/src/main/resources/benchmark_caffeine_policy.json  localhost:9090/policy
sleep 30
#start the kafka feeder
java -jar benchmark/target/sparkta-benchmark-kafka-producer-0.1.0-SNAPSHOT.one-jar.jar cafeina1-10G:9092,cafeina2-10G:9092,cafeina3-10G:9092
benchPid=$!
#wait during the test
echo "wait for the test about 15 minutes"
sleep 15 m
#make report
echo "max events aggregated by second ->"
mongo sparkta benchmark/src/main/js/mongoReport.js
echo "pids" $sparktaPid

#stop all
kill -9  $sparktaPid


