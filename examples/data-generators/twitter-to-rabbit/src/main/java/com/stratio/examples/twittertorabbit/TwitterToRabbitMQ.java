package com.stratio.examples.twittertorabbit;

import com.rabbitmq.client.Channel;
import org.codehaus.jackson.map.ObjectMapper;

public abstract class TwitterToRabbitMQ {
    protected static final String EXCHANGE_NAME = "twitterExchange";
    protected static final String HOST = "localhost";

    protected static Channel channel;
    protected static ObjectMapper mapper;
}
