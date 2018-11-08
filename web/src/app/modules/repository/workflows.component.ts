/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
   Component, ViewChild, ViewContainerRef, OnDestroy, OnInit, ChangeDetectorRef, AfterViewInit, ElementRef,
} from '@angular/core';
import { Store } from '@ngrx/store';
import { StModalService } from '@stratio/egeo';

import { Observable, Subscription } from 'rxjs';
import { distinctUntilChanged } from 'rxjs/operators';

import * as workflowActions from './actions/workflow-list';
import {
   State,
   getWorkflowsOrderedList,
   getSelectedWorkflows,
   getGroupsOrderedList,
   getSelectedGroups,
   getSelectedEntity,
   getSelectedVersions,
   getSelectedVersion,
   getLoadingState,
   getVersionsOrderedList,
   getShowExecutionConfig,
   getExecutionContexts,
   getBlockRunButtonState
} from './reducers';
import { WorkflowsManagingService } from './workflows.service';
import { DataDetails } from './models/data-details';
import { GroupWorkflow, Group } from './models/workflows';

@Component({
   selector: 'sparta-manage-workflows',
   styleUrls: ['workflows.styles.scss'],
   templateUrl: 'workflows.template.html'
})

export class WorkflowsManagingComponent implements OnInit, AfterViewInit, OnDestroy {

   @ViewChild('newWorkflowModal', { read: ViewContainerRef }) target: any;
   @ViewChild('rightbarRef', {read: ElementRef}) rightbarRef: ElementRef;
   public workflowList: GroupWorkflow[] = [];
   public showDetails = true;

   public groupsList$: Observable<Array<Group>>;
   public workflowStatuses: any = {};
   public selectedGroupsList$: Observable<Array<string>>;
   public selectedVersions$: Observable<Array<string>>;
   public selectedWorkflows$: Observable<Array<any>>;
   public selectedEntity$: Observable<DataDetails>;
   public workflowVersions$: Observable<Array<GroupWorkflow>>;
   public isLoading$: Observable<boolean>;
   public showExecutionConfig$: Observable<any>;
   public executionContexts$: Observable<any>;
   public blockRunButton$: Observable<boolean>;

   public selectedWorkflowsIds: string[] = [];
   public breadcrumbOptions: string[] = [];
   public menuOptions: any = [];

   public groupList: Array<any>;
   public selectedVersion: DataDetails;

   public sidebarPosition = 0;

   private _modalOpen$: Subscription;
   private _selectedWorkflows$: Subscription;
   private _groupList$: Subscription;
   private _workflowList$: Subscription;
   private _selectedVersion: Subscription;

   private _nodeContainer;

   private timer: any;

   constructor(private _store: Store<State>,
      private _modalService: StModalService,
      public workflowsService: WorkflowsManagingService,
      private _cd: ChangeDetectorRef) {
      this._calculatePosition = this._calculatePosition.bind(this);
   }

   ngOnInit() {
      this._modalService.container = this.target;
      this._store.dispatch(new workflowActions.ListGroupsAction());
      // this._store.dispatch(new workflowActions.ListGroupWorkflowsAction());
      this.blockRunButton$ = this._store.select(getBlockRunButtonState);
      this.showExecutionConfig$ = this._store.select(getShowExecutionConfig);
      this.executionContexts$ = this._store.select(getExecutionContexts);
      this._workflowList$ = this._store.select(getWorkflowsOrderedList)
         .pipe(distinctUntilChanged())
         .subscribe((workflowList: any) => {
            this.workflowList = workflowList;
            this._cd.markForCheck();
         });
      this.isLoading$ = this._store.select(getLoadingState);
      this._selectedVersion = this._store.select(getSelectedVersion).subscribe((selectedVersion) => {
         this.selectedVersion = selectedVersion ? {
            type: 'version',
            data: selectedVersion
         } : null;
         this._cd.markForCheck();
      });

      this.workflowVersions$ = this._store.select(getVersionsOrderedList);
      this.selectedVersions$ = this._store.select(getSelectedVersions);
      this.groupsList$ = this._store.select(getGroupsOrderedList);
      this.selectedGroupsList$ = this._store.select(getSelectedGroups);
      this.selectedWorkflows$ = this._store.select(getSelectedWorkflows);
      this.selectedEntity$ = this._store.select(getSelectedEntity);
      // this.updateWorkflowsStatus();
      this._store.dispatch(new workflowActions.ListGroupWorkflowsAction());
   }

   updateWorkflowsStatus(): void {
      this.timer = setInterval(() => this._store.dispatch(new workflowActions.ListGroupWorkflowsAction()), 5000);
   }

   showWorkflowInfo() {
      this.showDetails = !this.showDetails;
   }

   closeCustomExecution() {
      this._store.dispatch(new workflowActions.CancelAdvancedExecutionAction());
   }

   executeWorkflow(event) {
      this._store.dispatch(new workflowActions.RunWorkflowAction({
         workflowId: this.selectedVersion.data.id,
         workflowName: this.selectedVersion.data.name,
         executionContext: event
      }));
   }

   showExecutionConfiguration(data) {
      this.selectedVersion = { type: 'version', data };
      this._store.dispatch(new workflowActions.ConfigAdvancedExecutionAction(data.id));
   }

   ngAfterViewInit(): void {
      setTimeout(() => {
         this._nodeContainer = this.rightbarRef.nativeElement;
         this._calculatePosition();
         window.addEventListener('resize', this._calculatePosition);
      });
   }

   private _calculatePosition() {
      const rect = this._nodeContainer.getBoundingClientRect();
      this.sidebarPosition = window.innerWidth - rect.left - rect.width - 13;
      this._cd.markForCheck();
   }

   public ngOnDestroy(): void {
      this._workflowList$ && this._workflowList$.unsubscribe();
      this._modalOpen$ && this._modalOpen$.unsubscribe();
      this._selectedWorkflows$ && this._selectedWorkflows$.unsubscribe();
      this._groupList$ && this._groupList$.unsubscribe();
      this._selectedVersion && this._selectedVersion.unsubscribe();
      window.removeEventListener('resize', this._calculatePosition);
      //clearInterval(this.timer); // stop status requests
      this._store.dispatch(new workflowActions.RemoveWorkflowSelectionAction());
   }

}
