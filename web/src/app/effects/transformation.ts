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

import * as transformationActions from 'actions/transformation';


@Injectable()
export class TransformationEffect {

    @Effect()
    getTransformationList$: Observable<Action> = this.actions$
        .ofType(transformationActions.LIST_TRANSFORMATION).switchMap((response: any) => {
            return this.templatesService.getTemplateList('transformation')
                .map((transformationList: any) => {
                    return new transformationActions.ListTransformationCompleteAction(transformationList);
                }).catch(function (error: any) {
                    return Observable.of(new transformationActions.ListTransformationFailAction(''));
                });
        });

    @Effect()
    deleteTransformation$: Observable<Action> = this.actions$
        .ofType(transformationActions.DELETE_TRANSFORMATION)
        .map((action: any) => action.payload.selected)
        .switchMap((transformations: any) => {
            const joinObservables: Observable<any>[] = [];
            transformations.map((transformation: any) => {
                joinObservables.push(this.templatesService.deleteTemplate('transformation', transformation.id));
            });
            return Observable.forkJoin(joinObservables).mergeMap(results => {
                return [new transformationActions.DeleteTransformationCompleteAction(transformations), new transformationActions.ListTransformationAction()];
            }).catch(function (error) {
                return Observable.of(new transformationActions.DeleteTransformationErrorAction(''));
            });
        });

    @Effect()
    duplicateTransformation$: Observable<Action> = this.actions$
        .ofType(transformationActions.DUPLICATE_TRANSFORMATION)
        .switchMap((data: any) => {
            let transformation = Object.assign(data.payload);
            delete transformation.id;
            return this.templatesService.createTemplate(transformation).mergeMap((data: any) => {
                return [new transformationActions.DuplicateTransformationCompleteAction(), new transformationActions.ListTransformationAction];
            }).catch(function (error: any) {
                return Observable.of(new transformationActions.DuplicateTransformationErrorAction(''));
            });
        });

    @Effect()
    createTransformation$: Observable<Action> = this.actions$
        .ofType(transformationActions.CREATE_TRANSFORMATION)
        .switchMap((data: any) => {
            return this.templatesService.createTemplate(data.payload).mergeMap((data: any) => {
                return [new transformationActions.CreateTransformationCompleteAction(), new transformationActions.ListTransformationAction];
            }).catch(function (error: any) {
                return Observable.of(new transformationActions.CreateTransformationErrorAction(''));
            });
        });

    @Effect()
    updateTransformation$: Observable<Action> = this.actions$
        .ofType(transformationActions.UPDATE_TRANSFORMATION)
        .switchMap((data: any) => {
            return this.templatesService.updateFragment(data.payload).mergeMap((data: any) => {
                return [new transformationActions.UpdateTransformationCompleteAction(), new transformationActions.ListTransformationAction];
            }).catch(function (error: any) {
                return Observable.of(new transformationActions.UpdateTransformationErrorAction(''));
            });
        });

    constructor(
        private actions$: Actions,
        private templatesService: TemplatesService
    ) { }

}
