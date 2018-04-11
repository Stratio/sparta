/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
    Component, OnDestroy, OnInit, ChangeDetectorRef,
    ChangeDetectionStrategy
} from '@angular/core';
import { Store } from '@ngrx/store';

import 'rxjs/add/operator/distinctUntilChanged';
import { Subscription } from 'rxjs/Subscription';

import * as workflowActions from './actions/workflows';
import { State,
        getWorkflowList,
        getSelectedWorkflows,
        getExecutionInfo
} from './reducers';
import { MonitoringWorkflow } from './models/workflow';
import { ExecutionInfo } from './models/execution-info';


@Component({
    selector: 'sparta-workflows',
    styleUrls: ['workflows.styles.scss'],
    templateUrl: 'workflows.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsComponent implements OnInit, OnDestroy {

    public workflowList: Array<MonitoringWorkflow> = [];
    public showDetails = false;

    public selectedWorkflows: Array<MonitoringWorkflow> = [];
    public selectedWorkflowsIds: string[] = [];
    public executionInfo: ExecutionInfo;
    public showExecutionInfo = false;

    private _modalOpen: Subscription;
    private _executionInfo: Subscription;
    private _selectedWorkflows: Subscription;
    private _groupList: Subscription;
    private _workflowList: Subscription;

    private timer;

    constructor(private _store: Store<State>,
        private _cd: ChangeDetectorRef) { }

    ngOnInit() {
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
        this._workflowList && this._workflowList.unsubscribe();
        this._modalOpen && this._modalOpen.unsubscribe();
        this._executionInfo && this._executionInfo.unsubscribe();
        this._selectedWorkflows && this._selectedWorkflows.unsubscribe();
        this._groupList  && this._groupList.unsubscribe();

        clearInterval(this.timer); // stop status requests
        this._store.dispatch(new workflowActions.RemoveWorkflowSelectionAction());
    }
}
