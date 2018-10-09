/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { Effect, Actions } from '@ngrx/effects';
import { Store, Action } from '@ngrx/store';
import { from } from 'rxjs/observable/from';

import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/withLatestFrom';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/timer';
import 'rxjs/add/operator/takeUntil';
import 'rxjs/add/operator/concatMap';
import 'rxjs/add/observable/from';
import { Observable } from 'rxjs/Observable';

import * as errorActions from 'actions/errors';
import * as executionsActions from '../actions/executions';
import * as fromRoot from '../reducers';
import { ExecutionService } from 'services/execution.service';
import { isEqual } from 'lodash';

import { of } from 'rxjs/observable/of';
import { ExecutionHelperService } from 'app/services/helpers/execution.service';


@Injectable()
export class ExecutionsEffect {

   private _lastExecutionsValue: any;

   @Effect()
   getExecutionsList$: Observable<any> = this.actions$
      .ofType(executionsActions.LIST_EXECUTIONS)
      .switchMap(() => Observable.timer(0, 5000)
         .takeUntil(this.actions$.ofType(executionsActions.CANCEL_EXECUTION_POLLING))
         .concatMap(() => this._executionService.getExecutionsByQuery({ archived: false })
            .map(executions => {
               if (isEqual(executions, this._lastExecutionsValue)) {
                  return { type: 'NO_ACTION' };
               }
               this._lastExecutionsValue = executions;
               return new executionsActions.ListExecutionsCompleteAction(
                  executions.map(execution => this._executionHelperService.normalizeExecution(execution)))
            }).catch(err => of(new executionsActions.ListExecutionsFailAction()))));

   @Effect()
   getArhivedExecutionsList: Observable<any> = this.actions$
      .ofType(executionsActions.LIST_ARCHIVED_EXECUTIONS)
      .switchMap(executions =>  this._executionService.getExecutionsByQuery({ archived: true})
         .map((archived: Array<any>) => new executionsActions.ListArchivedExecutionsCompleteAction(archived.map(execution =>
         this._executionHelperService.normalizeExecution(execution)))))
      .catch(error => of(new executionsActions.ListArchivedExecutionsFailAction()));

   @Effect()
   archiveExecutions: Observable<any> = this.actions$
      .ofType(executionsActions.ARCHIVE_EXECUTIONS)
      .withLatestFrom(this.store.select(state => state.executions.executions.selectedExecutionsIds))
      .switchMap(([action, ids]) => {
         const observables: any = [];
         ids.forEach(id => observables.push(this._executionService.archiveExecution(id, true)));
         return Observable.forkJoin(observables)
            .mergeMap(() => [new executionsActions.ListExecutionsAction(), new executionsActions.ArchiveExecutionsCompleteAction()])
            .catch(error => of(new executionsActions.ArchiveExecutionsFailAction()));
      });

   @Effect()
   unarchiveExecutions: Observable<any> = this.actions$
      .ofType(executionsActions.UNARCHIVE_EXECUTIONS)
      .withLatestFrom(this.store.select(state => state.executions.executions.selectedExecutionsIds))
      .switchMap(([action, ids]) => {
         const observables: any = [];
         ids.forEach(id => observables.push(this._executionService.archiveExecution(id, false)));
         return Observable.forkJoin(observables)
            .mergeMap(() => [new executionsActions.ListArchivedExecutionsAction(), new executionsActions.UnarchiveExecutionsCompleteAction()])
            .catch(error => of(new executionsActions.UnarchiveExecutionsFailAction()));
      });


   @Effect()
   selectStatusFilter: Observable<any> = this.actions$
      .ofType(executionsActions.SELECT_STATUS_FILTER)
      .switchMap((action: any) =>
         action.status === 'Archived' ? of(new executionsActions.ListArchivedExecutionsAction()) : of({ type: 'NO_ACTION' }));

   @Effect()
   stopExecutionsList$: Observable<any> = this.actions$
      .ofType(executionsActions.STOP_EXECUTIONS_ACTION)
      .withLatestFrom(this.store.select(state => state.executions.executions.selectedExecutionsIds))
      .switchMap(([action, ids]) => {
         const observables: any = [];
         ids.forEach(id => observables.push(this._executionService.stopExecutionsById(id)));

         return Observable.forkJoin(observables)
            .mergeMap((results: any) => {
               const actions: Array<Action> = [];
               if (results.length) {
                  actions.push(new executionsActions.ListExecutionsAction());
               }
               return actions;
            })
            .catch(error => of(new executionsActions.ListExecutionsFailAction()));
      });


   @Effect()
   getExecutionInfo$: Observable<Action> = this.actions$
      .ofType(executionsActions.GET_WORKFLOW_EXECUTION_INFO)
      .switchMap((data: any) => this._executionService.getWorkflowExecutionInfo(data.payload.id)
         .map((response: any) => new executionsActions.GetExecutionInfoCompleteAction({
            ...response,
            name: data.payload.name
         })).catch(error => from([new executionsActions.GetExecutionInfoErrorAction(), new errorActions.ServerErrorAction(error)])));

   constructor(
      private actions$: Actions,
      private _executionHelperService: ExecutionHelperService,
      private store: Store<fromRoot.State>,
      private _executionService: ExecutionService
   ) { }
}
