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

export const LIST_GROUPS = '[Workflow-Managing] List groups';
export const LIST_GROUPS_COMPLETE = '[Workflow-Managing] List groups complete';
export const LIST_GROUPS_ERROR = '[Workflow-Managing] List groups error';
export const INIT_CREATE_GROUP = '[Workflow-Managing] Init create group';
export const CREATE_GROUP = '[Workflow-Managing] Create group';
export const CREATE_GROUP_COMPLETE = '[Workflow-Managing] Create group complete';
export const CREATE_GROUP_ERROR = '[Workflow-Managing] Create group error';
export const CHANGE_GROUP_LEVEL = '[Workflow-Managing] Change group level';
export const CHANGE_GROUP_LEVEL_COMPLETE = '[Workflow-Managing] Change group level complete';
export const LIST_GROUP_WORKFLOWS = '[Workflow-Managing] List workflows';
export const LIST_GROUP_WORKFLOWS_COMPLETE = '[Workflow-Managing] List workflows complete';
export const LIST_GROUP_WORKFLOWS_FAIL = '[Workflow-Managing] List workflow fail';
export const SELECT_WORKFLOW = '[Workflow-Managing] Select workflow';
export const SHOW_WORKFLOW_VERSIONS = '[Workflow-Managing] Show workflow versions';
export const SELECT_GROUP = '[Workflow-Managing] Select group';
export const SELECT_VERSION = '[Workflow-Managing] Select version';
export const REMOVE_WORKFLOW_SELECTION = '[Workflow-Managing] Remove workflow selection';
export const DELETE_WORKFLOW = '[Workflow-Managing] Delete workflow';
export const DELETE_WORKFLOW_COMPLETE = '[Workflow-Managing] Delete workflow complete';
export const DELETE_WORKFLOW_ERROR = '[Workflow-Managing] Delete workflow error';
export const DELETE_GROUP_COMPLETE = '[Workflow-Managing] Delete group complete';
export const DELETE_VERSION = '[Workflow-Managing] Delete version';
export const DELETE_VERSION_COMPLETE = '[Workflow-Managing] Delete version complete';
export const DELETE_VERSION_ERROR = '[Workflow-Managing] Delete version error';
export const GENERATE_NEW_VERSION = '[Workflow-Managing] Generate new version';
export const GENERATE_NEW_VERSION_COMPLETE = '[Workflow-Managing] Generate new version complete';
export const GENERATE_NEW_VERSION_ERROR = '[Workflow-Managing] Generate new version error';
export const DUPLICATE_WORKFLOW = '[Workflow-Managing] Duplicate workflow';
export const DUPLICATE_WORKFLOW_COMPLETE = '[Workflow-Managing] Duplicate workflow complete';
export const DUPLICATE_WORKFLOW_ERROR = '[Workflow-Managing] Duplicate workflow error';
export const DOWNLOAD_WORKFLOWS = '[Workflow-Managing] Download workflows';
export const DOWNLOAD_WORKFLOWS_COMPLETE = '[Workflow-Managing] Download workflows complete';
export const DOWNLOAD_WORKFLOWS_ERROR = '[Workflow-Managing] Download workflows error';
export const RUN_WORKFLOW = '[Workflow-Managing] Run workflow';
export const RUN_WORKFLOW_COMPLETE = '[Workflow-Managing] Run workflow complete';
export const RUN_WORKFLOW_ERROR = '[Workflow-Managing] Run workflow error';
export const STOP_WORKFLOW = '[Workflow-Managing] Stop workflow';
export const STOP_WORKFLOW_COMPLETE = '[Workflow-Managing] Stop workflow complete';
export const STOP_WORKFLOW_ERROR = '[Workflow-Managing] Stop workflow error';
export const FILTER_WORKFLOWS = '[Workflow-Managing] Search workflows';
export const DISPLAY_MODE = '[Workflow-Managing] Display mode workflows';
export const VALIDATE_WORKFLOW_NAME = '[Workflow-Managing] Validate workflow name';
export const VALIDATE_WORKFLOW_NAME_COMPLETE = '[Workflow-Managing] Correct worflow name validation';
export const VALIDATE_WORKFLOW_NAME_ERROR = '[Workflow-Managing] Incorrect workflow name validation';
export const SAVE_JSON_WORKFLOW = '[Workflow-Managing] Save workflow';
export const SAVE_JSON_WORKFLOW_COMPLETE = '[Worflow] Save workflow complete';
export const SAVE_JSON_WORKFLOW_ERROR = '[Workflow-Managing] Save json workflow error';
export const GET_WORKFLOW_EXECUTION_INFO = '[Worflow] Get Workflow execution info';
export const GET_WORKFLOW_EXECUTION_INFO_COMPLETE = '[Worflow] Get Workflow execution info complete';
export const GET_WORKFLOW_EXECUTION_INFO_ERROR = '[Worflow] Get Workflow execution info error';
export const CLOSE_WORKFLOW_EXECUTION_INFO = '[Worflow] Close workflow execution info';
export const CHANGE_ORDER = '[Workflow-Managing] Change order';
export const CHANGE_VERSIONS_ORDER = '[Workflow-Managing] Change versions order';
export const RENAME_GROUP = '[Workflow-Managing] Rename group';
export const RENAME_GROUP_COMPLETE = '[Workflow-Managing] Rename group complete';
export const RENAME_GROUP_ERROR = '[Workflow-Managing] Rename group error';
export const RENAME_WORKFLOW = '[Workflow-Managing] Rename workflow';
export const RENAME_WORKFLOW_COMPLETE = '[Workflow-Managing] Rename workflow complete';
export const RENAME_WORKFLOW_ERROR = '[Workflow-Managing] Rename workflow error';
export const MOVE_WORKFLOW = '[Workflow-Managing] Move workflow';
export const MOVE_WORKFLOW_COMPLETE = '[Workflow-Managing] Move workflow complete';
export const MOVE_WORKFLOW_ERROR = '[Workflow-Managing] Move workflow error';
export const RESET_MODAL = '[Workflow-Managing] Reset modal';

export class ListGroupWorkflowsAction implements Action {
  readonly type = LIST_GROUP_WORKFLOWS;
}

export class ListGroupWorkflowsFailAction implements Action {
  readonly type = LIST_GROUP_WORKFLOWS_FAIL;
}

export class ListGroupWorkflowsCompleteAction implements Action {
  readonly type = LIST_GROUP_WORKFLOWS_COMPLETE;
  constructor(public payload: any) { }
}

export class ChangeGroupLevelAction implements Action {
  readonly type = CHANGE_GROUP_LEVEL;
  constructor(public payload: any) { }
}

export class ChangeGroupLevelCompleteAction implements Action {
  readonly type = CHANGE_GROUP_LEVEL_COMPLETE;
  constructor(public payload: any) { }
}

export class InitCreateGroupAction implements Action {
  readonly type = INIT_CREATE_GROUP;
  constructor() { }
}

export class CreateGroupAction implements Action {
  readonly type = CREATE_GROUP;
  constructor(public payload: any) { }
}

export class CreateGroupCompleteAction implements Action {
  readonly type = CREATE_GROUP_COMPLETE;
  constructor(public payload: any) { }
}

export class CreateGroupErrorAction implements Action {
  readonly type = CREATE_GROUP_ERROR;
  constructor(public payload: any) { }
}

export class ListGroupsAction implements Action {
  readonly type = LIST_GROUPS;
  constructor() { }
}

export class ListGroupsCompleteAction implements Action {
  readonly type = LIST_GROUPS_COMPLETE;
  constructor(public payload: any) { }
}

export class ListGroupsErrorAction implements Action {
  readonly type = LIST_GROUPS_ERROR;
  constructor() { }
}

export class SelectWorkflowAction implements Action {
  readonly type = SELECT_WORKFLOW;

  constructor(public payload: any) { }
}

export class ShowWorkflowVersionsAction implements Action {
  readonly type = SHOW_WORKFLOW_VERSIONS;

  constructor(public payload: any) { }
}

export class SelectGroupAction implements Action {
  readonly type = SELECT_GROUP;

  constructor(public payload: any) { }
}

export class SelectVersionAction implements Action {
  readonly type = SELECT_VERSION;

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

export class DeleteGroupCompleteAction implements Action {
  readonly type = DELETE_GROUP_COMPLETE;
}

export class DeleteVersionAction implements Action {
  readonly type = DELETE_VERSION;

  constructor() { }
}

export class DeleteVersionCompleteAction implements Action {
  readonly type = DELETE_VERSION_COMPLETE;

  constructor(public payload: any) { }
}

export class DeleteVersionErrorAction implements Action {
  readonly type = DELETE_VERSION_ERROR;
}

export class GenerateNewVersionAction implements Action {
  readonly type = GENERATE_NEW_VERSION;
}

export class GenerateNewVersionCompleteAction implements Action {
  readonly type = GENERATE_NEW_VERSION_COMPLETE;
}


export class GenerateNewVersionErrorAction implements Action {
  readonly type = GENERATE_NEW_VERSION_ERROR;
}

export class DuplicateWorkflowAction implements Action {
  readonly type = DUPLICATE_WORKFLOW;
  constructor(public payload: any) { }
}

export class DuplicateWorkflowCompleteAction implements Action {
  readonly type = DUPLICATE_WORKFLOW_COMPLETE;
}

export class DuplicateWorkflowErrorAction implements Action {
  readonly type = DUPLICATE_WORKFLOW_ERROR;
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

export class SearchAction implements Action {
  readonly type = FILTER_WORKFLOWS;

  constructor(public payload: String) { }
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

  constructor(public payload: any) { }
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

export class ChangeVersionsOrderAction implements Action {
  readonly type = CHANGE_VERSIONS_ORDER;
  constructor(public payload: any) { }
}

export class RenameGroupAction implements Action {
  readonly type = RENAME_GROUP;
  constructor(public payload: any) { }
}

export class RenameGroupCompleteAction implements Action {
  readonly type = RENAME_GROUP_COMPLETE;
  constructor() { }
}

export class RenameGroupErrorAction implements Action {
  readonly type = RENAME_GROUP_ERROR;
  constructor(public payload: any) { }
}

export class RenameWorkflowAction implements Action {
  readonly type = RENAME_WORKFLOW;
  constructor(public payload: any) { }
}

export class RenameWorkflowCompleteAction implements Action {
  readonly type = RENAME_WORKFLOW_COMPLETE;
  constructor() { }
}

export class RenameWorkflowErrorAction implements Action {
  readonly type = RENAME_WORKFLOW_ERROR;
  constructor(public payload: any) { }
}

export class MoveWorkflowAction implements Action {
  readonly type = MOVE_WORKFLOW;
  constructor(public payload: any) { }
}

export class MoveWorkflowCompleteAction implements Action {
  readonly type = MOVE_WORKFLOW_COMPLETE;
}

export class MoveWorkflowErrorAction implements Action {
  readonly type = MOVE_WORKFLOW_ERROR;
}

export class ResetModalAction implements Action {
  readonly type = RESET_MODAL;
}


export type Actions =
  ListGroupWorkflowsAction |
  ListGroupWorkflowsFailAction |
  ListGroupWorkflowsCompleteAction |
  ListGroupsAction |
  ListGroupsCompleteAction |
  ListGroupsErrorAction |
  ChangeGroupLevelAction |
  ChangeGroupLevelCompleteAction |
  InitCreateGroupAction |
  CreateGroupAction |
  CreateGroupCompleteAction |
  CreateGroupErrorAction |
  SelectWorkflowAction |
  SelectVersionAction |
  SelectGroupAction |
  RemoveWorkflowSelectionAction |
  DeleteWorkflowAction |
  DeleteWorkflowCompleteAction |
  DeleteWorkflowErrorAction |
  DeleteGroupCompleteAction |
  DeleteVersionAction |
  DeleteVersionCompleteAction |
  DeleteVersionErrorAction |
  GenerateNewVersionAction |
  GenerateNewVersionErrorAction |
  GenerateNewVersionCompleteAction |
  DuplicateWorkflowAction |
  DuplicateWorkflowCompleteAction |
  DuplicateWorkflowErrorAction |
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
  GetExecutionInfoErrorAction |
  CloseWorkflowExecutionInfoAction |
  ChangeOrderAction |
  ChangeVersionsOrderAction |
  RenameGroupAction |
  RenameGroupCompleteAction |
  RenameGroupErrorAction |
  RenameWorkflowAction |
  RenameWorkflowCompleteAction |
  RenameWorkflowErrorAction |
  ResetModalAction |
  MoveWorkflowAction |
  MoveWorkflowCompleteAction |
  MoveWorkflowErrorAction;
