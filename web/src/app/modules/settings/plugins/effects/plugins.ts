/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { Action, Store, select } from '@ngrx/store';
import { Actions, Effect, ofType } from '@ngrx/effects';

import { from, Observable, forkJoin } from 'rxjs';
import { catchError, map, mergeMap, switchMap, withLatestFrom } from 'rxjs/operators';

import * as pluginsActions from './../actions/plugins';
import { ResourcesService } from 'app/services';
import * as fromRoot from './../reducers';
import * as errorActions from 'actions/errors';

@Injectable()
export class PluginsEffect {

  @Effect()
  getPluginsList$: Observable<Action> = this.actions$
    .pipe(ofType(pluginsActions.LIST_PLUGINS))
    .pipe(switchMap((response: any) => {
      return this.resourcesService.getPluginsList()
        .pipe(map((pluginsList: any) => {
          return new pluginsActions.ListPluginsCompleteAction(pluginsList);
        })).pipe(catchError(function (error) {
          return from([
            new pluginsActions.ListPluginsErrorAction(''),
            new errorActions.ServerErrorAction(error)
          ]);
        }));
    }));

  @Effect()
  getDriversList$: Observable<Action> = this.actions$
    .pipe(ofType(pluginsActions.LIST_DRIVERS))
    .pipe(switchMap((response: any) => {
      return this.resourcesService.getDriversList()
        .pipe(map((driversList: any) => {
          return new pluginsActions.ListDriversCompleteAction(driversList);
        })).pipe(catchError(function (error) {
          return from([
            new pluginsActions.ListDriversErrorAction(''),
            new errorActions.ServerErrorAction(error)
          ]);
        }));
    }));

  @Effect()
  uploadDriver$: Observable<Action> = this.actions$
    .pipe(ofType(pluginsActions.UPLOAD_DRIVER))
    .pipe(switchMap((data: any) => {
      return this.resourcesService.uploadDriver(data.payload)
        .pipe(mergeMap(() => {
          return [new pluginsActions.UploadDriverCompleteAction(''), new pluginsActions.ListDriversAction()];
        })).pipe(catchError(function (error) {
          return from([
            new pluginsActions.UploadDriverErrorAction(''),
            new errorActions.ServerErrorAction(error)
          ]);
        }));
    }));

  @Effect()
  uploadPlugin$: Observable<Action> = this.actions$
    .pipe(ofType(pluginsActions.UPLOAD_PLUGIN))
    .pipe(switchMap((data: any) => {
      return this.resourcesService.uploadPlugin(data.payload)
        .pipe(mergeMap(() => {
          return [new pluginsActions.UploadPluginCompleteAction(''), new pluginsActions.ListPluginsAction()];
        })).pipe(catchError(function (error) {
          return from([
            new pluginsActions.UploadPluginErrorAction(''),
            new errorActions.ServerErrorAction(error)
          ]);
        }));
    }));

  @Effect()
  deletePlugin$: Observable<Action> = this.actions$
    .pipe(ofType(pluginsActions.DELETE_PLUGIN))
    .pipe(withLatestFrom(this.store.pipe(select((state: any) => state.plugins.plugins))))
    .pipe(switchMap(([payload, resources]: [any, any]) => {
      const joinObservables: Observable<any>[] = [];
      resources.selectedPlugins.forEach((fileName: string) => {
        joinObservables.push(this.resourcesService.deletePlugin(fileName));
      });
      return forkJoin(joinObservables).pipe(mergeMap(results => {
        return [new pluginsActions.DeletePluginCompleteAction(''), new pluginsActions.ListPluginsAction()];
      })).pipe(catchError(function (error: any) {
        return from([
          new pluginsActions.DeletePluginErrorAction(''),
          new errorActions.ServerErrorAction(error)
        ]);
      }));
    }));


  @Effect()
  deleteDriver$: Observable<Action> = this.actions$
    .pipe(ofType(pluginsActions.DELETE_DRIVER))
    .pipe(switchMap((data: any) => {
      return this.resourcesService.deleteDriver(data.payload)
        .pipe(mergeMap(() => {
          return [new pluginsActions.DeleteDriverCompleteAction(''), new pluginsActions.ListDriversAction()];
        })).pipe(catchError((error) => from([
          new pluginsActions.DeleteDriverErrorAction(''),
          new errorActions.ServerErrorAction(error)
        ])));
    }));

  constructor(
    private actions$: Actions,
    private resourcesService: ResourcesService,
    private store: Store<fromRoot.State>
  ) { }
}
