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

import * as outputActions from './../actions/output';
import * as errorActions from 'actions/errors';
import { catchError, map, mergeMap, switchMap } from 'rxjs/operators';

@Injectable()
export class OutputEffect {

  @Effect()
  getOutputList$: Observable<Action> = this.actions$
    .pipe(ofType(outputActions.LIST_OUTPUT))
    .pipe(switchMap((response: any) => this.templatesService.getTemplateList('output')
      .pipe(map((outputList: any) => new outputActions.ListOutputCompleteAction(outputList)))
      .pipe(catchError(error => iif(() => error.statusText === 'Unknown Error',
        of(new outputActions.ListOutputFailAction('')),
        of(new errorActions.ServerErrorAction(error))
      )))
    ));

  @Effect()
  getOutputTemplate$: Observable<Action> = this.actions$
    .pipe(ofType(outputActions.GET_EDITED_OUTPUT))
    .pipe(map((action: any) => action.payload))
    .pipe(switchMap((param: any) => this.templatesService.getTemplateById('output', param)
      .pipe(map((output: any) => new outputActions.GetEditedOutputCompleteAction(output)))
      .pipe(catchError(error => iif(() => error.statusText === 'Unknown Error',
        of(new outputActions.GetEditedOutputErrorAction('')),
        of(new errorActions.ServerErrorAction(error))
      )))
    ));

  @Effect()
  deleteOutput$: Observable<Action> = this.actions$
    .pipe(ofType(outputActions.DELETE_OUTPUT))
    .pipe(map((action: any) => action.payload.selected))
    .pipe(switchMap((outputs: any) => {
      const joinObservables: Observable<any>[] = [];
      outputs.map((output: any) => {
        joinObservables.push(this.templatesService.deleteTemplate('output', output.id));
      });
      return forkJoin(joinObservables).pipe(mergeMap(results => [
        new outputActions.DeleteOutputCompleteAction(outputs),
        new outputActions.ListOutputAction()
      ])).pipe(catchError((error) => {
        return of(new errorActions.ServerErrorAction(error));
      }));
    }));

  @Effect()
  duplicateOutput$: Observable<Action> = this.actions$
    .pipe(ofType(outputActions.DUPLICATE_OUTPUT))
    .pipe(switchMap((data: any) => {
      const output = Object.assign(data.payload);
      delete output.id;
      return this.templatesService.createTemplate(output)
        .pipe(mergeMap(() => [
          new outputActions.DuplicateOutputCompleteAction(),
          new outputActions.ListOutputAction
        ])).pipe(catchError((error: any) => of(new errorActions.ServerErrorAction(error))));
    }));

  @Effect()
  createOutput$: Observable<Action> = this.actions$
    .pipe(ofType(outputActions.CREATE_OUTPUT))
    .pipe(switchMap((data: any) => this.templatesService.createTemplate(data.payload)
      .pipe(mergeMap(() => [
        new outputActions.CreateOutputCompleteAction(),
        new outputActions.ListOutputAction
      ])).pipe(catchError((error: any) => of(new errorActions.ServerErrorAction(error))))));

  @Effect()
  updateOutput$: Observable<Action> = this.actions$
    .pipe(ofType(outputActions.UPDATE_OUTPUT))
    .pipe(switchMap((data: any) => this.templatesService.updateFragment(data.payload)
      .pipe(mergeMap(() => [
        new outputActions.UpdateOutputCompleteAction(),
        new outputActions.ListOutputAction
      ])).pipe(catchError((error: any) => of(new errorActions.ServerErrorAction(error))))));

  constructor(
    private actions$: Actions,
    private templatesService: TemplatesService
  ) { }
}
