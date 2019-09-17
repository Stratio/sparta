/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { StepType, Engine } from '@models/enums';

import avroTemplate from './inputs/avro.json';
import binaryTemplate from './inputs/binary.json';
import cassandraBatchTemplate from './inputs/cassandraBatch.json';
import csvTemplate from './inputs/csv.json';
import elasticSearchBatchTemplate from './inputs/elasticSearchBatch.json';
import genericDatasourceBatchTemplate from './inputs/genericDatasourceBatch.json';
import jsonTemplate from './inputs/json.json';
import jdbcTemplate from './inputs/jdbc.json';
import parquetTemplate from './inputs/parquet.json';
import kafkaTemplate from './inputs/kafka.json';
import crossdataBatchTemplate from './inputs/crossdataBatch.json';
import sqlStreamingTemplate from './inputs/sqlStreaming.json';
import sqlBatchTemplate from './inputs/sqlBatch.json';
import customTemplate from './inputs/custom.json';
import customLiteTemplate from './inputs/customLite.json';
import customLiteXdTemplate from './inputs/customLiteXd.json';
import filesystemStreamingTemplate from './inputs/filesystemStreaming.json';
import filesystemBatchTemplate from './inputs/filesystemBatch.json';
//import flumeTemplate from './inputs/flume.json';
//import rabbitmqDistributedTemplate from './inputs/rabbitmq-distributed-node.json';
//import rabbitmqSingleTemplate from './inputs/rabbitmq-single-node.json';
//import socketTemplate from './inputs/socket.json';
//import twitterJsonTemplate from './inputs/twitter-json.json';
import arangoDBTemplate from './inputs/arangoInput.json';
import restBatchTemplate from './inputs/restBatch.json';
import restStreamingTemplate from './inputs/restStreaming.json';
import rowGeneratorBatchTemplate from './inputs/rowGeneratorBatch.json';
import sftpTemplate from './inputs/sftp.json';
import testStreamingTemplate from './inputs/testStreaming.json';
import testBatchTemplate from './inputs/testBatch.json';
import xmlBatchTemplate from './inputs/xml.json';
import websocketTemplate from './inputs/websocket.json';


export const inputs = [
    arangoDBTemplate,
    avroTemplate,
    binaryTemplate,
    cassandraBatchTemplate,
    crossdataBatchTemplate,
    csvTemplate,
    customTemplate,
    customLiteTemplate,
    customLiteXdTemplate,
    elasticSearchBatchTemplate,
    genericDatasourceBatchTemplate,
    jdbcTemplate,
    jsonTemplate,
    kafkaTemplate,
    filesystemBatchTemplate,
    filesystemStreamingTemplate,
    //flumeTemplate,
    //rabbitmqDistributedTemplate,
    //rabbitmqSingleTemplate,
    //socketTemplate,
    //twitterJsonTemplate,
    parquetTemplate,
    restBatchTemplate,
    restStreamingTemplate,
    rowGeneratorBatchTemplate,
    sftpTemplate,
    sqlBatchTemplate,
    sqlStreamingTemplate,
    testBatchTemplate,
    testStreamingTemplate,
    xmlBatchTemplate,
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
            name: input.classPrettyName,
            value: input,
            stepType: StepType.Input
        });
    }
    if (input.supportedEngines.indexOf(Engine.Streaming) > -1) {
        _streamingInputs.push(input);
        _streamingInputsObject[input.classPrettyName] = input;
        _streamingInputsNames.push({
            name: input.classPrettyName,
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

