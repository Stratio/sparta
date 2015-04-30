package com.stratio.examples.datagenerator.producers;

import com.stratio.examples.datagenerator.events.Event;
import com.stratio.examples.datagenerator.events.VisitLogEvent;

import java.io.IOException;

public class VisitLogProducer extends Producer{

    private final static String ROUTING_KEY = "webLogsRoute";

    public VisitLogProducer(String host, Integer port) throws IOException {
        super(host, port);
    }

    @Override
    protected Event getInstance(Object[] args) {
        return VisitLogEvent.getInstance(args);
    }

    @Override
    protected String getRoutingKey() {
        return ROUTING_KEY;
    }
}
