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

import { forkJoin, of, Observable } from 'rxjs';

import { Injectable } from '@angular/core';
import { Store, Action } from '@ngrx/store';
import { Effect, Actions } from '@ngrx/effects';

import * as fromParameters from './../reducers';
import * as customParametersActions from './../actions/custom';
import * as alertParametersActions from './../actions/alert';

import { ParametersService } from 'app/services';
import { from } from 'rxjs/observable/from';

@Injectable()
export class CustomParametersEffect {

    @Effect()
    getCustomList$: Observable<any> = this._actions$
        .ofType(customParametersActions.LIST_CUSTOM_PARAMS)
        .switchMap(() => this._parametersService.getParamList()
            .map((customLists) => new customParametersActions.ListCustomParamsCompleteAction(customLists.filter(list => list.name !== 'Environment' && !list.parent)))
            .catch(error => of(new customParametersActions.ListCustomParamsErrorAction())));

    @Effect()
    getCustomListName$: Observable<any> = this._actions$
        .ofType(customParametersActions.LIST_CUSTOM_PARAMS_NAME)
        .map((action: any) => action.payload)
        .switchMap(() => this._parametersService.getParamList()
            .map(() => new customParametersActions.ListCustomParamsErrorAction())
            .catch(error => of(new customParametersActions.ListCustomParamsErrorAction())));

    @Effect()
    navigateParam$: Observable<any> = this._actions$
        .ofType(customParametersActions.NAVIGAGE_TO_LIST)
        .map((action: any) => action.payload)
        .withLatestFrom(this._store.select(state => state.parameterGroup.custom))
        .switchMap(([param, state]) => this._parametersService.getCustomAndContext(param.name)
            .map(res => new customParametersActions.NavigateToListCompleteAction(res))
            .catch(error => of(new customParametersActions.ListCustomParamsErrorAction())));

    @Effect()
    goParam$: Observable<any> = this._actions$
        .ofType(customParametersActions.GO_CUSTOM_PARAMS)
        .switchMap(() => of(new customParametersActions.ListCustomParamsAction()));

    @Effect()
    saveCustomList$: Observable<any> = this._actions$
        .ofType(customParametersActions.SAVE_CUSTOM_LIST)
        .map((action: any) => action.payload)
        .withLatestFrom(this._store.select(state => state.parameterGroup.custom))
        .switchMap(([customList, state]) => {
            const { customList: oldCustomList } = state;
            const { value: { name: newName }, list } = customList;
            const observables: any = [];
            const index = oldCustomList.findIndex(custom => custom.name === newName);
            if (list.name !== newName && index === -1) {
                list.name = newName;
                if (list.id) {
                    observables.push(this._parametersService.updateParamList(list));
                } else {
                    observables.push(this._parametersService.createParamList(list));
                }
            }
            return forkJoin(observables)
                .mergeMap((results: any) => {
                    const actions: Array<Action> = [];
                    if (results.length) {
                        actions.push(
                            new customParametersActions.ListCustomParamsAction(),
                            new alertParametersActions.ShowAlertAction('List saved')
                        );
                    }
                    return actions;
                })
                .catch(error => {
                    return from([
                        new customParametersActions.ListCustomParamsAction(),
                        new alertParametersActions.ShowAlertAction('List can not save')
                    ]);
                });
        });

    @Effect()
    saveCustomContext$: Observable<any> = this._actions$
        .ofType(customParametersActions.SAVE_CUSTOM_CONTEXT)
        .map((action: any) => action.payload)
        .withLatestFrom(this._store.select(state => state.parameterGroup.custom))
        .switchMap(([customList, state]) => {
            const { customList: oldCustomList, list: { name } } = state;
            const { value: { name: newName }, list } = customList;

            const observables: any = [];
            const index = oldCustomList.findIndex(custom => custom.name === newName);
            if (list.name !== newName && index === -1) {
                list.name = newName;
                if (list.id) {
                    observables.push(this._parametersService.updateParamList(list));
                } else {
                    observables.push(this._parametersService.createParamList(list));
                }
            }
            return forkJoin(observables)
                .mergeMap((results: any) => {
                    const actions: Array<Action> = [];
                    if (results.length) {
                        actions.push(
                            new customParametersActions.NavigateToListAction({ name }),
                            new alertParametersActions.ShowAlertAction('Context saved')
                        );
                    }
                    return actions;
                })
                .catch(error => {
                    return from([
                        new customParametersActions.NavigateToListAction({ name }),
                        new alertParametersActions.ShowAlertAction('Context can not save')
                    ]);
                });
        });
    @Effect()
    saveEnvironment$: Observable<any> = this._actions$
        .ofType(customParametersActions.SAVE_PARAM)
        .map((action: any) => action.payload)
        .withLatestFrom(this._store.select(state => state.parameterGroup.custom))
        .switchMap(([param, state]) => {
            const { name: oldParamName, value: { name: paramName, value, contexts } } = param;
            const { customVariables, list } = state;
            const index = customVariables.findIndex(env => env.name === oldParamName);
            const { name, id } = list;
            const observables: any = [];

            const parameters = index !== -1 ?
                [...customVariables.slice(0, index), param.value, ...customVariables.slice(index + 1)] :
                [...customVariables, param.value];
            const updatedList = { name, id, parameters };
            if (index !== -1 && (customVariables[index].value !== value || customVariables[index].name !== paramName)) {
                observables.push(this._parametersService.updateParamList(updatedList));
            }
            const contextsList = contexts.map(context => {
                return {
                    ...context,
                    parameters: [
                        ...state.customVariables.slice(0, index),
                        { name: paramName, value: context.value },
                        ...state.customVariables.slice(index + 1)
                    ],
                    parent: name
                };
            });

            contextsList.forEach((context: any) => {
                observables.push(this._parametersService.updateParamList(context));
            });

            return forkJoin(observables)
                .mergeMap((results: any) => {
                    const actions: Array<Action> = [];
                    if (results.length) {
                        actions.push(
                            new customParametersActions.NavigateToListAction({ name }),
                            new alertParametersActions.ShowAlertAction('Param saved')
                        );
                    }
                    return actions;
                })
                .catch(error => of(new customParametersActions.ListCustomParamsErrorAction()));
        });

    @Effect()
    deleteCustom$: Observable<any> = this._actions$
        .ofType(customParametersActions.DELETE_CUSTOM_PARAMS)
        .map((action: any) => action.payload)
        .withLatestFrom(this._store.select(state => state.parameterGroup.custom))
        .switchMap(([param, state]) => {
            const { customVariables, list: { name, id } } = state;
            const index = customVariables.findIndex(env => env.name === param.name);
            const parameters = [...customVariables.slice(0, index), ...customVariables.slice(index + 1)];
            const updatedList = { name, id, parameters };
            return this._parametersService.updateParamList(updatedList)
                .mergeMap(res => [
                    new customParametersActions.NavigateToListAction({ name }),
                    new alertParametersActions.ShowAlertAction('List deleted')
                ])
                .catch(error => of(new alertParametersActions.ShowAlertAction('List can not delete')));
        });

    @Effect()
    deleteCustomList$: Observable<any> = this._actions$
        .ofType(customParametersActions.DELETE_CUSTOM_LIST)
        .map((action: any) => action.list)
        .switchMap((list: any) => {
            return this._parametersService.deleteList(list.id)
                .mergeMap(res => [
                    new customParametersActions.ListCustomParamsAction(),
                    new alertParametersActions.ShowAlertAction('List deleted')
                ])
                .catch(error => of(new alertParametersActions.ShowAlertAction('List can not delete')));
        });

    @Effect()
    deleteCustomContext$: Observable<any> = this._actions$
        .ofType(customParametersActions.DELETE_CUSTOM_CONTEXT)
        .map((action: any) => action.context)
        .switchMap((context: any) => {
            const { parent: name } = context;
            return this._parametersService.deleteList(context.id)
                .mergeMap(res => [
                    new customParametersActions.NavigateToListAction({ name }),
                    new alertParametersActions.ShowAlertAction('Context deleted')
                ])
                .catch(error => of(new alertParametersActions.ShowAlertAction('Context can not delete')));
        });


    @Effect()
    saveContext$: Observable<any> = this._actions$
        .ofType(customParametersActions.ADD_CONTEXT)
        .map((action: any) => action.payload)
        .withLatestFrom(this._store.select(state => state.parameterGroup.custom))
        .switchMap(([name, state]) => {
            const { customVariables: parameters, list: { name: parent } } = state;
            const newContext = { name, parent, parameters };

            return this._parametersService.createParamList(newContext)
                .mergeMap((context) => [
                    new customParametersActions.NavigateToListAction({ name: parent }),
                    new alertParametersActions.ShowAlertAction('Context saved')
                ])
                .catch(error => from([
                    new customParametersActions.NavigateToListAction({ name: parent }),
                    new alertParametersActions.ShowAlertAction('Context can not save')
                ]));
        });

    constructor(
        private _actions$: Actions,
        private _store: Store<fromParameters.State>,
        private _parametersService: ParametersService
    ) { }
}
