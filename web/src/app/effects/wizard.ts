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
import { OutputService } from 'services/output.service';
import { Injectable } from '@angular/core';
import { Action, Store } from '@ngrx/store';
import { OutputType } from 'app/models/output.model';
import { Effect, Actions, toPayload } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';
import * as fromRoot from 'reducers';

import * as wizardActions from 'actions/wizard';
import { InitializeWorkflowService } from 'services/initialize-workflow.service';


@Injectable()
export class WizardEffect {

    @Effect()
    saveEntity$: Observable<Action> = this.actions$
        .ofType(wizardActions.actionTypes.SAVE_ENTITY)
        .map(toPayload)
        // Retrieve part of the current state
        .withLatestFrom(this.store.select(state => state.wizard))
        .switchMap(([payload, wizard]) => {
            if (payload.oldName === payload.data.name) {
                return Observable.of(new wizardActions.SaveEntityCompleteAction(payload));
            } else {
                for (let i = 0; i < wizard.nodes.length; i++) {
                    if (payload.data.name === wizard.nodes[i].name) {
                        return Observable.of(new wizardActions.SaveEntityErrorAction(""));
                    }
                }
            }
            return Observable.of(new wizardActions.SaveEntityCompleteAction(payload));
        });



    @Effect()
    saveWorkflow$: Observable<Action> = this.actions$
        .ofType(wizardActions.actionTypes.SAVE_WORKFLOW)
        .map(toPayload)
        // Retrieve part of the current state
        .withLatestFrom(this.store.select(state => state.wizard))
        .switchMap(([payload, wizard]) => {
            const workflow = Object.assign({
                uiSettings: {
                    position: wizard.svgPosition
                },
                pipelineGraph: {
                    nodes: wizard.nodes,
                    edges: wizard.edges
                },
                settings: wizard.settings.advanced
            }, wizard.settings.basic);

            return (workflow.id ? this.workflowService.updateWorkflow(workflow) : this.workflowService.saveWorkflow(workflow))
                .map(() => {
                    return new wizardActions.SaveWorkflowCompleteAction(workflow.name);
                }).catch(function (error) {
                    return Observable.of(new wizardActions.SaveWorkflowErrorAction(''));
                });
        });


    @Effect()
    createNodeRelation$: Observable<Action> = this.actions$
        .ofType(wizardActions.actionTypes.CREATE_NODE_RELATION)
        .map(toPayload)
        .withLatestFrom(this.store.select(state => state.wizard))
        .switchMap(([payload, wizard]) => {
            const filtered = wizard.edges.filter((edge: any) => {
                return edge.origin === payload.origin && edge.destination === payload.destination;
            });
            if (filtered.length || (payload.origin === payload.destination)) {
                return Observable.of(new wizardActions.CreateNodeRelationErrorAction(''));
            } else {
                return Observable.of(new wizardActions.CreateNodeRelationCompleteAction(payload));
            }
        });

    @Effect()
    getEditedWorkflow$: Observable<Action> = this.actions$
        .ofType(wizardActions.actionTypes.MODIFY_WORKFLOW)
        .map((action: any) => action.payload)
        .switchMap((id: any) => {
            return this.workflowService.getWorkflowById(id)
                .map((workflow: any) => {
                    return new wizardActions.ModifyWorkflowCompleteAction(this.initializeWorkflowService.getInitializedWorkflow(workflow));
                }).catch(function (error: any) {
                    return Observable.of(new wizardActions.ModifyWorkflowErrorAction(''));
                });
        });

    constructor(
        private actions$: Actions,
        private store: Store<fromRoot.State>,
        private workflowService: WorkflowService,
        private initializeWorkflowService: InitializeWorkflowService
    ) { }

}
