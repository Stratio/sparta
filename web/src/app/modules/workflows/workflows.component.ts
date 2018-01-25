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

import {
    Component, ViewChild, ViewContainerRef, OnDestroy, OnInit, ChangeDetectorRef,
    ChangeDetectionStrategy
} from '@angular/core';
import { Store } from '@ngrx/store';
import { StModalService } from '@stratio/egeo';
import { Subscription } from 'rxjs/Rx';

import { WorkflowsService } from './workflows.service';
import * as workflowActions from './actions/workflow-list';
import { State,
        getWorkflowList,
        getSelectedWorkflows,
        getExecutionInfo,
        getWorkflowModalState } from './reducers';


@Component({
    selector: 'sparta-workflows',
    styleUrls: ['workflows.styles.scss'],
    templateUrl: 'workflows.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsComponent implements OnInit, OnDestroy {

    @ViewChild('newWorkflowModal', { read: ViewContainerRef }) target: any;

    public workflowList: any = [];
    public showDetails = false;

    public selectedWorkflows: any[] = [];
    public selectedWorkflowsIds: string[] = [];
    public breadcrumbOptions: string[] = [];
    public menuOptions: any = [];
    public executionInfo = '';
    public showExecutionInfo = false;

    public groupList: Array<any>;
    public currentLevel: string;

    private _modalOpen$: Subscription;
    private _executionInfo$: Subscription;
    private _selectedWorkflows$: Subscription;
    private _groupList$: Subscription;
    private _workflowList$: Subscription;

    private timer: any;

    constructor(private _store: Store<State>,
        private _modalService: StModalService,
        public workflowsService: WorkflowsService,
        private _cd: ChangeDetectorRef) { }

    ngOnInit() {
        this._modalService.container = this.target;

        this._store.dispatch(new workflowActions.ListGroupsAction());
        this._store.dispatch(new workflowActions.ListWorkflowAction());

        this.updateWorkflowsStatus();
        this._workflowList$ = this._store.select(getWorkflowList)
        .distinctUntilChanged()
        .subscribe((workflowList: any) => {
            this.workflowList = workflowList;
            this._cd.detectChanges();
        });

        this._selectedWorkflows$ = this._store.select(getSelectedWorkflows).subscribe((data: any) => {
            this.selectedWorkflows = data.selected;
            this.selectedWorkflowsIds = data.selectedIds;
            this._cd.detectChanges();
        });

        this._modalOpen$ = this._store.select(getWorkflowModalState).subscribe(() => {
            this._modalService.close();
            this._store.dispatch(new workflowActions.ResetJSONModal());
        });

        this._executionInfo$ = this._store.select(getExecutionInfo).subscribe((executionInfo: any) => {
            this.executionInfo = executionInfo;
            this._cd.detectChanges();
        });
    }

    updateWorkflowsStatus(): void {
        this.timer = setInterval(() => this._store.dispatch(new workflowActions.UpdateWorkflowStatusAction()), 5000);
    }

    showWorkflowInfo() {
        this.showDetails = !this.showDetails;
    }

    showWorkflowExecutionInfo(workflowEvent: any) {
        this._store.dispatch(new workflowActions.GetExecutionInfoAction({id: workflowEvent.id, name: workflowEvent.name}));
    }

    hideExecutionInfo() {
        this.showExecutionInfo = false;
    }

    public ngOnDestroy(): void {
        this._workflowList$ && this._workflowList$.unsubscribe();
        this._modalOpen$ && this._modalOpen$.unsubscribe();
        this._executionInfo$ && this._executionInfo$.unsubscribe();
        this._selectedWorkflows$ && this._selectedWorkflows$.unsubscribe();
        this._groupList$ && this._groupList$.unsubscribe();

        clearInterval(this.timer); // stop status requests
        this._store.dispatch(new workflowActions.RemoveWorkflowSelectionAction());
    }
}
