/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';
import { Effect, ofType, Actions } from '@ngrx/effects';
import { Injectable } from '@angular/core';

 import { Observable, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

 import * as crossdataActions from './../actions/crossdata';
import { CrossdataService } from 'app/services/api/crossdata.service';

 @Injectable()
export class CrossdataInputEffect {

   @Effect()
  getCrossDataDatabases$: Observable<Action> = this._actions$
    .pipe(
      ofType(crossdataActions.CrossdataInputActions.GET_DATABASES),
      switchMap(() => this._crossdataService.getCrossdataDatabases()
        .pipe(
          map((crossdataList: any) => new crossdataActions.GetDatabasesComplete(
            crossdataList.map(database => database.name))
          ),
          catchError(() => of(new crossdataActions.GetDatabasesError()))
        )
      )
    );

   @Effect()
  getDatabaseTables$: Observable<Action> = this._actions$
    .pipe(
      ofType<crossdataActions.GetDatabaseTables>(crossdataActions.CrossdataInputActions.GET_DATABASE_TABLES),
      map((action) => action.database),
      switchMap((database) => this._crossdataService.getDatabaseTables({
        dbName: database
      }).pipe(
        map((tables: any) => new crossdataActions.GetDatabaseTablesComplete(database, tables.map(table => table.name))),
        catchError((error) => of(new crossdataActions.GetDatabaseTablesError()))
      ))
    );

   constructor(
    private _actions$: Actions,
    private _crossdataService: CrossdataService
  ) { }
}
