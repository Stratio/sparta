/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { StepType, Engine } from '@models/enums';

import * as avroTemplate from './outputs/avro.json';
import * as cassandraTemplate from './outputs/cassandra.json';
import * as crossdataTemplate from './outputs/crossdata.json';
import * as csvTemplate from './outputs/csv.json';
import * as customTemplate from './outputs/custom.json';
import * as customLiteTemplate from './outputs/customLite.json';
import * as customLiteXdTemplate from './outputs/customLiteXd.json';
import * as elasticSearchTemplate from './outputs/elastic-search.json';
import * as genericDatasourceTemplate from './outputs/genericDatasource.json';
import * as httpTemplate from './outputs/http.json';
import * as jdbcTemplate from './outputs/jdbc.json';
import * as jsonTemplate from './outputs/json.json';
import * as kafkaTemplate from './outputs/kafka.json';
import * as mongodbTemplate from './outputs/mongodb.json';
import * as parquetTemplate from './outputs/parquet.json';
import * as postgresTemplate from './outputs/postgres.json';
import * as printTemplate from './outputs/print.json';
import * as redisTemplate from './outputs/redis.json';
import * as textTemplate from './outputs/text.json';
import * as xmlTemplate from './outputs/xml.json';


export const outputs = [
    avroTemplate,
    cassandraTemplate,
    crossdataTemplate,
    csvTemplate,
    customTemplate,
    customLiteTemplate,
    customLiteXdTemplate,
    elasticSearchTemplate,
    genericDatasourceTemplate,
    httpTemplate,
    jdbcTemplate,
    jsonTemplate,
    kafkaTemplate,
    mongodbTemplate,
    parquetTemplate,
    postgresTemplate,
    redisTemplate,
    printTemplate,
    textTemplate,
    xmlTemplate
];

/*********************** */

const _streamingOutputsNames: Array<any> = [];
const _batchOutputsNames: Array<any> = [];
const _streamingOutputsObject: any = [];
const _batchOutputsObject: any = [];
const _streamingOutputs: Array<any> = [];
const _batchOutputs: Array<any> = [];

outputs.forEach((output: any) => {
    if (!output.supportedEngines) {
        return;
    }
    if (output.supportedEngines.indexOf(Engine.Batch) > -1) {
        _batchOutputs.push(output);
        _batchOutputsObject[output.classPrettyName] = output;
        _batchOutputsNames.push({
            name: output.classPrettyName,
            value: output,
            stepType: StepType.Output
        });
    }
    if (output.supportedEngines.indexOf(Engine.Streaming) > -1) {
        _streamingOutputs.push(output);
        _streamingOutputsObject[output.classPrettyName] = output;
        _streamingOutputsNames.push({
            name: output.classPrettyName,
            value: output,
            stepType: StepType.Output
        });
    }
});

export const streamingOutputs = _streamingOutputs;
export const batchOutputs = _batchOutputs;
export const streamingOutputsNames = _streamingOutputsNames;
export const batchOutputsNames = _batchOutputsNames;
export const streamingOutputsObject = _streamingOutputsObject;
export const batchOutputsObject = _batchOutputsObject;
