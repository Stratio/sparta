/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as crossdata from '../actions/crossdata';

export interface CrossdataInputState {
 databases: Array<string>;
 databasetables: { [databaseName: string]: Array<string> };
}

const initialState: CrossdataInputState = {
 databases: [],
 databasetables: {}
};

export function reducer(state: CrossdataInputState = initialState, action: crossdata.CrossdataActionsUnion): CrossdataInputState {
 switch (action.type) {
   case crossdata.CrossdataInputActions.GET_DATABASES_COMPLETE:
     return {
       ...state,
       databases: action.databases
     };
   case crossdata.CrossdataInputActions.GET_DATABASE_TABLES_COMPLETE:
     return {
       ...state,
       databasetables: {
         ...state.databasetables,
         [action.database]: action.tables
       }
     };
   default:
     return state;
 }
}
