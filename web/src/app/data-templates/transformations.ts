/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { StepType, Engine } from "@models/enums";

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
import * as triggerStreamingTemplate from './transformations/triggerStreaming.json';
import * as triggerBatchTemplate from './transformations/triggerBatch.json';
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
    triggerStreamingTemplate,
    triggerBatchTemplate,
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
    if (transformation.supportedEngines.indexOf(Engine.Batch) > -1) {
        _batchTransformations.push(transformation);
        _batchTransformationsObject[transformation.classPrettyName] = transformation;
        _batchTransformationsNames.push({
            name: transformation.name,
            value: transformation,
            stepType: StepType.Transformation
        });
    }
    if (transformation.supportedEngines.indexOf(Engine.Streaming) > -1) {
        _streamingTransformations.push(transformation);
        _streamingTransformationsObject[transformation.classPrettyName] = transformation;
        _streamingTransformationsNames.push({
            name: transformation.name,
            value: transformation,
            stepType: StepType.Transformation
        });
    }
});

export const streamingTransformations = _streamingTransformations;
export const batchTransformations = _batchTransformations;
export const streamingTransformationsNames = _streamingTransformationsNames;
export const batchTransformationsNames = _batchTransformationsNames;
export const streamingTransformationsObject = _streamingTransformationsObject;
export const batchTransformationsObject = _batchTransformationsObject;
