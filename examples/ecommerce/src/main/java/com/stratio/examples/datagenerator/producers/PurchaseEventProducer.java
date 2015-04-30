package com.stratio.examples.datagenerator.producers;

import com.stratio.examples.datagenerator.events.Event;
import com.stratio.examples.datagenerator.events.PurchaseEvent;

import java.io.IOException;

public class PurchaseEventProducer extends Producer {

    private final static String ROUTING_KEY = "purchasesRoute";

    public PurchaseEventProducer(String host, Integer port) throws IOException {
        super(host, port);
    }

    @Override
    protected Event getInstance(Object[] args) {
        return PurchaseEvent.getInstance(args);
    }

    @Override
    protected String getRoutingKey() {
        return ROUTING_KEY;
    }
}
