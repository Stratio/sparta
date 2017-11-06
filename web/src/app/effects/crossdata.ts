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
import { Actions, Effect } from '@ngrx/effects';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import * as crossdataActions from 'actions/crossdata';
import { CrossdataService } from 'app/services';
import * as fromRoot from 'reducers';


@Injectable()
export class CrossdataEffect {

    @Effect()
    getCrossDataDatabases$: Observable<Action> = this.actions$
        .ofType(crossdataActions.GET_DATABASES).switchMap((response: any) => {
            return this.crossdataService.getCrossdataDatabases()
                .map((crossdataList: any) => {
                    return new crossdataActions.GetDatabasesCompleteAction(crossdataList);
                }).catch(function (error) {
                    return Observable.of(new crossdataActions.GetDatabasesErrorAction());
                });
        });

    @Effect()
    getCrossDataTables$: Observable<Action> = this.actions$
        .ofType(crossdataActions.LIST_CROSSDATA_TABLES).switchMap((response: any) => {
            return this.crossdataService.getCrossdataTables()
                .map((crossdataList: any) => {
                    return new crossdataActions.ListCrossdataTablesCompleteAction(crossdataList);
                }).catch(function (error) {
                    return Observable.of(new crossdataActions.ListCrossdataTablesErrorAction(''));
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
                return Observable.of(new crossdataActions.ListCrossdataTablesErrorAction(''));
            });
        });

    @Effect()
    getTableInfo$: Observable<Action> = this.actions$
        .ofType(crossdataActions.SELECT_TABLE)
        .map((action: any) => action.payload)
        .switchMap((data: any) => {
            return this.crossdataService.getCrossdataTablesInfo(data.name).map((tableInfo: any) => {
                return new crossdataActions.GetTableInfoCompleteAction({
                    tableName: data.name,
                    info: tableInfo
                });
            }).catch(function (error) {
                return Observable.of(new crossdataActions.GetTableInfoErrorAction(''));
            });
        });

    constructor(
        private actions$: Actions,
        private crossdataService: CrossdataService
    ) { }
}
