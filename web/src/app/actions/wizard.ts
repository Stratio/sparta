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

export const RESET_WIZARD = '[Wizard] Reset wizard';
export const MODIFY_WORKFLOW = '[Wizard] Modify workflow';
export const MODIFY_WORKFLOW_COMPLETE = '[Wizard] Modify workflow complete';
export const MODIFY_WORKFLOW_ERROR = '[Wizard] Modify workflow error';
export const SELECTED_CREATION_ENTITY = '[Wizard] Selected creation entity';
export const DESELECTED_CREATION_ENTITY = '[Wizard] Deselected creation entity';
export const TOGGLE_ENTITY_DETAILS = '[Wizard] Toggle entity details';
export const CHANGE_WORKFLOW_NAME = '[Wizard] Change workflow name';
export const SELECT_ENTITY = '[Wizard] Select entity';
export const UNSELECT_ENTITY = '[Wizard] Unselect entity';
export const EDIT_ENTITY = '[Wizard] Edit entity';
export const HIDE_EDIT_ENTITY = '[Wizard] Hide edit entity';
export const CREATE_NODE_RELATION = '[Wizard] Create node relation';
export const CREATE_NODE_RELATION_COMPLETE = '[Wizard] Create node relation complete';
export const CREATE_NODE_RELATION_ERROR = '[Wizard] Create node relation error';
export const DELETE_NODE_RELATION = '[Wizard] delete node relation';
export const CREATE_ENTITY = '[Wizard] create entity';
export const CREATE_ENTITY_COMPLETE = '[Wizard] create entity complete';
export const DELETE_ENTITY = '[Wizard] Delete entity';
export const SAVE_WORKFLOW_POSITIONS = '[Wizard] Save workflow positions';
export const SAVE_EDITOR_POSITION = '[Wizard] Save editor position';
export const SHOW_EDITOR_CONFIG = '[Wizard] Show editor config';
export const HIDE_EDITOR_CONFIG = '[Wizard] Hide editor config';
export const SAVE_ENTITY = '[Wizard] Save entity';
export const SAVE_ENTITY_COMPLETE = '[Wizard] Save entity complete';
export const SAVE_ENTITY_ERROR = '[Wizard] Save entity error';
export const SAVE_SETTINGS = '[Wizard] Save settings';
export const SAVE_WORKFLOW = '[Wizard] Save workflow';
export const SAVE_WORKFLOW_COMPLETE = '[Wizard] Save workflow complete';
export const SAVE_WORKFLOW_ERROR = '[Wizard] Save workflow error';
export const SELECT_SEGMENT = '[Wizard] select segment';
export const UNSELECT_SEGMENT = '[Wizard] Unselect segment';
export const DELETE_SEGMENT = '[Wizard] Delete segment';
export const SEARCH_MENU_OPTION = '[Wizard] Search floating menu option';
export const UNDO_CHANGES = '[Wizard] Undo changes';
export const REDO_CHANGES = '[Wizard] Redo changes';
export const HIDE_EDIT = '[Wizard] Hide edit';

export class ResetWizardAction implements Action {
    readonly type = RESET_WIZARD;
}

export class ModifyWorkflowAction implements Action {
    readonly type = MODIFY_WORKFLOW;
    constructor(public payload: any) { }
}

export class ModifyWorkflowCompleteAction implements Action {
    readonly type = MODIFY_WORKFLOW_COMPLETE;
    constructor(public payload: any) { }
}

export class ModifyWorkflowErrorAction implements Action {
    readonly type = MODIFY_WORKFLOW_ERROR;
    constructor(public payload: any) { }
}

export class SelectedCreationEntityAction implements Action {
    readonly type = SELECTED_CREATION_ENTITY;
    constructor(public payload: any) { }
}

export class DeselectedCreationEntityAction implements Action {
    readonly type = DESELECTED_CREATION_ENTITY;
}

export class ChangeWorkflowNameAction implements Action {
    readonly type = CHANGE_WORKFLOW_NAME;
    constructor(public payload: any) { }
}

export class EditEntityAction implements Action {
    readonly type = EDIT_ENTITY;
}

export class HideEditEntityAction implements Action {
    readonly type = HIDE_EDIT;
}

export class SelectEntityAction implements Action {
    readonly type = SELECT_ENTITY;
    constructor(public payload: any) { }
}

export class UnselectEntityAction implements Action {
    readonly type = UNSELECT_ENTITY;
}

export class ToggleDetailSidebarAction implements Action {
    readonly type = TOGGLE_ENTITY_DETAILS;
}

export class CreateNodeRelationAction implements Action {
    readonly type = CREATE_NODE_RELATION;
    constructor(public payload: any) { }
}

export class CreateNodeRelationCompleteAction implements Action {
    readonly type = CREATE_NODE_RELATION_COMPLETE;
    constructor(public payload: any) { }
}

export class CreateNodeRelationErrorAction implements Action {
    readonly type = CREATE_NODE_RELATION_ERROR;
    constructor(public payload: any) { }
}

export class DeleteNodeRelationAction implements Action {
    readonly type = DELETE_NODE_RELATION;
    constructor(public payload: any) { }
}

export class DeleteEntityAction implements Action {
    readonly type = DELETE_ENTITY;
}

export class CreateEntityAction implements Action {
    readonly type = CREATE_ENTITY;
    constructor(public payload: any) { }
}

export class CreateEntityCompleteAction implements Action {
    readonly type = CREATE_ENTITY_COMPLETE;
    constructor(public payload: any) { }
}

export class SaveWorkflowPositionsAction implements Action {
    readonly type = SAVE_WORKFLOW_POSITIONS;
    constructor(public payload: any) { }
}

export class SaveEditorPosition implements Action {
    readonly type = SAVE_EDITOR_POSITION;
    constructor(public payload: any) { }
}

export class ShowEditorConfigAction implements Action {
    readonly type = SHOW_EDITOR_CONFIG;
    constructor(public payload: any) { }
}

export class HideEditorConfigAction implements Action {
    readonly type = HIDE_EDITOR_CONFIG;
}

export class SaveSettingsAction implements Action {
    readonly type = SAVE_SETTINGS;
    constructor(public payload: any) { }
}

export class SaveEntityAction implements Action {
    readonly type = SAVE_ENTITY;
    constructor(public payload: any) { }
}

export class SaveEntityCompleteAction implements Action {
    readonly type = SAVE_ENTITY_COMPLETE;
    constructor(public payload: any) { }
}


export class SaveEntityErrorAction implements Action {
    readonly type = SAVE_ENTITY_ERROR;
    constructor(public payload: any) { }
}

export class SaveWorkflowAction implements Action {
    readonly type = SAVE_WORKFLOW;
}

export class SaveWorkflowCompleteAction implements Action {
    readonly type = SAVE_WORKFLOW_COMPLETE;
    constructor(public payload: any) { }
}

export class SaveWorkflowErrorAction implements Action {
    readonly type = SAVE_WORKFLOW_ERROR;
    constructor(public payload: any) { }
}

export class SelectSegmentAction implements Action {
    readonly type = SELECT_SEGMENT;
    constructor(public payload: any) { }
}

export class UnselectSegmentAction implements Action {
    readonly type = UNSELECT_SEGMENT;
}

export class SearchFloatingMenuAction implements Action {
    readonly type = SEARCH_MENU_OPTION;
    constructor(public payload: any) { }
}

export class UndoChangesAction implements Action {
    readonly type = UNDO_CHANGES;
}

export class RedoChangesAction implements Action {
    readonly type = REDO_CHANGES;
}

export type Actions =
    ResetWizardAction |
    ModifyWorkflowAction |
    ModifyWorkflowCompleteAction |
    ModifyWorkflowErrorAction |
    SelectedCreationEntityAction |
    DeselectedCreationEntityAction |
    ChangeWorkflowNameAction |
    EditEntityAction |
    HideEditEntityAction |
    SelectEntityAction |
    UnselectEntityAction |
    ToggleDetailSidebarAction |
    CreateNodeRelationAction |
    CreateNodeRelationErrorAction |
    CreateNodeRelationCompleteAction |
    DeleteNodeRelationAction |
    DeleteEntityAction |
    SaveWorkflowPositionsAction |
    ShowEditorConfigAction |
    HideEditorConfigAction |
    SaveEntityAction |
    SaveEntityCompleteAction |
    SaveEntityErrorAction |
    SaveSettingsAction |
    SaveWorkflowAction |
    SaveWorkflowCompleteAction |
    SaveWorkflowErrorAction |
    SearchFloatingMenuAction |
    UndoChangesAction |
    RedoChangesAction |
    CreateEntityAction |
    CreateEntityCompleteAction;
