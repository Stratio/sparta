/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';

export const LIST_WORKFLOW = '[Workflow] List workflows';
export const LIST_WORKFLOW_COMPLETE = '[Workflow] List workflows complete';
export const LIST_WORKFLOW_FAIL = '[Workflow] List workflow fail';
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
export const DISPLAY_MODE = '[Workflow] Display mode workflows';
export const VALIDATE_WORKFLOW_NAME = '[Workflow] Validate workflow name';
export const VALIDATE_WORKFLOW_NAME_COMPLETE = '[Workflow] Correct worflow name validation';
export const VALIDATE_WORKFLOW_NAME_ERROR = '[Workflow] Incorrect workflow name validation';
export const SAVE_JSON_WORKFLOW = '[Workflow] Save workflow';
export const SAVE_JSON_WORKFLOW_COMPLETE = '[Worflow] Save workflow complete';
export const SAVE_JSON_WORKFLOW_ERROR = '[Workflow] Save json workflow error';
export const GET_WORKFLOW_EXECUTION_INFO = '[Worflow] Get Workflow execution info';
export const GET_WORKFLOW_EXECUTION_INFO_COMPLETE = '[Worflow] Get Workflow execution info complete';
export const GET_WORKFLOW_EXECUTION_INFO_ERROR = '[Worflow] Get Workflow execution info error';
export const CLOSE_WORKFLOW_EXECUTION_INFO = '[Worflow] Close workflow execution info';
export const CHANGE_ORDER = '[Workflow] Change order';
export const RESET_JSON_MODAL = '[Workflow] Reset JSON Modal';
export const CHANGE_FILTER = '[Workflow] Change filter';
export const SEARCH_WORKFLOWS = '[Workflow] Search workflow';
export const RESET_SELECTION = '[Workflow] Reset selection';
export const SET_PAGINATION_NUMBER = '[Workflow] Set pagination number';

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

export class DisplayModeAction implements Action {
  readonly type = DISPLAY_MODE;

  constructor(public payload: String) { }
}

export class ValidateWorkflowNameAction implements Action {
  readonly type = VALIDATE_WORKFLOW_NAME;

  constructor(public payload: any) { }
}

export class ValidateWorkflowNameComplete implements Action {
  readonly type = VALIDATE_WORKFLOW_NAME_COMPLETE;

  constructor() { }
}

export class ValidateWorkflowNameError implements Action {
  readonly type = VALIDATE_WORKFLOW_NAME_ERROR;

  constructor() { }
}

export class SaveJsonWorkflowAction implements Action {
  readonly type = SAVE_JSON_WORKFLOW;

  constructor(public payload: any) { }
}

export class SaveJsonWorkflowActionComplete implements Action {
  readonly type = SAVE_JSON_WORKFLOW_COMPLETE;

  constructor() { }
}

export class SaveJsonWorkflowActionError implements Action {
  readonly type = SAVE_JSON_WORKFLOW_ERROR;

  constructor() { }
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

export class ChangeOrderAction implements Action {
  readonly type = CHANGE_ORDER;
  constructor(public payload: any) { }
}

export class ResetJSONModal implements Action {
  readonly type = RESET_JSON_MODAL;
  constructor() {}
}

export class ChangeFilterAction implements Action {
  readonly type = CHANGE_FILTER;
  constructor(public payload: any) {}
}

export class SearchWorkflowsAction implements Action {
  readonly type = SEARCH_WORKFLOWS;
  constructor(public payload: any) {}
}

export class ResetSelectionAction implements Action {
  readonly type = RESET_SELECTION;
}

export class SetPaginationNumber implements Action {
  readonly type = SET_PAGINATION_NUMBER;
  constructor(public payload: any) {}
}

export type Actions =
  ListWorkflowAction |
  ListWorkflowFailAction |
  ListWorkflowCompleteAction |
  SelectWorkflowAction |
  DeselectWorkflowAction |
  RemoveWorkflowSelectionAction |
  DeleteWorkflowAction |
  DeleteWorkflowCompleteAction |
  DeleteWorkflowErrorAction |
  RunWorkflowAction |
  StopWorkflowAction |
  DownloadWorkflowsAction |
  DownloadWorkflowsCompleteAction |
  DownloadWorkflowsErrorAction |
  ValidateWorkflowNameAction |
  ValidateWorkflowNameComplete |
  ValidateWorkflowNameError |
  SaveJsonWorkflowAction |
  SaveJsonWorkflowActionComplete |
  SaveJsonWorkflowActionError |
  GetExecutionInfoAction |
  GetExecutionInfoCompleteAction |
  GetExecutionInfoErrorAction |
  CloseWorkflowExecutionInfoAction |
  ChangeOrderAction |
  ChangeFilterAction |
  ResetJSONModal |
  SearchWorkflowsAction |
  ResetSelectionAction |
  SetPaginationNumber;
