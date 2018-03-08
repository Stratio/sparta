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
import { Subscription } from 'rxjs/Rx';

import { WorkflowsService } from './workflows.service';
import * as workflowActions from './actions/workflow-list';
import { State,
        getWorkflowList,
        getSelectedWorkflows,
        getExecutionInfo} from './reducers';


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

        this._store.dispatch(new workflowActions.ListWorkflowAction());

        this.updateWorkflowsStatus();
        this._workflowList$ = this._store.select(getWorkflowList)
        .distinctUntilChanged()
        .subscribe((workflowList: any) => {
            this.workflowList = workflowList;
            this._cd.markForCheck();
        });

        this._selectedWorkflows$ = this._store.select(getSelectedWorkflows).subscribe((data: any) => {
            this.selectedWorkflows = data.selected;
            this.selectedWorkflowsIds = data.selectedIds;
            this._cd.markForCheck();
        });

        this._executionInfo$ = this._store.select(getExecutionInfo).subscribe((executionInfo: any) => {
            this.executionInfo = executionInfo;
            this._cd.markForCheck();
        });
    }

    updateWorkflowsStatus(): void {
        this.timer = setInterval(() => this._store.dispatch(new workflowActions.ListWorkflowAction()), 3000);
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
