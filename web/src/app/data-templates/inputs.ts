/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { StepType, Engine } from "@models/enums";

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
    if (input.supportedEngines.indexOf(Engine.Batch) > -1) {
        _batchInputs.push(input);
        _batchInputsObject[input.classPrettyName] = input;
        _batchInputsNames.push({
            name: input.name,
            value: input,
            stepType: StepType.Input
        });
    }
    if (input.supportedEngines.indexOf(Engine.Streaming) > -1) {
        _streamingInputs.push(input);
        _streamingInputsObject[input.classPrettyName] = input;
        _streamingInputsNames.push({
            name: input.name,
            value: input,
            stepType: StepType.Input
        });
    }
});

export const streamingInputs = _streamingInputs;
export const batchInputs = _batchInputs;
export const streamingInputsNames = _streamingInputsNames;
export const batchInputsNames = _batchInputsNames;
export const streamingInputsObject = _streamingInputsObject;
export const batchInputsObject = _batchInputsObject;

