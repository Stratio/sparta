/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';
import { InputSchemaField, Join, OutputSchemaField, OutputSchemaFieldCopleteTable, InputSchema } from '@app/wizard/components/query-builder/models/schema-fields';
import { InnerJoinTypes } from '../query-builder.constants';

export enum QueryBuilderActions {
  SELECT_INPUT_SCHEMA_FIELD = '[Query Builder] Select input schema field',
  ADD_INPUT_FIELDS = '[Query Selector] Add inputs schema fields',
  ADD_OUTPUT_FIELDS = '[Query Selector] Add outputs schema fields',
  CHANGE_OUTPUT_FIELDS_ORDER = '[Query] Change output field order',
  CHANGE_PATHS_VISIBILITY = '[Query] Change paths visibility',
  RESET_QUERY_BUILDER_STATE = '[Query] Reset query builder state',
  ADD_JOIN = '[Query] Add join',
  CHANGE_JOIN_TYPE = '[Query] Change join type',
  DELETE_JOIN = '[Query] Delete join',
  SAVE_FILTER = '[Query] Save join',
  UPDATE_FIELD = '[Query] Update field',
  DELETE_OUTPUT_FIELD = '[Query] Delete output field',
  DELETE_ALL_OUTPUT_FIELD = '[Query] Delete all output field',
  REMOVE_SELECTED_INPUT_SCHEMAS = '[Query] Remove selected input schemas',
  TOGGLE_ORDER = '[Query] Toggle order',
  ADD_BACKUP = '[Query] Add backup',
  INIT_QUERY_BUILDER = '[Query] Init query builder',
  ADD_NEW_FIELD = '[Query] Add new field'
}


export class SelectInputSchemaFieldAction implements Action {
  readonly type = QueryBuilderActions.SELECT_INPUT_SCHEMA_FIELD;
  constructor(public selectedField: InputSchemaField) { }
}

export class AddOutputSchemaFieldsAction implements Action {
  readonly type = QueryBuilderActions.ADD_OUTPUT_FIELDS;
  constructor(public outputFields: {index: number, items: Array<OutputSchemaField | OutputSchemaFieldCopleteTable>}) { }
}

export class AddInputSchemaFieldsAction implements Action {
  readonly type = QueryBuilderActions.ADD_INPUT_FIELDS;
  constructor(public inputFields: Array<InputSchema>) { }
}


export class ChangeOutputSchemaFieldOrderAction implements Action {
  readonly type = QueryBuilderActions.CHANGE_OUTPUT_FIELDS_ORDER;
  constructor(public payload: any) { }
}

export class ChangePathsVisibilityAction implements Action {
  readonly type = QueryBuilderActions.CHANGE_PATHS_VISIBILITY;
  constructor(public visible: boolean) { }
}

export class AddJoin implements Action {
  readonly type = QueryBuilderActions.ADD_JOIN;
  constructor(public payload: Join) { }
}

export class ChangeJoinType implements Action {
  readonly type = QueryBuilderActions.CHANGE_JOIN_TYPE;
  constructor(public joinType: InnerJoinTypes) { }
}

export class DeleteJoin implements Action {
  readonly type = QueryBuilderActions.DELETE_JOIN;
  constructor() { }
}

export class SaveFilter implements Action {
  readonly type = QueryBuilderActions.SAVE_FILTER;
  constructor(public payload: string) { }
}

export class UpdateField implements Action {
  readonly type = QueryBuilderActions.UPDATE_FIELD;
  constructor(public payload: any) { }
}

export class DeleteOutputField implements Action {
  readonly type = QueryBuilderActions.DELETE_OUTPUT_FIELD;
  constructor(public position: number) { }
}

export class DeleteAllOutputFields implements Action {
  readonly type = QueryBuilderActions.DELETE_ALL_OUTPUT_FIELD;
}

export class AddNewField implements Action {
  readonly type = QueryBuilderActions.ADD_NEW_FIELD;
  constructor(public payload: any) { }
}

export class RemoveSelectedInputSchemas implements Action {
  readonly type = QueryBuilderActions.REMOVE_SELECTED_INPUT_SCHEMAS;
}

export class ToggleOrder implements Action {
  readonly type = QueryBuilderActions.TOGGLE_ORDER;
  constructor(public payload: any) { }
}

export class AddBackup implements Action {
  readonly type = QueryBuilderActions.ADD_BACKUP;
  constructor(public payload: any) { }
}

export class InitQueryBuilder implements Action {
  readonly type = QueryBuilderActions.INIT_QUERY_BUILDER;
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
