/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { TemplatesService } from 'services/templates.service';
import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';

import { Effect, Actions, ofType } from '@ngrx/effects';
import { Observable, of, iif, from, forkJoin } from 'rxjs';

import * as transformationActions from './../actions/transformation';
import * as errorActions from 'actions/errors';
import { switchMap, map, mergeMap, catchError } from 'rxjs/operators';

@Injectable()
export class TransformationEffect {

   @Effect()
   getTransformationList$: Observable<Action> = this.actions$
      .pipe(ofType(transformationActions.LIST_TRANSFORMATION))
      .pipe(switchMap((response: any) => this.templatesService.getTemplateList('transformation')
         .pipe(map((transformationList: any) => new transformationActions.ListTransformationCompleteAction(transformationList)))
         .pipe(catchError(error => iif(() => error.statusText === 'Unknown Error',
            of(new transformationActions.ListTransformationFailAction('')),
            of(new errorActions.ServerErrorAction(error))
         )))));

   @Effect()
   getTransformationTemplate$: Observable<Action> = this.actions$
      .pipe(ofType(transformationActions.GET_EDITED_TRANSFORMATION))
      .pipe(map((action: any) => action.payload))
      .pipe(switchMap((param: any) => this.templatesService.getTemplateById('transformation', param)
         .pipe(map((transformation: any) => new transformationActions.GetEditedTransformationCompleteAction(transformation)))
         .pipe(catchError(error => iif(() => error.statusText === 'Unknown Error',
            of(new transformationActions.GetEditedTransformationErrorAction('')),
            of(new errorActions.ServerErrorAction(error))
         )))));

   @Effect()
   deleteTransformation$: Observable<Action> = this.actions$
      .pipe(ofType(transformationActions.DELETE_TRANSFORMATION))
      .pipe(map((action: any) => action.payload.selected))
      .pipe(switchMap((transformations: any) => {
         const joinObservables: Observable<any>[] = [];
         transformations.map((transformation: any) => {
            joinObservables.push(this.templatesService.deleteTemplate('transformation', transformation.id));
         });
         return forkJoin(joinObservables).pipe(mergeMap(results => [
            new transformationActions.DeleteTransformationCompleteAction(transformations),
            new transformationActions.ListTransformationAction()]
         )).pipe(catchError(error => from([
            new transformationActions.DeleteTransformationErrorAction(''),
            new errorActions.ServerErrorAction(error)
         ])));
      }));

   @Effect()
   duplicateTransformation$: Observable<Action> = this.actions$
      .pipe(ofType(transformationActions.DUPLICATE_TRANSFORMATION))
      .pipe(switchMap((data: any) => {
         const transformation = Object.assign(data.payload);
         delete transformation.id;
         return this.templatesService.createTemplate(transformation).pipe(mergeMap(() => [
            new transformationActions.DuplicateTransformationCompleteAction(),
            new transformationActions.ListTransformationAction
         ])).pipe(catchError(error => of(new errorActions.ServerErrorAction(error))));
      }));

   @Effect()
   createTransformation$: Observable<Action> = this.actions$
      .pipe(ofType(transformationActions.CREATE_TRANSFORMATION))
      .pipe(switchMap((data: any) => this.templatesService.createTemplate(data.payload)
      .pipe(mergeMap(() => [
         new transformationActions.CreateTransformationCompleteAction(),
         new transformationActions.ListTransformationAction
      ])).pipe(catchError(error => of(new errorActions.ServerErrorAction(error))))
      ));

   @Effect()
   updateTransformation$: Observable<Action> = this.actions$
      .pipe(ofType(transformationActions.UPDATE_TRANSFORMATION))
      .pipe(switchMap((data: any) => this.templatesService.updateFragment(data.payload)
         .pipe(mergeMap(() => [
            new transformationActions.UpdateTransformationCompleteAction(),
            new transformationActions.ListTransformationAction
         ])).pipe(catchError(error => of(new errorActions.ServerErrorAction(error))))));

   constructor(
      private actions$: Actions,
      private templatesService: TemplatesService
   ) { }

}
