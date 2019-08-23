/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Action } from '@ngrx/store';

export enum CrossdataInputActions {
 GET_DATABASES = '[Crossdata-input] Get databases',
 GET_DATABASES_COMPLETE = '[Crossdata-input] Get databases complete',
 GET_DATABASES_ERROR = '[Crossdata-input] Get databases error',

  GET_DATABASE_TABLES = '[Crossdata-input] Get database tables',
 GET_DATABASE_TABLES_COMPLETE = '[Crossdata-input] Get database tables complete',
 GET_DATABASE_TABLES_ERROR = '[Crossdata-input] Get database tables error',
}

export class GetDatabases implements Action {
 readonly type = CrossdataInputActions.GET_DATABASES;
}

export class GetDatabasesComplete implements Action {
 readonly type = CrossdataInputActions.GET_DATABASES_COMPLETE;
 constructor(public databases: Array<string>) { }
}

export class GetDatabasesError implements Action {
 readonly type = CrossdataInputActions.GET_DATABASES_ERROR;
}

export class GetDatabaseTables implements Action {
 readonly type = CrossdataInputActions.GET_DATABASE_TABLES;
 constructor(public database: string) {}
}

export class GetDatabaseTablesComplete implements Action {
 readonly type = CrossdataInputActions.GET_DATABASE_TABLES_COMPLETE;
 constructor(public database: string, public tables: Array<string>) {}
}

export class GetDatabaseTablesError implements Action {
 readonly type = CrossdataInputActions.GET_DATABASE_TABLES_ERROR;
}

export type CrossdataActionsUnion = GetDatabases
 | GetDatabasesComplete
 | GetDatabasesError
 | GetDatabaseTables
 | GetDatabaseTablesComplete
 | GetDatabaseTablesError;

