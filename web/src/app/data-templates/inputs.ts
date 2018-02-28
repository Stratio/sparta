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
import * as crossdataStreamingTemplate from './inputs/crossdataStreaming.json';
import * as crossdataBatchTemplate from './inputs/crossdataBatch.json';
import * as customTemplate from './inputs/custom.json';
import * as filesystemStreamingTemplate from './inputs/filesystemStreaming.json';
import * as filesystemBatchTemplate from './inputs/filesystemBatch.json';
//import * as flumeTemplate from './inputs/flume.json';
//import * as rabbitmqDistributedTemplate from './inputs/rabbitmq-distributed-node.json';
//import * as rabbitmqSingleTemplate from './inputs/rabbitmq-single-node.json';
//import * as socketTemplate from './inputs/socket.json';
//import * as twitterJsonTemplate from './inputs/twitter-json.json';
import * as testStreamingTemplate from './inputs/testStreaming.json';
import * as testBatchTemplate from './inputs/testBatch.json';
import * as websocketTemplate from './inputs/websocket.json';


export const inputs = [
    crossdataStreamingTemplate,
    crossdataBatchTemplate,
    customTemplate,
    kafkaTemplate,
    filesystemBatchTemplate,
    filesystemStreamingTemplate,
    //flumeTemplate,
    //rabbitmqDistributedTemplate,
    //rabbitmqSingleTemplate,
    //socketTemplate,
    //twitterJsonTemplate,
    testBatchTemplate,
    testStreamingTemplate,
    websocketTemplate
];


/*********************** */

const _streamingInputs: Array<any> = [];
const _batchInputs: Array<any> = [];
const _streamingInputsNames: Array<any> = [];
const _batchInputsNames: Array<any> = [];
const _streamingInputsObject: any = [];
const _batchInputsObject: any = [];

inputs.forEach((input: any) => {
    if (!input.supportedEngines) {
        return;
    }
    if (input.supportedEngines.indexOf('Batch') > -1) {
        _batchInputs.push(input);
        _batchInputsObject[input.classPrettyName] = input;
        _batchInputsNames.push({
            name: input.name,
            value: input,
            stepType: 'Input'
        });
    }
    if (input.supportedEngines.indexOf('Streaming') > -1) {
        _streamingInputs.push(input);
        _streamingInputsObject[input.classPrettyName] = input;
        _streamingInputsNames.push({
            name: input.name,
            value: input,
            stepType: 'Input'
        });
    }
});

export const streamingInputs = _streamingInputs;
export const batchInputs = _batchInputs;
export const streamingInputsNames = _streamingInputsNames;
export const batchInputsNames = _batchInputsNames;
export const streamingInputsObject = _streamingInputsObject;
export const batchInputsObject = _batchInputsObject;

