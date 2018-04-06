/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Action } from '@ngrx/store';

export const LIST_WORKFLOW = '[Workflow] List workflows';
export const LIST_WORKFLOW_COMPLETE = '[Workflow] List workflows complete';
export const LIST_WORKFLOW_FAIL = '[Workflow] List workflow fail';
export const VALIDATE_SELECTED = '[Workflow] Validate selected';
export const SELECT_WORKFLOW = '[Workflow] Select workflow';
export const DESELECT_WORKFLOW = '[Workflow] Deselect workflow';
export const REMOVE_WORKFLOW_SELECTION = '[Workflow] Remove workflow selection';
export const DELETE_WORKFLOW = '[Workflow] Delete workflow';
export const DELETE_WORKFLOW_COMPLETE = '[Workflow] Delete workflow complete';
export const DELETE_WORKFLOW_ERROR = '[Workflow] Delete workflow error';
export const DOWNLOAD_WORKFLOWS = '[Workflow] Download workflows';
export const DOWNLOAD_WORKFLOWS_COMPLETE = '[Workflow] Download workflows complete';
export const DOWNLOAD_WORKFLOWS_ERROR = '[Workflow] Download workflows error';
export const RUN_WORKFLOW = '[Workflow] Run workflow';
export const RUN_WORKFLOW_COMPLETE = '[Workflow] Run workflow complete';
export const RUN_WORKFLOW_ERROR = '[Workflow] Run workflow error';
export const STOP_WORKFLOW = '[Workflow] Stop workflow';
export const STOP_WORKFLOW_COMPLETE = '[Workflow] Stop workflow complete';
export const STOP_WORKFLOW_ERROR = '[Workflow] Stop workflow error';
export const GET_WORKFLOW_EXECUTION_INFO = '[Worflow] Get Workflow execution info';
export const GET_WORKFLOW_EXECUTION_INFO_COMPLETE = '[Worflow] Get Workflow execution info complete';
export const GET_WORKFLOW_EXECUTION_INFO_ERROR = '[Worflow] Get Workflow execution info error';
export const CLOSE_WORKFLOW_EXECUTION_INFO = '[Worflow] Close workflow execution info';
export const RESET_SELECTION = '[Workflow] Reset selection';

export class ListWorkflowAction implements Action {
  readonly type = LIST_WORKFLOW;
}

export class ListWorkflowFailAction implements Action {
  readonly type = LIST_WORKFLOW_FAIL;
}

export class ListWorkflowCompleteAction implements Action {
  readonly type = LIST_WORKFLOW_COMPLETE;
  constructor(public payload: any) { }
}

export class ValidateSelectedAction implements Action {
  readonly type = VALIDATE_SELECTED;
}

export class SelectWorkflowAction implements Action {
  readonly type = SELECT_WORKFLOW;

  constructor(public payload: any) { }
}

export class DeselectWorkflowAction implements Action {
  readonly type = DESELECT_WORKFLOW;

  constructor(public payload: any) { }
}

export class RemoveWorkflowSelectionAction implements Action {
  readonly type = REMOVE_WORKFLOW_SELECTION;
}

export class DeleteWorkflowAction implements Action {
  readonly type = DELETE_WORKFLOW;

  constructor(public payload: any) { }
}

export class DeleteWorkflowCompleteAction implements Action {
  readonly type = DELETE_WORKFLOW_COMPLETE;

  constructor(public payload: any) { }
}

export class DeleteWorkflowErrorAction implements Action {
  readonly type = DELETE_WORKFLOW_ERROR;
}

export class DownloadWorkflowsAction implements Action {
  readonly type = DOWNLOAD_WORKFLOWS;

  constructor(public payload: any) { }
}

export class DownloadWorkflowsCompleteAction implements Action {
  readonly type = DOWNLOAD_WORKFLOWS_COMPLETE;

  constructor(public payload: any) { }
}

export class DownloadWorkflowsErrorAction implements Action {
  readonly type = DOWNLOAD_WORKFLOWS_ERROR;
}

export class RunWorkflowAction implements Action {
  readonly type = RUN_WORKFLOW;
  constructor(public payload: any) { }
}

export class RunWorkflowCompleteAction implements Action {
  readonly type = RUN_WORKFLOW_COMPLETE;
  constructor(public payload: any) { }
}


export class RunWorkflowErrorAction implements Action {
  readonly type = RUN_WORKFLOW_ERROR;
}

export class StopWorkflowAction implements Action {
  readonly type = STOP_WORKFLOW;
  constructor(public payload: any) { }
}

export class StopWorkflowCompleteAction implements Action {
  readonly type = STOP_WORKFLOW_COMPLETE;
  constructor(public payload: any) { }
}

export class StopWorkflowErrorAction implements Action {
  readonly type = STOP_WORKFLOW_ERROR;
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

  constructor() { };
}

export class ResetSelectionAction implements Action {
  readonly type = RESET_SELECTION;
}


export type Actions = ListWorkflowAction
  | ListWorkflowFailAction
  | ListWorkflowCompleteAction
  | ValidateSelectedAction
  | SelectWorkflowAction
  | DeselectWorkflowAction
  | RemoveWorkflowSelectionAction
  | DeleteWorkflowAction
  | DeleteWorkflowCompleteAction
  | DeleteWorkflowErrorAction
  | RunWorkflowAction
  | StopWorkflowAction
  | DownloadWorkflowsAction
  | DownloadWorkflowsCompleteAction
  | DownloadWorkflowsErrorAction
  | GetExecutionInfoAction
  | GetExecutionInfoCompleteAction
  | GetExecutionInfoErrorAction
  | CloseWorkflowExecutionInfoAction
  | ResetSelectionAction;
