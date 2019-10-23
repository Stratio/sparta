/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

 export const Engine = {
    Streaming : 'Streaming',
    Batch : 'Batch',
    PlannedQr: 'Proactive Qr'
 };

  export const Execution = {
    User : 'UserExecution',
    System : 'SystemExecution'
  };

 export const StepType = {
    Input : 'Input',
    Output : 'Output',
    Transformation : 'Transformation',
    Pipeline: 'Pipeline'
 };

export const PipelineType = {
    Preprocessing: 'Preprocessing',
    Algorithm: 'Algorithm'
};

export const DateFormats = {
  executionTimeStampMoment: 'YYYY-MM-DDTHH:mm:ssZ',
  executionTimeStampFormat: 'DD/MM/YYYY HH:mm:ss',
  executionDateFormat: 'DD/MM/YYYY',
  executionHourFormat: 'HH:mm:ss'
};

export enum ExecutionStatus {
  RunningStatus = 'Running',
  StoppedStatus = 'Stopped',
  FailedStatus = 'Failed'
}

export enum CITags {
  Released = 'Released',
  ReleaseCandidatePrefix = 'RC-',
  ReleaseCandidate = 'ReleaseCandidate'
}
