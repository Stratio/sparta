/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Action } from '@ngrx/store';
import { QualityRule } from '@app/executions/models';

export const CREATE_EXECUTION_DETAIL = '[Execution detail] List parameters';
export const GET_EXECUTION_DETAIL = '[Execution detail] Get parameters';
export const STOP_EXECUTION = '[Execution detail] Stop a execution';
export const RERUN_EXECUTION = '[Execution detail] Rerun a execution';

export const ARCHIVE_EXECUTION = '[Execution detail] Archive a execution';
export const UNARCHIVE_EXECUTION = '[Execution detail] Unarchive a execution';

export const DELETE_EXECUTION = '[Execution detail] Delete a execution';
export const CANCEL_POLLING = '[Execution detail] Cancel Polling';
export const RESET_EXECUTION_DETAIL = '[Execution detail] Reset execution detail';

export const GET_QUALITY_RULES = '[Execution detail] List Quality Rules';
export const GET_QUALITY_RULES_COMPLETE = '[Execution detail] Complete list Quality Rules';
export const CANCEL_QUALITY_RULES_POLLING = '[Execution detail] Cancel polling of the Quality Rules';

export const SHOW_CONSOLE = '[Execution detail] Show console';
export const HIDE_CONSOLE = '[Execution detail] Hide console';

export const FILTER_PARAMETERS = '[Execution detail] Change the filter for search parameters';

export class CreateExecutionDetailAction implements Action {
  readonly type = CREATE_EXECUTION_DETAIL;
  constructor(public payload: any) {}
}

export class GetExecutionDetailAction implements Action {
  readonly type = GET_EXECUTION_DETAIL;
  constructor(public executionId: string) {}
}

export class StopExecutionAction implements Action {
  readonly type = STOP_EXECUTION;
  constructor(public executionId: number) {}
}

export class RerunExecutionAction implements Action {
  readonly type = RERUN_EXECUTION;
  constructor(public executionId: number) {}
}

export class ArchiveExecutionAction implements Action {
  readonly type = ARCHIVE_EXECUTION;
  constructor(public executionId: String) {}
}

export class UnArchiveExecutionAction implements Action {
  readonly type = UNARCHIVE_EXECUTION;
  constructor(public executionId: String) {}
}

export class DeleteExecutionAction implements Action {
  readonly type = DELETE_EXECUTION;
  constructor(public executionId: String) {}
}

export class CancelPollingAction implements Action {
  readonly type = CANCEL_POLLING;
}

export class ResetExecutionDetail implements Action {
  readonly type = RESET_EXECUTION_DETAIL;
}

export class GetQualityRulesAction implements Action {
  readonly type = GET_QUALITY_RULES;
  constructor(public executionId: string) {}
}

export class GetQualityRulesActionComplete implements Action {
  readonly type = GET_QUALITY_RULES_COMPLETE;
  constructor(public payload: Array<QualityRule>) {}
}

export class CancelPollingQualityRulesAction implements Action {
  readonly type = CANCEL_QUALITY_RULES_POLLING;
}

export class FilterParametersAction implements Action {
  readonly type = FILTER_PARAMETERS;
  constructor(public filter: string) {}
}

export class ShowConsoleAction implements Action {
  readonly type = SHOW_CONSOLE;
}

export class HideConsoleAction implements Action {
  readonly type = HIDE_CONSOLE;
}


export type Actions =
  | CreateExecutionDetailAction
  | GetExecutionDetailAction
  | StopExecutionAction
  | RerunExecutionAction
  | ArchiveExecutionAction
  | UnArchiveExecutionAction
  | DeleteExecutionAction
  | CancelPollingAction
  | GetQualityRulesAction
  | GetQualityRulesActionComplete
  | CancelPollingQualityRulesAction
  | ResetExecutionDetail
  | FilterParametersAction
  | ShowConsoleAction
  | HideConsoleAction;
