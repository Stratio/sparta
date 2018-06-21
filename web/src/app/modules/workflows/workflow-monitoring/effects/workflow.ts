/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { Effect, Actions } from '@ngrx/effects';
import { Action, Store } from '@ngrx/store';

import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/withLatestFrom';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/from';
import { from } from 'rxjs/observable/from';
import { empty } from 'rxjs/observable/empty';
import { Observable } from 'rxjs/Observable';

import { isEqual } from 'underscore';

import * as errorActions from 'actions/errors';
import * as workflowActions from './../actions/workflows';
import * as fromRoot from './../reducers';
import { WorkflowService } from 'services/workflow.service';
import { generateJsonFile, formatDate, getFilterStatus } from '@utils';


@Injectable()
export class WorkflowEffect {
   reverse = true;
   @Effect()
   getWorkflowList$: Observable<any> = this.actions$
      .ofType(workflowActions.LIST_WORKFLOW)
      .withLatestFrom(this.store.select(state => state.workflows.workflows))
      .switchMap(([payload, workflowsState]: [any, any]) =>
         this.workflowService.findAllMonitoring().mergeMap((workflows: any) => {
            workflows.map((workflow: any) => {
               workflow.filterStatus = getFilterStatus(workflow.status.status);
               workflow.tagsAux = workflow.tags ? workflow.tags.join(', ') : '';
               try {
                  const sparkURI = workflow.execution.marathonExecution.sparkURI;
                  if (sparkURI.length) {
                     workflow.sparkURI = sparkURI;
                  }
               } catch (error) { }
                  try {
                     const lastErrorDate = workflow.execution.genericDataExecution.lastError.date;
                     workflow.lastErrorDate = formatDate(lastErrorDate, true, true);
                  } catch (error) { }
               try {
                  const sparkURI = workflow.execution.localExecution.sparkURI;
                  if (sparkURI.length) {
                     workflow.sparkURI = sparkURI;
                  }
               } catch (error) { }
               try {
                  workflow.lastUpdate = workflow.status.lastUpdateDate ? formatDate(workflow.status.lastUpdateDate) : '';
                  workflow.lastUpdateOrder = workflow.status.lastUpdateDate ? new Date(workflow.status.lastUpdateDate).getTime() : 0;
               } catch (error) { }
            });
            return isEqual(workflows, workflowsState.workflowList) && workflows.length ? empty() :
               from([
                  new workflowActions.ListWorkflowCompleteAction(workflows),
                  new workflowActions.ValidateSelectedAction()
               ]);
         })
            .catch(error => error.statusText === 'Unknown Error' ?
               from([new workflowActions.ListWorkflowFailAction(), new errorActions.ServerErrorAction(error)]) :
               Observable.of(new errorActions.ServerErrorAction(error))));

   @Effect()
   deleteWorkflow$: Observable<Action> = this.actions$
      .ofType(workflowActions.DELETE_WORKFLOW)
      .map((action: any) => action.payload)
      .switchMap(workflows => Observable.forkJoin(workflows.map(workflow =>
         this.workflowService.deleteWorkflow(workflow.id)))
         .mergeMap(results => [new workflowActions.DeleteWorkflowCompleteAction(workflows), new workflowActions.ListWorkflowAction()])
         .catch(error => from([new workflowActions.DeleteWorkflowErrorAction(), new errorActions.ServerErrorAction(error)])));

   @Effect()
   downloadWorkflow$: Observable<Action> = this.actions$
      .ofType(workflowActions.DOWNLOAD_WORKFLOWS)
      .map((action: any) => action.payload)
      .switchMap((workflows: Array<any>) => Observable.forkJoin(workflows.map(workflow =>
         this.workflowService.downloadWorkflow(workflow.id)))
         .mergeMap(results => {
            results.forEach((data: any) => generateJsonFile(data.name, data));
            return Observable.of(new workflowActions.DownloadWorkflowsCompleteAction(''));
         }).catch(error => Observable.of(new errorActions.ServerErrorAction(error))));

   @Effect()
   runWorkflow$: Observable<Action> = this.actions$
      .ofType(workflowActions.RUN_WORKFLOW)
      .switchMap((data: any) => this.workflowService.runWorkflow(data.payload.id)
         .map((response: any) => new workflowActions.RunWorkflowCompleteAction(data.payload.name))
         .catch(error => from([new workflowActions.RunWorkflowErrorAction(), new errorActions.ServerErrorAction(error)])));

   @Effect()
   stopWorkflow$: Observable<Action> = this.actions$
      .ofType(workflowActions.STOP_WORKFLOW)
      .switchMap((data: any) => this.workflowService.stopWorkflow(data.payload)
         .map(response => new workflowActions.StopWorkflowCompleteAction(data.payload))
         .catch(error => from([
            new workflowActions.StopWorkflowErrorAction(),
            new errorActions.ServerErrorAction(error)
         ])));


   @Effect()
   getExecutionInfo$: Observable<Action> = this.actions$
      .ofType(workflowActions.GET_WORKFLOW_EXECUTION_INFO)
      .switchMap((data: any) => this.workflowService.getWorkflowExecutionInfo(data.payload.id)
         .map((response: any) => new workflowActions.GetExecutionInfoCompleteAction({
            ...response,
            name: data.payload.name
         })).catch(error => from([new workflowActions.GetExecutionInfoErrorAction(), new errorActions.ServerErrorAction(error)])));

   constructor(
      private actions$: Actions,
      private store: Store<fromRoot.State>,
      private workflowService: WorkflowService
   ) { }
}
