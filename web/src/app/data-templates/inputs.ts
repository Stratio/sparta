///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import * as kafkaTemplate from './inputs/kafka.json';
import * as crossdataTemplate from './inputs/crossdata.json';
//import * as customTemplate from './inputs/custom.json';
import * as filesystemTemplate from './inputs/filesystem.json';
//import * as flumeTemplate from './inputs/flume.json';
//import * as rabbitmqDistributedTemplate from './inputs/rabbitmq-distributed-node.json';
//import * as rabbitmqSingleTemplate from './inputs/rabbitmq-single-node.json';
//import * as socketTemplate from './inputs/socket.json';
//import * as twitterJsonTemplate from './inputs/twitter-json.json';
import * as websocketTemplate from './inputs/websocket.json';
import * as testTemplate from './inputs/test.json';


export const inputs = [
    crossdataTemplate,
    kafkaTemplate,
    //customTemplate,
    filesystemTemplate,
    //flumeTemplate,
    //rabbitmqDistributedTemplate,
    //rabbitmqSingleTemplate,
    //socketTemplate,
    //twitterJsonTemplate,
    testTemplate,
    websocketTemplate
];

export const inputsObject: any = {};
export const inputNames = inputs.map((input: any) => {
    inputsObject[input.classPrettyName] = input;
    return {
        name: input.name,
        value: input,
        stepType: 'Input'
    };
});
