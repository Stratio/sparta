#!/usr/bin/env bash
#Case 1
KAFKA_HOME=/home/arincon/apps/kafka_2.10-0.8.2.0
echo $KAFKA_HOME
#start mongoDb
sudo service mongod start
mongoPid=$!
#clean mongoDb
mongo sparkta benchmark/src/main/js/mongoClean.js
#start zookeeper
nohup $KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties & > zookeeper.log
zookeeperPid=$!
echo $zookeeperPid
sleep 1
#start kafka
nohup $KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties & > kafka.log
kafkaPid=$!
echo  $kafkaPid
#start spakta
nohup bin/run & > sparkta.log
sparktaPid= $!
echo  $sparktaPid
sleep  5
#send the policy
curl -X POST -H "Content-Type: application/json" --data @benchmark/src/main/resources/benchmark_policy.json  localhost:9090/policy
sleep 30
#start the kafka feeder
java -jar benchmark/target/sparkta-benchmark-kafka-producer-0.1.0-SNAPSHOT.one-jar.jar localhost:2181
benchPid=$!
#wait during the test
echo "wait for the test about 15 minutes"
sleep 15 m
#make report
echo "max events aggregated by second ->"
mongo sparkta benchmark/src/main/js/mongoReport.js
echo "pids" $zookeeperPid $kafkaPid $sparktaPid

#stop all
kill -9 $zookeeperPid $kafkaPid $sparktaPid

echo "mongo is up if you want to see the aggregated data in detail MongoDB PID ->"$mongoPid
