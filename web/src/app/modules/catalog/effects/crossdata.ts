/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Injectable } from '@angular/core';

import { Observable, from, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

import * as crossdataActions from './../actions/crossdata';
import * as errorActions from 'actions/errors';

import { CrossdataService } from 'app/services';


@Injectable()
export class CrossdataEffect {

  @Effect()
  getCrossDataDatabases$: Observable<Action> = this.actions$
    .pipe(ofType(crossdataActions.GET_DATABASES))
    .pipe(switchMap((response: any) => {
      return this.crossdataService.getCrossdataDatabases()
        .pipe(map((crossdataList: any) => {
          return new crossdataActions.GetDatabasesCompleteAction(crossdataList);
        })).pipe(catchError(function (error) {
          return from([
            new crossdataActions.GetDatabasesErrorAction(),
            new errorActions.ServerErrorAction(error)
          ]);
        }));
    }));

  @Effect()
  getCrossDataTables$: Observable<Action> = this.actions$
    .pipe(ofType(crossdataActions.LIST_CROSSDATA_TABLES))
    .pipe(switchMap((response: any) => {
      return this.crossdataService.getCrossdataTables()
        .pipe(map((crossdataList: any) => {
          return new crossdataActions.ListCrossdataTablesCompleteAction(crossdataList);
        })).pipe(catchError(function (error) {
          return from([
            new crossdataActions.ListCrossdataTablesErrorAction(''),
            new errorActions.ServerErrorAction(error)
          ]);
        }));
    }));


  @Effect()
  executeQuery$: Observable<Action> = this.actions$
    .pipe(ofType(crossdataActions.EXECUTE_QUERY))
    .pipe(switchMap((data: any) => {
      return this.crossdataService.executeCrossdataQuery(data.payload)
        .pipe(map((queryResponse: any) => {
          return new crossdataActions.ExecuteQueryCompleteAction(queryResponse);
        })).pipe(catchError(function (error: any) {
          try {
            const errorParsed: any = JSON.parse(error.error);
            return of(new crossdataActions.ExecuteQueryErrorAction(errorParsed.exception));
          } catch (error) {
            return of(new crossdataActions.ExecuteQueryErrorAction('Unknow error'));
          }
        }));
    }));


  @Effect()
  getDatabaseTables$: Observable<Action> = this.actions$
    .pipe(ofType(crossdataActions.LIST_DATABASE_TABLES))
    .pipe(map((action: any) => action.payload))
    .pipe(switchMap((response: any) => this.crossdataService.getDatabaseTables({
      dbName: response
    }).pipe(map((crossdataList: any) => {
      return new crossdataActions.ListDatabaseTablesCompleteAction(crossdataList);
    })).pipe(catchError(function (error) {
      return from([
        new crossdataActions.ListCrossdataTablesErrorAction(''),
        new errorActions.ServerErrorAction(error)
      ]);
    }))));

  @Effect()
  getTableInfo$: Observable<Action> = this.actions$
    .pipe(ofType(crossdataActions.SELECT_TABLE))
    .pipe(map((action: any) => action.payload))
    .pipe(switchMap((data: any) => {
      return this.crossdataService.getCrossdataTablesInfo(data.name)
        .pipe(map((tableInfo: any) =>
          new crossdataActions.GetTableInfoCompleteAction({
            tableName: data.name,
            info: tableInfo
          })
        )).pipe(catchError(function (error) {
          return from([
            new crossdataActions.GetTableInfoErrorAction(''),
            new errorActions.ServerErrorAction(error)
          ]);
        }));
    }));

  constructor(
    private actions$: Actions,
    private crossdataService: CrossdataService
  ) { }
}
