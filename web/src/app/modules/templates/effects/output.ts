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

import { TemplatesService } from 'services/templates.service';
import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Effect, Actions } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import * as outputActions from './../actions/output';

@Injectable()
export class OutputEffect {

    @Effect()
    getOutputList$: Observable<Action> = this.actions$
        .ofType(outputActions.LIST_OUTPUT).switchMap((response: any) => {

            return this.templatesService.getTemplateList('output')
                .map((outputList: any) => {
                    return new outputActions.ListOutputCompleteAction(outputList);
                }).catch(function (error: any) {
                   return error.statusText === 'Unknown Error' ? Observable.of(new outputActions.ListOutputFailAction('')) :
                    Observable.of({type: 'NO_ACTION'});
                });
        });

    @Effect()
    deleteOutput$: Observable<Action> = this.actions$
        .ofType(outputActions.DELETE_OUTPUT)
        .map((action: any) => action.payload.selected)
        .switchMap((outputs: any) => {
            const joinObservables: Observable<any>[] = [];
            outputs.map((output: any) => {
                joinObservables.push(this.templatesService.deleteTemplate('output', output.id));
            });
            return Observable.forkJoin(joinObservables).mergeMap(results => {
                return [new outputActions.DeleteOutputCompleteAction(outputs), new outputActions.ListOutputAction()];
            }).catch(function (error) {
                return Observable.of(new outputActions.DeleteOutputErrorAction(''));
            });
        });

    @Effect()
    duplicateOutput$: Observable<Action> = this.actions$
        .ofType(outputActions.DUPLICATE_OUTPUT)
        .switchMap((data: any) => {
            let output = Object.assign(data.payload);
            delete output.id;
            return this.templatesService.createTemplate(output).mergeMap((data: any) => {
                return [new outputActions.DuplicateOutputCompleteAction(), new outputActions.ListOutputAction];
            }).catch(function (error: any) {
                return Observable.of(new outputActions.DuplicateOutputErrorAction(''));
            });
        });


    @Effect()
    createOutput$: Observable<Action> = this.actions$
        .ofType(outputActions.CREATE_OUTPUT)
        .switchMap((data: any) => {
            return this.templatesService.createTemplate(data.payload).mergeMap((data: any) => {
                return [new outputActions.CreateOutputCompleteAction(), new outputActions.ListOutputAction];
            }).catch(function (error: any) {
                return Observable.of(new outputActions.CreateOutputErrorAction(''));
            });
        });

    @Effect()
    updateOutput$: Observable<Action> = this.actions$
        .ofType(outputActions.UPDATE_OUTPUT)
        .switchMap((data: any) => {
            return this.templatesService.updateFragment(data.payload).mergeMap((data: any) => {
                return [new outputActions.UpdateOutputCompleteAction(), new outputActions.ListOutputAction];
            }).catch(function (error: any) {
                return Observable.of(new outputActions.UpdateOutputErrorAction(''));
            });
        });

    constructor(
        private actions$: Actions,
        private templatesService: TemplatesService
    ) { }

}