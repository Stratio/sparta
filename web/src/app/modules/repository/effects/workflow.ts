/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { Effect, Actions, ofType } from '@ngrx/effects';
import { Action, Store, select } from '@ngrx/store';

import { Observable, from, forkJoin, of, iif } from 'rxjs';
import { withLatestFrom, switchMap, map, mergeMap, catchError } from 'rxjs/operators';

import * as workflowActions from './../actions/workflow-list';
import * as errorActions from 'actions/errors';
import * as fromRoot from './../reducers';
import { WorkflowService, ScheduledService } from 'services/workflow.service';
import { generateJsonFile } from '@utils';
import { FOLDER_SEPARATOR } from './../workflow.constants';
import { Group, GroupWorkflow } from '../models/workflows';
import { homeGroup } from '@app/shared/constants/global';
import { Router } from '@angular/router';

@Injectable()
export class WorkflowEffect {

  @Effect()
  getWorkflowListAndStatus$: Observable<Action> = this.actions$
    .pipe(ofType(workflowActions.LIST_GROUP_WORKFLOWS))
    .pipe(withLatestFrom(this.store.pipe(select(fromRoot.getCurrentLevel))))
    .pipe(switchMap(([payload, currentLevel]) => {
      const groupId = currentLevel.name === homeGroup.name ? homeGroup.id : currentLevel.id;
      return this.workflowService.getWorkflowsByGroup(groupId)
        .pipe(map((result) => new workflowActions.ListGroupWorkflowsCompleteAction(result)))
        .pipe(catchError(error => from([
          new workflowActions.ListGroupWorkflowsFailAction(),
          new errorActions.ServerErrorAction(error)
        ])));
    }));

  @Effect()
  getWorkflowGroups$: Observable<Action> = this.actions$
    .pipe(ofType(workflowActions.LIST_GROUPS))
    .pipe(switchMap((data: any) =>
      this.workflowService.getGroups()
        .pipe(switchMap((groups: any) => [
          new workflowActions.ListGroupsCompleteAction(groups.find(group => group.name === '/home') ?
            groups : [...groups, homeGroup]), new workflowActions.ListGroupWorkflowsAction()]))
        .pipe(catchError(error => from([
          new workflowActions.ListGroupsErrorAction(),
          new errorActions.ServerErrorAction(error)])))
    ));

  @Effect()
  deleteWorkflow$: Observable<Action> = this.actions$
    .pipe(ofType(workflowActions.DELETE_WORKFLOW))
    .pipe(map((action: any) => action.payload))
    .pipe(withLatestFrom(this.store.pipe(select((state: any) => state.workflowsManaging))))
    .pipe(switchMap(([data, workflow]) => {
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
        workflows.forEach((workflowf: any) => {
          if (selectedWorkflows.indexOf(workflowf.name) > -1) {
            list.push(workflowf.id);
          }
        });
        observables.push(this.workflowService.deleteWorkflowList(list));
      }
      return forkJoin(observables).pipe(mergeMap((results: any) => {
        const actions: Array<Action> = [];
        if (selectedGroups.length) {
          actions.push(new workflowActions.DeleteGroupCompleteAction());
          actions.push(new workflowActions.DeleteGroup(selectedGroups));
        }
        if (selectedWorkflows.length) {
          actions.push(new workflowActions.DeleteWorkflowCompleteAction(''));
          actions.push(new workflowActions.DeleteWorkflowGroup(selectedWorkflows));
        }
        return actions;
      }));
    })).pipe(catchError(error => from([
      new workflowActions.DeleteWorkflowErrorAction(),
      new errorActions.ServerErrorAction(error)
    ])));


  @Effect()
  deleteSingleFolder$: Observable<Action> = this.actions$
    .pipe(ofType<workflowActions.DeleteSingleGroupAction>(workflowActions.DELETE_SINGLE_GROUP))
    .pipe(map((action: any) => action.groupId))
    .pipe(switchMap((id: string) => this.workflowService.deleteGroupById(id)
      .pipe(map(() => new workflowActions.DeleteSingleGroupCompleteAction(id)))
      .pipe(catchError((error) => of(new workflowActions.DeleteSingleGroupErrorAction())))));

  @Effect()
  deleteSingleWorkflow$: Observable<Action> = this.actions$
    .pipe(ofType<workflowActions.DeleteSingleWorkflowAction>(workflowActions.DELETE_SINGLE_WORKFLOW))
    .pipe(map((action: any) => action.workflowName))
    .pipe(withLatestFrom(this.store.pipe(select(fromRoot.getWorkflowList))))
    .pipe(switchMap(([workflowName, workflowList]: [string, Array<GroupWorkflow>]) => {
      const list = workflowList.filter(workflow => workflow.name === workflowName).map(workflow => workflow.id);
      return this.workflowService.deleteWorkflowList(list)
        .pipe(map(() => new workflowActions.DeleteWorkflowGroup([workflowName])))
        .pipe(catchError((error) => of(new workflowActions.DeleteSingleWorkflowErrorAction())));
    }));

  @Effect()
  deleteVersions$: Observable<Action> = this.actions$
    .pipe(ofType<workflowActions.DeleteVersionAction>(workflowActions.DELETE_VERSION))
    .pipe(withLatestFrom(this.store.pipe(select(fromRoot.getSelectedVersions))))
    .pipe(switchMap(([data, selectedVersions]) =>
      this.workflowService.deleteWorkflowList(selectedVersions)
        .pipe(mergeMap(() => [
          new workflowActions.DeleteVersionCompleteAction(''),
          new workflowActions.ListGroupsAction()]))
        .pipe(catchError(error => from([new workflowActions.DeleteVersionErrorAction(), new errorActions.ServerErrorAction(error)])))
    ));

  @Effect()
  deleteSingleVersion$: Observable<Action> = this.actions$
    .pipe(ofType<workflowActions.DeleteSingleVersionAction>(workflowActions.DELETE_SINGLE_VERSION))
    .pipe(map((action: any) => action.versionId))
    .pipe(switchMap((versionId: any) => this.workflowService.deleteWorkflowList([versionId])
      .pipe(mergeMap(() => [new workflowActions.DeleteSingleVersionCompleteAction(), new workflowActions.ListGroupsAction()]))
      .pipe(catchError(error => from([
        new workflowActions.DeleteSingleVersionErrorAction(),
        new errorActions.ServerErrorAction(error)
      ])))));

  @Effect()
  generateVersion$: Observable<Action> = this.actions$
    .pipe(ofType(workflowActions.GENERATE_NEW_VERSION))
    .pipe(map((action: any) => action.versionId))
    .pipe(withLatestFrom(this.store.pipe(select((state: any) => state.workflowsManaging))))
    .pipe(switchMap(([id, workflow]: [string, any]) => {
      const version = workflow.workflowsManaging.workflowList.find((w: any) => w.id === id);
      return this.workflowService.generateVersion({
        id: version.id,
        tag: version.tag,
        group: version.group
      }).pipe(mergeMap(response => [
        new workflowActions.GenerateNewVersionCompleteAction(response)
      ])).pipe(catchError(error => from([
        new workflowActions.GenerateNewVersionErrorAction(), new errorActions.ServerErrorAction(error)
      ])));
    }));

  @Effect()
  duplicateWorkflow$: Observable<Action> = this.actions$
    .pipe(ofType(workflowActions.DUPLICATE_WORKFLOW))
    .pipe(map((action: any) => action.payload))
    .pipe(withLatestFrom(this.store.pipe(select((state: any) => state.workflowsManaging))))
    .pipe(switchMap(([data, workflow]) => this.workflowService.generateVersion({
      id: data.id,
      name: data.name || null,
      group: workflow.workflowsManaging.groups.find((group: any) => group.name === data.group),
      version: 0
    }).pipe(mergeMap(() => [
      new workflowActions.DuplicateWorkflowCompleteAction(),
      new workflowActions.ListGroupWorkflowsAction()
    ])).pipe(catchError(error => from([
      new workflowActions.DuplicateWorkflowErrorAction(),
      new errorActions.ServerErrorAction(error)])))));

  @Effect()
  downloadWorkflow$: Observable<Action> = this.actions$
    .pipe(ofType(workflowActions.DOWNLOAD_WORKFLOWS))
    .pipe(map((action: any) => action.payload))
    .pipe(switchMap((workflows: Array<any>) =>
      forkJoin(workflows.map(workflow => this.workflowService.downloadWorkflow(workflow)))
        .pipe(mergeMap((results: any[]) => {
          results.forEach((data: any) => {
            generateJsonFile(data.name + '-v' + data.version, data);
          });
          return of(new workflowActions.DownloadWorkflowsCompleteAction(''));
        })).pipe(catchError(error => of(new errorActions.ServerErrorAction(error))))));

  @Effect()
  runWorkflow$: Observable<workflowActions.Actions> = this.actions$
    .pipe(ofType<workflowActions.RunWorkflowAction>(workflowActions.RUN_WORKFLOW))
    .pipe(switchMap((data: any) => this.workflowService.validateWithExecutionContext({
      workflowId: data.payload.workflowId,
      executionContext: data.payload.executionContext
    }).pipe(mergeMap((result: any) =>
      iif(() => result && result.valid,
        this.workflowService.runWorkflowWithParams({
          workflowId: data.payload.workflowId,
          executionContext: data.payload.executionContext
        }).pipe(map(() => {
          this.router.navigate(['executions']);
          return new workflowActions.RunWorkflowCompleteAction(data.payload.workflowName);
        })).pipe(catchError(error => of(new workflowActions.RunWorkflowErrorAction(error)))),
        of(new workflowActions.RunWorkflowValidationErrorAction(result.messages)))))
      .pipe(catchError(error => of(new workflowActions.RunWorkflowErrorAction(error))))
    ));


  @Effect()
  saveJsonWorkflow$: Observable<Action> = this.actions$
    .pipe(ofType(workflowActions.SAVE_JSON_WORKFLOW))
    .pipe(withLatestFrom(this.store.pipe(select((state: any) => state.workflowsManaging))))
    .pipe(mergeMap(([data, workflow]: [any, any]) => {
      delete data.payload.id;
      data.payload.group = workflow.workflowsManaging.currentLevel;
      return this.workflowService.saveWorkflow(data.payload)
        .pipe(mergeMap((response: any) => [
          new workflowActions.SaveJsonWorkflowActionComplete(),
          new workflowActions.SaveWorkflowGroup(response)
        ]))
        .pipe(catchError(error => from([
          new workflowActions.SaveJsonWorkflowActionError(error),
          new errorActions.ServerErrorAction(error)
        ])));
    }));

  @Effect()
  getExecutionInfo$: Observable<Action> = this.actions$
    .pipe(ofType(workflowActions.GET_WORKFLOW_EXECUTION_INFO))
    .pipe(switchMap((data: any) => this.workflowService.getWorkflowExecutionInfo(data.payload.id)
      .pipe(map((response: any) =>
        new workflowActions.GetExecutionInfoCompleteAction(Object.assign(response, { name: data.payload.name })
        ))
      ))).pipe(catchError(error => from([
        new workflowActions.GetExecutionInfoErrorAction(),
        new errorActions.ServerErrorAction(error)
      ])));

  @Effect()
  createGroup$: Observable<Action> = this.actions$
    .pipe(ofType<workflowActions.CreateGroupAction>(workflowActions.CREATE_GROUP))
    .pipe(map((action: any) => action.payload))
    .pipe(withLatestFrom(this.store.pipe(select(fromRoot.getCurrentGroupLevel))))
    .pipe(switchMap(([data, groupLevel]) => this.workflowService.createGroup(groupLevel.group.name + FOLDER_SEPARATOR + data)
      .pipe(switchMap(group => [
        new workflowActions.CreateGroupCompleteAction(''),
        new workflowActions.AddGroupAction(group)
      ])).pipe(catchError(error => from([
        new workflowActions.CreateGroupErrorAction(''),
        new errorActions.ServerErrorAction(error)
      ])))
    ));

  @Effect()
  changeGroupLevel$: Observable<Action> = this.actions$
    .pipe(ofType<workflowActions.CreateGroupAction>(workflowActions.CHANGE_GROUP_LEVEL))
    .pipe(withLatestFrom(this.store.pipe(select((state: any) => state.workflowsManaging))))
    .pipe(mergeMap(([data, workflow]) => from([
      new workflowActions.ChangeGroupLevelCompleteAction(
        typeof data.payload === 'string' ? workflow.workflowsManaging.groups
          .find((g: any) => g.name === data.payload) : data.payload),
      new workflowActions.ListGroupWorkflowsAction()])));

  @Effect()
  renameGroup$: Observable<Action> = this.actions$
    .pipe(ofType<workflowActions.RenameGroupAction>(workflowActions.RENAME_GROUP))
    .pipe(withLatestFrom(this.store.pipe(select((state: any) => state.workflowsManaging))))
    .pipe(mergeMap(([data, workflow]) => this.workflowService.updateGroup({
      ...workflow.workflowsManaging.groups.find((group: any) => group.name === data.payload.oldName),
      name: data.payload.newName
    }).pipe(mergeMap(() => [
      new workflowActions.RenameGroupCompleteAction(),
      new workflowActions.ListGroupsAction()
    ])).pipe(catchError((error) => from([
      new workflowActions.RenameGroupErrorAction(''),
      new errorActions.ServerErrorAction(error)
    ])))));

  @Effect()
  renameWorkflow$: Observable<Action> = this.actions$
    .pipe(ofType<workflowActions.RenameGroupAction>(workflowActions.RENAME_WORKFLOW))
    .pipe(withLatestFrom(this.store.pipe(select((state: any) => state.workflowsManaging))))
    .pipe(mergeMap(([data, workflow]) => this.workflowService.renameWorkflow({
      oldName: data.payload.oldName,
      newName: data.payload.newName,
      groupId: workflow.workflowsManaging.currentLevel.id
    }))).pipe(mergeMap(() => [
      new workflowActions.RenameWorkflowCompleteAction(),
      new workflowActions.ListGroupsAction()
    ])).pipe(catchError(error => from([
      new workflowActions.RenameWorkflowErrorAction(''),
      new errorActions.ServerErrorAction(error)
    ])));

  @Effect()
  getExecutionContexts$: Observable<Action> = this.actions$
    .pipe(ofType<workflowActions.RenameGroupAction>(workflowActions.CONFIG_ADVANCED_EXECUTION))
    .pipe(switchMap((action: any) => this.workflowService.getRunParameters(action.payload.workflowId)
      .pipe(map(data => new workflowActions.ConfigAdvancedExecutionCompleteAction({
        data,
        schedule: action.payload.schedule,
        workflowId: action.payload.workflowId
      })))
      .pipe(catchError(error => of(new workflowActions.ConfigAdvancedExecutionErrorAction())))));


  @Effect()
  createScheduledExecution$: Observable<Action> = this.actions$
    .pipe(ofType<workflowActions.RenameGroupAction>(workflowActions.CREATE_SCHEDULED_EXECUTION))
    .pipe(switchMap((action: any) => this.scheduledService.createSheduledExecution(action.config)
      .pipe(map(data => new workflowActions.CreateScheduledExecutionComplete()))
      .pipe(catchError(error => of(new workflowActions.CreateScheduledExecutionError(error))))));

  @Effect()
  moveWorkflow$: Observable<Action> = this.actions$
    .pipe(ofType<workflowActions.RenameGroupAction>(workflowActions.MOVE_WORKFLOW))
    .pipe(withLatestFrom(this.store.pipe(select((state: any) => state.workflowsManaging))))
    .pipe(mergeMap(([data, workflow]) => this.workflowService.moveWorkflow({
      workflowName: data.payload.workflowName,
      groupSourceId: data.payload.groupSourceId,
      groupTargetId: workflow.workflowsManaging.groups.find((group: any) => group.name === data.payload.groupTarget).id
    }).pipe(mergeMap(workflowF => [
      new workflowActions.MoveWorkflowCompleteAction(''),
      new workflowActions.MoveWorkflowGroup(data.payload.workflowName)
    ]))))
    .pipe(catchError(error => from([
      new workflowActions.MoveWorkflowErrorAction(),
      new errorActions.ServerErrorAction(error)
    ])));

  constructor(
    private actions$: Actions,
    private store: Store<fromRoot.State>,
    private workflowService: WorkflowService,
    private scheduledService: ScheduledService,
    private router: Router
  ) { }
}
