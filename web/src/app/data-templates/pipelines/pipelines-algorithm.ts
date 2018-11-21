/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { PipelineType, Engine } from '@models/enums';
import aftSurvivalRegression from './algorithm/AFTSurvivalRegression.json';
import als from './algorithm/ALS.json';
import bisectingKMeans from './algorithm/BisectingKMeans.json';
import crossValidator from './algorithm/CrossValidator.json';
import decisionTreeClassifier from './algorithm/DecisionTreeClassifier.json';
import decisionTreeRegressor from './algorithm/DecisionTreeRegressor.json';
import fpGrowth from './algorithm/FPGrowth.json';
import gaussianMixture from './algorithm/GaussianMixture.json';
import gbtClassifier from './algorithm/GBTClassifier.json';
import gbtRegressor from './algorithm/GBTRegressor.json';
import generalizedLinearRegression from './algorithm/GeneralizedLinearRegression.json';
import isotonicRegression from './algorithm/IsotonicRegression.json';
import kMeans from './algorithm/KMeans.json';
import lda from './algorithm/LDA.json';
import linearRegression from './algorithm/LinearRegression.json';
import linearSvc from './algorithm/LinearSVC.json';
import logisticRegression from './algorithm/LogisticRegression.json';
import multilayerPerceptronClassifier from './algorithm/MultilayerPerceptronClassifier.json';
import naiveBayes from './algorithm/NaiveBayes.json';
import oneVsRest from './algorithm/OneVsRest.json';
import randomForestClassifier from './algorithm/RandomForestClassifier.json';
import randomForestRegressor from './algorithm/RandomForestRegressor.json';
import trainValidationSplit from './algorithm/TrainValidationSplit.json';

export const algorithm = [
  aftSurvivalRegression,
  als,
  bisectingKMeans,
  crossValidator,
  decisionTreeClassifier,
  decisionTreeRegressor,
  fpGrowth,
  gaussianMixture,
  gbtClassifier,
  gbtRegressor,
  generalizedLinearRegression,
  isotonicRegression,
  kMeans,
  lda,
  linearRegression,
  linearSvc,
  logisticRegression,
  multilayerPerceptronClassifier,
  naiveBayes,
  oneVsRest,
  randomForestClassifier,
  randomForestRegressor,
  trainValidationSplit
];

/*********************** */

const _streamingAlgorithmNames: Array<any> = [];
const _batchAlgorithmNames: Array<any> = [];
const _streamingAlgorithmObject: any = [];
const _batchAlgorithmObject: any = [];
const _streamingAlgorithm: Array<any> = [];
const _batchAlgorithm: Array<any> = [];

algorithm.forEach((a: any) => {
  if (!a.supportedEngines) {
    return;
  }
  if (a.supportedEngines.indexOf(Engine.Batch) > -1) {
    _batchAlgorithm.push(a);
    _batchAlgorithmObject[a.classPrettyName] = a;
    _batchAlgorithmNames.push({
      name: a.classPrettyName,
      value: a,
      stepType: PipelineType.Algorithm
    });
  }
  if (a.supportedEngines.indexOf(Engine.Streaming) > -1) {
    _streamingAlgorithm.push(a);
    _streamingAlgorithmObject[a.classPrettyName] = a;
    _streamingAlgorithmNames.push({
      name: a.classPrettyName,
      value: a,
      stepType: PipelineType.Algorithm
    });
  }
});

export const streamingAlgorithm = _streamingAlgorithm;
export const batchAlgorithm = _batchAlgorithm;
export const streamingAlgorithmNames = _streamingAlgorithmNames;
export const batchAlgorithmNames = _batchAlgorithmNames;
export const streamingAlgorithmObject = _streamingAlgorithmObject;
export const batchAlgorithmObject = _batchAlgorithmObject;
