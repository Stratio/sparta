/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Action } from '@ngrx/store';


export const CREATE_EXECUTION_DETAIL = '[Execution detail] List parameters';
export const GET_EXECUTION_DETAIL = '[Execution detail] Get parameters';
export const STOP_EXECUTION = '[Execution detail] Stop a execution';
export const RERUN_EXECUTION = '[Execution detail] Rerun a execution';

export const ARCHIVE_EXECUTION = '[Execution detail] Archive a execution';
export const UNARCHIVE_EXECUTION = '[Execution detail] Unarchive a execution';

export const DELETE_EXECUTION = '[Execution detail] Delete a execution';
export const CANCEL_POLLING = '[Execution detail] Cancel Polling';


export class CreateExecutionDetailAction implements Action {
  readonly type = CREATE_EXECUTION_DETAIL;
  constructor(public payload: any) { }
}

export class GetExecutionDetailAction implements Action {
  readonly type = GET_EXECUTION_DETAIL;
  constructor(public executionId: string) { }
}

export class StopExecutionAction implements Action {
  readonly type = STOP_EXECUTION;
  constructor(public executionId: number) { }
}


export class RerunExecutionAction implements Action {
  readonly type = RERUN_EXECUTION;
  constructor(public executionId: number) { }
}


export class ArchiveExecutionAction implements Action {
  readonly type = ARCHIVE_EXECUTION;
  constructor(public executionId: String) { }
}

export class UnArchiveExecutionAction implements Action {
  readonly type = UNARCHIVE_EXECUTION;
  constructor(public executionId: String) { }
}


export class DeleteExecutionAction implements Action {
  readonly type = DELETE_EXECUTION;
  constructor(public executionId: String) {}
}


export class CancelPollingAction implements Action {
  readonly type = CANCEL_POLLING;
}

export type Actions =
  CreateExecutionDetailAction |
  GetExecutionDetailAction |
  StopExecutionAction |
  RerunExecutionAction |
  ArchiveExecutionAction |
  UnArchiveExecutionAction |
  DeleteExecutionAction |
  CancelPollingAction;
