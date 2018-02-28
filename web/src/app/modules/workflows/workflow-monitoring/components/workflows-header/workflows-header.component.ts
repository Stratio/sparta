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
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    Output,
    ViewChild,
    ViewContainerRef,
    OnInit
} from '@angular/core';
import { Router } from '@angular/router';
import { StModalService, StModalResponse, StModalButton } from '@stratio/egeo';

import { WorkflowsService } from './../../workflows.service';
import { BreadcrumbMenuService } from 'services';

@Component({
    selector: 'workflows-header',
    styleUrls: ['workflows-header.component.scss'],
    templateUrl: 'workflows-header.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsHeaderComponent {

    @ViewChild('newWorkflowModal', { read: ViewContainerRef }) target: any;
    @Input() selectedWorkflows: Array<any> = [];
    @Input() showDetails = false;
    @Input() monitoringStatus: any = {};
    @Input() selectedFilter: any = '';
    @Input() searchQuery = '';
    @Input() workflowListLength: number;

    @Output() downloadWorkflows = new EventEmitter<void>();
    @Output() showWorkflowInfo = new EventEmitter<void>();
    @Output() onSelectFilter = new EventEmitter<string>();
    @Output() onSearch = new EventEmitter<string>();

    public breadcrumbOptions: string[] = [];
    public menuOptions: any = [];

    public filters: any = [{
        name: 'workflows',
        label: 'WORKFLOWS',
        value: ''
    },
    {
        name: 'running',
        label: 'RUNNING',
        value: 'Running'
    },
    {
        name: 'starting',
        label: 'STARTING',
        value: 'Starting'
    },
    {
        name: 'stopped',
        label: 'STOPPED',
        value: 'Stopped'
    },
    {
        name: 'failed',
        label: 'FAILED',
        value: 'Failed'
    }];

    constructor(private _modalService: StModalService,
        public workflowsService: WorkflowsService,
        public breadcrumbMenuService: BreadcrumbMenuService,
        private route: Router) {

        this.breadcrumbOptions = ['Home'];
    }

    public runWorkflow(workflow: any): void {
        const policyStatus = workflow.context.status;
        if (this.isRunning(policyStatus)) {
            const stopPolicy = {
                'id': workflow.id,
                'status': 'Stopping'
            };
            this.workflowsService.stopWorkflow(stopPolicy);
        } else {
            this.workflowsService.runWorkflow(workflow.id, workflow.name);
        }
    }

    public isRunning(policyStatus: string) {
        return policyStatus && policyStatus.toLowerCase() !== 'notstarted' && policyStatus.toLowerCase() !== 'failed' &&
            policyStatus.toLowerCase() !== 'stopped' && policyStatus.toLowerCase() !== 'stopping' &&
            policyStatus.toLowerCase() !== 'finished' && policyStatus.toLowerCase() !== 'created';
    }

    public editWorkflow(): void {
        this.route.navigate(['wizard', 'edit', this.selectedWorkflows[0].id]);
    }

    public selectFilter(filter: string): void {
        this.onSelectFilter.emit(filter);
    }

    public searchWorkflow($event: any): void {
        this.onSearch.emit($event.text);
    }

}
