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
    RESET_WIZARD: type('[Wizard] Reset wizard'),
    MODIFY_WORKFLOW: type('[Wizard] Modify workflow'),
    MODIFY_WORKFLOW_COMPLETE: type('[Wizard] Modify workflow complete'),
    MODIFY_WORKFLOW_ERROR: type('[Wizard] Modify workflow error'),
    SELECTED_CREATION_ENTITY: type('[Wizard] Selected creation entity'),
    DESELECTED_CREATION_ENTITY: type('[Wizard] Deselected creation entity'),
    TOGGLE_ENTITY_DETAILS: type('[Wizard] Toggle entity details'),
    CHANGE_WORKFLOW_NAME: type('[Wizard] Change workflow name'),
    SELECT_ENTITY: type('[Wizard] Select entity'),
    UNSELECT_ENTITY: type('[Wizard] Unselect entity'),
    EDIT_ENTITY: type('[Wizard] Edit entity'),
    HIDE_EDIT_ENTITY: type('[Wizard] Hide edit entity'),
    CREATE_NODE_RELATION: type('[Wizard] Create node relation'),
    CREATE_NODE_RELATION_COMPLETE: type('[Wizard] Create node relation complete'),
    CREATE_NODE_RELATION_ERROR: type('[Wizard] Create node relation error'),
    DELETE_NODE_RELATION: type('[Wizard] delete node relation'),
    DELETE_ENTITY: type('[Wizard] Delete entity'),
    SAVE_WORKFLOW_POSITIONS: type('[Wizard] Save workflow positions'),
    SAVE_EDITOR_POSITION: type('[Wizard] Save editor position'),
    SHOW_EDITOR_CONFIG: type('[Wizard] Show editor config'),
    HIDE_EDITOR_CONFIG: type('[Wizard] Hide editor config'),
    SAVE_ENTITY: type('[Wizard] Save entity'),
    SAVE_ENTITY_COMPLETE: type('[Wizard] Save entity complete'),
    SAVE_ENTITY_ERROR: type('[Wizard] Save entity error'),
    SAVE_SETTINGS: type('[Wizard] Save settings'),
    SAVE_WORKFLOW: type('[Wizard] Save workflow'),
    SAVE_WORKFLOW_COMPLETE: type('[Wizard] Save workflow complete'),
    SAVE_WORKFLOW_ERROR: type('[Wizard] Save workflow error'),
};

export class ResetWizardAction implements Action {
    type: any = actionTypes.RESET_WIZARD;
}

export class ModifyWorkflowAction implements Action {
    type: any = actionTypes.MODIFY_WORKFLOW;
    constructor(public payload: any) { }
}

export class ModifyWorkflowCompleteAction implements Action {
    type: any = actionTypes.MODIFY_WORKFLOW_COMPLETE;
    constructor(public payload: any) { }
}

export class ModifyWorkflowErrorAction implements Action {
    type: any = actionTypes.MODIFY_WORKFLOW_ERROR;
    constructor(public payload: any) { }
}

export class SelectedCreationEntityAction implements Action {
    type: any = actionTypes.SELECTED_CREATION_ENTITY;
    constructor(public payload: any) { }
}

export class DeselectedCreationEntityAction implements Action {
    type: any = actionTypes.DESELECTED_CREATION_ENTITY;
}

export class ChangeWorkflowNameAction implements Action {
    type: any = actionTypes.CHANGE_WORKFLOW_NAME;
    constructor(public payload: any) { }
}

export class EditEntityAction implements Action {
    type: any = actionTypes.EDIT_ENTITY;
}

export class HideEditEntityAction implements Action {
    type: any = actionTypes.HIDE_EDIT;
}

export class SelectEntityAction implements Action {
    type: any = actionTypes.SELECT_ENTITY;
    constructor(public payload: any) { }
}

export class UnselectEntityAction implements Action {
    type: any = actionTypes.UNSELECT_ENTITY;
}

export class ToggleDetailSidebarAction implements Action {
    type: any = actionTypes.TOGGLE_ENTITY_DETAILS;
}

export class CreateNodeRelationAction implements Action {
    type: any = actionTypes.CREATE_NODE_RELATION;
    constructor(public payload: any) { }
}

export class CreateNodeRelationCompleteAction implements Action {
    type: any = actionTypes.CREATE_NODE_RELATION_COMPLETE;
    constructor(public payload: any) { }
}

export class CreateNodeRelationErrorAction implements Action {
    type: any = actionTypes.CREATE_NODE_RELATION_ERROR;
    constructor(public payload: any) { }
}

export class DeleteNodeRelationAction implements Action {
    type: any = actionTypes.DELETE_NODE_RELATION;
    constructor(public payload: any) { }
}

export class DeleteEntityAction implements Action {
    type: any = actionTypes.DELETE_ENTITY;
}

export class SaveWorkflowPositionsAction implements Action {
    type: any = actionTypes.SAVE_WORKFLOW_POSITIONS;
    constructor(public payload: any) { }
}

export class SaveEditorPosition implements Action {
    type: any = actionTypes.SAVE_EDITOR_POSITION;
    constructor(public payload: any) { }
}

export class ShowEditorConfigAction implements Action {
    type: any = actionTypes.SHOW_EDITOR_CONFIG;
    constructor(public payload: any) { }
}

export class HideEditorConfigAction implements Action {
    type: any = actionTypes.HIDE_EDITOR_CONFIG;
}

export class SaveSettingsAction implements Action {
    type: any = actionTypes.SAVE_SETTINGS;
    constructor(public payload: any) { }
}

export class SaveEntityAction implements Action {
    type: any = actionTypes.SAVE_ENTITY;
    constructor(public payload: any) { }
}

export class SaveEntityCompleteAction implements Action {
    type: any = actionTypes.SAVE_ENTITY_COMPLETE;
    constructor(public payload: any) { }
}


export class SaveEntityErrorAction implements Action {
    type: any = actionTypes.SAVE_ENTITY_ERROR;
    constructor(public payload: any) { }
}

export class SaveWorkflowAction implements Action {
    type: any = actionTypes.SAVE_WORKFLOW;
}

export class SaveWorkflowCompleteAction implements Action {
    type: any = actionTypes.SAVE_WORKFLOW_COMPLETE;
    constructor(public payload: any) { }
}

export class SaveWorkflowErrorAction implements Action {
    type: any = actionTypes.SAVE_WORKFLOW_ERROR;
    constructor(public payload: any) { }
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
    SaveWorkflowErrorAction;
