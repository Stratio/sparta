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
import { Observable, Subscription } from 'rxjs/Rx';

import * as workflowActions from './actions/workflow-list';
import {
    State,
    getWorkflowList,
    getSelectedWorkflows,
    getExecutionInfo,
    getGroupsList,
    getSelectedGroups,
    getSelectedEntity,
    getSelectedVersions,
    getSelectedVersion,
    getWorkflowVersions
} from './reducers';
import { WorkflowsManagingService } from './workflows.service';
import { DataDetails } from './models/data-details';


@Component({
    selector: 'sparta-manage-workflows',
    styleUrls: ['workflows.styles.scss'],
    templateUrl: 'workflows.template.html'
})

export class WorkflowsManagingComponent implements OnInit, OnDestroy {

    @ViewChild('newWorkflowModal', { read: ViewContainerRef }) target: any;

    public workflowList: any = [];
    public showDetails = false;

    public groupsList$: Observable<Array<any>>;
    public workflowStatuses: any = {};
    public selectedGroupsList$: Observable<Array<string>>;
    public selectedVersions$: Observable<Array<string>>;
    public selectedWorkflows$: Observable<Array<any>>;
    public selectedEntity$: Observable<DataDetails>;
    public workflowVersions$: Observable<Array<any>>;

    public selectedWorkflowsIds: string[] = [];
    public breadcrumbOptions: string[] = [];
    public menuOptions: any = [];
    public executionInfo = '';
    public showExecutionInfo = false;

    public groupList: Array<any>;
    public currentLevel: string;
    public selectedVersion: DataDetails;

    private _modalOpen$: Subscription;
    private _executionInfo$: Subscription;
    private _selectedWorkflows$: Subscription;
    private _groupList$: Subscription;
    private _workflowList$: Subscription;
    private _selectedVersion: Subscription;

    private timer: any;

    constructor(private _store: Store<State>,
        private _modalService: StModalService,
        public workflowsService: WorkflowsManagingService,
        private _cd: ChangeDetectorRef) { }

    ngOnInit() {
        this._modalService.container = this.target;
        this._store.dispatch(new workflowActions.ListGroupsAction());
        // this._store.dispatch(new workflowActions.ListGroupWorkflowsAction());

        this._workflowList$ = this._store.select(getWorkflowList)
            .distinctUntilChanged()
            .subscribe((workflowList: any) => {
                this.workflowList = workflowList;
                this._cd.detectChanges();
            });

        this._selectedVersion = this._store.select(getSelectedVersion).subscribe((selectedVersion) => {
            this.selectedVersion = selectedVersion ? {
                type: 'version',
                data: selectedVersion
            } : null;
            this._cd.detectChanges();
        });


        this._executionInfo$ = this._store.select(getExecutionInfo).subscribe((executionInfo: any) => {
            this.executionInfo = executionInfo;
            this._cd.detectChanges();
        });

        this.workflowVersions$ = this._store.select(getWorkflowVersions);
        this.selectedVersions$ = this._store.select(getSelectedVersions);
        this.groupsList$ = this._store.select(getGroupsList);
        this.selectedGroupsList$ = this._store.select(getSelectedGroups);
        this.selectedWorkflows$ = this._store.select(getSelectedWorkflows);
        this.selectedEntity$ = this._store.select(getSelectedEntity);
        this.updateWorkflowsStatus();


    }

    updateWorkflowsStatus(): void {
        this.timer = setInterval(() => this._store.dispatch(new workflowActions.ListGroupWorkflowsAction()), 5000);
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

    public ngOnDestroy(): void {
        this._workflowList$ && this._workflowList$.unsubscribe();
        this._modalOpen$ && this._modalOpen$.unsubscribe();
        this._executionInfo$ && this._executionInfo$.unsubscribe();
        this._selectedWorkflows$ && this._selectedWorkflows$.unsubscribe();
        this._groupList$ && this._groupList$.unsubscribe();
        this._selectedVersion && this._selectedVersion.unsubscribe();

        clearInterval(this.timer); // stop status requests
        this._store.dispatch(new workflowActions.RemoveWorkflowSelectionAction());
    }
}
