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
    OnChanges
} from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
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

    @Output() downloadWorkflows = new EventEmitter<void>();
    @Output() showWorkflowInfo = new EventEmitter<void>();
    @Output() onDeleteWorkflows = new EventEmitter<any>();

    public breadcrumbOptions: string[] = [];
    public menuOptions: any = [];

    public deleteWorkflowModalTitle: string;
    public deleteWorkflowModalMessage: string;
    public messageDeleteTitle: string;

    constructor(private _modalService: StModalService,
        private translate: TranslateService,
        public workflowsService: WorkflowsService,
        public breadcrumbMenuService: BreadcrumbMenuService,
        private route: Router) {

        this.breadcrumbOptions = breadcrumbMenuService.getOptions();
        this.menuOptions = [

            {
                name: 'New workflow from json file',
                value: 'file'
            },
            {
                name: 'New workflow from scratch',
                value: 'scratch',
                /* subMenus: [
                     {
                         name: 'Streaming',
                         value: 'streaming'
                     },
                     {
                         name: 'Batch',
                         value: 'batch'
                     }
                 ]*/
            }];
        const deleteWorkflowModalTitle = 'DASHBOARD.DELETE_WORKFLOW_TITLE';
        const deleteWorkflowModalMessage = 'DASHBOARD.DELETE_WORKFLOW_MESSAGE';
        const messageDeleteTitle = 'DASHBOARD.MESSAGE_DELETE_TITLE';

        this.translate.get([deleteWorkflowModalTitle, deleteWorkflowModalMessage, messageDeleteTitle]).subscribe(
            (value: { [key: string]: string }) => {
                this.deleteWorkflowModalTitle = value[deleteWorkflowModalTitle];
                this.deleteWorkflowModalMessage = value[deleteWorkflowModalMessage];
                this.messageDeleteTitle = value[messageDeleteTitle];
            });
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
        return policyStatus.toLowerCase() !== 'notstarted' && policyStatus.toLowerCase() !== 'failed' &&
            policyStatus.toLowerCase() !== 'stopped' && policyStatus.toLowerCase() !== 'stopping' &&
            policyStatus.toLowerCase() !== 'finished';
    }

    public editWorkflow(): void {
        this.route.navigate(['wizard', 'edit', this.selectedWorkflows[0].id]);
    }

    public deleteWorkflows(): void {
        this.deleteWorkflowConfirmModal(this.selectedWorkflows);
    }

    public deleteWorkflowConfirmModal(workflows: Array<any>): void {
        const buttons: StModalButton[] = [
            { label: 'Cancel', classes: 'button-secondary-gray', responseValue: StModalResponse.NO },
            { label: 'Delete', classes: 'button-critical', responseValue: StModalResponse.YES, closeOnClick: true }
        ];
        this._modalService.show({
            messageTitle: this.deleteWorkflowModalMessage,
            modalTitle: this.deleteWorkflowModalTitle,
            buttons: buttons,
            maxWidth: 500,
            message: this.messageDeleteTitle,
        }).subscribe((response: any) => {
            if (response === 1) {
                this._modalService.close();
            } else if (response === 0) {
                this.onDeleteWorkflows.emit(workflows);
            }
        });
    }

    public selectedMenuOption(event: any) {
        switch (event.value) {
            case 'streaming':
                this.route.navigate(['wizard', 'streaming']);
                break;
            case 'batch':
                this.route.navigate(['wizard', 'batch']);
                break;
            case 'group':
                break;
            case 'scratch':
                this.route.navigate(['wizard', 'streaming']);
                break;
            case 'file':
                this.workflowsService.showCreateJsonModal();
                break;
        }
    }
}
