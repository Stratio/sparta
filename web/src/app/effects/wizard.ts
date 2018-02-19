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
import { Effect, Actions, toPayload } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import * as fromRoot from 'reducers';
import * as errorActions from 'actions/errors';
import * as wizardActions from 'actions/wizard';
import { InitializeWorkflowService, TemplatesService } from 'services/initialize-workflow.service';
import { homeGroup } from '@app/shared/constants/global';


@Injectable()
export class WizardEffect {

    @Effect()
    getTemplates$: Observable<Action> = this.actions$
        .ofType(wizardActions.GET_MENU_TEMPLATES)
        .switchMap((toPayload: any) => {
            return this.templatesService.getAllTemplates().map((results: any) => {
                const templatesObj: any = {
                    input: [],
                    output: [],
                    transformation: []
                };
                results.map((template: any) => {
                    templatesObj[template.templateType].push(template);
                });
                return new wizardActions.GetMenuTemplatesCompleteAction(templatesObj);
            }).catch((error) => {
                return  Observable.if(() => error.statusText === 'Unknown Error',
                Observable.of(new wizardActions.GetMenuTemplatesErrorAction()),
                Observable.of(new errorActions.ServerErrorAction(error)));
            });
        });


    @Effect()
    saveEntity$: Observable<Action> = this.actions$
        .ofType(wizardActions.SAVE_ENTITY)
        .map(toPayload)
        // Retrieve part of the current state
        .withLatestFrom(this.store.select(state => state.wizard))
        .map(([payload, wizard]: [any, any]) => {

            if (payload.oldName === payload.data.name) {
                return new wizardActions.SaveEntityCompleteAction(payload);
            } else {
                for (let i = 0; i < wizard.nodes.length; i++) {
                    if (payload.data.name === wizard.nodes[i].name) {
                        return new wizardActions.SaveEntityErrorAction(true);
                    }
                }
            }
            return new wizardActions.SaveEntityCompleteAction(payload);
        });



    @Effect()
    saveWorkflow$: Observable<any> = this.actions$
        .ofType(wizardActions.SAVE_WORKFLOW)
        .map(toPayload)
        // Retrieve part of the current state
        .withLatestFrom(this.store.select(state => state))
        .switchMap(([payload, state]: [any, any]) => {
            const wizard = state.wizard;
            if (!wizard.nodes.length) {
                return Observable.of(new wizardActions.SaveWorkflowErrorAction({
                    title: 'NO_ENTITY_WORKFLOW_TITLE',
                    description: 'NO_ENTITY_WORKFLOW_MESSAGE'
                }));
            }
            for (let i = 0; i < wizard.nodes.length; i++) {
                if (wizard.nodes[i].hasErrors) { //At least one entity has errors
                    return Observable.of(new wizardActions.SaveWorkflowErrorAction({
                        title: 'VALIDATION_ERRORS_TITLE',
                        description: 'VALIDATION_ERRORS_MESSAGE'
                    }));
                }
            };

            const workflow = Object.assign({
                id: wizard.workflowId,
                version: wizard.workflowVersion,
                executionEngine: wizard.workflowType,
                uiSettings: {
                    position: wizard.svgPosition
                },
                pipelineGraph: {
                    nodes: wizard.nodes,
                    edges: wizard.edges
                },
                settings: wizard.settings.advancedSettings
            }, wizard.settings.basic);

            if (wizard.workflowId && wizard.workflowId.length) {
                workflow.group = wizard.workflowGroup;
                return this.workflowService.updateWorkflow(workflow).map(() => {
                    return new wizardActions.SaveWorkflowCompleteAction(workflow.name);
                }).catch(function (error) {
                    return Observable.of(new errorActions.ServerErrorAction(error));
                });
            } else {
                delete workflow.id;
                workflow.group = state.workflowsManaging ? state.workflowsManaging.workflowsManaging.currentLevel : homeGroup;
                return this.workflowService.saveWorkflow(workflow).map(() => {
                    return new wizardActions.SaveWorkflowCompleteAction(workflow.name);
                }).catch(function (error) {
                    return Observable.of(new errorActions.ServerErrorAction(error));
                });
            }

        });


    @Effect()
    createNodeRelation$: Observable<Action> = this.actions$
        .ofType(wizardActions.CREATE_NODE_RELATION)
        .map(toPayload)
        .withLatestFrom(this.store.select(state => state.wizard))
        .map(([payload, wizard]: [any, any]) => {
            let relationExist = false;
            // get number of connected entities in destionation and check if relation exists
            wizard.edges.forEach((edge: any) => {
                if ((edge.origin === payload.origin && edge.destination === payload.destination) ||
                    (edge.origin === payload.destination && edge.destination === payload.origin)) {
                    relationExist = true;
                }
            });
            // throw error if relation exist or destination is the same than the origin
            if (relationExist || (payload.origin === payload.destination)) {
                return new wizardActions.CreateNodeRelationErrorAction('');
            } else {
                return new wizardActions.CreateNodeRelationCompleteAction(payload);
            }
        });

    @Effect()
    getEditedWorkflow$: Observable<Action> = this.actions$
        .ofType(wizardActions.MODIFY_WORKFLOW)
        .map((action: any) => action.payload)
        .switchMap((id: any) => {
            return this.workflowService.getWorkflowById(id)
                .switchMap((workflow: any) => {
                    return [
                        new wizardActions.SetWorkflowTypeAction(workflow.executionEngine),
                        new wizardActions.GetMenuTemplatesAction(),
                        new wizardActions.ModifyWorkflowCompleteAction(this.initializeWorkflowService.getInitializedWorkflow(workflow))
                    ];
                }).catch(function (error: any) {
                    return Observable.of(new wizardActions.ModifyWorkflowErrorAction(''));
                });
        });

    @Effect()
    validateWorkflow$: Observable<Action> = this.actions$
        .ofType(wizardActions.VALIDATE_WORKFLOW)
        .map(toPayload)
        .withLatestFrom(this.store.select(state => state.wizard))
        .switchMap(([payload, wizard]: [any, any]) => {
            const workflow = Object.assign({
                id: wizard.workflowId,
                uiSettings: {
                    position: wizard.svgPosition
                },
                pipelineGraph: {
                    nodes: wizard.nodes,
                    edges: wizard.edges
                },
                settings: wizard.settings.advancedSettings
            }, wizard.settings.basic);
            return this.workflowService.validateWorkflow(workflow).map((response: any) => {
                return new wizardActions.ValidateWorkflowCompleteAction(response);
            }).catch((error: any) => {
                return Observable.of(new wizardActions.ValidateWorkflowErrorAction());
            });
        }).catch((error: any) => {
            return Observable.of(new wizardActions.ValidateWorkflowErrorAction());
        });


    constructor(
        private actions$: Actions,
        private store: Store<fromRoot.State>,
        private workflowService: WorkflowService,
        private templatesService: TemplatesService,
        private initializeWorkflowService: InitializeWorkflowService
    ) { }


}

