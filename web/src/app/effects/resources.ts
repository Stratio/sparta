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
import * as resourcesActions from 'actions/resources';
import { ResourcesService } from 'app/services';


@Injectable()
export class ResourcesEffect {


    @Effect()
    getPluginsList$: Observable<Action> = this.actions$
        .ofType(resourcesActions.LIST_PLUGINS).switchMap((response: any) => {
            return this.resourcesService.getPluginsList()
                .map((pluginsList: any) => {
                    return new resourcesActions.ListPluginsCompleteAction(pluginsList);
                }).catch(function (error) {
                    return Observable.of(new resourcesActions.ListPluginsErrorAction(''));
                });
        });

    @Effect()
    getDriversList$: Observable<Action> = this.actions$
        .ofType(resourcesActions.LIST_DRIVERS).switchMap((response: any) => {
            return this.resourcesService.getDriversList()
                .map((driversList: any) => {
                    return new resourcesActions.ListDriversCompleteAction(driversList);
                }).catch(function (error) {
                    return Observable.of(new resourcesActions.ListDriversErrorAction(''));
                });
        });

    @Effect()
    uploadDriver$: Observable<Action> = this.actions$
        .ofType(resourcesActions.UPLOAD_DRIVER).switchMap((data: any) => {
            return this.resourcesService.uploadDriver(data.payload)
                .mergeMap(() => {
                    return [new resourcesActions.UploadDriverCompleteAction(''), new resourcesActions.ListDriversAction()];
                }).catch(function (error) {
                    return Observable.of(new resourcesActions.UploadDriverErrorAction(''));
                });
        });

    @Effect()
    uploadPlugin$: Observable<Action> = this.actions$
        .ofType(resourcesActions.UPLOAD_PLUGIN).switchMap((data: any) => {
            return this.resourcesService.uploadPlugin(data.payload)
                .mergeMap(() => {
                    return [new resourcesActions.UploadPluginCompleteAction(''), new resourcesActions.ListPluginsAction()];
                }).catch(function (error) {
                    return Observable.of(new resourcesActions.UploadPluginErrorAction(''));
                });
        });
        
    @Effect()
    deletePlugin$: Observable<Action> = this.actions$
        .ofType(resourcesActions.DELETE_PLUGIN).switchMap((data: any) => {
            return this.resourcesService.deletePlugin(data.payload)
                .mergeMap(() => {
                    return [new resourcesActions.DeletePluginCompleteAction(''), new resourcesActions.ListPluginsAction()];
                }).catch(function (error) {
                    return Observable.of(new resourcesActions.DeletePluginErrorAction(''));
                });
        });

    @Effect()
    deleteDriver$: Observable<Action> = this.actions$
        .ofType(resourcesActions.DELETE_DRIVER).switchMap((data: any) => {
            return this.resourcesService.deleteDriver(data.payload)
                .mergeMap(() => {
                    return [new resourcesActions.DeleteDriverCompleteAction(''), new resourcesActions.ListDriversAction()];
                }).catch(function (error) {
                    return Observable.of(new resourcesActions.DeleteDriverErrorAction(''));
                });
        });

    constructor(
        private actions$: Actions,
        private resourcesService: ResourcesService
    ) { }
}
