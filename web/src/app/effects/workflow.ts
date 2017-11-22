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

import { WorkflowService } from 'services/workflow.service';
import { Injectable } from '@angular/core';
import { Action, Store } from '@ngrx/store';

import { WorkflowListType } from 'app/models/workflow.model';
import { Effect, Actions, toPayload } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import * as workflowActions from 'actions/workflow';
import * as fromRoot from 'reducers';
import { generateJsonFile } from 'utils';
import * as errorsActions from 'actions/errors';


@Injectable()
export class WorkflowEffect {

    @Effect()
    getWorkflowList$: Observable<Action> = this.actions$
        .ofType(workflowActions.LIST_WORKFLOW).switchMap((response: any) => {

            const context$ = this.workflowService.getWorkFlowContextList();
            const workflows$ = this.workflowService.getWorkflowList();
            return Observable.combineLatest(workflows$, context$, (workflows, context) => {
                workflows.map((workflow: any) => {
                    const c = context.filter((item: any) => {
                        return workflow.id === item.id;
                    });
                    return workflow.context = (c && Array.isArray(c) && c.length) ? c[0] : {};
                });
                return new workflowActions.ListWorkflowCompleteAction(workflows);
            }).catch((error: any) => {
                return Observable.from([new workflowActions.ListWorkflowFailAction(), new errorsActions.HttpErrorAction(error)]);
            });
        });

    @Effect()
    updateWorkflowStatus$: Observable<Action> = this.actions$
        .ofType(workflowActions.UPDATE_WORKFLOWS)
        .switchMap((r: any) => {
            return this.workflowService.getWorkFlowContextList().map((response: any) => {
                return new workflowActions.UpdateWorkflowStatusCompleteAction(response);
            }).catch(function (error) {
                return Observable.from([new workflowActions.UpdateWorkflowStatusErrorAction(), new errorsActions.HttpErrorAction(error)]);
            });
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
                return Observable.from([new workflowActions.DeleteWorkflowErrorAction(), new errorsActions.HttpErrorAction(error)]);

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
            return Observable.from([new workflowActions.DownloadWorkflowsCompleteAction('')]);
        });

    @Effect()
    runWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.RUN_WORKFLOW)
        .switchMap((data: any) => {
            return this.workflowService.runWorkflow(data.payload.id).map((response: any) => {
                return new workflowActions.RunWorkflowCompleteAction(data.payload.name);
            }).catch(function (error) {
                return Observable.from([new workflowActions.RunWorkflowErrorAction(), new errorsActions.HttpErrorAction(error)]);
            });
        });

    @Effect()
    stopWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.STOP_WORKFLOW)
        .switchMap((data: any) => {
            return this.workflowService.stopWorkflow(data.payload).map((response: any) => {
                console.log(data.payload);
                return new workflowActions.StopWorkflowCompleteAction(data.payload);
            }).catch(function (error) {
                return Observable.from([new workflowActions.StopWorkflowErrorAction(), new errorsActions.HttpErrorAction(error)]);

            });
        });

    @Effect()
    validateWorkflowName$: Observable<Action> = this.actions$
        .ofType(workflowActions.VALIDATE_WORKFLOW_NAME)
        .switchMap((response: any) => {
            return this.workflowService.getWorkflowByName(response.payload.name).map((response: any) => {
                return new workflowActions.ValidateWorkflowNameError();
            }).catch(function (error) {
                return Observable.from([new workflowActions.SaveJsonWorkflowAction(response.payload)]);

            });
        });


    @Effect()
    saveJsonWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.SAVE_JSON_WORKFLOW)
        .switchMap((response: any) => {
            delete response.payload.id;
            return this.workflowService.saveWorkflow(response.payload).mergeMap((response: any) => {
                return [new workflowActions.SaveJsonWorkflowActionComplete(), new workflowActions.ListWorkflowAction()];
            }).catch(function (error) {
                return Observable.from([new workflowActions.SaveJsonWorkflowActionError(), new errorsActions.HttpErrorAction(error)]);

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
                return Observable.from([new workflowActions.GetExecutionInfoErrorAction(), new errorsActions.HttpErrorAction(error)]);

            });
        });

    @Effect() httpError$ = this.actions$
        .ofType(errorsActions.HTTP_ERROR)
        .map((action: any) => action.payload)
        .switchMap((payload: any) => {
            if (payload && payload.status) {
                if (payload.status === 401) {
                    return Observable.of(new errorsActions.ForbiddenErrorAction(''));
                }
            }

            if (payload.error) {
                try {
                    const error = JSON.parse(payload.error);
                    if (error.message && error.exception) {
                        return Observable.of(new errorsActions.ServerErrorAction({
                            title: error.message,
                            description: error.exception
                        }));
                    }
                } catch (e) {

                }
            }

            return Observable.empty();
        });

    constructor(
        private actions$: Actions,
        private store: Store<fromRoot.State>,
        private workflowService: WorkflowService
    ) { }
}
