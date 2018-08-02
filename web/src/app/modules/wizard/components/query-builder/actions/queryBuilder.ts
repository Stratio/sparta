/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';
import { InputSchemaField, Join } from '@app/wizard/components/query-builder/models/SchemaFields';

export const SELECT_INPUT_SCHEMA_FIELD = '[Query Builder] Select input schema field';
export const ADD_INPUT_FIELDS = '[Query Selector] Add inputs schema fields';
export const ADD_OUTPUT_FIELDS = '[Query Selector] Add outputs schema fields';
export const CHANGE_OUTPUT_FIELDS_ORDER = '[Query] Change output field order';
export const CHANGE_PATHS_VISIBILITY = '[Query] Change paths visibility';
export const RESET_QUERY_BUILDER_STATE = '[Query] Reset query builder state';
export const ADD_JOIN = '[Query] Add join';
export const CHANGE_JOIN_TYPE = '[Query] Change join type';
export const DELETE_JOIN = '[Query] Delete join';
export const SAVE_FILTER = '[Query] Save join';
export const UPDATE_FIELD = '[Query] Update field';
export const DELETE_OUTPUT_FIELD = '[Query] Delete output field';
export const DELETE_ALL_OUTPUT_FIELD = '[Query] Delete all output field';
export const REMOVE_SELECTED_INPUT_SCHEMAS = '[Query] Remove selected input schemas';
export const TOGGLE_ORDER = '[Query] Toggle order';
export const ADD_BACKUP = '[Query] Add backup';
export const INIT_QUERY_BUILDER = '[Query] Init query builder';
export const ADD_NEW_FIELD = '[Query] Add new field';

export class SelectInputSchemaFieldAction implements Action {
    readonly type = SELECT_INPUT_SCHEMA_FIELD;
    constructor(public selectedField: InputSchemaField) { }
}

export class AddOutputSchemaFieldsAction implements Action {
    readonly type = ADD_OUTPUT_FIELDS;
    constructor(public payload: any) { }
}

export class AddInputSchemaFieldsAction implements Action {
   readonly type = ADD_INPUT_FIELDS;
   constructor(public payload: any) { }
}


export class ChangeOutputSchemaFieldOrderAction implements Action {
    readonly type = CHANGE_OUTPUT_FIELDS_ORDER;
    constructor(public payload: any) { }
}

export class ChangePathsVisibilityAction implements Action {
    readonly type = CHANGE_PATHS_VISIBILITY;
    constructor(public visible: boolean) { }
}

export class AddJoin implements Action {
   readonly type = ADD_JOIN;
   constructor(public payload: Join) { }
}

export class ChangeJoinType implements Action {
   readonly type = CHANGE_JOIN_TYPE;
   constructor(public payload: string) { }
}

export class DeleteJoin implements Action {
   readonly type = DELETE_JOIN;
   constructor() { }
}

export class SaveFilter implements Action {
   readonly type = SAVE_FILTER;
   constructor(public payload: string) { }
}

export class UpdateField implements Action {
   readonly type = UPDATE_FIELD;
   constructor(public payload: any) { }
}

export class DeleteOutputField implements Action {
   readonly type = DELETE_OUTPUT_FIELD;
   constructor(public payload: any) { }
}

export class DeleteAllOutputFields implements Action {
   readonly type = DELETE_ALL_OUTPUT_FIELD;
}

export class AddNewField implements Action {
   readonly type = ADD_NEW_FIELD;
   constructor(public payload: any) { }
}

export class RemoveSelectedInputSchemas implements Action {
   readonly type = REMOVE_SELECTED_INPUT_SCHEMAS;
}

export class ToggleOrder implements Action {
   readonly type = TOGGLE_ORDER;
   constructor(public payload: any) { }
}

export class AddBackup implements Action {
   readonly type = ADD_BACKUP;
   constructor(public payload: any) { }
}

export class InitQueryBuilder implements Action {
   readonly type = INIT_QUERY_BUILDER;
   constructor(public payload: any) { }
}


export type Actions = SelectInputSchemaFieldAction |
    AddOutputSchemaFieldsAction |
    AddInputSchemaFieldsAction |
    ChangeOutputSchemaFieldOrderAction |
    ChangePathsVisibilityAction |
    AddJoin |
    DeleteJoin |
    SaveFilter |
    UpdateField |
    RemoveSelectedInputSchemas |
    ToggleOrder |
    ChangeJoinType |
    DeleteOutputField |
    DeleteAllOutputFields |
    AddBackup |
    InitQueryBuilder |
    AddNewField;
