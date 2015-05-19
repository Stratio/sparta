Stratio-Sparkta Benchmark
==========
Benchmark -> kafka live-stream over MongoDB
---------
### Introduction

We want to see the performance of SpaRkTA whit a live stream, then with this benchmark we can create a
continous flow, the script inserts 2 millon of events in apache-kafka while SpaRkTA aggregates
and stores the data by second in MongoDB

### Prerequisites

   + Java
   + Maven
   + Kafka
   + MongoDB
    
### Steps

   1. Run zookeeper, kafka release has their own zookeeper

         $KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookper.properties
         
   2. Start kafka server
        
        $KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookper.properties
   3.  Create the test topic "page_visits"

        $KAFKA_HOME/bin/kafka-topics.sh --create  --zookeeper localhost:2181 --topic page_visits --partitions 5 --replication-factor 1
   4. Start the SpaRkTA service

        $KAFKA_HOME/bin/kafka-topics.sh --create  --zookeeper localhost:2181 --topic page_visits --partitions 5 --replication-factor 1
   5. Send the policy json to SpaRkTA
        
        curl -X POST -H "Content-Type: application/json" --data @src/main/resources/benchmark_policy.json  localhost:9090/policy
   6. Run the data generator script
        
        mvn compile
   7. Get the result from mongo
        
        mongo
        use sparkta
        db.ip_second_url.find().sort({count: -1})

    
    
        