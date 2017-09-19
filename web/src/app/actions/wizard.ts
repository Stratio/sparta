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
    SELECTED_CREATION_ENTITY: type('[Wizard] Selected creation entity'),
    DESELECTED_CREATION_ENTITY: type('[Wizard] Deselected creation entity'),
    TOGGLE_ENTITY_DETAILS: type('[Wizard] Toggle entity details'),
    SELECT_ENTITY: type('[Wizard] Select entity'),
    UNSELECT_ENTITY: type('[Wizard] Unselect entity'),
    EDIT_ENTITY: type('[Wizard] Edit entity'),
    HIDE_EDIT_ENTITY: type('[Wizard] Hide edit entity'),
    CREATE_NODE_RELATION:  type('[Wizard] Create node relation')
};

export class SelectedCreationEntityAction implements Action {
    type: any = actionTypes.SELECTED_CREATION_ENTITY;
    constructor(public payload: any) { }
}

export class DeselectedCreationEntityAction implements Action {
    type: any = actionTypes.DESELECTED_CREATION_ENTITY;
}

export class EditEntityAction implements Action {
    type: any = actionTypes.EDIT_ENTITY;
    constructor(public payload: any) { }
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

export class CreateNodeRelation implements Action {
    type: any = actionTypes.CREATE_NODE_RELATION;
    constructor(public payload: any) { }
}


export type Actions =
    SelectedCreationEntityAction |
    DeselectedCreationEntityAction |
    EditEntityAction |
    HideEditEntityAction |
    SelectEntityAction |
    UnselectEntityAction |
    ToggleDetailSidebarAction |
    CreateNodeRelation;