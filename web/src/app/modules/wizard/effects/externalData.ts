/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/concatMap';
import 'rxjs/add/operator/withLatestFrom';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/from';
import 'rxjs/add/operator/delay';
import 'rxjs/add/observable/if';
import 'rxjs/add/observable/empty';
import 'rxjs/add/observable/timer';
import 'rxjs/add/observable/throw';
import { of } from 'rxjs/observable/of';
import { Observable } from 'rxjs/Observable';

import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { Effect, Actions } from '@ngrx/effects';

import * as fromWizard from './../reducers';
import * as externalDataActions from './../actions/externalData';
import { EnvironmentService, ParametersService } from 'app/services';

@Injectable()
export class ExternalDataEffect {

   @Effect()
   getParamList$: Observable<any> = this._actions$
      .ofType(externalDataActions.GET_PARAMS_LIST)
      .switchMap(() => Observable.forkJoin([
         this._parametersService.getParamList(),
         this._parametersService.getGlobalParameters()
      ])
      .map((response) => new externalDataActions.GetParamsListCompleteAction(response))
      .catch(error => of(new externalDataActions.GetParamsListErrorAction())));

   constructor(
      private _actions$: Actions,
      private _store: Store<fromWizard.State>,
      private _environmentService: EnvironmentService,
      private _parametersService: ParametersService
   ) { }


}

