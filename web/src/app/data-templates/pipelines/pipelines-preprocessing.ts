/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { PipelineType, Engine } from '@models/enums';

import binarizer from 'data-templates/pipelines/preprocessing/Binarizer.json';
import bucketedRandomProjectionLsh from './preprocessing/BucketedRandomProjectionLSH.json';
import bucketizer from './preprocessing/Bucketizer.json';
import chiSqSelector from './preprocessing/ChiSqSelector.json';
import countVectorizer from './preprocessing/CountVectorizer.json';
import dct from './preprocessing/DCT.json';
import elementWiseProduct from './preprocessing/ElementWiseProduct.json';
import hashingTf from './preprocessing/HashingTF.json';
import idf from './preprocessing/IDF.json';
import imputer from './preprocessing/Imputer.json';
import indexToString from './preprocessing/IndexToString.json';
import interaction from './preprocessing/Interaction.json';
import maxAbsScaler from './preprocessing/MaxAbsScaler.json';
import minHashLSH from './preprocessing/MinHashLSH.json';
import minMaxScaler from './preprocessing/MinMaxScaler.json';
import nGram from './preprocessing/NGram.json';
import normalizer from './preprocessing/Normalizer.json';
import oneHotEncoder from './preprocessing/OneHotEncoder.json';
import pca from './preprocessing/PCA.json';
import polynomialExpansion from './preprocessing/PolynomialExpansion.json';
import quantileDiscretizer from './preprocessing/QuantileDiscretizer.json';
import regexTokenizer from './preprocessing/RegexTokenizer.json';
import standardScaler from './preprocessing/StandardScaler.json';
import stopWordsRemover from './preprocessing/StopWordsRemover.json';
import stringIndexer from './preprocessing/StringIndexer.json';
import tokenizer from './preprocessing/Tokenizer.json';
import vectorAssembler from './preprocessing/VectorAssembler.json';
import vectorIndexer from './preprocessing/VectorIndexer.json';
import vectorSlicer from './preprocessing/VectorSlicer.json';
import word2Vec from './preprocessing/Word2Vec.json';

export const preprocessing = [
  binarizer,
  bucketedRandomProjectionLsh,
  bucketizer,
  chiSqSelector,
  countVectorizer,
  dct,
  elementWiseProduct,
  hashingTf,
  idf,
  imputer,
  indexToString,
  interaction,
  maxAbsScaler,
  minHashLSH,
  minMaxScaler,
  nGram,
  normalizer,
  oneHotEncoder,
  pca,
  polynomialExpansion,
  quantileDiscretizer,
  regexTokenizer,
  standardScaler,
  stopWordsRemover,
  stringIndexer,
  tokenizer,
  vectorAssembler,
  vectorIndexer,
  vectorSlicer,
  word2Vec
];

/*********************** */

const _streamingPreprocessingNames: Array<any> = [];
const _batchPreprocessingNames: Array<any> = [];
const _streamingPreprocessingObject: any = [];
const _batchPreprocessingObject: any = [];
const _streamingPreprocessing: Array<any> = [];
const _batchPreprocessing: Array<any> = [];

preprocessing.forEach((pp: any) => {
  if (!pp.supportedEngines) {
    return;
  }
  if (pp.supportedEngines.indexOf(Engine.Batch) > -1) {
    _batchPreprocessing.push(pp);
    _batchPreprocessingObject[pp.classPrettyName] = pp;
    _batchPreprocessingNames.push({
      name: pp.classPrettyName,
      value: pp,
      stepType: PipelineType.Preprocessing
    });
  }
  if (pp.supportedEngines.indexOf(Engine.Streaming) > -1) {
    _streamingPreprocessing.push(pp);
    _streamingPreprocessingObject[pp.classPrettyName] = pp;
    _streamingPreprocessingNames.push({
      name: pp.classPrettyName,
      value: pp,
      stepType: PipelineType.Preprocessing
    });
  }
});

export const streamingPreprocessing = _streamingPreprocessing;
export const batchPreprocessing = _batchPreprocessing;
export const streamingPreprocessingNames = _streamingPreprocessingNames;
export const batchPreprocessingNames = _batchPreprocessingNames;
export const streamingPreprocessingObject = _streamingPreprocessingObject;
export const batchPreprocessingObject = _batchPreprocessingObject;
