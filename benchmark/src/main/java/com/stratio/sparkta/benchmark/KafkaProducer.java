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
import java.util.Random;


public class KafkaProducer {

    public static void main(String[] args) {
        long events = Long.parseLong("20000000");
        Random rnd = new Random();

        Properties props = new Properties();

        props.put("metadata.broker.list", "localhost:9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("partitioner.class", "com.stratio.sparkta.benchmark.SimplePartitioner");
        props.put("request.required.acks", "1");

        ProducerConfig config = new ProducerConfig(props);
        Producer<String, String> producer = new Producer<String, String>(config);
        String ip = "192.168.2.2";
        for (long nEvents = 0; nEvents < events; nEvents++) {
            long runtime = new Date().getTime();

            String msg ="{" +
                        "\"timestamp\":\""+runtime+"\""+
                        ",\"url\":\"www.example.com\","+
                        "\"ip\":\""+ip+"\""+
                    "}" ;
            KeyedMessage<String, String> data = new KeyedMessage<String, String>("page_visits", ip, msg);
            producer.send(data);
            if(nEvents%10000==0) System.out.println("inserting--->"+nEvents+msg);
        }
        producer.close();
        System.out.println("---inserted events:"+events);
    }
}
