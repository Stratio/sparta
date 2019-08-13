/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { cloneDeep as _cloneDeep } from 'lodash';

import * as queryBuilderActions from './../actions/query-builder';
import { OutputSchemaField, SelectedInputFields, JoinSchema, InputSchema, OutputSchemaFieldCopleteTable } from './../models/schema-fields';
import { InnerJoinTypes } from '../query-builder.constants';

export interface State {
  selectedInputSchemas: SelectedInputFields;
  outputSchemaFields: Array<OutputSchemaField | OutputSchemaFieldCopleteTable>;
  inputSchemaFields: Array<InputSchema>;
  showRelationPaths: boolean;
  join: JoinSchema;
  filter: string;
}

const initialState: State = {
  selectedInputSchemas: {},
  outputSchemaFields: [],
  inputSchemaFields: [],
  showRelationPaths: true,
  join: {
    type: InnerJoinTypes.Inner,
    joins: []
  },
  filter: ''
};

export function reducer(state: State = initialState, action: queryBuilderActions.Actions): State {
  switch (action.type) {
    case queryBuilderActions.QueryBuilderActions.SELECT_INPUT_SCHEMA_FIELD: {
      return {
        ...state,
        selectedInputSchemas: {
          ...state.selectedInputSchemas,
          [action.selectedField.table]: state.selectedInputSchemas[action.selectedField.table] ?
            (state.selectedInputSchemas[action.selectedField.table].find(field => field.column === action.selectedField.column && field.table === action.selectedField.table) ?
              state.selectedInputSchemas[action.selectedField.table].filter(field => !(field.column === action.selectedField.column && field.table === action.selectedField.table)) :
              [...state.selectedInputSchemas[action.selectedField.table], action.selectedField])
            : [action.selectedField]
        }
      };
    }
    case queryBuilderActions.QueryBuilderActions.ADD_OUTPUT_FIELDS: {
      const newSchema = [
        ...state.outputSchemaFields.slice(0, action.outputFields.index),
        ...action.outputFields.items,
        ...state.outputSchemaFields.slice(action.outputFields.index)];
      return {
        ...state,
        outputSchemaFields: newSchema,
        selectedInputSchemas: action.outputFields.items.length && action.outputFields.items[0].originFields.length ? {
          ...state.selectedInputSchemas,
          [action.outputFields.items[0].originFields[0].table]: []
        } : state.selectedInputSchemas
      };
    }
    case queryBuilderActions.QueryBuilderActions.ADD_INPUT_FIELDS: {
      return {
        ...state,
        inputSchemaFields: action.inputFields
      };
    }
    case queryBuilderActions.QueryBuilderActions.CHANGE_OUTPUT_FIELDS_ORDER: {
      const schemaFields = _cloneDeep(state.outputSchemaFields);
      schemaFields.splice(action.payload.newPosition, 0, schemaFields.splice(action.payload.oldPosition, 1)[0]);
      return {
        ...state,
        outputSchemaFields: schemaFields
      };
    }
    case queryBuilderActions.QueryBuilderActions.CHANGE_PATHS_VISIBILITY: {
      return {
        ...state,
        showRelationPaths: action.visible
      };
    }
    case queryBuilderActions.QueryBuilderActions.ADD_JOIN: {
      const newJoin = {
        ...state.join,
        joins: [
          ...state.join.joins,
          action.payload
        ]
      };
      return {
        ...state,
        join: newJoin,
        selectedInputSchemas: initialState.selectedInputSchemas
      };
    }
    case queryBuilderActions.QueryBuilderActions.CHANGE_JOIN_TYPE: {
      const newJoin = {
        ...state.join,
        type: action.joinType
      };
      return {
        ...state,
        join: newJoin,
        selectedInputSchemas: initialState.selectedInputSchemas
      };
    }
    case queryBuilderActions.QueryBuilderActions.DELETE_JOIN: {
      return {
        ...state,
        join: initialState.join
      };
    }
    case queryBuilderActions.QueryBuilderActions.SAVE_FILTER: {
      return {
        ...state,
        filter: action.payload
      };
    }

    case queryBuilderActions.QueryBuilderActions.UPDATE_FIELD: {
      const { field, position } = action.payload;
      return {
        ...state,
        outputSchemaFields: [
          ...state.outputSchemaFields.slice(0, position),
          field,
          ...state.outputSchemaFields.slice(position + 1)],
        selectedInputSchemas: initialState.selectedInputSchemas
      };
    }
    case queryBuilderActions.QueryBuilderActions.DELETE_OUTPUT_FIELD: {
      return {
        ...state,
        outputSchemaFields: [
          ...state.outputSchemaFields.slice(0, action.position),
          ...state.outputSchemaFields.slice(action.position + 1)],
        selectedInputSchemas: initialState.selectedInputSchemas
      };
    }
    case queryBuilderActions.QueryBuilderActions.DELETE_ALL_OUTPUT_FIELD: {
      return {
        ...state,
        outputSchemaFields: initialState.outputSchemaFields,
        selectedInputSchemas: initialState.selectedInputSchemas
      };
    }
    case queryBuilderActions.QueryBuilderActions.REMOVE_SELECTED_INPUT_SCHEMAS: {
      return {
        ...state,
        selectedInputSchemas: initialState.selectedInputSchemas
      };
    }
    case queryBuilderActions.QueryBuilderActions.TOGGLE_ORDER: {
      const { order, position } = action.payload;
      const field = { ...state.outputSchemaFields[position], order };
      return {
        ...state,
        outputSchemaFields: [
          ...state.outputSchemaFields.slice(0, position),
          field,
          ...state.outputSchemaFields.slice(position + 1)],
        selectedInputSchemas: initialState.selectedInputSchemas
      };
    }
    case queryBuilderActions.QueryBuilderActions.ADD_BACKUP: {
      return {
        ...state,
        ...action.payload
      };
    }
    case queryBuilderActions.QueryBuilderActions.INIT_QUERY_BUILDER: {
      return {
        ...state,
        ...initialState,
        inputSchemaFields: action.payload
      };
    }
    case queryBuilderActions.QueryBuilderActions.ADD_NEW_FIELD: {
      return {
        ...state,
        outputSchemaFields: [
          ...state.outputSchemaFields,
          action.payload
        ],
      };
    }
    default:
      return state;
  }
}
