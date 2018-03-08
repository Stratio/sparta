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

import * as outputActions from './../actions/output';
import * as errorActions from 'actions/errors';

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
                    Observable.of(new errorActions.ServerErrorAction(error));
                });
        });

    @Effect()
    getOutputTemplate$: Observable<Action> = this.actions$
        .ofType(outputActions.GET_EDITED_OUTPUT)
        .map((action: any) => action.payload)
        .switchMap((param: any) => {
            return this.templatesService.getTemplateById('output', param)
                .map((output: any) => {
                    return new outputActions.GetEditedOutputCompleteAction(output)
                }).catch(function (error: any) {
                    console.log(error)
                    return error.statusText === 'Unknown Error' ? Observable.of(new outputActions.GetEditedOutputErrorAction(''))
                        : Observable.of(new errorActions.ServerErrorAction(error));
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
                return Observable.of(new errorActions.ServerErrorAction(error));
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
                return Observable.of(new errorActions.ServerErrorAction(error));
            });
        });


    @Effect()
    createOutput$: Observable<Action> = this.actions$
        .ofType(outputActions.CREATE_OUTPUT)
        .switchMap((data: any) => {
            return this.templatesService.createTemplate(data.payload).mergeMap((data: any) => {
                return [new outputActions.CreateOutputCompleteAction(), new outputActions.ListOutputAction];
            }).catch(function (error: any) {
                return Observable.of(new errorActions.ServerErrorAction(error));
            });
        });

    @Effect()
    updateOutput$: Observable<Action> = this.actions$
        .ofType(outputActions.UPDATE_OUTPUT)
        .switchMap((data: any) => {
            return this.templatesService.updateFragment(data.payload).mergeMap((data: any) => {
                return [new outputActions.UpdateOutputCompleteAction(), new outputActions.ListOutputAction];
            }).catch(function (error: any) {
                return Observable.of(new errorActions.ServerErrorAction(error));
            });
        });

    constructor(
        private actions$: Actions,
        private templatesService: TemplatesService
    ) { }

}