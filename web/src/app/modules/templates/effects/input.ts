/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { TemplatesService } from 'services/templates.service';
import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';

import { Effect, Actions, ofType } from '@ngrx/effects';

import { iif, of, forkJoin, Observable } from 'rxjs';
import { map, switchMap, mergeMap, catchError } from 'rxjs/operators';

import * as inputActions from './../actions/input';
import * as errorActions from 'actions/errors';


@Injectable()
export class InputEffect {

   @Effect()
   getInputList$: Observable<Action> = this.actions$
      .pipe(ofType(inputActions.LIST_INPUT))
      .pipe(switchMap((response: any) => this.templatesService.getTemplateList('input')
         .pipe(map((inputList: any) => new inputActions.ListInputCompleteAction(inputList)))
         .pipe(catchError(error => iif(() => error.statusText === 'Unknown Error',
            of(new inputActions.ListInputFailAction('')),
            of(new errorActions.ServerErrorAction(error))
         )))));

   @Effect()
   getInputTemplate$: Observable<Action> = this.actions$
      .pipe(ofType(inputActions.GET_EDITED_INPUT))
      .pipe(map((action: any) => action.payload))
      .pipe(switchMap((param: any) => this.templatesService.getTemplateById('input', param)
         .pipe(map(input => new inputActions.GetEditedInputCompleteAction(input)))
         .pipe(catchError(error => iif(() => error.statusText === 'Unknown Error',
            of(new inputActions.GetEditedInputErrorAction('')),
            of(new errorActions.ServerErrorAction(error))
         )))));

   @Effect()
   deleteInput$: Observable<Action> = this.actions$
      .pipe(ofType(inputActions.DELETE_INPUT))
      .pipe(map((action: any) => action.payload.selected))
      .pipe(switchMap((inputs: any) => {
         const joinObservables: Observable<any>[] = [];
         inputs.map((input: any) => {
            joinObservables.push(this.templatesService.deleteTemplate('input', input.id));
         });
         return forkJoin(joinObservables).pipe(mergeMap(results => [
            new inputActions.DeleteInputCompleteAction(inputs),
            new inputActions.ListInputAction()
         ])).pipe(catchError(function (error) {
            return of(new errorActions.ServerErrorAction(error));
         }));
      }));

   @Effect()
   duplicateInput$: Observable<Action> = this.actions$
      .pipe(ofType(inputActions.DUPLICATE_INPUT))
      .pipe(switchMap((data: any) => {
         const input = Object.assign(data.payload);
         delete input.id;
         return this.templatesService.createTemplate(input).pipe(mergeMap(() => [
            new inputActions.DuplicateInputCompleteAction(),
            new inputActions.ListInputAction
         ])).pipe(catchError((error: any) => of(new errorActions.ServerErrorAction(error))));
      }));

   @Effect()
   createInput$: Observable<Action> = this.actions$
      .pipe(ofType(inputActions.CREATE_INPUT))
      .pipe(switchMap((data: any) => this.templatesService.createTemplate(data.payload).pipe(mergeMap(() => [
         new inputActions.CreateInputCompleteAction(),
         new inputActions.ListInputAction
      ])).pipe(catchError((error: any) => of(new errorActions.ServerErrorAction(error))))));

   @Effect()
   updateInput$: Observable<Action> = this.actions$
      .pipe(ofType(inputActions.UPDATE_INPUT))
      .pipe(switchMap((data: any) => this.templatesService.updateFragment(data.payload)
         .pipe(mergeMap(() => [
            new inputActions.UpdateInputCompleteAction(),
            new inputActions.ListInputAction
         ])).pipe(catchError((error: any) => of(new errorActions.ServerErrorAction(error))))));

   constructor(
      private actions$: Actions,
      private templatesService: TemplatesService
   ) { }

}
