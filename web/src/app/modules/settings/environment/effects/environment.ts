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

import { EnvironmentService } from 'services/environment.service';
import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Effect, Actions } from '@ngrx/effects';

import { Observable } from 'rxjs/Observable';
import { generateJsonFile } from 'utils';
import * as errorActions from 'actions/errors';
import * as environmentActions from './../actions/environment';


@Injectable()
export class EnvironmentEffect {

    @Effect()
    getEnvironmentList$: Observable<Action> = this.actions$
        .ofType(environmentActions.LIST_ENVIRONMENT).switchMap((response: any) => {
            return this.environmentService.getEnvironment()
                .map((environmentList: any) => {
                    return new environmentActions.ListEnvironmentCompleteAction(environmentList);
                }).catch(function (error: any) {
                    return Observable.from([
                        new environmentActions.ListEnvironmentErrorAction(''),
                        new errorActions.ServerErrorAction(error)
                    ]);
                });
        });


    @Effect()
    saveEnvironmentList$: Observable<Action> = this.actions$
        .ofType(environmentActions.SAVE_ENVIRONMENT).switchMap((response: any) => {
            return this.environmentService.updateEnvironment(response.payload)
                .map((environmentList: any) => {
                    return new environmentActions.ListEnvironmentAction();
                }).catch(function (error: any) {
                    return Observable.from([
                        new environmentActions.SaveEnvironmentErrorAction(''),
                        new errorActions.ServerErrorAction(error)
                    ]);
                });
        });

    @Effect()
    importEnvironment$: Observable<Action> = this.actions$
        .ofType(environmentActions.IMPORT_ENVIRONMENT).switchMap((response: any) => {
            return this.environmentService.importEnvironment(response.payload)
                .mergeMap((environmentList: any) => {
                    return [
                        new environmentActions.ImportEnvironmentCompleteAction(), 
                        new environmentActions.ListEnvironmentAction()
                    ];
                }).catch(function (error: any) {
                    return Observable.from([
                        new environmentActions.ImportEnvironmentErrorAction(),
                        new errorActions.ServerErrorAction(error)
                    ]);
                });
        });

    @Effect()
    exportEnvironment$: Observable<Action> = this.actions$
        .ofType(environmentActions.EXPORT_ENVIRONMENT).switchMap((response: any) => {
            return this.environmentService.exportEnvironment()
                .map((envData: any) => {
                    generateJsonFile('environment-data', envData);
                    return new environmentActions.ExportEnvironmentCompleteAction();
                }).catch(function (error: any) {
                    return Observable.from([
                        new environmentActions.ExportEnvironmentErrorAction(),
                        new errorActions.ServerErrorAction(error)
                    ]);
                });
        });

    constructor(
        private actions$: Actions,
        private environmentService: EnvironmentService
    ) { }

}
