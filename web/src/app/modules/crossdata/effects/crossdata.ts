/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Injectable } from '@angular/core';

import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/withLatestFrom';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/from';
import { Observable } from 'rxjs/Observable';
import { from } from 'rxjs/observable/from';

import * as crossdataActions from './../actions/crossdata';
import * as errorActions from 'actions/errors';

import { CrossdataService } from 'app/services';


@Injectable()
export class CrossdataEffect {

    @Effect()
    getCrossDataDatabases$: Observable<Action> = this.actions$
        .ofType(crossdataActions.GET_DATABASES).switchMap((response: any) => {
            return this.crossdataService.getCrossdataDatabases()
                .map((crossdataList: any) => {
                    return new crossdataActions.GetDatabasesCompleteAction(crossdataList);
                }).catch(function (error) {
                    return from([
                        new crossdataActions.GetDatabasesErrorAction(),
                        new errorActions.ServerErrorAction(error)
                    ]);
                });
        });

    @Effect()
    getCrossDataTables$: Observable<Action> = this.actions$
        .ofType(crossdataActions.LIST_CROSSDATA_TABLES).switchMap((response: any) => {
            return this.crossdataService.getCrossdataTables()
                .map((crossdataList: any) => {
                    return new crossdataActions.ListCrossdataTablesCompleteAction(crossdataList);
                }).catch(function (error) {
                    return from([
                        new crossdataActions.ListCrossdataTablesErrorAction(''),
                        new errorActions.ServerErrorAction(error)
                    ]);
                });
        });


    @Effect()
    executeQuery$: Observable<Action> = this.actions$
        .ofType(crossdataActions.EXECUTE_QUERY).switchMap((data: any) => {
            return this.crossdataService.executeCrossdataQuery(data.payload)
                .map((queryResponse: any) => {
                    return new crossdataActions.ExecuteQueryCompleteAction(queryResponse);
                }).catch(function (error: any) {
                    try {
                        const errorParsed: any = JSON.parse(error.error);
                        return Observable.of(new crossdataActions.ExecuteQueryErrorAction(errorParsed.exception));
                    } catch (error) {
                        return Observable.of(new crossdataActions.ExecuteQueryErrorAction('Unknow error'));
                    }
                });
        });


    @Effect()
    getDatabaseTables$: Observable<Action> = this.actions$
        .ofType(crossdataActions.LIST_DATABASE_TABLES)
        .map((action: any) => action.payload)
        .switchMap((response: any) => {
            return this.crossdataService.getDatabaseTables({
                dbName: response
            }).map((crossdataList: any) => {
                return new crossdataActions.ListDatabaseTablesCompleteAction(crossdataList);
            }).catch(function (error) {
                return from([
                    new crossdataActions.ListCrossdataTablesErrorAction(''),
                    new errorActions.ServerErrorAction(error)
                ]);
            });
        });

    @Effect()
    getTableInfo$: Observable<Action> = this.actions$
        .ofType(crossdataActions.SELECT_TABLE)
        .map((action: any) => action.payload)
        .switchMap((data: any) => {
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
        });

    constructor(
        private actions$: Actions,
        private crossdataService: CrossdataService
    ) { }
}
