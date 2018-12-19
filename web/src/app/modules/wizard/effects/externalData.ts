/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { Effect, Actions, ofType } from '@ngrx/effects';
import { forkJoin, Observable, of } from 'rxjs';
import { switchMap, map, catchError } from 'rxjs/operators';

import * as fromWizard from './../reducers';
import * as externalDataActions from './../actions/externalData';
import { EnvironmentService, ParametersService } from 'app/services';

@Injectable()
export class ExternalDataEffect {

   @Effect()
   getParamList$: Observable<any> = this._actions$
      .pipe(ofType(externalDataActions.GET_PARAMS_LIST))
      .pipe(switchMap(() => forkJoin([
         this._parametersService.getParamList(),
         this._parametersService.getGlobalParameters()
      ])
      .pipe(map((response) => new externalDataActions.GetParamsListCompleteAction(response)))
      .pipe(catchError(error => of(new externalDataActions.GetParamsListErrorAction())))));

   constructor(
      private _actions$: Actions,
      private _store: Store<fromWizard.State>,
      private _environmentService: EnvironmentService,
      private _parametersService: ParametersService
   ) { }
}

