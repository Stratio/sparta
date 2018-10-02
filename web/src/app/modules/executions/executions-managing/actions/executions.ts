/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Action } from '@ngrx/store';
import { Order } from '@stratio/egeo';

export const LIST_EXECUTIONS = '[Executions Managing] List executions';
export const LIST_EXECUTIONS_COMPLETE = '[Executions Managing] List executions complete';
export const LIST_EXECUTIONS_FAIL = '[Executions Managing] List executions fail';
export const SELECT_EXECUTIONS_ACTION = '[Executions Managing] Select execution';
export const DESELECT_EXECUTIONS_ACTION = '[Executions Managing] Deselect execution';
export const STOP_EXECUTIONS_ACTION = '[Executions Managing] Stop execution';
export const STOP_EXECUTIONS_ACTION_COMPLETE = '[Executions Managing] Stop execution complete';

export const CHANGE_PAGINATION = '[Executions Managing] Change pagination';

export const SELECT_TYPE_FILTER = '[Executions Managing] Select type filter';
export const SELECT_STATUS_FILTER = '[Executions Managing] Select status filter';
export const SELECT_TIME_INTERVAL_FILTER = '[Executions Managing] Select time interval filter';

export const SEARCH_EXECUTION = '[Executions Managing] Search execution';

export const CHANGE_ORDER = '[Executions Managing] Change order';

export const RESET_VALUES = '[Executions Managing] Reset values';

export const GET_WORKFLOW_EXECUTION_INFO = '[Worflow] Get Workflow execution info';
export const GET_WORKFLOW_EXECUTION_INFO_COMPLETE = '[Worflow] Get Workflow execution info complete';
export const GET_WORKFLOW_EXECUTION_INFO_ERROR = '[Worflow] Get Workflow execution info error';

export class ListExecutionsAction implements Action {
   readonly type = LIST_EXECUTIONS;
}

export class ListExecutionsFailAction implements Action {
   readonly type = LIST_EXECUTIONS_FAIL;
}

export class ListExecutionsCompleteAction implements Action {
   readonly type = LIST_EXECUTIONS_COMPLETE;
   constructor(public payload: any) { }
}

export class SelectExecutionAction implements Action {
   readonly type = SELECT_EXECUTIONS_ACTION;
   constructor(public execution: any) { }
}

export class DeselectExecutionAction implements Action {
   readonly type = DESELECT_EXECUTIONS_ACTION;
   constructor(public execution: any) { }
}

export class StopExecutionAction implements Action {
   readonly type = STOP_EXECUTIONS_ACTION;
}

export class StopExecutionCompleteAction implements Action {
   readonly type = STOP_EXECUTIONS_ACTION_COMPLETE;
}

export class SelectTypeFilterAction implements Action {
  readonly type = SELECT_TYPE_FILTER;
  constructor(public workflowType: string) {}
}

export class SelectStatusFilterAction implements Action {
  readonly type = SELECT_STATUS_FILTER;
  constructor(public status: string) {}
}

export class SelectTimeIntervalFilterAction implements Action {
  readonly type = SELECT_TIME_INTERVAL_FILTER;
  constructor(public time: number) {}
}

export class SearchExecutionAction implements Action {
  readonly type = SEARCH_EXECUTION;
  constructor(public searchQuery: string) {}
}

export class ChangeExecutionsOrderAction implements Action {
  readonly type = CHANGE_ORDER;
  constructor(public order: Order) {}
}

export class ResetValuesAction implements Action {
  readonly type = RESET_VALUES;
}

export class ChangePaginationAction implements Action {
  readonly type = CHANGE_PAGINATION;
  constructor(public payload: any) {}
}

export type Actions = ListExecutionsAction
   | ListExecutionsFailAction
   | ListExecutionsCompleteAction
   | SelectExecutionAction
   | DeselectExecutionAction
   | SelectTypeFilterAction
   | SelectStatusFilterAction
   | SelectTimeIntervalFilterAction
   | SearchExecutionAction
   | ChangeExecutionsOrderAction
   | ResetValuesAction
   | ChangePaginationAction;
