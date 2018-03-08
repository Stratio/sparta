///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { Injectable } from '@angular/core';
import { Effect, Actions } from '@ngrx/effects';
import { Action, Store } from '@ngrx/store';
import { Observable } from 'rxjs/Observable';
import * as _ from 'underscore';

import * as errorActions from 'actions/errors';
import * as workflowActions from './../actions/workflow-list';
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
        .switchMap(([payload, workflowsState]: [any, any]) => {
            return this.workflowService.findAllMonitoring().mergeMap((workflows: any) => {
                workflows.map((workflow: any) => {
                    workflow.filterStatus = getFilterStatus(workflow.status.status);
                    workflow.tagsAux = workflow.tags ? workflow.tags.join(', ') : '';
                    try {
                        workflow.lastUpdate = workflow.status.lastUpdateDate ? formatDate(workflow.status.lastUpdateDate) : '';
                        workflow.lastUpdateOrder = workflow.lastUpdateDate ? new Date(workflow.lastUpdateDate).getTime() : 0;
                    } catch (error) { }
                });
                if (_.isEqual(workflows, workflowsState.workflowList)) {
                    return Observable.empty();
                } else {
                    return Observable.of(new workflowActions.ListWorkflowCompleteAction(workflows));
                }
            });

        }).catch((error: any) => {
            return error.statusText === 'Unknown Error' ?
                Observable.from([
                    new workflowActions.ListWorkflowFailAction(),
                    new errorActions.ServerErrorAction(error)
                ]) : Observable.of(new errorActions.ServerErrorAction(error));
        });

    @Effect()
    deleteWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.DELETE_WORKFLOW)
        .map((action: any) => action.payload)
        .switchMap((workflows: any) => {
            const joinObservables: Observable<any>[] = [];
            workflows.map((workflow: any) => {
                joinObservables.push(this.workflowService.deleteWorkflow(workflow.id));
            });

            return Observable.forkJoin(joinObservables).mergeMap(results => {
                return [new workflowActions.DeleteWorkflowCompleteAction(workflows), new workflowActions.ListWorkflowAction()];
            }).catch(function (error: any) {
                return Observable.from([
                    new workflowActions.DeleteWorkflowErrorAction(),
                    new errorActions.ServerErrorAction(error)
                ]);

            });
        });

    @Effect()
    downloadWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.DOWNLOAD_WORKFLOWS)
        .map((action: any) => action.payload)
        .switchMap((payload: any) => {
            const $downloadsSubscriptions = [];
            for (const workflow of payload) {
                $downloadsSubscriptions.push(this.workflowService.downloadWorkflow(workflow.id));
            }
            return Observable.forkJoin($downloadsSubscriptions);
        })
        .mergeMap((results: any[], index: number) => {
            results.forEach((data: any) => {
                generateJsonFile(data.name, data);
            });
            return Observable.of(new workflowActions.DownloadWorkflowsCompleteAction(''));
        }).catch((error: any) => {
            return Observable.of(new errorActions.ServerErrorAction(error));
        });

    @Effect()
    runWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.RUN_WORKFLOW)
        .switchMap((data: any) => {
            return this.workflowService.runWorkflow(data.payload.id).map((response: any) => {
                return new workflowActions.RunWorkflowCompleteAction(data.payload.name);
            }).catch(function (error) {
                return Observable.from([
                    new workflowActions.RunWorkflowErrorAction(),
                    new errorActions.ServerErrorAction(error)
                ]);
            });
        });

    @Effect()
    stopWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.STOP_WORKFLOW)
        .switchMap((data: any) => {
            return this.workflowService.stopWorkflow(data.payload).map((response: any) => {
                return new workflowActions.StopWorkflowCompleteAction(data.payload);
            }).catch(function (error) {
                return Observable.from([
                    new workflowActions.StopWorkflowErrorAction(),
                    new errorActions.ServerErrorAction(error)
                ]);
            });
        });


    @Effect()
    getExecutionInfo$: Observable<Action> = this.actions$
        .ofType(workflowActions.GET_WORKFLOW_EXECUTION_INFO)
        .switchMap((data: any) => {
            return this.workflowService.getWorkflowExecutionInfo(data.payload.id).map((response: any) => {
                response.name = data.payload.name;
                return new workflowActions.GetExecutionInfoCompleteAction(response);
            }).catch(function (error) {
                return Observable.from([
                    new workflowActions.GetExecutionInfoErrorAction(),
                    new errorActions.ServerErrorAction(error)
                ]);
            });
        });

    constructor(
        private actions$: Actions,
        private store: Store<fromRoot.State>,
        private workflowService: WorkflowService
    ) { }
}
