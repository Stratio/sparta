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

export const LIST_CROSSDATA_TABLES = '[Crossdata] List crossdata tables';
export const LIST_CROSSDATA_TABLES_COMPLETE = '[Crossdata] List crossdata tables complete';
export const LIST_CROSSDATA_TABLES_ERROR = '[Crossdata] List crossdata tables error';
export const GET_DATABASES = '[Crossdata] Get databases';
export const GET_DATABASES_COMPLETE = '[Crossdata] Get databases complete';
export const GET_DATABASES_ERROR = '[Crossdata] Get databases error';
export const GET_TABLE_INFO = '[Crossdata] Get Crossdata table info';
export const GET_TABLE_INFO_COMPLETE = '[Crossdata] Get Crossdata table info complete';
export const CHANGE_TABLES_ORDER = '[Crossdata] Change tables order';
export const FILTER_TABLES = '[Crossdata] Filter tables';
export const GET_TABLE_INFO_ERROR = '[Crossdata] Get Crossdata table info error';
export const EXECUTE_QUERY = '[Crossdata] Execute query';
export const EXECUTE_QUERY_COMPLETE = '[Crossdata] Execute query complete';
export const EXECUTE_QUERY_ERROR = '[Crossdata] Execute query error';
export const SHOW_TEMPORARY_TABLES = '[Crossdata] Show temporary tables';
export const LIST_DATABASE_TABLES = '[Crossdata] List database tables';
export const LIST_DATABASE_TABLES_COMPLETE = '[Crossdata] List database tables complete';
export const LIST_DATABASE_TABLES_ERROR = '[Crossdata] List database tables error';
export const SELECT_DATABASE = '[Crossdata] Select database';
export const SELECT_TABLE = '[Crossdata] Select table';
export const UNSELECT_TABLE = '[Crossdata] Unselect table';


export class ListCrossdataTablesAction implements Action {
    readonly type = LIST_CROSSDATA_TABLES;

    constructor() { }
}

export class ListCrossdataTablesCompleteAction implements Action {
    readonly type = LIST_CROSSDATA_TABLES_COMPLETE;

    constructor(public payload: any) { }
}


export class ListCrossdataTablesErrorAction implements Action {
    readonly type = LIST_CROSSDATA_TABLES_ERROR;

    constructor(public payload: any) { }
}


export class ExecuteQueryAction implements Action {
    readonly type = EXECUTE_QUERY;

    constructor(public payload: string) { }
}

export class ExecuteQueryCompleteAction implements Action {
    readonly type = EXECUTE_QUERY_COMPLETE;

    constructor(public payload: any) { }
}


export class ExecuteQueryErrorAction implements Action {
    readonly type = EXECUTE_QUERY_ERROR;

    constructor(public payload: any) { }
}


export class GetTableInfoAction implements Action {
    readonly type = GET_TABLE_INFO;

    constructor(public payload: string) { }
}

export class GetTableInfoCompleteAction implements Action {
    readonly type = GET_TABLE_INFO_COMPLETE;

    constructor(public payload: any) { }
}


export class GetTableInfoErrorAction implements Action {
    readonly type = GET_TABLE_INFO_ERROR;

    constructor(public payload: any) { }
}

export class GetDatabasesAction implements Action {
    readonly type = GET_DATABASES;
}

export class GetDatabasesCompleteAction implements Action {
    readonly type = GET_DATABASES_COMPLETE;
    constructor(public payload: any) { }
}

export class GetDatabasesErrorAction implements Action {
    readonly type = GET_DATABASES_ERROR;
}

export class ShowTemporaryTablesAction implements Action {
    readonly type = SHOW_TEMPORARY_TABLES;
    constructor(public payload: any) { }
}

export class SelectDatabaseAction implements Action {
    readonly type = SELECT_DATABASE;
    constructor(public payload: any) { }
}

export class ListDatabaseTablesAction implements Action {
    readonly type = LIST_DATABASE_TABLES;
    constructor(public payload: any) { }
}

export class ListDatabaseTablesCompleteAction implements Action {
    readonly type = LIST_DATABASE_TABLES_COMPLETE;
    constructor(public payload: any) { }
}

export class ListDatabaseTablesErrorAction implements Action {
    readonly type = LIST_DATABASE_TABLES_ERROR;
}

export class ChangeTablesOrderAction implements Action {
  readonly type = CHANGE_TABLES_ORDER;
  constructor(public payload: any) { }
}

export class FilterTablesAction implements Action {
    readonly type = FILTER_TABLES;
    constructor(public payload: any) {}
}

export class SelectTableAction implements Action {
    readonly type = SELECT_TABLE;
    constructor(public payload: any) { }
}

export class UnselectTableAction implements Action {
    readonly type = UNSELECT_TABLE;
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
    ExecuteQueryErrorAction |
    GetDatabasesAction |
    SelectDatabaseAction |
    GetDatabasesCompleteAction |
    GetDatabasesErrorAction |
    ShowTemporaryTablesAction |
    ListDatabaseTablesAction |
    ListDatabaseTablesCompleteAction |
    ListDatabaseTablesErrorAction |
    UnselectTableAction |
    SelectTableAction |
    ChangeTablesOrderAction |
    FilterTablesAction;
