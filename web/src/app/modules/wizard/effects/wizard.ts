/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ActivatedRoute, Router } from '@angular/router';
import { Injectable } from '@angular/core';
import { Action, Store } from '@ngrx/store';
import { Effect, Actions, ofType } from '@ngrx/effects';
import { Location } from '@angular/common';

import { iif, of, Observable, from } from 'rxjs';
import { switchMap, map, withLatestFrom } from 'rxjs/operators';

import * as fromWizard from './../reducers';
import * as errorActions from 'actions/errors';
import * as wizardActions from './../actions/wizard';
import { InitializeWorkflowService, TemplatesService } from 'services/initialize-workflow.service';
import { WorkflowService } from 'services/workflow.service';
import { WizardEdge } from '@app/wizard/models/node';
import { WizardService } from '@app/wizard/services/wizard.service';

@Injectable()
export class WizardEffect {

    @Effect()
    getTemplates$: Observable<Action> = this.actions$
        .pipe(ofType(wizardActions.GET_MENU_TEMPLATES))
        .pipe(switchMap((toPayload: any) => this._templatesService.getAllTemplates()
            .pipe(map((results: any) => {
                const templatesObj: any = {
                    input: [],
                    output: [],
                    transformation: []
                };
                results.forEach(template => templatesObj[template.templateType].push(template));
                return new wizardActions.GetMenuTemplatesCompleteAction(templatesObj);
            })).catch(error =>
                iif(() => error.statusText === 'Unknown Error',
                    of(new wizardActions.GetMenuTemplatesErrorAction()),
                    of(new errorActions.ServerErrorAction(error))))));


    @Effect()
    saveEntity$: Observable<Action> = this.actions$
        .pipe(ofType(wizardActions.SAVE_ENTITY))
        .pipe(withLatestFrom(this._store.select(state => state.wizard.wizard)))
        .pipe(map(([payload, wizard]: [any, any]) => {
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
        }));


    @Effect()
    saveWorkflow$: Observable<any> = this.actions$
        .pipe(ofType(wizardActions.SAVE_WORKFLOW))
        // Retrieve part of the current state
        .pipe(withLatestFrom(this._store.select(state => state)))
        .pipe(switchMap(([redirectOnSave, state]: [any, any]) => {
            const wizard = state.wizard.wizard;
            if (!wizard.nodes.length) {
                return of(new wizardActions.SaveWorkflowErrorAction({
                    title: 'NO_ENTITY_WORKFLOW_TITLE',
                    description: 'NO_ENTITY_WORKFLOW_MESSAGE'
                }));
            }
            const workflow = this._wizardService.getWorkflowModel(state);

            if (wizard.editionMode) {
                return this._workflowService.updateWorkflow(workflow).mergeMap((res) => {
                    redirectOnSave && this.redirectOnSave();
                    return [
                        new wizardActions.SaveWorkflowCompleteAction(workflow.id),
                        new wizardActions.ShowNotificationAction({
                            type: 'success',
                            message: 'WORKFLOW_SAVE_SUCCESS'
                        })
                    ];
                }).catch(error => from([
                    new errorActions.ServerErrorAction(error),
                    new wizardActions.SaveWorkflowErrorAction('')
                ]));
            } else {
                return this._workflowService.saveWorkflow(workflow).mergeMap((res) => {
                    redirectOnSave && this.redirectOnSave();
                    return [
                        new wizardActions.SaveWorkflowCompleteAction(res.id),
                        new wizardActions.ShowNotificationAction({
                            type: 'success',
                            message: 'WORKFLOW_SAVE_SUCCESS'
                        })
                    ];
                }).catch(error => from([
                    new errorActions.ServerErrorAction(error),
                    new wizardActions.SaveWorkflowErrorAction('')
                ]));
            }
        }));

    @Effect()
    createEdge$: Observable<Action> = this.actions$
        .pipe(ofType(wizardActions.CREATE_NODE_RELATION))
        .pipe(withLatestFrom(this._store.select(state => state.wizard.wizard)))
        .pipe(map(([payload, wizard]: [any, any]) => {
            let relationExist = false;
            // get number of connected entities in destionation and check if relation exists
            wizard.edges.forEach((edge: WizardEdge) => {
                if ((edge.origin === payload.origin && edge.destination === payload.destination) ||
                    (edge.origin === payload.destination && edge.destination === payload.origin)) {
                    relationExist = true;
                }
            });
            // throw error if relation exist or destination is the same than the origin
            if (relationExist || (payload.origin === payload.destination)) {
                return new wizardActions.CreateNodeRelationErrorAction('');
            } else {
                payload.dataType = 'ValidData';
                return new wizardActions.CreateNodeRelationCompleteAction(payload);
            }
        }));

    @Effect()
    getEditedWorkflow$: Observable<Action> = this.actions$
        .pipe(ofType(wizardActions.MODIFY_WORKFLOW))
        .pipe(map((action: any) => action.payload))
        .pipe(switchMap((id: any) => this._workflowService.getWorkflowById(id)
            .pipe(switchMap(workflow => [
                new wizardActions.SetWorkflowTypeAction(workflow.executionEngine),
                new wizardActions.GetMenuTemplatesAction(),
                new wizardActions.ModifyWorkflowCompleteAction(this._initializeWorkflowService.getInitializedWorkflow(workflow))
            ])).catch(error => {
                return of(new wizardActions.ModifyWorkflowErrorAction(''));
            })));

    @Effect()
    validateWorkflow$: Observable<Action> = this.actions$
        .pipe(ofType(wizardActions.VALIDATE_WORKFLOW))
        .pipe(withLatestFrom(this._store.select(state => state)))
        .pipe(switchMap(([payload, state]: [any, any]) => {
            const workflow = this._wizardService.getWorkflowModel(state);
            return this._workflowService.validateWorkflow(workflow)
                .pipe(map((response: any) =>
                    new wizardActions.ValidateWorkflowCompleteAction(response)))
                .catch(error => of(new wizardActions.ValidateWorkflowErrorAction()));
        })).catch(error => of(new wizardActions.ValidateWorkflowErrorAction()));

    redirectOnSave() {
        window.history.length > 2 ? this._location.back() : this._route.navigate(['repository']);
    }

    constructor(
        private actions$: Actions,
        private _store: Store<fromWizard.State>,
        private _workflowService: WorkflowService,
        private _wizardService: WizardService,
        private _templatesService: TemplatesService,
        private _initializeWorkflowService: InitializeWorkflowService,
        private _route: Router,
        private _currentActivatedRoute: ActivatedRoute,
        private _location: Location
    ) { }
}

