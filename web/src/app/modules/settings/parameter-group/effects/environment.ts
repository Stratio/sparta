/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/if';
import 'rxjs/add/observable/throw';
import { of } from 'rxjs/observable/of';
import { Observable } from 'rxjs/Observable';

import { Injectable } from '@angular/core';
import { Store, Action } from '@ngrx/store';
import { Effect, Actions } from '@ngrx/effects';

import * as fromParameters from './../reducers';
import * as environmentParametersActions from './../actions/environment';
import * as alertParametersActions from './../actions/alert';

import { ParametersService } from 'app/services';

@Injectable()
export class EnviromentParametersEffect {

   @Effect()
   getEnvironmentParameters$: Observable<any> = this._actions$
      .ofType(environmentParametersActions.LIST_ENVIRONMENT_PARAMS)
      .switchMap(() => this._parametersService.getEnvironmentAndContext()
         .mergeMap(response => [
            new environmentParametersActions.ListEnvironmentParamsCompleteAction(response),
            new alertParametersActions.HideLoadingAction()
         ])
         .catch(error => of(new alertParametersActions.HideLoadingAction())));

   @Effect()
   saveContext$: Observable<any> = this._actions$
      .ofType(environmentParametersActions.ADD_CONTEXT)
      .map((action: any) => action.payload)
      .withLatestFrom(this._store.select(state => state.parameterGroup.environment))
      .switchMap(([name, state]) => {
         const { environmentVariables: parameters, list: { name: parent } } = state;
         const newContext = { name, parent, parameters };

         return this._parametersService.createParamList(newContext)
            .mergeMap(context => [
               new environmentParametersActions.AddContextCompleteAction({ name: context.name, id: context.id }),
               new alertParametersActions.ShowAlertAction('Context saved')
            ])
            .catch(error => of(new alertParametersActions.ShowAlertAction('Context can not save')));
      });

   @Effect()
   saveEnvironment$: Observable<any> = this._actions$
      .ofType(environmentParametersActions.SAVE_PARAM)
      .map((action: any) => action.payload)
      .withLatestFrom(this._store.select(state => state.parameterGroup.environment))
      .switchMap(([param, state]) => {
         const { name: oldParamName, value: { name: paramName, value, contexts } } = param;
         const { environmentVariables, list } = state;
         const index = environmentVariables.findIndex(env => env.name === oldParamName);
         const { name, id } = list;
         const observables: any = [];

         const parameters = index !== -1 ?
            [...environmentVariables.slice(0, index), param.value, ...environmentVariables.slice(index + 1)] :
            [...environmentVariables, param.value];
         const updatedList = { name, id, parameters };
         if (index !== -1 && (environmentVariables[index].value !== value || environmentVariables[index].name !== paramName)) {
            observables.push(this._parametersService.updateParamList(updatedList));
         }
         const contextsList = contexts.map(context => {
            return {
               ...context,
               parameters: [
                  ...state.environmentVariables.slice(0, index),
                  { name: paramName, value: context.value },
                  ...state.environmentVariables.slice(index + 1)
               ],
               parent: name
            };
         });

         contextsList.forEach((context: any) => {
            observables.push(this._parametersService.updateParamList(context));
         });

         return Observable.forkJoin(observables)
            .mergeMap((results: any) => {
               const actions: Array<Action> = [];
               if (results.length) {
                  actions.push(
                     new environmentParametersActions.ListEnvironmentParamsAction(),
                     new alertParametersActions.ShowAlertAction('Params saved')
                  );
               }
               return actions;
            })
            .catch(error => of(new alertParametersActions.ShowAlertAction('Params can not save')));
      });

   @Effect()
   deleteEnvironment$: Observable<any> = this._actions$
      .ofType(environmentParametersActions.DELETE_ENVIRONMENT_PARAMS)
      .map((action: any) => action.payload)
      .withLatestFrom(this._store.select(state => state.parameterGroup.environment))
      .switchMap(([param, state]) => {
         const { environmentVariables, list: { name, id } } = state;
         const index = environmentVariables.findIndex(env => env.name === param.name);
         const parameters = [...environmentVariables.slice(0, index), ...environmentVariables.slice(index + 1)];
         const updatedList = { name, id, parameters };
         return this._parametersService.updateParamList(updatedList)
            .mergeMap(context => [
               new environmentParametersActions.ListEnvironmentParamsAction(),
               new alertParametersActions.ShowAlertAction('Param deleted')
            ])
            .catch(error => of(new alertParametersActions.ShowAlertAction('Param can not delete')));
      });

   @Effect()
   saveEnvironmentContext$: Observable<any> = this._actions$
      .ofType(environmentParametersActions.SAVE_ENVIRONMENT_CONTEXT)
      .map((action: any) => action.payload)
      .switchMap((context: any) => {
         const saveContext = context.id ?
            this._parametersService.updateParamList(context) :
            this._parametersService.createParamList(context);
         return saveContext
            .mergeMap(res => [
               new environmentParametersActions.ListEnvironmentParamsAction(),
               new alertParametersActions.ShowAlertAction('Context saved')
            ])
            .catch(error => of(new alertParametersActions.ShowAlertAction('Context can not save')));
      });



   constructor(
      private _actions$: Actions,
      private _store: Store<fromParameters.State>,
      private _parametersService: ParametersService
   ) { }
}
