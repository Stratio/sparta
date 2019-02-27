/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import {Action, Store} from '@ngrx/store';
import { Effect, Actions, ofType } from '@ngrx/effects';
import {Observable, of, timer} from 'rxjs';
import {switchMap, map, catchError, takeUntil, mergeMap} from 'rxjs/operators';

import * as executionDetailActions from './../actions/execution-detail';
import { ExecutionService } from 'services/execution.service';
import {ExecutionDetailHelperService} from '@app/executions/execution-detail/services/execution-detail.service';
import * as fromRoot from '@app/executions/executions-managing/reducers';
import {Router} from '@angular/router';
import * as errorActions from 'actions/errors';

@Injectable()
export class ExecutionDetailEffect {

  @Effect()
  getExecutionDetail$: Observable<Action> = this.actions$
    .pipe(ofType(executionDetailActions.GET_EXECUTION_DETAIL))
    .pipe(map((action: any) => action.executionId))
    .pipe(switchMap((executionId) =>
      timer(0, 3000).pipe(switchMap(() =>
        this._executionService.getExecutionById(executionId)
      ))
      .pipe(takeUntil(this.actions$.pipe(ofType(executionDetailActions.CANCEL_POLLING))))
      .pipe(map(response => {
        return {
          info: this._executionDetailHelperService.getExecutionDetail(response),
          parameters: this._executionDetailHelperService.getExecutionParameters(response),
          statuses: this._executionDetailHelperService.getExecutionStatuses(response),
          showedActions: this._executionDetailHelperService.getShowedActions(response)
        };
      }))
      .pipe(mergeMap(executionDetail => {
        const actions: Array<Action> = [];
        const reducerAction = new executionDetailActions.CreateExecutionDetailAction(executionDetail);
        actions.push(reducerAction);
        if (executionDetail.info.status === 'Stopped' || executionDetail.info.status === 'Failed') {
          const cancelAction = new executionDetailActions.CancelPollingAction();
          actions.push(cancelAction);
        }
        return actions;
      }))
    ));

  @Effect()
  archiveExecutions: Observable<any> = this.actions$
  .pipe(ofType(executionDetailActions.ARCHIVE_EXECUTION))
  .pipe(switchMap((action: any) =>
    this._executionService.archiveExecution([action.executionId], true).pipe(map(() => {
      this.route.navigate(['executions']);
      return {type: 'NO_ACTION'};
    }))
    .pipe(catchError(error => {
      let message = {
        title: "Error",
        description: "Workflow couldnt be archived"
      };
      return of(new errorActions.ServerErrorCompleteAction(message));
    }))
  ));

  @Effect()
  unarchiveExecutions: Observable<any> = this.actions$
    .pipe(ofType(executionDetailActions.UNARCHIVE_EXECUTION))
    .pipe(switchMap((action: any) =>
      this._executionService.archiveExecution([action.executionId], false).pipe(map(()=>{
        this.route.navigate(['executions']);
        return {type: 'NO_ACTION'};
      }))
      .pipe(catchError(error => {
        let message = {
          title: "Error",
          description: "Workflow couldnt be unarchived"
        };
        return of(new errorActions.ServerErrorCompleteAction(message));
      }))
    ));

  @Effect()
  deleteExecution: Observable<any> = this.actions$
    .pipe(ofType(executionDetailActions.DELETE_EXECUTION))
    .pipe(switchMap((action: any) =>
      this._executionService.deleteExecution(action.executionId).pipe(map(()=>{
        this.route.navigate(['executions']);
        return {type: 'NO_ACTION'};
      }))
      .pipe(catchError((error) => {
        let message = {
          title: "Error",
          description: "Workflow couldnt be deleted"
        };
        return of(new errorActions.ServerErrorCompleteAction(message));
      }))
  ));

  @Effect()
  stopExecution$: Observable<Action> = this.actions$
    .pipe(ofType(executionDetailActions.STOP_EXECUTION))
    .pipe(map((action: any) => action.executionId))
    .pipe(switchMap((executionId: any) =>
      this._executionService.stopExecutionsById(executionId)
      .pipe(map(status => new executionDetailActions.GetExecutionDetailAction(executionId)))
      .pipe(map(() => {return {type: 'NO_ACTION'}}))
      .pipe(catchError(error => {
        const message = {
          title: 'Error',
          description: 'Workflow couldnt be stopped'
        };
        return of(new errorActions.ServerErrorCompleteAction(message));
      }))
    ));

  @Effect()
  rerunExecution$: Observable<Action> = this.actions$
    .pipe(ofType(executionDetailActions.RERUN_EXECUTION))
    .pipe(map((action: any) => action.executionId))
    .pipe(switchMap((executionId) => this._executionService.reRunExecution(executionId)))
    .pipe(map(() => {
      this.route.navigate(['executions']);
      return {type: 'NO_ACTION'};
    })).pipe(catchError(error => {
      const message = {
        title: 'Error',
        description: 'Workflow couldnt be relaunched'
      };
      return of(new errorActions.ServerErrorCompleteAction(message));
    }));

  constructor(
    private actions$: Actions,
    private _executionService: ExecutionService,
    private store: Store<fromRoot.State>,
    private _executionDetailHelperService: ExecutionDetailHelperService,
    private route: Router
  ) { }
}
