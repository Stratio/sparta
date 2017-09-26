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
    LIST_CROSSDATA_TABLES: type('[Crossdata] List crossdata tables'),
    LIST_CROSSDATA_TABLES_COMPLETE: type('[Crossdata] List crossdata tables complete'),
    LIST_CROSSDATA_TABLES_ERROR: type('[Crossdata] List crossdata tables error'),
    GET_TABLE_INFO: type('[Crossdata] Get Crossdata table info'),
    GET_TABLE_INFO_COMPLETE: type('[Crossdata] Get Crossdata table info complete'),
    GET_TABLE_INFO_ERROR: type('[Crossdata] Get Crossdata table info error'),
    EXECUTE_QUERY: type('[Crossdata] Execute query'),
    EXECUTE_QUERY_COMPLETE: type('[Crossdata] Execute query complete'),
    EXECUTE_QUERY_ERROR: type('[Crossdata] Execute query error')

};

export class ListCrossdataTablesAction implements Action {
    type: any = actionTypes.LIST_CROSSDATA_TABLES;

    constructor() { }
}

export class ListCrossdataTablesCompleteAction implements Action {
    type: any = actionTypes.LIST_CROSSDATA_TABLES_COMPLETE;

    constructor(public payload: any) { }
}


export class ListCrossdataTablesErrorAction implements Action {
    type: any = actionTypes.LIST_CROSSDATA_TABLES_ERROR;

    constructor(public payload: any) { }
}


export class ExecuteQueryAction implements Action {
    type: any = actionTypes.EXECUTE_QUERY;

    constructor(public payload: string) { }
}

export class ExecuteQueryCompleteAction implements Action {
    type: any = actionTypes.EXECUTE_QUERY_COMPLETE;

    constructor(public payload: any) { }
}


export class ExecuteQueryErrorAction implements Action {
    type: any = actionTypes.EXECUTE_QUERY_ERROR;

    constructor(public payload: any) { }
}


export class GetTableInfoAction implements Action {
    type: any = actionTypes.GET_TABLE_INFO;

    constructor(public payload: string) { }
}

export class GetTableInfoCompleteAction implements Action {
    type: any = actionTypes.GET_TABLE_INFO_COMPLETE;

    constructor(public payload: any) { }
}


export class GetTableInfoErrorAction implements Action {
    type: any = actionTypes.GET_TABLE_INFO_ERROR;

    constructor(public payload: any) { }
}

export type Actions =
    ListCrossdataTablesAction |
    ListCrossdataTablesCompleteAction |
    ListCrossdataTablesErrorAction |
    GetTableInfoAction |
    GetTableInfoCompleteAction |
    GetTableInfoErrorAction |
    ExecuteQueryAction |
    ExecuteQueryCompleteAction |
    ExecuteQueryErrorAction;
