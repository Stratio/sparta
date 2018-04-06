/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
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
    getWorkflowsOrderedList,
    getSelectedWorkflows,
    getExecutionInfo,
    getGroupsOrderedList,
    getSelectedGroups,
    getSelectedEntity,
    getSelectedVersions,
    getSelectedVersion,
    getLoadingState,
    getVersionsOrderedList
} from './reducers';
import { WorkflowsManagingService } from './workflows.service';
import { DataDetails } from './models/data-details';
import { GroupWorkflow, Group } from './models/workflows';


@Component({
    selector: 'sparta-manage-workflows',
    styleUrls: ['workflows.styles.scss'],
    templateUrl: 'workflows.template.html'
})

export class WorkflowsManagingComponent implements OnInit, OnDestroy {

    @ViewChild('newWorkflowModal', { read: ViewContainerRef }) target: any;

    public workflowList: GroupWorkflow[] = [];
    public showDetails = false;

    public groupsList$: Observable<Array<Group>>;
    public workflowStatuses: any = {};
    public selectedGroupsList$: Observable<Array<string>>;
    public selectedVersions$: Observable<Array<string>>;
    public selectedWorkflows$: Observable<Array<any>>;
    public selectedEntity$: Observable<DataDetails>;
    public workflowVersions$: Observable<Array<GroupWorkflow>>;
    public isLoading$: Observable<boolean>;

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

        this._workflowList$ = this._store.select(getWorkflowsOrderedList)
            .distinctUntilChanged()
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


        this._executionInfo$ = this._store.select(getExecutionInfo).subscribe((executionInfo: any) => {
            this.executionInfo = executionInfo;
            this._cd.markForCheck();
        });

        this.workflowVersions$ = this._store.select(getVersionsOrderedList);
        this.selectedVersions$ = this._store.select(getSelectedVersions);
        this.groupsList$ = this._store.select(getGroupsOrderedList);
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
