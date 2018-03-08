/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { TemplatesService } from 'services/templates.service';
import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';

import { Effect, Actions } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import * as inputActions from './../actions/input';
import * as errorActions from 'actions/errors';


@Injectable()
export class InputEffect {

    @Effect()
    getInputList$: Observable<Action> = this.actions$
        .ofType(inputActions.LIST_INPUT).switchMap((response: any) => {
            return this.templatesService.getTemplateList('input')
                .map((inputList: any) => {
                    return new inputActions.ListInputCompleteAction(inputList);
                }).catch(function (error: any) {
                    return error.statusText === 'Unknown Error' ? Observable.of(new inputActions.ListInputFailAction(''))
                        : Observable.of(new errorActions.ServerErrorAction(error));
                });
        });

    @Effect()
    getInputTemplate$: Observable<Action> = this.actions$
        .ofType(inputActions.GET_EDITED_INPUT)
        .map((action: any) => action.payload)
        .switchMap((param: any) => {
            return this.templatesService.getTemplateById('input', param)
                .map((input: any) => {
                    return new inputActions.GetEditedInputCompleteAction(input)
                }).catch(function (error: any) {
                    console.log(error)
                    return error.statusText === 'Unknown Error' ? Observable.of(new inputActions.GetEditedInputErrorAction(''))
                        : Observable.of(new errorActions.ServerErrorAction(error));
                });
        });
    @Effect()
    deleteInput$: Observable<Action> = this.actions$
        .ofType(inputActions.DELETE_INPUT)
        .map((action: any) => action.payload.selected)
        .switchMap((inputs: any) => {
            const joinObservables: Observable<any>[] = [];
            inputs.map((input: any) => {
                joinObservables.push(this.templatesService.deleteTemplate('input', input.id));
            });
            return Observable.forkJoin(joinObservables).mergeMap(results => {
                return [new inputActions.DeleteInputCompleteAction(inputs), new inputActions.ListInputAction()];
            }).catch(function (error) {
                return Observable.of(new errorActions.ServerErrorAction(error));
            });
        });

    @Effect()
    duplicateInput$: Observable<Action> = this.actions$
        .ofType(inputActions.DUPLICATE_INPUT)
        .switchMap((data: any) => {
            let input = Object.assign(data.payload);
            delete input.id;
            return this.templatesService.createTemplate(input).mergeMap((data: any) => {
                return [new inputActions.DuplicateInputCompleteAction(), new inputActions.ListInputAction];
            }).catch(function (error: any) {
                return Observable.of(new errorActions.ServerErrorAction(error));
            });
        });

    @Effect()
    createInput$: Observable<Action> = this.actions$
        .ofType(inputActions.CREATE_INPUT)
        .switchMap((data: any) => {
            return this.templatesService.createTemplate(data.payload).mergeMap((data: any) => {
                return [new inputActions.CreateInputCompleteAction(), new inputActions.ListInputAction];
            }).catch(function (error: any) {
                return Observable.of(new errorActions.ServerErrorAction(error));
            });
        });

    @Effect()
    updateInput$: Observable<Action> = this.actions$
        .ofType(inputActions.UPDATE_INPUT)
        .switchMap((data: any) => {
            return this.templatesService.updateFragment(data.payload).mergeMap((data: any) => {
                return [new inputActions.UpdateInputCompleteAction(), new inputActions.ListInputAction];
            }).catch(function (error: any) {
                return Observable.of(new errorActions.ServerErrorAction(error));
            });
        });

    constructor(
        private actions$: Actions,
        private templatesService: TemplatesService
    ) { }

}
