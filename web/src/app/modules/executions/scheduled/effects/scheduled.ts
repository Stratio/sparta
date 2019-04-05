/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Store, Action, select } from '@ngrx/store';
import { Observable, timer, of, forkJoin, from, iif } from 'rxjs';

import * as scheduledActions from '../actions/scheduled';
import * as fromRoot from '../reducers';
import { ScheduledService } from 'services/scheduled.service';
import { isEqual } from 'lodash';

import { switchMap, takeUntil, concatMap, catchError, withLatestFrom, mergeMap, map, tap } from 'rxjs/operators';
import { SchedulerHelperService } from '../services/scheduled-helper.service';


@Injectable()
export class ScheduledEffect {
    private _lastSqueduledExecutionsValue: any;
    @Effect()
    getScheduledExecutionsList$: Observable<any> = this.actions$
      .pipe(ofType(scheduledActions.ScheduledActions.LIST_SCHEDULED_EXECUTIONS))
      .pipe(switchMap(() => timer(0, 5000)
      .pipe(takeUntil(this.actions$.pipe(ofType(scheduledActions.ScheduledActions.CANCEL_LIST_SCHEDULED_EXECUTIONS))))
      .pipe(withLatestFrom(this.store.pipe(select((state: any) => state.executions.executions))))
      .pipe(concatMap(([status, filter]) => this._scheduledService.getScheduledWorkflowTasks()))
      .pipe(map((executions: Array<any>) => {
        if (isEqual(executions, this._lastSqueduledExecutionsValue)) {
          return {type: 'NO_ACTION'};
        }
        this._lastSqueduledExecutionsValue = executions;
        return new scheduledActions.ListScheduledExecutionsCompleteAction(
          executions.map(execution => this._scheduledHelperService.normalizeScheduledExecution(execution)));
      }))
      .pipe(catchError(err => of(new scheduledActions.ListScheduledExecutionsFailedAction())))));


  constructor(
    private actions$: Actions,
    private _scheduledHelperService: SchedulerHelperService,
    private store: Store<fromRoot.State>,
    private _scheduledService: ScheduledService
  ) { }
}
