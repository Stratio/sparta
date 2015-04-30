Example to take data in streaming from Twitter and ingesting it in RabbitMQ in order to test the SpaRkTA input.

There are two ways of testing it:
- Producing data directly into a RabbitMQ queue
    - Run SpaRkTA and the policy
    - Run the RabbitMQ server in local
    - Run the class TwitterToRabbitMQSimple

- Producing data into a RabbitMQ queue through a direct exchange
  (https://www.rabbitmq.com/tutorials/tutorial-four-java.html)
    - Run SpaRkTA and the policy
    - Run the RabbitMQ server in local
    - Run the class TwitterToRabbitMQWithRouting with the routingKey you want to write         the data as argument.