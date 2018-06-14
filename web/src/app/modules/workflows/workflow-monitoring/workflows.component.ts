/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
   ChangeDetectionStrategy,
   ChangeDetectorRef,
   Component,
   OnDestroy,
   OnInit,
   ViewChild,
   ViewContainerRef
} from '@angular/core';
import { Store } from '@ngrx/store';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/distinctUntilChanged';
import { Subscription } from 'rxjs/Subscription';
import { StModalService } from '@stratio/egeo';

import * as workflowActions from './actions/workflows';
import {
   State,
   getWorkflowList,
   getSelectedWorkflows,
   getExecutionInfo,
   showInitialMode
} from './reducers';
import { MonitoringWorkflow } from './models/workflow';
import { ExecutionInfo } from './models/execution-info';
import { WorkflowJsonModal } from '@app/workflows/workflow-managing';


@Component({
   selector: 'sparta-workflows',
   styleUrls: ['workflows.styles.scss'],
   templateUrl: 'workflows.template.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsComponent implements OnInit, OnDestroy {

   @ViewChild('newWorkflowModalMonitoring', { read: ViewContainerRef }) target: any;

   public workflowList: Array<MonitoringWorkflow> = [];
   public showDetails = false;

   public selectedWorkflows: Array<MonitoringWorkflow> = [];
   public selectedWorkflowsIds: string[] = [];
   public executionInfo: ExecutionInfo;
   public showExecutionInfo = false;
   public showInitialMode: Observable<boolean>;

   private _modalOpen: Subscription;
   private _executionInfo: Subscription;
   private _selectedWorkflows: Subscription;
   private _groupList: Subscription;
   private _workflowList: Subscription;

   private timer;

   constructor(private _store: Store<State>,
      private _route: Router,
      private _cd: ChangeDetectorRef,
      private _modalService: StModalService) { }

   ngOnInit() {
      this._modalService.container = this.target;
      this._store.dispatch(new workflowActions.ListWorkflowAction());
      this.updateWorkflowsStatus();
      this._workflowList = this._store.select(getWorkflowList)
         .distinctUntilChanged()
         .subscribe((workflowList: Array<MonitoringWorkflow>) => {
            this.workflowList = workflowList;
            this._cd.markForCheck();
         });

      this._selectedWorkflows = this._store.select(getSelectedWorkflows).subscribe((data: any) => {
         this.selectedWorkflows = data.selected;
         this.selectedWorkflowsIds = data.selectedIds;
         this._cd.markForCheck();
      });

      this._executionInfo = this._store.select(getExecutionInfo).subscribe(executionInfo => {
         this.executionInfo = executionInfo;
         this._cd.markForCheck();
      });

      this.showInitialMode = this._store.select(showInitialMode);
   }

   updateWorkflowsStatus(): void {
      this.timer = setInterval(() => this._store.dispatch(new workflowActions.ListWorkflowAction()), 3000);
   }

   showWorkflowInfo() {
      this.showDetails = !this.showDetails;
   }

   showWorkflowExecutionInfo(workflowEvent: any) {
      this._store.dispatch(new workflowActions.GetExecutionInfoAction({ id: workflowEvent.id, name: workflowEvent.name }));
   }

   hideExecutionInfo() {
      this.showExecutionInfo = false;
   }

   createWorkflow(creationType: string) {
      switch (creationType) {
         case 'streaming':
            this._route.navigate(['wizard/streaming']);
            break;
         case 'batch':
            this._route.navigate(['wizard/batch']);
            break;
         case 'json':
            this._showCreateJsonModal();
            break;
      }
   }

   private _showCreateJsonModal(): void {
      this._modalService.show({
         modalTitle: 'Configuration from JSON',
         maxWidth: 980,
         outputs: {
            onCloseJsonModal: this._onCloseJsonModal.bind(this)
         },
      }, WorkflowJsonModal);
   }

   private _onCloseJsonModal(action: any) {
      this._modalService.close();
      this._store.dispatch(new workflowActions.ListWorkflowAction());
   }

   public ngOnDestroy(): void {
      this._workflowList && this._workflowList.unsubscribe();
      this._modalOpen && this._modalOpen.unsubscribe();
      this._executionInfo && this._executionInfo.unsubscribe();
      this._selectedWorkflows && this._selectedWorkflows.unsubscribe();
      this._groupList && this._groupList.unsubscribe();

      clearInterval(this.timer); // stop status requests
      this._store.dispatch(new workflowActions.RemoveWorkflowSelectionAction());
   }
}
