/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */


import { Injectable } from '@angular/core';
import { Store, Action, select } from '@ngrx/store';
import { Effect, Actions, ofType } from '@ngrx/effects';
import { Observable, of, forkJoin, iif, from } from 'rxjs';
import { withLatestFrom, switchMap, mergeMap, map, catchError, delay } from 'rxjs/operators';
import * as fromParameters from './../reducers';
import * as environmentParametersActions from './../actions/environment';
import * as alertParametersActions from './../actions/alert';

import { ParametersService } from 'app/services';
import { generateJsonFile } from '@utils';

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
         .pipe(catchError(error => of(new alertParametersActions.HideLoadingAction())))));

   @Effect()
   saveContext$: Observable<any> = this._actions$
      .pipe(ofType(environmentParametersActions.ADD_CONTEXT))
      .pipe(map((action: any) => action.payload))
      .pipe(withLatestFrom(this._store.pipe(select(state => state.parameterGroup.environment))))
      .pipe(switchMap(([name, state]) => {
         const { allVariables: parameters, list: { name: parent } } = state;
         const newContext = { name, parent, parameters };

         return this._parametersService.createParamList(newContext)
            .pipe(mergeMap((context: any) => [
               new environmentParametersActions.AddContextCompleteAction({ name: context.name, id: context.id }),
               new alertParametersActions.ShowAlertAction({ type: 'success', text: 'Context saved' })
            ]))
            .pipe(catchError(error => {
                return of(new alertParametersActions.ShowAlertAction(error.errorCode && error.message ? { type: 'critical', text: error.message } : { type: 'critical', text: 'Context can not save' } ));
            }));
      }));

   @Effect()
   saveEnvironment$: Observable<any> = this._actions$
      .pipe(ofType(environmentParametersActions.SAVE_PARAM))
      .pipe(map((action: any) => action.payload))
      .pipe(withLatestFrom(this._store.pipe(select((state: any) => state.parameterGroup.environment))))
      .pipe(switchMap(([param, state]) => {
         const { name: oldParamName, value: { name: paramName, value, contexts } } = param;
         const { list, creationMode, allVariables } = state;
         const index = allVariables.findIndex(env => env.name === oldParamName);
         const exist = allVariables.findIndex(env => env.name === paramName) !== -1 && creationMode;
         const { name, id } = list;
         const observables: any = [];

         const parameters = index !== -1 ?
            [...allVariables.slice(0, index), { ...allVariables[index], ...param.value }, ...allVariables.slice(index + 1)] :
            [...allVariables, param.value];
         const updatedList = { name, id, parameters };

         const oldContextsList = allVariables[index] && allVariables[index].contexts;

         const updateContextList = oldContextsList ? oldContextsList
            .filter(c => !contexts.find(a => c.name === a.name))
            .map(context => ({
                ...context,
                parameters: parameters.map(p => {
                    const defaultContext = p.contexts.find(c => c.name === context.name);
                    return { name: p.name, value: defaultContext ? defaultContext.value : p.value };
                }),
                parent: name
            })) : [];

         const contextsList = contexts ? contexts.map(context => ({
               ...context,
               parameters: parameters.map(p => {
                    const defaultContext = p.contexts.find(c => c.name === context.name);
                    return { name: p.name, value: defaultContext ? defaultContext.value : p.value };
                }),
               parent: name
         })) : [];

         const updatedLists = [...updateContextList, ...contextsList, updatedList];

         observables.push(this._parametersService.updateParamList(updatedLists));
         return iif(() => exist ,
            of(new alertParametersActions.ShowAlertAction({ type: 'warning', text: 'Parameters already exist' })),
            forkJoin(observables)
            .pipe(mergeMap((results: any) => {
                const actions: Array<Action> = [];
                if (results.length) {
                    actions.push(
                        new environmentParametersActions.ListEnvironmentParamsAction(),
                        new alertParametersActions.ShowAlertAction({ type: 'success', text: 'Parameters saved' })
                    );
                }
                return actions;
            }))
            .pipe(catchError(error => of(new alertParametersActions.ShowAlertAction({ type: 'critical', text: 'Parameters can not save' })))));
      }));

   @Effect()
   deleteEnvironment$: Observable<any> = this._actions$
      .pipe(ofType(environmentParametersActions.DELETE_ENVIRONMENT_PARAMS))
      .pipe(map((action: any) => action.payload))
      .pipe(withLatestFrom(this._store.pipe(select((state: any) => state.parameterGroup.environment))))
      .pipe(switchMap(([param, state]) => {
         const { allVariables, list: { name, id } } = state;
         const index = allVariables.findIndex(env => env.name === param.param.name);
         const parameters = [...allVariables.slice(0, index), ...allVariables.slice(index + 1)];
         const updatedList = { name, id, parameters };
         return this._parametersService.updateParamList(updatedList)
            .pipe(mergeMap(context => [
               new environmentParametersActions.ListEnvironmentParamsAction(),
               new alertParametersActions.ShowAlertAction({ type: 'success', text: 'Parameters deleted' })
            ]))
            .pipe(catchError(error => of(new alertParametersActions.ShowAlertAction({ type: 'critical', text: 'Parameters can not delete' }))));
      }));

   @Effect()
   saveEnvironmentContext$: Observable<any> = this._actions$
      .pipe(ofType(environmentParametersActions.SAVE_ENVIRONMENT_CONTEXT))
      .pipe(map((action: any) => action.payload))
      .pipe(withLatestFrom(this._store.pipe(select((state: any) => state.parameterGroup.environment))))
      .pipe(switchMap(([context, state]) => {
        const { contexts } = state;
        const exist = contexts.findIndex(c => c.name === context.name) !== -1;
         const saveContext = context.id ?
            this._parametersService.updateParamList(context) :
            this._parametersService.createParamList(context);
            return iif(() => exist,
                of(new alertParametersActions.ShowAlertAction({ type: 'warning', text: 'Context name already exist' })),
                saveContext
                .pipe(mergeMap(res => [
                    new environmentParametersActions.ListEnvironmentParamsAction(),
                    new alertParametersActions.ShowAlertAction({ type: 'success', text: 'Context saved' })
                ]))
                .pipe(catchError(error => of(new alertParametersActions.ShowAlertAction({ type: 'critical', text: 'Context can not save' })))));
      }));

      @Effect()
      deleteCustomContext$: Observable<any> = this._actions$
         .pipe(ofType(environmentParametersActions.DELETE_ENVIRONMENT_CONTEXT))
         .pipe(map((action: any) => action.context))
         .pipe(switchMap((context: any) => {
            return this._parametersService.deleteList(context.id)
               .pipe(mergeMap(res => [
                  new environmentParametersActions.ListEnvironmentParamsAction(),
                  new alertParametersActions.ShowAlertAction({ type: 'success', text: 'Context deleted' })
               ]))
               .pipe(catchError(error => of(new alertParametersActions.ShowAlertAction({ type: 'critical', text: 'Context can not delete' }))));
         }));

    @Effect()
    exportGlobal$: Observable<any> = this._actions$
        .pipe(ofType(environmentParametersActions.EXPORT_ENVIRONMENT_PARAMS))
        .pipe(switchMap((response: any) =>
            this._parametersService.getEnvironmentAndContext()
                .pipe(map(envData => {
                    generateJsonFile('environment-parameters', envData);
                    return new environmentParametersActions.ExportEnvironmentParamsCompleteAction();
                }))
                .pipe(catchError(error => from([
                    new environmentParametersActions.ExportEnvironmentParamsErrorAction(),
                    new alertParametersActions.ShowAlertAction({ type: 'critical', text: error })
                ]))))
        );

    @Effect()
    importGlobal$: Observable<any> = this._actions$
        .pipe(ofType(environmentParametersActions.IMPORT_ENVIRONMENT_PARAMS))
        .pipe(map((action: any) => action.payload))
        .pipe(switchMap((environment: any) =>
            this._parametersService.updateParamList([environment.parameterList, ...environment.contexts])
            .pipe(mergeMap(res => [
                new environmentParametersActions.ListEnvironmentParamsAction(),
                new alertParametersActions.ShowAlertAction({ type: 'success', text: 'Parameter upload successful' })
             ]))
            .pipe(catchError(error => from([
                new environmentParametersActions.ImportEnvironmentParamsErrorAction(),
                new alertParametersActions.ShowAlertAction({ type: 'critical', text: error })
            ]))))
        );


   constructor(
      private _actions$: Actions,
      private _store: Store<fromParameters.State>,
      private _parametersService: ParametersService
   ) { }
}
