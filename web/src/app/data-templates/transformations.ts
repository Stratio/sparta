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

import * as avroTemplate from './transformations/avro.json';
import * as castingTemplate from './transformations/casting.json';
import * as checkpointTemplate from './transformations/checkpoint.json';
import * as csvTemplate from './transformations/csv.json';
import * as cubeTemplate from './transformations/cube.json';
import * as customTemplate from './transformations/custom.json';
import * as datetimeTemplate from './transformations/datetime.json';
import * as explodeTemplate from './transformations/explode.json';
import * as distinctTemplate from './transformations/distinct.json';
import * as filterTemplate from './transformations/filter.json';
import * as intersectionTemplate from './transformations/intersection.json';
import * as jsonPathTemplate from './transformations/jsonpath.json';
import * as jsonTemplate from './transformations/json.json';
import * as orderByTemplate from './transformations/orderBy.json';
import * as persistTemplate from './transformations/persist.json';
import * as repartitionTemplate from './transformations/repartition.json';
import * as selectTemplate from './transformations/select.json';
import * as splitTemplate from './transformations/split.json';
import * as triggerTemplate from './transformations/trigger.json';
import * as unionTemplate from './transformations/union.json';
import * as windowTemplate from './transformations/window.json';

export const transformations: any = [
    avroTemplate,
    castingTemplate,
    checkpointTemplate,
    csvTemplate,
    cubeTemplate,
    customTemplate,
    datetimeTemplate,
    distinctTemplate,
    explodeTemplate,
    filterTemplate,
    intersectionTemplate,
    jsonPathTemplate,
    jsonTemplate,
    orderByTemplate,
    persistTemplate,
    repartitionTemplate,
    selectTemplate,
    splitTemplate,
    triggerTemplate,
    unionTemplate,
    windowTemplate
];

/*********************** */

const _streamingTransformations: Array<any> = [];
const _batchTransformations: Array<any> = [];
const _streamingTransformationsNames: Array<any> = [];
const _batchTransformationsNames: Array<any> = [];
const _streamingTransformationsObject: any = [];
const _batchTransformationsObject: any = [];

transformations.forEach((transformation: any) => {
    if (!transformation.supportedEngines) {
        return;
    }
    if (transformation.supportedEngines.indexOf('Batch') > -1) {
        _batchTransformations.push(transformation);
        _batchTransformationsObject[transformation.classPrettyName] = transformation;
        _batchTransformationsNames.push({
            name: transformation.name,
            value: transformation,
            stepType: 'Transformation'
        });
    }
    if (transformation.supportedEngines.indexOf('Streaming') > -1) {
        _streamingTransformations.push(transformation);
        _streamingTransformationsObject[transformation.classPrettyName] = transformation;
        _streamingTransformationsNames.push({
            name: transformation.name,
            value: transformation,
            stepType: 'Transformation'
        });
    }
});

export const streamingTransformations = _streamingTransformations;
export const batchTransformations = _batchTransformations;
export const streamingTransformationsNames = _streamingTransformationsNames;
export const batchTransformationsNames = _batchTransformationsNames;
export const streamingTransformationsObject = _streamingTransformationsObject;
export const batchTransformationsObject = _batchTransformationsObject;
