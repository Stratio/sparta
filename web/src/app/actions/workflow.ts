///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { Action } from '@ngrx/store';
import { type } from '../utils';

export const actionTypes: any = {
  LIST_WORKFLOW: type('[Workflow] List workflows'),
  LIST_WORKFLOW_COMPLETE: type('[Workflow] List workflows complete'),
  LIST_WORKFLOW_FAIL: type('[Workflow] List workflow fail'),
  SELECT_WORKFLOW: type('[Workflow] Select workflow'),
  DESELECT_WORKFLOW: type('[Workflow] Deselect workflow'),
  REMOVE_WORKFLOW_SELECTION: type('[Workflow] Remove workflow selection'),
  UPDATE_WORKFLOWS: type('[Workflow] Update workflows status'),
  UPDATE_WORKFLOWS_COMPLETE: type('[Worflow] Update workflows status complete'),
  UPDATE_WORKFLOWS_ERROR: type('[Worflow] Update workflows error'),
  DELETE_WORKFLOW: type('[Workflow] Delete workflow'),
  DELETE_WORKFLOW_COMPLETE: type('[Workflow] Delete workflow complete'),
  DELETE_WORKFLOW_ERROR: type('[Workflow] Delete workflow error'),
  DOWNLOAD_WORKFLOWS: type('[Workflow] Download workflows'),
  DOWNLOAD_WORKFLOWS_COMPLETE: type('[Workflow] Download workflows complete'),
  DOWNLOAD_WORKFLOWS_ERROR: type('[Workflow] Download workflows error'),
  RUN_WORKFLOW: type('[Workflow] Run workflow'),
  RUN_WORKFLOW_COMPLETE: type('[Workflow] Run workflow complete'),
  RUN_WORKFLOW_ERROR: type('[Workflow] Run workflow error'),
  STOP_WORKFLOW: type('[Workflow] Stop workflow'),
  STOP_WORKFLOW_COMPLETE: type('[Workflow] Stop workflow complete'),
  STOP_WORKFLOW_ERROR: type('[Workflow] Stop workflow error'),
  FILTER_WORKFLOWS: type('[Workflow] Search workflows'),
  DISPLAY_MODE: type('[Workflow] Display mode workflows'),
  VALIDATE_WORKFLOW_NAME: type('[Workflow] Validate workflow name'),
  VALIDATE_WORKFLOW_NAME_COMPLETE: type('[Workflow] Correct worflow name validation'),
  VALIDATE_WORKFLOW_NAME_ERROR: type(['[Workflow] Incorrect workflow name validation']),
  SAVE_JSON_WORKFLOW: type('[Workflow] Save workflow'),
  SAVE_JSON_WORKFLOW_COMPLETE: type('[Worflow] Save workflow complete'),
  GET_WORKFLOW_EXECUTION_INFO: type('[Worflow] Get Workflow execution info'),
  GET_WORKFLOW_EXECUTION_INFO_COMPLETE: type('[Worflow] Get Workflow execution info complete'),
  GET_WORKFLOW_EXECUTION_INFO_ERROR: type('[Worflow] Get Workflow execution info error'),
};

export class ListWorkflowAction implements Action {
  type: any = actionTypes.LIST_WORKFLOW;
}

export class ListWorkflowFailAction implements Action {
  type: any = actionTypes.LIST_WORKFLOW_FAIL;
}

export class ListWorkflowCompleteAction implements Action {
  type: any = actionTypes.LIST_WORKFLOW_COMPLETE;

  constructor(public payload: any) { }
}

export class SelectWorkflowAction implements Action {
  type: any = actionTypes.SELECT_WORKFLOW;

  constructor(public payload: any) { }
}

export class DeselectWorkflowAction implements Action {
  type: any = actionTypes.DESELECT_WORKFLOW;

  constructor(public payload: any) { }
}

export class RemoveWorkflowSelectionAction implements Action {
  type: any = actionTypes.REMOVE_WORKFLOW_SELECTION;
}


export class UpdateWorkflowStatusAction implements Action {
  type: any = actionTypes.UPDATE_WORKFLOWS;
}


export class UpdateWorkflowStatusCompleteAction implements Action {
  type: any = actionTypes.UPDATE_WORKFLOWS_COMPLETE;

  constructor(public payload: any) { }
}

export class UpdateWorkflowStatusErrorAction implements Action {
  type: any = actionTypes.UPDATE_WORKFLOWS_ERROR;
}

export class DeleteWorkflowAction implements Action {
  type: any = actionTypes.DELETE_WORKFLOW;

  constructor(public payload: any) { }
}

export class DeleteWorkflowCompleteAction implements Action {
  type: any = actionTypes.DELETE_WORKFLOW_COMPLETE;

  constructor(public payload: any) { }
}

export class DeleteWorkflowErrorAction implements Action {
  type: any = actionTypes.DELETE_WORKFLOW_ERROR;
}

export class DownloadWorkflowsAction implements Action {
  type: any = actionTypes.DOWNLOAD_WORKFLOWS;

  constructor(public payload: any) { }
}

export class DownloadWorkflowsCompleteAction implements Action {
  type: any = actionTypes.DOWNLOAD_WORKFLOWS_COMPLETE;

  constructor(public payload: any) { }
}

export class DownloadWorkflowsErrorAction implements Action {
  type: any = actionTypes.DOWNLOAD_WORKFLOWS_ERROR;
}

export class RunWorkflowAction implements Action {
  type: any = actionTypes.RUN_WORKFLOW;
  constructor(public payload: any) { }
}

export class RunWorkflowCompleteAction implements Action {
  type: any = actionTypes.RUN_WORKFLOW_COMPLETE;
  constructor(public payload: any) { }
}


export class RunWorkflowErrorAction implements Action {
  type: any = actionTypes.RUN_WORKFLOW_ERROR;
}

export class StopWorkflowAction implements Action {
  type: any = actionTypes.STOP_WORKFLOW;
  constructor(public payload: any) { }
}

export class StopWorkflowCompleteAction implements Action {
  type: any = actionTypes.STOP_WORKFLOW_COMPLETE;
  constructor(public payload: any) { }
}

export class StopWorkflowErrorAction implements Action {
  type: any = actionTypes.STOP_WORKFLOW_ERROR;
}

export class SearchAction implements Action {
  type: any = actionTypes.FILTER_WORKFLOWS;

  constructor(public payload: String) { }
}

export class DisplayModeAction implements Action {
  type: any = actionTypes.DISPLAY_MODE;

  constructor(public payload: String) { }
}

export class ValidateWorkflowNameAction implements Action {
  type: any = actionTypes.VALIDATE_WORKFLOW_NAME;

  constructor(public payload: any) { }
}

export class ValidateWorkflowNameComplete implements Action {
  type: any = actionTypes.VALIDATE_WORKFLOW_NAME_COMPLETE;

  constructor() { }
}

export class ValidateWorkflowNameError implements Action {
  type: any = actionTypes.VALIDATE_WORKFLOW_NAME_ERROR;

  constructor() { }
}

export class SaveJsonWorkflowAction implements Action {
  type: any = actionTypes.SAVE_JSON_WORKFLOW;

  constructor(public payload: any) { }
}

export class SaveJsonWorkflowActionComplete implements Action {
  type: any = actionTypes.SAVE_JSON_WORKFLOW_COMPLETE;

  constructor() { }
}

export class SaveJsonWorkflowActionError implements Action {
  type: any = actionTypes.SAVE_JSON_WORKFLOW_ERROR;

  constructor() { }
}

export class GetExecutionInfoAction implements Action {
  type: any = actionTypes.GET_WORKFLOW_EXECUTION_INFO;

  constructor(public payload: any) { }
}

export class GetExecutionInfoCompleteAction implements Action {
  type: any = actionTypes.GET_WORKFLOW_EXECUTION_INFO_COMPLETE;

  constructor(public payload: any) { }
}
export class GetExecutionInfoErrorAction implements Action {
  type: any = actionTypes.GET_WORKFLOW_EXECUTION_INFO_ERROR;

  constructor() { }
}

export type Actions =
  ListWorkflowAction |
  ListWorkflowFailAction |
  ListWorkflowCompleteAction |
  SelectWorkflowAction |
  DeselectWorkflowAction |
  RemoveWorkflowSelectionAction |
  UpdateWorkflowStatusAction |
  UpdateWorkflowStatusCompleteAction |
  UpdateWorkflowStatusErrorAction |
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
  SearchAction |
  GetExecutionInfoAction |
  GetExecutionInfoCompleteAction |
  GetExecutionInfoErrorAction;
