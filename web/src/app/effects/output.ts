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

import { OutputService } from 'services/output.service';
import { Injectable } from '@angular/core';
import { Action, Store } from '@ngrx/store';
import { OutputType } from 'app/models/output.model';
import { Effect, Actions, toPayload } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import * as outputActions from 'actions/output';
import * as errorsActions from 'actions/errors';


@Injectable()
export class OutputEffect {

    @Effect()
    getOutputList$: Observable<Action> = this.actions$
        .ofType(outputActions.LIST_OUTPUT).switchMap((response: any) => {

            return this.outputService.getOutputList()
                .map((outputList: any) => {
                    return new outputActions.ListOutputCompleteAction(outputList);
                }).catch(function (error: any) {
                    return Observable.from([new outputActions.ListOutputFailAction(''), new errorsActions.HttpErrorAction(error)]);
                });
        });

    @Effect()
    deleteOutput$: Observable<Action> = this.actions$
        .ofType(outputActions.DELETE_OUTPUT)
        .map((action: any) => action.payload.selected)
        .switchMap((outputs: any) => {
            const joinObservables: Observable<any>[] = [];
            outputs.map((output: any) => {
                joinObservables.push(this.outputService.deleteOutput(output.id));
            });
            return Observable.forkJoin(joinObservables).mergeMap(results => {
                return [new outputActions.DeleteOutputCompleteAction(outputs), new outputActions.ListOutputAction()];
            }).catch(function (error) {
                return Observable.from([new outputActions.DeleteOutputErrorAction(''), new errorsActions.HttpErrorAction(error)]);
            });
        });

    @Effect()
    duplicateOutput$: Observable<Action> = this.actions$
        .ofType(outputActions.DUPLICATE_OUTPUT)
        .switchMap((data: any) => {
            let output = Object.assign(data.payload);
            delete output.id;
            return this.outputService.createFragment(output).mergeMap((data: any) => {
                return [new outputActions.DuplicateOutputCompleteAction(), new outputActions.ListOutputAction];
            }).catch(function (error: any) {
                return Observable.from([new outputActions.DuplicateOutputErrorAction(''), new errorsActions.HttpErrorAction(error)]);
            });
        });


    @Effect()
    createOutput$: Observable<Action> = this.actions$
        .ofType(outputActions.CREATE_OUTPUT)
        .switchMap((data: any) => {
            return this.outputService.createFragment(data.payload).mergeMap((data: any) => {
                return [new outputActions.CreateOutputCompleteAction(), new outputActions.ListOutputAction];
            }).catch(function (error: any) {
                return Observable.from([new outputActions.CreateOutputErrorAction(''), new errorsActions.HttpErrorAction(error)]);
            });
        });

    @Effect()
    updateOutput$: Observable<Action> = this.actions$
        .ofType(outputActions.UPDATE_OUTPUT)
        .switchMap((data: any) => {
            return this.outputService.updateFragment(data.payload).mergeMap((data: any) => {
                return [new outputActions.UpdateOutputCompleteAction(), new outputActions.ListOutputAction];
            }).catch(function (error: any) {
                return Observable.from([new outputActions.UpdateOutputErrorAction(''), new errorsActions.HttpErrorAction(error)]);
            });
        });

    constructor(
        private actions$: Actions,
        private outputService: OutputService
    ) { }

}