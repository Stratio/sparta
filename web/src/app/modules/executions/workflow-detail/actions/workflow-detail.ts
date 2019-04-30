/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {Action} from '@ngrx/store';
import { QualityRule } from '@app/executions/models';

export const GET_WORKFLOW_DETAIL = '[Executions] Get workflow detail';
export const GET_WORKFLOW_DETAIL_COMPLETE = '[Executions] Get workflow detail complete';
export const GET_QUALITY_RULES = '[Execution detail] List Quality Rules';
export const GET_QUALITY_RULES_COMPLETE = '[Execution detail] Complete list Quality Rules';
export const GET_SELECTED_EDGE = '[Execution detail] Create a list of filtered edges';

export class GetWorkflowDetailAction implements Action {
  readonly type = GET_WORKFLOW_DETAIL;
  constructor(public executionId: string) {}
}

export class GetWorkflowDetailCompleteAction implements Action {
  readonly type = GET_WORKFLOW_DETAIL_COMPLETE;
  constructor(public execution: any) {}
}

export class GetQualityRulesAction implements Action {
  readonly type = GET_QUALITY_RULES;
  constructor(public executionId: string) {}
}

export class GetQualityRulesActionComplete implements Action {
  readonly type = GET_QUALITY_RULES_COMPLETE;
  constructor(public payload: Array<QualityRule>) {}
}

export class SelectEdgeAction implements Action {
  readonly type = GET_SELECTED_EDGE;
  constructor(public payload: any) {}
}

export type Actions =
  | GetWorkflowDetailAction
  | GetWorkflowDetailCompleteAction
  | GetQualityRulesAction
  | GetQualityRulesActionComplete
  | SelectEdgeAction;
