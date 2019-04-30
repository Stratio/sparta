/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

export interface QualityRule {
  id: string;
  name: String;
  description: String;
  threshold: String;
  status: boolean;
  totalRows: String;
  rowsPassed: String;
  rowsFailed: String;
  qualityScore: String;
  satisfiedMessage: String;
  satisfiedIcon: String;
  warningIcon: String;
  globalAction: String;
  transformationStepName: String;
  outputStepName: String;
  successfulWriting: Boolean;
  sentToApi: Boolean;
  warning: Boolean;
  condition: Boolean;
  metadataPath: String;
  globalActionResume: String;
}

export interface Edge {
  origin: string;
  destination: string;
}

export const QUALITY_RULE_ACT_PASS = 'ACT_PASS';
export const QUALITY_RULE_ACT_NOT_PASS = 'ACT_MOV';

export const globalActionMap = new Map();
globalActionMap.set('ACT_MOV', 'Move refusals');
globalActionMap.set('ACT_PASS', 'Pass through');
