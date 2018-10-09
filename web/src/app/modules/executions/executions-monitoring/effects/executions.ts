/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { Effect, Actions } from '@ngrx/effects';
import {  Store } from '@ngrx/store';

import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/withLatestFrom';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/timer';
import 'rxjs/add/operator/takeUntil';
import 'rxjs/add/operator/concatMap';
import 'rxjs/add/observable/from';
import { Observable } from 'rxjs/Observable';

import * as executionsActions from '../actions/executions';
import * as fromRoot from '../reducers';
import { ExecutionService } from 'services/execution.service';

import { of } from 'rxjs/observable/of';
import { ExecutionHelperService } from 'app/services/helpers/execution.service';


@Injectable()
export class ExecutionsEffect {

   @Effect()
   getExecutionsList$: Observable<any> = this.actions$
      .ofType(executionsActions.LIST_EXECUTIONS)
      .switchMap(() => Observable.timer(0, 5000)
         .takeUntil(this.actions$.ofType(executionsActions.CANCEL_EXECUTION_POLLING))
         .concatMap(() => this._executionService.getDashboardExecutions()
         .map(executions => new executionsActions.ListExecutionsCompleteAction({
             executionsSummary: executions.executionsSummary,
             executionList: executions.lastExecutions.map(execution => this._executionHelperService.normalizeExecution(execution))
         }))
         .catch(err => of(new executionsActions.ListExecutionsFailAction()))));

   constructor(
      private actions$: Actions,
      private _executionHelperService: ExecutionHelperService,
      private store: Store<fromRoot.State>,
      private _executionService: ExecutionService
   ) { }
}
