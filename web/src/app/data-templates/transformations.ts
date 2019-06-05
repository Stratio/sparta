/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { StepType, Engine } from '@models/enums';

import addColumnTemplate from './transformations/addColumns.json';
import avroTemplate from './transformations/avro.json';
import basicDataQualityTemplate from './transformations/basicDataQuality.json';
import caseLetterTemplate from './transformations/caseLetter.json';
import caseTemplate from './transformations/case.json';
import castingTemplate from './transformations/casting.json';
import catalogTemplate from './transformations/catalog.json';
import characterTrimmerTemplate from './transformations/characterTrimmer.json';
import checkpointTemplate from './transformations/checkpoint.json';
import coalesceTemplate from './transformations/coalesce.json';
import commaDelimiterTemplate from './transformations/commaDelimiter.json';
import csvTemplate from './transformations/csv.json';
import cubeTemplate from './transformations/cube.json';
import cubeBatchTemplate from './transformations/cubeBatch.json';
import customTemplate from './transformations/custom.json';
import customLiteTemplate from './transformations/customLite.json';
import customLiteXdTemplate from './transformations/customLiteXd.json';
import dataProfilingTemplate from './transformations/dataProfiling.json';
import datetimeTemplate from './transformations/datetime.json';
import distinctTemplate from './transformations/distinct.json';
import dropColumnsTemplate from './transformations/dropColumns.json';
import dropDuplicatesTemplate from './transformations/dropDuplicates.json';
import dropNullsTemplate from './transformations/dropNulls.json';
import duplicateColumnsTemplate from './transformations/duplicateColumns.json';
import explodeTemplate from './transformations/explode.json';
import filterTemplate from './transformations/filter.json';
import formatterTemplate from './transformations/formatter.json';
import hashTemplate from './transformations/hash.json';
import initNullsTemplate from './transformations/initNulls.json';
import insertLiteralTemplate from './transformations/insertLiteral.json';
import integrityTemplate from './transformations/integrity.json';
import intersectionTemplate from './transformations/intersection.json';
import joinTemplate from './transformations/join.json';
import jsonPathTemplate from './transformations/jsonpath.json';
import jsonTemplate from './transformations/json.json';
import leftPaddingTemplate from './transformations/leftPadding.json';
import mlModelTemplate from './transformations/mlModel.json';
import orderByTemplate from './transformations/orderBy.json';
import persistTemplate from './transformations/persist.json';
import pivotTemplate from './transformations/pivot.json';
import queryBuilderTemplate from './transformations/queryBuilder.json';
import renameColumnTemplate from './transformations/renamecolumn.json';
import repartitionTemplate from './transformations/repartition.json';
import restTemplate from './transformations/rest.json';
import selectTemplate from './transformations/select.json';
import splitTemplate from './transformations/split.json';
import triggerStreamingTemplate from './transformations/triggerStreaming.json';
import triggerBatchTemplate from './transformations/triggerBatch.json';
import unionTemplate from './transformations/union.json';
import windowTemplate from './transformations/window.json';


export const transformations: any = [
    addColumnTemplate,
    avroTemplate,
    basicDataQualityTemplate,
    caseTemplate,
    caseLetterTemplate,
    castingTemplate,
    catalogTemplate,
    characterTrimmerTemplate,
    checkpointTemplate,
    coalesceTemplate,
    commaDelimiterTemplate,
    csvTemplate,
    cubeTemplate,
    cubeBatchTemplate,
    customTemplate,
    customLiteTemplate,
    customLiteXdTemplate,
    dataProfilingTemplate,
    datetimeTemplate,
    distinctTemplate,
    dropColumnsTemplate,
    dropDuplicatesTemplate,
    dropNullsTemplate,
    duplicateColumnsTemplate,
    explodeTemplate,
    filterTemplate,
    formatterTemplate,
    hashTemplate,
    initNullsTemplate,
    insertLiteralTemplate,
    integrityTemplate,
    intersectionTemplate,
    joinTemplate,
    jsonPathTemplate,
    jsonTemplate,
    leftPaddingTemplate,
    mlModelTemplate,
    orderByTemplate,
    persistTemplate,
    pivotTemplate,
    queryBuilderTemplate,
    renameColumnTemplate,
    repartitionTemplate,
    restTemplate,
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
            name: transformation.classPrettyName,
            value: transformation,
            stepType: StepType.Transformation
        });
    }
    if (transformation.supportedEngines.indexOf(Engine.Streaming) > -1) {
        _streamingTransformations.push(transformation);
        _streamingTransformationsObject[transformation.classPrettyName] = transformation;
        _streamingTransformationsNames.push({
            name: transformation.classPrettyName,
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
