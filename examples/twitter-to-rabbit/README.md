Twitter  example
=====================

Example to take data in streaming from Twitter and ingesting it in RabbitMQ in order to test the SpaRkTA input.
To access to the Twitter API it is necessary to config the file:

    src/main/resources/twitter4j.properties

Steps
---------------------

There are two ways of testing it:

- Producing data directly into a RabbitMQ queue

    - Run the RabbitMQ and Mongodb server in local
    
        sudo service rabbitmq-server start
        
        sudo service mongod start
            
    - Run SpaRkTA and the policy
    
        cd /opt/sds/sparkta
  
        sudo sh bin/run
  
        curl -H "Content-Type: application/json" http://localhost:9090 --data 
        @examples/twitter-to-rabbit/twitter-policy.json
            
    - Run the class TwitterToRabbitMQSimple
    
        mvn clean package
        
        mvn exec:java -Dexec.mainClass="com.stratio.examples.twittertorabbit.TwitterToRabbitMQSimple"


- Producing data into a RabbitMQ queue through a direct exchange
  (https://www.rabbitmq.com/tutorials/tutorial-four-java.html)
  
    - Run the RabbitMQ and Mongodb server in local
    
        sudo service rabbitmq-server start
        
        sudo service mongod start
    
    - Run SpaRkTA and the policy
    
        cd /opt/sds/sparkta
          
        sudo sh bin/run
          
        curl -H "Content-Type: application/json" http://localhost:9090 --data 
        @examples/twitter-to-rabbit/twitter-policy.json
    
    - Run the class TwitterToRabbitMQWithRouting with the routingKey you want to write the data as argument.
    
        mvn clean package
            
        mvn exec:java -Dexec.mainClass="com.stratio.examples.twittertorabbit.TwitterToRabbitMQWithRouting"