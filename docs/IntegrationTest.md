#Integration Test 

When running integration test in the CI server , 
the CI server will run all docker images in the ```Jenkinsfile```  
defined at ```ITSERVICES```


###Docker Containers in local machine

####Zookeeper

```bash
docker run -d -i jplock/zookeeper:3.5.2-alpha
```

####Kafka

```bash
docker run -d -i confluent/kafka:0.10.0.0-cp1
```

####HDFS

```bash
docker run -d -i stratio/hdfs:2.6.0
```

####Posgres

```bash
docker run -d -i postgresql:9.6
```

####Elasticsearch

```bash
docker run -d -i elasticsearch/elasticsearch:5.6.2
```


