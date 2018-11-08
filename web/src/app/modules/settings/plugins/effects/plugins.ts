/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { Action, Store } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';

import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/withLatestFrom';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/from';
import { from, Observable, forkJoin } from 'rxjs';

import * as pluginsActions from './../actions/plugins';
import { ResourcesService } from 'app/services';
import * as fromRoot from './../reducers';
import * as errorActions from 'actions/errors';

@Injectable()
export class PluginsEffect {

   @Effect()
   getPluginsList$: Observable<Action> = this.actions$
      .ofType(pluginsActions.LIST_PLUGINS).switchMap((response: any) => {
         return this.resourcesService.getPluginsList()
            .map((pluginsList: any) => {
               return new pluginsActions.ListPluginsCompleteAction(pluginsList);
            }).catch(function (error) {
               return from([
                  new pluginsActions.ListPluginsErrorAction(''),
                  new errorActions.ServerErrorAction(error)
               ]);
            });
      });

   @Effect()
   getDriversList$: Observable<Action> = this.actions$
      .ofType(pluginsActions.LIST_DRIVERS).switchMap((response: any) => {
         return this.resourcesService.getDriversList()
            .map((driversList: any) => {
               return new pluginsActions.ListDriversCompleteAction(driversList);
            }).catch(function (error) {
               return from([
                  new pluginsActions.ListDriversErrorAction(''),
                  new errorActions.ServerErrorAction(error)
               ]);
            });
      });

   @Effect()
   uploadDriver$: Observable<Action> = this.actions$
      .ofType(pluginsActions.UPLOAD_DRIVER).switchMap((data: any) => {
         return this.resourcesService.uploadDriver(data.payload)
            .mergeMap(() => {
               return [new pluginsActions.UploadDriverCompleteAction(''), new pluginsActions.ListDriversAction()];
            }).catch(function (error) {
               return from([
                  new pluginsActions.UploadDriverErrorAction(''),
                  new errorActions.ServerErrorAction(error)
               ]);
            });
      });

   @Effect()
   uploadPlugin$: Observable<Action> = this.actions$
      .ofType(pluginsActions.UPLOAD_PLUGIN).switchMap((data: any) => {
         return this.resourcesService.uploadPlugin(data.payload)
            .mergeMap(() => {
               return [new pluginsActions.UploadPluginCompleteAction(''), new pluginsActions.ListPluginsAction()];
            }).catch(function (error) {
               return from([
                  new pluginsActions.UploadPluginErrorAction(''),
                  new errorActions.ServerErrorAction(error)
               ]);
            });
      });

   @Effect()
   deletePlugin$: Observable<Action> = this.actions$
      .ofType(pluginsActions.DELETE_PLUGIN)
      .withLatestFrom(this.store.select(state => state.plugins.plugins))
      .switchMap(([payload, resources]: [any, any]) => {
         const joinObservables: Observable<any>[] = [];
         resources.selectedPlugins.forEach((fileName: string) => {
            joinObservables.push(this.resourcesService.deletePlugin(fileName));
         });
         return forkJoin(joinObservables).mergeMap(results => {
            return [new pluginsActions.DeletePluginCompleteAction(''), new pluginsActions.ListPluginsAction()];
         }).catch(function (error: any) {
            return from([
               new pluginsActions.DeletePluginErrorAction(''),
               new errorActions.ServerErrorAction(error)
            ]);
         });
      });


   @Effect()
   deleteDriver$: Observable<Action> = this.actions$
      .ofType(pluginsActions.DELETE_DRIVER).switchMap((data: any) => {
         return this.resourcesService.deleteDriver(data.payload)
            .mergeMap(() => {
               return [new pluginsActions.DeleteDriverCompleteAction(''), new pluginsActions.ListDriversAction()];
            }).catch((error) => from([
               new pluginsActions.DeleteDriverErrorAction(''),
               new errorActions.ServerErrorAction(error)
            ]));
      });

   constructor(
      private actions$: Actions,
      private resourcesService: ResourcesService,
      private store: Store<fromRoot.State>
   ) { }
}
