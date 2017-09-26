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
    private tasksSubscription: Subscription;
    private modalSubscription: Subscription;
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

    editWorkflow(): void {
        this.route.navigate(['wizard', this.selectedWorkflows[0].id]);
    }

    selectedMenuOption(event: any) {
        if (event.value === 'scratch') {
            this.route.navigate(['wizard']);
        } else {
            this.workflowsService.showCreateJsonModal();
        }
    }


    checkValue($event: any) {
        if ($event.checked) {
            this.store.dispatch(new workflowActions.SelectWorkflowAction($event.value));
        } else {
            this.store.dispatch(new workflowActions.DeselectWorkflowAction($event.value));
        }
    }


    public ngOnDestroy(): void {
        this.workflowListSubscription.unsubscribe();
        this.modalSubscription.unsubscribe();
        clearInterval(this.timer);
        this.store.dispatch(new workflowActions.RemoveWorkflowSelectionAction());
    }
}
