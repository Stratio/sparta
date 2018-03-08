/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Injectable } from '@angular/core';
import { Action, Store } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import * as resourcesActions from 'actions/resources';
import { ResourcesService } from 'app/services';
import * as fromRoot from 'reducers';
import * as errorActions from 'actions/errors';

@Injectable()
export class ResourcesEffect {

    @Effect()
    getPluginsList$: Observable<Action> = this.actions$
        .ofType(resourcesActions.LIST_PLUGINS).switchMap((response: any) => {
            return this.resourcesService.getPluginsList()
                .map((pluginsList: any) => {
                    return new resourcesActions.ListPluginsCompleteAction(pluginsList);
                }).catch(function (error) {
                    return Observable.from([
                        new resourcesActions.ListPluginsErrorAction(''),
                        new errorActions.ServerErrorAction(error)
                    ]);
                });
        });

    @Effect()
    getDriversList$: Observable<Action> = this.actions$
        .ofType(resourcesActions.LIST_DRIVERS).switchMap((response: any) => {
            return this.resourcesService.getDriversList()
                .map((driversList: any) => {
                    return new resourcesActions.ListDriversCompleteAction(driversList);
                }).catch(function (error) {
                    return Observable.from([
                        new resourcesActions.ListDriversErrorAction(''),
                        new errorActions.ServerErrorAction(error)
                    ]);
                });
        });

    @Effect()
    uploadDriver$: Observable<Action> = this.actions$
        .ofType(resourcesActions.UPLOAD_DRIVER).switchMap((data: any) => {
            return this.resourcesService.uploadDriver(data.payload)
                .mergeMap(() => {
                    return [new resourcesActions.UploadDriverCompleteAction(''), new resourcesActions.ListDriversAction()];
                }).catch(function (error) {
                    return Observable.from([
                        new resourcesActions.UploadDriverErrorAction(''),
                        new errorActions.ServerErrorAction(error)
                    ]);
                });
        });

    @Effect()
    uploadPlugin$: Observable<Action> = this.actions$
        .ofType(resourcesActions.UPLOAD_PLUGIN).switchMap((data: any) => {
            return this.resourcesService.uploadPlugin(data.payload)
                .mergeMap(() => {
                    return [new resourcesActions.UploadPluginCompleteAction(''), new resourcesActions.ListPluginsAction()];
                }).catch(function (error) {
                    return Observable.from([
                        new resourcesActions.UploadPluginErrorAction(''),
                        new errorActions.ServerErrorAction(error)
                    ]);
                });
        });

    @Effect()
    deletePlugin$: Observable<Action> = this.actions$
        .ofType(resourcesActions.DELETE_PLUGIN)
        .withLatestFrom(this.store.select(state => state.resources))
        .switchMap(([payload, resources]: [any, any]) => {
            const joinObservables: Observable<any>[] = [];
            resources.selectedPlugins.forEach((fileName: string) => {
                joinObservables.push(this.resourcesService.deletePlugin(fileName));
            });
            return Observable.forkJoin(joinObservables).mergeMap(results => {
                return [new resourcesActions.DeletePluginCompleteAction(''), new resourcesActions.ListPluginsAction()];
            }).catch(function (error: any) {
                return Observable.from([
                    new resourcesActions.DeletePluginErrorAction(''),
                    new errorActions.ServerErrorAction(error)
                ]);
            });
        });


    @Effect()
    deleteDriver$: Observable<Action> = this.actions$
        .ofType(resourcesActions.DELETE_DRIVER).switchMap((data: any) => {
            return this.resourcesService.deleteDriver(data.payload)
                .mergeMap(() => {
                    return [new resourcesActions.DeleteDriverCompleteAction(''), new resourcesActions.ListDriversAction()];
                }).catch(function (error) {
                    return Observable.from([
                        new resourcesActions.DeleteDriverErrorAction(''),
                        new errorActions.ServerErrorAction(error)
                    ]);
                });
        });

    constructor(
        private actions$: Actions,
        private resourcesService: ResourcesService,
        private store: Store<fromRoot.State>
    ) { }
}
