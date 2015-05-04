/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.examples.datagenerator.producers;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.stratio.examples.datagenerator.events.Event;

import java.io.IOException;

public abstract class Producer {

    protected final static String EXCHANGE_NAME = "logsExchange";
    protected static final int NUM_BATCH_MESSAGES = 100;
    protected int numMessage = 0;
    protected ConnectionFactory factory = new ConnectionFactory();
    protected Connection connection;
    protected Channel channel;

    public Producer(String host, Integer port) {
        factory.setVirtualHost("/");
        factory.setHost(host);
        factory.setPort(port);
    }

    public void write(Object[] args) throws IOException {

        Event event = getInstance(args);

        if (numMessage == 0) {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        }

        channel.basicPublish(EXCHANGE_NAME, getRoutingKey(),
                null, event.toJsonOutput().getBytes());

        if (numMessage == NUM_BATCH_MESSAGES) {
            channel.close();
            connection.close();
            numMessage = 0;
        } else {
            numMessage++;
        }
    }

    protected abstract Event getInstance(Object[] args);

    protected abstract String getRoutingKey();
}
