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

import * as avroTemplate from './outputs/avro.json';
//import * as cassandraTemplate from './outputs/cassandra.json';
import * as crossdataTemplate from './outputs/crossdata.json';
import * as csvTemplate from './outputs/csv.json';
import * as customTemplate from './outputs/custom.json';
import * as elasticSearchTemplate from './outputs/elastic-search.json';
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


export const outputs = [
  avroTemplate,
  crossdataTemplate,
  //cassandraTemplate,
  csvTemplate,
  customTemplate,
  elasticSearchTemplate,
  httpTemplate,
  jdbcTemplate,
  jsonTemplate,
  kafkaTemplate,
  mongodbTemplate,
  parquetTemplate,
  postgresTemplate,
  redisTemplate,
  printTemplate,
  textTemplate
];

/*********************** */

const _streamingOutputsNames: Array<any> = [];
const _batchOutputsNames: Array<any> = [];

const _streamingOutputsObject: any = [];
const _batchOutputsObject: any = [];

outputs.forEach((output: any) => {
    if (output.type && output.type === 'batch' ) {
        _batchOutputsObject[output.classPrettyName] = output;
        _batchOutputsNames.push({
            name: output.name,
            value: output,
            stepType: 'Output'
        });
    } else {
        _streamingOutputsObject[output.classPrettyName] = output;
        _streamingOutputsNames.push({
            name: output.name,
            value: output,
            stepType: 'Output'
        });
    }
});

export const streamingOutputsNames = _streamingOutputsNames;
export const batchOutputsNames = _batchOutputsNames;

export const streamingOutputsObject = _streamingOutputsObject;
export const batchOutputsObject = _batchOutputsObject;
