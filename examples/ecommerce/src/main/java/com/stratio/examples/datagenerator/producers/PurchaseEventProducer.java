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
