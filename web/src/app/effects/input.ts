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

import { InputService } from 'services/input.service';
import { Injectable } from '@angular/core';
import { Action, Store } from '@ngrx/store';

import { InputType } from 'app/models/input.model';
import { Effect, Actions, toPayload } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import * as inputActions from 'actions/input';


@Injectable()
export class InputEffect {

    @Effect()
    getInputList$: Observable<Action> = this.actions$
        .ofType(inputActions.actionTypes.LIST_INPUT).switchMap((response: any) => {
            return this.inputService.getInputList()
                .map((inputList: any) => {
                    return new inputActions.ListInputCompleteAction(inputList);
                }).catch(function (error: any) {
                    return Observable.of(new inputActions.ListInputFailAction(''));
                });
        });

    @Effect()
    deleteInput$: Observable<Action> = this.actions$
        .ofType(inputActions.actionTypes.DELETE_INPUT)
        .map((action: any) => action.payload.selected)
        .switchMap((inputs: any) => {
            const joinObservables: Observable<any>[] = [];
            inputs.map((input: any) => {
                joinObservables.push(this.inputService.deleteInput(input.id));
            });
            return Observable.forkJoin(joinObservables).mergeMap(results => {
                return [new inputActions.DeleteInputCompleteAction(inputs), new inputActions.ListInputAction()];
            }).catch(function (error) {
                return Observable.of(new inputActions.DeleteInputErrorAction(''));
            });
        });

    @Effect()
    duplicateInput$: Observable<Action> = this.actions$
        .ofType(inputActions.actionTypes.DUPLICATE_INPUT)
        .switchMap((data: any) => {
            return this.inputService.createFragment(data.payload).mergeMap((data: any) => {
                return [new inputActions.DuplicateInputCompleteAction(), new inputActions.ListInputAction];
            }).catch(function (error: any) {
                return Observable.of(new inputActions.DuplicateInputErrorAction(''));
            });
        });

    @Effect()
    createInput$: Observable<Action> = this.actions$
        .ofType(inputActions.actionTypes.CREATE_INPUT)
        .switchMap((data: any) => {
            return this.inputService.createFragment(data.payload).mergeMap((data: any) => {
                return [new inputActions.CreateInputCompleteAction(), new inputActions.ListInputAction];
            }).catch(function (error: any) {
                return Observable.of(new inputActions.CreateInputErrorAction(''));
            });
        });

    @Effect()
    updateInput$: Observable<Action> = this.actions$
        .ofType(inputActions.actionTypes.UPDATE_INPUT)
        .switchMap((data: any) => {
            return this.inputService.updateFragment(data.payload).mergeMap((data: any) => {
                return [new inputActions.UpdateInputCompleteAction(), new inputActions.ListInputAction];
            }).catch(function (error: any) {
                return Observable.of(new inputActions.UpdateInputErrorAction(''));
            });
        });

    constructor(
        private actions$: Actions,
        private inputService: InputService
    ) { }

}
