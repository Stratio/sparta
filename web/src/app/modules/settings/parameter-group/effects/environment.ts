/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */


import { Injectable } from '@angular/core';
import { Store, Action } from '@ngrx/store';
import { Effect, Actions, ofType } from '@ngrx/effects';
import { Observable, of, forkJoin } from 'rxjs';
import { withLatestFrom, switchMap, mergeMap, map } from 'rxjs/operators';
import * as fromParameters from './../reducers';
import * as environmentParametersActions from './../actions/environment';
import * as alertParametersActions from './../actions/alert';

import { ParametersService } from 'app/services';

@Injectable()
export class EnviromentParametersEffect {

   @Effect()
   getEnvironmentParameters$: Observable<any> = this._actions$
      .pipe(ofType(environmentParametersActions.LIST_ENVIRONMENT_PARAMS))
      .pipe(switchMap(() => this._parametersService.getEnvironmentAndContext()
         .pipe(mergeMap((response: any) => [
            new environmentParametersActions.ListEnvironmentParamsCompleteAction(response),
            new alertParametersActions.HideLoadingAction()
         ]))
         .catch(error => of(new alertParametersActions.HideLoadingAction()))));

   @Effect()
   saveContext$: Observable<any> = this._actions$
      .pipe(ofType(environmentParametersActions.ADD_CONTEXT))
      .pipe(map((action: any) => action.payload))
      .pipe(withLatestFrom(this._store.select(state => state.parameterGroup.environment)))
      .pipe(switchMap(([name, state]) => {
         const { environmentVariables: parameters, list: { name: parent } } = state;
         const newContext = { name, parent, parameters };

         return this._parametersService.createParamList(newContext)
            .pipe(mergeMap((context: any) => [
               new environmentParametersActions.AddContextCompleteAction({ name: context.name, id: context.id }),
               new alertParametersActions.ShowAlertAction('Context saved')
            ]))
            .catch(error => of(new alertParametersActions.ShowAlertAction('Context can not save')));
      }));

   @Effect()
   saveEnvironment$: Observable<any> = this._actions$
      .pipe(ofType(environmentParametersActions.SAVE_PARAM))
      .pipe(map((action: any) => action.payload))
      .pipe(withLatestFrom(this._store.select(state => state.parameterGroup.environment)))
      .pipe(switchMap(([param, state]) => {
         const { name: oldParamName, value: { name: paramName, value, contexts } } = param;
         const { environmentVariables, list } = state;
         const index = environmentVariables.findIndex(env => env.name === oldParamName);
         const { name, id } = list;
         const observables: any = [];

         const parameters = index !== -1 ?
            [...environmentVariables.slice(0, index), { ...environmentVariables[0], ...param.value }, ...environmentVariables.slice(index + 1)] :
            [...environmentVariables, param.value];
         const updatedList = { name, id, parameters };
         observables.push(this._parametersService.updateParamList(updatedList));


         const oldContextsList = environmentVariables[index] && environmentVariables[index].contexts;
         const defaultValue = environmentVariables[index] && environmentVariables[index].defaultValue;

         const updateContextList = oldContextsList.filter(c => !contexts.find(a => c.name === a.name));
         const a = updateContextList.map(context => {
            const { value: val, ...formatContext } = context;
            return {
               ...formatContext,
               parameters: [
                  ...state.environmentVariables.slice(0, index),
                  { name: paramName, value: defaultValue },
                  ...state.environmentVariables.slice(index + 1)
               ],
               parent: name
            };
         });

         const contextsList = contexts.map(context => {
            const { value: val, ...formatContext } = context;
            return {
               ...formatContext,
               parameters: [
                  ...state.environmentVariables.slice(0, index),
                  { name: paramName, value: context.value },
                  ...state.environmentVariables.slice(index + 1)
               ],
               parent: name
            };
         });



         a.forEach((context: any) => {
            observables.push(this._parametersService.updateParamList(context));
         });

         contextsList.forEach((context: any) => {
            observables.push(this._parametersService.updateParamList(context));
         });

         return forkJoin(observables)
            .pipe(mergeMap((results: any) => {
               const actions: Array<Action> = [];
               if (results.length) {
                  actions.push(
                     new environmentParametersActions.ListEnvironmentParamsAction(),
                     new alertParametersActions.ShowAlertAction('Params saved')
                  );
               }
               return actions;
            }))
            .catch(error => of(new alertParametersActions.ShowAlertAction('Params can not save')));
      }));

   @Effect()
   deleteEnvironment$: Observable<any> = this._actions$
      .pipe(ofType(environmentParametersActions.DELETE_ENVIRONMENT_PARAMS))
      .pipe(map((action: any) => action.payload))
      .pipe(withLatestFrom(this._store.select(state => state.parameterGroup.environment)))
      .pipe(switchMap(([param, state]) => {
         const { environmentVariables, list: { name, id } } = state;
         const index = environmentVariables.findIndex(env => env.name === param.name);
         const parameters = [...environmentVariables.slice(0, index), ...environmentVariables.slice(index + 1)];
         const updatedList = { name, id, parameters };
         return this._parametersService.updateParamList(updatedList)
            .pipe(mergeMap(context => [
               new environmentParametersActions.ListEnvironmentParamsAction(),
               new alertParametersActions.ShowAlertAction('Param deleted')
            ]))
            .catch(error => of(new alertParametersActions.ShowAlertAction('Param can not delete')));
      }));

   @Effect()
   saveEnvironmentContext$: Observable<any> = this._actions$
      .pipe(ofType(environmentParametersActions.SAVE_ENVIRONMENT_CONTEXT))
      .pipe(map((action: any) => action.payload))
      .pipe(switchMap((context: any) => {
         const saveContext = context.id ?
            this._parametersService.updateParamList(context) :
            this._parametersService.createParamList(context);
         return saveContext
            .pipe(mergeMap(res => [
               new environmentParametersActions.ListEnvironmentParamsAction(),
               new alertParametersActions.ShowAlertAction('Context saved')
            ]))
            .catch(error => of(new alertParametersActions.ShowAlertAction('Context can not save')));
      }));

      @Effect()
      deleteCustomContext$: Observable<any> = this._actions$
         .pipe(ofType(environmentParametersActions.DELETE_ENVIRONMENT_CONTEXT))
         .pipe(map((action: any) => action.context))
         .pipe(switchMap((context: any) => {
            return this._parametersService.deleteList(context.id)
               .pipe(mergeMap(res => [
                  new environmentParametersActions.ListEnvironmentParamsAction(),
                  new alertParametersActions.ShowAlertAction('Context deleted')
               ]))
               .catch(error => of(new alertParametersActions.ShowAlertAction('Context can not delete')));
         }));


   constructor(
      private _actions$: Actions,
      private _store: Store<fromParameters.State>,
      private _parametersService: ParametersService
   ) { }
}
