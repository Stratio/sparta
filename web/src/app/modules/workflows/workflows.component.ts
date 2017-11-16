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
    ChangeDetectionStrategy, HostListener
} from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs/Observable';
import { TranslateService } from '@ngx-translate/core';
import { StTableHeader, StModalService, StModalType } from '@stratio/egeo';
import { Subscription } from 'rxjs/Rx';
import { WorkflowListType } from 'app/models/workflow.model';
import { WorkflowsService } from './workflows.service';
import * as workflowActions from 'actions/workflow';
import * as fromRoot from 'reducers';
import { BreadcrumbMenuService } from 'services';
import { ActivatedRoute, Router } from '@angular/router';


@Component({
    styleUrls: ['workflows.styles.scss'],
    templateUrl: 'workflows.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsComponent implements OnInit, OnDestroy {

    @ViewChild('newWorkflowModal', { read: ViewContainerRef }) target: any;

    public title: string;
    public fields: StTableHeader[] = this.workflowsService.getTableFields();
    public workflowList: any = [];
    public showDetails = false;
    public workflowListSubscription: Subscription;
    public selectedWorkflows: any[] = [];
    public selectedWorkflowsIds: string[] = [];
    public breadcrumbOptions: string[] = [];
    public menuOptions: any = [];
    public executionInfo = '';
    public showExecutionInfo = false;

    private tasksSubscription: Subscription;
    private modalSubscription: Subscription;
    private executionInfoSubscription: Subscription;
    private timer: any;

    constructor(private store: Store<fromRoot.State>, private _modalService: StModalService,
        public workflowsService: WorkflowsService,
        private _cd: ChangeDetectorRef, public breadcrumbMenuService: BreadcrumbMenuService,
        private route: Router, private currentActivatedRoute: ActivatedRoute) {
        this.breadcrumbOptions = breadcrumbMenuService.getOptions();
    }

    ngOnInit() {
        this.workflowsService.setModalContainer(this.target);
        this.store.dispatch(new workflowActions.ListWorkflowAction());
        this.updateWorkflowsStatus();
        this.workflowListSubscription = this.store.select(fromRoot.getWorkflowList).subscribe((workflowList: any) => {
            this.workflowList = workflowList;
            this._cd.detectChanges();
        });

        this.store.select(fromRoot.getSelectedWorkflows).subscribe((data: any) => {
            this.selectedWorkflows = data.selected;
            this.selectedWorkflowsIds = data.selectedIds;
        });

        this.modalSubscription = this.store.select(fromRoot.getWorkflowModalState).subscribe(() => {
            this._modalService.close();
        });

        this.executionInfoSubscription = this.store.select(fromRoot.getExecutionInfo).subscribe((executionInfo: any) => {
            this.executionInfo = executionInfo;
            this._cd.detectChanges();
        });

        this.menuOptions = [{
            name: 'New workflow from scratch',
            value: 'scratch'
        },
        {
            name: 'New workflow from json file',
            value: 'file'
        }];
    }

    updateWorkflowsStatus(): void {
        this.timer = setInterval(() => this.store.dispatch(new workflowActions.UpdateWorkflowStatusAction()), 5000);
    }

    downloadWorkflows(): void {
        this.store.dispatch(new workflowActions.DownloadWorkflowsAction(this.selectedWorkflows));
    }

    deleteWorkflows(): void {
        this.workflowsService.deleteWorkflowConfirmModal(this.selectedWorkflows);
    }

    showWorkflowInfo() {
        this.showDetails = !this.showDetails;
    }

    editSelectedWorkflow($event: any, workflowId: string) {
        $event.stopPropagation();
        this.route.navigate(['wizard', workflowId]);
    }

    editWorkflow(): void {
        this.route.navigate(['wizard', this.selectedWorkflows[0].id]);
    }

    changeOrder($event: any): void {
        this.store.dispatch(new workflowActions.ChangeOrderAction({
            orderBy: $event.orderBy,
            sortOrder: $event.type
        }));
    }

    selectedMenuOption(event: any) {
        if (event.value === 'scratch') {
            this.route.navigate(['wizard']);
        } else {
            this.workflowsService.showCreateJsonModal();
        }
    }

    public runWorkflow(workflow: any): void {
        const policyStatus = workflow.context.status;
        if (policyStatus.toLowerCase() !== 'notstarted' && policyStatus.toLowerCase() !== 'failed' &&
            policyStatus.toLowerCase() !== 'stopped' && policyStatus.toLowerCase() !== 'stopping' &&
            policyStatus.toLowerCase() !== 'finished') {
            const stopPolicy = {
                'id': workflow.id,
                'status': 'Stopping'
            };
            this.workflowsService.stopWorkflow(stopPolicy);

        } else {
            this.workflowsService.runWorkflow(workflow.id, workflow.name);
        }
    }

    public showStopButton(policyStatus: string) {
        return policyStatus.toLowerCase() !== 'notstarted' && policyStatus.toLowerCase() !== 'failed' &&
            policyStatus.toLowerCase() !== 'stopped' && policyStatus.toLowerCase() !== 'stopping' &&
            policyStatus.toLowerCase() !== 'finished';
    }

    public showWorkflowExecutionInfo(workflowEvent: any) {
        this.store.dispatch(new workflowActions.GetExecutionInfoAction({id:workflowEvent.id, name: workflowEvent.name}));
    }

    checkRow(isChecked: boolean, value: any) {
        this.checkValue({
            checked: isChecked,
            value: value
        });
    }

    checkValue($event: any) {
        if ($event.checked) {
            this.store.dispatch(new workflowActions.SelectWorkflowAction($event.value));
        } else {
            this.store.dispatch(new workflowActions.DeselectWorkflowAction($event.value));
        }
    }

    hideExecutionInfo() {
        this.showExecutionInfo = false;
    }


    public ngOnDestroy(): void {
        this.workflowListSubscription && this.workflowListSubscription.unsubscribe();
        this.modalSubscription && this.modalSubscription.unsubscribe();
        this.executionInfoSubscription && this.executionInfoSubscription.unsubscribe();
        clearInterval(this.timer);
        this.store.dispatch(new workflowActions.RemoveWorkflowSelectionAction());
    }
}
