/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Effect, Actions, ofType } from '@ngrx/effects';

import { from, Observable } from 'rxjs';
import { catchError, map, mergeMap, switchMap } from 'rxjs/operators';

import { generateJsonFile } from '@utils';
import * as errorActions from 'actions/errors';
import * as environmentActions from './../actions/environment';
import { EnvironmentService } from 'services/environment.service';


@Injectable()
export class EnvironmentEffect {

   @Effect()
   getEnvironmentList$: Observable<Action> = this.actions$
      .pipe(ofType(environmentActions.LIST_ENVIRONMENT))
      .pipe(switchMap((response: any) => this.environmentService.getEnvironment()
         .pipe(map((environmentList: any) => new environmentActions.ListEnvironmentCompleteAction(environmentList)))
         .pipe(catchError(error => from([
            new environmentActions.ListEnvironmentErrorAction(''),
            new errorActions.ServerErrorAction(error)
         ])))
      ));


   @Effect()
   saveEnvironmentList$: Observable<Action> = this.actions$
      .pipe(ofType(environmentActions.SAVE_ENVIRONMENT))
      .pipe(switchMap((response: any) => this.environmentService.updateEnvironment(response.payload)
         .pipe(map(environmentList => new environmentActions.ListEnvironmentAction()))
         .pipe(catchError(error => from([
            new environmentActions.SaveEnvironmentErrorAction(''),
            new errorActions.ServerErrorAction(error)
         ]))))
      );

   @Effect()
   importEnvironment$: Observable<Action> = this.actions$
      .pipe(ofType(environmentActions.IMPORT_ENVIRONMENT))
      .pipe(switchMap((response: any) => this.environmentService.importEnvironment(response.payload)
         .pipe(mergeMap((environmentList: any) => [
            new environmentActions.ImportEnvironmentCompleteAction(),
            new environmentActions.ListEnvironmentAction()
         ]))
         .pipe(catchError(error => from([
            new environmentActions.ImportEnvironmentErrorAction(),
            new errorActions.ServerErrorAction(error)
         ])))
      ));

   @Effect()
   exportEnvironment$: Observable<Action> = this.actions$
      .pipe(ofType(environmentActions.EXPORT_ENVIRONMENT))
      .pipe(switchMap((response: any) => this.environmentService.exportEnvironment()
         .pipe(map(envData => {
            generateJsonFile('environment-data', envData);
            return new environmentActions.ExportEnvironmentCompleteAction();
         })).pipe(catchError(error => from([
            new environmentActions.ExportEnvironmentErrorAction(),
            new errorActions.ServerErrorAction(error)
         ])))
      ));

   constructor(
      private actions$: Actions,
      private environmentService: EnvironmentService
   ) { }

}
