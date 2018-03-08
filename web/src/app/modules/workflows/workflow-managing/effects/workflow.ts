/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Injectable } from '@angular/core';
import { Effect, Actions } from '@ngrx/effects';
import { Action, Store } from '@ngrx/store';
import { Observable } from 'rxjs/Observable';

import * as workflowActions from './../actions/workflow-list';
import * as errorActions from 'actions/errors';
import * as fromRoot from './../reducers';
import { WorkflowService } from 'services/workflow.service';
import { generateJsonFile } from '@utils';
import { DEFAULT_FOLDER, FOLDER_SEPARATOR } from './../workflow.constants';


@Injectable()
export class WorkflowEffect {

    @Effect()
    getWorkflowListAndStatus$: Observable<Action> = this.actions$
        .ofType(workflowActions.LIST_GROUP_WORKFLOWS)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .switchMap(([payload, workflow]: [any, any]) => {
            const currentLevel = workflow.workflowsManaging.currentLevel;
            const groupId = currentLevel.name === DEFAULT_FOLDER ? '940800b2-6d81-44a8-84d9-26913a2faea4' : currentLevel.id;
            return this.workflowService.getWorkflowsByGroup(groupId).map((result) => {
                return new workflowActions.ListGroupWorkflowsCompleteAction(result);
            }).catch((error: any) => {
                return Observable.from([new workflowActions.ListGroupWorkflowsFailAction(), new errorActions.ServerErrorAction(error)]);
            });
        });

    @Effect()
    getWorkflowGroups$: Observable<Action> = this.actions$
        .ofType(workflowActions.LIST_GROUPS).switchMap((response: any) => {
            return this.workflowService.getGroups().switchMap((groups) => {
                return [new workflowActions.ListGroupsCompleteAction(groups), new workflowActions.ListGroupWorkflowsAction()];
            }).catch((error: any) => {
                return Observable.from([
                    new workflowActions.ListGroupsErrorAction(),
                    new errorActions.ServerErrorAction(error)
                ]);
            });
        });

    @Effect()
    deleteWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.DELETE_WORKFLOW)
        .map((action: any) => action.payload)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .switchMap(([data, workflow]: [any, any]) => {

            const selectedGroups: Array<string> = workflow.workflowsManaging.selectedGroups;
            const selectedWorkflows: Array<string> = workflow.workflowsManaging.selectedWorkflows;
            const groups: any = workflow.workflowsManaging.groups;
            const workflows: any = workflow.workflowsManaging.workflowList;

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
        }).catch(function (error: any) {
            return Observable.from([
                new workflowActions.DeleteWorkflowErrorAction(),
                new errorActions.ServerErrorAction(error)
            ]);
        });

    @Effect()
    deleteVersions$: Observable<Action> = this.actions$
        .ofType(workflowActions.DELETE_VERSION)
        .map((action: any) => action.payload)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .switchMap(([data, workflow]: [any, any]) => {
            return this.workflowService.deleteWorkflowList(workflow.workflowsManaging.selectedVersions)
                .mergeMap(() => [new workflowActions.DeleteVersionCompleteAction(''), new workflowActions.ListGroupsAction()]);
        }).catch(function (error: any) {
            return Observable.from([
                new workflowActions.DeleteVersionErrorAction(),
                new errorActions.ServerErrorAction(error)
            ]);

        });

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
            })
                .mergeMap(() => {
                    return [new workflowActions.GenerateNewVersionCompleteAction(), new workflowActions.ListGroupWorkflowsAction()];
                });
        }).catch(function (error: any) {
            return Observable.from([
                new workflowActions.GenerateNewVersionErrorAction(),
                new errorActions.ServerErrorAction(error)
            ]);

        });

    @Effect()
    duplicateWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.DUPLICATE_WORKFLOW)
        .map((action: any) => action.payload)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .switchMap(([data, workflow]: [any, any]) => {
            const group = workflow.workflowsManaging.groups.find((group: any) => {
                return group.name === data.group;
            });
            return this.workflowService.generateVersion({
                id: data.id,
                group: group,
                version: 0
            })
            .mergeMap(() => {
                return [new workflowActions.DuplicateWorkflowCompleteAction(), new workflowActions.ListGroupWorkflowsAction()];
            });
        }).catch(function (error: any) {
            return Observable.from([
                new workflowActions.DuplicateWorkflowErrorAction(),
                new errorActions.ServerErrorAction(error)
            ]);

        });

    @Effect()
    downloadWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.DOWNLOAD_WORKFLOWS)
        .map((action: any) => action.payload)
        .switchMap((payload: any) => {
            const $downloadsSubscriptions = [];
            for (const workflow of payload) {
                $downloadsSubscriptions.push(this.workflowService.downloadWorkflow(workflow));
            }
            return Observable.forkJoin($downloadsSubscriptions);
        })
        .mergeMap((results: any[], index: number) => {
            results.forEach((data: any) => {
                generateJsonFile(data.name + '-v' + data.version, data);
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
                return Observable.of(new workflowActions.RunWorkflowErrorAction());
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
    saveJsonWorkflow$: Observable<Action> = this.actions$
        .ofType(workflowActions.SAVE_JSON_WORKFLOW)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .mergeMap(([data, workflow]: [any, any]) => {
            delete data.payload.id;
            data.payload.group = workflow.workflowsManaging.currentLevel;
            return this.workflowService.saveWorkflow(data.payload).mergeMap((response: any) => {
                return [new workflowActions.SaveJsonWorkflowActionComplete(), new workflowActions.ListGroupWorkflowsAction()];
            }).catch(function (error) {
                return Observable.from([
                    new workflowActions.SaveJsonWorkflowActionError(error),
                    new errorActions.ServerErrorAction(error)
                ]
                );
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

    @Effect()
    createGroup$: Observable<Action> = this.actions$
        .ofType<workflowActions.CreateGroupAction>(workflowActions.CREATE_GROUP)
        .mergeMap((data: any) =>
            this.store.select(fromRoot.getCurrentGroupLevel)
                .take(1)
                .mergeMap((groupLevel) => {
                    const groupName = groupLevel.group.name + FOLDER_SEPARATOR + data.payload;
                    return this.workflowService.createGroup(groupName).mergeMap(() => {
                        return [new workflowActions.CreateGroupCompleteAction(''), new workflowActions.ListGroupsAction()];
                    }).catch(function (error) {
                        return Observable.from([
                            new workflowActions.CreateGroupErrorAction(''),
                            new errorActions.ServerErrorAction(error)
                        ]);
                    });
                }));

    @Effect()
    changeGroupLevel$: Observable<Action> = this.actions$
        .ofType<workflowActions.CreateGroupAction>(workflowActions.CHANGE_GROUP_LEVEL)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .mergeMap(([data, workflow]: [any, any]) => {
            const group: any = typeof data.payload === 'string' ? workflow.workflowsManaging.groups.find((g: any) => {
                return g.name === data.payload;
            }) : data.payload;
            return Observable.from([
                new workflowActions.ChangeGroupLevelCompleteAction(group),
                new workflowActions.ListGroupWorkflowsAction()]);
        });

    @Effect()
    renameGroup$: Observable<Action> = this.actions$
        .ofType<workflowActions.RenameGroupAction>(workflowActions.RENAME_GROUP)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .mergeMap(([data, workflow]: [any, any]) => {
            const changedGroup = Object.assign({}, workflow.workflowsManaging.groups.find((group: any) => {
                return group.name === data.payload.oldName;
            }), {
                    name: data.payload.newName
                });

            return this.workflowService.updateGroup(changedGroup).mergeMap(() =>
                [new workflowActions.RenameGroupCompleteAction(), new workflowActions.ListGroupsAction()])
                .catch((error) => Observable.from([
                    new workflowActions.RenameGroupErrorAction(''), 
                    new errorActions.ServerErrorAction(error)
                ]));
        });

    @Effect()
    renameWorkflow$:Observable<Action> = this.actions$
        .ofType<workflowActions.RenameGroupAction>(workflowActions.RENAME_WORKFLOW)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .mergeMap(([data, workflow]: [any, any]) => {
            return this.workflowService.renameWorkflow({
                oldName: data.payload.oldName,
                newName: data.payload.newName,
                groupId: workflow.workflowsManaging.currentLevel.id
            }).mergeMap(() =>
                [new workflowActions.RenameWorkflowCompleteAction(), new workflowActions.ListGroupsAction()])
                .catch((error) => Observable.from([
                    new workflowActions.RenameWorkflowErrorAction(''),
                    new errorActions.ServerErrorAction(error)
                ]));
        });

    @Effect()
    moveWorkflow$:Observable<Action> = this.actions$
        .ofType<workflowActions.RenameGroupAction>(workflowActions.MOVE_WORKFLOW)
        .withLatestFrom(this.store.select(state => state.workflowsManaging))
        .mergeMap(([data, workflow]: [any, any]) => {
            return this.workflowService.moveWorkflow({
                workflowName: data.payload.workflowName,
                groupSourceId: data.payload.groupSourceId,
                groupTargetId: workflow.workflowsManaging.groups.find((group: any) => {
                    return group.name === data.payload.groupTarget;
                }).id
            }).mergeMap(() =>
                [new workflowActions.MoveWorkflowCompleteAction(), new workflowActions.ListGroupsAction()])
                .catch((error) => Observable.from([
                    new workflowActions.MoveWorkflowErrorAction(),
                    new errorActions.ServerErrorAction(error)
                ]));
        });
    constructor(
        private actions$: Actions,
        private store: Store<fromRoot.State>,
        private workflowService: WorkflowService
    ) { }
}
