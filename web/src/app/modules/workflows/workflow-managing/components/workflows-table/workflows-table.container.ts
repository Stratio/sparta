/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
   ChangeDetectionStrategy,
   Component,
   EventEmitter,
   Input,
   Output,
   OnInit
} from '@angular/core';
import { Store } from '@ngrx/store';
import { Order } from '@stratio/egeo';

import * as workflowActions from './../../actions/workflow-list';
import { State, getVersionsOrderedList } from './../../reducers';
import { Observable } from 'rxjs/Observable';
import { Group } from '../../models/workflows';


@Component({
   selector: 'workflows-manage-table-container',
   template: `
        <workflows-manage-table [workflowList]="workflowList"
            [workflowVersions]="workflowVersions$ | async"
            [selectedGroupsList]="selectedGroupsList"
            [selectedWorkflows]="selectedWorkflows"
            [selectedVersions]="selectedVersions"
            [groupList]="groupList"
            (onChangeOrder)="changeOrder($event)"
            (onChangeOrderVersions)="changeOrderVersions($event)"
            (changeFolder)="changeFolder($event)"
            (openWorkflow)="showWorkflowVersions($event)"
            (onDeleteVersion)="onDeleteVersion($event)"
            (selectWorkflow)="selectWorkflow($event)"
            (onDeleteFolder)="onDeleteFolder($event)"
            (generateVersion)="generateVersion($event)"
            (onDeleteWorkflow)="onDeleteWorkflow($event)"
            (selectGroup)="selectGroup($event)"
            (selectVersion)="selectVersion($event)"
            (onSimpleRun)="simpleRun($event)"
            (showExecutionConfig)="showExecutionConfig($event)"></workflows-manage-table>
    `,
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsManagingTableContainer implements OnInit {

   @Input() selectedWorkflows: Array<string> = [];
   @Input() selectedGroupsList: Array<string> = [];
   @Input() workflowList: Array<any> = [];
   @Input() groupList: Array<any> = [];
   @Input() selectedVersions: Array<string> = [];
   @Input() workflowVersions: Array<any> = [];

   @Output() showWorkflowInfo = new EventEmitter<void>();
   @Output() showExecution = new EventEmitter<any>();



   public workflowVersions$: Observable<Array<any>>;

   ngOnInit(): void {
      this.workflowVersions$ = this._store.select(getVersionsOrderedList);
   }

   changeOrder(event: Order) {
      this._store.dispatch(new workflowActions.ChangeOrderAction(event));
   }

   changeOrderVersions(event: Order) {
      this._store.dispatch(new workflowActions.ChangeVersionsOrderAction(event));
   }

   selectWorkflow(name: string) {
      this._store.dispatch(new workflowActions.SelectWorkflowAction(name));
   }

   selectGroup(name: string) {
      this._store.dispatch(new workflowActions.SelectGroupAction(name));
   }

   selectVersion(id: string) {
      this._store.dispatch(new workflowActions.SelectVersionAction(id));
   }

   changeFolder(event: Group) {
      this._store.dispatch(new workflowActions.ChangeGroupLevelAction(event));
   }

   showWorkflowVersions(workflow: any) {
      this._store.dispatch(new workflowActions.ShowWorkflowVersionsAction({
         name: workflow.name,
         group: workflow.group
      }));
   }

   onDeleteFolder(folderId: string) {
      this._store.dispatch(new workflowActions.DeleteSingleGroupAction(folderId));
   }

   onDeleteWorkflow(workflowName: string) {
      this._store.dispatch(new workflowActions.DeleteSingleWorkflowAction(workflowName));
   }

   onDeleteVersion(versionId: string) {
      this._store.dispatch(new workflowActions.DeleteSingleVersionAction(versionId));
   }

   generateVersion(versionId: string): void {
      this._store.dispatch(new workflowActions.GenerateNewVersionAction(versionId));
   }

   showExecutionConfig(version: any) {
      this.showExecution.emit(version);

   }

   simpleRun(version: any) {
      this._store.dispatch(new workflowActions.RunWorkflowAction(version));
   }

   constructor(private _store: Store<State>) { }

}
