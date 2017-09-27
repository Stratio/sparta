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
import * as cassandraTemplate from './outputs/cassandra.json';
import * as crossdataTemplate from './outputs/crossdata.json';
import * as csvTemplate from './outputs/csv.json';
import * as customTemplate from './outputs/custom.json';
import * as elasticSearchTemplate from './outputs/elastic-search.json';
import * as filesystemTemplate from './outputs/filesystem.json';
import * as httpTemplate from './outputs/http.json';
import * as jdbcTemplate from './outputs/jdbc.json';
import * as kafkaTemplate from './outputs/kafka.json';
import * as mongodbTemplate from './outputs/mongodb.json';
import * as parquetTemplate from './outputs/parquet.json';
import * as postgressTemplate from './outputs/postgress.json';
import * as printTemplate from './outputs/print.json';
import * as redisTemplate from './outputs/redis.json';


export const outputs = [
  crossdataTemplate,
  avroTemplate,
  cassandraTemplate,
  csvTemplate,
  customTemplate,
  elasticSearchTemplate,
  filesystemTemplate,
  httpTemplate,
  jdbcTemplate,
  kafkaTemplate,
  mongodbTemplate,
  parquetTemplate,
  postgressTemplate,
  printTemplate,
  redisTemplate
];

export const outputNames = outputs.map((output: any) => {
  return {
        name: output.name,
        value: output,
        stepType: 'Output'
  };
});
