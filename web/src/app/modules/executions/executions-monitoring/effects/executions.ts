/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { Effect, Actions, ofType } from '@ngrx/effects';
import { Action } from '@ngrx/store';

import { Observable, timer, of } from 'rxjs';

import * as executionsActions from '../actions/executions';
import { ExecutionService } from 'services/execution.service';

import { ExecutionHelperService } from 'app/services/helpers/execution.service';
import { catchError, concatMap, switchMap, takeUntil, map } from 'rxjs/operators';
import { ExecutionPeriodsService } from '../services/execution-periods.service';
import { StAlertsService, STALERT_SEVERITY } from '@stratio/egeo';


@Injectable()
export class ExecutionsEffect {
  private _pollingError: boolean = false;
  private _lastErrorTime: Date;
  private _firstError: boolean = false;

  @Effect()
  getExecutionsList$: Observable<Action> = this.actions$
    .pipe(ofType(executionsActions.LIST_EXECUTIONS))
    .pipe(switchMap(() => timer(0, 5000)
      .pipe(takeUntil(this.actions$.pipe(ofType(executionsActions.CANCEL_EXECUTION_POLLING))))
      .pipe(concatMap(() =>
        this._executionService.getDashboardExecutions()
          .pipe(map((executions: any) => {
            this._pollingError = false;
            this._firstError = false;
            return new executionsActions.ListExecutionsCompleteAction({
                executionsSummary: executions.executionsSummary,
                executionList: executions.lastExecutions.map(execution =>
                  this._executionHelperService.normalizeExecution(execution))
              });
          }),
        catchError( () => {
          const currentTime: Date = new Date();
          this._pollingError = true;

          if (!this._lastErrorTime) {
            this._lastErrorTime = new Date();
          }

          if ((currentTime.getTime() - this._lastErrorTime.getTime()) > 30000 || !this._firstError) {
            this._alertsService.notifyAlert(
              'Error',
              'There was an internal server error',
              STALERT_SEVERITY.ERROR,
              null,
              5000);

            this._lastErrorTime = new Date();
            this._firstError = true;
          }

          return of(new executionsActions.ListExecutionsFailAction());
        })
      ))
    )));

  @Effect()
  getExecutionPeriodData: Observable<any> = this.actions$
    .pipe(ofType(executionsActions.SET_GRAPH_DATA_PERIOD))
    .pipe(map((action: any) => action.payload))
    .pipe(switchMap((payload: any) => {

      return this._executionPeriodsService.getExecutionPeriodData(payload)
        .pipe(map((response: any) => {
          return new executionsActions.GetGraphDataPeriodCompleteAction(response);
        }));

    }));


  constructor(
    private actions$: Actions,
    private _alertsService: StAlertsService,
    private _executionHelperService: ExecutionHelperService,
    private _executionPeriodsService: ExecutionPeriodsService,
    private _executionService: ExecutionService
  ) { }
}
