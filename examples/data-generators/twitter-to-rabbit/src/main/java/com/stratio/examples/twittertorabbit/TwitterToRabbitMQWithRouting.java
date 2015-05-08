package com.stratio.examples.twittertorabbit;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;

import java.io.IOException;

public class TwitterToRabbitMQWithRouting extends TwitterToRabbitMQ {

    private final static Logger LOG = LoggerFactory.getLogger(TwitterToRabbitMQWithRouting.class);

    /**
     * Main entry of this application.
     *
     * @param args routing key to publish in
     */
    public static void main(String[] args) {

        final String routingKey = args[0];


        ConnectionFactory factory = new ConnectionFactory();
        try {
            Connection conn = factory.newConnection();
            channel = conn.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        } catch (IOException e) {
            LOG.error("Cannot create RabbitMQ channel", e);
        }

        mapper = new ObjectMapper();

        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        StatusListener listener = new StatusListener() {

            public void onStatus(Status status) {
                try {
                    channel.basicPublish(EXCHANGE_NAME, routingKey, null, mapper.writeValueAsBytes(status));
                } catch (IOException e) {
                    LOG.error("Cannot publish status: " + status, e);
                }
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            }

            public void onScrubGeo(long userId, long upToStatusId) {
            }

            public void onStallWarning(StallWarning warning) {
            }

            public void onException(Exception ex) {
                LOG.error("Error listening on twitter stream", ex);
            }
        };
        twitterStream.addListener(listener);
        twitterStream.sample();
    }

}
