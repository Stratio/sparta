/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Injectable } from '@angular/core';

import { Observable, from, of } from 'rxjs';
import { switchMap, map } from 'rxjs/operators';

import * as crossdataActions from './../actions/crossdata';
import * as errorActions from 'actions/errors';

import { CrossdataService } from 'app/services';


@Injectable()
export class CrossdataEffect {

   @Effect()
   getCrossDataDatabases$: Observable<Action> = this.actions$
      .ofType(crossdataActions.GET_DATABASES).pipe(switchMap((response: any) => {
         return this.crossdataService.getCrossdataDatabases()
            .map((crossdataList: any) => {
               return new crossdataActions.GetDatabasesCompleteAction(crossdataList);
            }).catch(function (error) {
               return from([
                  new crossdataActions.GetDatabasesErrorAction(),
                  new errorActions.ServerErrorAction(error)
               ]);
            });
      }));

   @Effect()
   getCrossDataTables$: Observable<Action> = this.actions$
      .ofType(crossdataActions.LIST_CROSSDATA_TABLES).pipe(switchMap((response: any) => {
         return this.crossdataService.getCrossdataTables()
            .map((crossdataList: any) => {
               return new crossdataActions.ListCrossdataTablesCompleteAction(crossdataList);
            }).catch(function (error) {
               return from([
                  new crossdataActions.ListCrossdataTablesErrorAction(''),
                  new errorActions.ServerErrorAction(error)
               ]);
            });
      }));


   @Effect()
   executeQuery$: Observable<Action> = this.actions$
      .ofType(crossdataActions.EXECUTE_QUERY).pipe(switchMap((data: any) => {
         return this.crossdataService.executeCrossdataQuery(data.payload)
            .map((queryResponse: any) => {
               return new crossdataActions.ExecuteQueryCompleteAction(queryResponse);
            }).catch(function (error: any) {
               try {
                  const errorParsed: any = JSON.parse(error.error);
                  return of(new crossdataActions.ExecuteQueryErrorAction(errorParsed.exception));
               } catch (error) {
                  return of(new crossdataActions.ExecuteQueryErrorAction('Unknow error'));
               }
            });
      }));


   @Effect()
   getDatabaseTables$: Observable<Action> = this.actions$
      .ofType(crossdataActions.LIST_DATABASE_TABLES)
      .pipe(map((action: any) => action.payload))
      .pipe(switchMap((response: any) => this.crossdataService.getDatabaseTables({
         dbName: response
      }).map((crossdataList: any) => {
         return new crossdataActions.ListDatabaseTablesCompleteAction(crossdataList);
      }).catch(function (error) {
         return from([
            new crossdataActions.ListCrossdataTablesErrorAction(''),
            new errorActions.ServerErrorAction(error)
         ]);
      })));

   @Effect()
   getTableInfo$: Observable<Action> = this.actions$
      .ofType(crossdataActions.SELECT_TABLE)
      .pipe(map((action: any) => action.payload))
      .pipe(switchMap((data: any) => {
         return this.crossdataService.getCrossdataTablesInfo(data.name).map((tableInfo: any) =>
            new crossdataActions.GetTableInfoCompleteAction({
               tableName: data.name,
               info: tableInfo
            })
         ).catch(function (error) {
            return from([
               new crossdataActions.GetTableInfoErrorAction(''),
               new errorActions.ServerErrorAction(error)
            ]);
         });
      }));

   constructor(
      private actions$: Actions,
      private crossdataService: CrossdataService
   ) { }
}
