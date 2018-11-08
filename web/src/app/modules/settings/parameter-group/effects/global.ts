/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Observable, of, from } from 'rxjs';
import { withLatestFrom, switchMap, mergeMap, map, catchError } from 'rxjs/operators';

import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { Effect, Actions, ofType } from '@ngrx/effects';

import * as fromParameters from './../reducers';
import * as globalParametersActions from './../actions/global';
import * as alertParametersActions from './../actions/alert';

import { ParametersService } from 'app/services';
import { generateJsonFile } from '@utils';

@Injectable()
export class GlobalParametersEffect {

   @Effect()
   getGlobalParameters$: Observable<any> = this._actions$
      .pipe(ofType(globalParametersActions.LIST_GLOBAL_PARAMS))
      .pipe(switchMap(() => this._parametersService.getGlobalParameters()))
      .pipe(mergeMap((response: any) => [
            new globalParametersActions.ListGlobalParamsCompleteAction(response.variables),
            new alertParametersActions.HideLoadingAction()
         ]))
      .pipe(catchError(error => of(new globalParametersActions.ListGlobalParamsErrorAction())));

   @Effect()
   saveGlobals$: Observable<any> = this._actions$
      .pipe(ofType(globalParametersActions.SAVE_GLOBAL_PARAMS))
      .pipe(map((action: any) => action.payload))
      .pipe(withLatestFrom(this._store.select(state => state.parameterGroup.global)))
      .pipe(switchMap(([param, state]) => {
          console.log('******');
         const { allVariables } = state;
         const { name: oldName, value: { name, value } } = param;
         const index = allVariables.findIndex(env => env.name === oldName);

         const { contexts, ...paramWithoutContexts } = param.value;
         const parameters = index !== -1 ?
            [...allVariables.slice(0, index), paramWithoutContexts, ...allVariables.slice(index + 1)] :
            [...allVariables, paramWithoutContexts];
         const updateVariables = { variables: parameters };

        return this._parametersService.updateGlobalParameter(updateVariables)
            .pipe(mergeMap(res => [
                new globalParametersActions.ListGlobalParamsAction(),
                new alertParametersActions.ShowAlertAction({ type: 'success', text: 'Parameter save successful' })
            ]))
            .pipe(catchError(error => of(new alertParametersActions.ShowAlertAction({ type: 'critical', text: 'Parameter can not save' }))));
      }));

    @Effect()
    deleteGlobals$: Observable<any> = this._actions$
        .pipe(ofType(globalParametersActions.DELETE_GLOBAL_PARAMS))
        .pipe(switchMap((action: any) =>  {
            if (action.payload.creation) {
                return of({ type: '[Global Params] Delete new global params' });
            }
            return this._parametersService.deleteGlobalParameter(action.payload.param.name)
                .pipe(mergeMap(res => from ([
                    new globalParametersActions.ListGlobalParamsAction(),
                    new alertParametersActions.ShowAlertAction({ type: 'success', text: 'Parameter delete successful' })
                ])))
                .pipe(catchError(error => of(new alertParametersActions.ShowAlertAction({ type: 'critical', text: 'Parameter can not delete' }))));
        }));

    @Effect()
    exportGlobal$: Observable<any> = this._actions$
        .pipe(ofType(globalParametersActions.EXPORT_GLOBAL_PARAMS))
        .pipe(switchMap((response: any) =>
            this._parametersService.getGlobalParameters()
            .pipe(map(envData => {
                generateJsonFile('global-parameters', envData);
                return new globalParametersActions.ExportGlobalParamsCompleteAction();
            }))
            .pipe(catchError(error => from([
                new globalParametersActions.ExportGlobalParamsErrorAction(),
                new alertParametersActions.ShowAlertAction({ type: 'critical', text: error })
            ]))))
        );

    @Effect()
    importGlobal$: Observable<any> = this._actions$
        .pipe(ofType(globalParametersActions.IMPORT_GLOBAL_PARAMS))
        .pipe(map((action: any) => action.payload))
        .pipe(switchMap((globals: any) =>
            this._parametersService.updateGlobalParameter(globals)
            .pipe(mergeMap(res => [
                new globalParametersActions.ListGlobalParamsAction(),
                new alertParametersActions.ShowAlertAction({ type: 'success', text: 'Parameter upload successful' })
             ]))
            .pipe(catchError(error => from([
                new globalParametersActions.ImportGlobalParamsErrorAction(),
                new alertParametersActions.ShowAlertAction({ type: 'critical', text: error })
            ]))))
        );

   constructor(
      private _actions$: Actions,
      private _store: Store<fromParameters.State>,
      private _parametersService: ParametersService
   ) { }
}

