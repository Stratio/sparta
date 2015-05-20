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

package com.stratio.sparkta.benchmark;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Date;
import java.util.Properties;


public class KafkaProducer {

    static final String FIXED_SUFFIX = ",\"url\":\"www.example.com\",\"ip\":\"192.168.2.2\"}";
    static final long EVENTS=20000000L;

    public static void main(String[] args) {

        Properties props = getKafkaProperties();
        ProducerConfig config = new ProducerConfig(props);
        Producer<String, String> producer = new Producer<String, String>(config);

        for (long nEvents = 0; nEvents < EVENTS; nEvents++) {

            KeyedMessage<String, String> data = getMsg(nEvents);
            producer.send(data);
        }

        producer.close();
        System.out.println("---inserted events: " + EVENTS);
    }

    private static KeyedMessage<String, String> getMsg(long nEvents) {
        long runtime = new Date().getTime();
        StringBuffer msg = new StringBuffer("{\"timestamp\":\"").append(runtime).append("\"").append(FIXED_SUFFIX);
        KeyedMessage<String, String> data = new KeyedMessage<String, String>("page_visits", String.valueOf(runtime), msg.toString());
        if (isTimeToLog(nEvents))
            printLog(nEvents, msg);
        return data;
    }

    private static Properties getKafkaProperties() {
        Properties props = new Properties();
        props.put("metadata.broker.list", "localhost:9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("partitioner.class", "com.stratio.sparkta.benchmark.SimplePartitioner");
        props.put("request.required.acks", "1");
        return props;
    }

    private static void printLog(long nEvents, StringBuffer msg) {
        System.out.println("inserting---> " + nEvents + msg);
    }

    private static boolean isTimeToLog(long nEvents) {
        return nEvents % 10000 == 0;
    }
}
