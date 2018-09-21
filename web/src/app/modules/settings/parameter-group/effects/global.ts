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
import { Store } from '@ngrx/store';
import { Effect, Actions } from '@ngrx/effects';

import * as fromParameters from './../reducers';
import * as globalParametersActions from './../actions/global';
import * as alertParametersActions from './../actions/alert';

import { ParametersService } from 'app/services';
import { from } from 'rxjs/observable/from';

@Injectable()
export class GlobalParametersEffect {

   @Effect()
   getGlobalParameters$: Observable<any> = this._actions$
      .ofType(globalParametersActions.LIST_GLOBAL_PARAMS)
      .switchMap(() => this._parametersService.getGlobalParameters()
      .mergeMap(response => [
            new globalParametersActions.ListGlobalParamsCompleteAction(response.variables),
            new alertParametersActions.HideLoadingAction()
         ])
      .catch(error => of(new globalParametersActions.ListGlobalParamsErrorAction())));

   @Effect()
   saveGlobals$: Observable<any> = this._actions$
      .ofType(globalParametersActions.SAVE_GLOBAL_PARAMS)
      .map((action: any) => action.payload)
      .withLatestFrom(this._store.select(state => state.parameterGroup.global))
      .switchMap(([param, state]) => {
         const { globalVariables } = state;
         const { name: oldName, value: { name, value } } = param;
         const index = globalVariables.findIndex(env => env.name === oldName);

         const { contexts, ...paramWithoutContexts } = param.value;
         const parameters = index !== -1 ?
            [...globalVariables.slice(0, index), paramWithoutContexts, ...globalVariables.slice(index + 1)] :
            [...globalVariables, paramWithoutContexts];
         const updateVariables = { variables: parameters };
         if (index !== -1 && (globalVariables[index].value !== value || globalVariables[index].name !== name)) {
            return this._parametersService.saveGlobalParameter(updateVariables)
               .mergeMap(res => [
                  new globalParametersActions.ListGlobalParamsCompleteAction(res.variables),
                  new alertParametersActions.ShowAlertAction('Param save successful')
               ])
               .catch(error => of(new alertParametersActions.ShowAlertAction('Param can not save')));
         } else {
            return of(new alertParametersActions.ShowAlertAction('Param saved'));
         }
      });

   @Effect()
   deleteGlobals$: Observable<any> = this._actions$
      .ofType(globalParametersActions.DELETE_GLOBAL_PARAMS)
      .map((action: any) => action.payload)
      .withLatestFrom(this._store.select(state => state.parameterGroup.global))
      .switchMap(([param, state]) => {
         const { globalVariables } = state;
         const { name, value } = param;
         const index = globalVariables.findIndex(env => env.name === name);
         const parameters = [...globalVariables.slice(0, index), ...globalVariables.slice(index + 1)];
         const updateVariables = { variables: parameters };
         return this._parametersService.saveGlobalParameter(updateVariables)
            .mergeMap(res => [
               new globalParametersActions.ListGlobalParamsCompleteAction(updateVariables.variables),
               new alertParametersActions.ShowAlertAction('Param delete successful')
            ])
            .catch(error => of(new alertParametersActions.ShowAlertAction('Param can not delete')));
      });

   constructor(
      private _actions$: Actions,
      private _store: Store<fromParameters.State>,
      private _parametersService: ParametersService
   ) { }
}

