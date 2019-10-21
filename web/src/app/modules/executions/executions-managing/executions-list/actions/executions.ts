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
export const LIST_EXECUTIONS_EMPTY = '[Executions Managing] List executions empty';

export const LIST_ARCHIVED_EXECUTIONS = '[Executions Managing] List archived executions';
export const LIST_ARCHIVED_EXECUTIONS_COMPLETE = '[Executions Managing] List archived executions complete';
export const LIST_ARCHIVED_EXECUTIONS_FAIL = '[Executions Managing] List archived executions fail';

export const ARCHIVE_EXECUTIONS = '[Executions Managing] Archive executions';
export const ARCHIVE_EXECUTIONS_COMPLETE = '[Executions Managing] Archive executions complete';
export const ARCHIVE_EXECUTIONS_FAIL = '[Executions Managing] Archive executions fail';

export const UNARCHIVE_EXECUTIONS = '[Executions Managing] Unarchive executions';
export const UNARCHIVE_EXECUTIONS_COMPLETE = '[Executions Managing] Unarchive executions complete';
export const UNARCHIVE_EXECUTIONS_FAIL = '[Executions Managing] Unarchive executions fail';

export const SELECT_EXECUTIONS_ACTION = '[Executions Managing] Select execution';
export const DESELECT_EXECUTIONS_ACTION = '[Executions Managing] Deselect execution';
export const STOP_EXECUTIONS_ACTION = '[Executions Managing] Stop execution';
export const STOP_EXECUTIONS_ACTION_COMPLETE = '[Executions Managing] Stop execution complete';
export const RERUN_EXECUTION = '[Executions Managing] Rerun execution';
export const RERUN_EXECUTION_COMPLETE = '[Executions Managing] Rerun execution complete';
export const RERUN_EXECUTION_ERROR = '[Executions Managing] Rerun execution error';

export const CHANGE_PAGINATION = '[Executions Managing] Change pagination';

export const SELECT_EXEC_TYPE_FILTER = '[Executions Managing] Select execution type filter';
export const SELECT_WF_TYPE_FILTER = '[Executions Managing] Select workflow type filter';
export const SELECT_STATUS_FILTER = '[Executions Managing] Select status filter';
export const SELECT_TIME_INTERVAL_FILTER = '[Executions Managing] Select time interval filter';

export const SEARCH_EXECUTION = '[Executions Managing] Search execution';

export const CHANGE_ORDER = '[Executions Managing] Change order';

export const SELECT_ALL_EXECUTIONS = '[Executions Managing] Select all executions';
export const DESELECT_ALL_EXECUTIONS = '[Executions Managing] Deselect all executions';

export const RESET_VALUES = '[Executions Managing] Reset values';

export const GET_WORKFLOW_EXECUTION_INFO = '[Worflow] Get Workflow execution info';
export const GET_WORKFLOW_EXECUTION_INFO_COMPLETE = '[Worflow] Get Workflow execution info complete';
export const GET_WORKFLOW_EXECUTION_INFO_ERROR = '[Worflow] Get Workflow execution info error';
export const CLOSE_WORKFLOW_EXECUTION_INFO = '[Worflow] Close workflow execution info';

export const CANCEL_EXECUTION_POLLING = '[Workflow] Cancel execution polling';

export const SET_ARCHIVED_PAGE = '[Workflow] Set archived page';

export const DELETE_EXECUTION = '[Workflow] Delete execution';
export const DELETE_EXECUTION_COMPLETE = '[Workflow] Delete execution complete';
export const DELETE_EXECUTION_ERROR = '[Workflow] Delete execution error';


export const GET_EXECUTION = '[Executions] Get execution';
export const GET_EXECUTION_COMPLETE = '[Executions] Get execution complete';
export const GET_EXECUTION_ERROR = '[Executions] Get execution error';

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

export class ListExecutionsEmptyAction implements Action {
  readonly type = LIST_EXECUTIONS_EMPTY;
}

export class ListArchivedExecutionsAction implements Action {
   readonly type = LIST_ARCHIVED_EXECUTIONS;
}

export class ListArchivedExecutionsFailAction implements Action {
   readonly type = LIST_ARCHIVED_EXECUTIONS_FAIL;
}

export class ListArchivedExecutionsCompleteAction implements Action {
   readonly type = LIST_ARCHIVED_EXECUTIONS_COMPLETE;
   constructor(public payload: any) { }
}

export class ArchiveExecutionsAction implements Action {
   readonly type = ARCHIVE_EXECUTIONS;
}

export class ArchiveExecutionsFailAction implements Action {
   readonly type = ARCHIVE_EXECUTIONS_FAIL;
}

export class ArchiveExecutionsCompleteAction implements Action {
   readonly type = ARCHIVE_EXECUTIONS_COMPLETE;
}

export class UnarchiveExecutionsAction implements Action {
   readonly type = UNARCHIVE_EXECUTIONS;
}

export class UnarchiveExecutionsFailAction implements Action {
   readonly type = UNARCHIVE_EXECUTIONS_FAIL;
}

export class UnarchiveExecutionsCompleteAction implements Action {
   readonly type = UNARCHIVE_EXECUTIONS_COMPLETE;
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

export class SelectExecTypeFilterAction implements Action {
  readonly type = SELECT_EXEC_TYPE_FILTER;
  constructor(public executionType: string) {}
}

export class SelectWfTypeFilterAction implements Action {
  readonly type = SELECT_WF_TYPE_FILTER;
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

export class ChangeExecutionsSelectAllExecutions implements Action {
  readonly type = SELECT_ALL_EXECUTIONS;
}

export class ChangeExecutionsDeselectAllExecutions implements Action {
  readonly type = DESELECT_ALL_EXECUTIONS;
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

export class CancelExecutionPolling implements Action {
  readonly type = CANCEL_EXECUTION_POLLING;
}

export class GetExecutionInfoAction implements Action {
  readonly type = GET_WORKFLOW_EXECUTION_INFO;

  constructor(public payload: any) { }
}

export class GetExecutionInfoCompleteAction implements Action {
  readonly type = GET_WORKFLOW_EXECUTION_INFO_COMPLETE;

  constructor(public payload: any) { }
}
export class GetExecutionInfoErrorAction implements Action {
  readonly type = GET_WORKFLOW_EXECUTION_INFO_ERROR;

  constructor() { }
}

export class CloseWorkflowExecutionInfoAction implements Action {
  readonly type = CLOSE_WORKFLOW_EXECUTION_INFO;

  constructor() { }
}

export class SetArchivedPageAction implements Action {
  readonly type = SET_ARCHIVED_PAGE;
  constructor(public payload) {}
}

export class DeleteExecutionAction implements Action {
  readonly type = DELETE_EXECUTION;
  constructor(public executionId: string) {}
}

export class DeleteExecutionCompleteAction implements Action {
  readonly type = DELETE_EXECUTION_COMPLETE;
  constructor(public executionIds: Array<string>) {}
}

export class DeleteExecutionErrorAction implements Action {
  readonly type = DELETE_EXECUTION_ERROR;
}

export class GetExecutionAction implements Action {
  readonly type = GET_EXECUTION;
  constructor(public executionId: string) {}
}

export class GetExecutionCompleteAction implements Action {
  readonly type = GET_EXECUTION_COMPLETE;
  constructor(public execution: any) {}
}

export class GetExecutionErrorAction implements Action {
  readonly type = GET_EXECUTION_ERROR;
}

export class ReRunExecutionAction implements Action {
  readonly type = RERUN_EXECUTION;
  constructor(public executionId: string) {}
}

export class ReRunExecutionCompleteAction implements Action {
  readonly type = RERUN_EXECUTION_COMPLETE;
}

export class ReRunExecutionErrorAction implements Action {
  readonly type = RERUN_EXECUTION_ERROR;
  constructor(public payload: any) {}
}

export type Actions = ListExecutionsAction
   | ListExecutionsFailAction
   | ListExecutionsCompleteAction
   | ListExecutionsEmptyAction
   | ListArchivedExecutionsAction
   | ListArchivedExecutionsCompleteAction
   | ListArchivedExecutionsFailAction
   | ArchiveExecutionsAction
   | ArchiveExecutionsCompleteAction
   | ArchiveExecutionsFailAction
   | UnarchiveExecutionsAction
   | UnarchiveExecutionsCompleteAction
   | UnarchiveExecutionsFailAction
   | SelectExecutionAction
   | DeselectExecutionAction
   | SelectExecTypeFilterAction
   | SelectWfTypeFilterAction
   | SelectStatusFilterAction
   | SelectTimeIntervalFilterAction
   | SearchExecutionAction
   | ChangeExecutionsSelectAllExecutions
   | ChangeExecutionsDeselectAllExecutions
   | ChangeExecutionsOrderAction
   | ResetValuesAction
   | ChangePaginationAction
   | CancelExecutionPolling
   | GetExecutionInfoAction
   | GetExecutionInfoCompleteAction
   | GetExecutionInfoErrorAction
   | CloseWorkflowExecutionInfoAction
   | SetArchivedPageAction
   | DeleteExecutionAction
   | DeleteExecutionCompleteAction
   | DeleteExecutionErrorAction
   | GetExecutionAction
   | GetExecutionCompleteAction
   | GetExecutionErrorAction
   | ReRunExecutionAction
   | ReRunExecutionCompleteAction
   | ReRunExecutionErrorAction;
