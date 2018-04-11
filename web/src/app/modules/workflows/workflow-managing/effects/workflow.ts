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
import { Observable } from 'rxjs/Observable';

import * as workflowActions from './../actions/workflow-list';
import * as errorActions from 'actions/errors';
import * as fromRoot from './../reducers';
import { WorkflowService } from 'services/workflow.service';
import { generateJsonFile } from '@utils';
import { DEFAULT_FOLDER, FOLDER_SEPARATOR } from './../workflow.constants';
import { Group, GroupWorkflow } from '../models/workflows';

@Injectable()
export class WorkflowEffect {

    @Effect()
    getWorkflowListAndStatus$: Observable<Action> = this.actions$
        .ofType(workflowActions.LIST_GROUP_WORKFLOWS)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .switchMap(([payload, workflow]) => {
            const currentLevel = workflow.workflowsManaging.currentLevel;
            const groupId = currentLevel.name === DEFAULT_FOLDER ? '940800b2-6d81-44a8-84d9-26913a2faea4' : currentLevel.id;
            return this.workflowService.getWorkflowsByGroup(groupId)
                .map((result) => new workflowActions.ListGroupWorkflowsCompleteAction(result))
                .catch(error => Observable.from([
                    new workflowActions.ListGroupWorkflowsFailAction(),
                    new errorActions.ServerErrorAction(error)
                ]));
            });



    @Effect()
    getWorkflowGroups$: Observable<Action> = this.actions$
        .ofType(workflowActions.LIST_GROUPS)
        .switchMap((data: any) =>
            this.workflowService.getGroups()
            .switchMap(groups => [new workflowActions.ListGroupsCompleteAction(groups), new workflowActions.ListGroupWorkflowsAction()])
            .catch(error => Observable.from([new workflowActions.ListGroupsErrorAction(), new errorActions.ServerErrorAction(error)])));

    @Effect()
    deleteWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.DELETE_WORKFLOW)
        .map((action: any) => action.payload)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .switchMap(([data, workflow]) => {
            const selectedGroups: Array<string> = workflow.workflowsManaging.selectedGroups;
            const selectedWorkflows: Array<string> = workflow.workflowsManaging.selectedWorkflows;
            const groups: Group[] = workflow.workflowsManaging.groups;
            const workflows: GroupWorkflow[] = workflow.workflowsManaging.workflowList;
            const observables: any = [];
            if (selectedGroups.length) {
                groups.forEach((group: any) => {
                    if (selectedGroups.indexOf(group.name) > -1) {
                        observables.push(this.workflowService.deleteGroupById(group.id));
                    }
                });
            }
            if (selectedWorkflows.length) {
                const list: Array<string> = [];
                workflows.forEach((workflow: any) => {
                    if (selectedWorkflows.indexOf(workflow.name) > -1) {
                        list.push(workflow.id);
                    }
                });
                observables.push(this.workflowService.deleteWorkflowList(list));
            }
            return Observable.forkJoin(observables).mergeMap((results: any) => {
                const actions: Array<Action> = [new workflowActions.ListGroupsAction()];
                if (selectedGroups.length) {
                    actions.push(new workflowActions.DeleteGroupCompleteAction());
                }
                if (selectedWorkflows.length) {
                    actions.push(new workflowActions.DeleteWorkflowCompleteAction(''));
                }
                return actions;
            });
        }).catch(error => Observable.from([new workflowActions.DeleteWorkflowErrorAction(), new errorActions.ServerErrorAction(error)]));

    @Effect()
    deleteVersions$: Observable<Action> = this.actions$
       .ofType(workflowActions.DELETE_VERSION)
       .map((action: any) => action.payload)
       .withLatestFrom(this.store.select(state => state.workflowsManaging))
       .switchMap(([data, workflow]) => this.workflowService.deleteWorkflowList(workflow.workflowsManaging.selectedVersions))
       .mergeMap(() => [new workflowActions.DeleteVersionCompleteAction(''), new workflowActions.ListGroupsAction()])
       .catch(error => Observable.from([new workflowActions.DeleteVersionErrorAction(), new errorActions.ServerErrorAction(error)]));

    @Effect()
    generateVersion$: Observable<Action> = this.actions$
       .ofType(workflowActions.GENERATE_NEW_VERSION)
       .map((action: any) => action.payload)
       .withLatestFrom(this.store.select(state => state.workflowsManaging))
       .switchMap(([data, workflow]: [any, any]) => {
            const id = workflow.workflowsManaging.selectedVersions[0];
            const version = workflow.workflowsManaging.workflowList.find((w: any) => w.id === id);
            return this.workflowService.generateVersion({
                id: version.id,
                tag: version.tag,
                group: version.group
            })})
        .mergeMap(() => [new workflowActions.GenerateNewVersionCompleteAction(), new workflowActions.ListGroupWorkflowsAction()])
        .catch(error => Observable.from([new workflowActions.GenerateNewVersionErrorAction(), new errorActions.ServerErrorAction(error)]));

    @Effect()
    duplicateWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.DUPLICATE_WORKFLOW)
        .map((action: any) => action.payload)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .switchMap(([data, workflow]) => this.workflowService.generateVersion({
            id: data.id,
            group: workflow.workflowsManaging.groups.find((group: any) => group.name === data.group),
            version: 0
        }))
        .mergeMap(() => [new workflowActions.DuplicateWorkflowCompleteAction(), new workflowActions.ListGroupWorkflowsAction()])
        .catch(error => Observable.from([new workflowActions.DuplicateWorkflowErrorAction(), new errorActions.ServerErrorAction(error)]));

    @Effect()
    downloadWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.DOWNLOAD_WORKFLOWS)
        .map((action: any) => action.payload)
        .switchMap((workflows: Array<any>) => Observable.forkJoin(workflows.map(workflow => this.workflowService.downloadWorkflow(workflow))))
        .mergeMap((results: any[]) => {
            results.forEach((data: any) => {
                generateJsonFile(data.name + '-v' + data.version, data);
            });
            return Observable.of(new workflowActions.DownloadWorkflowsCompleteAction(''));
        }).catch(error => Observable.of(new errorActions.ServerErrorAction(error)));

    @Effect()
    runWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.RUN_WORKFLOW)
        .switchMap((data: any) => this.workflowService.runWorkflow(data.payload.id)
        .map((response: any) => new workflowActions.RunWorkflowCompleteAction(data.payload.name))
        .catch(error => Observable.of(new workflowActions.RunWorkflowErrorAction())));

    @Effect()
    stopWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.STOP_WORKFLOW)
        .switchMap((data: any) => this.workflowService.stopWorkflow(data.payload)
            .map((response: any) =>  new workflowActions.StopWorkflowCompleteAction(data.payload)))
        .catch(error => Observable.from([new workflowActions.StopWorkflowErrorAction(), new errorActions.ServerErrorAction(error)]));


    @Effect()
    saveJsonWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.SAVE_JSON_WORKFLOW)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .mergeMap(([data, workflow]: [any, any]) => {
            delete data.payload.id;
            data.payload.group = workflow.workflowsManaging.currentLevel;
            return this.workflowService.saveWorkflow(data.payload)
                .mergeMap((response: any) => [
                    new workflowActions.SaveJsonWorkflowActionComplete(),
                    new workflowActions.ListGroupWorkflowsAction()
                ])
                .catch(error => Observable.from([
                    new workflowActions.SaveJsonWorkflowActionError(error),
                    new errorActions.ServerErrorAction(error)
                ]));
        });

    @Effect()
    getExecutionInfo$: Observable<Action> = this.actions$
        .ofType(workflowActions.GET_WORKFLOW_EXECUTION_INFO)
        .switchMap((data: any) => this.workflowService.getWorkflowExecutionInfo(data.payload.id)
        .map((response: any) => new workflowActions.GetExecutionInfoCompleteAction(Object.assign(response, {name: data.payload.name}))))
        .catch(error => Observable.from([new workflowActions.GetExecutionInfoErrorAction(), new errorActions.ServerErrorAction(error)]));

    @Effect()
    createGroup$: Observable<Action> = this.actions$
        .ofType<workflowActions.CreateGroupAction>(workflowActions.CREATE_GROUP)
        .mergeMap((data: any) => this.store.select(fromRoot.getCurrentGroupLevel)
        .take(1)
        .mergeMap((groupLevel) => this.workflowService.createGroup(groupLevel.group.name + FOLDER_SEPARATOR + data.payload)
        .mergeMap(() => [new workflowActions.CreateGroupCompleteAction(''), new workflowActions.ListGroupsAction()]))
        .catch(error => Observable.from([new workflowActions.CreateGroupErrorAction(''), new errorActions.ServerErrorAction(error)])));

    @Effect()
    changeGroupLevel$: Observable<Action> = this.actions$
        .ofType<workflowActions.CreateGroupAction>(workflowActions.CHANGE_GROUP_LEVEL)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .mergeMap(([data, workflow]) =>  Observable.from([
            new workflowActions.ChangeGroupLevelCompleteAction(
                typeof data.payload === 'string' ? workflow.workflowsManaging.groups
                    .find((g: any) => g.name === data.payload) : data.payload),
                    new workflowActions.ListGroupWorkflowsAction()]));

    @Effect()
    renameGroup$: Observable<Action> = this.actions$
        .ofType<workflowActions.RenameGroupAction>(workflowActions.RENAME_GROUP)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .mergeMap(([data, workflow]) => this.workflowService.updateGroup({
            ...workflow.workflowsManaging.groups.find((group: any) => group.name === data.payload.oldName),
            name: data.payload.name
        }).mergeMap(() => [new workflowActions.RenameGroupCompleteAction(), new workflowActions.ListGroupsAction()])
        .catch((error) => Observable.from([new workflowActions.RenameGroupErrorAction(''), new errorActions.ServerErrorAction(error)])));

    @Effect()
    renameWorkflow$: Observable<Action> = this.actions$
        .ofType<workflowActions.RenameGroupAction>(workflowActions.RENAME_WORKFLOW)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .mergeMap(([data, workflow]) =>  this.workflowService.renameWorkflow({
            oldName: data.payload.oldName,
            newName: data.payload.newName,
            groupId: workflow.workflowsManaging.currentLevel.id
        }).mergeMap(() => [new workflowActions.RenameWorkflowCompleteAction(), new workflowActions.ListGroupsAction()])
        .catch(error => Observable.from([new workflowActions.RenameWorkflowErrorAction(''), new errorActions.ServerErrorAction(error)])));


    @Effect()
    moveWorkflow$: Observable<Action> = this.actions$
        .ofType<workflowActions.RenameGroupAction>(workflowActions.MOVE_WORKFLOW)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .mergeMap(([data, workflow]) => this.workflowService.moveWorkflow({
            workflowName: data.payload.workflowName,
            groupSourceId: data.payload.groupSourceId,
            groupTargetId: workflow.workflowsManaging.groups.find((group: any) => group.name === data.payload.groupTarget).id
        }).mergeMap(() => [new workflowActions.MoveWorkflowCompleteAction(), new workflowActions.ListGroupsAction()]))
        .catch(error => Observable.from([new workflowActions.MoveWorkflowErrorAction(), new errorActions.ServerErrorAction(error)]));

    constructor(
        private actions$: Actions,
        private store: Store<fromRoot.State>,
        private workflowService: WorkflowService
    ) { }
}
